/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import junit.framework.Test;

/**
 * Collects {@link com.top_logic.ai.chat.view.TestPresentTool} into {@code test.TestAll}. The cases
 * live next to {@code ChatViewBinding} because they use its package-visible bind.
 */
public class TestPresentTool {

	public static Test suite() {
		return com.top_logic.ai.chat.view.TestPresentTool.suite();
	}
}
