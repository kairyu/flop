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

/**
 * @author Kai Ryu
 *
 */
public enum Command {

    configure(),
    read(),
    dump(),
    edump("dump-eeprom"),
    udump("dump-user"),
    erase(),
    flash(),
    user("flash-user"),
    eflash("flash-eeprom"),
    get(),
    getfuse(),
    launch(),
    reset(),
    setfuse(),
    setsecure(),
    start_app("start"),
    none();

    private final String name;

    private Command() {
        this.name = super.toString();
    }

    private Command(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static Command getByName(final String name) {
        for (Command command: Command.values()) {
            if (command.name.equals(name.trim())) {
                return command;
            }
        }
        throw new IllegalArgumentException();
    }

}
