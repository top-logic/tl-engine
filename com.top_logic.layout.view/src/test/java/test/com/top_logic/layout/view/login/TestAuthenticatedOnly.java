/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.login;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.login.AuthenticatedOnly;
import com.top_logic.layout.view.login.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;

/**
 * Tests for {@link AuthenticatedOnly}: a command reserved for a logged-in user is hidden from the
 * anonymous user, and a refusal tells that user to log in.
 */
public class TestAuthenticatedOnly extends TestCase {

	/**
	 * Without a login, the command is hidden, and the state gives the missing login as its reason.
	 */
	public void testAnonymousUserIsToldToLogIn() {
		assertTrue("Precondition: the test runs without a logged-in user.", TLContext.isAnonymous());

		ExecutableState state = createRule().isExecutable(null);

		assertTrue(state.isHidden());
		assertFalse(state.isExecutable());
		assertEquals(I18NConstants.ERROR_LOGIN_REQUIRED, state.getI18NReasonKey());
	}

	/**
	 * A command the rule refuses is reported with the request to log in, not a generic reason.
	 */
	public void testRefusalAsksToLogIn() {
		HandlerResult result = HandlerResult.notExecutable(createRule().isExecutable(null));

		assertFalse(result.isSuccess());
		assertEquals(I18NConstants.ERROR_LOGIN_REQUIRED, result.getErrorMessage());
	}

	private static ViewExecutabilityRule createRule() {
		AuthenticatedOnly.Config config = TypedConfiguration.newConfigItem(AuthenticatedOnly.Config.class);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module the rule's configuration is created with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestAuthenticatedOnly.class, TypeIndex.Module.INSTANCE));
	}
}
