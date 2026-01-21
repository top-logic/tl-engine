/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.oracle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.model.ModelService;

/**
 * Test to generate the oracle JSON file for the luigis.pizzashop module.
 * 
 * <p>
 * Run this test once to generate the oracle file, then check it into version control.
 * </p>
 */
public class GenerateOracleTest extends TestCase {

	/**
	 * Generates the oracle file for luigis.pizzashop module.
	 * 
	 * This test can be run manually to regenerate the oracle when the model changes.
	 */
	public void testGenerateOracle() throws IOException {
		String outputPath = "src/test/resources/com/top_logic/ai/demo/luigis-pizzashop-oracle.json";
		
		System.out.println("\n==========================================");
		System.out.println("Generating Oracle for luigis.pizzashop");
		System.out.println("==========================================\n");
		
		OracleGenerator generator = new OracleGenerator();
		String oracleJson = generator.generatePizzaShopOracle(outputPath);
		
		System.out.println("\nOracle generated successfully!");
		System.out.println("Output file: " + outputPath);
		System.out.println("Size: " + oracleJson.length() + " characters");
		System.out.println("\nFirst 500 characters of oracle:");
		System.out.println(oracleJson.substring(0, Math.min(500, oracleJson.length())));
		System.out.println("\n...");
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(GenerateOracleTest.class);
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

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
