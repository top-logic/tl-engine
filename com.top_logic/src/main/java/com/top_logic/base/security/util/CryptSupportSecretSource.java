/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * {@link SecretSource} delivering a secret stored in the configuration in the form encoded by
 * {@link CryptSupport}, or in plain text.
 *
 * <p>
 * The value is decoded with {@link CryptSupport} when the secret is used. A
 * value that is not in the encoded form (a list of decimal numbers) is the secret in plain text.
 * </p>
 *
 * <p>
 * A property using the {@link CryptSupportSecretSourceFormat} accepts this source as plain
 * attribute, so that a configuration storing the secret in one of these forms keeps working. A
 * secret given in the configuration encrypted by the configuration encryption is a
 * {@link LiteralSecretSource}, a secret from the environment an element without
 * <code>class</code> attribute naming the variable.
 * </p>
 *
 * @see CryptSupportSecretSourceFormat
 * @see CryptSupport#decodeString(String)
 */
public class CryptSupportSecretSource extends AbstractConfiguredInstance<CryptSupportSecretSource.Config>
		implements SecretSource {

	/**
	 * Configuration options for {@link CryptSupportSecretSource}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<CryptSupportSecretSource> {

		/**
		 * Tag name of this configuration in a list of {@link SecretSource}s.
		 */
		String TAG_NAME = "crypt-support-secret";

		/**
		 * @see #getValue()
		 */
		String VALUE = "value";

		/**
		 * The secret encoded by {@link CryptSupport}, or the secret in plain text.
		 *
		 * @see CryptSupport#encodeString(String)
		 */
		@Name(VALUE)
		String getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(String value);

	}

	/**
	 * Creates a {@link CryptSupportSecretSource} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public CryptSupportSecretSource(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String getSecret() {
		String value = getConfig().getValue();
		if (StringServices.isEmpty(value)) {
			throw new I18NRuntimeException(com.top_logic.basic.credential.I18NConstants.ERROR_SECRET_NOT_CONFIGURED);
		}
		try {
			return CryptSupport.getInstance().decodeString(value);
		} catch (NumberFormatException ex) {
			// Not in the encoded form.
			return value;
		} catch (RuntimeException ex) {
			// The value is not part of the message, it may be the secret in plain text.
			Logger.error("Unable to decode the configured secret, using it as given.", ex,
				CryptSupportSecretSource.class);
			return value;
		}
	}

	@Override
	public ResKey reference() {
		return com.top_logic.basic.credential.I18NConstants.SECRET_IN_CONFIGURATION;
	}

}
