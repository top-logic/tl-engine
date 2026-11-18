/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService.AgentOverride;
import com.top_logic.tl.ai.core.provider.vertex.VertexProviderConfig;

/**
 * Tests {@link ModelToolChoiceConstraint}: forcing a tool call on a model whose provider accepts
 * only some tool choices is warned about in the agent's settings.
 */
public class TestModelToolChoiceConstraint extends TestCase {

	private static final String VERTEX_MODEL = "claude-sonnet-5-5";

	private static final String OTHER_MODEL = "claude-opus-5-5";

	private LLMProviderService _service;

	/**
	 * Instantiating a provider configuration resolves its implementation class through the
	 * {@code TypeIndex}, which needs the module system.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestModelToolChoiceConstraint.class));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		@SuppressWarnings("unchecked")
		LLMProviderService.Config<LLMProviderService> config =
			(LLMProviderService.Config<LLMProviderService>) TypedConfiguration.newConfigItem(
				LLMProviderService.Config.class);
		LLMProviderConfig.Config vertex = provider(VertexProviderConfig.class,
			"name='vertex' project='p' location='eu' tool-choice='auto' models='" + VERTEX_MODEL + "'");
		LLMProviderConfig.Config vertexAll = provider(VertexProviderConfig.class,
			"name='vertex-all' project='p' location='eu' models='" + OTHER_MODEL + "'");
		config.getProviders().put(vertex.getName(), vertex);
		config.getProviders().put(vertexAll.getName(), vertexAll);
		_service = LLMProviderServiceFixture.start(new DefaultInstantiationContext(TestModelToolChoiceConstraint.class),
			config);
	}

	@Override
	protected void tearDown() throws Exception {
		LLMProviderServiceFixture.stop(_service);
		_service = null;
		super.tearDown();
	}

	/**
	 * A forced tool call on a provider that accepts only {@code auto} is warned about.
	 */
	public void testRequiredOnAutoOnlyProvider() throws Exception {
		List<ConstraintFailure> failures = check(override(VERTEX_MODEL, "REQUIRED"));

		assertEquals(1, failures.size());
		ConstraintFailure failure = failures.get(0);
		assertTrue("Must be a warning, not an error.", failure.isWarning());
		assertEquals(AgentOverride.TOOL_CHOICE, failure.getContextProperty().getPropertyName());
		assertEquals(I18NConstants.WARNING_TOOL_CHOICE_NOT_ACCEPTED__MODEL_CHOICES.fill(VERTEX_MODEL, "auto"),
			failure.getConstraintName());
	}

	/**
	 * The automatic tool choice is accepted, and so is a tool choice left to the provider.
	 */
	public void testAutoOnAutoOnlyProvider() throws Exception {
		assertEquals(List.of(), check(override(VERTEX_MODEL, "AUTO")));
		assertEquals(List.of(), check(override(VERTEX_MODEL, null)));
	}

	/**
	 * A provider that accepts every tool choice gives no warning.
	 */
	public void testRequiredOnUnrestrictedProvider() throws Exception {
		assertEquals(List.of(), check(override(OTHER_MODEL, "REQUIRED")));
	}

	private static List<ConstraintFailure> check(ConfigurationItem item) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(item);
		return checker.getFailures().stream()
			.filter(failure -> failure.getContextProperty().getPropertyName().equals(AgentOverride.TOOL_CHOICE))
			.toList();
	}

	private static AgentOverride override(String model, String toolChoice) {
		AgentOverride result = TypedConfiguration.newConfigItem(AgentOverride.class);
		result.setName("analyst");
		set(result, AgentOverride.MODEL, model);
		set(result, AgentOverride.TOOL_CHOICE, toolChoice);
		return result;
	}

	private static LLMProviderConfig.Config provider(Class<? extends LLMProviderConfig> type, String attributes)
			throws ConfigurationException {
		String xml = "<provider xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + type.getName() + "$Config'"
			+ " class='" + type.getName() + "' " + attributes + "/>";
		return (LLMProviderConfig.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}
}
