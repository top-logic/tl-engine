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


/**
 * Utility to load the model XML for scoring tests.
 */
public class OracleLoader {

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
		Path path = Paths.get("src/main/webapp/WEB-INF/model/luigis.pizzashop.model.xml");
		return new String(Files.readAllBytes(path));
	}
}
