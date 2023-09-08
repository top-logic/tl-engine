/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * A {@link Renderer} wrapper that safely wraps any {@link Renderer} to allow calling it with values
 * of any type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class SafeRenderer<T> implements Renderer<Object> {
	private final Class<T> _availableType;

	private Renderer<T> _impl;

	/**
	 * Creates a {@link SafeRenderer}.
	 * 
	 * @see Renderer#generic(Class)
	 */
	SafeRenderer(Class<T> availableType, Renderer<T> impl) {
		_availableType = availableType;
		_impl = impl;
	}

	/**
	 * The wrapped {@link Renderer} instance.
	 */
	public Renderer<T> getImpl() {
		return _impl;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		if (_availableType.isInstance(value)) {
			@SuppressWarnings({ "unchecked" })
			T compatibleValue = (T) value;
			_impl.write(context, out, compatibleValue);
		}
	}

	@Override
	public <X> Renderer<? super X> generic(Class<X> otherType) {
		return this;
	}
}