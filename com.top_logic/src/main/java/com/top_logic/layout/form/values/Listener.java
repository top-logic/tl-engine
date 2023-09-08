/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

/**
 * Observer of an {@link Observable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Listener {

	/**
	 * Notifies about a change in the given {@link Value}.
	 * 
	 * @param sender
	 *        The changed value.
	 */
	void handleChange(Value<?> sender);

}
