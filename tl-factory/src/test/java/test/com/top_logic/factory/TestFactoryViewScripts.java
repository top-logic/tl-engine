/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory;

import junit.framework.Test;

import test.com.top_logic.layout.view.TestViewScripts;

/**
 * Parses the views of this module and checks their scripts, see {@link TestViewScripts}.
 */
public class TestFactoryViewScripts {

	/**
	 * The test checking the views of this module.
	 */
	public static Test suite() {
		return TestViewScripts.suite(TestFactoryViewScripts.class);
	}
}
