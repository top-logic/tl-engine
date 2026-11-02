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
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * Source of a single secret (API key, password, token) that is fetched at the moment of use.
 *
 * <p>
 * A consumer asks for the secret each time it needs it instead of keeping the value. This allows
 * rotating a secret without restarting the application: an {@link EnvironmentSecretSource} reads
 * the variable on each call, a {@link FileSecretSource} re-reads its file after it has been
 * replaced, e.g. when a Kubernetes or Docker secret is updated, and a {@link NamedSecretSource}
 * asks the {@link SecretStore} of the installation each time.
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
 * <li>An element without <code>class</code> attribute is a {@link NamedSecretSource} naming the
 * secret. Where the named secrets live is decided once per installation in the
 * {@link SecretStoreService}:
 * by default in system properties or environment variables of the same name, in production e.g. in
 * the files of a mounted secret directory or in a vault:
 *
 * <pre>
 * &lt;my-service&gt;
 *   &lt;api-key name="my_api_key"/&gt;
 * &lt;/my-service&gt;
 * </pre>
 *
 * This is the recommended form: the configuration holds the name of the secret, not the secret,
 * and stays the same in all installations, whereas a secret resolved from an alias is part of the
 * effective configuration and ends up in a file the service editor stores.</li>
 * <li>An element with <code>class</code> attribute selects another {@link SecretSource}
 * implementation for a secret that lives apart from the secret storage of the installation, e.g. a
 * {@link FileSecretSource} or an {@link EnvironmentSecretSource} reading a variable regardless of
 * the configured storage:
 *
 * <pre>
 * &lt;my-service&gt;
 *   &lt;api-key class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/my-api-key"/&gt;
 * &lt;/my-service&gt;
 * </pre>
 *
 * </li>
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
 * {@link LiteralSecretSource}, the element without <code>class</code> for a
 * {@link NamedSecretSource} and the element with <code>class</code> for all other sources. The
 * service editor preselects the {@link NamedSecretSource} for a new mandatory secret, since it
 * keeps the secret out of the configuration files. A property may choose another default with an
 * {@link ImplementationClassDefault} annotation of its own.
 * </p>
 *
 * @see Format
 */
@ImplementationClassDefault(NamedSecretSource.class)
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
	 * The current value of the secret, if its source holds it.
	 *
	 * <p>
	 * In contrast to {@link #getSecret()}, a secret that is absent from its source - an undefined
	 * variable, a file that does not exist, a name the secret store does not hold - is answered with
	 * <code>null</code> instead of an error. This tells a missing secret from one that is present
	 * but cannot be read, e.g. when checking whether all secrets of an installation are provided.
	 * </p>
	 *
	 * @return The secret, never empty, or <code>null</code> if the source does not hold it.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is present but cannot be delivered, e.g. an empty or unreadable file.
	 *         The message of the exception never contains the secret.
	 */
	default String lookup() {
		return getSecret();
	}

	/**
	 * Whether this source holds its secret now.
	 *
	 * <p>
	 * Asked each time a consumer decides whether it can offer a function that needs the secret, so
	 * that the function becomes available as soon as the secret is provided, without restarting
	 * the application. The secret is {@link #lookup() looked up} but not returned.
	 * </p>
	 *
	 * <p>
	 * A secret that is present but cannot be read, e.g. an empty or unreadable file, counts as
	 * available: the consumer then uses it and reports the actual problem with
	 * {@link #getSecret()}, instead of silently hiding a function whose secret was provided.
	 * </p>
	 *
	 * @return Whether {@link #lookup()} finds the secret or fails because the secret cannot be
	 *         read; <code>false</code> if the source does not hold it.
	 */
	default boolean isAvailable() {
		try {
			return lookup() != null;
		} catch (I18NRuntimeException ex) {
			// Present but not readable, reported when the secret is used.
			return true;
		}
	}

	/**
	 * Where this source takes its secret from, for an administrator checking the secrets of an
	 * installation.
	 *
	 * <p>
	 * The description identifies the secret without revealing it: the name of a secret or of a
	 * variable, the path of a file, the vault and the name of a secret in it. A secret kept in the
	 * configuration itself is described as such.
	 * </p>
	 *
	 * @return The description, <code>null</code> if this source cannot tell.
	 */
	default ResKey reference() {
		return null;
	}

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
