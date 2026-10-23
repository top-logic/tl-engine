/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * {@link SecretSource} delivering a secret stored in the configuration itself.
 *
 * <p>
 * The secret is an {@link Encrypted} configuration value: it is written encrypted and accepts a
 * plain value with the prefix <code>unencrypted:</code>. A property using the
 * {@link SecretSourceFormat} accepts this source as plain attribute:
 * </p>
 *
 * <pre>
 * &lt;my-service api-key="unencrypted:my-secret-key"/&gt;
 * </pre>
 *
 * <p>
 * A secret from the environment is referenced by an {@link EnvironmentSecretSource} instead, an
 * element without <code>class</code> attribute: the configuration then holds the name of the
 * variable, not the secret.
 * </p>
 *
 * <pre>
 * &lt;my-service&gt;
 *   &lt;api-key name="MY_API_KEY"/&gt;
 * &lt;/my-service&gt;
 * </pre>
 */
public class LiteralSecretSource extends AbstractConfiguredInstance<LiteralSecretSource.Config>
		implements SecretSource {

	/**
	 * Configuration options for {@link LiteralSecretSource}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<LiteralSecretSource> {

		/**
		 * Tag name of this configuration in a list of {@link SecretSource}s.
		 */
		String TAG_NAME = "literal-secret";

		/**
		 * @see #getValue()
		 */
		String VALUE = "value";

		/**
		 * The secret.
		 */
		@Name(VALUE)
		@Encrypted
		String getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(String value);

	}

	/**
	 * Creates a {@link LiteralSecretSource} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public LiteralSecretSource(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String getSecret() {
		String value = getConfig().getValue();
		if (StringServices.isEmpty(value)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_NOT_CONFIGURED);
		}
		return value;
	}

}
