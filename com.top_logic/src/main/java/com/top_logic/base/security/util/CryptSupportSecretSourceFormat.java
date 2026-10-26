/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;

/**
 * {@link Format} of a configuration property holding a {@link SecretSource}, that accepts a secret
 * given as attribute value in the form encoded by {@link CryptSupport}, or in plain text.
 *
 * <p>
 * An attribute value is read as {@link CryptSupportSecretSource} with that value. This is the form
 * of a {@link String} property whose value is decoded with {@link CryptSupport}, so existing
 * configurations keep working. An empty attribute value means that no {@link SecretSource} is
 * configured. All other sources are given as element, as with the {@link SecretSourceFormat}, e.g.
 * an element without <code>class</code> attribute naming the environment variable holding the
 * secret:
 * </p>
 *
 * <pre>
 * &lt;master-credential name="ldap_credential"/&gt;
 * </pre>
 *
 * <p>
 * When writing, a {@link CryptSupportSecretSource} is written as attribute with its value as
 * configured. Any other {@link SecretSource} is written as element and read back from it.
 * </p>
 */
public class CryptSupportSecretSourceFormat
		extends AbstractConfigurationValueProvider<PolymorphicConfiguration<? extends SecretSource>> {

	/**
	 * Singleton {@link CryptSupportSecretSourceFormat} instance.
	 */
	public static final CryptSupportSecretSourceFormat INSTANCE = new CryptSupportSecretSourceFormat();

	private CryptSupportSecretSourceFormat() {
		super(PolymorphicConfiguration.class);
	}

	@Override
	protected PolymorphicConfiguration<? extends SecretSource> getValueNonEmpty(String propertyName,
			CharSequence propertyValue) throws ConfigurationException {
		CryptSupportSecretSource.Config result =
			TypedConfiguration.newConfigItem(CryptSupportSecretSource.Config.class);
		result.setValue(propertyValue.toString());
		return result;
	}

	@Override
	protected String getSpecificationNonNull(PolymorphicConfiguration<? extends SecretSource> configValue) {
		return ((CryptSupportSecretSource.Config) configValue).getValue();
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value == null || isAttributeValue(value);
	}

	/**
	 * Whether the given value is a {@link CryptSupportSecretSource} that can be written as
	 * attribute without losing information.
	 */
	private static boolean isAttributeValue(Object value) {
		if (!(value instanceof CryptSupportSecretSource.Config)) {
			return false;
		}
		CryptSupportSecretSource.Config config = (CryptSupportSecretSource.Config) value;
		return config.descriptor().getConfigurationInterface() == CryptSupportSecretSource.Config.class
			&& config.getImplementationClass() == CryptSupportSecretSource.class
			&& !StringServices.isEmpty(config.getValue());
	}

}
