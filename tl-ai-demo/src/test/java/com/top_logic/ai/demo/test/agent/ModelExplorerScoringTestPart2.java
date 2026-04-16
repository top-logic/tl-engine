/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.demo.test.agent.judge.JudgeClient;
import com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult;
import com.top_logic.ai.demo.test.agent.oracle.OracleLoader;
import com.top_logic.ai.demo.test.agent.scorer.NeutralScorerResult;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;

/**
 * Simple scoring tests for the Model Explorer Agent (Part 2: Tests 6-10).
 *
 * <p>
 * Each test:
 * <ol>
 * <li>Defines a prompt</li>
 * <li>Explicitly calls tools to build required facts</li>
 * <li>Runs the agent</li>
 * <li>Loads the oracle</li>
 * <li>Scores with AI judge</li>
 * <li>Gets final score from neutral scorer</li>
 * <li>Asserts on the result</li>
 * </ol>
 * </p>
 *
 * <p>
 * After all tests, a system prompt improver analyzes all results and provides suggestions.
 * </p>
 */
public class ModelExplorerScoringTestPart2 extends ModelExplorerScoringTestBase {

	/**
	 * Test 6: Explain how cost attribute on ProductComponent is defined.
	 *
	 * Prompt: "explain how the cost attribute on ProductComponent is defined, using only model or script references"
	 *
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt6_CostAttributeDefinition() throws IOException {
		String testName = "Cost Attribute Definition (Prompt-Based)";
		String prompt = "explain how the cost attribute on ProductComponent is defined, using only model or script references";
		int minScore = 7;

		String modelXml = OracleLoader.loadPizzaShopModelXml();

		ConversationLog log = new ConversationLog();
		String agentAnswer = runModelExplorer(prompt, log);

		List<Part.ToolCallPart> toolCalls = extractToolCalls(log);
		Map<String, Object> toolResults = ToolResultMinimizer.extractAndMinimizeToolResults(toolCalls);

		JudgeClient judge = new JudgeClient();
		String judgePrompt = loadJudgeSystemPrompt();

		JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
			prompt, modelXml, agentAnswer, toolCalls, toolResults, judgePrompt);

		NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
		collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
		saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);

		assertTrue(
			"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
			finalScore.getFinalScore() >= minScore
		);
	}

	/**
	 * Test 7: List all calculated/derived attributes and how they are computed.
	 *
	 * Prompt: "list all calculated / derived attributes in the model and how they are computed"
	 *
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt7_CalculatedAttributes() throws IOException {
		String testName = "Calculated/Derived Attributes (Prompt-Based)";
		String prompt = "list all calculated / derived attributes in the model and how they are computed";
		int minScore = 7;

		String modelXml = OracleLoader.loadPizzaShopModelXml();

		ConversationLog log = new ConversationLog();
		String agentAnswer = runModelExplorer(prompt, log);

		List<Part.ToolCallPart> toolCalls = extractToolCalls(log);
		Map<String, Object> toolResults = ToolResultMinimizer.extractAndMinimizeToolResults(toolCalls);

		JudgeClient judge = new JudgeClient();
		String judgePrompt = loadJudgeSystemPrompt();

		JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
			prompt, modelXml, agentAnswer, toolCalls, toolResults, judgePrompt);

		NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
		collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
		saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);

		assertTrue(
			"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
			finalScore.getFinalScore() >= minScore
		);
	}

	/**
	 * Test 8: Find where Component is used across the model.
	 *
	 * Prompt: "where is Component used across the model (attributes, relations, scripts)"
	 *
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt8_ComponentUsage() throws IOException {
		String testName = "Component Usage (Prompt-Based)";
		String prompt = "where is Component used across the model (attributes, relations, scripts)";
		int minScore = 7;

		String modelXml = OracleLoader.loadPizzaShopModelXml();

		ConversationLog log = new ConversationLog();
		String agentAnswer = runModelExplorer(prompt, log);

		List<Part.ToolCallPart> toolCalls = extractToolCalls(log);
		Map<String, Object> toolResults = ToolResultMinimizer.extractAndMinimizeToolResults(toolCalls);

		JudgeClient judge = new JudgeClient();
		String judgePrompt = loadJudgeSystemPrompt();

		JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
			prompt, modelXml, agentAnswer, toolCalls, toolResults, judgePrompt);

		NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
		collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
		saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);

		assertTrue(
			"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
			finalScore.getFinalScore() >= minScore
		);
	}

	/**
	 * Test 9: Find all types that reference Unit and explain each reference.
	 *
	 * Prompt: "find all types that reference Unit and explain the nature of each reference"
	 *
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt9_UnitReferences() throws IOException {
		String testName = "Unit References (Prompt-Based)";
		String prompt = "find all types that reference Unit and explain the nature of each reference";
		int minScore = 7;

		String modelXml = OracleLoader.loadPizzaShopModelXml();

		ConversationLog log = new ConversationLog();
		String agentAnswer = runModelExplorer(prompt, log);

		List<Part.ToolCallPart> toolCalls = extractToolCalls(log);
		Map<String, Object> toolResults = ToolResultMinimizer.extractAndMinimizeToolResults(toolCalls);

		JudgeClient judge = new JudgeClient();
		String judgePrompt = loadJudgeSystemPrompt();

		JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
			prompt, modelXml, agentAnswer, toolCalls, toolResults, judgePrompt);

		NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
		collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
		saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);

		assertTrue(
			"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
			finalScore.getFinalScore() >= minScore
		);
	}

	/**
	 * Test 10: Search for anything related to pricing or cost and summarize by type.
	 *
	 * Prompt: "search for anything related to pricing or cost and summarize the findings by type"
	 *
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt10_PricingCostSearch() throws IOException {
		String testName = "Pricing/Cost Search (Prompt-Based)";
		String prompt = "search for anything related to pricing or cost and summarize the findings by type";
		int minScore = 7;

		String modelXml = OracleLoader.loadPizzaShopModelXml();

		ConversationLog log = new ConversationLog();
		String agentAnswer = runModelExplorer(prompt, log);

		List<Part.ToolCallPart> toolCalls = extractToolCalls(log);
		Map<String, Object> toolResults = ToolResultMinimizer.extractAndMinimizeToolResults(toolCalls);

		JudgeClient judge = new JudgeClient();
		String judgePrompt = loadJudgeSystemPrompt();

		JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
			prompt, modelXml, agentAnswer, toolCalls, toolResults, judgePrompt);

		NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
		collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
		saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);

		assertTrue(
			"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
			finalScore.getFinalScore() >= minScore
		);
	}

	/**
	 * Extracts all {@link com.top_logic.tl.ai.core.session.Part.ToolCallPart}s from the
	 * conversation log.
	 */
	private static List<Part.ToolCallPart> extractToolCalls(ConversationLog log) {
		List<Part.ToolCallPart> toolCalls = new ArrayList<>();
		for (Turn turn : log.turns()) {
			for (Part part : turn.assistantParts()) {
				if (part instanceof Part.ToolCallPart tc) {
					toolCalls.add(tc);
				}
			}
		}
		return toolCalls;
	}

