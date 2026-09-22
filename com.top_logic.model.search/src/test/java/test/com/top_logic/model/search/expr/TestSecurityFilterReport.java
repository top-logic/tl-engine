/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
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
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SecurityFilterReport;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;

/**
 * Tests for the {@link SecurityFilterReport} filled by the security filter of a script result.
 *
 * <p>
 * The fixture reuses the model {@code TestTLScriptSecurity} (see {@link TestTLScriptSecurity}):
 * {@code Project} is readable for the responsible employee's account, {@code Employee} has no grant
 * and is therefore not readable for a non-administrative user.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSecurityFilterReport extends AbstractSearchExpressionTest {

	private static final String ALL_EMPLOYEES = "all(`TestTLScriptSecurity:Employee`)";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	/**
	 * Non-administrative user: the account of the responsible employees, so it may read both
	 * projects, but no employee.
	 */
	private Person _user;

	private TLObject _e1;

	private TLObject _e2;

	private TLObject _p1;

	private TLObject _p2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user = TestPerson.createPerson("securityReportUser");

			_e1 = newObject("Employee");
			_e1.tUpdateByName("salary", Integer.valueOf(2000));
			_e1.tUpdateByName("account", _user);

			_e2 = newObject("Employee");
			_e2.tUpdateByName("salary", Integer.valueOf(500));
			_e2.tUpdateByName("account", _user);

			_p1 = newObject("Project");
			_p1.tUpdateByName("name", "p1");
			_p1.tUpdateByName("responsible", _e1);
			_p1.tUpdateByName("members", Collections.emptyList());

			_p2 = newObject("Project");
			_p2.tUpdateByName("name", "p2");
			_p2.tUpdateByName("responsible", _e2);
			_p2.tUpdateByName("members", Collections.emptyList());

			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	private TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType("TestTLScriptSecurity:" + className);
		return DynamicModelService.getFactoryFor("TestTLScriptSecurity").createObject(type);
	}

	/**
	 * Establishes a person context for the given user, so that the security check is not bypassed by
	 * a system context.
	 */
	private void becomeUser(Person person) {
		TLContext.getContext().setCurrentPerson(person);
	}

	private TLStructuredType employeeType() {
		return (TLStructuredType) TLModelUtil.findType("TestTLScriptSecurity:Employee");
	}

	/**
	 * The readable objects are kept, the removed ones are counted and grouped by their type.
	 */
	public void testDroppedObjectsAreCountedByType() throws Exception {
		becomeUser(_user);

		List<TLObject> value = list(_p1, _e1, _p2, _e2);
		SecurityFilterReport report = new SecurityFilterReport();
		Object filtered = SearchExpression.filterSecurity(_user, value, report);

		assertEquals(list(_p1, _p2), filtered);
		assertFalse(report.isEmpty());
		assertEquals(2, report.droppedCount());
		assertEquals(Collections.singletonMap(employeeType(), Integer.valueOf(2)), report.droppedByType());
	}

	/**
	 * When nothing is removed, the report stays empty and the original instance is returned.
	 */
	public void testNothingDropped() throws Exception {
		becomeUser(_user);

		List<TLObject> value = list(_p1, _p2);
		SecurityFilterReport report = new SecurityFilterReport();
		Object filtered = SearchExpression.filterSecurity(_user, value, report);

		assertSame("An unchanged value must be returned as-is.", value, filtered);
		assertTrue(report.isEmpty());
		assertEquals(0, report.droppedCount());
		assertTrue(report.droppedByType().isEmpty());
	}

	/**
	 * Objects removed from a nested collection or from a map (as a value or as a key) are counted as
	 * well.
	 */
	public void testNestedValuesAreCounted() throws Exception {
		becomeUser(_user);

		List<TLObject> readable = list(_p1, _p2);
		Map<Object, Object> value = new LinkedHashMap<>();
		value.put("readable", readable);
		value.put("mixed", list(_p1, _e1));
		value.put(_e2, "value of an unreadable key");

		SecurityFilterReport report = new SecurityFilterReport();
		Object filtered = SearchExpression.filterSecurity(_user, value, report);

		Map<Object, Object> expected = new LinkedHashMap<>();
		expected.put("readable", readable);
		expected.put("mixed", list(_p1));
		assertEquals(expected, filtered);
		assertSame("A nested value that needs no filtering must be returned as-is.",
			readable, ((Map<?, ?>) filtered).get("readable"));

		assertEquals(2, report.droppedCount());
		assertEquals(Collections.singletonMap(employeeType(), Integer.valueOf(2)), report.droppedByType());
	}

	/**
	 * An execution fills the report attached to its {@link EvalContext}.
	 */
	public void testExecutorFillsReport() throws Exception {
		becomeUser(_user);

		QueryExecutor query = QueryExecutor.compile(kb(), model(), search(ALL_EMPLOYEES));
		SecurityFilterReport report = new SecurityFilterReport();
		EvalContext context = query.context();
		context.setSecurityReport(report);

		assertEquals(set(), asSet(query.executeWith(context, Args.none())));
		assertEquals(2, report.droppedCount());
		assertEquals(Collections.singletonMap(employeeType(), Integer.valueOf(2)), report.droppedByType());
	}

	/**
	 * Without a report attached to the {@link EvalContext}, the execution delivers the same result.
	 */
	public void testExecutionWithoutReport() throws Exception {
		becomeUser(_user);

		QueryExecutor query = QueryExecutor.compile(kb(), model(), search(ALL_EMPLOYEES));
		assertEquals(set(), asSet(query.execute()));
	}

	/**
	 * An executor with disabled security filters nothing and therefore records nothing.
	 */
	public void testDisabledSecurityRecordsNothing() throws Exception {
		becomeUser(_user);

		QueryExecutor query = QueryExecutor.compile(kb(), model(), search(ALL_EMPLOYEES));
		query.disableSecurity();
		SecurityFilterReport report = new SecurityFilterReport();
		EvalContext context = query.context();
		context.setSecurityReport(report);

		assertEquals(set(_e1, _e2), asSet(query.executeWith(context, Args.none())));
		assertTrue("Nothing is filtered, so nothing can be reported.", report.isEmpty());
	}

	/**
	 * The filter without a report keeps its behaviour.
	 */
	public void testFilterWithoutReport() throws Exception {
		becomeUser(_user);

		assertEquals(list(_p1), SearchExpression.filterSecurity(_user, list(_p1, _e1)));

		List<TLObject> readable = list(_p1, _p2);
		assertSame(readable, SearchExpression.filterSecurity(_user, readable));
	}

	/**
	 * A reset report can be used for a further execution.
	 */
	public void testReset() throws Exception {
		becomeUser(_user);

		SecurityFilterReport report = new SecurityFilterReport();
		SearchExpression.filterSecurity(_user, list(_p1, _e1), report);
		assertEquals(1, report.droppedCount());

		report.reset();
		assertTrue(report.isEmpty());
		assertEquals(0, report.droppedCount());
		assertTrue(report.droppedByType().isEmpty());

		SearchExpression.filterSecurity(_user, list(_e1, _e2), report);
		assertEquals(2, report.droppedCount());
	}

	public static Test suite() {
		return suite(TestSecurityFilterReport.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}
