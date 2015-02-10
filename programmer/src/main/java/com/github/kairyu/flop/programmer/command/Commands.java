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

package com.github.kairyu.flop.programmer.command;

import com.github.kairyu.flop.programmer.Log;
import com.github.kairyu.flop.programmer.Error;
import com.github.kairyu.flop.programmer.Arguments;
import com.github.kairyu.flop.programmer.atmel.AtmelBuffer;
import com.github.kairyu.flop.programmer.atmel.AtmelDevice;
import com.github.kairyu.flop.programmer.atmel.BufferIn;
import com.github.kairyu.flop.programmer.atmel.BufferOut;
import com.github.kairyu.flop.programmer.atmel.DeviceInfo;
import com.github.kairyu.flop.programmer.atmel.DeviceType;
import com.github.kairyu.flop.programmer.atmel.EraseMode;
import com.github.kairyu.flop.programmer.atmel.MemoryUnit;
import com.github.kairyu.flop.programmer.exception.ControllerErrorException;

/**
 * @author Kai Ryu
 *
 */
public class Commands {

    private static final Log log = Log.getLog(Commands.class.getPackage().getName(), 40);

    private static int executeErase(final AtmelDevice device, final Arguments args) {
        final boolean quiet = args.getQuiet();
        final int start = args.getFlashAddressBottom();
        final int end = args.getFlashAddressTop();

        if (!args.getEraseForce()) {
            if (device.checkBlank(start, end, quiet) == 0) {
                if (!quiet) {
                    System.err.println("Chip already blank, to force erase user --force.");
                }
                return 0;
            }
        }

        log.debug("erase 0x%X bytes.", end - start);

        int result = device.eraseFlash(EraseMode.ERASE_BLOCK_ALL, quiet);
        if (result != 0) {
            return result;
        }

        if (!args.getEraseSuppressValidation()) {
            result = device.checkBlank(start, end, quiet);
        }

        return result;
    }

    private static int executeSecure(final AtmelDevice device, final Arguments args) {
        return 0;
    }

    private static int serializeMemoryImage(final BufferOut bufferOut, final Arguments args) {
        return 0;
    }

    private static int executeValidate(final AtmelDevice device, final BufferOut bufferOut,
            final MemoryUnit memorySegment, final boolean quiet) {
        BufferIn bufferIn = new BufferIn();
        if (bufferIn.init(bufferOut) != 0) {
            log.debug("ERROR initializing a buffer.");
        }
        bufferIn.getDataRange().set(bufferOut.getValidRange());

        int retval = Error.UNSPECIFIED_ERROR;
        try {
            int result = device.readFlash(bufferIn, memorySegment, quiet);
            if (result != 0) {
                log.debug("ERROR: could not read memory, err %d.", result);
                retval = Error.FLASH_READ_ERROR;
                return retval;
            }

            result = device.validateBuffer(bufferIn, bufferOut, quiet);
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

    private static int executeFlash(final AtmelDevice device, final Arguments args) {
        int memorySize;
        int pageSize;
        int targetOffset = 0;
        MemoryUnit memoryType = args.getFlashSegment();

        switch (memoryType) {
            case flash:
                memorySize = args.getMemoryAddressTop() + 1;
                pageSize = args.getFlashPageSize();
                break;
            case eeprom:
                if (args.getEepromMemorySize() == 0) {
                    System.err.println("This device has no eeprom.");
                    return -1;
                }
                memorySize = args.getEepromMemorySize();
                pageSize = args.getEepromPageSize();
                break;
            case user:
                if (!device.getType().isType(DeviceType.ADC_AVR32)) {
                    System.err.println("Flash User only implemented for ADC_AVR32 devices.");
                    return Error.ARGUMENT_ERROR;
                }
                memorySize = args.getFlashPageSize();
                pageSize = args.getFlashPageSize();
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

        final boolean quiet = args.getQuiet();
        int result = bufferOut.readHexFile(args.getFlashFileName(), quiet);
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
            bufferOut.getValidRange().set(args.getFlashAddressBottom(), args.getFlashAddressTop());

            final boolean suppressBootloader = args.getFlashSuppressBootloaderMem();
            for (int i = args.getBootloaderBottom(); i <= args.getBootloaderTop(); i++) {
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

            if (!args.getFlashForce()) {
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
            result = device.user(bufferOut);
        }
        else {
            result = device.flash(bufferOut,
                    (memoryType == MemoryUnit.eeprom),
                    args.getFlashForce(), quiet);
        }
        if (result != 0) {
            log.debug("Error writing %s data. (err %d)", "memory", result);
            return Error.FLASH_WRITE_ERROR;
        }

        // ---------- VALIDATE PROGRAM ----------
        if (!args.getFlashSuppressValidation()) {
            result = executeValidate(device, bufferOut, memoryType, quiet);
            if (result != 0) {
                System.err.println("Memory did not validate. Did you erase?");
                return result;
            }
            else {
                if (!quiet) {
                    printFlashUsage(bufferOut);
                }
            }
        }
        else {
            if (!quiet) {
                printFlashUsage(bufferOut);
            }
        }

        return Error.SUCCESS;
    }

    private static int executeGet(final AtmelDevice device, final Arguments args) {
        DeviceType type = device.getType();
        DeviceInfo info = new DeviceInfo(type);

        device.checkSecurity();

        int status = device.readConfig(info);

        if (status != 0) {
            log.debug("Error reading %s config information.", args.getDeviceTypeName());
            System.err.println(String.format("Error reading %s config information.", args.getDeviceTypeName()));
            device.securityMessage();
            return status;
        }

        Get get = args.getGetName();
        String message = get.getDescription();
        short value = 0;

        try {
            value = info.getInfo(get);
        }
        catch (ControllerErrorException e) {
            log.debug(e.getMessage());
            System.err.println(String.format("%s requires 8051 based controller", message));
            return -1;
        }

        if (value < 0) {
            System.err.println("The requested device info is unavailable.");
            return -2;
        }

        boolean quiet = args.getQuiet();
        System.out.println(String.format("%s0x%02x (%d)",
                (!quiet ? message + ": " : ""),
                value, value));
        return 0;
    }

    public static int executeLaunch(final AtmelDevice device, final Arguments args) {
        if (args.getLaunchNoReset()) {
            return device.startAppNoReset();
        }
        else {
            return device.startAppReset();
        }
    }

    public static int executeCommand(final AtmelDevice device, final Arguments args) {
        device.setType(args.getDeviceType());
        switch (args.getCommand()) {
            case erase:
                return executeErase(device, args);
            case flash:
                return executeFlash(device, args);
            //case eflash:
            //case user:
            case start_app:
            case reset:
            case launch:
                return executeLaunch(device, args);
            case get:
                return executeGet(device, args);
            //case getfuse:
            //case dump:
            //case edump:
            //case udump:
            //case read:
            //case configure:
            //case setfuse:
            //case setsecure:
            default:
                System.err.println("Not supported at this time");
                break;
        }

        return -1;
    }

}
