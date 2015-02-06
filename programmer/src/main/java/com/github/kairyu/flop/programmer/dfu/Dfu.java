/*
 * Copyright (C) 2015  Kai Ryu <kai1103@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.kairyu.flop.programmer.dfu;

import com.github.kairyu.flop.programmer.Log;
import com.github.kairyu.flop.programmer.exception.DfuException;

import java.nio.ByteBuffer;

import org.usb4java.DeviceHandle;
import org.usb4java.Interface;
import org.usb4java.InterfaceDescriptor;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceList;
import org.usb4java.DeviceDescriptor;
import org.usb4java.ConfigDescriptor;

/**
 * @author Kai Ryu
 *
 */
public abstract class Dfu {

    private final Log log = Log.getLog(Dfu.class.getPackage().getName(),
            DEBUG_THRESHOLD, TRACE_THRESHOLD, MSG_DEBUG_THRESHOLD);

    protected static final int DEBUG_THRESHOLD     = 100;
    protected static final int TRACE_THRESHOLD     = 200;
    protected static final int MSG_DEBUG_THRESHOLD = 300;

    private static final byte USB_CLASS_APP_SPECIFIC = (byte)0xfe;
    private static final byte DFU_SUBCLASS           = (byte)0x01;

    private static final long DFU_TIMEOUT = 20000;
    protected static final int DFU_DETACH_TIMEOUT = 1000;

    private Context context = new Context();

    public int init() throws LibUsbException {
        int result = LibUsb.init(context);
        if (result < 0) {
            throw new LibUsbException("Unable to initialize libusb", result);
        }
        return result;
    }

    public void uninit() {
        LibUsb.exit(this.context);
    }

    public void setDebug(final int level) {
        LibUsb.setDebug(this.context, level);
    }

    public Device initDevice(
            final int vendor,
            final int product,
            final int busNumber,
            final int deviceAddress,
            final boolean honorInterfaceClass,
            final boolean initialAbort,
            DeviceHandle handle,
            byte[] iface)
            throws LibUsbException, DfuException {
        log.trace("dfu_init_device( %d, %d, %s, %b, %b )", vendor, product, handle.hashCode(),
                honorInterfaceClass, initialAbort);
        log.debug("dfu_init_device( 0x%08x, 0x%08x )", vendor, product);

        int retries = 4;

        while (retries > 0) {
            DeviceList list = new DeviceList();
            int result = LibUsb.getDeviceList(this.context, list);
            if (result < 0) {
                throw new LibUsbException("Unable to get device list", result);
            }

            try {
                int i = 0;
                for (Device device: list) {
                    i++;
                    DeviceDescriptor descriptor = new DeviceDescriptor();
                    result = LibUsb.getDeviceDescriptor(device, descriptor);
                    if (result < 0) {
                        log.debug("failed in LibUsb.getDeviceDescriptor");
                        throw new LibUsbException("Unable to read device descriptor", result);
                    }

                    log.debug("%2d: 0x%04x, 0x%04x", i, descriptor.idVendor(), descriptor.idProduct());

                    if ((vendor == descriptor.idVendor()) &&
                        (product == descriptor.idProduct()) &&
                        ((busNumber == 0) ||
                            (busNumber == LibUsb.getBusNumber(device)) &&
                            (deviceAddress == LibUsb.getDeviceAddress(device))
                        )) {
                        log.debug("found device at USB:%d,%d",
                                LibUsb.getBusNumber(device), LibUsb.getDeviceAddress(device));

                        // We found a device that looks like it matches...
                        // let's try to find the DFU interface, open the device and claim it
                        try {
                            iface[0] = findInterface(device, honorInterfaceClass, descriptor.bNumConfigurations());
                            // The interface is valid
                            if (iface[0] >= 0) {
                                // open
                                result = LibUsb.open(device, handle);
                                if (result != LibUsb.SUCCESS) {
                                    throw new LibUsbException("Unable to open device", result);
                                }
                                log.debug("opened interface %d...", iface[0]);

                                try {
                                    // set configuration
                                    result = LibUsb.setConfiguration(handle, 1);
                                    if (result != LibUsb.SUCCESS) {
                                        log.debug("Failed to set configuration.");
                                        throw new LibUsbException("Unable to set configuration", result);
                                    }
                                    log.debug("set configuration %d...", 1);
                                    // claim interface
                                    result = LibUsb.claimInterface(handle, iface[0]);
                                    if (result != LibUsb.SUCCESS) {
                                        log.debug("Failed to claim the DFU interface.");
                                        throw new LibUsbException("Unable to claim the DFU interface", result);
                                    }
                                    log.debug("claimed interface %d...", iface[0]);

                                    result = this.makeIdle(initialAbort);
                                    if (result == 0) {
                                        return device;
                                    }
                                    else if (result == 1) {
                                        retries--;
                                        break;
                                    }

                                    log.debug("Failed to put the device in dfuIDLE mode.");
                                    LibUsb.releaseInterface(handle, iface[0]);
                                    retries = 4;
                                    throw new DfuException("Failed to put the device in dfuIDLE mode.");
                                }
                                catch (LibUsbException e) {
                                    LibUsb.close(handle);
                                }
                                catch (DfuException e) {
                                    LibUsb.close(handle);
                                }
                            }
                        }
                        catch (LibUsbException e) {
                            throw e;
                        }
                    }
                }
            }
            finally {
                LibUsb.freeDeviceList(list, true);
            }

            break;
        }

        return null;
    }

