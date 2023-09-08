/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.display;

import com.top_logic.basic.col.DeferredReference;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Future object for a call to the accessor method
 * {@link Wrapper#getValue(String)} of a {@link Wrapper} property.
 * 
 * @see "http://de.wikipedia.org/wiki/Future (Programmierung)" 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrapperProperty<T> implements DeferredReference<T> {

	/**
	 * @see #getWrapper()
	 */
	private final Wrapper wrapper;
	
	/**
	 * @see #getPropertyName()
	 */
	private final String property;

	/**
	 * Create a new future for accessing the given property of the given
	 * {@link Wrapper}.
	 */
	public WrapperProperty(Wrapper wrapper, String property) {
		this.wrapper = wrapper;
		this.property = property;
	}

	/**
	 * The accessed {@link Wrapper}.
	 */
	public String getPropertyName() {
		return property;
	}

	/**
	 * The accessed property name.
	 */
	public Wrapper getWrapper() {
		return wrapper;
	}
	
	/**
	 * Returns the result of the actual access to
	 * {@link Wrapper#getValue(String)}.
	 * 
	 * @see "java.util.concurrent.Future#get()"
	 */
	@Override
	@SuppressWarnings("unchecked")
    public T get() {
		return (T) wrapper.getValue(property);
	}
	
}
