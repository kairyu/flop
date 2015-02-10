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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import cz.jaybee.intelhex.IntelHexDataListener;
import cz.jaybee.intelhex.IntelHexParser;

/**
 * @author Kai Ryu
 *
 */
public class BufferOut extends AtmelBuffer {

    private class DataListener implements IntelHexDataListener {
        private int invalidAddressCount = 0;
        private boolean quiet;

        public DataListener(final boolean quiet) {
            this.quiet = quiet;
        }

        public int getInvalidAddressCount() {
            return this.invalidAddressCount;
        }

        @Override
        public void data(long address, byte[] data) {
            for (int i = 0; i < data.length; i++) {
                int addr = (int)address + i;
                if (processData(data[i], addr) != 0) {
                    if (this.invalidAddressCount == 0) {
                        warnInvalidAddress(addr);
                    }
                    this.invalidAddressCount++;
                }
            }
        }

        @Override
        public void eof() {
            if (this.invalidAddressCount > 0) {
                if (!this.quiet) {
                    System.err.println(String.format("Total of 0x%X bytes in invalid addressed.",
                            invalidAddressCount));
                }
            }
        }
    }

    public int readHexString(final String hex, final boolean quiet) {
        InputStream is = new ByteArrayInputStream(hex.getBytes());

        return this.readHex(is, quiet);
    }

    public int readHexFile(final String filename, final boolean quiet) {
        if (filename == null) {
            if (!quiet) {
                System.err.println("Invalid filename.");
            }
            return -2;
        }

        InputStream is;
        try {
            is = new FileInputStream(filename);
        } catch (FileNotFoundException e1) {
            if (!quiet) {
                System.err.println(String.format("Error opening %s", filename));
            }
            return -3;
        }

        return this.readHex(is, quiet);
    }

    private int readHex(final InputStream is, final boolean quiet) {
        if (this.getTotalSize() <= 0) {
            log.debug("Must provide valid memory size in bout");
            return -1;
        }

        IntelHexParser ihp = new IntelHexParser(is);
        DataListener dl = new DataListener(quiet);
        ihp.setDataListener(dl);

        try {
            ihp.parse();
        } catch (IOException e) {
            if (!quiet) {
                System.err.println(String.format("Error opening %s", "source"));
            }
            return -3;
        } catch (Exception e) {
            e.printStackTrace();
        }

        int invalidAddressCount = dl.getInvalidAddressCount();
        if (invalidAddressCount > 0 && !quiet) {
            System.err.println(String.format("See --debug=%d or greater for more information.",
                    DEBUG_THRESHOLD + 1));
        }

        return invalidAddressCount;
    }

    public int prepareBuffer() {
        log.trace("atmel_flash_prep_buffer( %s )", this.hashCode());
        final int pageSize = this.getPageSize();
        for (int page: this.getValidRange().getArray(pageSize)) {
            int i;
            for (i = 0; i < pageSize; i++) {
                if (this.isDataValid(page + i)) {
                    break;
                }
            }
            if (i != pageSize) {
                for (i = 0; i < pageSize; i++) {
                    if (!this.isDataValid(page + i)) {
                        this.putData(page + i, BYTE_MAX);
                    }
                }
            }
        }
        return 0;
    }

    private int processData(final byte value, final int address) {
        if (this.isValidAddress(address)) {
            int relativeAddress = this.getRelativeAddress(address);
            this.putData(relativeAddress, value);
            return 0;
        }
        else {
            log.debug("Address 0x%X is outside valid range %s.", address,
                    this.getOffsetValidRange());
            return -1;
        }
    }

    private void warnInvalidAddress(final int address) {
        log.debug("Valid address region from %s.", this.getOffsetValidRange());
        System.err.println(String.format("WARNING: 0x%02x address outside valid region,",
                address));
        System.err.println(" suppressing additional address error messages.");
    }

    @Override
    protected int findBlockStart(final int end) {
        int start = end + 1;
        for (; start <= this.getDataRange().getEnd(); start++) {
            if (this.isDataValid(start)) break;
        }
        return start;
    }

    @Override
    protected int findBlockEnd(final int start) {
        int end = start;
        for (; end <= this.getDataRange().getEnd(); end++) {
            if (!this.isDataValid(end)) break;
            if (end - start + 1 > MAX_TRANSFER_SIZE) break;
            if (end / PAGE_SIZE > start / PAGE_SIZE) break;
        }
        return --end;
    }
}
