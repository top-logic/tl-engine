/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.oracle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.top_logic.basic.Logger;

/**
 * Simple utility to load the pre-generated oracle JSON file.
 */
public class OracleLoader {

	/**
	 * Loads the oracle JSON from a file.
	 * 
	 * @param filePath
	 *        The path to the oracle file (relative to project root)
	 * @return The oracle JSON string
	 */
	public static String loadOracle(String filePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}

	/**
	 * Loads the oracle JSON from a file, generating it if it doesn't exist.
	 * 
	 * @param filePath
	 *        The path to the oracle file (relative to project root)
	 * @param generator
	 *        A function to generate the oracle if the file doesn't exist
	 * @return The oracle JSON string
	 */
	private static String loadOrGenerateOracle(String filePath, OracleSupplier generator) throws IOException {
		Path path = Paths.get(filePath);
		if (!Files.exists(path)) {
			Logger.info("Oracle file not found, generating: " + filePath, OracleLoader.class);
			return generator.generate(filePath);
		}
		return loadOracle(filePath);
	}

	@FunctionalInterface
	private interface OracleSupplier {
		String generate(String outputPath) throws IOException;
	}

	/**
	 * Loads the luigis.pizzashop oracle (complete, with usages and annotations).
	 * 
	 * @return The oracle JSON string
	 */
	public static String loadPizzaShopOracle() throws IOException {
		return loadOracle("src/test/resources/com/top_logic/ai/demo/luigis-pizzashop-oracle.json");
	}

	/**
	 * Loads the luigis.pizzashop overview oracle (types + parts only, no usages or annotations).
	 * 
	 * <p>
	 * This is a lighter oracle suitable for overview prompts that don't need usage details. If the
	 * oracle file doesn't exist, it will be generated automatically.
	 * </p>
	 * 
	 * @return The oracle JSON string
	 */
	public static String loadPizzaShopOracleForOverview() throws IOException {
		return loadOrGenerateOracle(
			"src/test/resources/com/top_logic/ai/demo/luigis-pizzashop-oracle-overview.json",
			outputPath -> {
				OracleGenerator generator = new OracleGenerator();
				return generator.generatePizzaShopOracleForOverview(outputPath);
			});
	}

	/**
	 * Loads the luigis.pizzashop model XML file.
	 * 
	 * <p>
	 * This returns the authoritative XML model definition which serves as the single source of truth
	 * for the judge to verify agent answers against.
	 * </p>
	 * 
	 * @return The model XML string
	 */
	public static String loadPizzaShopModelXml() throws IOException {
		return loadOracle("src/main/webapp/WEB-INF/model/luigis.pizzashop.model.xml");
	}
}
