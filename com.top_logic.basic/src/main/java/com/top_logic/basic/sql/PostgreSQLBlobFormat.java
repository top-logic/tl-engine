/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

/**
 * PostgreSQL specific format for binary literals.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class PostgreSQLBlobFormat extends AbstractBlobFormat {

	/**
	 * Singleton {@link PostgreSQLBlobFormat} instance.
	 */
	public static final PostgreSQLBlobFormat INSTANCE = new PostgreSQLBlobFormat();

	@Override
	protected void appendStopQuote(StringBuffer buffer) {
		buffer.append("'::bytea");
	}

	@Override
	protected void appendStartQuote(StringBuffer buffer) {
		buffer.append("'\\x");
	}

}
