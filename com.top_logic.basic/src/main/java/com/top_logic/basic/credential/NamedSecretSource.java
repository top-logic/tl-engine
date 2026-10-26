/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link SecretSource} fetching a secret by its name from the secret storage of the installation.
 *
 * <p>
 * It is the default {@link SecretSource}: an element without <code>class</code> attribute names
 * the secret:
 * </p>
 *
 * <pre>
 * &lt;api-key name="tl_openai_api_key"/&gt;
 * </pre>
 *
 * <p>
 * Where the named secrets live is configured once per installation in the
 * {@link SecretStoreService}: by
 * default, the secret is read from the system property or environment variable with its name
 * (see {@link EnvironmentSecretStore}).
 * </p>
 *
 * <p>
 * A service reading a named secret declares a dependency on the {@link SecretStoreService}; the
 * secret cannot be read while that service is not started.
 * </p>
 *
 * @see SecretStoreService#getSecret(String)
 */
@Label("Secret by name")
public class NamedSecretSource extends AbstractConfiguredInstance<NamedSecretSource.Config>
		implements SecretSource {

	/**
	 * Configuration options for {@link NamedSecretSource}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<NamedSecretSource> {

		/**
		 * Tag name of this configuration in a list of {@link SecretSource}s.
		 */
		String TAG_NAME = "named-secret";

		/**
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * Name of the secret in the secret storage of the installation.
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
	 * Creates a {@link NamedSecretSource} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public NamedSecretSource(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String getSecret() {
		return SecretStoreService.secret(getConfig().getName());
	}

}
