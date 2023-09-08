/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * Adaptor implementation for the {@link Protocol} interface that dispatches all
 * calls to another {@link Protocol} implementation instance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ProtocolAdaptor extends AbstractProtocolAdaptor {

	private Protocol _impl;

	/**
	 * Creates a new {@link Protocol} that dispatches all calls to the given
	 * implementation.
	 * 
	 * @param impl
	 *        the {@link Protocol} implementation all calls are redirected to.
	 */
	public ProtocolAdaptor(Protocol impl) {
		setProtocolImplementation(impl);
	}
	
	/**
	 * The underlying {@link Protocol}.
	 */
	public Protocol getProtocolImplementation() {
		return _impl;
	}

	/**
	 * @see #getProtocolImplementation()
	 */
	public final void setProtocolImplementation(Protocol implementation) {
		checkNotNull(implementation);
		_impl = implementation;
	}

	private void checkNotNull(Protocol implementation) {
		if (implementation == null) {
			throw new IllegalArgumentException("Protocol must not be null.");
		}
	}

	@Override
	protected Protocol impl() {
		return _impl;
	}

}
