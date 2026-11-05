/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.addons.loginmessages.view;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.view.login.DialogStep;
import com.top_logic.layout.view.login.LoginStep;
import com.top_logic.layout.view.login.LoginStepsConfig;
import com.top_logic.model.search.expr.config.SearchBuilder;

/**
 * Tests the login step this module contributes to the {@link LoginStepsConfig}.
 */
public class TestLoginMessagesStep extends BasicTestCase {

	/** Name of the login step of this module. */
	private static final String STEP = "login-messages";

	/**
	 * Tests that the step runs right after the second factor, so that only an account that has
	 * proven itself is shown the messages.
	 */
	public void testPosition() {
		List<String> names = new ArrayList<>();
		for (LoginStep.Config<?> step : ApplicationConfig.getInstance().getConfig(LoginStepsConfig.class).getSteps()) {
			names.add(step.getName());
		}
		int position = names.indexOf(STEP);
		assertTrue("The step is configured: " + names, position > 0);
		assertEquals(names.toString(), "second-factor", names.get(position - 1));
	}

	/**
	 * Tests that the step is created, which compiles its condition calling
	 * <code>loginMessagesDue()</code>.
	 */
	public void testCreated() {
		List<LoginStep> steps = LoginStepsConfig.configured();
		assertTrue(steps.stream().anyMatch(DialogStep.class::isInstance));
	}

	/**
	 * The suite, with the TL-Script functions.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestLoginMessagesStep.class),
				TypeIndex.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE));
	}

}
