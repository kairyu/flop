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

package com.github.kairyu.flop.programmer.atmel;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.github.kairyu.flop.programmer.Log;
import com.github.kairyu.flop.programmer.command.Get;
import com.github.kairyu.flop.programmer.dfu.DfuDevice;
import com.github.kairyu.flop.programmer.exception.ControllerErrorException;
import com.github.kairyu.flop.programmer.exception.DeviceNotInitializedException;
import com.github.kairyu.flop.programmer.dfu.DfuStatus;
import com.github.kairyu.flop.programmer.dfu.Errno;

public class AtmelDevice extends DfuDevice {

    private final Log log = Log.getLog(AtmelDevice.class.getPackage().getName(),
            DEBUG_THRESHOLD, TRACE_THRESHOLD);

    public static final int USER_PAGE_OFFSET = 0x80800000;
    private static final int DEBUG_THRESHOLD = 50;
    private static final int TRACE_THRESHOLD = 55;
    private static final int PAGE_SIZE       = 0x10000;
    private static final int AVR32_CONTROL_BLOCK_SIZE = 64;
    private static final int CONTROL_BLOCK_SIZE       = 32;
    private static final String PROGRESS_METER = "0%                            100%  ";
    private static final String PROGRESS_START = "[";
    private static final String PROGRESS_BAR   = ">";
    private static final String PROGRESS_END   = "]  ";
    private static final String PROGRESS_ERROR = " X  ";

    private DeviceType type;
    private SecurityBit securityBitState;
    private long progress;

