/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.TransactionException;
import com.top_logic.tl.ai.core.provider.ProviderException;

/**
 * Whose failure the chat blames when a run dies.
 *
 * <p>
 * Issue #410: every throwable that reached the chat's catch-all was reported to the browser as
 * {@code provider_error} of kind {@code UNKNOWN}, which the UI renders as "the model reported an
 * error, you can send again". A user whose write was refused by their own application's
 * constraints was therefore told to repeat a message that would be refused again, and no client
 * could tell a model failure from an application failure.
 * </p>
 *
 * @see ChatProcessor#errorKind(Throwable)
 */
public class TestChatErrorKind extends TestCase {

	/**
	 * A provider failure keeps its own kind, which is what a client acts on.
	 */
	public void testProviderFailureKeepsItsKind() {
		assertEquals(ProviderException.Kind.RATE_LIMITED.name(),
			ChatProcessor.errorKind(new ProviderException(
				ProviderException.Kind.RATE_LIMITED, 429, "Too many requests")));
	}

	/**
	 * A provider failure the loop wrapped is still a provider failure.
	 */
	public void testWrappedProviderFailureKeepsItsKind() {
		ProviderException cause = new ProviderException(
			ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED, 400, "Too long");

		assertEquals(ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED.name(),
			ChatProcessor.errorKind(new RuntimeException("Run failed", cause)));
	}

	/**
	 * A failure of this application is not a provider failure and must not be reported as one.
	 */
	public void testApplicationFailureIsNotBlamedOnTheModel() {
		assertEquals("A refused write is the application's failure, not the provider's.",
			ChatProcessor.APPLICATION_ERROR,
			ChatProcessor.errorKind(
				new TransactionException("Commit failed: Cannot store, constraints violated.")));
		assertEquals("Anything else that breaks the run is equally not the provider's failure.",
			ChatProcessor.APPLICATION_ERROR,
			ChatProcessor.errorKind(new IllegalStateException("No session")));
	}

}
