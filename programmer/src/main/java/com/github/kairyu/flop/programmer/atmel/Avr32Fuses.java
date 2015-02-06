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
public class Avr32Fuses {

    private int lock;
    private int epfl;
    private int bootprot;
    private int bodlevel;
    private int bodjyst;
    private int boden;
    private int ispBodEn;
    private int ispIoCondEn;
    private int ispForce;
    public int getLock() {
        return lock;
    }
    public void setLock(int lock) {
        this.lock = lock;
    }
    public int getEpfl() {
        return epfl;
    }
    public void setEpfl(int epfl) {
        this.epfl = epfl;
    }
    public int getBootprot() {
        return bootprot;
    }
    public void setBootprot(int bootprot) {
        this.bootprot = bootprot;
    }
    public int getBodlevel() {
        return bodlevel;
    }
    public void setBodlevel(int bodlevel) {
        this.bodlevel = bodlevel;
    }
    public int getBodjyst() {
        return bodjyst;
    }
    public void setBodjyst(int bodjyst) {
        this.bodjyst = bodjyst;
    }
    public int getBoden() {
        return boden;
    }
    public void setBoden(int boden) {
        this.boden = boden;
    }
    public int getIspBodEn() {
        return ispBodEn;
    }
    public void setIspBodEn(int ispBodEn) {
        this.ispBodEn = ispBodEn;
    }
    public int getIspIoCondEn() {
        return ispIoCondEn;
    }
    public void setIspIoCondEn(int ispIoCondEn) {
        this.ispIoCondEn = ispIoCondEn;
    }
    public int getIspForce() {
        return ispForce;
    }
    public void setIspForce(int ispForce) {
        this.ispForce = ispForce;
    }

}
