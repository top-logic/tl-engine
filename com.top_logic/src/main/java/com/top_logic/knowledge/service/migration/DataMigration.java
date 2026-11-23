/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLLoader;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.StackedEventWriter;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBTypeRepository;
import com.top_logic.knowledge.service.db2.id.factory.IdFactory;
import com.top_logic.knowledge.service.db2.id.factory.TransientIdFactory;
import com.top_logic.knowledge.service.db2.migration.DumpReader;
import com.top_logic.knowledge.service.db2.migration.FuzzyTableNameMapping;
import com.top_logic.knowledge.service.db2.migration.GuiceContext;
import com.top_logic.knowledge.service.db2.migration.Migration;
import com.top_logic.knowledge.service.db2.migration.MigrationImpl;
import com.top_logic.knowledge.service.db2.migration.MigrationRepository;
import com.top_logic.knowledge.service.db2.migration.TypeMapping;
import com.top_logic.knowledge.service.db2.migration.UnversionedCreations;
import com.top_logic.knowledge.service.db2.migration.config.ReplayConfig;
import com.top_logic.knowledge.service.db2.migration.config.ReplayConfig.Step;
import com.top_logic.knowledge.service.db2.migration.config.ReplayConfig.TableTransformerStep;
import com.top_logic.knowledge.service.db2.migration.db.RequiresRowTransformation;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;
import com.top_logic.knowledge.service.db2.migration.db.TableContent;
import com.top_logic.knowledge.service.db2.migration.db.transformers.StackedRowTransformer;
import com.top_logic.knowledge.service.db2.migration.sql.InsertWriter;
import com.top_logic.knowledge.service.db2.migration.sql.SQLDumper;

