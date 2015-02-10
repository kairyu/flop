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

package com.github.kairyu.flop.applet;

import javax.swing.JOptionPane;

/**
 * @author Kai Ryu
 *
 */
public class Main {

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(
            null,
            "This program cannot be run interactively.",
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        System.exit(0);
    }

}
