/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import junit.framework.TestCase;

/**
 * Tests for {@link ConversationLogListenerBinding#bind(ConversationLogListener)}.
 */
public class TestConversationLogListenerBinding extends TestCase {

	@Override
	protected void tearDown() {
		try (ConversationLogListenerBinding.Binding b = ConversationLogListenerBinding.bind(null)) {
			// nothing
		}
	}

	public void testBindRestoresAbsence() {
		assertNull(ConversationLogListenerBinding.getListener());

		ConversationLogListener listener = new ConversationLogListener(new ConversationLog());
		try (ConversationLogListenerBinding.Binding binding = ConversationLogListenerBinding.bind(listener)) {
			assertSame(listener, ConversationLogListenerBinding.getListener());
		}

		assertNull(ConversationLogListenerBinding.getListener());
	}

	public void testBindRestoresPriorValue() {
		ConversationLogListener outer = new ConversationLogListener(new ConversationLog());
		try (ConversationLogListenerBinding.Binding b1 = ConversationLogListenerBinding.bind(outer)) {
			ConversationLogListener inner = new ConversationLogListener(new ConversationLog());
			try (ConversationLogListenerBinding.Binding b2 = ConversationLogListenerBinding.bind(inner)) {
				assertSame(inner, ConversationLogListenerBinding.getListener());
			}
			assertSame(outer, ConversationLogListenerBinding.getListener());
		}
	}

	public void testBindRestoresOnException() {
		ConversationLogListener outer = new ConversationLogListener(new ConversationLog());
		try (ConversationLogListenerBinding.Binding b1 = ConversationLogListenerBinding.bind(outer)) {
			try (ConversationLogListenerBinding.Binding b2 = ConversationLogListenerBinding.bind(
					new ConversationLogListener(new ConversationLog()))) {
				throw new RuntimeException("boom");
			} catch (RuntimeException expected) {
				// expected
			}
			assertSame(outer, ConversationLogListenerBinding.getListener());
		}
	}
}
