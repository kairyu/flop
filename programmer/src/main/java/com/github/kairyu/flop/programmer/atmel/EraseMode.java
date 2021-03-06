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

/**
 * @author Kai Ryu
 *
 */
public enum EraseMode {

    ERASE_BLOCK_0   (0x00),
    ERASE_BLOCK_1   (0x20),
    ERASE_BLOCK_2   (0x40),
    ERASE_BLOCK_3   (0x80),
    ERASE_BLOCK_ALL (0xff);

    private final byte command;

    private EraseMode(int command) {
        this.command = (byte)command;
    }

    public byte getCommand() {
        return this.command;
    }

}
