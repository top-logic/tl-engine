/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * A {@link DeferredReference} is a handle to an object that might be created or initialized on
 * demand.
 * 
 * @param <T>
 *        The type of the referenced object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DeferredReference<T> {
	
    /**
	 * Provide access to the object, for which the creation or initialization
	 * was deferred.
	 * 
	 * <p>
	 * After this method returns, the resulting object is guaranteed to be
	 * completely initialized. Multiple calls to {@link #get()} are
	 * expected to return the same object, or at least an object that is
	 * {@link Object#equals(Object) equal} to the object returned by the first
	 * call.
	 * </p>
	 * 
	 * @return The object, for which this instance is a handler for.
	 */
	public T get();
}
