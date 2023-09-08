/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Collection;
import java.util.List;

/**
 * Generic listener storage for implementing observables that cannot inherit
 * {@link ObservableBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Listeners extends ObservableBase {

	/**
	 * @see #addListener(Class, Object)
	 */
	public final <T> boolean add(Class<T> listenerInterface, T listener) {
		return addListener(listenerInterface, listener);
	}
	
	/**
	 * @see #removeListener(Class, Object)
	 */
	public final <T> boolean remove(Class<T> listenerInterface, T listener) {
		return removeListener(listenerInterface, listener);
	}
	
	/**
	 * @see #getListeners(Class)
	 */
	public final <T> List<T> get(Class<T> listenerInterface) {
		return getListeners(listenerInterface);
	}
	
	/**
	 * @see #getListenersDirect(Class)
	 */
	public final <T> Collection<T> getDirect(Class<T> listenerInterface) {
		return getListenersDirect(listenerInterface);
	}

	/**
	 * @see #hasListeners(Class)
	 */
    public final <T> boolean has(Class<T> listenerInterface) {
    	return hasListeners(listenerInterface);
    }

    /**
     * @see #hasListener(Class, Object)
     */
    public final <T> boolean has(Class<T> listenerInterface, T someListener) {
        return hasListener(listenerInterface, someListener);
    }

	
}