    public DeviceType getType() {
        return this.type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public void securityMessage() {
        if (this.securityBitState != SecurityBit.SECURE_OFF) {
            System.err.println(String.format("The security bit %s set", this.securityBitState.getVerb()));
            System.err.println(String.format("Erase the device to clear temporarily."));
        }
    }

    public void checkSecurity() {
        if (this.type.isType(DeviceType.ADC_AVR32)) {
            this.securityBitState = this.getSecure();
            log.debug("Security bit check returned %d", this.securityBitState);
        }
        else {
            this.securityBitState = SecurityBit.SECURE_OFF;
        }
    }

    private short readCommand(final byte[] cmd) {
        log.trace("atmel_read_command( %s, 0x%02x, 0x%02x )", this.getHandle().hashCode(), cmd[0], cmd[1]);

        if (!this.isInitialized()) {
            log.debug("invalid arguments.");
            throw new DeviceNotInitializedException();
        }

        if (this.type.isType(DeviceType.GRP_AVR32)) {
            // TODO: implement for avr32
            return 0;
        }
        else {
            final ByteBuffer command = ByteBuffer.allocateDirect(3)
                    .put((byte)0x05)
                    .put(cmd);

            if (this.download(command) != command.capacity()) {
                log.debug("dfu_download failed");
                return -1;
            }

            if (this.updateStatus() != 0) {
                log.debug("dfu_get_status failed");
                return -2;
            }

            if (!this.isStatusOK()) {
                log.debug("status(%s) was not OK.", this.getStatus());
                this.clearStatus();
                return -3;
            }

            ByteBuffer data = ByteBuffer.allocateDirect(1);
            if (this.upload(data) != data.capacity()) {
                log.debug("dfu_upload failed");
                return -4;
            }

            return (short)(0xff & data.get());
        }
    }

    private void printProgress(final AtmelBuffer buffer) {
        if (Log.getGlobalDebug() <= DEBUG_THRESHOLD) {
            while (buffer.getBlockOffset() * 32 > this.progress) {
                System.err.print(PROGRESS_BAR);
                this.progress += buffer.getDataLength();
            }
        }
    }

    public int validateBuffer(final BufferIn bufferIn, final BufferOut bufferOut, final boolean quiet) {
        log.debug("Validating image from byte %s", bufferOut.getValidRange());

        int invalidDataRegion = 0;
        int invalidOutsideDataRegion = 0;

        if (!quiet) {
            System.err.println("validating...  ");
        }
        for (int i: bufferOut.getValidRange().getArray()) {
            if (bufferOut.isDataValid(i)) {
                if (bufferOut.getData(i) != bufferIn.getData(i)) {
                    if (invalidDataRegion == 0) {
                        if (!quiet) {
                            System.err.println("ERROR");
                        }
                        log.debug("Image did not validate as byte: 0x%X of 0x%X.", i, bufferOut.getValidLength());
                        log.debug("Wanted 0x%02x but read 0x%02x.", bufferOut.getData(i), bufferIn.getData(i));
                        log.debug("suppressing additional warnings.");
                    }
                    invalidDataRegion++;
                }
            }
            else {
                if (bufferIn.getData(i) != BufferIn.BYTE_MAX) {
                    if (invalidOutsideDataRegion == 0) {
                        // TODO: debug message
                        log.debug("Outside program region: byte 0x%X expected 0xFF.", i);
                        log.debug("but read 0x%02X.  suppressing additional warnings.", bufferIn.getData(i));
                    }
                    invalidOutsideDataRegion++;
                }
            }
        }

        if (!quiet) {
            if (invalidDataRegion + invalidOutsideDataRegion == 0) {
                System.err.println("Success");
            }
            else {
                System.err.println(String.format("%d invalid bytes in program region, %d outside region.",
                        invalidDataRegion, invalidOutsideDataRegion));
            }
        }

        return invalidDataRegion > 0 ? -invalidDataRegion : invalidOutsideDataRegion;
    }

    public int readFuse(final Avr32Fuses info) {
        return 0;
    }

    public int readConfig(final DeviceInfo info) {
        log.trace("atmel_read_config( %s, %s )", this.getHandle().hashCode(), info.hashCode());

        if (!this.isInitialized()) {
            log.debug("invalid arguments.");
            throw new DeviceNotInitializedException();
        }

        int retval = 0;

        for (Get get: Get.values()) {
            byte[] command = get.getCommand(this.type);
            if (command != null) {
                short result = this.readCommand(command);
                if (result < 0) {
                    retval = result;
                }
                try {
                    info.setInfo(get, result);
                }
                catch (ControllerErrorException e) {
                    log.debug(e.getMessage());
                }
            }
        }

        return retval;
    }

    public int eraseFlash(final EraseMode mode, final boolean quiet) {
        log.trace("atmel_erase_flash( %s, %s )", this.getHandle().hashCode(), mode);

        ByteBuffer command = ByteBuffer.allocateDirect(3)
                .put(new byte[] { 0x04, 0x00 })
                .put(mode.getCommand());

        if (!quiet) {
            System.err.print("Erasing flash...  ");
            // TODO: debug message
            if (Log.getGlobalDebug() > DEBUG_THRESHOLD) {
                System.err.println();
            }
        }
        if (this.download(command) != command.capacity()) {
            if (!quiet) {
                System.err.println("ERROR");
            }
            log.debug("dfu_download failed");
            return -2;
        }

        final long ERASE_SECONDS = 20;
        long start = System.currentTimeMillis();
        int retries = 0;
        do {
            if (this.updateStatus() == 0) {
                if (this.isStatus(DfuStatus.Status.ERROR_NOTDONE) &&
                        this.isState(DfuStatus.State.DFU_DOWNLOAD_BUSY)) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    if (!quiet) {
                        System.err.println("Success");
                    }
                    log.debug("CMD_ERASE status: Erase Done.");
                    return this.getStatus().getValue();
                }
            }
            else {
                this.clearStatus();
                retries++;
                if (!quiet) {
                    System.err.println("ERROR");
                }
                log.debug("CMD_ERASE status check %d returned nonzero.", retries);
            }
            if ((System.currentTimeMillis() - start) / 1000 > ERASE_SECONDS) {
                break;
            }
        }
        while (retries < 10);

        if (retries < 10) {
            log.debug("CMD_ERASE time limit %ds exceeded.", 20);
        }

        return -3;
    }

    public int setFuse(final byte property, final int value) {
        return 0;
    }

    public int setConfig(final byte property, final byte value) {
        return 0;
    }

