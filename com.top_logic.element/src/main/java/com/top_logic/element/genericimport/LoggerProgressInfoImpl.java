/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.basic.Logger;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class LoggerProgressInfoImpl implements LoggerProgressInfo {

    private static final int MESSAGE_BUFFER_SIZE = 1024;
    
	private static final int DEBUG = 0;

	private static final int ERROR = 3;

	private static final int WARN = 2;

	private static final int INFO = 1;

	private static final int FATAL = 4;
    
    private StringBuffer message;
    private int     refreshInterval;

    private long    current;
    private boolean finished;
    
    /** Stick Flag set by {@link #signalStop()} */
    private boolean shouldStop;

    private long  expected;
    private final int   messageBufferSize;
    private final Class<?> caller;
    
    public LoggerProgressInfoImpl(Properties someProperties) {
        this.messageBufferSize = Integer.parseInt(someProperties.getProperty("messageBufferSize", String.valueOf(MESSAGE_BUFFER_SIZE)));
        try {
            this.caller        = Class.forName(someProperties.getProperty("caller", this.getClass().getName()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        long theExp            = Long.parseLong(someProperties.getProperty("expected", "1000"));
        this.reset(theExp);
    }
    
    public LoggerProgressInfoImpl(Class aCaller, long expected) {
        this.caller   = aCaller;
        this.messageBufferSize = MESSAGE_BUFFER_SIZE;
        
        this.reset(expected);
    }
    
    @Override
	public final void info(String aMessage) {
        this.log(aMessage, null, this.caller, INFO);
    }
    
    @Override
	public final void info(String aMessage, Class aCaller) {
        this.log(aMessage, null, aCaller, INFO);
    }

    @Override
	public final void info(String aMessage, Throwable anException) {
        this.log(aMessage, anException, this.caller, INFO);
    }
    
    @Override
	public final void info(String aMessage, Throwable anException, Class aCaller) {
        this.log(aMessage, anException, aCaller, INFO);
    }

    @Override
	public final void warn(String aMessage) {
        this.log(aMessage, null, this.caller, WARN);
    }
    
    @Override
	public final void warn(String aMessage, Class aCaller) {
        this.log(aMessage, null, aCaller, WARN);
    }
    
    @Override
	public final void warn(String aMessage, Throwable anException) {
        this.log(aMessage, anException, this.caller, WARN);
    }

    @Override
	public final void warn(String aMessage, Throwable anException, Class aCaller) {
        this.log(aMessage, anException, aCaller, WARN);
    }

    @Override
	public final void error(String aMessage) {
        this.log(aMessage, null, this.caller, ERROR);
    }
    
    @Override
	public final void error(String aMessage, Class aCaller) {
        this.log(aMessage, null, aCaller, ERROR);
    }

    @Override
	public final void error(String aMessage, Throwable anException) {
        this.log(aMessage, anException, this.caller, ERROR);
    }
    
    @Override
	public final void error(String aMessage, Throwable anException, Class aCaller) {
        this.log(aMessage, anException, aCaller, ERROR);
    }

    @Override
	public final void debug(String aMessage) {
        this.log(aMessage, null, this.caller, DEBUG);
    }
    
    @Override
	public final void debug(String aMessage, Class aCaller) {
        this.log(aMessage, null, aCaller, DEBUG);
    }

    @Override
	public final void debug(String aMessage, Throwable anException) {
        this.log(aMessage, anException, this.caller, DEBUG);
    }
    
    @Override
	public final void debug(String aMessage, Throwable anException, Class aCaller) {
        this.log(aMessage, anException, aCaller, DEBUG);
    }
    
    @Override
	public final void setFinished(String aMessage) {
        this.setFinished(aMessage, null, this.caller);
    }
    
    @Override
	public final void setFinished(String aMessage, Class aCaller) {
        this.setFinished(aMessage, null, aCaller);
    }

    @Override
	public final void setFinished(String aMessage, Throwable anException) {
        this.setFinished(aMessage, anException, this.caller);
    }
    
    @Override
	public final void setFinished(String aMessage, Throwable anException, Class aCaller) {
        this.finished = true;
        this.log(aMessage, anException, aCaller, INFO);
    }
    
    private void log(String aMessage, Throwable anException, Class aCaller, int aPrio) {
        
        if (aCaller == null) {
            aCaller = this.getClass();
        }
        
        switch (aPrio) {
            case DEBUG:
                Logger.debug(aMessage, anException, aCaller);
                break;
            case INFO:
                Logger.info(aMessage, anException, aCaller);
                break;
            case WARN:
                Logger.warn(aMessage, anException, aCaller);
                break;
            case ERROR:
                Logger.error(aMessage, anException, aCaller);
                break;
            case FATAL:
                Logger.fatal(aMessage, anException, aCaller);
                break;
            default:
                Logger.info(aMessage, anException, aCaller);
                break;
        }
        
        this.appendMessage(aMessage, anException, aCaller, aPrio);
    }
    
    protected void appendMessage(String aMessage, Throwable anException, Class aCaller, int aPrio) {
        if (anException != null) {
            if (aMessage != null) {
                aMessage += ": " + anException.toString();
            }
            else {
                aMessage = anException.toString();
            }
        }
        
        if (aMessage != null) {
            this.getMessageBuffer().append(aMessage);
        }
    }
    
    @Override
	public final void reset(long expected) {
        this.expected = expected == 0 ? 1 : expected;
        this.current  = 1;
        this.finished = false;
        this.refreshInterval = 1;
        this.clearMessageBuffer();
    }
    
    @Override
	public final void increaseProgress() {
        this.current++;
    }
    
    @Override
	public final long getCurrent() {
        return this.current;
    }

    @Override
	public final long getExpected() {
        return this.expected;
    }

    @Override
	public final String getMessage() {
        String theMessage = this.getMessageBuffer().toString();
        if (! this.isFinished()) {
            this.clearMessageBuffer();
        }
        return theMessage;
    }

    @Override
	public final float getProgress() {
        return this.current / this.expected * 100f;
    }

    @Override
	public final int getRefreshSeconds() {
        return this.refreshInterval;
    }

    @Override
	public final boolean isFinished() {
        return this.finished;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Will set {@link #shouldStop}.
     */
    @Override
	public boolean signalStop() {
        return shouldStop = true;
    }

    /**
     * Return true when {@link #signalStop()} was called.
     * 
     * In this case you should stop processing as soon as possible.
     */
	@Override
	public boolean shouldStop() {
        return shouldStop;
    }

    
    protected final StringBuffer getMessageBuffer() {
        if (this.message == null) {
            this.clearMessageBuffer();
        }
        return this.message;
    }
    
    private void clearMessageBuffer() {
        this.message = new StringBuffer(this.messageBufferSize);
    }
}

