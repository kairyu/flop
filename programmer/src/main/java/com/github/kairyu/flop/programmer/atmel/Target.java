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
public enum Target {
    at89c51snd1c    (DeviceType.ADC_8051,  0x2FFF, 0x03eb, 0x10000, 0x1000, true,  128, false, true,  0,   0),
    at89c51snd2c    (DeviceType.ADC_8051,  0x2FFF, 0x03eb, 0x10000, 0x1000, true,  128, false, true,  0,   0),
    at89c5130       (DeviceType.ADC_8051,  0x2FFD, 0x03eb, 0x04000, 0x0000, true,  128, false, true,  128, 0x0400),
    at89c5131       (DeviceType.ADC_8051,  0x2FFD, 0x03eb, 0x08000, 0x0000, true,  128, false, true,  128, 0x0400),
    at89c5132       (DeviceType.ADC_8051,  0x2FFF, 0x03eb, 0x10000, 0x0C00, true,  128, false, true,  0,   0),
    at90usb1287     (DeviceType.ADC_AVR,   0x2FFB, 0x03eb, 0x20000, 0x2000, true,  128, true,  false, 128, 0x1000),
    at90usb1286     (DeviceType.ADC_AVR,   0x2FFB, 0x03eb, 0x20000, 0x2000, true,  128, true,  false, 128, 0x1000),
    at90usb1287_4k  (DeviceType.ADC_AVR,   0x2FFB, 0x03eb, 0x20000, 0x1000, true,  128, true,  false, 128, 0x1000, "at90usb1287-4k"),
    at90usb1286_4k  (DeviceType.ADC_AVR,   0x2FFB, 0x03eb, 0x20000, 0x1000, true,  128, true,  false, 128, 0x1000, "at90usb1286-4k"),
    at90usb647      (DeviceType.ADC_AVR,   0x2FF9, 0x03eb, 0x10000, 0x2000, true,  128, true,  false, 128, 0x0800),
    at90usb646      (DeviceType.ADC_AVR,   0x2FF9, 0x03eb, 0x10000, 0x2000, true,  128, true,  false, 128, 0x0800),
    at90usb162      (DeviceType.ADC_AVR,   0x2FFA, 0x03eb, 0x04000, 0x1000, true,  128, true,  false, 128, 0x0200),
    at90usb82       (DeviceType.ADC_AVR,   0x2FF7, 0x03eb, 0x02000, 0x1000, true,  128, true,  false, 128, 0x0200),
    atmega32u6      (DeviceType.ADC_AVR,   0x2FF2, 0x03eb, 0x08000, 0x1000, true,  128, true,  false, 128, 0x0400),
    atmega32u4      (DeviceType.ADC_AVR,   0x2FF4, 0x03eb, 0x08000, 0x1000, true,  128, true,  false, 128, 0x0400),
    atmega32u2      (DeviceType.ADC_AVR,   0x2FF0, 0x03eb, 0x08000, 0x1000, true,  128, true,  false, 128, 0x0400),
    atmega16u4      (DeviceType.ADC_AVR,   0x2FF3, 0x03eb, 0x04000, 0x1000, true,  128, true,  false, 128, 0x0200),
    atmega16u2      (DeviceType.ADC_AVR,   0x2FEF, 0x03eb, 0x04000, 0x1000, true,  128, true,  false, 128, 0x0200),
    atmega8u2       (DeviceType.ADC_AVR,   0x2FEE, 0x03eb, 0x02000, 0x1000, true,  128, true,  false, 128, 0x0200),
    at32uc3a0128    (DeviceType.ADC_AVR32, 0x2FF8, 0x03eb, 0x20000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a1128    (DeviceType.ADC_AVR32, 0x2FF8, 0x03eb, 0x20000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a0256    (DeviceType.ADC_AVR32, 0x2FF8, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a1256    (DeviceType.ADC_AVR32, 0x2FF8, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a0512    (DeviceType.ADC_AVR32, 0x2FF8, 0x03eb, 0x80000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a1512    (DeviceType.ADC_AVR32, 0x2FF8, 0x03eb, 0x80000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a0512es  (DeviceType.ADC_AVR32, 0x2FF8, 0x03eb, 0x80000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a1512es  (DeviceType.ADC_AVR32, 0x2FF8, 0x03eb, 0x80000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a364     (DeviceType.ADC_AVR32, 0x2FF1, 0x03eb, 0x10000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a364s    (DeviceType.ADC_AVR32, 0x2FF1, 0x03eb, 0x10000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a3128    (DeviceType.ADC_AVR32, 0x2FF1, 0x03eb, 0x20000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a3128s   (DeviceType.ADC_AVR32, 0x2FF1, 0x03eb, 0x20000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a3256    (DeviceType.ADC_AVR32, 0x2FF1, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a3256s   (DeviceType.ADC_AVR32, 0x2FF1, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3a4256s   (DeviceType.ADC_AVR32, 0x2FF1, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b064     (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x10000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b164     (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x10000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b0128    (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x20000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b1128    (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x20000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b0256    (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b1256    (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b0256es  (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b1256es  (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b0512    (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x80000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3b1512    (DeviceType.ADC_AVR32, 0x2FF6, 0x03eb, 0x80000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c064     (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x10000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c0128    (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x20000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c0256    (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c0512    (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x80000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c164     (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x10000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c1128    (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x20000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c1256    (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c1512    (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x80000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c264     (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x10000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c2128    (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x20000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c2256    (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x40000, 0x2000, false, 512, false, true,  0,   0),
    at32uc3c2512    (DeviceType.ADC_AVR32, 0x2FEB, 0x03eb, 0x80000, 0x2000, false, 512, false, true,  0,   0),
    atxmega64a1u    (DeviceType.ADC_XMEGA, 0x2FE8, 0x03eb, 0x10000, 0x1000, true,  256, true,  false, 32,  0x0800),
    atxmega128a1u   (DeviceType.ADC_XMEGA, 0x2FED, 0x03eb, 0x20000, 0x2000, true,  256, true,  false, 32,  0x0800),
    atxmega64a3u    (DeviceType.ADC_XMEGA, 0x2FE5, 0x03eb, 0x10000, 0x1000, true,  256, true,  false, 32,  0x0800),
    atxmega128a3u   (DeviceType.ADC_XMEGA, 0x2FE6, 0x03eb, 0x20000, 0x2000, true,  512, true,  false, 32,  0x0800),
    atxmega192a3u   (DeviceType.ADC_XMEGA, 0x2FE7, 0x03eb, 0x30000, 0x2000, true,  512, true,  false, 32,  0x0800),
    atxmega256a3u   (DeviceType.ADC_XMEGA, 0x2FEC, 0x03eb, 0x40000, 0x2000, true,  512, true,  false, 32,  0x1000),
    atxmega16a4u    (DeviceType.ADC_XMEGA, 0x2FE3, 0x03eb, 0x04000, 0x1000, true,  256, true,  false, 32,  0x0400),
    atxmega32a4u    (DeviceType.ADC_XMEGA, 0x2FE4, 0x03eb, 0x08000, 0x1000, true,  256, true,  false, 32,  0x0400),
    atxmega64a4u    (DeviceType.ADC_XMEGA, 0x2FDD, 0x03eb, 0x10000, 0x1000, true,  256, true,  false, 32,  0x0800),
    atxmega128a4u   (DeviceType.ADC_XMEGA, 0x2FDE, 0x03eb, 0x20000, 0x2000, true,  256, true,  false, 32,  0x0800),
    atxmega256a3b   (DeviceType.ADC_XMEGA, 0x2FE2, 0x03eb, 0x40000, 0x2000, true,  512, true,  false, 32,  0x1000),
    atxmega64b1     (DeviceType.ADC_XMEGA, 0x2FE1, 0x03eb, 0x10000, 0x1000, true,  256, true,  false, 32,  0x0800),
    atxmega128b1    (DeviceType.ADC_XMEGA, 0x2FEA, 0x03eb, 0x20000, 0x2000, true,  256, true,  false, 32,  0x0800),
    atxmega64b3     (DeviceType.ADC_XMEGA, 0x2FDF, 0x03eb, 0x10000, 0x1000, true,  256, true,  false, 32,  0x0800),
    atxmega128b3    (DeviceType.ADC_XMEGA, 0x2FE0, 0x03eb, 0x20000, 0x2000, true,  256, true,  false, 32,  0x0800),
    atxmega64c3     (DeviceType.ADC_XMEGA, 0x2FD6, 0x03eb, 0x10000, 0x1000, true,  256, true,  false, 32,  0x0800),
    atxmega128c3    (DeviceType.ADC_XMEGA, 0x2FD7, 0x03eb, 0x20000, 0x2000, true,  512, true,  false, 32,  0x0800),
    atxmega256c3    (DeviceType.ADC_XMEGA, 0x2FDA, 0x03eb, 0x40000, 0x2000, true,  512, true,  false, 32,  0x1000),
    atxmega384c3    (DeviceType.ADC_XMEGA, 0x2FDB, 0x03eb, 0x60000, 0x2000, true,  512, true,  false, 32,  0x1000),
    atxmega16c4     (DeviceType.ADC_XMEGA, 0x2FD8, 0x03eb,  0x4000, 0x1000, true,  256, true,  false, 32,   0x400),
    atxmega32c4     (DeviceType.ADC_XMEGA, 0x2FD9, 0x03eb,  0x8000, 0x1000, true,  256, true,  false, 32,   0x400),
    none            (DeviceType.NULL,           0,      0,       0,      0, false,   0, false, false,  0,       0);

    private final String name;
    private final DeviceType deviceType;
    private final int chipId;
    private final int vendorId;
    private final int memorySize;
    private final int bootloaderSize;
    private final boolean bootloaderAtHighMem;
    private final int flashPageSize;
    private final boolean initialAbort;
    private final boolean honorInterfaceClass;
    private final int eepromPageSize;
    private final int eepromMemorySize;

    private Target(DeviceType deviceType, int chipId, int vendorId, int memorySize, int bootloaderSize,
            boolean bootloaderAtHighMem, int flashPageSize, boolean initialAbort,
            boolean honorInterfaceClass, int eepromPageSize, int eepromMemorySize) {
        this.name = super.toString();
        this.deviceType = deviceType;
        this.chipId = chipId;
        this.vendorId = vendorId;
        this.memorySize = memorySize;
        this.bootloaderSize = bootloaderSize;
        this.bootloaderAtHighMem = bootloaderAtHighMem;
        this.flashPageSize = flashPageSize;
        this.initialAbort = initialAbort;
        this.honorInterfaceClass = honorInterfaceClass;
        this.eepromPageSize = eepromPageSize;
        this.eepromMemorySize = eepromMemorySize;
    }

    private Target(DeviceType deviceType, int chipId, int vendorId, int memorySize, int bootloaderSize,
            boolean bootloaderAtHighMem, int flashPageSize, boolean initialAbort,
            boolean honorInterfaceClass, int eepromPageSize, int eepromMemorySize,
            String name) {
        this.name = name;
        this.deviceType = deviceType;
        this.chipId = chipId;
        this.vendorId = vendorId;
        this.memorySize = memorySize;
        this.bootloaderSize = bootloaderSize;
        this.bootloaderAtHighMem = bootloaderAtHighMem;
        this.flashPageSize = flashPageSize;
        this.initialAbort = initialAbort;
        this.honorInterfaceClass = honorInterfaceClass;
        this.eepromPageSize = eepromPageSize;
        this.eepromMemorySize = eepromMemorySize;
    }

    public String getName() {
        return this.name;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public int getVendorId() {
        return this.vendorId;
    }

    public int getChipId() {
        return this.chipId;
    }

    public int getMemorySize() {
        return this.memorySize;
    }

    public int getBootloaderSize() {
        return this.bootloaderSize;
    }

    public boolean getBootloaderAtHighMem() {
        return this.bootloaderAtHighMem;
    }

    public int getFlashPageSize() {
        return this.flashPageSize;
    }

    public boolean getInitialAbort() {
        return this.initialAbort;
    }

    public boolean getHonorInterfaceClass() {
        return this.honorInterfaceClass;
    }

    public int getEepromPageSize() {
        return this.eepromPageSize;
    }

    public int getEepromMemorySize() {
        return this.eepromMemorySize;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static Target getByName(final String name) {
        for (Target target: Target.values()) {
            if (target.name.equals(name.trim())) {
                return target;
            }
        }
        throw new IllegalArgumentException();
    }

}
