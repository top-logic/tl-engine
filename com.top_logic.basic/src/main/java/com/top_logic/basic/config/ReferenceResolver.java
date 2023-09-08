/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Callback interface for resolving a configuration reference.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ReferenceResolver<T> {

	/**
	 * Called when the reference requested by the call to
	 * {@link InstantiationContext#resolveReference(Object, Class, ReferenceResolver)} becomes
	 * available.
	 * 
	 * @param value
	 *        The target value of the reference.
	 */
	void setReference(T value);

}