/**
 * Algorithm applying a data migration.
 * 
 * <p>
 * The process must be executed in the following steps:
 * </p>
 * 
 * <ol>
 * <li>{@link #build(Log)}</li>
 * <li>{@link #createSQLSteps(MigrationContext, Log, PooledConnection)}</li>
 * <li>{@link #createInsertSQL(Log, KnowledgeBaseConfiguration, ConnectionPool, long, InputStream, InsertWriter)}</li>
 * <li>{@link #dropAndRecreateDatabase(PooledConnection, KnowledgeBaseConfiguration, Reader)}</li>
 * </ol>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DataMigration {

	private Map<String, Version> _maximalVersionByModule;

	private List<Step> _steps;

	private List<TableTransformerStep> _transformerSteps;

	private boolean _withReplayMigration;

	private MigrationInfo _migrationInfo;

	private int _bufferChunkSize = 100000;

	/**
	 * Creates a {@link com.top_logic.knowledge.service.migration.DataMigration}.
	 * 
	 * @param migrationInfo
	 *        The migration descriptor. See
	 *        {@link MigrationUtil#buildMigration(Log, VersionDescriptor)}.
	 */
	public DataMigration(MigrationInfo migrationInfo) {
		_migrationInfo = migrationInfo;

		_maximalVersionByModule = new HashMap<>();
		_steps = new ArrayList<>();
		_transformerSteps = new ArrayList<>();
		_withReplayMigration = false;
	}

	/**
	 * Number of objects buffered before creating an insert statement.
	 */
	public int getBufferChunkSize() {
		return _bufferChunkSize;
	}

	/**
	 * @see #getBufferChunkSize()
	 */
	public void setBufferChunkSize(int bufferChunkSize) {
		_bufferChunkSize = bufferChunkSize;
	}

	/**
	 * Initial step calculating the required information for applying the migration.
	 */
	public void build(Log log) {
		for (MigrationConfig migration : _migrationInfo.getMigrations()) {
			Version version = migration.getVersion();
	
			// Automatic migrations have no version.
			if (version != null) {
				_maximalVersionByModule.put(version.getModule(), version);
			}
	
			ReplayConfig replayMigration = migration.getMigration();
			if (replayMigration == null) {
				continue;
			}
			Collection<Step> localSteps = replayMigration.getSteps().values();
			List<TableTransformerStep> localTransformers = replayMigration.getTransformerSteps();
			if (!replayMigration.getForceReplay() && localSteps.isEmpty() && localTransformers.isEmpty()) {
				continue;
			}
			_withReplayMigration = true;
			_steps.addAll(localSteps);
			_transformerSteps.addAll(localTransformers);
		}
	}

	/**
	 * The source describing the migration.
	 */
	public MigrationInfo getMigrationInfo() {
		return _migrationInfo;
	}

	/**
	 * Whether a replay is required.
	 */
	public boolean isReplayRequired() {
		return _withReplayMigration;
	}

	/**
	 * The version descriptor this migration produces.
	 */
	public Map<String, Version> getNewVersion() {
		return _maximalVersionByModule;
	}

	/**
	 * The SQL preprocessors to apply.
	 */
	public List<PolymorphicConfiguration<? extends MigrationProcessor>> getProcessors() {
		List<PolymorphicConfiguration<? extends MigrationProcessor>> processors = new ArrayList<>();
		for (MigrationConfig migration : _migrationInfo.getMigrations()) {
			processors.addAll(migration.getPreProcessors());
		}
		for (MigrationConfig migration : _migrationInfo.getMigrations()) {
			processors.addAll(migration.getProcessors());
		}
		return processors;
	}

	/**
	 * The changeset transformations to apply during replay.
	 */
	public List<Step> getSteps() {
		return _steps;
	}

	/**
	 * The table data transformation to apply during replay.
	 */
	public List<TableTransformerStep> getTransformerSteps() {
		return _transformerSteps;
	}

	/**
	 * The steps of the SQL migration: One step for each pre-processor of all migrations, followed
	 * by one step for each processor of all migrations.
	 * 
	 * <p>
	 * The steps must be executed before
	 * {@link #createInsertSQL(Log, KnowledgeBaseConfiguration, ConnectionPool, long, InputStream, InsertWriter)}.
	 * </p>
	 * 
	 * @see MigrationProgress#execute(com.top_logic.basic.Protocol, PooledConnection, MigrationContext,
	 *      List)
	 */
	public List<MigrationStep> createSQLSteps(MigrationContext context, Log log, PooledConnection connection) {
		List<MigrationStep> steps = new ArrayList<>();
		for (MigrationConfig migration : _migrationInfo.getMigrations()) {
			addSteps(steps, context, log, connection, migration, "pre-processor", migration.getPreProcessors());
		}
		for (MigrationConfig migration : _migrationInfo.getMigrations()) {
			addSteps(steps, context, log, connection, migration, "processor", migration.getProcessors());
		}
		return steps;
	}

	private void addSteps(List<MigrationStep> steps, MigrationContext context, Log log, PooledConnection connection,
			MigrationConfig migration, String kind,
			List<? extends PolymorphicConfiguration<? extends MigrationProcessor>> processorConfigs) {
		String versionName = MigrationUtil.toString(migration.getVersion());
		int number = 1;
		for (PolymorphicConfiguration<? extends MigrationProcessor> processorConfig : processorConfigs) {
			String name = kind + " " + number + " (" + processorConfig.getImplementationClass().getSimpleName()
				+ ") of migration " + versionName;
			boolean first = number == 1;
			steps.add(MigrationStep.create(name, completion -> {
				if (first) {
					logHeader(log, "Performing " + kind + "s of migration " + versionName);
				}
				MigrationProcessor processor =
					SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(processorConfig);
				processor.doMigration(context, log, connection);
			}));
			number++;
		}
	}

	private void logHeader(Log log, String msg) {
		String border = "=".repeat(msg.length() + 6);

		log.info(border);
		log.info("== " + msg + " ==");
		log.info(border);
	}

	/**
	 * Executes the given {@link MigrationProcessor}s in their order.
	 * 
	 * <p>
	 * A failing processor is reported to the given {@link Log}, the following processors are
	 * executed nevertheless.
	 * </p>
	 * 
	 * @param context
	 *        The context of the running migration.
	 * @param log
	 *        The {@link Log} to report to.
	 * @param connection
	 *        The connection to the database to migrate.
	 * @param processorConfigs
	 *        Configurations of the processors to execute.
	 */
	public static void processProcessors(MigrationContext context, Log log, PooledConnection connection,
			List<? extends PolymorphicConfiguration<? extends MigrationProcessor>> processorConfigs) {
		List<? extends MigrationProcessor> processors = processorConfigs.stream()
			.map(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY::getInstance)
			.collect(Collectors.toList());

		for (MigrationProcessor processor : processors) {
			try {
				processor.doMigration(context, log, connection);
			} catch (Exception ex) {
				log.error("Unable to process migration processor " + processor, ex);
			}
		}
	}

	/**
	 * Creates insert SQL that builds the transformed database.
	 * 
	 * <p>
	 * Only required, if {@link #isReplayRequired()}.
	 * </p>
	 * 
	 * @param in
	 *        The XML dump data extracted from the source version.
	 * @param out
	 *        The output for SQL inserts.
	 */
	public void createInsertSQL(Log log, KnowledgeBaseConfiguration kbConfig, ConnectionPool pool, long nextId,
			InputStream in, InsertWriter out)
			throws Exception {
		MORepository typeRepository;
		DBHelper sqlDialect = pool.getSQLDialect();
		try {
			typeRepository = new MigrationRepository(sqlDialect, kbConfig);
		} catch (ConfigurationException | DataObjectException ex) {
			log.error("Unable to get MORepository.", ex);
			return;
		}

		/* Note: Fetch before injecting of members to ensure the TypeMapping is initialised
		 * correct. */
		GuiceContext guice = new GuiceContext(log, new DefaultInstantiationContext(DataMigration.class));
		TypeMapping typeMapper =
			guice.getInstance(TypedConfiguration.newConfigItem(FuzzyTableNameMapping.Config.class));

		MigrationImpl migration = new MigrationImpl(log);
		guice.bind(MORepository.class, typeRepository);
		guice.bind(Migration.class, migration);
		guice.bind(Log.class, migration);
		guice.bind(KnowledgeBaseConfiguration.class, kbConfig);
		guice.bind(IdFactory.class, new TransientIdFactory(nextId));

		List<EventRewriter> rewriters = new ArrayList<>();
		for (Step step : getSteps()) {
			rewriters.addAll(TypedConfiguration.getInstanceList(guice, step.getRewriters()));
		}

		// Creating rewriter for table types.
		List<RowTransformer> transformers = new ArrayList<>();
		for (EventRewriter rewriter : rewriters) {
			if (rewriter instanceof RequiresRowTransformation) {
				transformers.add(((RequiresRowTransformation) rewriter).getRequiredTransformations());
			}
		}
		for (TableTransformerStep step : getTransformerSteps()) {
			transformers.addAll(TypedConfiguration.getInstanceList(guice, step.getTransformers()));
		}

		guice.inject();

		try (SQLDumper sqlDumper = new SQLDumper(log, pool, typeRepository, getBufferChunkSize(), out)) {
			// Creating rewriter for versioned types.
			EventWriter versionedWriter = StackedEventWriter.createWriter(0, sqlDumper, rewriters);

			// Creating rewriter for unversioned types.
			EventWriter unversionedOut = sqlDumper.getUnversionedWriter();
			EventWriter unversionedWriter = StackedEventWriter.createWriter(0, unversionedOut, rewriters);
	
			RowWriter tableOut = sqlDumper.getRowWriter();
			RowWriter tableWriter = StackedRowTransformer.createRowWriter(transformers, tableOut);
	
			try (DumpReader reader = new DumpReader(typeMapper, in)) {
				log.info("Processing change sets.");
				writeChangeSets(log, migration, reader, versionedWriter);

				log.info("Processing unversioned types.");
				writeUnversiondTypes(log, reader, unversionedWriter);

				log.info("Processing non KB tables.");
				writeRows(log, reader, tableWriter);
			}
		}
	}

	/**
	 * Write raw table data from the {@link DumpReader} to the given {@link RowWriter}.
	 */
	private void writeRows(Log log, DumpReader reader, RowWriter tableWriter) {
		Iterator<TableContent> tables = reader.getTables();
		while (tables.hasNext()) {
			TableContent table = tables.next();

			log.info("Processing table '" + table.getTable().getName() + "'.");

			Iterator<RowValue> rows = table.getRows();
			while (rows.hasNext()) {
				tableWriter.write(rows.next());
			}
		}
	}

	/**
	 * Write unversioned object data from the {@link DumpReader} to the given {@link EventWriter}.
	 */
	private void writeUnversiondTypes(Log log, DumpReader reader, EventWriter writer) {
		long eventRev = Revision.CURRENT_REV;
		Iterator<UnversionedCreations> unversioned = reader.getUnversionedObjects(eventRev);
		ChangeSet cs = new ChangeSet(eventRev);
		cs.setCommit(new CommitEvent(eventRev, "migration", System.currentTimeMillis(),
			I18NConstants.SYNTHESIZED_COMMIT_DURING_REPLAY));

		while (unversioned.hasNext()) {
			UnversionedCreations unversionedType = unversioned.next();

			log.info("Processing unversioned type '" + unversionedType.getType().getName() + "'.");

			Iterator<ObjectCreation> items = unversionedType.getItems();
			while (items.hasNext()) {
				cs.addCreation(items.next());
			}
		}
		writer.write(cs);
	}

	/**
	 * Write versioned application data from the {@link DumpReader} to the given
	 * {@link EventWriter}.
	 */
	private void writeChangeSets(Log log, MigrationImpl migration, DumpReader reader, EventWriter writer) {
		long last = System.nanoTime();

		ChangeSet cs = null;
		Iterator<ChangeSet> changes = reader.getChangeSets();
		while (changes.hasNext()) {
			cs = changes.next();
			migration.setChangeSet(cs);
			writer.write(cs);

			long now = System.nanoTime();
			if (now - last > 5000000000L) {
				log.info("Processed change set " + cs.getRevision() + ".");
				last = now;
			}
		}

		if (cs == null) {
			log.info("No change sets found.");
		} else {
			log.info("Change sets processed, last revision was " + cs.getRevision() + ".");
		}
	}

	/**
	 * Loads the data transformed data.
	 *
	 * <p>
	 * Only required, if {@link #isReplayRequired()}.
	 * </p>
	 * 
	 * @param in
	 *        The SQL script produced by
	 *        {@link #createInsertSQL(Log, KnowledgeBaseConfiguration, ConnectionPool, long, InputStream, InsertWriter)}.
	 */
	public static void dropAndRecreateDatabase(PooledConnection connection, KnowledgeBaseConfiguration kbConfig, Reader in)
			throws SQLException, ConfigurationException, DataObjectException, IOException {
		dropAndRecreateDatabase(connection, kbConfig, in, Collections.emptySet());
	}

	/**
	 * Loads the transformed data, keeping the given tables.
	 *
	 * <p>
	 * All tables of the schema are dropped and created again before the script is executed, except
	 * the given ones, which keep their content. The re-created tables and each statement of the
	 * script are committed.
	 * </p>
	 *
	 * @param in
	 *        The SQL script produced by
	 *        {@link #createInsertSQL(Log, KnowledgeBaseConfiguration, ConnectionPool, long, InputStream, InsertWriter)}.
	 *        It must not insert rows into the kept tables.
	 * @param keepTables
	 *        The names of the tables that are neither dropped nor created again.
	 */
	public static void dropAndRecreateDatabase(PooledConnection connection, KnowledgeBaseConfiguration kbConfig,
			Reader in, Set<String> keepTables)
			throws SQLException, ConfigurationException, DataObjectException, IOException {
		SchemaSetup schemaSetup = KBUtils.getSchemaConfigResolved(kbConfig);
		DBHelper dbHelper = connection.getSQLDialect();
		DBTypeRepository newRepository = DBTypeRepository.newRepository(dbHelper, schemaSetup,
			!kbConfig.getDisableVersioning());
		DBSchema schema = schemaSetup.buildSchema(newRepository);
		schema.getTables().removeIf(table -> keepTables.contains(table.getName()));
		DBSchemaUtils.recreateTables(connection, schema);

		// The script is loaded with a commit after each statement, the empty schema is committed
		// before.
		connection.commit();

		new SQLLoader(connection)
			.setAutoCommit(true)
			.setContinueOnError(true)
			.executeSQL(in);
	}

}