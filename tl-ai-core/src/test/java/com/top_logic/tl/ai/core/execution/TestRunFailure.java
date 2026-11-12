/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.tl.ai.core.agent.TransactionException;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.provider.I18NConstants;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Whose failure a run that died with an exception is blamed on, and what the user is told it died
 * of.
 *
 * <p>
 * Issue #410: every throwable that ended a chat run was reported to the browser as a provider error
 * of kind {@code UNKNOWN}, which the UI renders as "the model reported an error, you can send
 * again". A user whose write was refused by their own application's constraints was therefore told
 * to repeat a message that would be refused again, and no client could tell a model failure from an
 * application failure.
 * </p>
 *
 * <p>
 * A run that finds no provider to choose its model from fails with a {@link TopLogicException}
 * naming the secrets to provide. Its message is a resource key; the user is told the text of the
 * key in their language, not the key.
 * </p>
 *
 * @see RunFailure
 */
public class TestRunFailure extends TestCase {

	/** The secret named in the message. */
	private static final String SECRET = "tl_anthropic_api_key";

	/** A provider failure keeps its own kind, which is what a client acts on. */
	public void testProviderFailureKeepsItsKind() {
		assertEquals(ProviderException.Kind.RATE_LIMITED.name(),
			RunFailure.kind(new ProviderException(ProviderException.Kind.RATE_LIMITED, 429, "Too many requests")));
	}

	/** A provider failure the loop wrapped is still a provider failure. */
	public void testWrappedProviderFailureKeepsItsKind() {
		ProviderException cause = new ProviderException(
			ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED, 400, "Too long");

		assertEquals(ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED.name(),
			RunFailure.kind(new RuntimeException("Run failed", cause)));
	}

	/** A failure of this application is not a provider failure and must not be reported as one. */
	public void testApplicationFailureIsNotBlamedOnTheModel() {
		assertEquals("A refused write is the application's failure, not the provider's.",
			RunFailure.APPLICATION_ERROR,
			RunFailure.kind(new TransactionException("Commit failed: Cannot store, constraints violated.")));
		assertEquals("Anything else that breaks the run is equally not the provider's failure.",
			RunFailure.APPLICATION_ERROR,
			RunFailure.kind(new IllegalStateException("No session")));
	}

	/** A failure with a message for the user is told in the user's language, and carries its key. */
	public void testMessageOfTheUserIsTranslated() {
		ResKey error = I18NConstants.ERROR_NO_PROVIDER_AVAILABLE__SECRETS.fill(ResKey.text(SECRET));
		TopLogicException failure = new TopLogicException(error);

		String message = RunFailure.message(failure);

		assertEquals(Resources.getInstance().getString(error), message);
		assertTrue(message, message.contains(SECRET));
		assertFalse("The key is not the message: " + message,
			message.contains(I18NConstants.class.getName()));
		assertEquals(error, RunFailure.messageKey(failure));
	}

	/** The run's wrapping one level up does not hide the message. */
	public void testWrappedMessageIsTranslated() {
		ResKey error = I18NConstants.ERROR_NO_PROVIDER_CONFIGURED;

		String message = RunFailure.message(new RuntimeException("Run failed", new TopLogicException(error)));

		assertEquals(Resources.getInstance().getString(error), message);
	}

	/** A provider failure keeps the provider's words. */
	public void testProviderFailureKeepsItsMessage() {
		ProviderException failure = new ProviderException(ProviderException.Kind.RATE_LIMITED, 429, "Too many requests");

		assertEquals(failure.getMessage(), RunFailure.message(failure));
		assertNull(RunFailure.messageKey(failure));
	}

	/** A provider failure with a message for the user shows that message in the user's language. */
	public void testProviderFailureWithAKeyIsTranslated() {
		ResKey error = I18NConstants.ERROR_PROVIDER_UNAVAILABLE__MODEL_PROVIDER_SECRET
			.fill("model", "provider", ResKey.text(SECRET));
		ProviderException failure = new ProviderException(ProviderException.Kind.NO_CREDENTIALS, -1, error);

		assertEquals(Resources.getInstance().getString(error), RunFailure.message(failure));
	}

	/** Any other failure is told with its own message. */
	public void testOtherFailureKeepsItsMessage() {
		assertEquals("No session", RunFailure.message(new IllegalStateException("No session")));
	}

	/**
	 * The failure becomes the run's provider error: its kind, its message in the language of the
	 * thread, its key; the class name where the failure has no message.
	 */
	public void testEvent() {
		ResKey error = I18NConstants.ERROR_NO_PROVIDER_CONFIGURED;

		AgentEvent.ProviderError event = RunFailure.event("run-1", 3, new TopLogicException(error));

		assertEquals("run-1", event.runId());
		assertEquals(3, event.turnNumber());
		assertEquals(RunFailure.APPLICATION_ERROR, event.kind());
		assertEquals(Resources.getInstance().getString(error), event.message());
		assertEquals(error, event.messageKey());
		assertEquals("NullPointerException",
			RunFailure.event("run-1", 0, new NullPointerException()).message());
	}

	/** Suite providing the resources the messages are resolved with. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestRunFailure.class, ResourcesModule.Module.INSTANCE));
	}

}
