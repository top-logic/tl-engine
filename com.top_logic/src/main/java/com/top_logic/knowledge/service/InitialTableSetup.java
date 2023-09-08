/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import com.top_logic.basic.Environment;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.migration.MigrationService;

/**
 * Service that installs the database tables.
 * 
 * <p>
 * The trigger for this service to become active is the fact that the
 * {@link RowLevelLockingSequenceManager#checkTable(PooledConnection) sequence table does not
 * exist}.
 * </p>
 * 
 * <p>
 * If a setup is required (during the first boot of the application), the configured
 * {@link DBSetupAction}s are performed as configured in {@link DBSetupActions.Config#getActions()}.
 * One of these actions is normally the {@link InitialKBTableSetup} that sets up the table for the
 * {@link KnowledgeBase}.
 * </p>
 * 
 * <p>
 * If this is not the first boot of the application, the {@link MigrationService} is responsible for
 * upgrading the persistency layer to the currently booting application version.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceExtensionPoint(ConnectionPoolRegistry.Module.class)
public class InitialTableSetup extends ManagedClass {

	/**
	 * Configuration of the {@link InitialTableSetup}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<InitialTableSetup> {

		// No special config here
	}

	/**
	 * System property that can be set to disable automatic database setup also when no sequence
	 * table exists.
	 */
	public static String NO_DATABASE_TABLE_SETUP = "tl_noAutomaticalDatabaseSetup";

	/**
	 * Creates a new {@link InitialTableSetup} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link InitialTableSetup}.
	 */
	public InitialTableSetup(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		try {
			setupDatabase();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void setupDatabase() throws Exception {
		if (Environment.getSystemPropertyOrEnvironmentVariable(NO_DATABASE_TABLE_SETUP, false)) {
			Logger.info(
				"No automatic database setup, because system property '" + NO_DATABASE_TABLE_SETUP + "' is set.",
				InitialTableSetup.class);
			return;
		}

		LogProtocol log = new LogProtocol(InitialTableSetup.class);
		CreateTablesContext context = new CreateTablesContext(log);
		log.info("Checking for automatic table setup.");
		if (sequenceTableExists(context.getConnectionPool())) {
			context.info("Tables already set up.");
			return;
		}

		log.info("Setting up database tables...");
		StopWatch timer = StopWatch.createStartedWatch();
		DBSetupActions.newInstance().createTables(context);
		timer.stop();
		context.checkErrors();

		log.info("Database tables created in " + StopWatch.toStringMillis(timer.getElapsedMillis()) + ".");
	}

	private boolean sequenceTableExists(ConnectionPool pool) throws SQLException {
		PooledConnection connection = pool.borrowReadConnection();
		try {
			return RowLevelLockingSequenceManager.checkTable(connection);
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Module for {@link InitialTableSetup}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Module extends TypedRuntimeModule<InitialTableSetup> {

		/**
		 * Singleton {@link InitialTableSetup.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			Collection<? extends Class<? extends BasicRuntimeModule<?>>> dependencies = super.getDependencies();
			DBSetupActions setupAction = DBSetupActions.newInstance();
			Collection<? extends BasicRuntimeModule<?>> actionDependencies = setupAction.getDependencies();
			if (actionDependencies.isEmpty()) {
				return dependencies;
			}
			Collection<Class<? extends BasicRuntimeModule<?>>> enhancedDependencies = new HashSet<>(dependencies);
			for (BasicRuntimeModule<?> dependency : actionDependencies) {
				@SuppressWarnings("unchecked")
				Class<? extends BasicRuntimeModule<?>> moduleClass =
					(Class<? extends BasicRuntimeModule<?>>) dependency.getClass();
				enhancedDependencies.add(moduleClass);
			}
			return enhancedDependencies;
		}

		@Override
		public Class<InitialTableSetup> getImplementation() {
			return InitialTableSetup.class;
		}

	}

}

