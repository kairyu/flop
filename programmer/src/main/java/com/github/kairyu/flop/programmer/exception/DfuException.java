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

package com.github.kairyu.flop.programmer.exception;

/**
 * @author Kai Ryu
 *
 */
public class DfuException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int errorCode;

    public DfuException(final String message) {
        super(message);
        this.errorCode = 0;
    }

    public DfuException(final int errorCode) {
        super(String.format("DFU error %d", errorCode));
        this.errorCode = errorCode;
    }

    public DfuException(final String message, final int errorCode) {
        super(String.format("DFU error %d: %s", errorCode, message));
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

}