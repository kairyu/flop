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

package com.github.kairyu.flop.programmer;

import com.github.kairyu.flop.programmer.atmel.AtmelDevice;
import com.github.kairyu.flop.programmer.command.Commands;
import com.github.kairyu.flop.programmer.exception.DfuException;

import org.usb4java.Device;
import org.usb4java.LibUsbException;

/**
 * @author Kai Ryu
 *
 */
public class Main {

    public static void main(String[] args) {

        int retval = Error.SUCCESS;

        Arguments arguments = new Arguments();
        final int status = arguments.parseArguments(args);
        if (status < 0) {
            System.exit(Error.ARGUMENT_ERROR);
        }
        else if (status > 0) {
            System.exit(Error.SUCCESS);
        }

        final int debug = arguments.getDebug();
        Log.setGlobalDebug(debug);

        AtmelDevice device = new AtmelDevice();
        try {
            device.init();
        }
        catch (LibUsbException e) {
            System.err.println("can't init libusb.");
            System.exit(Error.DEVICE_ACCESS_ERROR);
        }

        if (debug >= 200) {
            device.setDebug(debug);
        }

        int bus_number = 0;
        int device_address = 0;
        try {
            Device result = device.initDevice(
                    arguments.getVendorId(),
                    arguments.getProductId(),
                    bus_number,
                    device_address,
                    arguments.getHonorInterfaceClass(),
                    arguments.getInitialAbort());
            if (result == null) {
                System.err.println("flop: no device present");
                retval = Error.DEVICE_ACCESS_ERROR;
            }
            else {
                retval = Commands.executeCommand(device, arguments);
            }
        }
        catch (LibUsbException e) {
            System.err.println(e.getMessage());
        }
        catch (DfuException e) {
            System.err.println(e.getMessage());
        }
        finally {
            try {
                if (device.isInitialized()) {
                    device.uninitDevice();
                }
            }
            catch (Exception e) {
                retval = Error.DEVICE_ACCESS_ERROR;
            }
            device.uninit();
        }

        System.exit(retval);

    }

}
