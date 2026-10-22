/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.contact;

import junit.framework.Test;

import test.com.top_logic.layout.view.TestViewScripts;

/**
 * Checks the scripts of the views of this module, see {@link TestViewScripts}.
 */
public class TestContactViewScripts {

	/**
	 * The test checking the views of this module.
	 */
	public static Test suite() {
		return TestViewScripts.suite(TestContactViewScripts.class);
	}
}
