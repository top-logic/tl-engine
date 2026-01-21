/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.demo.test.agent.judge.JudgeClient;
import com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult;
import com.top_logic.ai.demo.test.agent.oracle.OracleLoader;
import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.tools.help.ModelExplorerTool;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.model.ModelService;

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
 * <li>Asserts on the result</li>
 * </ol>
 * </p>
 */
public class ModelExplorerScoringTest extends TestCase {

	/**
	 * Test 1: High-level overview of types and relationships.
	 * 
	 * Prompt: "give me a high-level overview of the luigis.pizzashop model (types and relationships only)"
	 * 
	 * Uses prompt-based judging where the LLM infers expected facts from the prompt.
	 */
	public void testPrompt1_HighLevelOverview() throws IOException {
		String prompt = "give me a high-level overview of the luigis.pizzashop model (types and relationships only)";
		int minScore = 7;
		
		System.out.println("\n==========================================");
		System.out.println("TEST 1: High-Level Overview (Prompt-Based)");
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
		
		JudgeResult result = judge.scoreWithPrompt(prompt, modelXml, agentAnswer, judgePrompt);
		
		// 4. Print results
		printJudgeResult(result);
		
		// 5. Assertions
		assertTrue(
			"Score must be at least " + minScore + " (was: " + result.getScore() + ")",
			result.getScore() >= minScore
		);
		
		assertTrue(
			"No incorrect claims allowed. Found: " + result.getIncorrectClaims(),
			result.getIncorrectClaims().isEmpty()
		);
		
		assertTrue(
			"No scope violations allowed. Found: " + result.getScopeViolations(),
			result.getScopeViolations().isEmpty()
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
		String prompt = "list all root / aggregate types in the model and how they relate";
		int minScore = 7;
		
		System.out.println("\n==========================================");
		System.out.println("TEST 2: Root/Aggregate Types (Prompt-Based)");
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
		
		JudgeResult result = judge.scoreWithPrompt(prompt, modelXml, agentAnswer, judgePrompt);
		
		// 4. Print results
		printJudgeResult(result);
		
		// 5. Assertions
		assertTrue(
			"Score must be at least " + minScore + " (was: " + result.getScore() + ")",
			result.getScore() >= minScore
		);
		
		assertTrue(
			"No incorrect claims allowed. Found: " + result.getIncorrectClaims(),
			result.getIncorrectClaims().isEmpty()
		);
		
		assertTrue(
			"No scope violations allowed. Found: " + result.getScopeViolations(),
			result.getScopeViolations().isEmpty()
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
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(ModelExplorerScoringTest.class);
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
