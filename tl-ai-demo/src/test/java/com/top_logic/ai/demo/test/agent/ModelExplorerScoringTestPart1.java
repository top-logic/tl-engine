/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent;

import java.io.IOException;
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
import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.tools.ToolCallTracker;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.basic.module.BasicRuntimeModule;

/**
 * Simple scoring tests for the Model Explorer Agent.
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
 * 
 * @author jhu
 */
public class ModelExplorerScoringTestPart1 extends ModelExplorerScoringTestBase {

	/**
	 * Test 1: High-level overview of types and relationships.
	 * 
	 * Prompt: "give me a high-level overview of the luigis.pizzashop model (types and relationships only)"
	 * 
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt1_HighLevelOverview() throws IOException {
		String testName = "High-Level Overview (Prompt-Based)";
		String prompt = "give me a high-level overview of the luigis.pizzashop model (types and relationships only)";
		int minScore = 7;
		
		// 1. Load model XML (single source of truth)
		String modelXml = OracleLoader.loadPizzaShopModelXml();
		
		// 2. Run the agent with tool tracking
		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);
		try {
			String agentAnswer = runModelExplorer(prompt);
			
			// 2b. Extract and minimize tool results
			Map<String, Object> toolResults = ToolResultMinimizer.minimizeToolResults(tracker.getToolResults());
			
			// 3. Score with AI judge (prompt-based with tool trace)
			JudgeClient judge = new JudgeClient(AIService.getInstance());
			String judgePrompt = loadJudgeSystemPrompt();
			
			JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
				prompt, modelXml, agentAnswer, tracker.getToolCalls(), toolResults, judgePrompt);
			
			// 4. Get final score from neutral scorer (or use judge score if disabled)
			NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
			
			// 5. Collect results for later analysis
			collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 6. Save test assessment to file
			saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 7. Assertions (using final score only)
			assertTrue(
				"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
				finalScore.getFinalScore() >= minScore
			);
		} finally {
			ToolCallTracking.setTracker(null);
		}
	}

	/**
	 * Test 2: List all root/aggregate types and their relationships.
	 * 
	 * Prompt: "list all root / aggregate types in the model and how they relate"
	 * 
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt2_RootAggregateTypes() throws IOException {
		String testName = "Root/Aggregate Types (Prompt-Based)";
		String prompt = "list all root / aggregate types in the model and how they relate";
		int minScore = 7;
		
		// 1. Load model XML (single source of truth)
		String modelXml = OracleLoader.loadPizzaShopModelXml();
		
		// 2. Run the agent with tool tracking
		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);
		try {
			String agentAnswer = runModelExplorer(prompt);
			
			// 2b. Extract and minimize tool results
			Map<String, Object> toolResults = ToolResultMinimizer.minimizeToolResults(tracker.getToolResults());
			
			// 3. Score with AI judge (prompt-based with tool trace)
			JudgeClient judge = new JudgeClient(AIService.getInstance());
			String judgePrompt = loadJudgeSystemPrompt();
			
			JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
				prompt, modelXml, agentAnswer, tracker.getToolCalls(), toolResults, judgePrompt);
			
			// 4. Get final score from neutral scorer (or use judge score if disabled)
			NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
			
			// 5. Collect results for later analysis
			collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 6. Save test assessment to file
			saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 7. Assertions (using final score only)
			assertTrue(
				"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
				finalScore.getFinalScore() >= minScore
			);
		} finally {
			ToolCallTracking.setTracker(null);
		}
	}

	/**
	 * Test 3: Describe ProductComponent type with attributes, types, and constraints.
	 * 
	 * Prompt: "describe the ProductComponent type: attributes, attribute types, and constraints"
	 * 
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt3_ProductComponentType() throws IOException {
		String testName = "ProductComponent Type Description (Prompt-Based)";
		String prompt = "describe the ProductComponent type: attributes, attribute types, and constraints";
		int minScore = 7;
		
		// 1. Load model XML (single source of truth)
		String modelXml = OracleLoader.loadPizzaShopModelXml();
		
		// 2. Run the agent with tool tracking
		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);
		try {
			String agentAnswer = runModelExplorer(prompt);
			
			// 2b. Extract and minimize tool results
			Map<String, Object> toolResults = ToolResultMinimizer.minimizeToolResults(tracker.getToolResults());
			
			// 3. Score with AI judge (prompt-based with tool trace)
			JudgeClient judge = new JudgeClient(AIService.getInstance());
			String judgePrompt = loadJudgeSystemPrompt();
			
			JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
				prompt, modelXml, agentAnswer, tracker.getToolCalls(), toolResults, judgePrompt);
			
			// 4. Get final score from neutral scorer (or use judge score if disabled)
			NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
			
			// 5. Collect results for later analysis
			collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 6. Save test assessment to file
			saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 7. Assertions (using final score only)
			assertTrue(
				"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
				finalScore.getFinalScore() >= minScore
			);
		} finally {
			ToolCallTracking.setTracker(null);
		}
	}

	/**
	 * Test 4: List all attributes on Component and their source (declared vs inherited).
	 * 
	 * Prompt: "what are all attributes defined on Component and where do they come from (declared vs inherited)"
	 * 
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt4_ComponentAttributes() throws IOException {
		String testName = "Component Attributes (Declared vs Inherited) (Prompt-Based)";
		String prompt = "what are all attributes defined on Component and where do they come from (declared vs inherited)";
		int minScore = 7;
		
		// 1. Load model XML (single source of truth)
		String modelXml = OracleLoader.loadPizzaShopModelXml();
		
		// 2. Run the agent with tool tracking
		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);
		try {
			String agentAnswer = runModelExplorer(prompt);
			
			// 2b. Extract and minimize tool results
			Map<String, Object> toolResults = ToolResultMinimizer.minimizeToolResults(tracker.getToolResults());
			
			// 3. Score with AI judge (prompt-based with tool trace)
			JudgeClient judge = new JudgeClient(AIService.getInstance());
			String judgePrompt = loadJudgeSystemPrompt();
			
			JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
				prompt, modelXml, agentAnswer, tracker.getToolCalls(), toolResults, judgePrompt);
			
			// 4. Get final score from neutral scorer (or use judge score if disabled)
			NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
			
			// 5. Collect results for later analysis
			collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 6. Save test assessment to file
			saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 7. Assertions (using final score only)
			assertTrue(
				"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
				finalScore.getFinalScore() >= minScore
			);
		} finally {
			ToolCallTracking.setTracker(null);
		}
	}

	/**
	 * Test 5: Show all enum types and which attributes use them.
	 * 
	 * Prompt: "show all enum types in the model and which attributes use them"
	 * 
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt5_EnumTypes() throws IOException {
		String testName = "Enum Types (Prompt-Based)";
		String prompt = "show all enum types in the model and which attributes use them";
		int minScore = 7;
		
		// 1. Load model XML (single source of truth)
		String modelXml = OracleLoader.loadPizzaShopModelXml();
		
		// 2. Run the agent with tool tracking
		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);
		try {
			String agentAnswer = runModelExplorer(prompt);
			
			// 2b. Extract and minimize tool results
			Map<String, Object> toolResults = ToolResultMinimizer.minimizeToolResults(tracker.getToolResults());
			
			// 3. Score with AI judge (prompt-based with tool trace)
			JudgeClient judge = new JudgeClient(AIService.getInstance());
			String judgePrompt = loadJudgeSystemPrompt();
			
			JudgeResult judgeResult = judge.scoreWithPromptAndToolTrace(
				prompt, modelXml, agentAnswer, tracker.getToolCalls(), toolResults, judgePrompt);
			
			// 4. Get final score from neutral scorer (or use judge score if disabled)
			NeutralScorerResult finalScore = getFinalScore(prompt, agentAnswer, judgeResult);
			
			// 5. Collect results for later analysis
			collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 6. Save test assessment to file
			saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);
			
			// 7. Assertions (using final score only)
			assertTrue(
				"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
				finalScore.getFinalScore() >= minScore
			);
		} finally {
			ToolCallTracking.setTracker(null);
		}
	}


	/**
	 * Creates the test suite with required service dependencies.
	 * 
	 * Tests are explicitly ordered to ensure testZZZ_SystemPromptImprovement runs last.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(ModelExplorerScoringTestPart1.class.getName());
		
		// Add tests in explicit order to ensure test ZZZ runs last
		// Using {@link TestSuite#addTestSuite(Class)} with a custom suite that only contains specific methods
		try {
			suite.addTest(createTest("testPrompt1_HighLevelOverview"));
			suite.addTest(createTest("testPrompt2_RootAggregateTypes"));
			suite.addTest(createTest("testPrompt3_ProductComponentType"));
			suite.addTest(createTest("testPrompt4_ComponentAttributes"));
			suite.addTest(createTest("testPrompt5_EnumTypes"));
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
		return new ModelExplorerScoringTestPart1(methodName);
	}
	
	/**
	 * Constructor to create a test case for a specific method.
	 */
	public ModelExplorerScoringTestPart1(String testName) {
		super(testName);
	}
	
	/**
	 * Default constructor.
	 */
	public ModelExplorerScoringTestPart1() {
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
