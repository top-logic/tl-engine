/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;


/**
 * Standard SQL format for binary data.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class SLQBlobFormat extends AbstractBlobFormat {

	/**
	 * Singleton {@link SLQBlobFormat} instance.
	 */
	public static final SLQBlobFormat INSTANCE = new SLQBlobFormat();

	private SLQBlobFormat() {
		// Singleton constructor.
	}

	@Override
	protected void appendStopQuote(StringBuffer buffer) {
		buffer.append("'");
	}

	@Override
	protected void appendStartQuote(StringBuffer buffer) {
		buffer.append("X'");
	}

}