/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.db2.RevisionXref;

/**
 * Progress of a data migration, stored in the global {@link DBProperties database property}
 * {@value #PROGRESS_PROPERTY}.
 *
 * <p>
 * A migration consists of a sequence of {@link MigrationStep}s. Each step commits its changes
 * together with the progress record, so that a start after an interrupted migration continues
 * after the last completed step instead of starting all over. Before a step is executed, the
 * progress records that the step has started, in a commit of its own. A start finding a started
 * but not completed step repeats it only on a database whose DDL is transactional (see
 * {@link com.top_logic.basic.sql.DBHelper#supportsTransactionalDDL()}): Only there the
 * interruption has rolled back the step completely. Otherwise, the start fails, and the database
 * must be restored from a backup.
 * </p>
 *
 * <p>
 * A step writing in a {@link com.top_logic.knowledge.service.KnowledgeBase} transaction of its own
 * (see {@link MigrationStep#inKnowledgeBaseTransaction()}) writes the progress in that
 * transaction. It writes no DDL, so it is not marked as started: An interruption before its commit
 * executes it again at the next start, an interruption after its commit never executes it twice.
 * The post-processors and each startup action are such steps. The startup actions are executed
 * after the start of the application (see {@link #executeRemaining(Protocol, List)}), so that the
 * progress is kept until the last startup action is committed.
 * </p>
 *
 * <p>
 * The progress records the {@link #getPlan() plan} of the migration, i.e. the versions of the
 * migrations being applied, since the versions stored in the database do not tell which
 * migrations were pending, once the step storing the new versions is completed. It also records
 * the pending {@link RevisionXref} changes of the {@link MigrationContext}.
 * </p>
 */
public final class MigrationProgress {

	/**
	 * Name of the global {@link DBProperties database property} holding the progress record.
	 */
	public static final String PROGRESS_PROPERTY = "migration.progress";

	/**
	 * Root tag of the serialized {@link Record}.
	 */
	public static final String ROOT_TAG = "migration-progress";

	/**
	 * Separator between the module and the name of a version in the {@link Record#getPlan() plan}.
	 */
	private static final char MODULE_SEPARATOR = '/';

	/**
	 * Serialized form of a {@link MigrationProgress}.
	 */
	public interface Record extends ConfigurationItem {

		/**
		 * @see #getPlan()
		 */
		String PLAN = "plan";

		/**
		 * @see #getCompleted()
		 */
		String COMPLETED = "completed";

		/**
		 * @see #getStarted()
		 */
		String STARTED = "started";

		/**
		 * @see #getXRefInvalidated()
		 */
		String XREF_INVALIDATED = "xref-invalidated";

		/**
		 * @see #getXRefDeleted()
		 */
		String XREF_DELETED = "xref-deleted";

		/**
		 * Tag of an entry of {@link #getPlan()}.
		 */
		String VERSION_TAG = "version";

		/**
		 * Tag of an entry of {@link #getXRefInvalidated()} and {@link #getXRefDeleted()}.
		 */
		String TABLE_TAG = "table";

		/**
		 * Attribute holding the value of a list entry.
		 */
		String NAME_ATTRIBUTE = "name";

		/**
		 * The versions of the migrations being applied in their order, each as module and name
		 * separated by a slash.
		 */
		@Name(PLAN)
		@ListBinding(tag = VERSION_TAG, attribute = NAME_ATTRIBUTE)
		List<String> getPlan();

		/**
		 * @see #getPlan()
		 */
		void setPlan(List<String> value);

		/**
		 * The number of steps completed.
		 */
		@Name(COMPLETED)
		int getCompleted();

		/**
		 * @see #getCompleted()
		 */
		void setCompleted(int value);

		/**
		 * The number of the step that has been started but not completed, <code>0</code> if no
		 * step is running.
		 */
		@Name(STARTED)
		int getStarted();

		/**
		 * @see #getStarted()
		 */
		void setStarted(int value);

		/**
		 * The tables whose {@link RevisionXref} entries must be rebuilt.
		 *
		 * @see MigrationContext#getXRefInvalidated()
		 */
		@Name(XREF_INVALIDATED)
		@ListBinding(tag = TABLE_TAG, attribute = NAME_ATTRIBUTE)
		List<String> getXRefInvalidated();

		/**
		 * @see #getXRefInvalidated()
		 */
		void setXRefInvalidated(List<String> value);

		/**
		 * The dropped tables whose {@link RevisionXref} entries must be removed.
		 *
		 * @see MigrationContext#getXRefDeleted()
		 */
		@Name(XREF_DELETED)
		@ListBinding(tag = TABLE_TAG, attribute = NAME_ATTRIBUTE)
		List<String> getXRefDeleted();

		/**
		 * @see #getXRefDeleted()
		 */
		void setXRefDeleted(List<String> value);

	}

