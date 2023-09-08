/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

/**
 * Proxy for a constraint that dispatches to an specific implementation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleProxyConstraint extends ProxyConstraint {

	private final Constraint _impl;

	/**
	 * Creates a new {@link SimpleProxyConstraint}.
	 * 
	 * @param impl
	 *        the actual implementation. Must not be <code>null</code>.
	 */
	public SimpleProxyConstraint(Constraint impl) {
		_impl = impl;
	}

	@Override
	protected Constraint getImpl() {
		return _impl;
	}

}

