/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collects {@link com.top_logic.ai.chat.view.TestChatViewBinding} into {@code test.TestAll}. The
 * cases live next to {@code ChatViewBinding} because they use its package-visible bind and
 * preference hook.
 */
public class TestChatViewBinding {

	public static Test suite() {
		return new TestSuite(com.top_logic.ai.chat.view.TestChatViewBinding.class);
	}
}
