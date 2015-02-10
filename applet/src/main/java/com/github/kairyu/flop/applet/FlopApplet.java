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

import com.github.kairyu.flop.programmer.Log;
import com.github.kairyu.flop.programmer.atmel.AtmelDevice;
import com.github.kairyu.flop.programmer.atmel.DeviceInfo;
import com.github.kairyu.flop.programmer.atmel.DeviceType;
import com.github.kairyu.flop.programmer.atmel.Target;
import com.github.kairyu.flop.programmer.command.Get;

import netscape.javascript.JSObject;

import org.usb4java.Device;
import org.usb4java.LibUsbException;

import java.applet.Applet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Kai Ryu
 *
 */
public class FlopApplet extends Applet implements Error {

    private Commands commands;

    @Override
    public void start() {
        super.start();
        commands = new Commands();
        JSObject window = JSObject.getWindow(this);
        window.call("flopInit", this.commands.init());
        new AtomicReference<Thread>(commands).get().start();
    }

    @Override
    public void stop() {
        this.commands.uninit();
        super.stop();
    }

    @Override
    public void init() {
    }

    public void setQuiet(final boolean quiet) {
        this.commands.setQuiet(quiet);
    }

    public void setDebug(final int debug) {
        this.commands.setDebug(debug);
    }

    public int setTarget(final String name) {
        return this.commands.setTarget(name);
    }

    public int get(final String name) {
        return this.commands.get(name);
    }

    public int get() {
        return this.commands.get("bootloader-version");
    }

    public int erase(final boolean force, final boolean validate) {
        return this.commands.erase(force, validate);
    }

    public int erase() {
        return this.commands.erase(false, true);
    }

    public int flash(final String hex, final String segment, final boolean force, final boolean validate) {
        return this.commands.flash(hex, segment, force, validate);
    }

    public int flash(final String hex) {
        return this.commands.flash(hex, "flash", false, true);
    }

    public int flashEEPROM(final String hex) {
        return this.commands.flash(hex, "eeprom", true, true);
    }

    public int startApp() {
        return this.commands.launch(false);
    }

    public int reset() {
        return this.commands.launch(true);
    }

    public int launch(final boolean reset) {
        return this.commands.launch(reset);
    }

}
