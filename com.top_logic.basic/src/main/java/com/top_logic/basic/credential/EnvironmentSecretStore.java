/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.core.workspace.Environment;
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
 * It is the default store of the installation, so that the secret
 * <code>&lt;api-key name="tl_openai_api_key"/&gt;</code> is read from the variable
 * <code>tl_openai_api_key</code> unless {@link SecretStores} configures another store.
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
	 * The value of the system property or environment variable with the given name.
	 *
	 * @return The value, or <code>null</code> if the variable is undefined or empty.
	 */
	static String variable(String name) {
		String value = Environment.getSystemPropertyOrEnvironmentVariable(name, null);
		return StringServices.isEmpty(value) ? null : value;
	}

	/**
	 * The error reported for an undefined variable with the given name.
	 */
	static ResKey undefined(String name) {
		return I18NConstants.ERROR_SECRET_VARIABLE_UNDEFINED__NAME.fill(name);
	}

}
