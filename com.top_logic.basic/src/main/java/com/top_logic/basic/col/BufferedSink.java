/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;



/**
 * Buffered {@link Sink} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BufferedSink<T> implements Sink<T>, Closeable {

	final List<T> _data = new ArrayList<>();

	private boolean _closed;

	/**
	 * Creates a {@link BufferedSink}.
	 */
	public BufferedSink() {
		super();
	}
	
	@Override
	public void add(T value) {
		if (_closed) {
			throw new IllegalStateException("Buffer already closed.");
		}
		_data.add(value);
	}

	/**
	 * Access to the already collected data.
	 */
	public List<T> getData() {
		return _data;
	}

	@Override
	public void close() {
		_closed = true;
	}

}