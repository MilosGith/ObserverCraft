/*
 * Yardstick: A Benchmark for Minecraft-like Services
 * Copyright (C) 2020 AtLarge Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package mcproxy.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * A {@link Handler} for publishing logs to the console.
 */
public class SimpleConsoleHandler extends Handler {

    public static final String LINE_SEPERATOR = System.getProperty("line.separator");
    private final Formatter formatter;

    /**
     * Creates a new SimpleConsoleHandler.
     *
     * @param formatter the formatter to use to format data.
     */
    public SimpleConsoleHandler(Formatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void publish(LogRecord lr) {
        System.out.print(formatter.format(lr));
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
