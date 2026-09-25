/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.TLScriptMethod;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for {@link AdminOnly} TL-Script functions.
 *
 * @see AdminOnlyFunctions
 * @see AdminOnlyClassFunctions
 */
@SuppressWarnings("javadoc")
public class TestAdminOnly extends AbstractSearchExpressionTest {

	private static final String RESTRICTED = AdminOnlyFunctions.PREFIX + "Restricted";

	private static final String OPEN = AdminOnlyFunctions.PREFIX + "Open";

	private static final String CLASS_RESTRICTED = AdminOnlyClassFunctions.PREFIX + "Echo";

	private Person _user;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user = TestPerson.createPerson("adminOnlyUser");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user.tDelete();
			tx.commit();
		}
		_user = null;
		super.tearDown();
	}

	public void testRefusedInteractivelyForNonAdmin() throws Exception {
		becomeUser(_user);
		assertRefused(call(RESTRICTED));
		assertRefused(call(CLASS_RESTRICTED));
		assertRefused("x -> " + call(RESTRICTED).replace("'a'", "$x"), "a");
	}

	public void testAllowedInteractivelyForAdmin() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals("a", evalInteractive(call(RESTRICTED)));
		assertEquals("a", evalInteractive(call(CLASS_RESTRICTED)));
	}

	public void testAllowedNonInteractivelyForNonAdmin() throws Exception {
		becomeUser(_user);
		assertEquals("a", eval(call(RESTRICTED)));
		assertEquals("a", eval(call(CLASS_RESTRICTED)));
	}

	public void testUnannotatedAllowedInteractivelyForNonAdmin() throws Exception {
		becomeUser(_user);
		assertEquals("a", evalInteractive(call(OPEN)));
	}

	public void testNoCompileTimeEvaluation() throws Exception {
		TLScriptMethod call = build(RESTRICTED);
		assertFalse("An admin-only function must be checked when the script runs.",
			call.canEvaluateAtCompileTime(new Object[] { "a" }));
		assertFalse("Copy must keep the admin check.",
			((TLScriptMethod) call.copy(call.getArguments())).canEvaluateAtCompileTime(new Object[] { "a" }));
	}

	private static TLScriptMethod build(String function) throws Exception {
		TLScriptMethod.Builder builder = (TLScriptMethod.Builder) SearchBuilder.getInstance().getBuilder(function);
		assertNotNull("Function '" + function + "' not registered.", builder);
		return builder.build(null, new SearchExpression[] { SearchExpressionFactory.literal("a") });
	}

	private static String call(String function) {
		return function + "('a')";
	}

	private void assertRefused(String script, Object... args) throws Exception {
		try {
			evalInteractive(script, args);
			fail("A user who is not an administrator must not call an admin-only function interactively: "
				+ script);
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.model.search.expr.I18NConstants.PERMISSION_DENIED__NAME,
				ex.getErrorKey().plain());
		}
	}

	private Object evalInteractive(String script, Object... args) throws Exception {
		QueryExecutor executor = QueryExecutor.compile(kb(), model(), search(script));
		return executor.executeWith(executor.context(true, null, null), Args.some(args));
	}

	public static Test suite() {
		return suite(TestAdminOnly.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}
