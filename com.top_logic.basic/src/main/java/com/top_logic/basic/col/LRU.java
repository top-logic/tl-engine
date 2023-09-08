/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Simple interface for LRU objects ("Last Recently Used").
 * 
 * <p>
 * This objects can be used as a cache. 
 * Unlike {@link java.lang.ref.Reference},
 * the instances will care about the deletion of data by themself. The
 * Garbage Collector is not able to catch them!</p>
 * 
 * <p>This interface will be used by the 
 * {@link com.top_logic.basic.col.LRUWatcher}, who cares about the 
 * removing of expired objects.</p>
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface LRU {

    /**
     * Return the time in milliseconds, the instance has to be cleared next.
     * 
     * @return    The time in milliseconds or "0", if no objects contained.
     */
    public long nextExpiration();

    /**
     * Clear the expired objects.
     * 
     * @return the result of {@link #nextExpiration()}.
     */
    public long removeExpired();
}
