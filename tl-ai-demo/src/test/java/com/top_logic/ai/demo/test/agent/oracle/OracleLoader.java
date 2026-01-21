/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.oracle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	 * Loads the luigis.pizzashop oracle.
	 * 
	 * @return The oracle JSON string
	 */
	public static String loadPizzaShopOracle() throws IOException {
		return loadOracle("src/test/resources/com/top_logic/ai/demo/luigis-pizzashop-oracle.json");
	}
}
