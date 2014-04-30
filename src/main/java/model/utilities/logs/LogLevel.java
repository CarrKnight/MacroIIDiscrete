/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import org.slf4j.Logger;

/**
 * Just an enum to pass around the log-levels and convert them to sfl4j calls
 * Created by carrknight on 4/30/14.
 */
public enum LogLevel {

        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR;


    /**
     * convert an enum level into a logger call!
     */
    public static void log(Logger logger, LogLevel level, String format, Object... argArray) {
        if (logger != null && level != null) {
            switch (level) {
                case TRACE:
                    logger.trace(format, argArray);
                    break;
                case DEBUG:
                    logger.debug(format, argArray);
                    break;
                case INFO:
                    logger.info(format, argArray);
                    break;
                case WARN:
                    logger.warn(format, argArray);
                    break;
                case ERROR:
                    logger.error(format, argArray);
                    break;
            }
        }
    }
}
