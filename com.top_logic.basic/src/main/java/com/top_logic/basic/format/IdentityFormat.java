/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

/**
 * A dummy format that simply converts its value to a string as output and does no parsing at all.
 *
 * <p>
 * Can be used as inner format in a {@link ListFormat}, if no formatting of the parsed tokens is
 * required.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IdentityFormat extends AbstractStringFormat {

	/**
	 * Singleton {@link IdentityFormat} instance.
	 */
	public static final IdentityFormat INSTANCE = new IdentityFormat();

	private IdentityFormat() {
		// Singleton constructor.
	}
	
	@Override
	protected String format(String input) {
		return input;
	}

}
