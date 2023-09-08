/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link MSSQLHelper} for MSSQLServer with version &lt;= 9.0 (MSSQLServer 2005).
 * 
 * <p>
 * Note: This is currently just a Marker helper to detect an old version and skip tests that do not
 * work with MSSQL 2005.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MSSQLHelper90 extends MSSQLHelper {

	/**
	 * Creates a new MSSQLHelper90.
	 */
	public MSSQLHelper90(InstantiationContext context, Config config) {
		super(context, config);
	}

}

