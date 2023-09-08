/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.text.Format;
import java.text.ParseException;

/**
 * An adapter for using {@link Format}s where {@link TLFormat}s are required.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TLFormatAdapter<T> implements TLFormat<T> {

	private final Format _inner;

	private final Class<T> _type;

	/**
	 * Create a {@link TLFormatAdapter} with the given inner {@link Format}.
	 * <p>
	 * None of the parameters is allowed to be <code>null</code>.
	 * </p>
	 */
	public TLFormatAdapter(Format inner, Class<T> type) {
		if ((inner == null) || (type == null)) {
			throw new NullPointerException(); // Fail early
		}
		_inner = inner;
		_type = type;
	}

	@Override
	public T parse(String formattedValue) throws ParseException {
		return _type.cast(_inner.parseObject(formattedValue));
	}

	@Override
	public String format(T value) {
		return _inner.format(value);
	}

	/** Getter for the inner {@link Format}. Never <code>null</code>. */
	public Format getInner() {
		return _inner;
	}

	/** Getter for result type of {@link #parse(String)}. Never <code>null</code>. */
	public Class<T> getType() {
		return _type;
	}

}
