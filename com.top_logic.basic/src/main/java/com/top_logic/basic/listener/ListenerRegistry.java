/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import java.lang.ref.WeakReference;

/**
 * Informs {@link Listener}s when an event they are listening for is happening.
 * <p>
 * {@link Listener}s are usually called <b>after</b> the event, unless stated explicitly otherwise.
 * <br/>
 * {@link Listener}s are informed in their registration order. <br/>
 * Depending on the implementation, {@link Listener}s are stored via {@link WeakReference}s or
 * directly. If they are stored directly, forgotten listeners are a memory leak. <br/>
 * Implementations can be thread safe or not.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface ListenerRegistry<T> {

	/**
	 * The behavior is unspecified if the {@link Listener} is registered twice.
	 * <p>
	 * This limitation strongly reduces the synchronization overhead for the
	 * weak-concurrent-version. Also, it should not happen that someone wants to register a listener
	 * but does not know whether it is already registered or not.
	 * </p>
	 * 
	 * @throws NullPointerException
	 *         if the given {@link Listener} is <code>null</code>.
	 */
	void register(Listener<? super T> newListener);

	/**
	 * Unregisters the given {@link Listener}.
	 * <p>
	 * If the {@link Listener} is not registered, nothing happens.
	 * </p>
	 * 
	 * @throws NullPointerException
	 *         if the given {@link Listener} is <code>null</code>.
	 */
	void unregister(Listener<? super T> oldListener);

	/**
	 * Notifies the {@link Listener}s that the event they are listening for is happening.
	 * <p>
	 * Whether <code>null</code> is a valid notification has to be specified by the
	 * {@link ListenerRegistry} instance.
	 * </p>
	 */
	void notify(T notification);

}