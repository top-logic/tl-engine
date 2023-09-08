/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.SyserrProtocol;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.util.SchemaExtraction;
import com.top_logic.basic.db.model.util.TableCopy;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleRuntimeException;
import com.top_logic.basic.module.ModuleSystem;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.basic.sql.SQLLoader;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ComputationEx;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.meta.MetaAttributeFactory;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.DefaultEventWriter;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.convert.BranchEventConverter;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.KnowledgeEventConverter;
import com.top_logic.knowledge.event.convert.StackedEventWriter;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.knowledge.service.CreateTablesContext;
import com.top_logic.knowledge.service.DBSetupActions;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WrapperResolver;
import com.top_logic.migrate.tl.sequence.CreateOracleSequences;
import com.top_logic.migrate.tl.sequence.SequenceDefinition;
import com.top_logic.migrate.tl.skip.ItemEventSkip;
import com.top_logic.migrate.tl.skip.TypeSkip;

/**
 * The class {@link MigrateUtils} contains utility methods
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MigrateUtils {

	public static MigrateUtils INSTANCE = new MigrateUtils();

	/**
	 * Copies the given tables from the source pool to the target pool
	 * 
	 * @param sourcePool
	 *        the pool to copy from
	 * @param targetPool
	 *        the pool to copy to. the database must not contain tables with the given names
	 * @param copyTables
	 *        the tables to copy
	 */
	public void copyTables(ConnectionPool sourcePool, ConnectionPool targetPool, List<String> copyTables,
			Protocol protocol) throws SQLException, IOException {
		if (copyTables.isEmpty()) {
			protocol.info("No tables to copy");
			return;

		}
		PooledConnection readConnection = sourcePool.borrowReadConnection();
		try {
			SchemaExtraction extraction =
				new SchemaExtraction(readConnection.getMetaData(), sourcePool.getSQLDialect());

			PooledConnection writeConnection = targetPool.borrowWriteConnection();
			try {
				TableCopy copy =
					new TableCopy(sourcePool.getSQLDialect(), readConnection, targetPool.getSQLDialect(),
						writeConnection, 1024);

				protocol.info("Copying tables '" + copyTables + "'.");

				DBSchema copySchema = DBSchemaFactory.createDBSchema();
				for (String tableName : copyTables) {
					protocol.info("Analyzing table '" + tableName + "'.", Protocol.DEBUG);
					extraction.addTable(copySchema, tableName);
					protocol.info("Finished analyzing table '" + tableName + "'.", Protocol.DEBUG);
				}

				String sql = copySchema.toSQL(targetPool.getSQLDialect());

				protocol.info("Creating target tables.", Protocol.DEBUG);
				new SQLLoader(writeConnection).executeSQL(sql);
				protocol.info("Finished creating target tables.", Protocol.DEBUG);

				for (String tableName : copyTables) {
					protocol.info("Copying table contents '" + tableName + "'.", Protocol.DEBUG);
					try {
						copy.copyTable(copySchema.getTable(tableName));
						protocol.info("Finished copying table contents '" + tableName + "'.", Protocol.DEBUG);
					} catch (Exception ex) {
						throw protocol.fatal("Unable to copy data for table '" + tableName + "'", ex);
					}
				}

				writeConnection.commit();
			} finally {
				targetPool.releaseWriteConnection(writeConnection);
			}
		} finally {
			sourcePool.releaseReadConnection(readConnection);
		}
	}

	/**
	 * Sets up the database to migrate to
	 * 
	 * @param protocol
	 *        some protocol
	 * @param targetKBConfig
	 *        the properties to of the target {@link KnowledgeBase}
	 * @param srcPoolName
	 *        the name of the {@link ConnectionPool} to read data from
	 * @param tablesForCopy
	 *        additional tables to copy from source connectionPool to the target DB. In most cases
	 *        the tables which are not known by the target KB
	 * 
	 * @throws Exception
	 *         if some error occurs
	 */
	public void setupTargetDatabase(final Protocol protocol, final KnowledgeBaseConfiguration targetKBConfig,
			final String srcPoolName, final String... tablesForCopy) throws Exception {
		final ComputationEx<Void, Exception> computation = new ComputationEx<>() {
			@Override
			public Void run() throws Exception {
				{
					ConnectionPool srcPool;
					if (srcPoolName == null) {
						srcPool = ConnectionPoolRegistry.getDefaultConnectionPool();
					} else {
						srcPool = ConnectionPoolRegistry.getConnectionPool(srcPoolName);
					}
					ConnectionPool destPool = getDestinationPool(targetKBConfig);

					List<String> copyTables = new ArrayList<>();
					for (String tbl : tablesForCopy) {
						copyTables.add(tbl.toUpperCase());
					}

					copyTables(srcPool, destPool, copyTables, protocol);

					CreateTablesContext context = new CreateTablesContext(new SyserrProtocol(), targetKBConfig);
					DBSetupActions.newInstance().createTables(context);

					// Sequence table is constructed in DBMain
					copySequences(srcPool, destPool, protocol);
					return null;
				}
			}
		};

		withConnectionPoolRegistry(MigrateUtils.class, computation);
	}

	void copySequences(ConnectionPool srcPool, ConnectionPool destPool, Protocol protocol) {
		String tableName = SQLH.mangleDBName("sequence");
		String idColumn = SQLH.mangleDBName("id");
		PooledConnection readConnection = srcPool.borrowReadConnection();
		try {
			PooledConnection writeConnection = destPool.borrowWriteConnection();
			try {
				try {
					Statement stmt = readConnection.createStatement();
					try {
						StringBuilder select = new StringBuilder();
						select.append("select * from ");
						select.append(tableName);
						select.append(" where not ");
						select.append(idColumn);
						select.append("='rev' and not ");
						select.append(idColumn);
						select.append("='branch'");
						/* no ; in statement as ORACLE does not allow */
//						select.append(';');
						ResultSet result = stmt.executeQuery(select.toString());
						try {
							StringBuilder sql = new StringBuilder("insert into ");
							sql.append(tableName);
							sql.append(" values (?,?)");
							PreparedStatement writeStmt = writeConnection.prepareStatement(sql.toString());
							try {
								boolean hasValues = false;
								while (result.next()) {
									hasValues = true;
									writeStmt.setString(1, result.getString(1));
									writeStmt.setLong(2, result.getLong(2));
									writeStmt.executeUpdate();
								}
								if (hasValues) {
									writeConnection.commit();
								}
							} finally {
								writeStmt.close();
							}
						} finally {
							result.close();
						}
					} finally {
						stmt.close();
					}
				} catch (SQLException ex) {
					throw protocol.fatal("Unable to copy additional entries of the sequence table", ex);
				}

			} finally {
				destPool.releaseWriteConnection(writeConnection);
			}
		} finally {
			srcPool.releaseReadConnection(readConnection);
		}
	}

	/**
	 * Executes the given {@link ComputationEx2} in a context in which
	 * {@link ConnectionPoolRegistry} is accessible.
	 * 
	 * @param <T>
	 *        the return type of the given computation
	 * @param caller
	 *        the caller of this method
	 * @param computation
	 *        the {@link ComputationEx2} to execute
	 * 
	 * @return the return value of the given {@link ComputationEx2}
	 */
	public <T, E1 extends Throwable, E2 extends Throwable> T withConnectionPoolRegistry(final Class<?> caller,
			final ComputationEx2<T, E1, E2> computation) throws E1, E2 {
		final ComputationEx2<T, E1, E2> startConnectionPool = new ComputationEx2<>() {

			@Override
			public T run() throws ModuleRuntimeException, E1, E2 {
				return ModuleUtil.INSTANCE.inModuleContext(ConnectionPoolRegistry.Module.INSTANCE,
					computation);
			}
		};
		return ThreadContext.inSystemContext(caller, startConnectionPool);
	}

	/**
	 * Does the actual migrations.
	 * <ol>
	 * <li>
	 * It rewrites events such that the {@link MetaObject} come from the target
	 * {@link KnowledgeBase}.</li>
	 * <li>
	 * It removes from branch events all types which does not exists in the target KB</li>
	 * <li>
	 * It migrates the documents</li>
	 * </ol>
	 * The events are delivered such that the {@link Document} events come first, then the
	 * {@link DocumentVersion}, and then the rest.
	 * 
	 * @see MigrateParameters
	 */
	public void migrate(final MigrateParameters migrateParam) throws Exception {

		final BasicRuntimeModule<?>[] necessaryModules =
			new BasicRuntimeModule<?>[migrateParam.neededModules.length + 1];
		necessaryModules[0] = PersistencyLayer.Module.INSTANCE;
		System.arraycopy(migrateParam.neededModules, 0, necessaryModules, 1, migrateParam.neededModules.length);

		ThreadContext.inSystemContext(MigrateUtils.class,
			new Computation<Void>() {
				@Override
				public Void run() {
					return ModuleUtil.INSTANCE.inModuleContext(new Computation<Void>() {
						@Override
						public Void run() {
							doMigrateWithKB(migrateParam);
							return null;
						}
					}, necessaryModules);
				}
			});
	}

	protected void doMigrateWithKB(final MigrateParameters migrateParam) {

		final KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getKnowledgeBase(migrateParam.targetKBName);
		final KnowledgeBase srcKB = KnowledgeBaseFactory.getInstance().getKnowledgeBase(migrateParam.srcKBName);

		migrateParam.protocol.info("KnowledgeBase source: " + srcKB.getName());
		migrateParam.protocol.info("KnowledgeBase destination: " + kb.getName());
		{
			Transaction targetKBTA = kb.beginTransaction();
			Transaction sourceKBTA = srcKB.beginTransaction();
			try {
				restartMetaAttributeFactory();
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (ModuleException e) {
				throw new RuntimeException(e);
			}

			// Drop created root elements.
			targetKBTA.rollback();
			sourceKBTA.rollback();
		}

		migrateParam.protocol.info("Creating event reader for KB " + srcKB.getName());

		try (ChangeSetReader reader = createKnowledgeEventReader(srcKB)) {

			List<EventRewriter> rewriters;
			final EventWriter replayWriter;
			{
				replayWriter = kb.getReplayWriter();
				rewriters =
					new ArrayList<>(migrateParam.rewriters.size() + 3);

				addNecessaryRewriters(kb, migrateParam.typeNameConversion, migrateParam.branchedTypes, rewriters);

				Rollback rollback = new Rollback() {

					@Override
					public void rollbackCurrentChanges() {
						((DefaultEventWriter) replayWriter).rollbackCurrentChanges();
					}

				};
				for (LazyEventRewriter lazyRewriter : migrateParam.rewriters) {
					final EventRewriter rewriter = lazyRewriter.createRewriter(srcKB, kb);
					if (rewriter instanceof NeedRollback) {
						((NeedRollback) rewriter).setRollback(rollback);
					}
					rewriters.add(rewriter);
				}
			}
			try (EventWriter stackedEvtWriter = StackedEventWriter.createWriter(0, replayWriter, rewriters)) {
				migrateParam.protocol.info("Start Replaying events");

				for (Object visitor : rewriters) {
					if (visitor instanceof EnhancedKnowledgeEventVisitor<?, ?>) {
						((EnhancedKnowledgeEventVisitor) visitor).handlePreVisit(replayWriter);
					}
				}
				ChangeSet changeSet;
				while ((changeSet = reader.read()) != null) {
					migrateParam.protocol.info("Processing: " + changeSet, Protocol.VERBOSE);
					stackedEvtWriter.write(changeSet);
				}
				for (Object visitor : rewriters) {
					if (visitor instanceof EnhancedKnowledgeEventVisitor<?, ?>) {
						((EnhancedKnowledgeEventVisitor) visitor).handlePostVisit(replayWriter);
					}
				}
			} catch (RuntimeException ex) {
				migrateParam.protocol.error("Failure during replay of events", ex);
				throw ex;
			}
		}

	}

	/**
	 * Calls {@link #addNecessaryRewriters(KnowledgeBase, Mapping, Collection, List)} with he
	 * identity mapping.
	 * 
	 * @see #addNecessaryRewriters(KnowledgeBase, Mapping, Collection, List)
	 */
	public static void addNecessaryRewriters(final KnowledgeBase destKB,
			Collection<String> additionalBranchedTypes, List<EventRewriter> rewriters) {
		addNecessaryRewriters(destKB, Mappings.<String> identity(), additionalBranchedTypes, rewriters);
	}

	/**
	 * Adds rewrites needed by the basic migration tool which must first be applied before
	 * additional {@link EventRewriter}s are applied.
	 * 
	 * E.g. the Rewriter which translates the used {@link MetaObject}s from the source
	 * {@link KnowledgeBase} into the target {@link KnowledgeBase}.
	 * 
	 * @param destKB
	 *        the {@link KnowledgeBase} to which the data are migrated
	 * @param typeNameMapping
	 *        Mapping of the type names in the source {@link KnowledgeBase} to the corresponding
	 *        types in the target {@link KnowledgeBase}.
	 * @param additionalBranchedTypes
	 *        The types which are branched at each branch event, additional to the given one. May be
	 *        <code>null</code> to indicate no type is branched.
	 * @param rewriters
	 *        the list to add additional {@link EventRewriter} to.
	 */
	public static void addNecessaryRewriters(final KnowledgeBase destKB, Mapping<String, String> typeNameMapping,
			Collection<String> additionalBranchedTypes, List<EventRewriter> rewriters) {
		final MORepository destRepository = destKB.getMORepository();

		KnowledgeEventConverter knowledgeEventConverter = new KnowledgeEventConverter(destRepository, typeNameMapping);
		rewriters.add(knowledgeEventConverter);

		EventRewriter branchEventConverter =
			BranchEventConverter.createBranchEventConverter(new HashSet<>(destRepository.getMetaObjectNames()),
				additionalBranchedTypes);
		rewriters.add(branchEventConverter);
	}

	protected ChangeSetReader createKnowledgeEventReader(final KnowledgeBase srcKB) {
		ChangeSetReader eventReader;
		{
			Revision startRev = srcKB.getHistoryManager().getRevision(1L);
			Revision stopRev = Revision.CURRENT;
			final ReaderConfig readerConfig =
				ReaderConfigBuilder.createComplexConfig(startRev, stopRev, null, null, true,
					true, null);
			eventReader = srcKB.getChangeSetReader(readerConfig);
		}
		return eventReader;
	}

	protected ConnectionPool getDestinationPool(KnowledgeBaseConfiguration config) {
		String destPoolName = config.getConnectionPool();
		return ConnectionPoolRegistry.getConnectionPool(destPoolName);
	}

	/**
	 * Executes some SQL which are present in the given SQL-file
	 * 
	 * @param caller
	 *        the caller of this method
	 * @param file
	 *        the File containing the SQL to execute. Must be encoded in UTF-8
	 * @param poolName
	 *        the name of the pool to execute the SQL
	 * @throws Exception
	 *         iff execution failed. Either {@link SQLException} or {@link IOException}
	 */
	public void executeSQL(final Class<?> caller, final File file, final String poolName) throws Exception {
		final ComputationEx2<Void, IOException, SQLException> computation =
			new ComputationEx2<>() {

			@Override
				public Void run() throws IOException, SQLException {
					{
					final ConnectionPool connectionPool = ConnectionPoolRegistry.getConnectionPool(poolName);
					final FileInputStream stream = new FileInputStream(file);
					BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "ISO-8859-1"));
					try {
						final PooledConnection con = connectionPool.borrowWriteConnection();
						try {

							String line;
							final Statement stmt = con.createStatement();
							try {
								while ((line = reader.readLine()) != null) {
									// empty lines can not be executed
									if (line.length() > 0) {
										stmt.execute(line);
									}
								}
								con.commit();
							} finally {
								stmt.close();
							}
						} finally {
							connectionPool.releaseWriteConnection(con);
						}
					} finally {
						reader.close();
					}

					return null;
				}
			}
		};
		withConnectionPoolRegistry(caller, computation);
	}

	/**
	 * Creates a {@link BinaryData} based on a file which is accessible via <code>new File(ref)</code>
	 * where <code>ref</code> is the reference to the file.
	 * 
	 * @param config
	 *        expected that it has a property 'file' referencing a file and a property 'contentType'
	 *        denoting the content type of the referenced file.
	 * 
	 * @throws ConfigurationException
	 *         if either property 'file' or property 'contentType' is missing
	 */
	public BinaryData resolveDataItem(Properties config) throws ConfigurationException {
		final String fileRef = config.getProperty("file");
		if (fileRef == null) {
			throw new ConfigurationException("Configuraton must have a property 'file' referencing a file.");
		}
		File file = new File(fileRef);
		if (!file.exists()) {
			throw new ConfigurationException("File " + file.getAbsolutePath() + " does not exists.");
		}
		if (file.isDirectory()) {
			throw new ConfigurationException("File " + file.getAbsolutePath() + " denotes a directory.");
		}
		String contentType = config.getProperty("contentType");
		if (contentType == null) {
			throw new ConfigurationException(
				"Configuraton must have a property 'contentType' denoting the content type of the referenced file.");
		}

		final String fileName = config.getProperty("fileName", file.getName());
		final BinaryData binaryData = BinaryDataFactory.createBinaryData(file);
		return new DefaultDataItem(fileName, binaryData, contentType);
	}

	/**
	 * Starts all modules configured in the module system which have an implementation class
	 * <code>clazz</code> such that the given <code>implClass</code> is assignable from it, i.e. the
	 * modules such that <code>implClass.isAssignableFrom(clazz)</code> returns <code>true</code>.
	 * 
	 * @param implClass
	 *        the super class of the implementation class of the modules to start. must not be
	 *        <code>null</code>
	 * @param protocol
	 *        the protocol filled with errors if some module could not be started. must not be
	 *        <code>null</code>
	 */
	public static Set<? extends BasicRuntimeModule<?>> startModulesWithGivenImplementationClass(
			Class<? extends ManagedClass> implClass, Protocol protocol) {
		final Collection<? extends Class<? extends BasicRuntimeModule<?>>> moduleDependencies =
			ModuleSystem.Module.INSTANCE.getDependencies();
		Set<BasicRuntimeModule<?>> startedModules = new HashSet<>();
		for (Class<? extends BasicRuntimeModule<?>> clazz : moduleDependencies) {
			BasicRuntimeModule<?> module;
			try {
				module = ConfigUtil.getSingleton(clazz);
			} catch (ConfigurationException ex) {
				protocol.error("Cannot resolve module: " + clazz.getName(), ex);
				continue;
			}
			final Class<?> implementation = module.getImplementation();
			if (implClass.isAssignableFrom(implementation)) {
				Collection<? extends BasicRuntimeModule<?>> activeModules =
					new HashSet<BasicRuntimeModule<?>>(ModuleUtil.INSTANCE.getActiveModules());
				try {
					ModuleUtil.INSTANCE.startUp(module);
				} catch (IllegalArgumentException ex) {
					protocol.error("Unable to start module: " + module, ex);
				} catch (ModuleException ex) {
					protocol.error("Unable to start module: " + module, ex);
				}
				Collection<? extends BasicRuntimeModule<?>> activeModulesAfterStarting =
					new HashSet<BasicRuntimeModule<?>>(ModuleUtil.INSTANCE.getActiveModules());
				activeModulesAfterStarting.removeAll(activeModules);

				startedModules.addAll(activeModulesAfterStarting);
			}

		}
		return startedModules;
	}

	/**
	 * Starts all enabled {@link WrapperResolver}.
	 */
	public static Set<? extends BasicRuntimeModule<?>> startWrapperResolver(Protocol protocol) {
		HashSet<BasicRuntimeModule<?>> result = new HashSet<>();
		result.addAll(startModulesWithGivenImplementationClass(DynamicModelService.class, protocol));
		return result;
	}

	/**
	 * Sets up the sequences for the given definitions. The corresponding tables must exists and be
	 * filled in the target database.
	 */
	public void setupSequenceTables(final Protocol protocol, final KnowledgeBaseConfiguration targetKBConfig,
			final SequenceDefinition... tableDefinitions) throws Exception {
		if (tableDefinitions.length == 0) {
			return;
		}
		final ComputationEx<Void, SQLException> computation = new ComputationEx<>() {
			@Override
			public Void run() throws SQLException {
				{
					ConnectionPool destPool = getDestinationPool(targetKBConfig);
					DBHelper sqlDialect = destPool.getSQLDialect();
					if (!sqlDialect.isSerialNeeded()) {
						protocol.info("No Serial needed by connectionPool " + destPool);
						return null;
					}
					PooledConnection readCon = destPool.borrowReadConnection();
					try {
						PooledConnection writeCon = destPool.borrowWriteConnection();
						try {
							for (SequenceDefinition table : tableDefinitions) {
								new CreateOracleSequences(table, protocol).create(sqlDialect, readCon, writeCon);
							}
							writeCon.commit();
						} finally {
							destPool.releaseWriteConnection(writeCon);
						}
					} finally {
						destPool.releaseReadConnection(readCon);
					}
					return null;
				}
			}

		};

		withConnectionPoolRegistry(MigrateUtils.class, computation);
	}

	/**
	 * Skips each event of each of the given types.
	 * 
	 * @since 5.7.3
	 */
	public static LazyEventRewriter skipTypes(Protocol log, String... objectNames) {
		return new EventRewriterAdaptor(ItemEventSkip.newItemEventSkip(TypeSkip.skip(objectNames), log));
	}

	/**
	 * Starts or restart {@link MetaAttributeFactory}, depending if it is currently active or not.
	 * 
	 * @since 5.7.3
	 */
	public static void restartMetaAttributeFactory() throws RestartException, ModuleException {
		restart(MetaAttributeFactory.Module.INSTANCE);
	}

	/**
	 * Starts or restart the given module, depending if it is currently active or not.
	 * 
	 * @since 5.7.3
	 */
	public static void restart(BasicRuntimeModule<?> module) throws RestartException, ModuleException {
		if (module.isActive()) {
			ModuleUtil.INSTANCE.restart(module, null);
		} else {
			ModuleUtil.INSTANCE.startUp(module);
		}
	}

	/**
	 * Returns the configuration of the {@link KnowledgeBaseFactory} without starting it.
	 * 
	 * @since 5.7.3
	 */
	public static KnowledgeBaseFactoryConfig getKnowledgeBaseFactoryConfig() throws ConfigurationException {
		ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
		ServiceConfiguration<KnowledgeBaseFactory> serviceConfig =
			applicationConfig.getServiceConfiguration(KnowledgeBaseFactory.class);
		return (KnowledgeBaseFactoryConfig) serviceConfig;
	}
}
