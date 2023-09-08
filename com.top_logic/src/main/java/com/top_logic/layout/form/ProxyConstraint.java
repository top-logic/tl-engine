/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.Collection;

/**
 * Proxy class for a constraint
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ProxyConstraint implements Constraint {

	@Override
	public boolean check(Object value) throws CheckException {
		return getImpl().check(value);
	}

	@Override
	public Collection<FormField> reportDependencies() {
		return getImpl().reportDependencies();
	}

	/**
	 * the constraint implementation to dispatch methods to.
	 */
	protected abstract Constraint getImpl();

}

