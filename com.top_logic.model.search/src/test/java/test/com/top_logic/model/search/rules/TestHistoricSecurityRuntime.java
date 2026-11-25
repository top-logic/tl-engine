/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.KBSetup.KBType;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy.AggregatingAnalyzer;
import com.top_logic.basic.sql.LoggingDataSourceProxy.Statistics;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Measures the time needed to check the access rights on many historic versions of one revision,
 * whose roles are inherited through a deep chain of role parents from a single anchor.
 *
 * <p>
 * The measured values are written to the console, together with the number of statements sent to
 * the database and, for the first two runs, the most frequent statements. The database is selected with the system
 * property <code>historicSecurityRuntime.db</code> (<code>h2</code>, <code>oracle19</code>,
 * <code>postgresql</code>, <code>mysql</code>, <code>mssql</code>), the default is <code>h2</code>.
 * The model, rules and grants are the ones of {@link TestHistoricSecurity}.
 * </p>
 */
@DeactivatedTest("Measurement of the runtime, to be started by hand.")
@SuppressWarnings("javadoc")
public class TestHistoricSecurityRuntime extends BasicTestCase {

	private static final String MODULE = "TestHistoricSecurity";

	/** System property selecting the database. */
	private static final String DB_PROPERTY = "historicSecurityRuntime.db";

	/** Number of chains of chapters below the released root chapter. */
	private static final int CHAINS = 111;

	/** Number of chapters from the released root chapter to the end of a chain. */
	private static final int DEPTH = 10;

	private static final int RUNS = 8;

	private static final String _db = System.getProperty(DB_PROPERTY, "h2");

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	private Person _recipient;

	private Person _editor;

	private TLObject _unrelated;

	private int _touch;

	/** Counts the statements sent to the database, <code>null</code> if they cannot be observed. */
	private AggregatingAnalyzer _statements;

	private LoggingDataSourceProxy _dataSource;

