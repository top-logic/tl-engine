/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import java.lang.ref.WeakReference;

/**
 * The factory for creating a {@link ListenerRegistry}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class ListenerRegistryFactory {

	private static final ListenerRegistryFactory INSTANCE = new ListenerRegistryFactory();

	private ListenerRegistryFactory() {
		// Reduce visibility
	}

	/** Returns the singleton {@link ListenerRegistryFactory} instance. */
	public static ListenerRegistryFactory getInstance() {
		return INSTANCE;
	}

	/** Creates a thread safe implementation which uses {@link WeakReference}s. */
	public <T> ListenerRegistry<T> createWeakConcurrent() {
		return new WeakConcurrentListenerRegistry<>();
	}

	/** Creates a thread safe implementation that stores the listeners directly. */
	public <T> ListenerRegistry<T> createConcurrent() {
		return new ConcurrentListenerRegistry<>();
	}

	/** Creates an implementation which uses {@link WeakReference}s but is <b>not</b> thread safe. */
	public <T> ListenerRegistry<T> createWeak() {
		return new WeakListenerRegistry<>();
	}

	/** Creates an implementation that stores the listeners directly and is <b>not</b> thread safe. */
	public <T> ListenerRegistry<T> createSimple() {
		return new SimpleListenerRegistry<>();
	}

}
