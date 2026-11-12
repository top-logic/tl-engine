/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import static test.com.top_logic.model.search.providers.TestChangeHandlerByExpression.*;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.providers.ChangeHandlerByExpression;
import com.top_logic.model.search.providers.LifecycleHandlerByExpression;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContextManager;

/**
 * Test that {@link ChangeHandlerByExpression} and {@link LifecycleHandlerByExpression} run their
 * scripts independently of the rights of the user committing the transaction.
 *
 * <p>
 * The user of the test has no role, in particular no right to read or write the objects of the
 * test model. The change is made by Java code, which is not checked, so only the scripts of the
 * handlers would be subject to the rights of the user.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestHandlerByExpressionSecurity extends AbstractSearchExpressionTest {

	private static final String COUNTER = "TestHandlersByExpression:Counter";

	private static final String COUNTER_REF = "counter";

	/**
	 * Non-administrative user without any role.
	 */
	private Person _user;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user = TestPerson.createPerson("handlerScriptUser");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user.tDelete();
			tx.commit();
		}
		_user = null;
		super.tearDown();
	}

	public void testChangeHandlerAsUserWithoutRights() {
		TLObject item = newObject(ITEM, "a");

		TLContextManager.inPersonContext(_user, () -> update(item, STATUS, "open"));

		assertEquals("->open:status", item.tValueByName(LOG));
	}

	public void testDeletingAsUserWithoutRights() {
		TLObject counter = newObject(COUNTER, "c");
		TLObject item = newObject(ITEM, "x");
		update(item, COUNTER_REF, counter);

		TLContextManager.inPersonContext(_user, () -> delete(item));

		assertFalse(item.tValid());
		assertEquals("The deleting script reads the item, its function writes the counter.",
			"deleted:x:null;", counter.tValueByName(LOG));
	}

	public static Test suite() {
		return suite(TestHandlerByExpressionSecurity.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}