    private int readBlock(final BufferIn bufferIn, boolean eeprom) {
        log.trace("__atmel_read_block( %s, %s, %b )", this.getHandle().hashCode(), bufferIn.hashCode(), eeprom);

        if (!this.isInitialized() || !bufferIn.isInitialized()) {
            log.debug("ERROR: Invalid arguments, device/buffer pointer is NULL.");
            throw new DeviceNotInitializedException();
        }
        else if (!bufferIn.getBlockRange().isValid()) {
            log.debug("ERROR: start address is after end address.");
            return -1;
        }
        else if (bufferIn.getBlockLength() > BufferOut.MAX_TRANSFER_SIZE) {
            log.debug("ERROR: transfer size must not exceed %d", BufferOut.MAX_TRANSFER_SIZE);
            return -1;
        }

        ByteBuffer command = ByteBuffer.allocateDirect(6).order(ByteOrder.BIG_ENDIAN).put((byte) 0x03);

        if (eeprom && this.type.isType(DeviceType.GRP_AVR)) {
            command.put((byte)0x02);
        }
        else {
            command.put((byte)0x00);
        }

        command.putShort((short)bufferIn.getBlockRange().getStart());
        command.putShort((short)bufferIn.getBlockRange().getEnd());

        if (this.download(command) != command.capacity()) {
            log.debug("dfu_download failed");
            return -1;
        }

        ByteBuffer block = ByteBuffer.allocateDirect(bufferIn.getBlockLength());
        int result = this.upload(block);
        if (result < 0) {
            log.debug("dfu_upload result: %d", result);
            if (this.updateStatus() == 0) {
                if (this.isStatus(DfuStatus.Status.ERROR_FILE)) {
                    System.err.println("The device is read protected.");
                }
                else {
                    System.err.println("Unknown error. Try enabling debug.");
                }
            }
            else {
                System.err.println("Device is unresponsive.");
            }
            this.clearStatus();

            return result;
        }

        bufferIn.putBlock(block);

        return 0;
    }

    public int readFlash(final BufferIn bufferIn, final MemoryUnit memorySegment, final boolean quiet) {
        log.trace("atmel_read_flash( %s, %s, %s, %b )", this.getHandle().hashCode(), bufferIn.hashCode(), memorySegment, quiet);

        if (!this.isInitialized()) {
            log.debug("invalid arguments.");
            if (!quiet) {
                System.err.println("Program Error, use debug for more info.");
            }
            throw new DeviceNotInitializedException();
        }
        else if (memorySegment != MemoryUnit.flash &&
                memorySegment != MemoryUnit.user &&
                memorySegment != MemoryUnit.eeprom) {
            log.debug("Invalid memory segment %s to read.", memorySegment);
            if (!quiet) {
                System.err.println("Program Error, use debug for more info.");
            }
            return -1;
        }

        if (this.selectMemoryUnit(memorySegment) != 0) {
            log.debug("Error selecting memory unit.");
            if (!quiet) {
                System.err.println("Memory access error, use debug for more info.");
            }
        }

        if (!quiet) {
            if (Log.getGlobalDebug() <= DEBUG_THRESHOLD) {
                System.err.print(PROGRESS_METER);
            }
            System.err.println(String.format("Reading 0x%X bytes...", bufferIn.getDataLength()));
            if (Log.getGlobalDebug() <= DEBUG_THRESHOLD) {
                System.err.print(PROGRESS_START);
            }
        }

        int retval = 0;
        int result;
        this.progress = 0;
        short memoryPage = -1;
        bufferIn.rewindBlock();

        try {
            while (bufferIn.hasRemainingBlock()) {
                if (memoryPage != bufferIn.getBlockPage()) {
                    memoryPage = bufferIn.getBlockPage();
                    result = this.selectPage(memoryPage);
                    if (result != 0) {
                        log.debug("ERROR selecting 64kB page %d.", result);
                        retval = -3;
                        return retval;
                    }
                }

                result = this.readBlock(bufferIn, (memorySegment == MemoryUnit.eeprom));
                if (result != 0) {
                    log.debug("Error reading block %s: err %d.", bufferIn.getBlockRange(), result);
                    retval = -5;
                    return retval;
                }

                if (!quiet) {
                    printProgress(bufferIn);
                }
            }
        }
        finally {
            if (!quiet) {
                if (retval == 0) {
                    if (Log.getGlobalDebug() <= DEBUG_THRESHOLD) {
                        System.err.print(PROGRESS_END);
                    }
                    System.err.println("Success");
                }
                else {
                    if (Log.getGlobalDebug() <= DEBUG_THRESHOLD) {
                        System.err.print(PROGRESS_ERROR);
                    }
                    System.err.println("ERROR");
                    if (retval == -3) {
                        System.err.println("Memory access error, use debug for more info.");
                    }
                    else if (retval == -5) {
                        System.err.println("Memory read error, use debug for more info.");
                    }
                }
            }
        }

        return 0;
    }

