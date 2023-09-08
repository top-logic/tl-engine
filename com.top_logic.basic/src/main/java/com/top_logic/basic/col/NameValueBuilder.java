/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Builder interface for collecting name/value pairs.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NameValueBuilder {

	/**
	 * Record a name/value assignment.
	 * 
	 * @param name
	 *        The name to associate a value with.
	 * @param value
	 *        The value to store.
	 * @return This instance for call chaining.
	 */
	public NameValueBuilder setValue(String name, Object value);

}
