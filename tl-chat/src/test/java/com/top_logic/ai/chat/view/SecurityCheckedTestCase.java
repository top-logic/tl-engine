/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.thread.ThreadContext;

/**
 * A test that runs each of its test methods with the security checks on, as a request of a browser
 * window runs.
 *
 * <p>
 * The test thread runs with the security checks bypassed, and with them bypassed the person of the
 * current context counts as an administrator. A test that asks whether an account may read or write
 * an object - the access to a chat is decided by the model-based security - must therefore switch
 * the checks on, or every account may do everything. The fixture made in {@link #setUp()} is made
 * with the checks bypassed.
 * </p>
 */
public abstract class SecurityCheckedTestCase extends BasicTestCase {

	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> withSecurityChecks(true, () -> {
			super.runTest();
			return null;
		});
	}

}
