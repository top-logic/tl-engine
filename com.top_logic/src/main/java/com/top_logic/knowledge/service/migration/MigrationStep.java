/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * A single step of a data migration, committed together with the {@link MigrationProgress} of
 * the migration.
 *
 * <p>
 * A step either writes through the connection of the migration, which is committed together with
 * the progress after the step, or it writes in a {@link KnowledgeBase} transaction of its own
 * (see {@link #inKnowledgeBaseTransaction()}), in which it {@link Completion#record(PooledConnection)
 * records} its completion.
 * </p>
 *
 * @see MigrationProgress#execute(Protocol, PooledConnection, MigrationContext, List, int)
 * @see MigrationProgress#executeRemaining(Protocol, List)
 */
public interface MigrationStep {

	/**
	 * Description of this step in messages, naming the migration it belongs to.
	 */
	String getName();

	/**
	 * Whether this step writes in a {@link KnowledgeBase} transaction of its own instead of the
	 * connection of the migration.
	 *
	 * <p>
	 * Such a step writes no DDL. It is not marked as started before it is executed, and it must
	 * {@link Completion#record(PooledConnection) record} its completion through the connection of
	 * its transaction before committing it.
	 * </p>
	 */
	boolean inKnowledgeBaseTransaction();

	/**
	 * Performs the changes of this step.
	 *
	 * <p>
	 * A step writing through the connection of the migration does not commit its changes. A
	 * failure is reported by an exception or an error in the log of the migration.
	 * </p>
	 *
	 * @param completion
	 *        Records the completion of this step in the progress of the migration.
	 */
	void run(Completion completion) throws Exception;

	/**
	 * Creates a {@link MigrationStep} writing through the connection of the migration.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param action
	 *        See {@link #run(Completion)}.
	 */
	static MigrationStep create(String name, Action action) {
		return create(name, false, action);
	}

	/**
	 * Creates a {@link MigrationStep} writing in a {@link KnowledgeBase} transaction of its own.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param action
	 *        See {@link #run(Completion)}.
	 *
	 * @see #inKnowledgeBaseTransaction()
	 */
	static MigrationStep createInKnowledgeBase(String name, Action action) {
		return create(name, true, action);
	}

	private static MigrationStep create(String name, boolean inKnowledgeBase, Action action) {
		return new MigrationStep() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public boolean inKnowledgeBaseTransaction() {
				return inKnowledgeBase;
			}

			@Override
			public void run(Completion completion) throws Exception {
				action.run(completion);
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
		 * See {@link MigrationStep#run(Completion)}.
		 */
		void run(Completion completion) throws Exception;

	}

	/**
	 * Records the completion of a {@link MigrationStep} in the progress of the migration.
	 */
	interface Completion {

		/**
		 * Writes the progress with the step completed to the given connection, without committing
		 * it.
		 *
		 * <p>
		 * After the last step of the migration, the progress is deleted instead.
		 * </p>
		 *
		 * @param connection
		 *        The connection whose commit completes the step.
		 */
		void record(PooledConnection connection) throws SQLException;

	}

}
