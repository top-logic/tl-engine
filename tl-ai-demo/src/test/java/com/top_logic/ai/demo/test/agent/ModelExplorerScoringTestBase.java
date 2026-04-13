/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.ai.demo.test.agent.improver.SystemPromptImprover;
import com.top_logic.ai.demo.test.agent.improver.TestResult;
import com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult;
import com.top_logic.ai.demo.test.agent.scorer.NeutralScorer;
import com.top_logic.ai.demo.test.agent.scorer.NeutralScorerResult;
import com.top_logic.ai.service.agents.AgentRegistry;
import com.top_logic.ai.service.scripting.AIScriptFunctions;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Base class for Model Explorer scoring tests containing shared helper methods.
 * 
 * <p>
 * This class provides common functionality for test result collection, scoring, and file output
 * that is shared between {@link ModelExplorerScoringTestPart1} and
 * {@link ModelExplorerScoringTestPart2}.
 * </p>
 * 
 * @author jhu
 */
public abstract class ModelExplorerScoringTestBase extends TestCase {

	/** Static list to collect test results across all tests for final analysis */
	protected static final List<TestResult> ALL_TEST_RESULTS = new ArrayList<>();

	/** Whether to use the neutral scorer (second judge). Set to false to skip and use judge score directly. */
	protected static final boolean USE_NEUTRAL_SCORER = false;

	/**
	 * Constructor to create a test case for a specific method.
	 */
	public ModelExplorerScoringTestBase(String testName) {
		super(testName);
	}

	/**
	 * Default constructor.
	 */
	public ModelExplorerScoringTestBase() {
		super();
	}

	/**
	 * Gets the final score, either from neutral scorer or by using judge score directly.
	 * 
	 * @param prompt
	 *        The user prompt
	 * @param agentAnswer
	 *        The agent's answer
	 * @param judgeResult
	 *        The judge result
	 * @return The final score result
	 */
	protected NeutralScorerResult getFinalScore(String prompt, String agentAnswer, JudgeResult judgeResult) {
		if (USE_NEUTRAL_SCORER) {
			NeutralScorer scorer = new NeutralScorer();
			String scorerPrompt = NeutralScorer.loadScorerSystemPrompt();
			return scorer.score(prompt, agentAnswer, judgeResult, scorerPrompt);
		} else {
			NeutralScorerResult result = new NeutralScorerResult();
			result.setFinalScore(judgeResult.getScore());
			result.setExplanation("Neutral scorer disabled - using judge score directly");
			return result;
		}
	}

	/**
	 * Collects test result for final analysis.
	 */
	protected void collectTestResult(String testName, String prompt, String agentAnswer, JudgeResult judgeResult,
		NeutralScorerResult finalScore) {
		try {
			// Convert judge result to JSON
			StringW buffer1 = new StringW();
			try (JsonWriter json1 = new JsonWriter(buffer1)) {
				judgeResult.writeTo(json1);
			}
			String judgeJson = buffer1.toString();

			// Convert final score to JSON
			StringW buffer2 = new StringW();
			try (JsonWriter json2 = new JsonWriter(buffer2)) {
				json2.beginObject();
				json2.name("finalScore").value(finalScore.getFinalScore());
				json2.name("explanation").value(finalScore.getExplanation());
				json2.name("keyIssues");
				json2.beginArray();
				for (String issue : finalScore.getKeyIssues()) {
					json2.value(issue);
				}
				json2.endArray();
				json2.name("strengths");
				json2.beginArray();
				for (String strength : finalScore.getStrengths()) {
					json2.value(strength);
				}
				json2.endArray();
				json2.endObject();
			}
			String finalScoreJson = buffer2.toString();

			TestResult testResult = new TestResult(
				testName,
				prompt,
				agentAnswer,
				judgeJson,
				finalScoreJson);

			ALL_TEST_RESULTS.add(testResult);
		} catch (Exception ex) {
			System.err.println("Warning: Failed to collect test result: " + ex.getMessage());
		}
	}

