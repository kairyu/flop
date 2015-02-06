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

package com.github.kairyu.flop.programmer.exception;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

/**
 * @author Kai Ryu
 *
 */
public class UnsupportedTargetException extends ArgumentParserException {

    /**
     *
     */
    private static final long serialVersionUID = -5854468925407704433L;
    private String target;

    public UnsupportedTargetException(String message, ArgumentParser parser,
            String target) {
        super(message, parser);
        this.target = target;
    }

    public String getTarget() {
        return this.target;
    }


}
