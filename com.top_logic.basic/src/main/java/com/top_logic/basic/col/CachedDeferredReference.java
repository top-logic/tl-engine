/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * An implementation of {@link DeferredReference} that caches a reference to the
 * Deferred object after it is accessed the first time.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */					
public abstract class CachedDeferredReference<T> implements DeferredReference<T> {
	
    private boolean isCreated;
	private T       cachedReference;
	
	@Override
	public synchronized final T get() {
		if (! isCreated) {
			isCreated = true;
			this.cachedReference = initInstance();
		}
		return cachedReference;
	}

	protected abstract T initInstance();

}
