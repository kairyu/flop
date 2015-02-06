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
public class AtmelRange {

    public static final int PAGE_SIZE = 0x10000;

    private int start = Integer.MAX_VALUE;
    private int end = Integer.MIN_VALUE;

    public AtmelRange() {
        this.setInvalid();
    }

    public AtmelRange(final int start, final int end) {
        this.set(start, end);
    }

    public AtmelRange(final AtmelRange range) {
        this.set(range);
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public short getStartInPage() {
        return (short)(this.start % PAGE_SIZE);
    }

    public short getEndInPage() {
        return (short)(this.end % PAGE_SIZE);
    }

    public short getStartPage() {
        return (short)(this.start / PAGE_SIZE);
    }

    public short getEndPage() {
        return (short)(this.end / PAGE_SIZE);
    }

    public int getPageCount() {
        return this.getEndPage() - this.getStartPage() + 1;
    }

    public AtmelRange getPageRange() {
        return new AtmelRange(this.start / PAGE_SIZE, this.end / PAGE_SIZE);
    }

    public int[] getArray() {
        return getArray(1);
    }

    public int[] getArray(final int step) {
        if (step <= 0) {
            return null;
        }
        int index = 0;
        int[] array = new int[(this.end - this.start) / step + 1];
        for (int i = this.start; i <= this.end; i += step) {
            array[index++] = i;
        }
        return array;
    }

    public void setStart(final int start) {
        this.start = start;
    }

    public void setEnd(final int end) {
        this.end = end;
    }

    public void set(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    public void set(final AtmelRange range) {
        this.start = range.start;
        this.end = range.end;
    }

    public void setInvalid() {
        this.start = Integer.MAX_VALUE;
        this.end = Integer.MIN_VALUE;
    }

    public int getLength() {
        return this.end - this.start + 1;
    }

    public boolean isValid() {
        return (this.end >= this.start);
    }

    public boolean contains(final int value) {
        return (value >= this.start) && (value <= this.end);
    }

    public boolean contains(final AtmelRange range) {
        return (range.start >= this.start) && (range.end <= this.end);
    }

    public boolean intersect(final AtmelRange range) {
        return this.contains(range.start) || this.contains(range.end);
    }

    public void inflate(final int value) {
        this.start = Integer.min(value, this.start);
        this.end = Integer.max(value, this.end);
    }

    public AtmelRange offset(final int offset) {
        return new AtmelRange(this.start + offset, this.end + offset);
    }

    public String toString() {
        return this.toString("0x%X");
    }

    public String toString(final String format) {
        return String.format(format + " to " + format, this.start, this.end);
    }

}
