/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link Protocol} delegating to another implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractProtocolAdaptor extends LogAdaptor implements Protocol {

	@Override
	public void checkErrors() {
		impl().checkErrors();
	}

	@Override
	public RuntimeException fatal(String message) {
		return impl().fatal(message);
	}

	@Override
	public RuntimeException fatal(String message, Throwable ex) {
		return impl().fatal(message, ex);
	}

	/**
	 * Provides access to the actual {@link Protocol} implementation.
	 */
	@Override
	protected abstract Protocol impl();

}
