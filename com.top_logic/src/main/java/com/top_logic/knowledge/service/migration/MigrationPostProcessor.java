/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import com.top_logic.basic.Log;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * High-level migration processor that operates directly on the persistency layer.
 * 
 * <p>
 * Operations performed in a {@link MigrationPostProcessor} cannot modify the history of an
 * application database. {@link MigrationPostProcessor} can only add changes to the current state of
 * the application database after a potential replay migration.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MigrationPostProcessor {

	/**
	 * Performs a migration step by adding a versioned change to the given {@link KnowledgeBase}.
	 * 
	 * <p>
	 * This operation is performed in a transaction context and must not start its own transaction.
	 * The operation must solely rely on the persistency layer being started, not any other modules
	 * such as the model service.
	 * </p>
	 *
	 * @param log
	 *        An error log.
	 * @param kb
	 *        The {@link PersistencyLayer} being migrated.
	 */
	void afterMigration(Log log, KnowledgeBase kb);

}
