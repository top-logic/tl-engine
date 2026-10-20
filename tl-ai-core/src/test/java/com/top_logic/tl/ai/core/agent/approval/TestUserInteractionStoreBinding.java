/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import junit.framework.TestCase;

/**
 * Tests for {@link UserInteractionStoreBinding#bind(UserInteractionStore)}.
 */
public class TestUserInteractionStoreBinding extends TestCase {

	@Override
	protected void tearDown() {
		try (UserInteractionStoreBinding.Binding b = UserInteractionStoreBinding.bind(null)) {
			// nothing
		}
	}

	public void testBindRestoresAbsence() {
		assertNull(UserInteractionStoreBinding.getStore());

		UserInteractionStore store = new UserInteractionStore();
		try (UserInteractionStoreBinding.Binding binding = UserInteractionStoreBinding.bind(store)) {
			assertSame(store, UserInteractionStoreBinding.getStore());
		}

		assertNull(UserInteractionStoreBinding.getStore());
	}

	public void testBindRestoresPriorValue() {
		UserInteractionStore outer = new UserInteractionStore();
		try (UserInteractionStoreBinding.Binding b1 = UserInteractionStoreBinding.bind(outer)) {
			UserInteractionStore inner = new UserInteractionStore();
			try (UserInteractionStoreBinding.Binding b2 = UserInteractionStoreBinding.bind(inner)) {
				assertSame(inner, UserInteractionStoreBinding.getStore());
			}
			assertSame(outer, UserInteractionStoreBinding.getStore());
		}
	}

	public void testBindRestoresOnException() {
		UserInteractionStore outer = new UserInteractionStore();
		try (UserInteractionStoreBinding.Binding b1 = UserInteractionStoreBinding.bind(outer)) {
			try (UserInteractionStoreBinding.Binding b2 = UserInteractionStoreBinding.bind(new UserInteractionStore())) {
				throw new RuntimeException("boom");
			} catch (RuntimeException expected) {
				// expected
			}
			assertSame(outer, UserInteractionStoreBinding.getStore());
		}
	}
}
