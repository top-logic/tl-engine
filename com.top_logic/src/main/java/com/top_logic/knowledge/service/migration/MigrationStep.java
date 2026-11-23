/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.util.List;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.sql.PooledConnection;

/**
 * A single step of a data migration, committed together with the {@link MigrationProgress} of
 * the migration.
 *
 * @see MigrationProgress#execute(Protocol, PooledConnection, MigrationContext, List)
 */
public interface MigrationStep {

	/**
	 * Description of this step in messages, naming the migration it belongs to.
	 */
	String getName();

	/**
	 * Performs the changes of this step on the connection of the migration.
	 *
	 * <p>
	 * The changes are not committed, a failure is reported by an exception or an error in the log
	 * of the migration.
	 * </p>
	 */
	void run() throws Exception;

	/**
	 * Creates a {@link MigrationStep}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param action
	 *        See {@link #run()}.
	 */
	static MigrationStep create(String name, Action action) {
		return new MigrationStep() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public void run() throws Exception {
				action.run();
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	/**
	 * The changes of a {@link MigrationStep}.
	 */
	interface Action {

		/**
		 * See {@link MigrationStep#run()}.
		 */
		void run() throws Exception;

	}

}
