/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool;

/**
 * Generates <em>unique</em> time-stamps even when called fast.
 * 
 * When using <code>System.currentTimeMillis()</code> especially under
 * windows you may encounter the same time twice when you need a unique
 * TimeStamp. This class helps around this by incrementing an internal counter.
 * It will (of course) fails when used too fast, well.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TimeStamp {

    /** tickes derived from System.currentTimeMillis() */
    private static long ticks;
    
    /** return a long based on System.currentTimeMillis() that is unique */
    public static final synchronized long getTick() {
        long now = System.currentTimeMillis();
        if (now > ticks) {
            return ticks = now;
        }
        else {
            return ++ticks;
        }
    }

    /** return a long based on System.currentTimeMillis() that is unique */
    public static final Long getLTick() {
		return Long.valueOf(getTick());
    }
}
