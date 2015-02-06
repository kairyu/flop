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

package com.github.kairyu.flop.programmer;

/**
 * @author Kai Ryu
 *
 */
public class Error {

    public static final int SUCCESS                         = 0;
    public static final int UNSPECIFIED_ERROR               = 1;
    public static final int ARGUMENT_ERROR                  = 2;
    public static final int DEVICE_ACCESS_ERROR             = 3;
    public static final int BUFFER_INIT_ERROR               = 4;
    public static final int FLASH_READ_ERROR                = 5;
    public static final int FLASH_WRITE_ERROR               = 6;
    public static final int VALIDATION_ERROR_IN_REGION      = 7;
    public static final int VALIDATION_ERROR_OUTSIDE_REGION = 8;

}
