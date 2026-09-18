/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import junit.framework.Test;

/**
 * Collects {@link com.top_logic.ai.chat.view.TestChatJoinLog} into {@code test.TestAll}. The cases
 * live next to {@code ChatJoinLog} because the tally is package-visible.
 */
public class TestChatJoinLog {

	public static Test suite() {
		return com.top_logic.ai.chat.view.TestChatJoinLog.suite();
	}
}
