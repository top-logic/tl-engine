/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import com.top_logic.basic.tools.NameBuilder;

/**
 * A proxy for a {@link Runnable}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class RunnableProxy<T extends Runnable> implements Runnable {

	private final T _inner;

	/**
	 * Creates a {@link RunnableProxy}.
	 * 
	 * @throws NullPointerException
	 *         If the given inner {@link Runnable} is null.
	 */
	public RunnableProxy(T inner) throws NullPointerException {
		if (inner == null) {
			throw new NullPointerException("Inner runnable is not allowed to  be null.");
		}
		_inner = inner;
	}

	/** Never null. */
	protected T getInner() {
		return _inner;
	}

	@Override
	public String toString() {
		return NameBuilder.buildName(this, getInner().toString());
	}

}
