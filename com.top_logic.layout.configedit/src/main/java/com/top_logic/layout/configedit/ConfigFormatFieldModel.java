/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationChange.Kind;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.edit.Labels;

/**
 * {@link ConfigFieldModel} for a property that is edited as text but holds a typed value.
 *
 * <p>
 * The property's {@link PropertyDescriptor#getValueProvider() value provider} is the format between
 * the two: it turns the value into the text the input displays, and the entered text back into the
 * value the configuration stores. Text the format rejects becomes an
 * {@link #getInputError() input error} and leaves the configuration untouched, so the editor behaves
 * like a form field over a model attribute.
 * </p>
 */
public class ConfigFormatFieldModel extends ConfigFieldModel {

	/**
	 * Creates a {@link ConfigFormatFieldModel}.
	 *
	 * @param config
	 *        The configuration item to bind to.
	 * @param property
	 *        The property of the configuration item. Must have a
	 *        {@link PropertyDescriptor#getValueProvider() value provider}.
	 */
	public ConfigFormatFieldModel(ConfigurationItem config, PropertyDescriptor property) {
		super(config, property);
		setDefaultValue(format(config.value(property)));
	}

	@Override
	public Object getValue() {
		return format(super.getValue());
	}

	@Override
	public void setValue(Object value) {
		String text = value == null ? null : value.toString();
		if (StringServices.isEmpty(text)) {
			setError(null);
			super.setValue(null);
			return;
		}

		Object parsed;
		try {
			parsed = valueProvider().getValue(getProperty().getPropertyName(), text);
		} catch (ConfigurationException ex) {
			// Keep the value: the input control still shows the rejected text, the configuration
			// keeps the last accepted value - the same split a model attribute's field makes.
			setError(I18NConstants.ERROR_INVALID_VALUE__VALUE_PROPERTY.fill(text,
				Labels.propertyLabel(getProperty(), false)));
			return;
		}

		setError(null);
		super.setValue(parsed);
	}

	@Override
	public void onChange(ConfigurationChange change) {
		if (change.getKind() == Kind.SET) {
			// The listener reports typed values, the control expects the formatted text.
			fireValueChanged(format(change.getOldValue()), format(change.getNewValue()));
		}
	}

	/**
	 * The given value as the text its format produces, or {@code null} for no value.
	 */
	private String format(Object value) {
		if (value == null) {
			return null;
		}
		return valueProvider().getSpecification(value);
	}

	@SuppressWarnings("unchecked")
	private ConfigurationValueProvider<Object> valueProvider() {
		return (ConfigurationValueProvider<Object>) getProperty().getValueProvider();
	}

}
