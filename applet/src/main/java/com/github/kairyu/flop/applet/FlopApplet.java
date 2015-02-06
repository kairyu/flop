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

import com.github.kairyu.flop.programmer.atmel.AtmelDevice;
import com.github.kairyu.flop.programmer.atmel.Target;

import netscape.javascript.JSObject;

import org.usb4java.LibUsbException;

import java.applet.Applet;

public class FlopApplet extends Applet {

    private Target target = Target.none;
    private AtmelDevice device;

    public void init() {
        boolean success = true;
        try {
            device = new AtmelDevice();
            device.init();
        }
        catch (LibUsbException e) {
            System.err.println("can't init libusb.");
            success = false;
        }
        JSObject window = JSObject.getWindow(this);
        window.call("flopInit", success);
    }

    public void setTarget(final String name) {
        try {
            target = Target.getByName(name);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
