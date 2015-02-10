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
public enum MemoryUnit {

    flash,
    eeprom,
    security,
    config,
    boot,
    sig,
    user,
    ram,
    ext0,
    ext1,
    ext2,
    ext3,
    ext,
    ext5,
    ext6,
    ext7,
    extdf;

    public byte getValue() {
        return (byte)ordinal();
    }

    public static MemoryUnit getByValue(final int value) {
        for (MemoryUnit unit: MemoryUnit.values()) {
            if (unit.getValue() == value) {
                return unit;
            }
        }
        throw new IllegalArgumentException();
    }

    public static MemoryUnit getByName(final String name) {
        for (MemoryUnit unit: MemoryUnit.values()) {
            if (unit.toString().equalsIgnoreCase(name)) {
                return unit;
            }
        }
        throw new IllegalArgumentException();
    }

}