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
public class BufferIn extends AtmelBuffer {

    @Override
    protected int findBlockStart(final int end) {
        return end + 1;
    }

    @Override
    protected int findBlockEnd(final int start) {
        int end = start + MAX_TRANSFER_SIZE - 1;
        if (end / PAGE_SIZE > start / PAGE_SIZE) end = (end / PAGE_SIZE) * PAGE_SIZE - 1;
        if (end > this.getDataRange().getEnd()) end = this.getDataRange().getEnd();
        return end;
    }

}
