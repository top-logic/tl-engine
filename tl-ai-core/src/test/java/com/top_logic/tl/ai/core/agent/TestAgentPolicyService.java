/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentSettings;

/**
 * Tests that {@link AgentPolicyService} holds what a run may cost and what an agent may keep about a
 * person - read from the XML an application writes (Gitea #691).
 */
public class TestAgentPolicyService extends TestCase {

	/** Reading a configuration needs the module system. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestAgentPolicyService.class));
	}

	private static final String POLICY = ServiceConfigFixture.file(AgentPolicyService.class.getName(), """
					<instance class="com.top_logic.tl.ai.core.agent.AgentPolicyService"
						user-preferences="false"
						max-preferences-length="300"
					>
						<budget token-budget="2000000" budget-window-hours="12"
							money-budget="15" currency="EUR" exchange-rate="0.92"
						>
							<role-budgets>
								<role-budget name="tl.admin" tokens="0"/>
								<role-budget name="analyst" money="40"/>
							</role-budgets>
						</budget>
						<user-memory enabled="false" max-loaded="5"/>
					</instance>""");

	private AgentPolicyService _service;

	@Override
	protected void tearDown() throws Exception {
		if (_service != null) {
			_service.shutDown();
			_service = null;
		}
		super.tearDown();
	}

	/** Every section reaches the running service. */
	public void testTheConfigurationApplies() throws Exception {
		start(POLICY);

		assertEquals(2000000L, _service.getBudget().getTokenBudget());
		assertEquals(12, _service.getBudget().getBudgetWindowHours());
		assertEquals(Long.valueOf(0), _service.getBudget().getRoleBudgets().get("tl.admin").getTokens());
		assertNull("A role that names no money figure says nothing about money.",
			_service.getBudget().getRoleBudgets().get("tl.admin").getMoney());
		assertEquals(15.0, _service.getBudget().getMoneyBudget());
		assertEquals("EUR", _service.getBudget().getCurrency());
		assertEquals(0.92, _service.getBudget().getExchangeRate());
		assertEquals(Double.valueOf(40), _service.getBudget().getRoleBudgets().get("analyst").getMoney());
		assertNull(_service.getBudget().getRoleBudgets().get("analyst").getTokens());
		assertFalse(_service.getUserMemory().getEnabled());
		assertEquals(5, _service.getUserMemory().getMaxLoaded());
		assertFalse(_service.getUserPreferences());
		assertEquals(300, _service.getMaxPreferencesLength());
	}

	/** Nothing configured: no budget, and the shipped preference switches. */
	public void testDefaults() throws Exception {
		start(ServiceConfigFixture.file(AgentPolicyService.class.getName(),
			"<instance class=\"com.top_logic.tl.ai.core.agent.AgentPolicyService\"/>"));

		assertNull(_service.getBudget());
		assertTrue(_service.getUserPreferences());
		assertEquals(EnvironmentSettings.DEFAULT_MAX_PREFERENCES_LENGTH, _service.getMaxPreferencesLength());
	}

	/** A currency no cost can be converted into stops the start, naming what is wrong. */
	public void testAnUnusableCurrencyStopsTheStart() throws Exception {
		try {
			start(ServiceConfigFixture.file(AgentPolicyService.class.getName(), """
				<instance class="com.top_logic.tl.ai.core.agent.AgentPolicyService">
					<budget money-budget="15" currency="Euro"/>
				</instance>"""));
			fail("'Euro' is no ISO 4217 code.");
		} catch (RuntimeException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("'Euro'"));
		}
		assertNull("A service that refused to start is not the running one.", AgentPolicyService.getInstance());
	}

	private void start(String file) throws ConfigurationException {
		AgentPolicyService service = new AgentPolicyService(
			new DefaultInstantiationContext(TestAgentPolicyService.class),
			ServiceConfigFixture.read(AgentPolicyService.class, file));
		service.startUp();
		_service = service;
	}
}
