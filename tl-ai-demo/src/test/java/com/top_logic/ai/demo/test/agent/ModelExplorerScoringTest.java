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
import com.top_logic.ai.demo.test.agent.util.ExpectedFactsBuilder;
import com.top_logic.ai.demo.test.agent.util.RequiredFacts;
import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.tools.help.ModelExplorerTool;
import com.top_logic.ai.service.tools.model.messages.TypeListResponse;
import com.top_logic.ai.service.tools.model.messages.TypePartListResponse;
import com.top_logic.ai.service.tools.util.DataClassSerializer;
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
	 * Builds required facts for overview prompts: types + relationships (references only).
	 * 
	 * @return JSON string with required facts
	 */
	private String buildOverviewRequiredFacts(String testMethodName) {
		ExpectedFactsBuilder builder = new ExpectedFactsBuilder();
		RequiredFacts required = new RequiredFacts();
		
		// Get all types (required)
		String typesJson = builder.getBrowserTool().listTypes("luigis.pizzashop");
		required.add(typesJson);
		
		// Get relationships via listTypeParts filtering for references (required)
		TypeListResponse typesResponse = builder.parseResponse(typesJson, TypeListResponse.class);
		if (typesResponse.getTypes() != null) {
			for (var type : typesResponse.getTypes()) {
				String typeName = type.getName();
				
				// Only call listTypeParts for classes (not enumerations)
				if (!"class".equals(type.getMetaType())) {
					continue;
				}
				
				try {
					// Get all parts first
					String allPartsJson = builder.getBrowserTool().listTypeParts("luigis.pizzashop", typeName, false);
					TypePartListResponse allPartsResponse =
						builder.parseResponse(allPartsJson, TypePartListResponse.class);

					// Filter to only reference parts
					TypePartListResponse referencesOnly = TypePartListResponse.create();
					for (var part : allPartsResponse.getParts()) {
						if ("reference".equals(part.getKind())) {
							referencesOnly.addPart(part);
						}
					}

					// Only add if there are reference parts
					if (!referencesOnly.getParts().isEmpty()) {
						String referencesJson = DataClassSerializer.toJson(referencesOnly,
							ModelExplorerScoringTest.class, testMethodName);
						required.add(referencesJson);
					}
				} catch (Exception ex) {
					System.err.println("Warning: Failed to get parts for " + typeName);
				}
			}
		}
		
		String requiredFactsJson = required.toJson();
		System.out.println("Required facts: " + required.size() + " tool results (types + relationships)");
		System.out.println("\n--- Required Facts JSON ---");
		System.out.println(requiredFactsJson);
		System.out.println("--- End Required Facts ---\n");
		
		return requiredFactsJson;
	}

	/**
	 * Test 1: High-level overview of types and relationships.
	 * 
	 * Prompt: "give me a high-level overview of the luigis.pizzashop model (types and relationships only)"
	 * 
	 * Required facts: All types + relationships (not individual attributes)
	 */
	public void _disabled_testPrompt1_HighLevelOverview() throws IOException {
		String prompt = "give me a high-level overview of the luigis.pizzashop model (types and relationships only)";
		int minScore = 7;
		
		System.out.println("\n==========================================");
		System.out.println("TEST 1: High-Level Overview");
		System.out.println("==========================================");
		System.out.println("Prompt: " + prompt);
		System.out.println();
		
		// 1. Build required facts - for overview we need: types + relationships ONLY
		String requiredFactsJson = buildOverviewRequiredFacts("testPrompt1_HighLevelOverview()");
		
		// 2. Load oracle (overview = types + parts only, no usages/annotations)
		String oracleJson = OracleLoader.loadPizzaShopOracleForOverview();
		System.out.println("Oracle loaded: " + oracleJson.length() + " chars (overview)");
		
		// 3. Run the agent
		System.out.println("\nRunning ModelExplorerTool agent...");
		ModelExplorerTool agent = new ModelExplorerTool();
		String agentAnswer = agent.exploreModel(prompt);
		
		System.out.println("\n--- Agent Answer ---");
		System.out.println(agentAnswer);
		System.out.println();
		
		// 4. Score with AI judge
		System.out.println("Scoring with AI judge...");
		JudgeClient judge = new JudgeClient(AIService.getInstance());
		String judgePrompt = loadJudgeSystemPrompt();
		
		JudgeResult result = judge.score(requiredFactsJson, oracleJson, agentAnswer, judgePrompt);
		
		// 5. Print results
		printJudgeResult(result);
		
		// 6. Assertions
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
	 * Required facts: All types + relationships (focusing on type hierarchy and aggregation)
	 */
	public void testPrompt2_RootAggregateTypes() throws IOException {
		String prompt = "list all root / aggregate types in the model and how they relate";
		int minScore = 7;
		
		System.out.println("\n==========================================");
		System.out.println("TEST 2: Root/Aggregate Types");
		System.out.println("==========================================");
		System.out.println("Prompt: " + prompt);
		System.out.println();
		
		// 1. Build required facts - for root/aggregate types we need: types + relationships
		String requiredFactsJson = buildOverviewRequiredFacts("testPrompt2_RootAggregateTypes()");
		
		// 2. Load oracle (overview = types + parts only)
		String oracleJson = OracleLoader.loadPizzaShopOracleForOverview();
		System.out.println("Oracle loaded: " + oracleJson.length() + " chars (overview)");
		
		// 3. Run the agent
		System.out.println("\nRunning ModelExplorerTool agent...");
		ModelExplorerTool agent = new ModelExplorerTool();
		String agentAnswer = agent.exploreModel(prompt);
		
		System.out.println("\n--- Agent Answer ---");
		System.out.println(agentAnswer);
		System.out.println();
		
		// 4. Score with AI judge
		System.out.println("Scoring with AI judge...");
		JudgeClient judge = new JudgeClient(AIService.getInstance());
		String judgePrompt = loadJudgeSystemPrompt();
		
		JudgeResult result = judge.score(requiredFactsJson, oracleJson, agentAnswer, judgePrompt);
		
		// 5. Print results
		printJudgeResult(result);
		
		// 6. Assertions
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
