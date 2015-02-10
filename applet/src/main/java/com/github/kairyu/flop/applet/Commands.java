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

package com.github.kairyu.flop.applet;

import com.github.kairyu.flop.programmer.Log;
import com.github.kairyu.flop.programmer.atmel.*;
import com.github.kairyu.flop.programmer.command.Command;

import com.github.kairyu.flop.programmer.command.Get;
import com.github.kairyu.flop.programmer.exception.ControllerErrorException;
import org.usb4java.Device;
import org.usb4java.LibUsbException;

/**
 * @author Kai Ryu
 *
 */
public class Commands extends Thread implements Runnable {

    private static final Log log = Log.getLog(Commands.class.getPackage().getName(), 40);

    private final int delay = 100;
    private Target target = Target.none;
    private AtmelDevice device;
    private boolean running;
    private int retval;
    private boolean quiet = false;
    private boolean setDebug;
    private int debug;
    private Command command = Command.none;
    private String getName;
    private String hex;
    private String segmentName;
    private boolean force;
    private boolean validate;
    private boolean reset;

    @Override
    public void run() {
        this.running = true;

        while (this.running) {
            try {
                if (this.setDebug) {
                    this.executeSetDebug(this.debug);
                    this.setDebug = false;
                }
                else if (this.command != Command.none) {
                    this.retval = this.executeCommand();
                }

                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                this.retval = Error.EXCEPTION_OCCURRED;
                this.setDebug = false;
                this.command = Command.none;
            }
        }
    }

    public boolean init() {
        try {
            this.device = new AtmelDevice();
            this.device.init();
            return true;
        }
        catch (LibUsbException e) {
            System.err.println("can't init libusb.");
            return false;
        }
    }

    public void uninit() {
        this.device.uninit();
    }

    public void setQuiet(final boolean quiet) {
        this.quiet = quiet;
    }

    public void setDebug(final int debug) {
        this.debug = debug;
        this.setDebug = true;
    }

    public int setTarget(final String name) {
        try {
            this.target = Target.getByName(name);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Error.ARGUMENT_ERROR;
        }

        return Error.SUCCESS;
    }

