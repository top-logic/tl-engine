/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.CreateTablesContext;
import com.top_logic.knowledge.service.DBSetupAction;
import com.top_logic.knowledge.service.DropTablesContext;

/**
 * {@link DBSetupAction} installing the correct database versions into the {@link DBProperties}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InitVersionActions implements DBSetupAction {

	@Override
	public void doCreateTables(CreateTablesContext context) throws Exception {
		Collection<Version> maximalVersions = MigrationUtil.maximalVersions(context);
		if (maximalVersions.isEmpty()) {
			return;
		}
		ConnectionPool pool = context.getConnectionPool();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			MigrationUtil.updateStoredVersions(context, connection, maximalVersions);
			context.checkErrors();
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	@Override
	public void doDropTables(DropTablesContext context, PrintWriter out) throws Exception {
		// nothing to drop here
	}

	@Override
	public Collection<? extends BasicRuntimeModule<?>> getDependencies() {
		return Collections.emptyList();
	}

}