	/**
	 * Saves the assessment results for a single test to a file (appends to single file).
	 */
	protected void saveTestAssessment(String testName, String prompt, String agentAnswer, JudgeResult judgeResult,
		NeutralScorerResult finalScore) throws IOException {
		try {
			java.nio.file.Path outputPath = Paths.get(
				"src/test/resources/com/top_logic/ai/demo/test-assessments/all-test-assessments.txt"
			);

			// Create parent directories if they don't exist
			Files.createDirectories(outputPath.getParent());

			// Build the content
			StringBuilder content = new StringBuilder();
			content.append("=".repeat(80)).append("\n");
			content.append("TEST ASSESSMENT: ").append(testName).append("\n");
			content.append("=".repeat(80)).append("\n\n");
			content.append("Generated: ").append(LocalDateTime.now()).append("\n\n");

			// Test information
			content.append("Test Information:\n");
			content.append("-".repeat(80)).append("\n");
			content.append("Test Name: ").append(testName).append("\n");
			content.append("Prompt: ").append(prompt).append("\n\n");

			// Agent Answer
			content.append("=".repeat(80)).append("\n");
			content.append("AGENT ANSWER\n");
			content.append("=".repeat(80)).append("\n\n");
			content.append(agentAnswer).append("\n\n");

			// Judge Result
			content.append("=".repeat(80)).append("\n");
			content.append("JUDGE RESULT\n");
			content.append("=".repeat(80)).append("\n\n");
			content.append("Score: ").append(judgeResult.getScore()).append("/10\n\n");

			if (!judgeResult.getMissingFacts().isEmpty()) {
				content.append("Missing facts:\n");
				for (String fact : judgeResult.getMissingFacts()) {
					content.append("  - ").append(fact).append("\n");
				}
				content.append("\n");
			}

			if (!judgeResult.getIncorrectClaims().isEmpty()) {
				content.append("Incorrect claims:\n");
				for (String claim : judgeResult.getIncorrectClaims()) {
					content.append("  - ").append(claim).append("\n");
				}
				content.append("\n");
			}

			if (!judgeResult.getScopeViolations().isEmpty()) {
				content.append("Scope violations:\n");
				for (String violation : judgeResult.getScopeViolations()) {
					content.append("  - ").append(violation).append("\n");
				}
				content.append("\n");
			}

			if (!judgeResult.getHallucinations().isEmpty()) {
				content.append("Hallucinations:\n");
				for (String hallucination : judgeResult.getHallucinations()) {
					content.append("  - ").append(hallucination).append("\n");
				}
				content.append("\n");
			}

			if (!judgeResult.getNotFetched().isEmpty()) {
				content.append("Not fetched (claimed without evidence):\n");
				for (String notFetched : judgeResult.getNotFetched()) {
					content.append("  - ").append(notFetched).append("\n");
				}
				content.append("\n");
			}

			if (!judgeResult.getFormatViolations().isEmpty()) {
				content.append("Format violations:\n");
				for (String violation : judgeResult.getFormatViolations()) {
					content.append("  - ").append(violation).append("\n");
				}
				content.append("\n");
			}

			// Final Score
			content.append("=".repeat(80)).append("\n");
			content.append("FINAL SCORE\n");
			content.append("=".repeat(80)).append("\n\n");
			content.append("Score: ").append(finalScore.getFinalScore()).append("/10\n");
			content.append("Explanation: ").append(finalScore.getExplanation()).append("\n\n");

			if (!finalScore.getKeyIssues().isEmpty()) {
				content.append("Key Issues:\n");
				for (String issue : finalScore.getKeyIssues()) {
					content.append("  - ").append(issue).append("\n");
				}
				content.append("\n");
			}

			if (!finalScore.getStrengths().isEmpty()) {
				content.append("Strengths:\n");
				for (String strength : finalScore.getStrengths()) {
					content.append("  - ").append(strength).append("\n");
				}
				content.append("\n");
			}

			content.append("\n").append("=".repeat(80)).append("\n\n");

			// Append to file
			Files.write(outputPath, content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),
				java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception ex) {
			System.err.println("Warning: Failed to save test assessment to file: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Loads the judge system prompt from file.
	 */
	protected String loadJudgeSystemPrompt() {
		try {
			return new String(Files.readAllBytes(
				Paths.get("src/test/resources/com/top_logic/ai/demo/judge-system-prompt.txt")
			));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load judge system prompt", ex);
		}
	}

	/**
	 * Final test that analyzes all results and provides improvement suggestions.
	 * 
	 * This should run last after all other tests.
	 */
	public void testZZZ_SystemPromptImprovement() throws IOException {
		if (ALL_TEST_RESULTS.isEmpty()) {
			return;
		}

		// Load current system prompt from ModelExplorer
		String currentSystemPrompt = loadModelExplorerSystemPrompt();

		// Run the improver
		SystemPromptImprover improver = new SystemPromptImprover();
		String improverPrompt = SystemPromptImprover.loadImproverSystemPrompt();

		String suggestions = improver.improveSystemPrompt(currentSystemPrompt, ALL_TEST_RESULTS, improverPrompt);

		// Save suggestions to file
		saveSuggestionsToFile(suggestions, ALL_TEST_RESULTS);

		// This test always passes - it's just for analysis
		assertTrue("System prompt improvement analysis completed", true);
	}

	/**
	 * Saves the improvement suggestions to a file for later review.
	 */
	protected void saveSuggestionsToFile(String suggestions, List<TestResult> testResults) throws IOException {
		try {
			java.nio.file.Path outputPath = Paths.get(
				"src/test/resources/com/top_logic/ai/demo/test-assessments/system-prompt-improvement-suggestions.txt"
			);

			// Create parent directories if they don't exist
			Files.createDirectories(outputPath.getParent());

			// Build the content with context
			StringBuilder content = new StringBuilder();
			content.append("=".repeat(80)).append("\n");
			content.append("SYSTEM PROMPT IMPROVEMENT SUGGESTIONS\n");
			content.append("=".repeat(80)).append("\n\n");
			content.append("Generated: ").append(LocalDateTime.now()).append("\n");
			content.append("Number of tests analyzed: ").append(testResults.size()).append("\n\n");

			// List all tests that were analyzed
			content.append("Tests Analyzed:\n");
			content.append("-".repeat(80)).append("\n");
			for (int i = 0; i < testResults.size(); i++) {
				TestResult result = testResults.get(i);
				content.append(String.format("%d. %s\n", i + 1, result.getTestName()));
				content.append("   Prompt: ").append(result.getUserPrompt()).append("\n\n");
			}

			content.append("\n").append("=".repeat(80)).append("\n");
			content.append("IMPROVEMENT SUGGESTIONS\n");
			content.append("=".repeat(80)).append("\n\n");
			content.append(suggestions);
			content.append("\n\n").append("=".repeat(80)).append("\n\n");

			// Append to file (don't overwrite existing content)
			Files.write(outputPath, content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),
				java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception ex) {
			System.err.println("Warning: Failed to save suggestions to file: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Runs the ModelExplorer agent with the given prompt and returns the result.
	 *
	 * @param prompt
	 *        The user prompt to send to the agent.
	 * @return The agent's answer as a string.
	 */
	protected String runModelExplorer(String prompt) {
		return ThreadContextManager.inSystemInteraction(
			ModelExplorerScoringTestBase.class,
			() -> AIScriptFunctions.agentChat("ModelExplorer", prompt));
	}

	/**
	 * Loads the ModelExplorer system prompt from {@link AgentRegistry} configuration.
	 */
	protected String loadModelExplorerSystemPrompt() {
		try {
			AgentRegistry registry = AgentRegistry.getInstance();
			if (registry == null) {
				throw new RuntimeException("AgentRegistry is not available");
			}
			String systemPrompt = registry.getAgentConfig("ModelExplorer").getSystemPrompt();
			if (systemPrompt == null || systemPrompt.isEmpty()) {
				throw new RuntimeException("ModelExplorer system prompt is empty");
			}
			return systemPrompt;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load ModelExplorer system prompt: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Gets the modules required for the test suite.
	 */
	protected static BasicRuntimeModule<?>[] getModules(BasicRuntimeModule<?>... additionalModules) {
		ArrayList<BasicRuntimeModule<?>> modules = new ArrayList<>();

		modules.add(com.top_logic.util.model.ModelService.Module.INSTANCE);
		modules.add(com.top_logic.layout.provider.LabelProviderService.Module.INSTANCE);
		modules.add(AgentRegistry.Module.INSTANCE);

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
