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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Kai Ryu
 *
 */
public class DfuStatus {

    private final Log log = Log.getLog(DfuDevice.class.getPackage().getName(), 100, 200, 300);

    public enum Status {
        OK                      (0x00),
        ERROR_TARGET            (0x01),
        ERROR_FILE              (0x02),
        ERROR_WRITE             (0x03),
        ERROR_ERASE             (0x04),
        ERROR_CHECK_ERASED      (0x05),
        ERROR_PROG              (0x06),
        ERROR_VERIFY            (0x07),
        ERROR_ADDRESS           (0x08),
        ERROR_NOTDONE           (0x09),
        ERROR_FIRMWARE          (0x0a),
        ERROR_VENDOR            (0x0b),
        ERROR_USBR              (0x0c),
        ERROR_POR               (0x0d),
        ERROR_UNKNOWN           (0x0e),
        ERROR_STALLEDPKT        (0x0f);

        private final byte value;

        private Status(int value) {
            this.value = (byte)value;
        }

        public byte getValue() {
            return value;
        }

        public static Status getByValue(byte value) {
            for (Status s: Status.values()) {
                if (s.value == value) {
                    return s;
                }
            }
            return null;
        }
    }

    public enum State {
        APP_IDLE                (0x00),
        APP_DETACH              (0x01),
        DFU_IDLE                (0x02),
        DFU_DOWNLOAD_SYNC       (0x03),
        DFU_DOWNLOAD_BUSY       (0x04),
        DFU_DOWNLOAD_IDLE       (0x05),
        DFU_MANIFEST_SYNC       (0x06),
        DFU_MANIFEST            (0x07),
        DFU_MANIFEST_WAIT_RESET (0x08),
        DFU_UPLOAD_IDLE         (0x09),
        DFU_ERROR               (0x0a);

        private final byte value;

        private State(int value) {
            this.value = (byte)value;
        }

        public byte getValue() {
            return value;
        }

        public static State getByValue(byte value) {
            for (State s: State.values()) {
                if (s.value == value) {
                    return s;
                }
            }
            return null;
        }
    }

    private static final int BUFFER_SIZE = 6;
    private Status bStatus;
    private int bwPollTimeout;
    private State bState;
    private byte iString;

    public DfuStatus() {
        init();
    }

    public static int getBufferSize() {
        return BUFFER_SIZE;
    }
    public Status getStatus() {
        return this.bStatus;
    }
    public int getPollTimeout() {
        return this.bwPollTimeout;
    }
    public State getState() {
        return this.bState;
    }
    public byte getString() {
        return this.iString;
    }

    public String toString() {
        return bStatus.toString();
    }

    public void init() {
        bStatus         = Status.ERROR_UNKNOWN;
        bwPollTimeout   = 0;
        bState          = State.DFU_ERROR;
        iString         = 0;
    }

    public int parse(final ByteBuffer buffer) {
        if (buffer.capacity() < BUFFER_SIZE) {
            return -1;
        }

        buffer.rewind();
        bStatus = Status.getByValue(buffer.get());
        byte[] bw = new byte[] { 0, 0, 0, 0 };
        buffer.get(bw, 1, 3);
        bwPollTimeout = ByteBuffer.wrap(bw).order(ByteOrder.BIG_ENDIAN).getInt();
        bState = State.getByValue(buffer.get());
        iString = buffer.get();

        log.debug("==============================");
        log.debug("status->bStatus: %s (0x%02x)", bStatus, bStatus.getValue());
        log.debug("status->bwPollTimeout: 0x%04x ms", bwPollTimeout);
        log.debug("status->bState: %s (0x%02x)", bState, bState.getValue());
        log.debug("status->iString: 0x%02x", iString);
        log.debug("------------------------------");

        return 0;
    }
}
