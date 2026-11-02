/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.configured;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.configured.ConfiguredTLScriptFunctions;
import com.top_logic.model.search.configured.I18NConstants;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests privileged TL-Script functions (ticket #29858) on the proposals of the model
 * {@code TestTLScriptSecurity}.
 *
 * <p>
 * Fixture: the approver is an approver of {@code p1} and therefore holds ProposalApprover (read, and
 * Approve by the test configuration) on it, but no write right. The auditor reads {@code p1} as
 * ProposalReader, but holds no Approve. Nobody but root holds a role on {@code p2}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestPrivilegedScript extends AbstractSearchExpressionTest {

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	Person _approver;

	Person _auditor;

	/** The employee of {@link #_approver}. */
	TLObject _member;

	TLObject _p1, _p2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_approver = TestPerson.createPerson("privilegedApprover");
			_auditor = TestPerson.createPerson("privilegedAuditor");

			_member = newObject("Employee");
			_member.tUpdateByName("account", _approver);

			TLObject reader = newObject("Employee");
			reader.tUpdateByName("account", _auditor);

			_p1 = newObject("Proposal");
			_p1.tUpdateByName("name", "p1");
			_p1.tUpdateByName("approvers", Collections.singletonList(_member));
			_p1.tUpdateByName("readers", Collections.singletonList(reader));

			_p2 = newObject("Proposal");
			_p2.tUpdateByName("name", "p2");
			tx.commit();
		}
	}

	/**
	 * The tests check the access rights of the calling user. A test starts without a user, since an
	 * administrator would bypass the checks; it switches to the root user to compute a value without
	 * restrictions.
	 */
	@Override
	protected boolean checksSecurity() {
		return true;
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			for (KnowledgeItem item : _toDelete.values()) {
				if (item.isAlive()) {
					_kb.delete(item);
				}
			}
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

	private Object inTransaction(String script, Object... args) throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			Object result = execute(search(script), args);
			tx.commit();
			return result;
		}
	}

	public void testDirectWriteIsDenied() throws Exception {
		becomeUser(_approver);
		try {
			inTransaction("p -> $p.set(`TestTLScriptSecurity:PathNode#name`, 'direct')", _p1);
			fail("The approver holds no write right on the proposal.");
		} catch (TopLogicException ex) {
			// expected
		}
	}

	public void testAllowedCallRunsUnsecured() throws Exception {
		becomeUser(_approver);
		inTransaction("p -> testPrivilegedRename($p, 'renamed')", _p1);
		assertEquals("renamed", _p1.tValueByName("name"));
	}

	public void testDeniedCallWithoutOnDeniedThrows() throws Exception {
		becomeUser(_auditor);
		try {
			inTransaction("p -> testPrivilegedRename($p, 'renamed')", _p1);
			fail("The auditor holds no Approve right.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedRename"));
		}
		assertEquals("p1", _p1.tValueByName("name"));
	}

	public void testOnDeniedValueIsReturned() throws Exception {
		becomeUser(_auditor);
		assertEquals("denied:p1", execute(search("p -> testPrivilegedOnDeniedValue($p)"), _p1));
		becomeUser(_approver);
		assertEquals("allowed", execute(search("p -> testPrivilegedOnDeniedValue($p)"), _p1));
	}

	public void testOnDeniedIsEvaluatedWithSecurity() throws Exception {
		// The auditor discovers only p1. An unsecured on-denied would also count p2.
		becomeUser(_auditor);
		assertEquals(Double.valueOf(1), execute(search("p -> testPrivilegedOnDeniedCount($p)"), _p1));
	}

	public void testOnDeniedThrow() throws Exception {
		becomeUser(_auditor);
		try {
			execute(search("p -> testPrivilegedOnDeniedThrow($p)"), _p1);
			fail("on-denied throws.");
		} catch (RuntimeException ex) {
			assertTrue(String.valueOf(ex.getMessage()), String.valueOf(ex.getMessage()).contains("custom denial"));
		}
	}

	public void testNullSecurityObjectDenies() throws Exception {
		becomeUser(_approver);
		try {
			execute(search("p -> testPrivilegedNever($p)"), _p1);
			fail("A null security object denies the call.");
		} catch (TopLogicException ex) {
			// expected
		}
	}

	public void testNullArgumentDenies() throws Exception {
		// p2 is not discoverable for the approver: the argument arrives as null.
		becomeUser(_approver);
		try {
			execute(search("p -> testPrivilegedRename($p, 'x')"), _p2);
			fail("An undiscoverable argument denies the call.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedRename"));
		}
	}

	/**
	 * Creates proposals for the collection tests: the approver holds Approve on the first, and
	 * only reads the second.
	 */
	private TLObject[] approvableAndReadable() {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject approvable = newObject("Proposal");
			approvable.tUpdateByName("name", "p3");
			approvable.tUpdateByName("approvers", Collections.singletonList(_member));

			TLObject readable = newObject("Proposal");
			readable.tUpdateByName("name", "p4");
			readable.tUpdateByName("readers", Collections.singletonList(_member));
			tx.commit();
			return new TLObject[] { approvable, readable };
		}
	}

	private static final String CALL_COLLECTION = "a -> b -> testPrivilegedCollection(list($a, $b))";

	private static final String CAN_CALL_COLLECTION = "a -> b -> canCall(testPrivilegedCollection(list($a, $b)))";

	public void testCollectionAllAllowed() throws Exception {
		TLObject approvable = approvableAndReadable()[0];
		becomeUser(_approver);
		assertEquals("allowed", execute(search(CALL_COLLECTION), _p1, approvable));
		assertEquals(Boolean.TRUE, execute(search(CAN_CALL_COLLECTION), _p1, approvable));
	}

	public void testCollectionOneElementDenied() throws Exception {
		TLObject readable = approvableAndReadable()[1];
		becomeUser(_approver);
		try {
			execute(search(CALL_COLLECTION), _p1, readable);
			fail("The approver holds no Approve on the second proposal.");
		} catch (TopLogicException ex) {
			assertDenial(I18NConstants.ERROR_CALL_NOT_ALLOWED__NAME_OPERATION_OBJECT, ex);
		}
		assertEquals(Boolean.FALSE, execute(search(CAN_CALL_COLLECTION), _p1, readable));
	}

	public void testCollectionEmptyDenies() throws Exception {
		becomeUser(_approver);
		try {
			execute(search("testPrivilegedCollection(list())"));
			fail("An empty collection denies the call.");
		} catch (TopLogicException ex) {
			assertDenial(I18NConstants.ERROR_CALL_NO_SECURITY_OBJECT__NAME, ex);
		}
		assertEquals(Boolean.FALSE, execute(search("canCall(testPrivilegedCollection(list()))")));
	}

	public void testCollectionNullDenies() throws Exception {
		becomeUser(_approver);
		try {
			execute(search("testPrivilegedCollection(null)"));
			fail("A null security object denies the call.");
		} catch (TopLogicException ex) {
			assertDenial(I18NConstants.ERROR_CALL_NO_SECURITY_OBJECT__NAME, ex);
		}
		assertEquals(Boolean.FALSE, execute(search("canCall(testPrivilegedCollection(null))")));
	}

	public void testCollectionWithTransientElementDenies() throws Exception {
		String transientElement = "p -> testPrivilegedCollection(list($p, new(`TestTLScriptSecurity:Proposal`, transient: true)))";
		becomeUser(_approver);
		try {
			execute(search(transientElement), _p1);
			fail("A transient element denies the whole call.");
		} catch (TopLogicException ex) {
			assertDenial(I18NConstants.ERROR_CALL_INVALID_SECURITY_OBJECT__NAME_OBJECT, ex);
		}
		assertEquals(Boolean.FALSE, execute(search(
			"p -> canCall(testPrivilegedCollection(list($p, new(`TestTLScriptSecurity:Proposal`, transient: true))))"),
			_p1));
	}

	public void testCollectionWithEditOverlay() throws Exception {
		TLObject approvable = approvableAndReadable()[0];
		TLObject overlay = formObject(_p1, false);
		becomeUser(_approver);
		assertEquals("allowed", execute(search(CALL_COLLECTION), overlay, approvable));
		assertEquals(Boolean.FALSE, execute(search(CAN_CALL_COLLECTION), formObject(_p1, true), approvable));
	}

	private static void assertDenial(ResKeyTemplate expected, TopLogicException ex) {
		assertEquals(ex.getMessage(), expected.getKey(), ex.getErrorKey().getKey());
		assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedCollection"));
	}

	public void testDefaultParameterReachesSecurityObject() throws Exception {
		becomeUser(_approver);
		assertEquals(_p1, execute(search("p -> testPrivilegedDefault($p, $p)"), _p1));
		try {
			// The default of the omitted parameter is null, which denies the call.
			execute(search("p -> testPrivilegedDefault($p)"), _p1);
			fail("The security object is the null default of the omitted parameter.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedDefault"));
		}
	}

	public void testResultIsFilteredForDiscover() throws Exception {
		becomeUser(_approver);
		Object proposals = execute(search("p -> testPrivilegedAll($p)"), _p1);
		assertTrue(asSet(proposals).contains(_p1));
		assertFalse("p2 is not discoverable for the approver.", asSet(proposals).contains(_p2));
	}

	public void testResultIsFilteredAtTheCall() throws Exception {
		// The values are computed by the calling script from the result of the call, so the final
		// filter of the execution can not hide a missing filter at the call.
		becomeUser(_approver);
		assertEquals("Only p1 is discoverable for the approver.", Double.valueOf(1),
			execute(search("p -> testPrivilegedAll($p).size()"), _p1));
	}

	public void testSingleResultIsFilteredAtTheCall() throws Exception {
		becomeUser(_approver);
		assertEquals("The discoverable p1 is delivered.", Boolean.FALSE,
			execute(search("p -> testPrivilegedByName($p, 'p1') == null"), _p1));
		assertEquals("The undiscoverable p2 becomes null.", Boolean.TRUE,
			execute(search("p -> testPrivilegedByName($p, 'p2') == null"), _p1));
	}

	public void testTransientSecurityObjectDenies() throws Exception {
		// A transient object passes every access check, it must not be accepted as security object.
		becomeUser(_auditor);
		try {
			execute(search("testPrivilegedCount(new(`TestTLScriptSecurity:Proposal`, transient: true))"));
			fail("A transient security object denies the call.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedCount"));
		}
		assertEquals(Boolean.FALSE,
			execute(search("canCall(testPrivilegedCount(new(`TestTLScriptSecurity:Proposal`, transient: true)))")));
	}

	public void testUncommittedSecurityObjectDenies() throws Exception {
		// An object created in the running transaction passes every access check, it must not be
		// accepted as security object.
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject created = newObject("Proposal");
			becomeUser(_auditor);
			try {
				execute(search("p -> testPrivilegedCount($p)"), created);
				fail("An uncommitted security object denies the call.");
			} catch (TopLogicException ex) {
				assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedCount"));
			}
			assertEquals(Boolean.FALSE, execute(search("p -> canCall(testPrivilegedCount($p))"), created));
			tx.rollback();
		}
	}

	public void testTracedCallIsChecked() throws Exception {
		// The tracing route (e.g. a computation in a form) checks the call like the plain route.
		becomeUser(_approver);
		assertEquals("allowed", trace("p -> testPrivilegedOnDeniedValue($p)", _p1));
		becomeUser(_auditor);
		assertEquals("denied:p1", trace("p -> testPrivilegedOnDeniedValue($p)", _p1));
		try {
			trace("p -> testPrivilegedRename($p, 'renamed')", _p1);
			fail("The auditor holds no Approve right.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedRename"));
		}
		assertEquals("p1", _p1.tValueByName("name"));
	}

	public void testTracedResultIsFilteredForDiscover() throws Exception {
		// A traced evaluation does not filter its final result, the call must filter it.
		becomeUser(_approver);
		Object proposals = trace("p -> testPrivilegedAll($p)", _p1);
		assertTrue(asSet(proposals).contains(_p1));
		assertFalse("p2 is not discoverable for the approver.", asSet(proposals).contains(_p2));
		assertEquals(Boolean.TRUE, trace("p -> testPrivilegedByName($p, 'p2') == null", _p1));
	}

	public void testTracedTransientSecurityObjectDenies() throws Exception {
		becomeUser(_auditor);
		try {
			trace("testPrivilegedCount(new(`TestTLScriptSecurity:Proposal`, transient: true))");
			fail("A transient security object denies the call.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedCount"));
		}
	}

	public void testEditOverlayIsCheckedAsTheEditedObject() throws Exception {
		// An edit form passes an overlay of the edited object, e.g. as security-object="$proposal".
		TLObject overlay = formObject(_p1, false);
		becomeUser(_approver);
		assertEquals(execute(search("p -> testPrivilegedCount($p)"), _p1),
			execute(search("p -> testPrivilegedCount($p)"), overlay));
		assertEquals(Boolean.TRUE, execute(search("p -> canCall(testPrivilegedCount($p))"), overlay));
		becomeUser(_auditor);
		try {
			execute(search("p -> testPrivilegedCount($p)"), overlay);
			fail("The auditor holds no Approve right on the edited proposal.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedCount"));
		}
		assertEquals(Boolean.FALSE, execute(search("p -> canCall(testPrivilegedCount($p))"), overlay));
	}

	public void testCreateOverlayDenies() throws Exception {
		// A create overlay stands for no persistent object, even if it names a base object.
		TLObject overlay = formObject(_p1, true);
		becomeUser(_approver);
		try {
			execute(search("p -> testPrivilegedCount($p)"), overlay);
			fail("A create overlay denies the call.");
		} catch (TopLogicException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("testPrivilegedCount"));
		}
		assertEquals(Boolean.FALSE, execute(search("p -> canCall(testPrivilegedCount($p))"), overlay));
	}

	/**
	 * A transient {@link TLFormObjectBase} of a form editing (or creating, if <code>create</code>
	 * is set) the given object.
	 */
	private static TLObject formObject(TLObject edited, boolean create) {
		return (TLObject) Proxy.newProxyInstance(TestPrivilegedScript.class.getClassLoader(),
			new Class<?>[] { TLFormObjectBase.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "isCreate":
						return Boolean.valueOf(create);
					case "getEditedObject":
						return create ? null : edited;
					case "tTransient":
						return Boolean.TRUE;
					case "tType":
						return edited.tType();
					case "equals":
						return Boolean.valueOf(proxy == args[0]);
					case "hashCode":
						return Integer.valueOf(System.identityHashCode(proxy));
					case "toString":
						return "formObject(" + edited + ", create: " + create + ")";
					default:
						throw new UnsupportedOperationException(method.toString());
				}
			});
	}

	private static Object trace(String script, Object... args) throws Exception {
		return ScriptTracer.compile(parse(script)).execute(pointer -> {
			// Accesses are not observed.
		}, null, args);
	}

	public void testComputedValueIsNotFiltered() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
		Object all = execute(search("all(`TestTLScriptSecurity:Proposal`).size()"));
		becomeUser(_approver);
		assertEquals(all, execute(search("p -> testPrivilegedCount($p)"), _p1));
	}

	public void testUnsecuredCallIsNotChecked() throws Exception {
		becomeUser(_approver);
		QueryExecutor executor = QueryExecutor.compile(search("p -> testPrivilegedNever($p)"));
		executor.disableSecurity();
		assertEquals("p1", executor.execute(_p1));
	}

	public void testNestedPrivilegedScriptIsNotCheckedAgain() throws Exception {
		becomeUser(_approver);
		assertEquals("p1", execute(search("p -> testPrivilegedOuter($p)"), _p1));
	}

	public void testPlainScriptKeepsSecurity() throws Exception {
		becomeUser(_auditor);
		assertEquals("p1", execute(search("p -> testPlainScript($p)"), _p1));
		becomeUser(_approver);
		assertEquals(null, execute(search("p -> testPlainScript($p)"), _p2));
	}

	public void testCanCall() throws Exception {
		becomeUser(_approver);
		assertEquals(Boolean.TRUE, execute(search("p -> canCall(testPrivilegedRename($p, 'x'))"), _p1));
		assertEquals("canCall does not call the function.", "p1", _p1.tValueByName("name"));
		becomeUser(_auditor);
		assertEquals(Boolean.FALSE, execute(search("p -> canCall(testPrivilegedRename($p, 'x'))"), _p1));
		assertEquals("canCall does not call the function.", "p1", _p1.tValueByName("name"));
	}

	public void testCanCallNullSecurityObject() throws Exception {
		becomeUser(_approver);
		assertEquals(Boolean.FALSE, execute(search("p -> canCall(testPrivilegedNever($p))"), _p1));
	}

	public void testCanCallPlainScript() throws Exception {
		becomeUser(_auditor);
		assertEquals(Boolean.TRUE, execute(search("p -> canCall(testPlainScript($p))"), _p1));
	}

	public void testCanCallChecksAlsoUnsecured() throws Exception {
		becomeUser(_auditor);
		QueryExecutor executor = QueryExecutor.compile(search("p -> canCall(testPrivilegedRename($p, 'x'))"));
		executor.disableSecurity();
		assertEquals(Boolean.FALSE, executor.execute(_p1));
	}

	public void testCanCallRejectsNonCall() {
		for (String script : new String[] { "canCall(true)", "p -> canCall(canExecute($p, 'Read'))" }) {
			try {
				QueryExecutor.compile(search(script));
				fail("canCall must reject: " + script);
			} catch (Exception ex) {
				assertTrue(script + ": " + ex.getMessage(),
					String.valueOf(ex.getMessage()).contains("must be a call of a configured TL-Script function"));
			}
		}
	}

	public static Test suite() {
		return suite(TestPrivilegedScript.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			CommandGroupRegistry.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE,
			ConfiguredTLScriptFunctions.Module.INSTANCE);
	}

}
