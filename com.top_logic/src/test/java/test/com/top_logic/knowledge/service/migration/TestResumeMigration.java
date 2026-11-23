/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;

import org.junit.Assume;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.TestingConnectionPoolRegistryAccess;
import test.com.top_logic.basic.TestingConnectionPoolRegistryAccess.PoolRef;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.InitialTableSetup;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.db2.RevisionXref;
import com.top_logic.knowledge.service.db2.migration.config.ReplayConfig;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationLock;
import com.top_logic.knowledge.service.migration.MigrationPostProcessor;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.MigrationProgress;
import com.top_logic.knowledge.service.migration.MigrationService;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.StartupAction;
import com.top_logic.knowledge.service.migration.Version;

/**
 * Test of the {@link MigrationService} resuming an interrupted migration from its
 * {@link MigrationProgress}.
 *
 * <p>
 * The test starts a {@link MigrationService} on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME} with two test migrations in two modules. The
 * migrations consist of {@link CountingProcessor}s that write data only (no DDL), so that a
 * rollback undoes them on every database. An abort is simulated by a processor that throws an
 * exception at a configured call.
 * </p>
 *
 * <p>
 * Startup actions ({@link CountingStartupAction}) run in a knowledge base on the same database,
 * see {@link StartableMigrationService#getStartupKnowledgeBase()}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestResumeMigration extends AbstractDBKnowledgeBaseTest {

	private static final String BASE_MODULE = "test-resume-base";

	private static final String APP_MODULE = "test-resume-app";

	private static final String BASE_MIGRATION = "Ticket_1_resume_base";

	private static final String APP_MIGRATION = "Ticket_2_resume_app";

	private static final String UNKNOWN_MIGRATION = "Ticket_3_unknown";

	/** Prefix of the database properties written by the {@link CountingProcessor}s. */
	private static final String RESULT_PREFIX = "test.resume.";

	/** Revision of the {@link RevisionXref} entry that the rebuild removes. */
	private static final long OUTDATED_XREF_REVISION = 987654321L;

	/** Branch of the {@link RevisionXref} entry that the rebuild removes. */
	private static final long OUTDATED_XREF_BRANCH = 1L;

	/** The processors and post-processors of the test migrations in the order of their execution. */
	private static final List<String> ALL_CALLS = List.of("pre1", "p1", "p2", "p3", "p4", "q1", "q2");

	/** Number of the steps before the post-processors: five processors, xref rebuild, versions. */
	private static final int STEPS_BEFORE_POST_PROCESSORS = 7;

	/** Number of the steps before the startup actions: the steps before and the post-processors. */
	private static final int STEPS_BEFORE_STARTUP_ACTIONS = STEPS_BEFORE_POST_PROCESSORS + 1;

	/** The startup actions of the test migrations in the order of their execution. */
	private static final List<String> STARTUP_ACTIONS = List.of("s1", "s2");

	/** Type of the object created during the simulated start of the application. */
	private static final String OBJECT_TYPE = "Person";

	/** Number of the replay step: after five processors and the xref rebuild. */
	private static final int REPLAY_STEP = 7;

	/** The post-processors of the test migrations in the order of their execution. */
	private static final List<String> POST_PROCESSORS = List.of("q1", "q2");

	/** The calls of a migration resumed after the interrupted processor "p2". */
	private static final List<String> RESUMED_AT_P2 = List.of("p2", "p3", "p4", "q1", "q2");

	/** IDs of the {@link CountingProcessor}s in the order of their calls. */
	static final List<String> CALLS = new ArrayList<>();

	/** IDs of the {@link CountingStartupAction}s in the order of their calls. */
	static final List<String> STARTUP_CALLS = new ArrayList<>();

	/**
	 * Number of committed objects of the {@link #OBJECT_TYPE} each {@link CountingStartupAction}
	 * has found, in the order of the calls.
	 */
	static final List<Integer> COMMITTED_OBJECTS = new ArrayList<>();

	/** ID of the {@link CountingStartupAction} that throws an exception, <code>null</code> for none. */
	static String _failStartupAction;

	/** Whether the test migrations have the {@link #STARTUP_ACTIONS}. */
	private boolean _withStartupActions;

	/** The knowledge base the {@link CountingStartupAction}s run in. */
	static KnowledgeBase _startupKB;

	/** Number of change sets rewritten by the {@link CountingRewriter}. */
	static final AtomicInteger REWRITES = new AtomicInteger();

	/** Whether the {@link CountingRewriter} throws an exception. */
	static boolean _failReplay;

	/** Call of a {@link CountingProcessor} or {@link CountingPostProcessor} that throws an exception, 0 for none. */
	static int _failAtCall;

	/** The table whose {@link RevisionXref} entries the processor "pre1" invalidates. */
	static String _xrefTable;

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private DBProperties _properties;

	private SetupKBHelper _setup;

	private Map<String, Map<Version, MigrationConfig>> _scripts;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		CALLS.clear();
		STARTUP_CALLS.clear();
		COMMITTED_OBJECTS.clear();
		_failStartupAction = null;
		_startupKB = null;
		_failAtCall = 0;
		_xrefTable = null;
		REWRITES.set(0);
		_failReplay = false;

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_properties = new DBProperties(_pool);
		_setup = new SetupKBHelper(_kbConfig);
		_setup.setup();

		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection);
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME,
				context.getApplicationSchema());
			MigrationUtil.updateStoredVersions(log, connection, MigrationUtil.maximalVersions(log));
			MigrationUtil.updateStoredVersions(log, connection,
				List.of(MigrationUtil.initialVersion(BASE_MODULE), MigrationUtil.initialVersion(APP_MODULE)));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertEquals("Errors: " + log.getErrors(), List.of(), log.getErrors());

		_scripts = new HashMap<>();
		Version baseInitial = addMigration(MigrationUtil.initialVersion(BASE_MODULE));
		Version appInitial = addMigration(MigrationUtil.initialVersion(APP_MODULE));

		MigrationConfig base = TypedConfiguration.newConfigItem(MigrationConfig.class);
		base.getPreProcessors().add(processor("pre1"));
		base.getProcessors().add(processor("p1"));
		base.getProcessors().add(processor("p2"));
		base.getPostProcessors().add(postProcessor("q1"));
		Version baseVersion =
			addMigration(base, MigrationUtil.newVersion(BASE_MODULE, BASE_MIGRATION), baseInitial);

		MigrationConfig app = TypedConfiguration.newConfigItem(MigrationConfig.class);
		app.getProcessors().add(processor("p3"));
		app.getProcessors().add(processor("p4"));
		app.getPostProcessors().add(postProcessor("q2"));
		addMigration(app, MigrationUtil.newVersion(APP_MODULE, APP_MIGRATION), appInitial, baseVersion);
	}

	@Override
	protected void tearDown() throws Exception {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			for (String key : DBProperties.getPropertiesForNode(connection, DBProperties.GLOBAL_PROPERTY).keySet()) {
				if (key.startsWith(RESULT_PREFIX) || key.equals(MigrationProgress.PROGRESS_PROPERTY)
					|| key.equals(MigrationLock.LOCK_PROPERTY)
					|| key.equals(MigrationUtil.COMMON_KEY_PREFIX + BASE_MODULE)
					|| key.equals(MigrationUtil.COMMON_KEY_PREFIX + APP_MODULE)) {
					DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, key, null);
				}
			}
			if (_xrefTable != null) {
				deleteOutdatedXref(connection);
			}
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		_setup.tearDown();
		_setup = null;
		_properties = null;
		_pool = null;
		_kbConfig = null;
		_scripts = null;
		CALLS.clear();
		STARTUP_CALLS.clear();
		COMMITTED_OBJECTS.clear();
		_startupKB = null;
		super.tearDown();
	}

	/**
	 * A migration that is not interrupted executes all steps once and leaves no progress.
	 */
	public void testUninterrupted() throws Exception {
		startMigration(null);

		assertEquals(ALL_CALLS, CALLS);
		assertCompleted();
	}

	/**
	 * (a) An abort in the third processor call: The next start executes the processors from the
	 * third one, the result equals the one of an uninterrupted migration.
	 */
	public void testResumeAfterAbort() throws Exception {
		_failAtCall = 3;
		assertStartFails(null, "p2", "Simulated abort");

		assertEquals(List.of("pre1", "p1", "p2"), CALLS);
		assertEquals(Map.of(RESULT_PREFIX + "pre1", "1", RESULT_PREFIX + "p1", "1"), results());
		MigrationProgress progress = progress();
		assertNotNull("No progress recorded.", progress);
		assertEquals(2, progress.getCompleted());
		assertEquals(3, progress.getStarted());
		assertEquals(List.of(MigrationUtil.newVersion(BASE_MODULE, BASE_MIGRATION),
			MigrationUtil.newVersion(APP_MODULE, APP_MIGRATION)), progress.getPlan());
		assertEquals("Versions stored before completion.", MigrationUtil.initialVersion(BASE_MODULE).getName(),
			storedVersions().get(BASE_MODULE).getName());

		_failAtCall = 0;
		CALLS.clear();
		startMigration(transactionalDDL());

		assertEquals(RESUMED_AT_P2, CALLS);
		assertCompleted();
	}

	/**
	 * (b) A progress naming a migration unknown to the application terminates the start.
	 */
	public void testUnknownVersion() throws Exception {
		storeProgress(List.of(MigrationUtil.newVersion(BASE_MODULE, BASE_MIGRATION),
			MigrationUtil.newVersion(APP_MODULE, UNKNOWN_MIGRATION)), 1, 0);

		assertStartFails(transactionalDDL(), UNKNOWN_MIGRATION);

		assertEquals(List.of(), CALLS);
		assertNotNull("Progress removed.", progress());
	}

	/**
	 * (c) An interrupted step on a database without transactional DDL terminates the start with a
	 * message naming the step.
	 */
	public void testInterruptedStepWithoutTransactionalDDL() throws Exception {
		storeInterruptedProgress();

		assertStartFails(nonTransactionalDDL(), "Step 3 of the migration", "processor 2 (CountingProcessor)",
			BASE_MIGRATION, "backup");

		assertEquals(List.of(), CALLS);
		assertEquals(3, progress().getStarted());
	}

	/**
	 * (c) An interrupted step on a database with transactional DDL is repeated.
	 */
	public void testInterruptedStepWithTransactionalDDL() throws Exception {
		storeInterruptedProgress();

		startMigration(transactionalDDL());

		assertEquals(RESUMED_AT_P2, CALLS);
		assertNull(progress());
	}

	/**
	 * (c) An interrupted step with the dialect of the test database: It is repeated, if the
	 * database has transactional DDL, otherwise the start fails (H2).
	 */
	public void testInterruptedStepWithDatabaseDialect() throws Exception {
		storeInterruptedProgress();

		if (_pool.getSQLDialect().supportsTransactionalDDL()) {
			startMigration(null);
			assertEquals(RESUMED_AT_P2, CALLS);
			assertNull(progress());
		} else {
			assertStartFails(null, "Step 3 of the migration", "backup");
			assertEquals(List.of(), CALLS);
			assertEquals(3, progress().getStarted());
		}
	}

	/**
	 * (d) The {@link RevisionXref} entries of a table invalidated before an abort are rebuilt
	 * after resuming the migration.
	 */
	public void testXrefInvalidatedBeforeAbort() throws Exception {
		_xrefTable = versionedTable();
		_failAtCall = 3;
		assertStartFails(null, "Simulated abort");

		assertEquals(List.of(_xrefTable), xrefInvalidated());
		assertEquals("Outdated xref entry not written.", 1, outdatedXrefCount());

		_failAtCall = 0;
		CALLS.clear();
		startMigration(transactionalDDL());

		assertEquals(RESUMED_AT_P2, CALLS);
		assertEquals("Xref entries of the invalidated table not rebuilt.", 0, outdatedXrefCount());
		assertNull(progress());
	}

	/**
	 * (e) An abort in a post-processor: The next start executes the post-processors again, but not
	 * the processors.
	 */
	public void testResumeAfterAbortInPostProcessor() throws Exception {
		_failAtCall = ALL_CALLS.size();
		assertStartFails(null, "post-processors", "Simulated abort in post-processor q2");

		assertEquals(ALL_CALLS, CALLS);
		MigrationProgress progress = progress();
		assertNotNull("No progress recorded.", progress);
		assertEquals("All steps before the post-processors completed.", STEPS_BEFORE_POST_PROCESSORS,
			progress.getCompleted());
		assertEquals(0, progress.getStarted());
		assertNull("Post-processor results not rolled back.", results().get(RESULT_PREFIX + "q1"));

		_failAtCall = 0;
		CALLS.clear();
		startMigration(null);

		assertEquals(POST_PROCESSORS, CALLS);
		assertCompleted();
	}

	/**
	 * (f) After the commit of the post-processors, no progress is left.
	 */
	public void testNoProgressAfterPostProcessors() throws Exception {
		startMigration(null);

		assertEquals(ALL_CALLS, CALLS);
		assertNull(progress());
		assertEquals("1", results().get(RESULT_PREFIX + "q2"));
	}

	/**
	 * (g) A migration with replay that is aborted before the database is dropped: The next start
	 * repeats the replay, the result equals the one of an uninterrupted migration.
	 */
	public void testResumeReplay() throws Exception {
		Version baseVersion = MigrationUtil.newVersion(BASE_MODULE, BASE_MIGRATION);
		ReplayConfig replay = TypedConfiguration.newConfigItem(ReplayConfig.class);
		ReplayConfig.Step step = TypedConfiguration.newConfigItem(ReplayConfig.Step.class);
		step.setName("test-rewrite");
		step.getRewriters().add(TypedConfiguration.newConfigItem(CountingRewriter.Config.class));
		replay.getSteps().put(step.getName(), step);
		TypedConfigUtil.setProperty(_scripts.get(BASE_MODULE).get(baseVersion), MigrationConfig.MIGRATION_PROPERTY,
			replay);

		int revisions = revisionCount();
		assertTrue("No history to replay.", revisions > 0);

		_failReplay = true;
		assertStartFails(null, "replay", "Simulated abort in replay");

		assertEquals(1, REWRITES.get());
		assertEquals(List.of("pre1", "p1", "p2", "p3", "p4"), CALLS);
		MigrationProgress progress = progress();
		assertEquals("Replay step not marked as started.", REPLAY_STEP, progress.getStarted());

		_failReplay = false;
		REWRITES.set(0);
		CALLS.clear();
		startMigration(transactionalDDL());

		assertTrue("Replay not repeated.", REWRITES.get() > 1);
		// The history is kept, the transaction of the post-processors adds a revision.
		assertEquals("History changed by the replay.", revisions + 1, revisionCount());
		assertEquals(POST_PROCESSORS, CALLS);
		assertCompleted();
	}

	/**
	 * (h) The second startup action fails: The change of the first one is committed, the progress
	 * names the second one as pending, the next start executes only the second one and leaves no
	 * progress.
	 */
	public void testResumeAfterAbortInStartupAction() throws Exception {
		addStartupActions();
		_failStartupAction = "s2";
		assertStartFails(null, "Step " + (STEPS_BEFORE_STARTUP_ACTIONS + 2) + " of the migration",
			"startup action 1 (CountingStartupAction)", APP_MIGRATION, "Simulated abort in startup action s2");

		assertEquals(ALL_CALLS, CALLS);
		assertEquals(STARTUP_ACTIONS, STARTUP_CALLS);
		assertEquals("1", results().get(RESULT_PREFIX + "s1"));
		assertNull("Change of the failing startup action not rolled back.", results().get(RESULT_PREFIX + "s2"));
		MigrationProgress progress = progress();
		assertNotNull("No progress recorded.", progress);
		assertEquals("The second startup action is pending.", STEPS_BEFORE_STARTUP_ACTIONS + 1,
			progress.getCompleted());
		assertEquals(0, progress.getStarted());

		_failStartupAction = null;
		CALLS.clear();
		STARTUP_CALLS.clear();
		startMigration(null);

		assertEquals(List.of(), CALLS);
		assertEquals(List.of("s2"), STARTUP_CALLS);
		assertCompleted();
		assertEquals("1", results().get(RESULT_PREFIX + "s2"));
	}

	/**
	 * (i) A start that migrates holds the lock until {@link MigrationService#applicationStarted()}
	 * has executed the startup actions.
	 */
	public void testLockHeldUntilApplicationStarted() throws Exception {
		addStartupActions();
		StartableMigrationService service = startService(null);

		assertNotNull("Lock released before the startup actions.", lockHolder());
		assertEquals(List.of(), STARTUP_CALLS);
		assertEquals("Startup actions pending.", STEPS_BEFORE_STARTUP_ACTIONS, progress().getCompleted());

		service.applicationStarted();

		assertNull("Lock not released after the startup actions.", lockHolder());
		assertEquals(STARTUP_ACTIONS, STARTUP_CALLS);
		assertCompleted();
	}

	/**
	 * (j) Shutting down a started service without {@link MigrationService#applicationStarted()}
	 * (a start failing after the migration) releases the lock and keeps the progress, the next
	 * start executes the startup actions.
	 */
	public void testShutDownWithoutApplicationStarted() throws Exception {
		addStartupActions();
		StartableMigrationService service = startService(null);
		assertNotNull(lockHolder());

		service.stop();

		assertNull("Lock not released on shut down.", lockHolder());
		assertEquals(List.of(), STARTUP_CALLS);
		MigrationProgress progress = progress();
		assertNotNull("Progress removed.", progress);
		assertEquals(STEPS_BEFORE_STARTUP_ACTIONS, progress.getCompleted());

		CALLS.clear();
		startMigration(null);

		assertEquals(List.of(), CALLS);
		assertEquals(STARTUP_ACTIONS, STARTUP_CALLS);
		assertCompleted();
	}

	/**
	 * (k) A start without pending changes takes no lock and executes nothing in
	 * {@link MigrationService#applicationStarted()}.
	 */
	public void testStartWithoutPendingChanges() throws Exception {
		addStartupActions();
		startMigration(null);
		assertCompleted();

		CALLS.clear();
		STARTUP_CALLS.clear();
		StartableMigrationService service = startService(null);
		assertNull("Lock taken by a start without changes.", lockHolder());

		service.applicationStarted();

		assertEquals(List.of(), CALLS);
		assertEquals(List.of(), STARTUP_CALLS);
		assertNull(lockHolder());
		assertCompleted();
	}

	/**
	 * (l) A startup action sees an object created during the start of the application, through a
	 * query of committed objects.
	 */
	public void testStartupActionSeesObjectsOfStart() throws Exception {
		addStartupActions();
		StartableMigrationService service = startService(null);

		// The start of the application: An object is created in the startup transaction, which is
		// committed before the startup actions are executed.
		KnowledgeBase kb = service.getStartupKnowledgeBase();
		int before = committedObjects();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			KnowledgeObject object = kb.createKnowledgeObject(OBJECT_TYPE);
			object.setAttributeValue("name", "test-resume-start");
			tx.commit();
		}

		service.applicationStarted();

		assertEquals(STARTUP_ACTIONS, STARTUP_CALLS);
		assertEquals("Object of the start not visible to the startup actions.", List.of(before + 1, before + 1),
			COMMITTED_OBJECTS);
		assertCompleted();
	}

	/**
	 * Adds the startup action "s1" to the base migration and "s2" to the app migration.
	 */
	private void addStartupActions() {
		_withStartupActions = true;
		_scripts.get(BASE_MODULE).get(MigrationUtil.newVersion(BASE_MODULE, BASE_MIGRATION)).getStartupActions()
			.add(startupAction("s1"));
		_scripts.get(APP_MODULE).get(MigrationUtil.newVersion(APP_MODULE, APP_MIGRATION)).getStartupActions()
			.add(startupAction("s2"));
	}

	private String lockHolder() throws SQLException {
		return _properties.getProperty(MigrationLock.LOCK_PROPERTY);
	}

	/**
	 * Number of the committed objects of the {@link #OBJECT_TYPE} in the knowledge base of the
	 * {@link CountingStartupAction}s, read through a connection of its own.
	 */
	static int committedObjects() throws SQLException {
		MOStructure type = (MOStructure) _startupKB.getMORepository().getTypeOrNull(OBJECT_TYPE);
		ConnectionPool pool = KBUtils.getConnectionPool(_startupKB);
		PooledConnection connection = pool.borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COUNT(*) FROM " + sql.tableRef(type.getDBMapping().getDBName()))) {
				try (ResultSet result = statement.executeQuery()) {
					assertTrue(result.next());
					return result.getInt(1);
				}
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	private int revisionCount() throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COUNT(*) FROM " + sql.tableRef(SQLH.mangleDBName(BasicTypes.REVISION_TYPE_NAME)))) {
				try (ResultSet result = statement.executeQuery()) {
					assertTrue(result.next());
					return result.getInt(1);
				}
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private void storeInterruptedProgress() throws Exception {
		// Results of the completed steps.
		_properties.setProperty(RESULT_PREFIX + "pre1", "1");
		_properties.setProperty(RESULT_PREFIX + "p1", "1");
		storeProgress(List.of(MigrationUtil.newVersion(BASE_MODULE, BASE_MIGRATION),
			MigrationUtil.newVersion(APP_MODULE, APP_MIGRATION)), 2, 3);
	}

	/**
	 * Stores a progress with the given state of a migration of the given plan.
	 */
	private void storeProgress(List<Version> plan, int completed, int started) throws Exception {
		MigrationProgress.Record record = TypedConfiguration.newConfigItem(MigrationProgress.Record.class);
		List<String> versions = new ArrayList<>();
		for (Version version : plan) {
			versions.add(version.getModule() + "/" + version.getName());
		}
		record.setPlan(versions);
		record.setCompleted(completed);
		record.setStarted(started);
		_properties.setProperty(MigrationProgress.PROGRESS_PROPERTY,
			TypedConfiguration.toString(MigrationProgress.ROOT_TAG, record));
	}

	private void assertCompleted() throws Exception {
		Map<String, String> expected = new TreeMap<>();
		for (String id : ALL_CALLS) {
			expected.put(RESULT_PREFIX + id, "1");
		}
		if (_withStartupActions) {
			for (String id : STARTUP_ACTIONS) {
				expected.put(RESULT_PREFIX + id, "1");
			}
		}
		assertEquals("Each processor and startup action must be applied exactly once.", expected, results());
		assertNull("Progress not removed.", progress());
		Map<String, Version> stored = storedVersions();
		assertEquals(BASE_MIGRATION, stored.get(BASE_MODULE).getName());
		assertEquals(APP_MIGRATION, stored.get(APP_MODULE).getName());
		assertNull(lockHolder());
	}

	private void assertStartFails(DBHelper dialect, String... messageParts) throws Exception {
		try {
			startMigration(dialect);
			fail("Start expected to fail.");
		} catch (RuntimeException ex) {
			String messages = messages(ex);
			for (String part : messageParts) {
				assertTrue("Message does not contain '" + part + "': " + messages, messages.contains(part));
			}
		}
		assertNull("Lock not released.",
			_properties.getProperty(MigrationLock.LOCK_PROPERTY));
	}

	private static String messages(Throwable ex) {
		StringBuilder result = new StringBuilder();
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			result.append(cause.getMessage()).append('\n');
		}
		return result.toString();
	}

	private Map<String, String> results() throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			Map<String, String> result = new TreeMap<>();
			DBProperties.getPropertiesForNode(connection, DBProperties.GLOBAL_PROPERTY).forEach((key, value) -> {
				if (key.startsWith(RESULT_PREFIX)) {
					result.put(key, value);
				}
			});
			return result;
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private MigrationProgress progress() throws Exception {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return MigrationProgress.read(connection);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private List<String> xrefInvalidated() throws Exception {
		MigrationProgress.Record record = TypedConfiguration.parse(MigrationProgress.ROOT_TAG,
			MigrationProgress.Record.class,
			CharacterContents.newContent(_properties.getProperty(MigrationProgress.PROGRESS_PROPERTY)));
		return record.getXRefInvalidated();
	}

	private Map<String, Version> storedVersions() throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return MigrationUtil.readStoredVersions(connection, List.of(BASE_MODULE, APP_MODULE));
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Name of a non-abstract versioned table of the application schema.
	 */
	private String versionedTable() {
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			MORepository repository = new MigrationContext(log, connection).getSchemaRepository();
			for (String name : new TreeSet<>(repository.getMetaObjectNames())) {
				MetaObject type = repository.getTypeOrNull(name);
				if (type instanceof MOClass && !((MOClass) type).isAbstract() && ((MOClass) type).isVersioned()) {
					return name;
				}
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
		throw new AssertionError("No versioned table.");
	}

	private int outdatedXrefCount() throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COUNT(*) FROM " + sql.tableRef(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME))
					+ " WHERE " + sql.columnRef(SQLH.mangleDBName(RevisionXref.XREF_REV_ATTRIBUTE)) + "=?")) {
				statement.setLong(1, OUTDATED_XREF_REVISION);
				try (ResultSet result = statement.executeQuery()) {
					assertTrue(result.next());
					return result.getInt(1);
				}
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private static void insertOutdatedXref(PooledConnection connection, String table) throws SQLException {
		DBHelper sql = connection.getSQLDialect();
		try (PreparedStatement statement = connection.prepareStatement(
			"INSERT INTO " + sql.tableRef(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME)) + " ("
				+ sql.columnRef(SQLH.mangleDBName(RevisionXref.XREF_REV_ATTRIBUTE)) + ", "
				+ sql.columnRef(SQLH.mangleDBName(RevisionXref.XREF_BRANCH_ATTRIBUTE)) + ", "
				+ sql.columnRef(SQLH.mangleDBName(RevisionXref.XREF_TYPE_ATTRIBUTE)) + ") VALUES (?, ?, ?)")) {
			statement.setLong(1, OUTDATED_XREF_REVISION);
			statement.setLong(2, OUTDATED_XREF_BRANCH);
			statement.setString(3, table);
			statement.executeUpdate();
		}
	}

	private static void deleteOutdatedXref(PooledConnection connection) throws SQLException {
		DBHelper sql = connection.getSQLDialect();
		try (PreparedStatement statement = connection.prepareStatement(
			"DELETE FROM " + sql.tableRef(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME))
				+ " WHERE " + sql.columnRef(SQLH.mangleDBName(RevisionXref.XREF_REV_ATTRIBUTE)) + "=?")) {
			statement.setLong(1, OUTDATED_XREF_REVISION);
			statement.executeUpdate();
		}
	}

	private Version addMigration(Version version, Version... dependencies) {
		return addMigration(TypedConfiguration.newConfigItem(MigrationConfig.class), version, dependencies);
	}

	private Version addMigration(MigrationConfig migration, Version version, Version... dependencies) {
		migration.setVersion(version);
		for (Version dependency : dependencies) {
			migration.getDependencies().put(dependency.getModule(), dependency);
		}
		_scripts.computeIfAbsent(version.getModule(), x -> new HashMap<>()).put(version, migration);
		return version;
	}

	private static PolymorphicConfiguration<? extends MigrationPostProcessor> postProcessor(String id) {
		CountingPostProcessor.Config<?> config = TypedConfiguration.newConfigItem(CountingPostProcessor.Config.class);
		config.setId(id);
		return config;
	}

	private static PolymorphicConfiguration<? extends StartupAction> startupAction(String id) {
		CountingStartupAction.Config<?> config = TypedConfiguration.newConfigItem(CountingStartupAction.Config.class);
		config.setId(id);
		return config;
	}

	private static PolymorphicConfiguration<? extends MigrationProcessor> processor(String id) {
		CountingProcessor.Config config = TypedConfiguration.newConfigItem(CountingProcessor.Config.class);
		config.setId(id);
		return config;
	}

	private DBHelper transactionalDDL() {
		assumeH2();
		return DBHelper.createDefaultInstance(TransactionalDDLDialect.class);
	}

	private DBHelper nonTransactionalDDL() {
		assumeH2();
		return DBHelper.createDefaultInstance(NonTransactionalDDLDialect.class);
	}

	/**
	 * The dialect stubs are H2 dialects, they are only used on an H2 database.
	 */
	private void assumeH2() {
		try {
			Assume.assumeTrue("Dialect stub requires H2.", _pool.getSQLDialect() instanceof H2Helper);
		} catch (SQLException ex) {
			throw new AssertionError(ex);
		}
	}

	/**
	 * Starts a {@link MigrationService} with the test migrations on
	 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME} and completes the start of the application by
	 * {@link MigrationService#applicationStarted()}.
	 *
	 * @param dialect
	 *        The dialect the {@link MigrationContext} reports, <code>null</code> for the dialect
	 *        of the database.
	 */
	private void startMigration(DBHelper dialect) throws ConfigurationException {
		startService(dialect).applicationStarted();
	}

	/**
	 * Starts a {@link MigrationService} with the test migrations on
	 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}, without completing the start of the
	 * application.
	 *
	 * @param dialect
	 *        The dialect the {@link MigrationContext} reports, <code>null</code> for the dialect
	 *        of the database.
	 */
	private StartableMigrationService startService(DBHelper dialect) throws ConfigurationException {
		MigrationService.Config config = TypedConfiguration.copy((MigrationService.Config) ApplicationConfig
			.getInstance().getServiceConfiguration(MigrationService.class));
		// The modules of the application stay migration modules, so that a start without the test
		// migrations finds no module without stored version.
		List<String> modules = new ArrayList<>(config.getModules());
		modules.add(BASE_MODULE);
		modules.add(APP_MODULE);
		TypedConfigUtil.setProperty(config, MigrationService.Config.MODULES_NAME, modules);
		TypedConfigUtil.setProperty(config, MigrationService.Config.MINIMUM_MODULES, List.of());

		String noTableSetup = System.clearProperty(InitialTableSetup.NO_DATABASE_TABLE_SETUP);
		try {
			// The knowledge bases of the replay and the post-processors are created on the default
			// pool.
			PoolRef defaultPool =
				TestingConnectionPoolRegistryAccess.setupDefaultPool(DatabaseTestSetup.MIGRATION_POOL_NAME);
			try {
				StartableMigrationService service = new StartableMigrationService(
					SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config, _scripts, dialect, _kbConfig);
				service.start();
				return service;
			} finally {
				TestingConnectionPoolRegistryAccess.restoreDefaultPool(defaultPool);
			}
		} finally {
			if (noTableSetup != null) {
				System.setProperty(InitialTableSetup.NO_DATABASE_TABLE_SETUP, noTableSetup);
			}
		}
	}

	private static KnowledgeBaseConfiguration migrationKBConfig() throws ConfigurationException {
		KnowledgeBaseFactoryConfig factoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
			.getServiceConfiguration(KnowledgeBaseFactory.class);
		KnowledgeBaseConfiguration result = TypedConfiguration.copy(
			factoryConfig.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME));
		result.update(result.descriptor().getProperty(KnowledgeBaseConfiguration.CONNECTION_POOL_PROPERTY),
			DatabaseTestSetup.MIGRATION_POOL_NAME);
		return result;
	}

	/**
	 * {@link MigrationProcessor} recording its call in {@link TestResumeMigration#CALLS} and
	 * counting its executions in the database property {@link TestResumeMigration#RESULT_PREFIX}
	 * plus its ID.
	 *
	 * <p>
	 * The processor with the ID <code>pre1</code> additionally writes an outdated
	 * {@link RevisionXref} entry for {@link TestResumeMigration#_xrefTable} and invalidates the
	 * entries of that table, if set.
	 * </p>
	 */
	public static class CountingProcessor extends AbstractConfiguredInstance<CountingProcessor.Config<?>>
			implements MigrationProcessor {

		/**
		 * Configuration of a {@link CountingProcessor}.
		 */
		public interface Config<I extends CountingProcessor> extends PolymorphicConfiguration<I> {

			/**
			 * ID of the processor.
			 */
			@Name("id")
			String getId();

			/**
			 * @see #getId()
			 */
			void setId(String value);

		}

		/**
		 * Creates a {@link CountingProcessor}.
		 */
		public CountingProcessor(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
			String id = getConfig().getId();
			CALLS.add(id);
			try {
				String key = RESULT_PREFIX + id;
				String count = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, key);
				DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, key,
					Integer.toString(count == null ? 1 : Integer.parseInt(count) + 1));

				if ("pre1".equals(id) && _xrefTable != null) {
					insertOutdatedXref(connection, _xrefTable);
					context.invalidateXRef((MOStructure) context.getSchemaRepository()
						.getTypeOrNull(_xrefTable));
				}
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
			if (CALLS.size() == _failAtCall) {
				throw new RuntimeException("Simulated abort in processor " + id + ".");
			}
		}

	}

	/**
	 * {@link MigrationPostProcessor} recording its call in {@link TestResumeMigration#CALLS} and
	 * counting its executions in the database property {@link TestResumeMigration#RESULT_PREFIX}
	 * plus its ID, in the transaction of the post-processors.
	 */
	public static class CountingPostProcessor extends AbstractConfiguredInstance<CountingPostProcessor.Config<?>>
			implements MigrationPostProcessor {

		/**
		 * Configuration of a {@link CountingPostProcessor}.
		 */
		public interface Config<I extends CountingPostProcessor> extends PolymorphicConfiguration<I> {

			/**
			 * ID of the post-processor.
			 */
			@Name("id")
			String getId();

			/**
			 * @see #getId()
			 */
			void setId(String value);

		}

		/**
		 * Creates a {@link CountingPostProcessor}.
		 */
		public CountingPostProcessor(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public void afterMigration(Log log, KnowledgeBase kb) {
			String id = getConfig().getId();
			CALLS.add(id);
			PooledConnection connection = KBUtils.getCurrentContext(kb).getConnection();
			try {
				String key = RESULT_PREFIX + id;
				String count = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, key);
				DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, key,
					Integer.toString(count == null ? 1 : Integer.parseInt(count) + 1));
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
			if (CALLS.size() == _failAtCall) {
				throw new RuntimeException("Simulated abort in post-processor " + id + ".");
			}
		}

	}

	/**
	 * {@link StartupAction} recording its call in {@link TestResumeMigration#STARTUP_CALLS} and the
	 * number of committed objects it finds in {@link TestResumeMigration#COMMITTED_OBJECTS}, and
	 * counting its executions in the database property {@link TestResumeMigration#RESULT_PREFIX}
	 * plus its ID, in its transaction.
	 */
	public static class CountingStartupAction extends AbstractConfiguredInstance<CountingStartupAction.Config<?>>
			implements StartupAction {

		/**
		 * Configuration of a {@link CountingStartupAction}.
		 */
		public interface Config<I extends CountingStartupAction> extends PolymorphicConfiguration<I> {

			/**
			 * ID of the action.
			 */
			@Name("id")
			String getId();

			/**
			 * @see #getId()
			 */
			void setId(String value);

		}

		/**
		 * Creates a {@link CountingStartupAction}.
		 */
		public CountingStartupAction(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public void perform() {
			String id = getConfig().getId();
			STARTUP_CALLS.add(id);
			PooledConnection connection = KBUtils.getCurrentContext(_startupKB).getConnection();
			try {
				COMMITTED_OBJECTS.add(committedObjects());
				String key = RESULT_PREFIX + id;
				String count = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, key);
				DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, key,
					Integer.toString(count == null ? 1 : Integer.parseInt(count) + 1));
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
			if (id.equals(_failStartupAction)) {
				throw new RuntimeException("Simulated abort in startup action " + id + ".");
			}
		}

	}

	/**
	 * {@link EventRewriter} counting its calls in {@link TestResumeMigration#REWRITES} and throwing
	 * an exception, while {@link TestResumeMigration#_failReplay} is set.
	 */
	public static class CountingRewriter extends AbstractConfiguredInstance<CountingRewriter.Config<?>>
			implements EventRewriter {

		/**
		 * Configuration of a {@link CountingRewriter}.
		 */
		public interface Config<I extends CountingRewriter> extends PolymorphicConfiguration<I> {
			// No properties.
		}

		/**
		 * Creates a {@link CountingRewriter}.
		 */
		public CountingRewriter(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public void rewrite(ChangeSet cs, EventWriter out) {
			REWRITES.incrementAndGet();
			if (_failReplay) {
				throw new RuntimeException("Simulated abort in replay.");
			}
			out.write(cs);
		}

	}

	/**
	 * H2 dialect reporting transactional DDL.
	 */
	public static class TransactionalDDLDialect extends H2Helper {

		/**
		 * Creates a {@link TransactionalDDLDialect}.
		 */
		public TransactionalDDLDialect(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public boolean supportsTransactionalDDL() {
			return true;
		}

	}

	/**
	 * H2 dialect reporting DDL that is not transactional, as {@link H2Helper} itself.
	 */
	public static class NonTransactionalDDLDialect extends H2Helper {

		/**
		 * Creates a {@link NonTransactionalDDLDialect}.
		 */
		public NonTransactionalDDLDialect(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public boolean supportsTransactionalDDL() {
			return false;
		}

	}

	/**
	 * {@link MigrationService} that can be started by the test, with the test migrations and a
	 * given dialect for the {@link MigrationContext}.
	 */
	private static class StartableMigrationService extends MigrationService {

		private final Map<String, Map<Version, MigrationConfig>> _testScripts;

		private final DBHelper _dialect;

		private final KnowledgeBaseConfiguration _kbConfig;

		private KnowledgeBase _kb;

		StartableMigrationService(InstantiationContext context, Config config,
				Map<String, Map<Version, MigrationConfig>> scripts, DBHelper dialect,
				KnowledgeBaseConfiguration kbConfig) {
			super(context, config);
			_testScripts = scripts;
			_dialect = dialect;
			_kbConfig = kbConfig;
		}

		void start() {
			startUp();
		}

		void stop() {
			shutDown();
		}

		/**
		 * A knowledge base on {@link DatabaseTestSetup#MIGRATION_POOL_NAME}, created on first
		 * access.
		 */
		@Override
		protected KnowledgeBase getStartupKnowledgeBase() {
			if (_kb == null) {
				BufferingProtocol log = new BufferingProtocol();
				_kb = KnowledgeBaseFactory.createKnowledgeBase(log, _kbConfig);
				((DBKnowledgeBase) _kb).initLocalVariablesFromDatabase(log);
				assertEquals("Errors: " + log.getErrors(), List.of(), log.getErrors());
				_startupKB = _kb;
			}
			return _kb;
		}

		@Override
		protected Map<String, Map<Version, MigrationConfig>> readMigrationScripts(Log log,
				List<String> migrationModules) throws DatabaseAccessException {
			List<String> applicationModules = new ArrayList<>(migrationModules);
			applicationModules.removeAll(_testScripts.keySet());
			Map<String, Map<Version, MigrationConfig>> result =
				new HashMap<>(super.readMigrationScripts(log, applicationModules));
			result.putAll(_testScripts);
			return result;
		}

		@Override
		protected MigrationContext createMigrationContext(Protocol log, PooledConnection connection) {
			if (_dialect == null) {
				return super.createMigrationContext(log, connection);
			}
			return new MigrationContext(log, connection) {
				@Override
				public DBHelper getSQLDialect() {
					return _dialect;
				}
			};
		}

	}

	/**
	 * Suite of {@link TestResumeMigration}.
	 */
	public static Test suite() {
		return suiteNeedsBranches(TestResumeMigration.class);
	}

}
