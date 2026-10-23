/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Source of a single secret (API key, password, token) that is fetched at the moment of use.
 *
 * <p>
 * A consumer asks for the secret each time it needs it instead of keeping the value. This allows
 * rotating a secret without restarting the application: an {@link EnvironmentSecretSource} reads
 * the variable on each call, a {@link FileSecretSource} re-reads its file after it has been
 * replaced, e.g. when a Kubernetes or Docker secret is updated.
 * </p>
 *
 * <p>
 * A consumer declares a secret as configuration property of type
 * {@link PolymorphicConfiguration} with the {@link SecretSourceFormat}:
 * </p>
 *
 * <pre>
 * &#64;Name(API_KEY)
 * &#64;Format(SecretSourceFormat.class)
 * PolymorphicConfiguration&lt;? extends SecretSource&gt; getApiKey();
 * </pre>
 *
 * <p>
 * Such a property accepts two forms in the configuration:
 * </p>
 *
 * <ul>
 * <li>An element selects any {@link SecretSource} implementation by its class, e.g. an
 * {@link EnvironmentSecretSource} naming the variable that holds the secret, or a
 * {@link FileSecretSource}:
 *
 * <pre>
 * &lt;my-service&gt;
 *   &lt;api-key class="com.top_logic.basic.credential.EnvironmentSecretSource" name="my_api_key"/&gt;
 * &lt;/my-service&gt;
 *
 * &lt;my-service&gt;
 *   &lt;api-key class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/my-api-key"/&gt;
 * &lt;/my-service&gt;
 * </pre>
 *
 * This is the recommended form for a secret from the environment: the configuration holds the
 * name of the variable, not the secret, whereas a secret resolved from an alias is part of the
 * effective configuration and ends up in a file the service editor stores.</li>
 * <li>An attribute value is an encrypted secret (or a value with the prefix
 * <code>unencrypted:</code>) and stands for a {@link LiteralSecretSource}. This is the form of an
 * encrypted {@link String} property, so existing configurations keep working:
 *
 * <pre>
 * &lt;my-service api-key="unencrypted:my-secret-key"/&gt;
 * </pre>
 *
 * </li>
 * </ul>
 *
 * <p>
 * Writing such a configuration back produces the attribute form (with the secret encrypted) for a
 * {@link LiteralSecretSource} and the element form for all other sources.
 * </p>
 *
 * @see Format
 */
public interface SecretSource {

	/**
	 * The current value of the secret.
	 *
	 * <p>
	 * A consumer calls this method each time it uses the secret and does not keep the result.
	 * </p>
	 *
	 * @return The secret, never <code>null</code> and never empty.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is not available. The message of the exception never contains the
	 *         secret.
	 */
	String getSecret();

	/**
	 * Whether the given value is the current secret, e.g. a key sent by a client that is checked
	 * against the configured key.
	 *
	 * <p>
	 * The comparison takes a time independent of the position of the first differing character,
	 * so that the secret cannot be guessed from response times.
	 * </p>
	 *
	 * @param candidate
	 *        The value to check, <code>null</code> never matches.
	 * @return Whether the candidate equals {@link #getSecret()}.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is not available.
	 */
	default boolean matches(String candidate) {
		String secret = getSecret();
		if (candidate == null) {
			return false;
		}
		return MessageDigest.isEqual(secret.getBytes(StandardCharsets.UTF_8),
			candidate.getBytes(StandardCharsets.UTF_8));
	}

}
