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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.demo.test.agent.improver.SystemPromptImprover;
import com.top_logic.ai.demo.test.agent.improver.TestResult;
import com.top_logic.ai.demo.test.agent.judge.JudgeClient;
import com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult;
import com.top_logic.ai.demo.test.agent.oracle.OracleLoader;
import com.top_logic.ai.demo.test.agent.scorer.NeutralScorer;
import com.top_logic.ai.demo.test.agent.scorer.NeutralScorerResult;
import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.tools.help.ModelExplorerTool;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.model.ModelService;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

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
 */
public class ModelExplorerScoringTest extends TestCase {

	/** Static list to collect test results across all tests for final analysis */
	private static final List<TestResult> ALL_TEST_RESULTS = new ArrayList<>();

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
		
		System.out.println("\n==========================================");
		System.out.println("TEST 1: " + testName);
		System.out.println("==========================================");
		System.out.println("Prompt: " + prompt);
		System.out.println();
		
		// 1. Load model XML (single source of truth)
		String modelXml = OracleLoader.loadPizzaShopModelXml();
		System.out.println("Model loaded: " + modelXml.length() + " chars (XML)");
		
		// 2. Run the agent
		System.out.println("\nRunning ModelExplorerTool agent...");
		ModelExplorerTool agent = new ModelExplorerTool();
		String agentAnswer = agent.exploreModel(prompt);
		
		System.out.println("\n--- Agent Answer ---");
		System.out.println(agentAnswer);
		System.out.println();
		
		// 3. Score with AI judge (prompt-based - no manual required_facts)
		System.out.println("Scoring with AI judge (prompt-based mode)...");
		JudgeClient judge = new JudgeClient(AIService.getInstance());
		String judgePrompt = loadJudgeSystemPrompt();
		
		JudgeResult judgeResult = judge.scoreWithPrompt(prompt, modelXml, agentAnswer, judgePrompt);
		
		// 4. Print judge results
		printJudgeResult(judgeResult);
		
		// 5. Get final score from neutral scorer
		System.out.println("\nGetting final score from neutral scorer...");
		NeutralScorer scorer = new NeutralScorer(AIService.getInstance());
		String scorerPrompt = NeutralScorer.loadScorerSystemPrompt();
		NeutralScorerResult finalScore = scorer.score(prompt, agentAnswer, judgeResult, scorerPrompt);
		
		printFinalScore(finalScore);
		
		// 6. Collect results for later analysis
		collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
		
