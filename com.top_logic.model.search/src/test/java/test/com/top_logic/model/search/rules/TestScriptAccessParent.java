/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.rules.ScriptAccessParent;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link ScriptAccessParent}, the access parent computed by a TL-Script function.
 *
 * <p>
 * The model {@code TestTLScriptSecurity} has the type {@code ProjectTask}, whose access parent is
 * the project the assignee of the task is responsible for (see
 * {@code TestTLScriptSecurity-test.config.xml}). The account of the responsible employee holds
 * {@code ProjectResponsible} on the project; employees themselves are not readable for
 * non-administrative users.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestScriptAccessParent extends BasicTestCase {

	private static final String MODULE = "TestTLScriptSecurity";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	/** The account of the responsible employee, holding {@code ProjectResponsible} on the project. */
	private Person _responsible;

	/** A user holding no role on any project. */
	private Person _other;

	private TLObject _employee;

	private TLObject _project;

	private TLObject _project2;

	/** A task assigned to {@link #_employee}. */
	private TLObject _task;

	/** A task without assignee, whose access parent is empty. */
	private TLObject _freeTask;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		try (Transaction tx = beginTx()) {
			_responsible = TestPerson.createPerson("scriptAccessParentResponsible");
			_other = TestPerson.createPerson("scriptAccessParentOther");

			_employee = newObject("Employee");
			_employee.tUpdateByName("salary", Integer.valueOf(1000));
			_employee.tUpdateByName("account", _responsible);

			_project = newObject("Project");
			_project.tUpdateByName("name", "scriptAccessParentProject");
			_project.tUpdateByName("responsible", _employee);

			_project2 = newObject("Project");
			_project2.tUpdateByName("name", "scriptAccessParentProject2");

			_task = newObject("ProjectTask");
			_task.tUpdateByName("name", "task");
			_task.tUpdateByName("assignee", _employee);

			_freeTask = newObject("ProjectTask");
			_freeTask.tUpdateByName("name", "freeTask");

			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = beginTx()) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	public void testScriptDelegatesToProject() {
		assertTrue("The task follows the project its assignee is responsible for.",
			allowed(_responsible, _task));
		assertFalse(allowed(_other, _task));
	}

	public void testScriptNavigatesThroughUnreadableObject() {
		assertFalse("Precondition: the employee itself is not readable.", allowed(_responsible, _employee));
		assertTrue("The script runs without security, so it reaches the project through the employee.",
			allowed(_responsible, _task));
	}

	public void testEmptyResultDenies() {
		assertFalse(allowed(_responsible, _freeTask));
	}

	public void testSingleObject() {
		assertSame(_project, expectNoError(() -> function("t -> " + project(_project)).resolve(_task)));
	}

	public void testCollectionOfOne() {
		assertSame(_project, expectNoError(() -> function("t -> list(" + project(_project) + ")").resolve(_task)));
	}

	public void testEmptyCollection() {
		assertNull(expectNoError(() -> function("t -> list()").resolve(_task)));
	}

	public void testNullResult() {
		assertNull(expectNoError(() -> function("t -> null").resolve(_task)));
	}

	public void testSeveralObjectsDenyAndLog() {
		assertNull(expectError(
			() -> function("t -> list(" + project(_project) + ", " + project(_project2) + ")").resolve(_task)));
	}

	public void testObjectWithNullDeniesAndLogs() {
		assertNull(expectError(() -> function("t -> list(" + project(_project) + ", null)").resolve(_task)));
	}

	public void testNonObjectDeniesAndLogs() {
		assertNull(expectError(() -> function("t -> 42").resolve(_task)));
	}

	public void testExceptionDeniesAndLogs() {
		ScriptAccessParent failing = function("t -> throw('broken')");
		assertNull(expectError(() -> failing.resolve(_task)));
		assertSame("Another object is still decided after a failure.", _project,
			function("t -> " + project(_project)).resolve(_freeTask));
	}

	public void testTypeLevelCheckCountsScriptTypeAsAccessible() {
		TLClass taskType = (TLClass) TLModelUtil.findType(MODULE + ":ProjectTask");
		assertTrue("The parent types of a computed access parent are unknown, so the type counts as accessible.",
			ModelAccessRights.getInstance().getAccessibleTypes(_other, SimpleBoundCommandGroup.READ).contains(taskType));
		assertFalse("The check of the object still denies.", allowed(_other, _task));
	}

	public void testLabelIsScriptText() {
		String expr = "t -> " + project(_project);
		assertTrue(function(expr).getLabel().toString().contains("scriptAccessParentProject"));
	}

	private static boolean allowed(Person person, TLObject object) {
		return ModelAccessRights.getInstance().isAllowed(person, object, SimpleBoundCommandGroup.READ);
	}

	/**
	 * A TL-Script expression yielding the given project, found by its unique name.
	 */
	private static String project(TLObject project) {
		return "all(`" + MODULE + ":Project`).filter(p -> $p.get(`" + MODULE + ":Project#name`) == '"
			+ project.tValueByName("name") + "').singleElement()";
	}

	private static ScriptAccessParent function(String expr) {
		try {
			ScriptAccessParent.Config config = TypedConfiguration.newConfigItem(ScriptAccessParent.Config.class);
			config.setExpr(ExprFormat.INSTANCE.getValue(ScriptAccessParent.Config.EXPR, expr));
			return (ScriptAccessParent) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		} catch (Exception ex) {
			throw new AssertionError("Cannot create the access parent script: " + expr, ex);
		}
	}

	private static <T> T expectError(Supplier<T> action) {
		CollectingLogListener listener = new CollectingLogListener(Set.of(Level.ERROR), true);
		T result;
		try {
			result = action.get();
		} finally {
			listener.deactivate();
		}
		List<LogEntry> errors = listener.getLogEntries();
		assertFalse("An error is logged.", errors.isEmpty());
		return result;
	}

	private static <T> T expectNoError(Supplier<T> action) {
		CollectingLogListener listener = new CollectingLogListener(Set.of(Level.ERROR), true);
		T result;
		try {
			result = action.get();
		} finally {
			listener.deactivate();
		}
		assertTrue("No error is logged: " + listener.getLogEntries(), listener.getLogEntries().isEmpty());
		return result;
	}

	private TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType(MODULE + ":" + className);
		return DynamicModelService.getFactoryFor(MODULE).createObject(type);
	}

	private Transaction beginTx() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	public static Test suite() {
		return KBSetup.getKBTest(TestScriptAccessParent.class,
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
