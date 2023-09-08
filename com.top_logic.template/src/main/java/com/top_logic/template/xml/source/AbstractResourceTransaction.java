/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for {@link ResourceTransaction} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractResourceTransaction implements ResourceTransaction {

	private boolean _closed;

	@Override
	public OutputStream open() throws IOException {
		checkClosed();
		return internalOpen();
	}

	@Override
	public void commit() throws IOException {
		checkClosed();
		internalCommit();
	}

	@Override
	public void close() throws IOException {
		if (!_closed) {
			_closed = true;

			internalClose();
		}
	}

	private void checkClosed() {
		if (_closed) {
			throw new IllegalStateException("Already closed.");
		}
	}

	/**
	 * Implementation of {@link #open()}.
	 * 
	 * <p>
	 * Called, if not already {@link #close() closed}.
	 * </p>
	 */
	protected abstract OutputStream internalOpen() throws IOException;

	/**
	 * Implementation of {@link #commit()}.
	 * 
	 * <p>
	 * Called, if not already {@link #close() closed}.
	 * </p>
	 */
	protected abstract void internalCommit() throws IOException;

	/**
	 * Implementation of {@link #close()}.
	 * 
	 * <p>
	 * Called, if not already {@link #close() closed}.
	 * </p>
	 */
	protected abstract void internalClose() throws IOException;

}
