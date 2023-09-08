/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import com.top_logic.basic.util.Stoppable;

/**
 * An interface used by the Progress Component to show the progress of an operation.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public interface ProgressInfo extends Stoppable {
    
    /** Minimum number of seconds allowed for a refresh */
    public static final int MIN_REFRESH = 1;
    
    /** Maximum number of seconds allowed for a refresh */
    public static final int MAX_REFRESH = 60;

	/**
	 * Default Factor used by implementations when checking there alive state.
	 * 
	 * When a ProgressInfo was not called for a time longer than STOP_IF_NOT_ALIVE_FACTOR *
	 * {@link #getRefreshSeconds()} it can automatically stop (assuming the Task should run only
	 * when observed).
	 * 
	 * @see DefaultProgressInfo#shouldStop(long, ProgressInfo)
	 */
	public static final long STOP_IF_NOT_ALIVE_FACTOR = 20 * 1000;
	// *1000 as getRefreshSeconds are seconds

    /** 
     * The number of seconds until a refresh should happen.
     * 
     * Assume this will be called only once.
     *
     * @return a value from 1..60 , other values will be ignored.
     */ 
    public int getRefreshSeconds();

    /** Total number of things (bytes, objects, ...) expected */ 
    public long getExpected();

    /** actual number of things (bytes, objects, ...) expected [0..getExpected()] */ 
    public long getCurrent();

    /** get Progress as percentage in the range [0.0 .. 100.0] */ 
    public float getProgress();

    /** An (optional) message to show to user 
     *
     * @return the message or null if there is no.
     */ 
    public String getMessage();
    
    /** Return true when Task is finished some way. */
    public boolean isFinished();

}
