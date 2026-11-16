/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.InvalidEncryptedValueException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.EncodingConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * {@link SecretStore} reading a secret from the system property or environment variable named
 * like the secret.
 *
 * <p>
 * The variable is looked up each time the secret is used, in the same order as the
 * <code>env</code> aliases of the configuration: a JNDI value of the application context first,
 * then a system property, then an environment variable. An empty variable counts as undefined.
 * </p>
 *
 * <p>
 * The variable holds the plain secret. A value with the prefix
 * {@link EncodingConfigurationValueProvider#UNENCRYPTED_PREFIX}, as written for an alias read into
 * an encrypted configuration property, is accepted as well; the prefix is stripped. A value in the
 * authenticated encrypted format with the prefix {@link ConfigurationEncryption#AUTHENTICATED_PREFIX}
 * is decrypted, with a warning in the log, since the encryption only obscures the secret. A value
 * in the old unauthenticated encrypted format cannot be told apart from a plain value and is read
 * as plain secret.
 * </p>
 *
 * <p>
 * It is the default store of the installation, so that the secret
 * <code>&lt;api-key name="tl_openai_api_key"/&gt;</code> is read from the variable
 * <code>tl_openai_api_key</code> unless the {@link SecretStoreService} configures another store.
 * </p>
 *
 * @see Environment#getSystemPropertyOrEnvironmentVariable(String, String)
 */
@Label("Environment variables")
public class EnvironmentSecretStore extends AbstractSecretStore<EnvironmentSecretStore.Config> {

	/**
	 * Configuration options for {@link EnvironmentSecretStore}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AbstractSecretStore.Config<EnvironmentSecretStore> {

		/**
		 * Tag name of this configuration in a list of {@link SecretStore}s.
		 */
		String TAG_NAME = "env-store";

	}

	/**
	 * Names of the variables with an encrypted value already reported in the log.
	 */
	private static final Set<String> ENCRYPTED_VARIABLES_REPORTED = ConcurrentHashMap.newKeySet();

	/**
	 * Creates a {@link EnvironmentSecretStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public EnvironmentSecretStore(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected String lookupKey(String key) {
		return variable(key);
	}

	@Override
	protected ResKey missing(String key) {
		return undefined(key);
	}

	/**
	 * The secret held by the system property or environment variable with the given name.
	 *
	 * <p>
	 * The prefix {@link EncodingConfigurationValueProvider#UNENCRYPTED_PREFIX} is stripped. A value
	 * in the authenticated encrypted format (see {@link ConfigurationEncryption#isAuthenticated(String)})
	 * is decrypted, with a warning in the log once per variable. Any other value is the plain
	 * secret, also a value in the unauthenticated encrypted format without prefix, which cannot be
	 * told apart from a plain value.
	 * </p>
	 *
	 * @return The secret, or <code>null</code> if the variable is undefined or empty.
	 * @throws I18NRuntimeException
	 *         If the variable holds an encrypted value that cannot be decrypted. The message names
	 *         the variable, not its value.
	 */
	static String variable(String name) {
		String value = Environment.getSystemPropertyOrEnvironmentVariable(name, null);
		if (value == null) {
			return null;
		}
		if (value.startsWith(EncodingConfigurationValueProvider.UNENCRYPTED_PREFIX)) {
			value = value.substring(EncodingConfigurationValueProvider.UNENCRYPTED_PREFIX.length());
		} else if (ConfigurationEncryption.isAuthenticated(value)) {
			value = decrypt(name, value);
		}
		return StringServices.isEmpty(value) ? null : value;
	}

	private static String decrypt(String name, String encryptedValue) {
		String value;
		try {
			value = ConfigurationEncryption.decrypt(encryptedValue);
		} catch (InvalidEncryptedValueException ex) {
			// The exception does not reveal the value.
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_VARIABLE_NOT_DECRYPTABLE__NAME.fill(name), ex);
		}
		if (ENCRYPTED_VARIABLES_REPORTED.add(name)) {
			Logger.warn("The secret variable '" + name + "' holds an encrypted value. The encryption with the "
				+ "configuration key only obscures the secret, since the key is part of the application. Set the "
				+ "plain value instead, or keep the secret in a secret store such as a directory or a key vault.",
				EnvironmentSecretStore.class);
		}
		return value;
	}

	/**
	 * The error reported for an undefined variable with the given name.
	 */
	static ResKey undefined(String name) {
		return I18NConstants.ERROR_SECRET_VARIABLE_UNDEFINED__NAME.fill(name);
	}

}
