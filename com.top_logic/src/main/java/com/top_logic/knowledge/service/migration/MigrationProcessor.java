/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import com.top_logic.basic.Log;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Processor executed <b>before</b> the actual database replay. All {@link MigrationProcessor}s of
 * all migration scripts are executed before the first replay is executed, therefore they must be
 * completely independent of eventually follow-up rewriter migrations.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MigrationProcessor {

	/**
	 * Executes the actual migration.
	 * 
	 * @param log
	 *        The {@link Log} to log informations and potential errors to.
	 * @param connection
	 *        The connection to the database to modify
	 */
	void doMigration(Log log, PooledConnection connection);

	/**
	 * Whether the migration of this {@link MigrationProcessor} should be performed "out of band"
	 * after other migrations.
	 * 
	 * <p>
	 * {@link MigrationProcessor}s must be deferred, if they try to parse configurations stored in
	 * the database, because code migrations must have been applied to those configurations before.
	 * </p>
	 */
	default boolean isDeferred() {
		return false;
	}

}