		// 6b. Save individual test assessment to file
		saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);
		
		// 7. Assertions (using final score only)
		assertTrue(
			"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
			finalScore.getFinalScore() >= minScore
		);
		
		System.out.println("\n✓ Test passed!");
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
		
		System.out.println("\n==========================================");
		System.out.println("TEST 2: " + testName);
		System.out.println("==========================================");
		System.out.println("Prompt: " + prompt);
		System.out.println();
		
		// 1. Load model XML (single source of truth)
		String modelXml = OracleLoader.loadPizzaShopModelXml();
		System.out.println("Model loaded: " + modelXml.length() + " chars (XML)");
		
		// 2. Run the agent
		System.out.println("\nRunning ModelExplorerTool agent...");
		ModelExplorerTool agent = new ModelExplorerTool();
		String agentAnswer = agent.exploreModel(prompt);
		
		System.out.println("\n--- Agent Answer ---");
		System.out.println(agentAnswer);
		System.out.println();
		
		// 3. Score with AI judge (prompt-based - no manual required_facts)
		System.out.println("Scoring with AI judge (prompt-based mode)...");
		JudgeClient judge = new JudgeClient(AIService.getInstance());
		String judgePrompt = loadJudgeSystemPrompt();
		
		JudgeResult judgeResult = judge.scoreWithPrompt(prompt, modelXml, agentAnswer, judgePrompt);
		
		// 4. Print judge results
		printJudgeResult(judgeResult);
		
		// 5. Get final score from neutral scorer
		System.out.println("\nGetting final score from neutral scorer...");
		NeutralScorer scorer = new NeutralScorer(AIService.getInstance());
		String scorerPrompt = NeutralScorer.loadScorerSystemPrompt();
		NeutralScorerResult finalScore = scorer.score(prompt, agentAnswer, judgeResult, scorerPrompt);
		
		printFinalScore(finalScore);
		
		// 6. Collect results for later analysis
		collectTestResult(testName, prompt, agentAnswer, judgeResult, finalScore);
		
		// 6b. Save individual test assessment to file
		saveTestAssessment(testName, prompt, agentAnswer, judgeResult, finalScore);
		
		// 7. Assertions (using final score only)
		assertTrue(
			"Final score must be at least " + minScore + " (was: " + finalScore.getFinalScore() + ")",
			finalScore.getFinalScore() >= minScore
		);
		
		System.out.println("\n✓ Test passed!");
	}

	/**
	 * Prints the judge result in a readable format.
	 */
	private void printJudgeResult(JudgeResult result) {
		System.out.println("\n--- Judge Result ---");
		System.out.println("Score: " + result.getScore() + "/10");

		if (!result.getMissingFacts().isEmpty()) {
			System.out.println("\nMissing facts:");
			for (String fact : result.getMissingFacts()) {
				System.out.println("  - " + fact);
			}
		}

		if (!result.getIncorrectClaims().isEmpty()) {
			System.out.println("\nIncorrect claims:");
			for (String claim : result.getIncorrectClaims()) {
				System.out.println("  - " + claim);
			}
		}

		if (!result.getScopeViolations().isEmpty()) {
			System.out.println("\nScope violations:");
			for (String violation : result.getScopeViolations()) {
				System.out.println("  - " + violation);
			}
		}

		if (!result.getHallucinations().isEmpty()) {
			System.out.println("\nHallucinations:");
			for (String hallucination : result.getHallucinations()) {
				System.out.println("  - " + hallucination);
			}
		}

		if (!result.getNotFetched().isEmpty()) {
			System.out.println("\nNot fetched (claimed without evidence):");
			for (String notFetched : result.getNotFetched()) {
				System.out.println("  - " + notFetched);
			}
		}

		if (!result.getFormatViolations().isEmpty()) {
			System.out.println("\nFormat violations:");
			for (String violation : result.getFormatViolations()) {
				System.out.println("  - " + violation);
			}
		}
	}

	/**
	 * Prints the final score from neutral scorer.
	 */
	private void printFinalScore(NeutralScorerResult result) {
		System.out.println("\n--- Final Score ---");
		System.out.println("Score: " + result.getFinalScore() + "/10");
		System.out.println("Explanation: " + result.getExplanation());

		if (!result.getKeyIssues().isEmpty()) {
			System.out.println("\nKey Issues:");
			for (String issue : result.getKeyIssues()) {
				System.out.println("  - " + issue);
			}
		}

		if (!result.getStrengths().isEmpty()) {
			System.out.println("\nStrengths:");
			for (String strength : result.getStrengths()) {
				System.out.println("  - " + strength);
			}
		}
	}

	/**
	 * Collects test result for final analysis.
	 */
	private void collectTestResult(String testName, String prompt, String agentAnswer, JudgeResult judgeResult,
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
	 * Saves the assessment results for a single test to a file.
	 */
	private void saveTestAssessment(String testName, String prompt, String agentAnswer, JudgeResult judgeResult,
		NeutralScorerResult finalScore) throws IOException {
		try {
			// Create a safe filename from test name
			String safeFileName = testName.replaceAll("[^a-zA-Z0-9\\-_]", "_").toLowerCase();
			java.nio.file.Path outputPath = Paths.get(
				"src/test/resources/com/top_logic/ai/demo/test-assessments/" + safeFileName + ".txt"
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

			content.append("\n").append("=".repeat(80)).append("\n");

			// Write to file
			Files.write(outputPath, content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));

			System.out.println("✓ Test assessment saved to: " + outputPath.toAbsolutePath());
		} catch (Exception ex) {
			System.err.println("Warning: Failed to save test assessment to file: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Loads the judge system prompt from file.
	 */
	private String loadJudgeSystemPrompt() {
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
			System.out.println("\nNo test results collected - skipping system prompt improvement.");
			return;
		}

		System.out.println("\n==========================================");
		System.out.println("SYSTEM PROMPT IMPROVEMENT ANALYSIS");
		System.out.println("==========================================");
		System.out.println("Analyzing " + ALL_TEST_RESULTS.size() + " test results...\n");

		// Load current system prompt from ModelExplorer
		String currentSystemPrompt = loadModelExplorerSystemPrompt();

		// Run the improver
		SystemPromptImprover improver = new SystemPromptImprover(AIService.getInstance());
		String improverPrompt = SystemPromptImprover.loadImproverSystemPrompt();

		String suggestions = improver.improveSystemPrompt(currentSystemPrompt, ALL_TEST_RESULTS, improverPrompt);

		System.out.println("\n--- System Prompt Improvement Suggestions ---");
		System.out.println(suggestions);
		System.out.println("\n==========================================");

		// Save suggestions to file
		saveSuggestionsToFile(suggestions, ALL_TEST_RESULTS);

		// This test always passes - it's just for analysis
		assertTrue("System prompt improvement analysis completed", true);
	}

	/**
	 * Saves the improvement suggestions to a file for later review.
	 */
	private void saveSuggestionsToFile(String suggestions, List<TestResult> testResults) throws IOException {
		try {
			java.nio.file.Path outputPath = Paths.get(
				"src/test/resources/com/top_logic/ai/demo/system-prompt-improvement-suggestions.txt"
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
			content.append("\n\n").append("=".repeat(80)).append("\n");

			// Write to file
			Files.write(outputPath, content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));

			System.out.println("\n✓ Suggestions saved to: " + outputPath.toAbsolutePath());
		} catch (Exception ex) {
			System.err.println("Warning: Failed to save suggestions to file: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Loads the ModelExplorer system prompt using reflection to read the @SystemMessage annotation.
	 */
	private String loadModelExplorerSystemPrompt() {
		try {
			// Use reflection to get the @SystemMessage annotation from the ModelExplorer interface
			Class<?> modelExplorerClass = com.top_logic.ai.service.agents.ModelExplorer.class;
			java.lang.reflect.Method exploreMethod = modelExplorerClass.getMethod("explore", String.class);
			
			// Get the SystemMessage annotation
			dev.langchain4j.service.SystemMessage systemMessageAnnotation = 
				exploreMethod.getAnnotation(dev.langchain4j.service.SystemMessage.class);
			
			if (systemMessageAnnotation == null) {
				throw new RuntimeException("Could not find @SystemMessage annotation on ModelExplorer.explore()");
			}
			
			// Get the value from the annotation (returns String[])
			String[] values = systemMessageAnnotation.value();
			if (values == null || values.length == 0) {
				throw new RuntimeException("@SystemMessage annotation has no value");
			}
			
			// Join multiple values if present (though typically there's only one)
			return String.join("", values);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load ModelExplorer system prompt: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Creates the test suite with required service dependencies.
	 * 
	 * Tests are explicitly ordered to ensure testZZZ_SystemPromptImprovement runs last.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(ModelExplorerScoringTest.class.getName());
		
		// Add tests in explicit order to ensure testZZZ runs last
		// Using TestSuite.addTestSuite with a custom suite that only contains specific methods
		try {
			suite.addTest(createTest("testPrompt1_HighLevelOverview"));
			suite.addTest(createTest("testPrompt2_RootAggregateTypes"));
			suite.addTest(createTest("testZZZ_SystemPromptImprovement"));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create ordered test suite", ex);
		}
		
		// Wrap with service setup
		BasicRuntimeModule<?>[] modules = getModules();
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(suite, modules));
	}
	
	/**
	 * Creates a TestCase for a specific test method.
	 */
	private static TestCase createTest(String methodName) {
		return new ModelExplorerScoringTest(methodName);
	}
	
	/**
	 * Constructor to create a test case for a specific method.
	 */
	public ModelExplorerScoringTest(String testName) {
		super(testName);
	}
	
	/**
	 * Default constructor.
	 */
	public ModelExplorerScoringTest() {
		super();
	}

	protected static Test suite(Class<? extends Test> test, BasicRuntimeModule<?>... additionalModules) {
		BasicRuntimeModule<?>[] modules = getModules(additionalModules);

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(test),
				modules));
	}

	private static BasicRuntimeModule<?>[] getModules(BasicRuntimeModule<?>... additionalModules) {
		ArrayList<BasicRuntimeModule<?>> modules = new ArrayList<>();

		modules.add(ModelService.Module.INSTANCE);
		modules.add(LabelProviderService.Module.INSTANCE);
		modules.add(AIService.Module.INSTANCE);

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