	/** Whether the most frequent statements of the measured check are written. */
	private boolean _details;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);
		ConnectionPool pool = KBUtils.getConnectionPool(_kb);
		if (pool.getDataSource() instanceof LoggingDataSourceProxy dataSource) {
			_statements = new AggregatingAnalyzer(true);
			dataSource.setAnalyzer(_statements);
			_dataSource = dataSource;
			pool.clear();
		} else {
			log("The statements cannot be counted: " + pool.getDataSource());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		if (_dataSource != null) {
			_dataSource.setAnalyzer(null);
			KBUtils.getConnectionPool(_kb).clear();
		}
		try (Transaction tx = beginTx()) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	public void testDeepChainOfRoleParents() {
		List<TLObject> chapters = new ArrayList<>();
		TLObject area;
		Revision released;
		try (Transaction tx = beginTx()) {
			_recipient = TestPerson.createPerson("histsecRuntimeRecipient");
			_editor = TestPerson.createPerson("histsecRuntimeEditor");
			_unrelated = newObject("Glossary");
			area = newObject("Area");
			area.tUpdateByName("name", "A");
			area.tUpdateByName("editors", list(_editor));
			TLObject root = chapter("C", area);
			chapters.add(root);
			List<TLObject> firstLevel = new ArrayList<>();
			for (int chain = 0; chain < CHAINS; chain++) {
				TLObject parent = null;
				for (int level = 1; level < DEPTH; level++) {
					TLObject child = chapter("C" + chain + "." + level, area);
					chapters.add(child);
					if (parent == null) {
						firstLevel.add(child);
					} else {
						parent.tUpdateByName("children", list(child));
					}
					parent = child;
				}
			}
			root.tUpdateByName("children", firstLevel);
			tx.commit();
		}
		try (Transaction tx = beginTx()) {
			TLObject release = newObject("AreaRelease");
			release.tUpdateByName("name", "R");
			release.tUpdateByName("roots", list(chapters.get(0)));
			release.tUpdateByName("recipients", list(_recipient));
			tx.commit();
			released = tx.getCommitRevision();
		}
		try (Transaction tx = beginTx()) {
			area.tUpdateByName("name", "A (current)");
			tx.commit();
		}
		List<BoundObject> versions = new ArrayList<>();
		List<BoundObject> currents = new ArrayList<>();
		for (TLObject chapter : chapters) {
			versions.add((BoundObject) at(chapter, released));
			currents.add((BoundObject) chapter);
		}
		AccessManager accessManager = AccessManager.getInstance();
		List<BoundedRole> roles = Arrays.asList(BoundedRole.getRoleByName(MODULE + ".AreaReader"),
			BoundedRole.getRoleByName(MODULE + ".AreaEditor"));
		log("database=" + _db + ", objects=" + chapters.size() + ", depth=" + DEPTH);
		for (int run = 0; run < RUNS; run++) {
			// The first run loads the objects, the second one works on loaded objects.
			_details = run < 2;
			String listVersionsRecipient = measure("listVersionsRecipient", () -> TLContextManager.inPersonContext(_recipient,
				() -> accessManager.getAllowedBusinessObjects(_recipient, roles, versions)).size());
			String singleChecks = measure("singleChecks", () -> TLContextManager.inPersonContext(_recipient, () -> {
				int allowed = 0;
				for (BoundObject version : versions) {
					if (ModelAccessRights.getInstance().isAllowed(_recipient, (TLObject) version,
						SimpleBoundCommandGroup.READ)) {
						allowed++;
					}
				}
				return allowed;
			}));
			String listVersionsEditor = measure("listVersionsEditor", () -> TLContextManager.inPersonContext(_editor,
				() -> accessManager.getAllowedBusinessObjects(_editor, roles, versions)).size());
			String listCurrentEditor = measure("listCurrentEditor", () -> TLContextManager.inPersonContext(_editor,
				() -> accessManager.getAllowedBusinessObjects(_editor, roles, currents)).size());
			String singleChecksCurrent = measure("singleChecksCurrentEditor",
				() -> TLContextManager.inPersonContext(_editor, () -> {
					int allowed = 0;
					for (BoundObject current : currents) {
						if (ModelAccessRights.getInstance().isAllowed(_editor, (TLObject) current,
							SimpleBoundCommandGroup.READ)) {
							allowed++;
						}
					}
					return allowed;
				}));
			log("run " + run + ": listVersionsRecipient=" + listVersionsRecipient + ", singleChecksRecipient="
				+ singleChecks + ", listVersionsEditor=" + listVersionsEditor + ", listCurrentEditor="
				+ listCurrentEditor + ", singleChecksCurrentEditor=" + singleChecksCurrent);
		}
	}

	/**
	 * Measures the given check, which starts without answers remembered for the interaction.
	 *
	 * @return The time needed and the number of objects the check allowed.
	 */
	private String measure(String name, Supplier<Integer> check) {
		touch();
		if (_statements != null) {
			_statements.take();
		}
		long start = System.nanoTime();
		int allowed = check.get();
		long nanos = System.nanoTime() - start;
		String result = nanos / 1000000 + "ms (" + allowed + " allowed";
		if (_statements != null) {
			List<Map.Entry<String, Statistics>> statements = _statements.take();
			statements.sort((s1, s2) -> Integer.compare(s2.getValue().getCnt(), s1.getValue().getCnt()));
			int calls = 0;
			long elapsed = 0;
			for (Map.Entry<String, Statistics> statement : statements) {
				calls += statement.getValue().getCnt();
				elapsed += statement.getValue().getElapsedNanoSum();
			}
			result += ", " + calls + " statements taking " + elapsed / 1000000 + "ms";
			if (_details) {
				for (Map.Entry<String, Statistics> statement : statements.subList(0, Math.min(20, statements.size()))) {
					Statistics statistics = statement.getValue();
					String sql = statement.getKey().replaceAll("\\s+", " ");
					int from = sql.indexOf(" FROM ");
					if (from > 0) {
						sql = "SELECT ..." + sql.substring(from);
					}
					log("  " + name + ": " + statistics.getCnt() + " x " + statistics.getElapsedNanoSum() / 1000000
						+ "ms, rows " + statistics.getRowsAvg() + ": " + sql.substring(0, Math.min(700, sql.length())));
					Exception stacktrace = statistics.getStacktrace();
					if (stacktrace != null) {
						StringBuilder frames = new StringBuilder();
						int count = 0;
						for (StackTraceElement frame : stacktrace.getStackTrace()) {
							String className = frame.getClassName();
							if (className.contains(".sql.") || className.contains(".db2.")
								|| !className.startsWith("com.top_logic")) {
								continue;
							}
							frames.append("\n      at ").append(className.substring(className.lastIndexOf('.') + 1))
								.append('.').append(frame.getMethodName()).append(':').append(frame.getLineNumber());
							if (++count == 14) {
								break;
							}
						}
						log("    first caller:" + frames);
					}
				}
			}
		}
		return result + ")";
	}

	/**
	 * Commits a change of an unrelated object: the answers remembered for the interaction do not
	 * hold for the new revision, so the next measurement starts without them.
	 */
	private void touch() {
		try (Transaction tx = beginTx()) {
			_unrelated.tUpdateByName("name", "G" + _touch++);
			tx.commit();
		}
	}

	private static void log(String message) {
		System.out.println("[" + TestHistoricSecurityRuntime.class.getSimpleName() + "] " + message);
	}

	private TLObject chapter(String name, TLObject area) {
		TLObject chapter = newObject("Chapter");
		chapter.tUpdateByName("name", name);
		chapter.tUpdateByName("area", area);
		return chapter;
	}

	private static TLObject at(TLObject object, Revision revision) {
		return WrapperHistoryUtils.getWrapper(revision, object);
	}

	private TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType(MODULE + ":" + className);
		return DynamicModelService.getFactoryFor(MODULE).createObject(type);
	}

	private Transaction beginTx() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	private static KBType kbType() {
		switch (_db) {
			case "h2":
				return KBType.KB_H2;
			case "oracle19":
				return KBType.KB_ORACLE19;
			case "postgresql":
				return KBType.KB_POSTGRESQL;
			case "mysql":
				return KBType.KB_MYSQL;
			case "mssql":
				return KBType.KB_MSSQL;
			default:
				throw new IllegalArgumentException("Unknown database in " + DB_PROPERTY + ": " + _db);
		}
	}

	public static Test suite() {
		return KBSetup.getKBTest(TestHistoricSecurityRuntime.class, kbType(),
			ServiceTestSetup.createStarterFactoryForModules(
				SearchBuilder.Module.INSTANCE,
				ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				InitialRolesManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE));
	}

}
