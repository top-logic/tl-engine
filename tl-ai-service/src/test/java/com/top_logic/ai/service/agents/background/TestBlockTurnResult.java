/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import junit.framework.TestCase;

/**
 * Tests for {@link BlockTurnResult} thread-local storage.
 */
public class TestBlockTurnResult extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BlockTurnResult.clear();
	}

	@Override
	protected void tearDown() throws Exception {
		BlockTurnResult.clear();
		super.tearDown();
	}

	/** Initially not blocked. */
	public void testInitiallyNotBlocked() {
		assertFalse(BlockTurnResult.isBlocked());
		assertNull(BlockTurnResult.consumeBlockReason());
	}

	/** Block stores reason. */
	public void testBlockStoresReason() {
		BlockTurnResult.block("unsafe");

		assertTrue(BlockTurnResult.isBlocked());
	}

	/** Consume returns reason and clears it. */
	public void testConsumeReturnsAndClears() {
		BlockTurnResult.block("reason");

		assertEquals("reason", BlockTurnResult.consumeBlockReason());
		assertFalse(BlockTurnResult.isBlocked());
		assertNull(BlockTurnResult.consumeBlockReason());
	}

	/** Clear removes pending block. */
	public void testClearRemovesPendingBlock() {
		BlockTurnResult.block("reason");
		BlockTurnResult.clear();

		assertFalse(BlockTurnResult.isBlocked());
		assertNull(BlockTurnResult.consumeBlockReason());
	}
}
