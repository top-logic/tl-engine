/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

/**
 * Oracle specific format for binary literals.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MSSQLBlobFormat extends AbstractBlobFormat {

	/**
	 * Singleton {@link MSSQLBlobFormat} instance.
	 */
	public static final MSSQLBlobFormat INSTANCE = new MSSQLBlobFormat();

	private MSSQLBlobFormat() {
		// Singleton constructor.
	}

	@Override
	protected void appendStartQuote(StringBuffer buffer) {
		buffer.append("0x");
	}

	@Override
	protected void appendStopQuote(StringBuffer buffer) {
		// No end quote.
	}

}
