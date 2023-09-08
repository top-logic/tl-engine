/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

/**
 * The class {@link QueryResultAdapter} delegates all {@link QueryResult} methods to an
 * implementation.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class QueryResultAdapter implements QueryResult {

	/**
	 * Returns the {@link QueryResult} which is used to dispatch methods to. Never <code>null</code>
	 * .
	 */
	protected abstract QueryResult getImplementation();

	@Override
	public boolean next() throws SQLException {
		return getImplementation().next();
	}

	@Override
	public void close() throws SQLException {
		getImplementation().close();
	}

}