    private int checkBlankPage(final int start, final int end) {
        log.trace("__atmel_blank_page_check( %s, 0x%08x, 0x%08x )", this.getHandle().hashCode(), start, end);

        if (!this.isInitialized()) {
            log.debug("ERROR: Invalid arguments, device pointer is NULL.");
            throw new DeviceNotInitializedException();
        }
        else if (start > end) {
            log.debug("ERROR: End address 0x%X before start address 0x%X.", end, start);
            return -1;
        }
        else if (end >= PAGE_SIZE) {
            log.debug("ERROR: Address 0x%X out of 64kb (0x10000) byte page range.", end);
            return -1;
        }

        ByteBuffer command = ByteBuffer.allocateDirect(6).order(ByteOrder.BIG_ENDIAN)
                .put(new byte[]{0x03, 0x01})
                .putShort((short) start)
                .putShort((short) end);

        if (this.download(command) != command.capacity()) {
            log.debug("__atmel_blank_page_check DFU_DNLOAD failed.");
            return -2;
        }

        if (this.updateStatus() != 0) {
            log.debug("__atmel_blank_page_check DFU_GETSTATUS failed.");
            return -3;
        }

        if (this.isStatusOK()) {
            log.debug("Flash region from 0x%X to 0x%X is blank.", start, end);
        }
        else if (this.isStatus(DfuStatus.Status.ERROR_CHECK_ERASED)) {
            log.debug("Region is NOT blank.");
            // TODO: bug of dfu-programmer?
            if (this.isStateError()) {
                this.clearStatus();
            }
            ByteBuffer addr = ByteBuffer.allocateDirect(2).order(ByteOrder.BIG_ENDIAN);
            if (this.upload(addr) != addr.capacity()) {
                log.debug("__atmel_blank_page_check DFU_UPLOAD failed.");
                return -4;
            }
            else {
                int retval = addr.getShort();
                log.debug(" First non-blank address in region is 0x%X.", retval);
                return retval + 1;
            }
        }
        else {
            log.debug("Error: status (%s) was not OK.", this.getStatus());
            if (this.isStateError()) {
                this.clearStatus();
            }
            return -4;
        }

        return 0;
    }

    public int checkBlank(final AtmelRange range, final boolean quiet) {
        return checkBlank(range.getStart(), range.getEnd(), quiet);
    }

    public int checkBlank(final int start, final int end, final boolean quiet) {
        log.trace("atmel_blank_check( %s, 0x%08X, 0x%08X )", this.getHandle().hashCode(), start, end);

        if (!this.isInitialized()) {
            log.debug("ERROR: Invalid arguments, device pointer is NULL.");
            throw new DeviceNotInitializedException();
        }
        else if (start > end) {
            log.debug("ERROR: End address 0x%X before start address 0x%X.", end, start);
            return -1;
        }

        if (this.selectMemoryUnit(MemoryUnit.flash) != 0) {
            return -2;
        }

        if (!quiet) {
            System.err.printf("Checking memory from 0x%X to 0x%X...  ", start, end);
            if (Log.getGlobalDebug() > DEBUG_THRESHOLD) {
                System.err.println();
            }
        }

        int retval = 0;
        int blankUpto = start;
        short currentPage;
        int checkUntil;
        do {
            currentPage = (short)(blankUpto / PAGE_SIZE);
            checkUntil = Math.min((currentPage + 1) * PAGE_SIZE - 1, end);

            if (this.selectPage(currentPage) != 0) {
                log.debug("page select error.");
                retval = -3;
                break;
            }

            int result = this.checkBlankPage(blankUpto % PAGE_SIZE, checkUntil % PAGE_SIZE);
            if (result == 0) {
                log.debug("Flash blank from 0x%X to 0x%X.", start, checkUntil);
                blankUpto = checkUntil + 1;
            }
            else if (result > 0) {
                blankUpto = result - 1 + PAGE_SIZE * currentPage;
                log.debug("Flash NOT blank beginning at 0x%X.", blankUpto);
                retval = blankUpto + 1;
                break;
            }
            else {
                log.debug("Blank check fail err %d. Flash status unknown.", result);
                retval = result;
                break;
            }
        }
        while (blankUpto < end);

        if (retval == 0) {
            if (!quiet) {
                System.err.println("Empty.");
            }
        }
        else if (retval > 0) {
            if (!quiet) {
                System.err.println(String.format("Not blank at 0x%X.", retval - 1));
            }
        }
        else {
            if (!quiet) {
                System.err.println("ERROR.");
            }
        }

        return retval;
    }

    public int startAppReset() {
        return 0;
    }

