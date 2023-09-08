/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * {@link DataSourceProxy} for the WebSphere Application Server.
 * 
 * @author <a href="mailto:aru@top-logic.com">aru</a>
 */
public class WASDataSourceJndiWrapper extends DataSourceProxy {

	private DataSource impl;

	@Override
	protected DataSource impl() {
		return impl;
	}

	public void setJndiImpl(String jndiName) {
		impl = SQLH.fetchJNDIDataSource(jndiName);
	}

	@Override
	public Connection getConnection() throws SQLException {
		final Connection connectionImpl = super.getConnection();
		return new ConnectionProxy() {
			private boolean readOnly;

			@Override
			protected Connection impl() throws SQLException {
				return connectionImpl;
			}

			@Override
			public void setReadOnly(boolean aReadOnly) throws SQLException {
				readOnly = aReadOnly;
			}

			@Override
			public boolean isReadOnly() throws SQLException {
				return readOnly;
			}
		};
	}

}
