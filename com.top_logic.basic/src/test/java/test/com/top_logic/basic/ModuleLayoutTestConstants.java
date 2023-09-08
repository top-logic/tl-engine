/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Constants defining the layout of module tests.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModuleLayoutTestConstants {

	/**
	 * Relative path to the test web application root from the module root.
	 */
	public static final String SRC_TEST_WEBAPP_PATH = "src/test/webapp";

	/**
	 * Relative path to the test web application root from the web application root.
	 */
	public static final String PATH_TO_TEST_WEB_APPLICATION =
		ModuleLayoutConstants.PATH_TO_MODULE_ROOT + "/" + SRC_TEST_WEBAPP_PATH;

}