    public int startAppNoReset() {
        return 0;
    }

    private int selectMemoryUnit(final MemoryUnit unit) {
        log.trace("atmel_select_memory_unit( %s, %s )", this.getHandle().hashCode(), unit);

        if (!this.isInitialized()) {
            log.debug("ERROR: Device pointer is NULL.");
            throw new DeviceNotInitializedException();
        }

        if (!this.type.isType(DeviceType.GRP_AVR32)) {
            log.debug("Ignore Select Memory Unit for non GRP_AVR32 device.");
            return 0;
        }
        else if (this.type.isType(DeviceType.ADC_AVR32) &&
                !(unit == MemoryUnit.flash || unit == MemoryUnit.security || unit == MemoryUnit.config ||
                  unit == MemoryUnit.boot || unit == MemoryUnit.sig || unit == MemoryUnit.user)) {
            log.debug("%d is not a valid memory unit for AVR32 devices.", unit);
            System.err.println("Invalid Memory Unit Selection.");
            return -1;
        }
        else if (unit.getValue() > MemoryUnit.extdf.getValue()) {
            log.debug("Valid Memory Units 0 to 0x%X, not 0x%X.", MemoryUnit.extdf.getValue(), unit.getValue());
            System.err.println("Invalid Memory Unit Selection.");
            return -1;
        }

        log.debug("Selecting %s memory unit.", unit);

        final ByteBuffer command = ByteBuffer.allocateDirect(4)
                .put(new byte[] { 0x06, 0x03, 0x00 })
                .put(unit.getValue());
        if (this.download(command) != command.capacity()) {
            log.debug("atmel_select_memory_unit 0x%02X dfu_download failed.", unit.getValue());
            return -2;
        }

        if (this.updateStatus() != 0) {
            log.debug("DFU_GETSTATUS failed after atmel_select_memory_unit.");
            return -3;
        }

        if (!this.isStatusOK()) {
            log.debug("Error: status (%s) was not OK.", this.getStatus());
            if (this.isStateError()) {
                this.clearStatus();
            }
            return -4;
        }

        return 0;
    }

    private int selectPage(final short memoryPage) {
        log.trace("atmel_select_page( %s, %d )", this.getHandle().hashCode(), memoryPage);

        if (!this.isInitialized()) {
            log.debug("ERROR: Invalid arguments, device pointer is NULL.");
            throw new DeviceNotInitializedException();
        }

        if (this.type.isType(DeviceType.ADC_8051)) {
            log.debug("Select page not implemented for 8051 device, ignoring.");
            return 0;
        }

        log.debug("Selecting page %d, address 0x%X.", memoryPage, PAGE_SIZE * memoryPage);

        ByteBuffer command;
        if (this.type.isType(DeviceType.GRP_AVR32)) {
            command = ByteBuffer.allocateDirect(5).order(ByteOrder.BIG_ENDIAN)
                    .put(new byte[] { 0x06, 0x03, 0x01 })
                    .putShort(memoryPage);

        }
        else if (this.type.isType(DeviceType.ADC_AVR)) {
            command = ByteBuffer.allocateDirect(4)
                    .put(new byte[] { 0x06, 0x03 })
                    .put((byte)memoryPage);
        }
        else {
            return 0;
        }

        if (this.download(command) != command.capacity()) {
            log.debug("atmel_select_page DFU_DNLOAD failed.");
            return -1;
        }

        if (this.updateStatus() != 0) {
            log.debug("atmel_select_page DFU_GETSTATUS failed.");
            return -3;
        }

        if (!this.isStatusOK()) {
            log.debug("Error: Status (%s) was not OK.", this.getStatus());
            if (this.isStateError()) {
                this.clearStatus();
            }
            return -4;
        }

        return 0;
    }

    private int prepareBuffer(final BufferOut bufferOut) {
        return 0;
    }

    public int user(final BufferOut bufferOut) {
        return 0;
    }

    public int secure() {
        return 0;
    }

    public SecurityBit getSecure() {
        return SecurityBit.SECURE_OFF;
    }

