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

/**
 * What the chat tells the user a provider error of a run said.
 *
 * <p>
 * A run whose model has a provider without its key ends with a provider error naming the secret.
 * Its message is a resource key; the chat shows the text of the key in the user's language, not
 * the key.
 * </p>
 *
 * @see ChatProcessor#providerErrorMessage(AgentEvent.ProviderError)
 */
public class TestChatErrorMessage extends TestCase {

	/** The secret named in the message. */
	private static final String SECRET = "tl_anthropic_api_key";

	/**
	 * The provider error a run reports is shown with the text of its key in the user's language, and
	 * in the provider's words where it has none.
	 */
	public void testProviderErrorEventIsTranslated() {
		ResKey error = I18NConstants.ERROR_PROVIDER_UNAVAILABLE__MODEL_PROVIDER_SECRET
			.fill("model", "provider", ResKey.text(SECRET));
		String message = ChatProcessor.providerErrorMessage(new AgentEvent.ProviderError("run", 1, Instant.now(),
			ProviderException.Kind.NO_CREDENTIALS.name(), "logged text", error));

		assertEquals(Resources.getInstance().getString(error), message);
		assertTrue(message, message.contains(SECRET));
		assertEquals("Overloaded", ChatProcessor.providerErrorMessage(new AgentEvent.ProviderError("run", 1,
			Instant.now(), ProviderException.Kind.OVERLOADED.name(), "Overloaded")));
	}

	/** Suite providing the resources the messages are resolved with. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestChatErrorMessage.class, ResourcesModule.Module.INSTANCE));
	}

}
