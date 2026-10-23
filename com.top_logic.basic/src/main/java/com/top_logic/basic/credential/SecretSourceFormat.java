/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.EncodingConfigurationValueProvider;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;

/**
 * {@link Format} of a configuration property holding a {@link SecretSource}, that accepts a secret
 * given as plain attribute value.
 *
 * <p>
 * An attribute value is read as {@link LiteralSecretSource} with that value. The value is decoded
 * like the value of an {@link Encrypted} property: it is either encrypted or carries the prefix
 * <code>unencrypted:</code>. An empty attribute value means that no {@link SecretSource} is
 * configured.
 * </p>
 *
 * <p>
 * When writing, a {@link LiteralSecretSource} with a non-empty secret is written as encrypted
 * attribute value. Any other {@link SecretSource} is written as element and read back from it.
 * </p>
 *
 * <p>
 * Being an {@link EncodingConfigurationValueProvider}, this format makes the property
 * {@link PropertyDescriptor#isEncrypted() encrypted}, so that a configured value is not reported in
 * error messages.
 * </p>
 *
 * @see SecretSource
 */
public class SecretSourceFormat extends EncodingConfigurationValueProvider<PolymorphicConfiguration<? extends SecretSource>> {

	/**
	 * Singleton {@link SecretSourceFormat} instance.
	 */
	public static final SecretSourceFormat INSTANCE = new SecretSourceFormat();

	private SecretSourceFormat() {
		super(LiteralFormat.INSTANCE);
	}

	/**
	 * Whether the given property holds the configuration of a {@link SecretSource}.
	 */
	public static boolean isSecretSource(PropertyDescriptor property) {
		Class<?> instanceType = property.getInstanceType();
		return property.kind() == PropertyKind.ITEM && instanceType != null
			&& SecretSource.class.isAssignableFrom(instanceType);
	}

	/**
	 * Format of a {@link LiteralSecretSource} given by its plain secret.
	 */
	private static final class LiteralFormat
			extends AbstractConfigurationValueProvider<PolymorphicConfiguration<? extends SecretSource>> {

		static final LiteralFormat INSTANCE = new LiteralFormat();

		private LiteralFormat() {
			super(PolymorphicConfiguration.class);
		}

		@Override
		protected PolymorphicConfiguration<? extends SecretSource> getValueNonEmpty(String propertyName,
				CharSequence propertyValue) throws ConfigurationException {
			LiteralSecretSource.Config result = TypedConfiguration.newConfigItem(LiteralSecretSource.Config.class);
			result.setValue(propertyValue.toString());
			return result;
		}

		@Override
		protected String getSpecificationNonNull(PolymorphicConfiguration<? extends SecretSource> configValue) {
			return ((LiteralSecretSource.Config) configValue).getValue();
		}

		@Override
		public boolean isLegalValue(Object value) {
			return value == null || isAttributeLiteral(value);
		}

		/**
		 * Whether the given value is a {@link LiteralSecretSource} that can be written as attribute
		 * without losing information.
		 */
		private static boolean isAttributeLiteral(Object value) {
			if (!(value instanceof LiteralSecretSource.Config)) {
				return false;
			}
			LiteralSecretSource.Config config = (LiteralSecretSource.Config) value;
			return config.descriptor().getConfigurationInterface() == LiteralSecretSource.Config.class
				&& config.getImplementationClass() == LiteralSecretSource.class
				&& !StringServices.isEmpty(config.getValue());
		}

	}

}
