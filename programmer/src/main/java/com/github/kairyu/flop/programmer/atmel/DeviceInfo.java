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

import com.github.kairyu.flop.programmer.command.Get;
import com.github.kairyu.flop.programmer.exception.ControllerErrorException;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Kai Ryu
 *
 */
public class DeviceInfo {

    private Map<Get, Short> info = new HashMap<Get, Short>();
    private final DeviceType type;

    public DeviceInfo(DeviceType type) {
        this.type = type;
    }

    public short getInfo(final Get get) throws ControllerErrorException {
        switch (get) {
            case BSB:
            case SBV:
            case SSB:
            case EB:
            case HSB:
                if (!type.isType(DeviceType.ADC_8051)) {
                    throw new ControllerErrorException(
                            get.getDescription(), DeviceType.ADC_8051.getName());
                }
            default:
                break;
        }
        return this.info.get(get);
    }

    public void setInfo(final Get get, final short value) throws ControllerErrorException {
        switch (get) {
            case BSB:
            case SBV:
            case SSB:
            case EB:
            case HSB:
                if (!type.isType(DeviceType.ADC_8051)) {
                    throw new ControllerErrorException(
                            get.getDescription(), DeviceType.ADC_8051.getName());
                }
            default:
                break;
        }
        this.info.put(get, value);
    }

    public String toString() {
        return this.info.toString();
    }

}
