/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Test for {@link ToolContext#forSubagent}.
 */
public class TestToolContextSubagent extends TestCase {

	/**
	 * forSubagent uses its own cancellation token and increments delegation depth.
	 */
	public void testForSubagentUsesOwnTokenAndIncrementsDepth() {
		ToolContext parent = ToolContext.EMPTY;
		int parentDepth = parent.delegationDepth();

		CancellationToken childToken = new CancellationToken();
		ToolContext child = parent.forSubagent(new TransactionScope(), childToken);

		assertSame(childToken, child.cancellationToken());
		assertEquals(parentDepth + 1, child.delegationDepth());
		assertNotSame(parent.cancellationToken(), child.cancellationToken());

		child.cancellationToken().cancel();
		assertFalse("parent token unaffected by child cancel", parent.cancellationToken().isCancelled());
	}

}
