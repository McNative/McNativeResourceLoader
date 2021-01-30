package org.mcnative.loader.utils;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class PrefixLogger extends Logger {

    private final String name;

    public PrefixLogger(Logger logger,String name) {
        super(PrefixLogger.class.getCanonicalName(), null);
        setParent(logger);
        setLevel(Level.ALL);
        this.name = "[" + name + "] ";
    }

    @Override
    public void log(LogRecord logRecord) {
        logRecord.setMessage(name + logRecord.getMessage());
        super.log(logRecord);
    }

}
