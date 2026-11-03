/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.provider.I18NConstants;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * What the chat tells the user a failed run died of.
 *
 * <p>
 * A run that finds no provider to choose its model from fails with a {@link TopLogicException}
 * naming the secrets to provide. A run whose model has a provider without its key ends with a
 * provider error naming the secret. Both messages are resource keys; the chat shows the text of the
 * key in the user's language, not the key.
 * </p>
 *
 * @see ChatProcessor#errorMessage(Throwable)
 */
public class TestChatErrorMessage extends TestCase {

	/** The secret named in the message. */
	private static final String SECRET = "tl_anthropic_api_key";

	/** A failure with a message for the user is shown in the user's language. */
	public void testMessageOfTheUserIsTranslated() {
		ResKey error = I18NConstants.ERROR_NO_PROVIDER_AVAILABLE__SECRETS.fill(ResKey.text(SECRET));

		String message = ChatProcessor.errorMessage(new TopLogicException(error));

		assertEquals(Resources.getInstance().getString(error), message);
		assertTrue(message, message.contains(SECRET));
		assertFalse("The key is not the message: " + message,
			message.contains(I18NConstants.class.getName()));
	}

	/** The run's wrapping one level up does not hide the message. */
	public void testWrappedMessageIsTranslated() {
		ResKey error = I18NConstants.ERROR_NO_PROVIDER_CONFIGURED;

		String message = ChatProcessor.errorMessage(new RuntimeException("Run failed", new TopLogicException(error)));

		assertEquals(Resources.getInstance().getString(error), message);
	}

	/** A provider failure keeps the provider's words. */
	public void testProviderFailureKeepsItsMessage() {
		ProviderException failure = new ProviderException(ProviderException.Kind.RATE_LIMITED, 429, "Too many requests");

		assertEquals(failure.getMessage(), ChatProcessor.errorMessage(failure));
	}

	/** A provider failure with a message for the user shows that message in the user's language. */
	public void testProviderFailureWithAKeyIsTranslated() {
		ResKey error = I18NConstants.ERROR_PROVIDER_UNAVAILABLE__MODEL_PROVIDER_SECRET
			.fill("model", "provider", ResKey.text(SECRET));
		ProviderException failure = new ProviderException(ProviderException.Kind.AUTHENTICATION, -1, error);

		assertEquals(Resources.getInstance().getString(error), ChatProcessor.errorMessage(failure));
	}

	/**
	 * The provider error a run reports is shown with the text of its key in the user's language, and
	 * in the provider's words where it has none.
	 */
	public void testProviderErrorEventIsTranslated() {
		ResKey error = I18NConstants.ERROR_PROVIDER_UNAVAILABLE__MODEL_PROVIDER_SECRET
			.fill("model", "provider", ResKey.text(SECRET));
		String message = ChatProcessor.providerErrorMessage(new AgentEvent.ProviderError("run", 1, Instant.now(),
			ProviderException.Kind.AUTHENTICATION.name(), "logged text", error));

		assertEquals(Resources.getInstance().getString(error), message);
		assertTrue(message, message.contains(SECRET));
		assertEquals("Overloaded", ChatProcessor.providerErrorMessage(new AgentEvent.ProviderError("run", 1,
			Instant.now(), ProviderException.Kind.OVERLOADED.name(), "Overloaded")));
	}

	/** Any other failure is told with its own message. */
	public void testOtherFailureKeepsItsMessage() {
		assertEquals("No session", ChatProcessor.errorMessage(new IllegalStateException("No session")));
	}

	/** Suite providing the resources the messages are resolved with. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestChatErrorMessage.class, ResourcesModule.Module.INSTANCE));
	}

}
