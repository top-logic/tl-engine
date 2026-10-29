/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicMessagesProvider;

/**
 * {@link CredentialProvider}s with a fixed secret for providers constructed directly in tests, and
 * secrets for provider configurations created in code.
 */
public final class FixedCredentials {

	/**
	 * Credentials adding nothing to a request.
	 */
	public static final CredentialProvider NONE = request -> {
		// No credentials.
	};

	private FixedCredentials() {
		// Utility class.
	}

	/**
	 * Credentials sending the given secret as value of the given header.
	 */
	public static CredentialProvider header(String header, String secret) {
		return request -> request.setHeader(header, secret);
	}

	/**
	 * Credentials sending the given token as bearer token.
	 */
	public static CredentialProvider bearer(String token) {
		return header(CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER + " " + token);
	}

	/**
	 * Credentials sending the given key the way the Anthropic Messages API expects it.
	 */
	public static CredentialProvider anthropic(String apiKey) {
		return header(AnthropicMessagesProvider.API_KEY_HEADER, apiKey);
	}

	/**
	 * The configuration of a {@link LiteralSecretSource} with the given value, e.g. for the API key
	 * of a provider configuration created in code.
	 */
	public static LiteralSecretSource.Config secret(String value) {
		LiteralSecretSource.Config result = TypedConfiguration.newConfigItem(LiteralSecretSource.Config.class);
		result.setValue(value);
		return result;
	}

}