    public void uninitDevice(DeviceHandle handle, byte iface) throws LibUsbException {
        int result = LibUsb.releaseInterface(handle, iface);
        if (result < 0) {
            System.err.println(String.format("failed to release interface %d.", iface));
            throw new LibUsbException("Failed to release interface", result);
        }
        LibUsb.close(handle);
    }

    private byte findInterface(
            final Device device,
            final boolean honorInterfaceClass,
            final int bNumConfigurations)
            throws LibUsbException {
        log.trace("dfu_find_interface()");

        // Loop through all of the configurations
        for (byte c = 0; c < bNumConfigurations; c++) {
            ConfigDescriptor config = new ConfigDescriptor();
            try {
                int result = LibUsb.getConfigDescriptor(device, c, config);
                if (result != LibUsb.SUCCESS) {
                    log.debug("can't get_config_descriptor: %d", c);
                    throw new LibUsbException("Unable to get config descriptor", result);
                }
                log.debug("config %d: maxpower=%d*2 mA", c, config.bMaxPower());

                // Loop through all of the interfaces
                for (int i = 0; i < config.bNumInterfaces(); i++) {
                    Interface iface = config.iface()[i];
                    log.debug("interface %d", i);

                    // Loop through all of the settings
                    for (int s = 0; s < iface.numAltsetting(); s++) {
                        InterfaceDescriptor setting = iface.altsetting()[s];
                        log.debug("setting %d: class:%02x, subclass:%02x, protocol:%02x", s,
                                0xff & setting.bInterfaceClass(), 0xff & setting.bInterfaceSubClass(),
                                0xff & setting.bInterfaceProtocol());

                        if (honorInterfaceClass) {
                            // Check if the interface is a DFU interface
                            if ((setting.bInterfaceClass() == USB_CLASS_APP_SPECIFIC) &&
                                (setting.bInterfaceSubClass() == DFU_SUBCLASS)) {
                                log.debug("Found DFU interface: %d", setting.bInterfaceNumber());
                                return setting.bInterfaceNumber();
                            }
                        }
                        else {
                            // If there is a bug in the DFU firmware, return the first found interface
                            log.debug("Found DFU interface: %d", setting.bInterfaceNumber());
                            return setting.bInterfaceNumber();
                        }
                    }
                }
            }
            finally {
                LibUsb.freeConfigDescriptor(config);
            }
        }

        return -1;
    }

    protected abstract int makeIdle(final boolean initialAbort);

    protected static int transferOut(
            final DeviceHandle handle,
            final byte request,
            final short value,
            final short iface,
            final ByteBuffer data) {
        return LibUsb.controlTransfer(
                handle,
                (byte)(LibUsb.ENDPOINT_OUT | LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE),
                request,
                value,
                iface,
                data,
                DFU_TIMEOUT);
    }

    protected static int transferIn(
            final DeviceHandle handle,
            final byte request,
            final short value,
            final short iface,
            final ByteBuffer data) {
        return LibUsb.controlTransfer(
                handle,
                (byte)(LibUsb.ENDPOINT_IN | LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE),
                request,
                value,
                iface,
                data,
                DFU_TIMEOUT);
    }

    public static void resetDevice(final DeviceHandle handle) {
        LibUsb.resetDevice(handle);
    }

    public void msgResponseOutput(final int result) {
        String msg = null;
        if (result >= 0) {
            msg = "No error.";
        }
        else {
            switch (result) {
                case -Errno.ENOENT:
                    msg = "-ENOENT: URB was canceled by ulink_urb";
                    break;
                case -Errno.EINPROGRESS:
                    msg = "-INPROGRESS: URB still pending, no results yet "
                            + "(actually no error until now)";
                    break;
                case -Errno.EPROTO:
                    msg = "-EPROTO: a) Bitstuff error or b) Unknown USB error";
                    break;
                case -Errno.EILSEQ:
                    msg = "-EILSEQ: CRC mismatch";
                    break;
                case -Errno.EPIPE:
                    msg = "-EPIPE: a) Babble detect or b) Endpoint stalled";
                    break;
                case -Errno.ETIMEDOUT:
                    msg = "-ETIMEDOUT: Transfer timed out, NAK";
                    break;
                case -Errno.ENODEV:
                    msg = "-ENODEV: Device was removed";
                    break;
                case -Errno.EIO:
                    msg = "-EIO: Usb I/O error";
                    break;
                case -Errno.EREMOTEIO:
                    msg = "-EREMOTEIO: Short packet detected";
                    break;
                case -Errno.EXDEV:
                    msg = "-EXDEV: ISO transfer only partially completed look at "
                            + "individual frame status for details";
                    break;
                case -Errno.EINVAL:
                    msg = "-EINVAL: ISO madness, if this happens: Log off and go home";
                    break;
                default:
                    msg = "Unknown error";
                    break;
            }

            log.debug("%s 0x%08x (%d)", msg, result, result);
            //throw new DfuException(msg, result);
        }
    }

    public static void test() {
        Context context = new Context();
        int result = LibUsb.init(context);
        DeviceList list = new DeviceList();
        result = LibUsb.getDeviceList(context, list);

        try {
            for (Device device: list) {
                int address = LibUsb.getDeviceAddress(device);
                int busNumber = LibUsb.getBusNumber(device);
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                System.out.format("Bus %03d, Device %03d, Vendor %04x, Product %04x%n",
                        busNumber, address, descriptor.idVendor(), descriptor.idProduct());
            }
        }
        finally {
            LibUsb.freeDeviceList(list, true);
        }

        LibUsb.exit(context);
    }

}
