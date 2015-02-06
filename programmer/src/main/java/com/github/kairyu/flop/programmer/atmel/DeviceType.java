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

import java.util.EnumSet;

/**
 * @author Kai Ryu
 *
 */
public enum DeviceType {

    ADC_8051    ((1<<1), "8051"),
    ADC_AVR     ((1<<2), "AVR"),
    ADC_AVR32   ((1<<3), "AVR32"),
    ADC_XMEGA   ((1<<4), "XMEGA"),
    NULL        (0);

    public static final EnumSet<DeviceType> GRP_AVR = EnumSet.of(DeviceType.ADC_8051, DeviceType.ADC_AVR);
    public static final EnumSet<DeviceType> GRP_AVR32 = EnumSet.of(DeviceType.ADC_AVR32, DeviceType.ADC_XMEGA);

    private final int value;
    private final String name;

    private DeviceType(final int value) {
        this.value = value;
        this.name = null;
    }

    private DeviceType(final int value, final String name) {
        this.value = value;
        this.name = name;
    }

    private DeviceType(final DeviceType type) {
        this.value = type.value;
        this.name = type.name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public boolean isType(final EnumSet<DeviceType> set) {
        return set.contains(this);
    }

    public boolean isType(final DeviceType type) {
        return (this == type);
    }

}