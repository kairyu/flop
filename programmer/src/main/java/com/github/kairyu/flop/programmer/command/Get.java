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

import com.github.kairyu.flop.programmer.atmel.DeviceType;

/**
 * @author Kai Ryu
 *
 */
public enum Get {

    bootloader  ("Bootloader Version", "bootloader-version"),
    ID1         ("Device boot ID 1"),
    ID2         ("Device boot ID 2"),
    BSB         ("Boot Status Byte"),
    SBV         ("Software Boot Vector"),
    SSB         ("Software Security Byte"),
    EB          ("Extra Byte"),
    manufacturer("Manufacture Code"),
    family      ("Family Code"),
    product_name("Product Name", "product-name"),
    product_rev ("Product Revision", "product-revision"),
    HSB         ("Hardware Security Byte");

    private final String name;
    private final String description;

    private Get(String description) {
        this.name = super.toString();
        this.description = description;
    }

    private Get(String description, String name) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public byte[] getCommand(final DeviceType type) {
        byte[] command = null;
        switch (this) {
            case bootloader:
                if (type.isType(DeviceType.GRP_AVR)) {
                    command = new byte[] { 0x00, 0x00 };
                }
                else if (type.isType(DeviceType.GRP_AVR32)) {
                    command = new byte[] { 0x04, 0x00 };
                }
                break;
            case ID1:
                if (type.isType(DeviceType.GRP_AVR)) {
                    command = new byte[] { 0x00, 0x01 };
                }
                else if (type.isType(DeviceType.GRP_AVR32)) {
                    command = new byte[] { 0x04, 0x01 };
                }
                break;
            case ID2:
                if (type.isType(DeviceType.GRP_AVR)) {
                    command = new byte[] { 0x00, 0x02 };
                }
                else if (type.isType(DeviceType.GRP_AVR32)) {
                    command = new byte[] { 0x04, 0x02 };
                }
                break;
            case manufacturer:
                if (type.isType(DeviceType.GRP_AVR)) {
                    command = new byte[] { 0x01, 0x30 };
                }
                else if (type.isType(DeviceType.GRP_AVR32)) {
                    command = new byte[] { 0x05, 0x00 };
                }
                break;
            case family:
                if (type.isType(DeviceType.GRP_AVR)) {
                    command = new byte[] { 0x01, 0x31 };
                }
                else if (type.isType(DeviceType.GRP_AVR32)) {
                    command = new byte[] { 0x05, 0x01 };
                }
                break;
            case product_name:
                if (type.isType(DeviceType.GRP_AVR)) {
                    command = new byte[] { 0x01, 0x60 };
                }
                else if (type.isType(DeviceType.GRP_AVR32)) {
                    command = new byte[] { 0x05, 0x02 };
                }
                break;
            case product_rev:
                if (type.isType(DeviceType.GRP_AVR)) {
                    command = new byte[] { 0x01, 0x61 };
                }
                else if (type.isType(DeviceType.GRP_AVR32)) {
                    command = new byte[] { 0x05, 0x03 };
                }
                break;
            case BSB:
                if (type.isType(DeviceType.ADC_8051)) {
                    command = new byte[] { 0x01, 0x00 };
                }
                break;
            case SBV:
                if (type.isType(DeviceType.ADC_8051)) {
                    command = new byte[] { 0x01, 0x01 };
                }
                break;
            case SSB:
                if (type.isType(DeviceType.ADC_8051)) {
                    command = new byte[] { 0x01, 0x05 };
                }
                break;
            case EB:
                if (type.isType(DeviceType.ADC_8051)) {
                    command = new byte[] { 0x01, 0x06 };
                }
                break;
            case HSB:
                if (type.isType(DeviceType.ADC_8051)) {
                    command = new byte[] { 0x02, 0x00 };
                }
                break;
            default:
                break;
        }
        return command;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static Get getByName(final String name) {
        for (Get get: Get.values()) {
            if (get.name.equalsIgnoreCase(name.trim())) {
                return get;
            }
        }
        throw new IllegalArgumentException();
    }

    public static String textualFormat() {
        String text = "";
        String delim = "";
        for (Get get: Get.values()) {
            text += delim + "'" + get + "'";
            delim = ", ";
        }
        return text;
    }

}
