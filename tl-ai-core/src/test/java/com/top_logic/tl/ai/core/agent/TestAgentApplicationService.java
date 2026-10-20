/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;

/**
 * Tests that {@link AgentApplicationService} holds what an agent knows about the application - read
 * from the XML an application writes (Gitea #691).
 */
public class TestAgentApplicationService extends TestCase {

	/** Instantiating a configured class resolves it through the {@code TypeIndex}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestAgentApplicationService.class));
	}

	private static final String APPLICATION = ServiceConfigFixture.file(AgentApplicationService.class.getName(), """
					<instance class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
						<prompt-contributors>
							<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor"
								name="application-context"
							>
								<text>
									<en>House rule.</en>
								</text>
							</contributor>
						</prompt-contributors>
						<environment>
							<application>
								<en>Demo</en>
							</application>
						</environment>
						<object-ids>
							<hints>
								<hint pattern="ORD-[0-9]+" explanation="is an order number."/>
							</hints>
						</object-ids>
					</instance>""");

	private AgentApplicationService _service;

	@Override
	protected void tearDown() throws Exception {
		if (_service != null) {
			_service.shutDown();
			_service = null;
		}
		super.tearDown();
	}

	/** Every section reaches the running service, the contributors instantiated once. */
	public void testTheConfigurationApplies() throws Exception {
		start(APPLICATION);

		List<SystemPromptContributor> contributors = _service.getPromptContributors();
		assertEquals(1, contributors.size());
		assertEquals("House rule.", ((TextPromptContributor) contributors.get(0)).text(Locale.ENGLISH));
		assertSame(contributors, AgentApplicationService.promptContributors());
		assertNotNull(_service.getApplicationName());
		assertNull(_service.getEnvironmentTemplate());
		assertEquals("ORD-[0-9]+", _service.getObjectIds().getHints().get(0).getPattern());
	}

	/** Nothing configured: nothing added to any prompt. */
	public void testDefaults() throws Exception {
		start(ServiceConfigFixture.file(AgentApplicationService.class.getName(),
			"<instance class=\"com.top_logic.tl.ai.core.agent.AgentApplicationService\"/>"));

		assertEquals(List.of(), _service.getPromptContributors());
		assertNull(_service.getApplicationName());
	}

	/** While the service is down, no contributor is asked. */
	public void testWithoutTheService() {
		assertFalse(AgentApplicationService.isRunning());
		assertEquals(List.of(), AgentApplicationService.promptContributors());
	}

	private void start(String file) throws ConfigurationException {
		AgentApplicationService service = new AgentApplicationService(
			new DefaultInstantiationContext(TestAgentApplicationService.class),
			ServiceConfigFixture.read(AgentApplicationService.class, file));
		service.startUp();
		_service = service;
	}
}