    public int flash(final BufferOut bufferOut, final boolean eeprom, final boolean force, final boolean quiet) {
        log.trace("atmel_flash( %s, %s, %b, %b )", this.getHandle().hashCode(), bufferOut.hashCode(), force, quiet);

        if (!this.isInitialized() || !bufferOut.isInitialized()) {
            log.debug("ERROR: Invalid arguments, device/buffer pointer is NULL.");
            throw new DeviceNotInitializedException();
        }
        else if (!bufferOut.hasData()) {
            log.debug("ERROR: No valid target memory, end before start");
            return -1;
        }

        if (bufferOut.prepareBuffer() != 0) {
            if (!quiet) {
                System.err.println("Program Error, use debug for more info.");
            }
            return -2;
        }

        log.debug("Flash available from %s (64kB p. %s), 0x%X bytes.",
                bufferOut.getValidRange(), bufferOut.getValidRange().getPageRange(),
                bufferOut.getValidRange().getLength());
        log.debug("Data start @ 0x%X: 64kB p %d; %dB p 0x%X + 0x%X offset.",
                bufferOut.getDataRange().getStart(), bufferOut.getDataRange().getStartPage(),
                bufferOut.getPageSize(), bufferOut.getFirstPage(), bufferOut.getOffsetInPage(bufferOut.getDataRange().getStart()));
        log.debug("Data end @ 0x%X: 64kB p %d; %dB p 0x%X + 0x%X offset.",
                bufferOut.getDataRange().getEnd(), bufferOut.getDataRange().getEndPage(),
                bufferOut.getPageSize(), bufferOut.getLastPage(), bufferOut.getOffsetInPage(bufferOut.getDataRange().getEnd()));
        log.debug("Totals: 0x%X bytes, %d %dB pages, %d 64kB bytes pages.",
                bufferOut.getDataRange().getLength(), bufferOut.getPageCount(), bufferOut.getPageSize(),
                bufferOut.getDataRange().getPageCount());

        int result;
        if (!bufferOut.isDataInsideValid()) {
            log.debug("ERROR: Data exists outside of the valid target flash region.");
            if (!quiet) {
                System.err.println("Hex file error, use debug for more info.");
            }
            return -1;
        }
        else if (!bufferOut.hasData()) {
            log.debug("ERROR: No valid data to flash.");
            if (!quiet) {
                System.err.println("Hex file error, use debug for more info.");
            }
            return -1;
        }
        else if (!force) {
            result = this.checkBlank(bufferOut.getDataRange(), quiet);
            if (result != 0) {
                if (!quiet) {
                    System.err.println("The target memory for the program is not blank.");
                    System.err.println("Use --force flag to override this error check.");
                }
                log.debug("The target memory is not blank.");
                return -1;
            }
        }

        if (this.selectMemoryUnit(eeprom ? MemoryUnit.eeprom : MemoryUnit.flash) != 0) {
            log.debug("Error selection memory unit.");
            if (!quiet) {
                System.err.println("Memory access error, use debug for more info.");
            }
            return -2;
        }

        if (!quiet) {
            if (Log.getGlobalDebug() <= DEBUG_THRESHOLD) {
                System.err.print(PROGRESS_METER);
            }
            System.err.println(String.format("Programming 0x%X bytes...", bufferOut.getDataLength()));
            if (Log.getGlobalDebug() <= DEBUG_THRESHOLD) {
                System.err.print(PROGRESS_START);
            }
        }

        int retval = 0;
        this.progress = 0;
        short memoryPage = -1;
        bufferOut.rewindBlock();

        try {
            while (bufferOut.hasRemainingBlock()) {
                if (memoryPage != bufferOut.getBlockPage()) {
                    memoryPage = bufferOut.getBlockPage();
                    result = this.selectPage(memoryPage);
                    if (result != 0) {
                        log.debug("ERROR selecting 64kB page %d.", result);
                        retval = -3;
                        return retval;
                    }
                }

                log.debug("Program data block: %s (p. %d), 0x%X bytes.",
                        bufferOut.getBlockRange(), bufferOut.getBlockPage(), bufferOut.getBlockLength());

                result = this.flashBlock(bufferOut, eeprom);
                if (result != 0) {
                    log.debug("Error flashing the block: err %d.", result);
                    retval = -4;
                    return retval;
                }

                if (!quiet) {
                    printProgress(bufferOut);
                }
            }
        }
        finally {
            if (!quiet) {
                if (retval == 0) {
                    if (Log.getGlobalDebug() <= DEBUG_THRESHOLD) {
                        System.err.print(PROGRESS_END);
                    }
                    System.err.println("Success");
                }
                else {
                    if (Log.getGlobalDebug() <= DEBUG_THRESHOLD) {
                        System.err.print(PROGRESS_ERROR);
                    }
                    System.err.println("ERROR");
                    if (retval == -3) {
                        System.err.println("Memory access error, use debug for more info.");
                    }
                    else if (retval == -4) {
                        System.err.println("Memory write error, use debug for more info.");
                    }
                }
            }
        }

        return retval;
    }

