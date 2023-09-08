/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;


/**
 * Command container associated with a {@link CheckScope}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CheckScoped {

	/**
	 * {@link CheckScope} to test for changes before executing internal commands.
	 */
	public CheckScope getCheckScope();

}
