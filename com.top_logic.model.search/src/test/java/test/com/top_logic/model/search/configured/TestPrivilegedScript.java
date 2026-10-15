/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.configured;

import java.util.Collections;
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
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.configured.ConfiguredTLScriptFunctions;
import com.top_logic.model.search.expr.query.QueryExecutor;
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

	TLObject _p1, _p2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_approver = TestPerson.createPerson("privilegedApprover");
			_auditor = TestPerson.createPerson("privilegedAuditor");

			TLObject member = newObject("Employee");
			member.tUpdateByName("account", _approver);

			TLObject reader = newObject("Employee");
			reader.tUpdateByName("account", _auditor);

			_p1 = newObject("Proposal");
			_p1.tUpdateByName("name", "p1");
			_p1.tUpdateByName("approvers", Collections.singletonList(member));
			_p1.tUpdateByName("readers", Collections.singletonList(reader));

			_p2 = newObject("Proposal");
			_p2.tUpdateByName("name", "p2");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
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

	public void testOnDeniedValueIsEvaluatedSecured() throws Exception {
		becomeUser(_auditor);
		assertEquals("denied:p1", execute(search("p -> testPrivilegedOnDeniedValue($p)"), _p1));
		becomeUser(_approver);
		assertEquals("allowed", execute(search("p -> testPrivilegedOnDeniedValue($p)"), _p1));
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

	public void testCollectionSecurityObjectDenies() throws Exception {
		becomeUser(_approver);
		try {
			execute(search("p -> testPrivilegedCollection($p)"), _p1);
			fail("A collection is no security object.");
		} catch (TopLogicException ex) {
			// expected
		}
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