    private ByteBuffer getPopulateFooter(final short vendorId, final short productId, final short bcdFirmware) {
        log.trace("atmel_flash_populate_footer( %d, %d, %d )", vendorId & 0xffff, productId & 0xffff, bcdFirmware & 0xffff);

        // TODO: Calculate the message CRC
        final int crc = 0;
        final int length = 16;
        return ByteBuffer.allocateDirect(length).order(ByteOrder.BIG_ENDIAN)
                .putInt(crc)
                .put((byte)length)
                .put("DFU".getBytes())
                .put(new byte[]{0x01, 0x10})
                .putShort(vendorId)
                .putShort(productId)
                .putShort(bcdFirmware);
    }

    private ByteBuffer getPopulateHeader(final int start, final int end, final boolean eeprom) {
        log.trace("atmel_flash_populate_header( %d, %d, %b )", start, end, eeprom);

        int controlBlockSize;
        int alignment;
        if (this.type.isType(DeviceType.GRP_AVR32)) {
            controlBlockSize = AVR32_CONTROL_BLOCK_SIZE;
            alignment = start % AVR32_CONTROL_BLOCK_SIZE;
        }
        else {
            controlBlockSize = CONTROL_BLOCK_SIZE;
            alignment = 0;
        }

        final int length = controlBlockSize + alignment;
        return ByteBuffer.allocateDirect(length).order(ByteOrder.BIG_ENDIAN)
                .put((byte)0x01)
                .put((byte) ((eeprom && !this.type.isType(DeviceType.ADC_XMEGA)) ? 0x01 : 0x00))
                .putShort((short)start)
                .putShort((short)end);
    }

    private int flashBlock(final BufferOut bufferOut, final boolean eeprom) {
        log.trace("__atmel_flash_block( %s, %s, %b )", this.getHandle().hashCode(), bufferOut.hashCode(), eeprom);

        if (!this.isInitialized() || !bufferOut.isInitialized()) {
            log.debug("ERROR: Invalid arguments, device/buffer pointer is NULL.");
            throw new DeviceNotInitializedException();
        }
        else if (!bufferOut.getBlockRange().isValid()) {
            log.debug("ERROR: End address 0x%X before start address 0x%X.",
                    bufferOut.getBlockRange().getEnd(), bufferOut.getBlockRange().getStart());
            return -1;
        }
        else if (bufferOut.getBlockLength() > BufferOut.MAX_TRANSFER_SIZE) {
            log.debug("ERROR: 0x%X byte message > MAX TRANSFER SIZE (0x%X).",
                    bufferOut.getBlockLength(), BufferOut.MAX_TRANSFER_SIZE);
            return -1;
        }

        ByteBuffer header = this.getPopulateHeader(
                bufferOut.getBlockRange().getStartInPage(),
                bufferOut.getBlockRange().getEndInPage(),
                eeprom);
        ByteBuffer footer = this.getPopulateFooter((short)0xffff, (short)0xffff, (short)0xffff);
        byte[] data = bufferOut.getBlock();
        header.rewind();
        footer.rewind();
        ByteBuffer message = ByteBuffer.allocateDirect(header.capacity() + data.length + footer.capacity())
                .put(header).put(data).put(footer);

        int result = this.download(message);
        if (result != message.capacity()) {
            if (result == -Errno.EPIPE) {
                System.err.println("Device is write protected.");
                this.clearStatus();
            }
            else {
                log.debug("atmel_flash: flash data dfu_download failed.");
                log.debug("Expected message length of %d, got %d.", message.capacity(), result);
            }
            return -2;
        }

        if (this.updateStatus() != 0) {
            log.debug("dfu_get_status failed.");
            return -3;
        }

        if (this.isStatusOK()) {
            log.debug("Page write success.");
        }
        else {
            // TODO: debug message
            log.debug("Page write unsuccessful (err %s).", this.getStatus());
            if (this.isStateError()) {
                this.clearStatus();
            }
            return this.getStatus().getValue();
        }

        return 0;
    }

    public void printDeviceInfo(final PrintStream stream, DeviceInfo info) {
    }


}
