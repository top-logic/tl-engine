/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;


/**
 * {@link GenericValueDependency} of two properties of the same type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ValueDependency<T> extends GenericValueDependency<T, T> {

	/**
	 * Creates a {@link ValueDependency}.
	 * 
	 * @param type
	 *        The expected type of the two properties to relate.
	 */
	public ValueDependency(Class<T> type) {
		super(type, type);
	}

}
