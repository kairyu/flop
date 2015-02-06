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

import com.github.kairyu.flop.programmer.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author Kai Ryu
 *
 */
public abstract class AtmelBuffer {

    protected final Log log = Log.getLog(AtmelDevice.class.getPackage().getName(),
            DEBUG_THRESHOLD, TRACE_THRESHOLD);

    protected static final int DEBUG_THRESHOLD = 50;
    protected static final int TRACE_THRESHOLD = 55;

    public static final int PAGE_SIZE         = 0x10000;
    public static final int MAX_TRANSFER_SIZE = 0x0400;
    protected static final byte BYTE_MAX      = (byte)0xff;
    private static final byte MARK_INVALID    = 0;
    private static final byte MARK_VALID      = 1;
    private static final int ADDRESS_MASK     = 0x7fffffff;

    private int totalSize;
    private int pageSize;
    private int offset;
    private AtmelRange dataRange;
    private AtmelRange validRange;
    private AtmelRange blockRange;
    private ByteBuffer buffer = null;
    private ByteBuffer mark = null;

    public int init(final AtmelBuffer buffer) {
        return this.init(buffer.totalSize, buffer.pageSize, buffer.offset);
    }

    public int init(final int totalSize, final int pageSize, final int offset) {
        if (totalSize == 0 || pageSize == 0) {
            return -1;
        }

        this.totalSize = totalSize;
        this.pageSize = pageSize;
        this.offset = offset;
        this.dataRange = new AtmelRange();
        this.validRange = new AtmelRange(0, totalSize - 1);
        this.blockRange = new AtmelRange();

        this.buffer = ByteBuffer.allocate(totalSize);
        this.mark = ByteBuffer.allocate(totalSize);
        while (this.buffer.hasRemaining()) {
            this.buffer.put(BYTE_MAX);
            this.mark.put(MARK_INVALID);
        }
        this.buffer.rewind();
        this.mark.rewind();

        return 0;
    }

    public int getTotalSize() {
        return this.totalSize;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public AtmelRange getDataRange() {
        return this.dataRange;
    }

    public AtmelRange getValidRange() {
        return this.validRange;
    }

    public AtmelRange getOffsetValidRange() {
        return this.validRange.offset(this.offset);
    }

    public AtmelRange getBlockRange() {
        return this.blockRange;
    }

    public int getDataLength() {
        return this.dataRange.getLength();
    }

    public int getValidLength() {
        return this.validRange.getLength();
    }

    public double getUsage() {
        return (double)this.getDataLength() / (double)this.getValidLength();
    }

    public int getFirstPage() {
        return this.dataRange.getStart() / this.pageSize;
    }

    public int getLastPage() {
        return this.dataRange.getEnd() / this.pageSize;
    }

    public int getPageCount() {
        return this.getLastPage() - this.getFirstPage() + 1;
    }

    public int getOffsetInPage(final int address) {
        return address % this.pageSize;
    }

    public boolean isInitialized() {
        return (this.buffer != null);
    }

    public boolean hasData() {
        return this.dataRange.isValid();
    }

    public boolean isDataInsideValid() {
        return this.validRange.contains(this.dataRange);
    }

    public boolean isValidAddress(final int address) {
        return this.validRange.offset(this.offset & ADDRESS_MASK).contains(address & ADDRESS_MASK);
    }

    public int getRelativeAddress(final int address) {
        return (address & ADDRESS_MASK) - (this.offset & ADDRESS_MASK);
    }

    public void putData(final int address, final byte data) {
        this.buffer.put(address, data);
        this.setDataValid(address);
        this.dataRange.inflate(address);
    }

    public byte getData(final int address) {
        return this.buffer.get(address);
    }

    public boolean isDataValid(final int address) {
        return (this.mark.get(address) == MARK_VALID);
    }

    public void setDataValid(final int address) {
        this.mark.put(address, MARK_VALID);
    }

    public void setDataInvalid(final int address) {
        this.mark.put(address,MARK_INVALID);
    }

    public short getBlockPage() {
        return this.blockRange.getStartPage();
    }

    public int getBlockLength() {
        return this.blockRange.getLength();
    }

    public int getBlockOffset() {
        return this.blockRange.getStart() - this.dataRange.getStart();
    }

    public byte[] getBlock() {
        byte[] block = new byte[this.getBlockLength()];
        this.buffer.position(this.blockRange.getStart());
        this.buffer.get(block, 0, block.length);
        this.nextBlock();
        return block;
    }

    public void putBlock(final ByteBuffer blockBuffer) {
        byte[] block = new byte[this.getBlockLength()];
        blockBuffer.get(block);
        this.putBlock(block);
    }

    public void putBlock(final byte[] block) {
        //this.buffer.position(this.blockRange.getStart());
        //this.buffer.put(block, 0, block.length);
        int start = this.blockRange.getStart();
        for (int i = 0; i < block.length; i++) {
            this.putData(start + i, block[i]);
        }
        this.nextBlock();
    }

    public boolean hasRemainingBlock() {
        return this.blockRange.isValid() && this.dataRange.contains(this.blockRange);
    }

    public void rewindBlock() {
        this.getBlockRange().setStart(this.getDataRange().getStart());
        this.getBlockRange().setEnd(this.findBlockEnd(this.getBlockRange().getStart()));
    }

    private void nextBlock() {
        this.getBlockRange().setStart(this.findBlockStart(this.getBlockRange().getEnd()));
        this.getBlockRange().setEnd(this.findBlockEnd(this.getBlockRange().getStart()));
    }

    protected abstract int findBlockStart(final int end);
    protected abstract int findBlockEnd(final int start);

}