	/**
	 * Creates the test suite with required service dependencies.
	 *
	 * Tests are explicitly ordered to ensure testZZZ_SystemPromptImprovement runs last.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(ModelExplorerScoringTestPart2.class.getName());

		try {
			suite.addTest(createTest("testPrompt6_CostAttributeDefinition"));
			suite.addTest(createTest("testPrompt7_CalculatedAttributes"));
			suite.addTest(createTest("testPrompt8_ComponentUsage"));
			suite.addTest(createTest("testPrompt9_UnitReferences"));
			suite.addTest(createTest("testPrompt10_PricingCostSearch"));
			suite.addTest(createTest("testZZZ_SystemPromptImprovement"));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create ordered test suite", ex);
		}

		// Wrap with service setup
		BasicRuntimeModule<?>[] modules = ModelExplorerScoringTestBase.getModules();
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(suite, modules));
	}

	/**
	 * Creates a TestCase for a specific test method.
	 */
	private static TestCase createTest(String methodName) {
		return new ModelExplorerScoringTestPart2(methodName);
	}

	/**
	 * Constructor to create a test case for a specific method.
	 */
	public ModelExplorerScoringTestPart2(String testName) {
		super(testName);
	}

	/** */
	public ModelExplorerScoringTestPart2() {
		super();
	}

	protected static Test suite(Class<? extends Test> test, BasicRuntimeModule<?>... additionalModules) {
		BasicRuntimeModule<?>[] modules = ModelExplorerScoringTestBase.getModules(additionalModules);

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(test),
				modules));
	}
}
