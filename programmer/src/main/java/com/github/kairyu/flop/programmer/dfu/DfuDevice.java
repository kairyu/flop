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
import com.github.kairyu.flop.programmer.exception.DeviceNotInitializedException;

import java.nio.ByteBuffer;

import org.usb4java.Device;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsbException;

/**
 * @author Kai Ryu
 *
 */
public class DfuDevice extends Dfu {

    private final Log log = Log.getLog(Dfu.class.getPackage().getName(),
            DEBUG_THRESHOLD, TRACE_THRESHOLD, MSG_DEBUG_THRESHOLD);

    private DeviceHandle handle;
    private DfuStatus status = null;
    private byte iface;
    private short transaction = 0;

    public DfuDevice() {
        this.handle = new DeviceHandle();
        this.iface = 0;
    }

    public DfuDevice(final DeviceHandle handle, final byte iface) {
        this.handle = handle;
        this.iface = iface;
    }

    public DeviceHandle getHandle() {
        return this.handle;
    }
    public byte getInterface() {
        return this.iface;
    }

    public int detach(final int timeout) throws DfuException {
        log.trace("dfu_detach( %s, %d )", this.handle.hashCode(), timeout);

        if ((!this.isInitialized()) || (timeout < 0)) {
            log.debug("Invalid parameter");
            throw new DeviceNotInitializedException();
        }

        int result = this.transferOut(DfuCommand.DETACH, timeout, null);

        super.msgResponseOutput(result);

        return result;
    }

    public int download(final ByteBuffer data) throws DfuException {
        data.rewind();
        log.trace("dfu_download( %s, %d, %s )", this.handle.hashCode(), data.capacity(), data);

        if (!this.isInitialized()) {
            log.debug("Invalid parameter");
            throw new DeviceNotInitializedException();
        }

        while (data.hasRemaining()) {
            log.msgDebug("Message: m[%d] = 0x%02x", data.position(), data.get());
        }
        data.rewind();

        int result = this.transferOut(DfuCommand.DNLOAD, transaction++, data);

        super.msgResponseOutput(result);

        return result;
    }

    public int upload(final ByteBuffer data) throws DfuException {
        data.rewind();
        log.trace("dfu_upload( %s, %d, %s )", this.handle.hashCode(), data.capacity(), data);

        if (!this.isInitialized()) {
            log.debug("Invalid parameter");
            throw new DeviceNotInitializedException();
        }

        int result = this.transferIn(DfuCommand.UPLOAD, transaction++, data);

        super.msgResponseOutput(result);

        return result;
    }

    private int getStatus(DfuStatus status) throws DfuException {
        log.trace("dfu_get_status( %s, %s )", this.handle.hashCode(), status.hashCode());

        if (!this.isInitialized()) {
            log.debug("Invalid parameter");
            throw new DeviceNotInitializedException();
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(DfuStatus.getBufferSize());
        int result = this.transferIn(DfuCommand.GETSTATUS, 0, buffer);

        if (result == buffer.capacity()) {
            status.parse(buffer);
        }
        else {
            if (result > 0) {
                log.debug("result: %d", result);
                return -2;
            }
        }

        return 0;
    }

    public int updateStatus() throws DfuException {
        this.status = null;
        this.status = new DfuStatus();
        return getStatus(this.status);
    }

    public DfuStatus.Status getStatus() {
        return this.status.getStatus();
    }

    public boolean isStatus(final DfuStatus.Status status) {
        return (this.status.getStatus() == status);
    }

    public boolean isStatusOK() {
        return this.isStatus(DfuStatus.Status.OK);
    }

    public boolean isState(final DfuStatus.State state) {
        return (this.status.getState() == state);
    }

    public boolean isStateError() {
        return this.isState(DfuStatus.State.DFU_ERROR);
    }

    public int clearStatus() throws DfuException {
        log.trace("dfu_clear_status( %s )", this.handle.hashCode());

        if (!this.isInitialized()) {
            log.debug("Invalid parameter");
            throw new DeviceNotInitializedException();
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(0);
        int result = this.transferOut(DfuCommand.CLRSTATUS, 0, buffer);

        super.msgResponseOutput(result);

        this.status = null;

        return result;
    }

    public int getState() throws DfuException {
        log.trace("dfu_get_state( %s )", this.handle.hashCode());

        if (!this.isInitialized()) {
            log.debug("Invalid parameter");
            throw new DeviceNotInitializedException();
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(1);
        int result = this.transferIn(DfuCommand.GETSTATE, 0, buffer);

        super.msgResponseOutput(result);

        if (result < 1) {
            return result;
        }

        return buffer.get();
    }

    public int abort() throws DfuException {
        log.trace("dfu_abort( %s )", this.handle.hashCode());

        if (!this.isInitialized()) {
            log.debug("Invalid parameter");
            throw new DeviceNotInitializedException();
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(0);
        int result = this.transferIn(DfuCommand.ABORT, 0, buffer);

        super.msgResponseOutput(result);

        return result;
    }

    public Device initDevice(
            final int vendor,
            final int product,
            final int busNumber,
            final int deviceAddress,
            final boolean honorInterfaceClass,
            final boolean initialAbort)
            throws LibUsbException, DfuException {
        Device device = null;
        try {
            byte[] iface = { 0 };
            device = super.initDevice(vendor, product, busNumber, deviceAddress,
                    honorInterfaceClass, initialAbort, this.handle, iface);
            this.iface = iface[0];
        }
        catch (LibUsbException e) {
            throw e;
        }
        catch (DfuException e) {
            throw e;
        }

        return device;
    }

    public void uninitDevice() throws DfuException {
        super.uninitDevice(this.getHandle(), this.getInterface());
    }

    public boolean isInitialized() {
        return (this.getHandle().getPointer() != 0);
    }

    protected int makeIdle(final boolean initialAbort) throws DfuException {
        int retries = 4;

        if (initialAbort) {
            this.abort();
        }

        while (retries > 0) {
            DfuStatus status = new DfuStatus();
            if (this.getStatus(status) != 0) {
                this.clearStatus();
                continue;
            }

            log.debug("State: %s (%d)", status.getState(), status.getState().getValue());

            switch (status.getState()) {
                case DFU_IDLE:
                    if (status.getStatus() == DfuStatus.Status.OK) {
                        return 0;
                    }
                    this.clearStatus();
                    break;

                case DFU_DOWNLOAD_SYNC:
                case DFU_DOWNLOAD_IDLE:
                case DFU_UPLOAD_IDLE:
                case DFU_MANIFEST_SYNC:
                case DFU_DOWNLOAD_BUSY:
                case DFU_MANIFEST:
                    this.abort();
                    break;

                case DFU_ERROR:
                    this.clearStatus();
                    break;

                case APP_IDLE:
                    this.detach(DFU_DETACH_TIMEOUT);
                    break;

                case APP_DETACH:
                case DFU_MANIFEST_WAIT_RESET:
                    log.debug("Resetting the device");
                    this.reset();
                    return 1;

                default:
                    break;
            }

            retries--;
        }

        log.debug("Not able to transition the device into the dfuIDLE state.");
        return -2;
    }

    public void reset() {
        resetDevice(this.getHandle());
    }

    public int transferOut(final DfuCommand command, final int value, final ByteBuffer data) {
        return transferOut(this.getHandle(), command.getValue(), (short) value, this.getInterface(), data);
    }

    public int transferIn(final DfuCommand command, final int value, final ByteBuffer data) {
        return transferIn(this.getHandle(), command.getValue(), (short) value, this.getInterface(), data);
    }
}
