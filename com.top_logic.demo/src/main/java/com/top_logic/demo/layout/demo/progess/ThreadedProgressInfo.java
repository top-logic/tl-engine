/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.progess;

import java.util.Date;

import com.top_logic.basic.Logger;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.progress.ProgressInfo;

/**
 * Simple {@link Thread}-based implementation of ProgressInfo.
 * 
 * Some Ideas of this class are not suitable for real world usage.
 * 
 * Better have a look at {@link DefaultProgressInfo}.
 * 
 * @author <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class ThreadedProgressInfo extends Thread implements ProgressInfo {

    /** Expected number as given by CTor. */
    protected long expected;

    /** Number of seconds between refresh, as given by CTor. */
    protected int  refreshSeconds;

    /** Current number in the Range from [0.. expected] */
    protected long current;

    /** increment current by this value until it reaches expected */
    protected long inc;
    
	/** String accumulated via {@link #getMessage()} */
    private String wholeMessage;
    
	/** Stick Flag set by {@link #signalStop()} */
	private boolean shouldStop;

	/** last Time when {@link #getProgress()} was called */
	private long lastAlive;

	/**
	 * Should the generated message span multiple lines?
	 */
	private final boolean _multiLineMessage;

    /** 
     * Create a new ThreadedProgressInfo.
     */
	public ThreadedProgressInfo(String threadName, long aExpected, int anIncrement, int aRefreshSeconds,
			boolean multiLineMessage) {
        super(threadName);
        expected       = aExpected;
        inc            = anIncrement;
        refreshSeconds = aRefreshSeconds;
        wholeMessage   = "";
		lastAlive = -1;
		_multiLineMessage = multiLineMessage;
    }

    /** 
     * Demo implementation.
     */
    @Override
	public long getCurrent() {
        return current;
    }

    /** 
     * Demo implementation.
     */
    @Override
	public long getExpected() {
        return expected;
    }

    /** 
     * Demo implementation.
     */
    @Override
	public String getMessage() {
		if (shouldStop) {
			wholeMessage = getMessagePrefix() + "Should Stop\n";
		} else {
			wholeMessage = getMessagePrefix() + new Date().toString() + " " + getProgress();
		}
        return wholeMessage;
    }

	private String getMessagePrefix() {
		if (_multiLineMessage) {
			return wholeMessage + "\n";
		}
		return "";
	}

    /** 
     * Demo implementation.
     */
    @Override
	public float getProgress() {
		lastAlive = System.currentTimeMillis();
        return 100.0f * current / expected ;
    }

    /** 
     * Demo implementation.
     */
    @Override
	public int getRefreshSeconds() {
        return refreshSeconds;
    }

    /** 
     * Demo implementation.
     */
    @Override
	public boolean isFinished() {
		return current >= expected || shouldStop();
    }

    /** 
     * run() until finished.
     */
    @Override
	public void run() {
        while (!isFinished()) {
            current += inc;
            try {
                sleep(refreshSeconds * 1000);
            } catch (InterruptedException iex) {
               Logger.error("Unexpected", iex, ThreadedProgressInfo.class);
            }
        }
        if (shouldStop) {
			Logger.info("Stopped", ThreadedProgressInfo.class);
        }
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
	public synchronized boolean shouldStop() {
		return shouldStop
			|| (shouldStop = DefaultProgressInfo.shouldStop(lastAlive, this));
	}


}