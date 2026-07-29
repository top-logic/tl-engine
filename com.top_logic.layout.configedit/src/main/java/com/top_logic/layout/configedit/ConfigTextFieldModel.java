/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Edits a configuration property whose values are not strings as their textual specification.
 *
 * <p>
 * A property may declare a value format, e.g. a channel reference written as {@code input="model"}.
 * Such a value is edited in the same form the configuration file uses for it: the property's own
 * {@link ConfigurationValueProvider} converts between the text shown in the input and the value
 * stored in the configuration, so exactly the specifications the configuration reader accepts can be
 * entered. Without the conversion the input would hand the property a string it cannot store.
 * </p>
 */
public class ConfigTextFieldModel extends ConfigFieldModel {

	private final ConfigurationValueProvider<Object> _format;

	/**
	 * Whether the given property is edited through its textual specification rather than directly.
	 *
	 * @param property
	 *        The property to edit.
	 * @return {@code true} if the property declares a value format and its values are not strings.
	 */
	public static boolean isFormatted(PropertyDescriptor property) {
		ConfigurationValueProvider<?> format = property.getValueProvider();
		return format != null && format.getValueType() != String.class;
	}

	/**
	 * Creates a {@link ConfigTextFieldModel}.
	 *
	 * @param config
	 *        The configuration item to bind to.
	 * @param property
	 *        The property of the configuration item, {@link #isFormatted(PropertyDescriptor)
	 *        formatted}.
	 */
	@SuppressWarnings("unchecked")
	public ConfigTextFieldModel(ConfigurationItem config, PropertyDescriptor property) {
		super(config, property);
		_format = (ConfigurationValueProvider<Object>) property.getValueProvider();
	}

	@Override
	public Object getValue() {
		Object value = super.getValue();
		return value == null ? "" : _format.getSpecification(value);
	}

	@Override
	public void setValue(Object value) {
		String text = value == null ? "" : value.toString();
		if (text.isEmpty()) {
			super.setValue(null);
			return;
		}
		try {
			super.setValue(_format.getValue(getProperty().getPropertyName(), text));
			clearParseError();
		} catch (ConfigurationException ex) {
			// Keep the stored value, so an entry the configuration cannot hold does not corrupt it,
			// and report the problem on the field the user is editing.
			setError(I18NConstants.ERROR_INVALID_VALUE__DETAILS.fill(ex.getMessage()));
		}
	}

	@Override
	public void onChange(ConfigurationChange change) {
		// The stored value changed elsewhere, so a previously reported parse error no longer applies.
		clearParseError();
		super.onChange(change);
	}

	private void clearParseError() {
		if (hasError()) {
			setError(null);
		}
	}
}
