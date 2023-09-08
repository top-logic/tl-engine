/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.SQLException;
import java.util.Properties;

import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link DataSourceProxy} for H2 database.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class H2DataSourceProxy extends ReadOnlySupportDataSource {

	private static final String DEFAULT_DRIVER_NAME = "org.h2.jdbcx.JdbcDataSource";

	/**
	 * Creates a new {@link H2DataSourceProxy}.
	 */
	public H2DataSourceProxy(Properties config) throws SQLException, ConfigurationException {
		super(DEFAULT_DRIVER_NAME, config);
	}

}

