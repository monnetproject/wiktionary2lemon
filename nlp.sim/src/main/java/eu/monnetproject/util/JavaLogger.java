/****************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package eu.monnetproject.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author John McCrae
 */
public class JavaLogger implements Logger {

    private final java.util.logging.Logger logger;
    private final Class srcClazz;

    public JavaLogger(java.util.logging.Logger logger, Class srcClazz) {
        this.logger = logger;
        this.srcClazz = srcClazz;
    }

    public void setPolicy(LogPolicy policy) {
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        logger.addHandler(policy.handler);
    }

    public void severe(String msg) {
        try {
            LogRecord record = new LogRecord(Level.SEVERE, msg);
            record.setSourceClassName(srcClazz.getName());
            logger.log(record);
        } catch (Exception x) {
            System.err.println("[SEVERE] " + msg);
        }
    }

    public void warning(String msg) {
        try {
            LogRecord record = new LogRecord(Level.WARNING, msg);
            record.setSourceClassName(srcClazz.getName());
            logger.log(record);
        } catch (Exception x) {
            System.err.println("[WARNING] " + msg);
        }
    }

    public void info(String msg) {
        try {
            LogRecord record = new LogRecord(Level.INFO, msg);
            record.setSourceClassName(srcClazz.getName());
            logger.log(record);
        } catch (Exception x) {
            System.err.println("[INFO] " + msg);
        }
    }

    public void config(String msg) {
        try {
            LogRecord record = new LogRecord(Level.CONFIG, msg);
            record.setSourceClassName(srcClazz.getName());
            logger.log(record);
        } catch (Exception x) {
        }
    }

    public void fine(String msg) {
        try {
            LogRecord record = new LogRecord(Level.FINE, msg);
            record.setSourceClassName(srcClazz.getName());
            logger.log(record);
        } catch (Exception x) {
        }
    }

    public void finer(String msg) {
        try {
            LogRecord record = new LogRecord(Level.FINER, msg);
            record.setSourceClassName(srcClazz.getName());
            logger.log(record);
        } catch (Exception x) {
        }
    }

    public void finest(String msg) {
        try {
            LogRecord record = new LogRecord(Level.FINEST, msg);
            record.setSourceClassName(srcClazz.getName());
            logger.log(record);
        } catch (Exception x) {
        }
    }

    public String getName() {
        return logger.getName();
    }

    public void stackTrace(Throwable x) {
        Logging.stackTrace(this, x);
    }
}