	private final Record _record;

	private MigrationProgress(Record record) {
		_record = record;
	}

	/**
	 * Creates the progress of a migration that has not yet started.
	 *
	 * @param plan
	 *        See {@link #getPlan()}.
	 */
	public static MigrationProgress create(Collection<Version> plan) {
		Record record = TypedConfiguration.newConfigItem(Record.class);
		List<String> versions = new ArrayList<>();
		for (Version version : plan) {
			versions.add(version.getModule() + MODULE_SEPARATOR + version.getName());
		}
		record.setPlan(versions);
		return new MigrationProgress(record);
	}

	/**
	 * Reads the progress of an interrupted migration.
	 *
	 * @return The stored progress, <code>null</code> if no migration was interrupted.
	 */
	public static MigrationProgress read(PooledConnection connection) throws SQLException, ConfigurationException {
		String value = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, PROGRESS_PROPERTY);
		if (value == null) {
			return null;
		}
		Record record = TypedConfiguration.parse(ROOT_TAG, Record.class,
			CharacterContents.newContent(value, PROGRESS_PROPERTY));
		return new MigrationProgress(record);
	}

	/**
	 * The versions of the migrations being applied in their order.
	 *
	 * @throws IllegalArgumentException
	 *         If an entry of the stored plan is no version.
	 */
	public List<Version> getPlan() {
		List<Version> result = new ArrayList<>();
		for (String entry : _record.getPlan()) {
			int separator = entry.indexOf(MODULE_SEPARATOR);
			if (separator <= 0 || separator == entry.length() - 1) {
				throw new IllegalArgumentException(
					"Invalid version '" + entry + "' in the database property '" + PROGRESS_PROPERTY + "'.");
			}
			result.add(MigrationUtil.newVersion(entry.substring(0, separator), entry.substring(separator + 1)));
		}
		return result;
	}

	/**
	 * The number of steps completed.
	 */
	public int getCompleted() {
		return _record.getCompleted();
	}

	/**
	 * The number of the step that has been started but not completed, <code>0</code> if no step is
	 * running.
	 */
	public int getStarted() {
		return _record.getStarted();
	}

	/**
	 * Executes the steps among the first <code>end</code> of the given steps that are not yet
	 * completed.
	 *
	 * <p>
	 * The pending {@link RevisionXref} changes of the progress are restored to the given context
	 * before. Each step is committed together with the progress. After the last of all steps, the
	 * progress record is deleted in the commit of the step. The steps from <code>end</code> on are
	 * executed later by {@link #executeRemaining(Protocol, List)}.
	 * </p>
	 *
	 * <p>
	 * A step that replaces the database (such as the replay) {@link MigrationStep.Completion#record(PooledConnection)
	 * records} its completion itself as soon as the database is re-created, since re-creating the
	 * database drops the progress.
	 * </p>
	 *
	 * @param log
	 *        The log of the migration. A step reporting an error is rolled back and terminates the
	 *        migration.
	 * @param connection
	 *        The connection the steps write to.
	 * @param context
	 *        The context of the migration.
	 * @param steps
	 *        All steps of the migration, including the completed ones.
	 * @param end
	 *        The number of steps to execute now. The remaining steps write in a
	 *        {@link MigrationStep#inKnowledgeBaseTransaction() knowledge base transaction} of their
	 *        own.
	 */
	public void execute(Protocol log, PooledConnection connection, MigrationContext context,
			List<? extends MigrationStep> steps, int end) {
		int completed = getCompleted();
		if (completed > steps.size()) {
			throw log.fatal("The migration progress in the database property '" + PROGRESS_PROPERTY
				+ "' names " + completed + " completed steps, the migration of this application has only "
				+ steps.size() + " steps. The interrupted migration was started by another application version.");
		}

		int started = getStarted();
		if (started != 0) {
			if (started != completed + 1) {
				throw log.fatal("Inconsistent migration progress in the database property '" + PROGRESS_PROPERTY
					+ "': step " + started + " started after " + completed + " completed steps.");
			}
			String interrupted = describe(started, steps);
			if (!context.getSQLDialect().supportsTransactionalDDL()) {
				throw log.fatal(interrupted + " was interrupted. The database does not roll back schema"
					+ " changes of an interrupted transaction, its state is unknown. Restore the database"
					+ " from a backup.");
			}
			log.info(interrupted + " was interrupted and is repeated.", Protocol.WARN);
		}
		if (completed > 0) {
			log.info("Resuming the interrupted migration after " + describe(completed, steps) + ".");
		}

		context.restoreXRef(_record.getXRefInvalidated(), _record.getXRefDeleted());

		for (int index = completed; index < end; index++) {
			executeStep(log, connection, context, steps, index);
		}
	}

	/**
	 * Executes the steps not yet completed after
	 * {@link #execute(Protocol, PooledConnection, MigrationContext, List, int)}.
	 *
	 * <p>
	 * Each of these steps writes in a {@link MigrationStep#inKnowledgeBaseTransaction() knowledge
	 * base transaction} of its own, in which it records its completion. The last step deletes the
	 * progress record.
	 * </p>
	 *
	 * @param log
	 *        The log of the migration. A failing step is rolled back and terminates the migration,
	 *        the progress names it as pending.
	 * @param steps
	 *        All steps of the migration, as given to
	 *        {@link #execute(Protocol, PooledConnection, MigrationContext, List, int)}.
	 */
	public void executeRemaining(Protocol log, List<? extends MigrationStep> steps) {
		for (int index = getCompleted(); index < steps.size(); index++) {
			if (!steps.get(index).inKnowledgeBaseTransaction()) {
				throw new IllegalArgumentException(
					describe(index + 1, steps) + " does not write in a knowledge base transaction.");
			}
			executeStep(log, null, null, steps, index);
		}
	}

	/**
	 * Executes the step with the given index.
	 *
	 * @param connection
	 *        The connection of the migration, <code>null</code> for a step writing in a
	 *        {@link MigrationStep#inKnowledgeBaseTransaction() knowledge base transaction}.
	 * @param context
	 *        The context whose pending {@link RevisionXref} changes are recorded with the progress,
	 *        <code>null</code> to keep the recorded ones.
	 */
	private void executeStep(Protocol log, PooledConnection connection, MigrationContext context,
			List<? extends MigrationStep> steps, int index) {
		int number = index + 1;
		MigrationStep step = steps.get(index);
		String description = describe(number, steps);
		boolean inKnowledgeBase = step.inKnowledgeBaseTransaction();

		if (!inKnowledgeBase) {
			_record.setStarted(number);
			commit(log, connection, store(log, connection), description);
		}

		boolean[] recorded = { false };
		MigrationStep.Completion completion = stepConnection -> {
			recordCompleted(context, number, steps.size(), stepConnection);
			recorded[0] = true;
		};
		try {
			step.run(completion);
		} catch (Exception ex) {
			if (!inKnowledgeBase) {
				rollback(connection);
			}
			throw log.fatal(description + " failed.", ex);
		}
		if (log.hasErrors()) {
			if (!inKnowledgeBase) {
				rollback(connection);
			}
			throw log.fatal(description + " failed.");
		}

		if (inKnowledgeBase) {
			if (!recorded[0]) {
				throw log.fatal(description + " did not record its completion.");
			}
		} else {
			boolean written;
			try {
				completion.record(connection);
				written = true;
			} catch (SQLException ex) {
				log.error("Unable to record the progress of the migration.", ex);
				written = false;
			}
			commit(log, connection, written, description);
		}
	}

	/**
	 * Writes the progress with the given step completed to the given connection, deletes it after
	 * the last step.
	 *
	 * @param context
	 *        The context whose pending {@link RevisionXref} changes are recorded,
	 *        <code>null</code> to keep the recorded ones.
	 */
	private void recordCompleted(MigrationContext context, int number, int stepCount, PooledConnection connection)
			throws SQLException {
		_record.setStarted(0);
		_record.setCompleted(number);
		if (context != null) {
			_record.setXRefInvalidated(new ArrayList<>(context.getXRefInvalidated()));
			_record.setXRefDeleted(new ArrayList<>(context.getXRefDeleted()));
		}
		if (number == stepCount) {
			DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, PROGRESS_PROPERTY, null);
		} else {
			DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, PROGRESS_PROPERTY,
				TypedConfiguration.toString(ROOT_TAG, _record));
		}
	}

	private static String describe(int number, List<? extends MigrationStep> steps) {
		return "Step " + number + " of the migration (" + steps.get(number - 1).getName() + ")";
	}

	private boolean store(Protocol log, PooledConnection connection) {
		try {
			DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, PROGRESS_PROPERTY,
				TypedConfiguration.toString(ROOT_TAG, _record));
			return true;
		} catch (SQLException ex) {
			log.error("Unable to store the migration progress.", ex);
			return false;
		}
	}

	private static void commit(Protocol log, PooledConnection connection, boolean progressWritten, String step) {
		if (!progressWritten) {
			rollback(connection);
			throw log.fatal("Unable to record the progress of " + step + ".");
		}
		try {
			connection.commit();
		} catch (SQLException ex) {
			rollback(connection);
			throw log.fatal("Unable to commit " + step + ".", ex);
		}
	}

	private static void rollback(PooledConnection connection) {
		try {
			connection.rollback();
		} catch (SQLException ex) {
			// Ignore, the original problem is reported.
		}
	}

}
