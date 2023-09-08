/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

/**
 * List of listeners that can be notified.
 * 
 * <p>
 * Modular variant of {@link AbstractObservable} that can be used through a public API instead of
 * providing services for inheriting classes.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractListeners<L, E> extends AbstractObservable<L, E> {

	@Override
	public final boolean addListener(L listener) {
		return super.addListener(listener);
	}

	@Override
	public final boolean removeListener(L listener) {
		return super.removeListener(listener);
	}

	@Override
	public final void notifyListeners(E event) {
		super.notifyListeners(event);
	}

	/**
	 * Whether any listener is registered.
	 */
	public final boolean hasRegisteredListeners() {
		return hasListeners();
	}

	/**
	 * Whether the given listener is registered.
	 */
	public final boolean isListenerRegistered(L listener) {
		return hasListener(listener);
	}
}
