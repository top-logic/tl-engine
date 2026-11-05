/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.addons.loginmessages.view;

import junit.framework.Test;

import test.com.top_logic.layout.view.TestViewScriptFunctions;

/**
 * Checks that the functions the views of this module call - among them
 * <code>loginMessagesDisplay()</code> and <code>loginMessagesConfirm()</code> - are known to
 * TL-Script, see {@link TestViewScriptFunctions}.
 */
public class TestLoginMessagesViewScriptFunctions {

	/**
	 * The test checking the views of this module.
	 */
	public static Test suite() {
		return TestViewScriptFunctions.suite(TestLoginMessagesViewScriptFunctions.class);
	}
}
