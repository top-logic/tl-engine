/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Encrypted;

/**
 * {@link ConfigurationValueProviderProxy} that decodes the values and encodes the specification.
 * 
 * @see ConfigurationEncryption#encrypt(String)
 * @see ConfigurationEncryption#decrypt(String)
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EncodingConfigurationValueProvider<T> extends ConfigurationValueProviderProxy<T> {

	/**
	 * Prefix for values of {@link Encrypted} properties that are unencrypted. This is useful for
	 * temporary configuration of unencrypted values until encrypted value is determined.
	 */
	private static final String UNENCRYPTED_PREFIX = "unencrypted:";

	/**
	 * Returns an {@link EncodingConfigurationValueProvider} with given
	 * {@link ConfigurationValueProvider} as actual implementation. If given
	 * {@link ConfigurationValueProvider} is already an {@link EncodingConfigurationValueProvider},
	 * the given implementation is returned.
	 */
	public static <T> EncodingConfigurationValueProvider<T> ensureEncodedConfigurationValue(
			ConfigurationValueProvider<T> impl) {
		if (impl instanceof EncodingConfigurationValueProvider) {
			return (EncodingConfigurationValueProvider<T>) impl;
		}
		return new EncodingConfigurationValueProvider<>(impl);
	}

	private final ConfigurationValueProvider<T> _impl;

	/**
	 * Creates a new {@link EncodingConfigurationValueProvider}.
	 * 
	 * @param impl
	 *        The actual {@link ConfigurationValueProvider} to encapsulate.
	 */
	protected EncodingConfigurationValueProvider(ConfigurationValueProvider<T> impl) {
		_impl = impl;
	}

	@Override
	protected ConfigurationValueProvider<T> impl() {
		return _impl;
	}

	@Override
	public T getValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		CharSequence decryptedCharSequence;
		if (StringServices.isEmpty(propertyValue)) {
			decryptedCharSequence = propertyValue;
		} else {
			decryptedCharSequence = decodePropertyValue(propertyName, propertyValue);
		}
		return super.getValue(propertyName, decryptedCharSequence);
	}

	private CharSequence decodePropertyValue(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		CharSequence decryptedCharSequence;
		String encodedValue = propertyValue.toString();
		try {
			decryptedCharSequence = ConfigurationEncryption.decrypt(encodedValue);
		} catch (RuntimeException ex) {
			decryptedCharSequence = handleUnencryptedValue(propertyName, encodedValue, ex);
		}
		return decryptedCharSequence;
	}

	private CharSequence handleUnencryptedValue(String propertyName, String encodedValue, RuntimeException ex)
			throws ConfigurationException {
		if (encodedValue.startsWith(UNENCRYPTED_PREFIX)) {
			return encodedValue.substring(UNENCRYPTED_PREFIX.length());
		}
		StringBuilder msg = new StringBuilder();
		msg.append("Unable to decode '");
		msg.append(encodedValue);
		msg.append("' as value for property '");
		msg.append(propertyName);
		msg.append("'. For temporary configuration of unencoded values use '");
		msg.append(UNENCRYPTED_PREFIX);
		msg.append("' as prefix.");
		throw new ConfigurationException(msg.toString(), ex);
	}

	@Override
	public String getSpecification(T configValue) {
		String specification = super.getSpecification(configValue);
		String encryptedSpecification;
		if (StringServices.isEmpty(specification)) {
			encryptedSpecification = specification;
		} else {
			encryptedSpecification = ConfigurationEncryption.encrypt(specification);
		}
		return encryptedSpecification;
	}

}

