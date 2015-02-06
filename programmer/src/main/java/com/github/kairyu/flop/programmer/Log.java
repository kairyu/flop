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

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Formatter;
import java.util.logging.ConsoleHandler;

/**
 * @author Kai Ryu
 *
 */
public class Log {

    private enum LogLevel {

        ERROR(Level.SEVERE),
        WARNING(Level.WARNING),
        DEFAULT(Level.INFO),
        DEBUG(Level.FINE),
        TRACE(Level.FINER),
        MSG_DEBUG(Level.FINEST);

        private final Level level;

        private LogLevel(Level level) {
            this.level = level;
        }

        public Level getLevel() {
            return level;
        }

        public static LogLevel getByLevel(Level level) {
            for (LogLevel l: LogLevel.values()) {
                if (l.level.equals(level)) {
                    return l;
                }
            }
            return null;
        }

    }

    public static class LogFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            return String.format("[%1$s] %2$s.%3$s: %4$s%n",
                    LogLevel.getByLevel(record.getLevel()),
                    record.getSourceClassName()
                        .replace(Log.class.getPackage().getName() + ".", ""),
                        //.split("\\.")[0],
                    record.getSourceMethodName(),
                    record.getMessage()
                    );
        }
    }

    private static List<Log> logs = new ArrayList<Log>();
    private static int globalDebug = 0;
    private Logger logger;
    private final int debugThreshold;
    private final int traceThreshold;
    private final int msgDebugThreshold;
    private static final int NULL = Integer.MAX_VALUE;

    public static Log getLog(final String name, final int debugThreshold) {
        return getLog(name, debugThreshold, NULL);
    }

    public static Log getLog(final String name, final int debugThreshold, final int traceThreshold) {
        return getLog(name, debugThreshold, traceThreshold, NULL);
    }

    public static Log getLog(final String name, final int debugThreshold, final int traceThreshold, final int msgDebugThreshold) {
        Log log = getLog(name);
        if (log == null) {
            log = new Log(name, debugThreshold, traceThreshold, msgDebugThreshold);
            log.setDebug(globalDebug);
            logs.add(log);
        }
        return log;
    }

    private static Log getLog(final String name) {
        for (Log log: logs) {
            if (log.getName().equals(name)) {
                return log;
            }
        }
        return null;
    }

    public static void setGlobalDebug(final int debug) {
        globalDebug = debug;
        for (Log log: logs) {
            log.setDebug(debug);
        }
    }

    public static int getGlobalDebug() {
        return globalDebug;
    }

    public Log(final String name, final int debugThreshold) {
        this(name, debugThreshold, NULL, NULL);
    }

    public Log(final String name, final int debugThreshold, final int traceThreshold) {
        this(name, debugThreshold, traceThreshold, NULL);
    }

    public Log(final String name, final int debugThreshold, final int traceThreshold, final int msgDebugThreshold) {
        this.logger = Logger.getLogger(name);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogFormatter());
        this.logger.addHandler(handler);

        this.debugThreshold = debugThreshold;
        this.traceThreshold = traceThreshold;
        this.msgDebugThreshold = msgDebugThreshold;
    }

    public String getName() {
        return this.logger.getName();
    }

    public void setDebug(final int debug) {
        LogLevel level;
        if (debug > this.msgDebugThreshold) {
            level = LogLevel.MSG_DEBUG;
        }
        else if (debug > this.traceThreshold) {
            level = LogLevel.TRACE;
        }
        else if (debug > this.debugThreshold) {
            level = LogLevel.DEBUG;
        }
        else {
            level = LogLevel.DEFAULT;
        }

        this.logger.setLevel(level.getLevel());
        for (Handler handler: this.logger.getHandlers()) {
            handler.setLevel(level.getLevel());
        }
    }

    public void error(final String format, final Object... objects) {
        this.log(LogLevel.ERROR, format, objects);
    }

    public void warning(final String format, final Object... objects) {
        this.log(LogLevel.WARNING, format, objects);
    }

    public void debug(final String format, final Object... objects) {
        this.log(LogLevel.DEBUG, format, objects);
    }

    public void trace(final String format, final Object... objects) {
        this.log(LogLevel.TRACE, format, objects);
    }

    public void msgDebug(final String format, final Object... objects) {
        this.log(LogLevel.MSG_DEBUG, format, objects);
    }

    private void log(final LogLevel level, final String format, final Object... objects) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[3];
        this.logger.logp(level.getLevel(), stackTrace.getClassName(), stackTrace.getMethodName(), String.format(format, objects));
    }

}
