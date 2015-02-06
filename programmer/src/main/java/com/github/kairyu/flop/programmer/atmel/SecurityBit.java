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
public enum SecurityBit {

    SECURE_OFF(0),
    SECURE_ON(1, "is"),
    SECURE_MAYBE(2, "maybe");

    private final int value;
    private final String verb;

    private SecurityBit(final int value) {
        this.value = value;
        this.verb = "";
    }

    private SecurityBit(final int value, final String verb) {
        this.value = value;
        this.verb = verb;
    }

    public int getValue() {
        return this.value;
    }

    public String getVerb() {
        return this.verb;
    }

}
