/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * {@link SecretSource} reading the secret from a system property or an environment variable.
 *
 * <p>
 * The variable is looked up each time the secret is used, in the same order as the
 * <code>env</code> aliases of the configuration: a JNDI value of the application context first,
 * then a system property, then an environment variable.
 * </p>
 *
 * <p>
 * It references a variable of the environment explicitly, regardless of the secret storage of the
 * installation:
 * </p>
 *
 * <pre>
 * &lt;api-key class="com.top_logic.basic.credential.EnvironmentSecretSource" name="MY_API_KEY"/&gt;
 * </pre>
 *
 * <p>
 * An element without <code>class</code> attribute is a {@link NamedSecretSource} instead, whose
 * secret is fetched from the {@link SecretStoreService secret storage} of the installation, by default
 * from the variable with the same name.
 * </p>
 *
 * @see Environment#getSystemPropertyOrEnvironmentVariable(String, String)
 */
public class EnvironmentSecretSource extends AbstractConfiguredInstance<EnvironmentSecretSource.Config>
		implements SecretSource {

	/**
	 * Configuration options for {@link EnvironmentSecretSource}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<EnvironmentSecretSource> {

		/**
		 * Tag name of this configuration in a list of {@link SecretSource}s.
		 */
		String TAG_NAME = "env-secret";

		/**
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * Name of the system property or environment variable holding the secret.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * @see #getName()
		 */
		void setName(String value);

	}

	/**
	 * Creates a {@link EnvironmentSecretSource} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public EnvironmentSecretSource(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String getSecret() {
		String name = getConfig().getName();
		String value = EnvironmentSecretStore.variable(name);
		if (value == null) {
			throw new I18NRuntimeException(EnvironmentSecretStore.undefined(name));
		}
		return value;
	}

}
