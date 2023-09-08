/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.SQLException;
import java.util.Properties;

import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link DataSourceProxy} for MS-SQL server.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MSSQLDataSourceProxy extends ReadOnlySupportDataSource {

	private static final String DEFAULT_DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDataSource";

	/**
	 * Creates a new {@link MSSQLDataSourceProxy}.
	 */
	public MSSQLDataSourceProxy(Properties config) throws SQLException, ConfigurationException {
		super(DEFAULT_DRIVER_NAME, config);
	}

}
