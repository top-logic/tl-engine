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
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
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
 * <code>TLSecUnsecuredData</code>, which everybody may read. Both implement the interface
 * <code>Record</code>. Accounts are stored in the table <code>Person</code>.
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

	private static final String RESOLVE = "t -> i -> objectResolve($i, $t)";

	private static final String ROUND_TRIP = "x -> objectId($x).objectResolve(objectTable($x))";

	private static final String RESOLVES = "t -> i -> objectResolve($i, $t) != null";

	private static final String KEY = "x -> objectKey($x)";

	private static final String RESOLVE_KEY = "k -> objectResolveKey($k)";

	private static final String RESOLVES_KEY = "k -> objectResolveKey($k) != null";

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

	public void testRoundTripByType() throws Exception {
		assertSame(_employee, eval(RESOLVE, type("Employee"), eval(ID, _employee)));
		assertSame(_unsecured, eval(RESOLVE, type("UnsecuredData"), eval(ID, _unsecured)));
		assertSame(_user, eval("x -> objectId($x).objectResolve(`tl.accounts:Person`)", _user));
		assertSame(_employee,
			eval("x -> objectResolve(objectId($x), `TestTLScriptSecurity:Employee`)", _employee));
	}

	public void testResolveBySupertype() throws Exception {
		TLClass record = type("Record");
		assertEquals("Subtypes must be stored in different tables for this test.", 2,
			TLModelUtil.potentialTables(record, false).size());
		assertSame(_employee, eval(RESOLVE, record, eval(ID, _employee)));
		assertSame(_unsecured, eval(RESOLVE, record, eval(ID, _unsecured)));
	}

	public void testTypeMismatch() throws Exception {
		// Same table, but not an instance of the requested type.
		assertNull(eval(RESOLVE, type("UnsecuredDataSub"), eval(ID, _unsecured)));
		// Another table.
		assertNull(eval(RESOLVE, type("Employee"), eval(ID, _unsecured)));
		assertNull(eval(RESOLVE, type("Project"), eval(ID, _employee)));
	}

	public void testNeitherTypeNorTable() throws Exception {
		assertNull(eval(RESOLVE, Double.valueOf(1), eval(ID, _unsecured)));
		assertNull(eval(RESOLVE, _unsecured, eval(ID, _unsecured)));
	}

	public void testNotReadableNotResolvedByType() throws Exception {
		String id = (String) eval(ID, _employee);
		assertEquals(Boolean.TRUE, eval(RESOLVES, type("Employee"), id));
		assertEquals(Boolean.TRUE, eval(RESOLVES, type("Record"), id));

		TLContext.getContext().setCurrentPerson(_user);
		assertEquals(Boolean.FALSE, eval(RESOLVES, type("Employee"), id));
		assertEquals(Boolean.FALSE, eval(RESOLVES, type("Record"), id));
		assertEquals(Boolean.TRUE, eval(RESOLVES, type("Record"), eval(ID, _unsecured)));

		QueryExecutor executor = QueryExecutor.compile(kb(), model(), search(RESOLVE));
		executor.disableSecurity();
		assertSame(_employee, executor.execute(type("Record"), id));
	}

	public void testArgumentsSwapped() throws Exception {
		String id = (String) eval(ID, _employee);
		assertNull("A type in place of the identifier names no object.",
			eval("i -> objectResolve(`TestTLScriptSecurity:Employee`, $i)", id));
		assertNull("A table in place of the identifier names no object.",
			eval("i -> objectResolve('" + EMPLOYEE_TABLE + "', $i)", id));
	}

	private static TLClass type(String className) {
		return (TLClass) TLModelUtil.findType(MODULE + ":" + className);
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

	public void testKeyRoundTrip() throws Exception {
		for (TLObject object : new TLObject[] { _employee, _unsecured, _user }) {
			String key = (String) eval(KEY, object);
			assertEquals("The key is the text form of the object's identifier.", object.tId().asString(), key);
			assertTrue("The key starts with the table storing the object: " + key,
				key.startsWith(eval(TABLE, object) + ":"));
			assertFalse("The key of a current object names no revision: " + key, key.contains("@"));
			assertSame(object, eval(RESOLVE_KEY, key));
		}
	}

	public void testKeyOfHistoricObject() throws Exception {
		TLObject historic = WrapperHistoryUtils.getWrapper(HistoryUtils.getLastRevision(), _unsecured);

		String key = (String) eval(KEY, historic);
		assertTrue("The key of a historic object names its revision: " + key, key.contains("@"));
		assertEquals("A key naming a revision finds the object as it was then.", historic,
			eval(RESOLVE_KEY, key));

		String currentKey = (String) eval(KEY, _unsecured);
		delete(_unsecured);
		assertEquals("A deleted object is still found in the revision it lived in.", historic,
			eval(RESOLVE_KEY, key));
		assertNull("A deleted object is not found now.", eval(RESOLVE_KEY, currentKey));
	}

	public void testKeyOfTransientObject() throws Exception {
		TLClass type = type("UnsecuredData");
		TLObject transientObject = TransientObjectFactory.INSTANCE.createObject(type);
		assertNull("A transient object has no key.", eval(KEY, transientObject));
		assertNull(eval(KEY, (Object) null));
	}

	public void testResolveNoKey() throws Exception {
		assertNull(eval(RESOLVE_KEY, "nonsense"));
		assertNull(eval(RESOLVE_KEY, "NoSuchTable:1"));
		assertNull(eval(RESOLVE_KEY, UNSECURED_TABLE + ":999999999"));
		assertNull(eval(RESOLVE_KEY, ""));
		assertNull(eval(RESOLVE_KEY, (Object) null));
	}

	public void testNotReadableNotResolvedByKey() throws Exception {
		String key = (String) eval(KEY, _employee);
		String historicKey =
			(String) eval(KEY, WrapperHistoryUtils.getWrapper(HistoryUtils.getLastRevision(), _employee));
		assertEquals(Boolean.TRUE, eval(RESOLVES_KEY, key));
		assertEquals(Boolean.TRUE, eval(RESOLVES_KEY, historicKey));

		TLContext.getContext().setCurrentPerson(_user);
		assertEquals("An object the user may not read must not be found by its key.",
			Boolean.FALSE, eval(RESOLVES_KEY, key));
		assertEquals("A historic object the user may not read must not be found by its key.",
			Boolean.FALSE, eval(RESOLVES_KEY, historicKey));
		assertEquals("An object the user may read is found by its key.",
			Boolean.TRUE, eval(RESOLVES_KEY, eval(KEY, _unsecured)));
	}

	public void testNotReadableResolvedByKeyWithoutSecurity() throws Exception {
		String key = (String) eval(KEY, _employee);
		TLContext.getContext().setCurrentPerson(_user);

		SearchExpression expr = search(RESOLVES_KEY);
		UpdateSecurityVisitor.disableSecurity(expr);
		assertEquals(Boolean.TRUE, executeCompiled(expr, key));

		QueryExecutor executor = QueryExecutor.compile(kb(), model(), search(RESOLVE_KEY));
		executor.disableSecurity();
		assertSame(_employee, executor.execute(key));
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
