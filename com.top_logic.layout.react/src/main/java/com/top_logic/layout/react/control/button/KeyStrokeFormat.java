/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link com.top_logic.basic.config.ConfigurationValueProvider} that parses a {@link KeyStroke}
 * from a string attribute value (e.g. {@code key="Ctrl+S"}), rejecting malformed gestures at
 * configuration load time.
 *
 * <p>
 * Usage on a configuration property:
 * </p>
 *
 * <pre>
 * &#64;Format(KeyStrokeFormat.class)
 * KeyStroke getKey();
 * </pre>
 */
public class KeyStrokeFormat extends AbstractConfigurationValueProvider<KeyStroke> {

	/** Singleton instance. */
	public static final KeyStrokeFormat INSTANCE = new KeyStrokeFormat();

	/**
	 * Creates a {@link KeyStrokeFormat}.
	 */
	public KeyStrokeFormat() {
		super(KeyStroke.class);
	}

	@Override
	protected KeyStroke getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		try {
			return KeyStroke.parse(propertyValue.toString());
		} catch (IllegalArgumentException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_INVALID_KEY_GESTURE, propertyName, propertyValue, ex);
		}
	}

	@Override
	protected String getSpecificationNonNull(KeyStroke configValue) {
		return configValue.toString();
	}
}
