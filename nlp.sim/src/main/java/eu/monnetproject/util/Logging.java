package eu.monnetproject.util;

import java.util.LinkedList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Interface for simple console based logging. Provides a consistent logging behavior across the entire
 * project
 *
 * @author John McCrae
 */
public final class Logging {

    private static LogPolicy defaultPolicy = new LogPolicy(".*", Level.INFO, makeHandler(Level.INFO));
    private static LinkedList<LogPolicy> policies = new LinkedList<LogPolicy>();
    private static LinkedList<Logger> loggers = new LinkedList<Logger>();
    private static final Object lock = new Object();
    private static LoggerFactory loggerFactory;
    
    
    /**
     * Get a logger. Same as calling <code>getLogger(source,getLoggerLevel(source))</code>
     * @param source Please pass <code>this</code> to identify the source class
     */
    public static Logger getLogger(Object source) {
        synchronized (lock) {
            if(loggerFactory != null) {
                if(source instanceof Class) {
                    return loggerFactory.getLogger((Class)source);
                } else {
                    return loggerFactory.getLogger(source.getClass());
                }
            }
            LogPolicy policy = getLogPolicy(source);
            Class clazz;
            if (source instanceof Class) {
                clazz = (Class) source;
            } else {
                clazz = source.getClass();
            }
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(clazz.getName());
            logger.setUseParentHandlers(false);
            if(logger.getHandlers().length == 0) {
                logger.addHandler(policy.handler);
            }
            return new JavaLogger(logger,clazz);
        }
    }

    /**
     * Get a logger
     * @param source Please pass <code>this</code> to identify the source class
     * @param level The minimum level to show
     */
    public static Logger getLogger(Object source, Level level) {
        Class clazz;
        if (source instanceof Class) {
            clazz = (Class) source;
        } else {
            clazz = source.getClass();
        }
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(clazz.getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(makeHandler(level));
        return new JavaLogger(logger,clazz);
    }

    /**
     * Set the default logging level
     * @param level The level
     */
    public static void setDefaultLogLevel(Level level) {
        defaultPolicy = new LogPolicy(defaultPolicy.regex, level, defaultPolicy.handler);
    }

    /**
     * Get the log level for a particular class
     * @param source The source object
     */
    public static Level getLogLevel(Object source) {
        return getLogPolicy(source).level;
    }


    private static Handler makeHandler(Level level) {
        Handler handler = new ConsoleHandler();
        handler.setLevel(level);
        Formatter formatter = new Formatter() {

            @Override
            public String format(LogRecord logRecord) {
                return "[" + colorString(logRecord.getLevel()) + logRecord.getLevel().getName() + black() + " " + logRecord.getSourceClassName() + "] "
                        + logRecord.getMessage() + System.getProperty("line.separator");
            }
        };
        handler.setFormatter(formatter);
        return handler;
    }

    private static String colorString(Level level) {
        if (System.getProperty("os.name").equals("Linux")) {
            if (level == Level.SEVERE) {
                return "\033[0;31m";
            } else if (level == Level.WARNING) {
                return "\033[0;33m";
            } else if (level == Level.INFO) {
                return "\033[0;32m";
            } else {
                return "\033[0;30m";
            }
        } else {
            return "";
        }
    }

    private static String black() {
        if (System.getProperty("os.name").equals("Linux")) {
            return "\033[0m";
        } else {
            return "";
        }
    }

    private static LogPolicy getLogPolicy(Object source) {
        for (LogPolicy policy : policies) {
            if (source.getClass().getName().matches(policy.regex)) {
                return policy;
            }
        }
        return defaultPolicy;
    }

    /**
     * Set the logging policy for a particular set of classes
     * @param regex A regex matching suitable classnames
     * @param level The level to set the components to
     */
    public static void setLogLevel(String regex, Level level) {
        synchronized (lock) {
            LogPolicy policy = new LogPolicy(regex, level, makeHandler(level));
            policies.add(policy);

            // Apply retroactively 
            for (Logger logger : loggers) {
                if (logger.getName().matches(regex)) {
                    
                }
            }
        }
    }
    
    /**
     * Print a stack trace to a logger
     * @param log The logger
     * @param t The object to stack trace
     */
    public static void stackTrace(Logger log, Throwable t) {
        log.severe(t.getClass().getName()+ (t.getMessage() == null ? "" : ": " + t.getMessage()));
        for (StackTraceElement stackElem : t.getStackTrace()) {
            log.severe("\t" + stackElem.toString());
        }
        if(t.getCause() != null) {
            log.severe("Caused by:");
            stackTrace(log,t.getCause());
        }
    }

    public static void setLoggerFactory(LoggerFactory loggerFactory) {
        Logging.loggerFactory = loggerFactory;
    }
}
