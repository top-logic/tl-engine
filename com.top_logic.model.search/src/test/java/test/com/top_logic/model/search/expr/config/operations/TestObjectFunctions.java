/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.operations.ObjectFunctions;
import com.top_logic.model.search.expr.interpreter.UpdateSecurityVisitor;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;

/**
 * Test for {@link ObjectFunctions}.
 *
 * <p>
 * The test uses the types of the model <code>TestTLScriptSecurity</code>, whose instances are stored
 * in different tables: <code>Employee</code> in <code>TLSecEmployee</code>, which a
 * non-administrative user may not read, and <code>UnsecuredData</code> in
 * <code>TLSecUnsecuredData</code>, which everybody may read. Accounts are stored in the table
 * <code>Person</code>.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestObjectFunctions extends AbstractSearchExpressionTest {

	private static final String MODULE = "TestTLScriptSecurity";

	private static final String EMPLOYEE_TABLE = "TLSecEmployee";

	private static final String UNSECURED_TABLE = "TLSecUnsecuredData";

	private static final String PERSON_TABLE = "Person";

	private static final String TABLE = "x -> objectTable($x)";

	private static final String ID = "x -> objectId($x)";

	private static final String RESOLVE = "t -> i -> objectResolve($t, $i)";

	private static final String ROUND_TRIP = "x -> objectResolve(objectTable($x), objectId($x))";

	private static final String RESOLVES = "t -> i -> objectResolve($t, $i) != null";

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	private Person _root;

	/** Non-administrative user without any role. */
	private Person _user;

	private TLObject _employee;

	private TLObject _unsecured;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		kb().addUpdateListener(_creationListener);
		_root = PersonManager.getManager().getRoot();

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user = TestPerson.createPerson("objectFunctionsUser");

			_employee = newObject("Employee");
			_employee.tUpdateByName("account", _user);

			_unsecured = newObject("UnsecuredData");
			_unsecured.tUpdateByName("data", "d1");

			tx.commit();
		}
		TLContext.getContext().setCurrentPerson(_root);
	}

	@Override
	protected void tearDown() throws Exception {
		kb().removeUpdateListener(_creationListener);
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		super.tearDown();
	}

	private static TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType(MODULE + ":" + className);
		return DynamicModelService.getFactoryFor(MODULE).createObject(type);
	}

	public void testTable() throws Exception {
		assertEquals(EMPLOYEE_TABLE, eval(TABLE, _employee));
		assertEquals(UNSECURED_TABLE, eval(TABLE, _unsecured));
		assertEquals(PERSON_TABLE, eval(TABLE, _user));
	}

	public void testRoundTrip() throws Exception {
		assertSame(_employee, eval(ROUND_TRIP, _employee));
		assertSame(_unsecured, eval(ROUND_TRIP, _unsecured));
		assertSame(_user, eval(ROUND_TRIP, _user));
	}

	public void testResolveFromTexts() throws Exception {
		String table = (String) eval(TABLE, _unsecured);
		String id = (String) eval(ID, _unsecured);
		assertSame(_unsecured, eval(RESOLVE, table, id));
	}

	public void testNull() throws Exception {
		assertNull(eval(TABLE, (Object) null));
		assertNull(eval(ID, (Object) null));
		assertNull(eval(RESOLVE, null, eval(ID, _unsecured)));
		assertNull(eval(RESOLVE, UNSECURED_TABLE, null));
	}

	public void testTransient() throws Exception {
		TLClass type = (TLClass) TLModelUtil.findType(MODULE + ":UnsecuredData");
		TLObject transientObject = TransientObjectFactory.INSTANCE.createObject(type);
		assertNull(eval(TABLE, transientObject));
		assertNull(eval(ID, transientObject));
	}

	public void testUnknownTable() throws Exception {
		assertNull(eval(RESOLVE, "NoSuchTable", eval(ID, _unsecured)));
		assertNull(eval(RESOLVE, "", eval(ID, _unsecured)));
	}

	public void testInvalidId() throws Exception {
		assertNull(eval(RESOLVE, UNSECURED_TABLE, ""));
		assertNull(eval(RESOLVE, UNSECURED_TABLE, "no id"));
		assertNull(eval(RESOLVE, UNSECURED_TABLE, "999999999"));
	}

	public void testIdOfOtherTable() throws Exception {
		assertNull(eval(RESOLVE, UNSECURED_TABLE, eval(ID, _employee)));
		assertNull(eval(RESOLVE, EMPLOYEE_TABLE, eval(ID, _unsecured)));
		assertNull(eval(RESOLVE, PERSON_TABLE, eval(ID, _employee)));
	}

	public void testNotReadableNotResolved() throws Exception {
		String id = (String) eval(ID, _employee);
		assertEquals(Boolean.TRUE, eval(RESOLVES, EMPLOYEE_TABLE, id));

		TLContext.getContext().setCurrentPerson(_user);
		assertEquals("An object the user may not read must not be found.",
			Boolean.FALSE, eval(RESOLVES, EMPLOYEE_TABLE, id));
		assertEquals("An object the user may read is found.",
			Boolean.TRUE, eval(RESOLVES, UNSECURED_TABLE, eval(ID, _unsecured)));
	}

	public void testNotReadableResolvedWithoutSecurity() throws Exception {
		String id = (String) eval(ID, _employee);
		TLContext.getContext().setCurrentPerson(_user);

		SearchExpression expr = search(RESOLVES);
		UpdateSecurityVisitor.disableSecurity(expr);
		assertEquals(Boolean.TRUE, executeCompiled(expr, EMPLOYEE_TABLE, id));

		QueryExecutor executor = QueryExecutor.compile(kb(), model(), search(RESOLVE));
		executor.disableSecurity();
		assertSame(_employee, executor.execute(EMPLOYEE_TABLE, id));
	}

	public static Test suite() {
		return suite(TestObjectFunctions.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}
