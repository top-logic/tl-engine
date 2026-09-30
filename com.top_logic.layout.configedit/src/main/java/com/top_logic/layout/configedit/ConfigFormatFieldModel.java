/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Objects;

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
 *
 * <p>
 * An empty/cleared entry is parsed like any other text - through the value provider's own
 * {@code getValueEmpty(String)} handling - rather than substituted by {@code null}: most
 * providers answer {@code null} there anyway, but a collection-typed provider (e.g.
 * {@code CommaSeparatedStrings} for a {@code List<String>} property) answers its own empty
 * value (an empty collection), which is the value such a property actually holds for "no
 * entries" - {@code null} would misrepresent that as "no value at all", which a property that
 * cannot actually be {@code null} (see {@link #isTechnicallyMandatory(PropertyDescriptor)}) does
 * not have in the first place.
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
		// Both the cached value and the default value live in the text domain from here on -
		// ConfigFieldModel's constructor cached the raw typed value, which would otherwise never
		// compare equal to the formatted text this class hands out (breaking isDirty()).
		String text = format(config.value(property));
		setValueInternal(text);
		setDefaultValue(text);
	}

	@Override
	public Object getValue() {
		return format(super.getValue());
	}

	@Override
	public void setValue(Object value) {
		String text = value == null ? null : value.toString();
		if (StringServices.isEmpty(text)) {
			text = null;
		}

		if (Objects.equals(text, getValue())) {
			// The displayed text already matches the configuration's current value: nothing to
			// parse or write. (ConfigFieldModel's own redundant-write guard cannot see this,
			// since it compares in the typed domain against this class's formatted getValue().)
			//
			// Still clear a previously rejected value's error first: re-entering the value the
			// configuration already holds is itself well-formed input and must not leave a stale
			// error on display next to a now-valid value.
			setError(null);
			return;
		}

		Object parsed;
		try {
			// An empty/cleared entry is routed through the property's own value provider too
			// (as "" - the provider's own empty-value handling, ConfigurationValueProvider#getValue
			// dispatching to its getValueEmpty(String)), not hard-coded to null: for most
			// providers that already answers null (e.g. a plain @Format text, or a Date), the
			// same value null previously stood in for directly. But for a collection-typed
			// provider (e.g. CommaSeparatedStrings for a List<String> property, as used by
			// StringEndingFilter.Config#getAllowedEndings()) it answers the empty collection
			// instead - the value's own, correct representation of "no entries" (see
			// ListConfigValueProvider#getValueEmpty) - which null would misrepresent as "no
			// value at all", a different thing for a property that cannot actually be null (see
			// isTechnicallyMandatory(PropertyDescriptor)).
			parsed = valueProvider().getValue(getProperty().getPropertyName(),
				text == null ? StringServices.EMPTY_STRING : text);
		} catch (ConfigurationException ex) {
			// Keep the value: the input control still shows the rejected text, the configuration
			// keeps the last accepted value - the same split a model attribute's field makes.
			setError(I18NConstants.ERROR_INVALID_VALUE__VALUE_PROPERTY.fill(text,
				Labels.propertyLabel(getProperty(), false)));
			return;
		}

		if (parsed == null && isTechnicallyMandatory(getProperty())) {
			// Same refusal as ConfigFieldModel#setValue(Object): a property that cannot actually
			// hold null must keep its last accepted value, reported as a field error instead of
			// reaching ConfigurationItem#update(PropertyDescriptor, Object), which rejects null
			// for e.g. a primitive property with a technical exception.
			setError(I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY.fill(Labels.propertyLabel(getProperty(), false)));
			return;
		}

		setError(null);
		// A validation verdict describes the value it was passed - see ConfigFieldModel#setValue.
		setModelValidationError(null);
		// Write through the configuration API directly (not ConfigFieldModel#setValue): its
		// redundant-write guard operates in the typed domain and would never fire for this
		// class, and the value change notification (and this model's cached value) still needs
		// to go through onChange() below.
		getConfig().update(getProperty(), parsed);
	}

	@Override
	public void onChange(ConfigurationChange change) {
		if (change.getKind() == Kind.SET) {
			// The listener reports typed values, the control expects the formatted text.
			String oldText = format(change.getOldValue());
			String newText = format(change.getNewValue());
			setValueInternal(newText);
			fireValueChanged(oldText, newText);
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
