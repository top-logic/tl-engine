/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.provider.LLMProviderService;

/**
 * That the arms of the matrix differ in their model and in nothing else.
 *
 * <p>
 * The whole comparison rests on this. One agent definition per model is what gets past the shipped
 * {@code model-access default="assigned"} policy, but it also means the system prompt and the tool
 * list exist once per arm - and an arm answering a different prompt is not a measurement of its
 * model, it is a measurement of two things at once. Nothing in the configuration system enforces
 * that the copies stay equal, so this test does.
 * </p>
 *
 * <p>
 * It is not a style check. The day somebody improves the prompt of one arm to see whether a model
 * gets better, every number in the last report becomes uncomparable, and the failure of this test is
 * the only thing that will say so. If two prompts are meant to differ, that is a second experiment:
 * give it its own arm names and say in the scenario file what is being varied.
 * </p>
 */
public class TestEvalArmsAreComparable extends BasicTestCase {

	/** Every agent an arm names exists, and is assigned the model the arm claims. */
	public void testEachArmsAgentIsAssignedTheArmsModel() throws Exception {
		for (EvalConfig.Arm arm : TestEvalConfig.pmScenarios().getArms().values()) {
			AgentDefinition agent = agent(arm);
			assertNotNull("Arm '" + arm.getName() + "' names the agent '" + arm.getAgent()
				+ "', which is not configured.", agent);
			assertEquals("Arm '" + arm.getName() + "' says it runs on '" + arm.getModel()
				+ "', but its agent is assigned another model. The CSV would then name a model that"
				+ " never answered.",
				arm.getModel(),
				AgentRuntimeService.getInstance().resolveModelAssignment(arm.getAgent()).model());
		}
	}

	/** Every arm answers the same system prompt, so a difference between arms is the model. */
	public void testAllArmsShareOneSystemPrompt() throws Exception {
		List<EvalConfig.Arm> arms = List.copyOf(TestEvalConfig.pmScenarios().getArms().values());
		EvalConfig.Arm first = arms.get(0);
		String prompt = agent(first).systemPrompt();

		assertTrue("The arms' system prompt must say what the fixture is; it is empty.",
			prompt != null && !prompt.isBlank());
		for (EvalConfig.Arm arm : arms) {
			assertEquals("Arm '" + arm.getName() + "' answers a different system prompt from arm '"
				+ first.getName() + "'. Every number comparing them is then about two variables at"
				+ " once. Copy the prompt of one into the other, or say in the scenario file that"
				+ " the prompt is what this eval varies.",
				prompt, agent(arm).systemPrompt());
		}
	}

	/** And the same tools, for the same reason - and so a forbidden tool means one thing. */
	public void testAllArmsCarryTheSameTools() throws Exception {
		List<EvalConfig.Arm> arms = List.copyOf(TestEvalConfig.pmScenarios().getArms().values());
		EvalConfig.Arm first = arms.get(0);
		List<String> tools = toolsOf(first);

		assertFalse("An arm with no tools cannot do any scenario of this fixture.", tools.isEmpty());
		for (EvalConfig.Arm arm : arms) {
			assertEquals("Arm '" + arm.getName() + "' carries other tools than arm '"
				+ first.getName() + "'. A scenario forbidding a tool would then forbid it on one"
				+ " arm and be unable to on the other.", tools, toolsOf(arm));
		}
	}

	private static List<String> toolsOf(EvalConfig.Arm arm) {
		return List.copyOf(agent(arm).tools());
	}

	private static AgentDefinition agent(EvalConfig.Arm arm) {
		return AgentDefinitionQuery.getInstance().findAgentByName(arm.getAgent());
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(
			new TestSuite(TestEvalArmsAreComparable.class),
			TopLogicAgentDefinitions.Module.INSTANCE,
			LLMProviderService.Module.INSTANCE,
			AgentRuntimeService.Module.INSTANCE,
			AgentApplicationService.Module.INSTANCE,
			AgentPolicyService.Module.INSTANCE));
	}
}