    public int get(final String name) {
        this.getName = name;
        this.command = Command.get;
        while (this.command != Command.none) {
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.retval;
    }

    public int erase(final boolean force, final boolean validate) {
        this.force = force;
        this.validate = validate;
        this.command = Command.erase;
        while (this.command != Command.none) {
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.retval;
    }

    public int flash(final String hex, final String segment, final boolean force, final boolean validate) {
        this.hex = hex;
        this.segmentName = segment;
        this.force = force;
        this.validate = validate;
        this.command = Command.flash;
        while (this.command != Command.none) {
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.retval;
    }

    public int launch(final boolean reset) {
        this.reset = reset;
        this.command = Command.launch;
        while (this.command != Command.none) {
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.retval;
    }

    private int initDevice() {
        uninitDevice();
        Device result = this.device.initDevice(
                this.target.getVendorId(),
                this.target.getChipId(),
                0, 0, false, // this.target.getHonorInterfaceClass(),
                this.target.getInitialAbort());
        if (result == null) {
            return -1;
        }

        this.device.setType(this.target.getDeviceType());
        return 0;
    }

    private void uninitDevice() {
        if (this.device.isInitialized()) {
            this.device.uninitDevice();
        }
    }

    private int executeCommand() throws Exception {
        if (initDevice() != 0) {
            return Error.NO_DEVICE_PRESENT;
        }
        try {
            switch (this.command) {
                case get:
                    return this.executeGet(this.getName);
                case erase:
                    return this.executeErase(this.force, this.validate);
                case flash:
                    return this.executeFlash(this.hex, this.segmentName, this.force, this.validate);
                case launch:
                    return this.executeLaunch(this.reset);
                default:
                    return Error.SUCCESS;
            }
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            uninitDevice();
            this.command = Command.none;
        }
    }

    private void executeSetDebug(final int debug) {
        Log.setGlobalDebug(debug);
    }

    private int executeGet(final String name) {
        if (this.device == null || !this.device.isInitialized()) {
            return Error.INVALID_DEVICE;
        }

        Get get;
        try {
            get = Get.getByName(name);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Error.ARGUMENT_ERROR;
        }

        DeviceType type = this.device.getType();
        DeviceInfo info = new DeviceInfo(type);

        this.device.checkSecurity();

        int status = this.device.readConfig(info);

        if (status != 0) {
            log.debug("Error reading %s config information.", type);
            System.err.println(String.format("Error reading %s config information.", type));
            this.device.securityMessage();
            return Error.UNSPECIFIED_ERROR;
        }

        String message = get.getDescription();
        short value = 0;

        try {
            value = info.getInfo(get);
        }
        catch (ControllerErrorException e) {
            log.debug(e.getMessage());
            System.err.println(String.format("%s requires 8051 based controller", message));
            return Error.UNSPECIFIED_ERROR;
        }

        if (value < 0) {
            System.err.println("The requested device info is unavailable.");
            return Error.UNSPECIFIED_ERROR;
        }

        System.out.println(String.format("%s: 0x%02x (%d)", message, value, value));
        return value;
    }

    private int executeErase(final boolean force, final boolean validate) {
        if (this.device == null || !this.device.isInitialized()) {
            return Error.INVALID_DEVICE;
        }

        final int start = this.target.getFlashAddressBottom();
        final int end = this.target.getFlashAddressTop();

        if (!force) {
            if (this.device.checkBlank(start, end, this.quiet) == 0) {
                System.err.println("Chip already blank, to force erase user --force.");
                return Error.ERASE_ALREADY_BLANK;
            }
        }

        log.debug("erase 0x%X bytes.", end - start);

        if (device.eraseFlash(EraseMode.ERASE_BLOCK_ALL, this.quiet) != 0) {
            return Error.ERASE_ERROR;
        }

        if (validate) {
            if (device.checkBlank(start, end, this.quiet) != 0) {
                return Error.ERASE_VALIDATION_ERROR;
            }
        }

        return Error.SUCCESS;
    }

    private int executeFlash(final String hex, final String segment, final boolean force, final boolean validate) {
        if (this.device == null || !this.device.isInitialized()) {
            return Error.INVALID_DEVICE;
        }

        MemoryUnit memoryType;
        try {
            memoryType = MemoryUnit.getByName(segment);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Error.ARGUMENT_ERROR;
        }

        int memorySize;
        int pageSize;
        int targetOffset = 0;
        switch (memoryType) {
            case flash:
                memorySize = this.target.getMemorySize();
                pageSize = this.target.getFlashPageSize();
                break;
            case eeprom:
                if (this.target.getEepromMemorySize() == 0) {
                    System.err.println("This device has no eeprom.");
                    return Error.ARGUMENT_ERROR;
                }
                memorySize = this.target.getEepromMemorySize();
                pageSize = this.target.getEepromPageSize();
                break;
            case user:
                if (!this.device.getType().isType(DeviceType.ADC_AVR32)) {
                    System.err.println("Flash User only implemented for ADC_AVR32 devices.");
                    return Error.ARGUMENT_ERROR;
                }
                memorySize = this.target.getFlashPageSize();
                pageSize = this.target.getFlashPageSize();
                targetOffset = AtmelDevice.USER_PAGE_OFFSET;
                break;
            default:
                memorySize = 0;
                pageSize = 0;
                break;
        }

        // ---------- CONVERT HEX FILE TO BINARY ----------
        BufferOut bufferOut = new BufferOut();
        if (bufferOut.init(memorySize, pageSize, targetOffset) != 0) {
            log.debug("ERROR initializing a buffer.");
            return Error.BUFFER_INIT_ERROR;
        }

        int result = bufferOut.readHexString(hex, this.quiet);
        if (result < 0) {
            log.debug("Something went wrong with creating the memory image.");
            return Error.BUFFER_INIT_ERROR;
        }
        else if (result > 0) {
            log.debug("WARNING: File contains 0x%X bytes outside target memory.", result);
            if (memoryType == MemoryUnit.flash) {
                log.debug("There may be data in the user page (offset %#X).", AtmelDevice.USER_PAGE_OFFSET);
                log.debug("Inspect the hex file or try flash-user.");
            }
            if (!quiet) {
                System.err.println(String.format("WARNING: 0x%X bytes are outside target memory,", result));
                System.err.println(" and will not be written.");
            }
        }

        // TODO: implement serialize_memory_image

        if (memoryType == MemoryUnit.flash) {
            bufferOut.getValidRange().set(this.target.getFlashAddressBottom(), this.target.getFlashAddressTop());

            final boolean suppressBootloader = true; // TODO:
            for (int i = this.target.getBootloaderBottom(); i <= this.target.getBootloaderTop(); i++) {
                if (bufferOut.isDataValid(i)) {
                    if (suppressBootloader) {
                        bufferOut.setDataInvalid(i);
                    } else {
                        System.err.println("Bootloader and code overlap.");
                        System.err.println("Use --suppress-bootloader-mem to ignore");
                        return Error.BUFFER_INIT_ERROR;
                    }
                }
            }
        }
        else if (memoryType == MemoryUnit.user) {
            if (!bufferOut.hasData()) {
                System.err.println("ERROR: NO data to write into the user page.");
                return Error.BUFFER_INIT_ERROR;
            }
            else {
                log.debug("Hex file contains %d bytes to write.", bufferOut.getDataLength());
            }

            if (!force) {
                System.err.println("ERROR: --force flag is required to write user page.");
                System.err.println(" Last word(S) in user page contain configuration data.");
                System.err.println(" The user page is erased whenever any data is written.");
                System.err.println(" Without valid config. device always resets in bootloader.");
                System.err.println(" User dump-user to obtain valid configuration words.");
                return Error.ARGUMENT_ERROR;
            }

            // TODO: implement checking data overlap with bootloader
        }

        // ---------- WRITE PROGRAM DATA ----------
        if (memoryType == MemoryUnit.user) {
            result = this.device.user(bufferOut);
        }
        else {
            result = this.device.flash(bufferOut, (memoryType == MemoryUnit.eeprom), force, this.quiet);
        }
        if (result != 0) {
            log.debug("Error writing %s data. (err %d)", "memory", result);
            return Error.FLASH_WRITE_ERROR;
        }

        // ---------- VALIDATE PROGRAM ----------
        if (validate) {
            result = this.executeValidate(bufferOut, memoryType, this.quiet);
            if (result != 0) {
                System.err.println("Memory did not validate. Did you erase?");
                return result;
            }
            else {
                if (!this.quiet) {
                    printFlashUsage(bufferOut);
                }
            }
        }
        else {
            if (!this.quiet) {
                printFlashUsage(bufferOut);
            }
        }

        return Error.SUCCESS;
    }

    private int executeValidate(final BufferOut bufferOut, final MemoryUnit memorySegment, final boolean quiet) {
        BufferIn bufferIn = new BufferIn();
        if (bufferIn.init(bufferOut) != 0) {
            log.debug("ERROR initializing a buffer.");
        }
        bufferIn.getDataRange().set(bufferOut.getValidRange());

        int retval = Error.UNSPECIFIED_ERROR;
        try {
            int result = this.device.readFlash(bufferIn, memorySegment, quiet);
            if (result != 0) {
                log.debug("ERROR: could not read memory, err %d.", result);
                retval = Error.FLASH_READ_ERROR;
                return retval;
            }

            result = this.device.validateBuffer(bufferIn, bufferOut, quiet);
            if (result != 0) {
                if (result < 0) {
                    retval = Error.VALIDATION_ERROR_IN_REGION;
                }
                else {
                    retval = Error.VALIDATION_ERROR_OUTSIDE_REGION;
                }
                return retval;
            }

            retval = Error.SUCCESS;
        }
        finally {
            if (!quiet && retval != Error.SUCCESS) {
                System.err.println("FAIL");
            }
        }

        return retval;
    }

    private static void printFlashUsage(final AtmelBuffer buffer) {
        System.err.println(String.format("0x%X bytes written into 0x%X bytes memory (%.02f%%).",
                buffer.getDataLength(), buffer.getValidLength(), buffer.getUsage() * 100.0));
    }

    private int executeLaunch(final boolean reset) {
        if (reset) {
            return this.device.startAppReset();
        }
        else {
            return this.device.startAppNoReset();
        }
    }

}
