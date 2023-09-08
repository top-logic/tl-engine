/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * {@link InvalidationListener} which doesn't care about invalidation.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public final class IgnoreInvalidation implements InvalidationListener {

	/**
	 * Singleton instance
	 */
	public static final InvalidationListener INSTANCE = new IgnoreInvalidation();

	private IgnoreInvalidation() {
		// singleton
	}

	/**
	 * ignores the invalidation
	 * 
	 * @see com.top_logic.layout.InvalidationListener#notifyInvalid(Object)
	 */
	@Override
	public void notifyInvalid(Object invalidObject) {
		// nothing to do here
	}
}