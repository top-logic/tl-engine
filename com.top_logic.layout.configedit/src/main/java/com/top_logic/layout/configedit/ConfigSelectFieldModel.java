/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationChange.Kind;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.ListenerBinding;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.edit.IdentityOptionMapping;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * A {@link ConfigFieldModel} that also implements {@link SelectFieldModel} for properties whose
 * value must come from a fixed set of options (e.g. enums).
 *
 * <p>
 * The options a property offers and the value it stores need not be the same thing: a property may
 * offer model parts and store their qualified names, or offer roles and store role names. The
 * {@link OptionMapping} declared with the property's
 * {@link com.top_logic.layout.form.values.edit.annotation.Options Options} is the translation
 * between the two, and this model applies it in both directions - the value it hands out is the
 * option (or, for a multiple property, the options) the stored value stands for, and a value set
 * from a select control is translated back into what the configuration stores. With the
 * {@link IdentityOptionMapping identity mapping} option and stored value coincide, and no
 * translation happens at all.
 * </p>
 *
 * <p>
 * A stored value none of the current options stands for has no option to be displayed as and is
 * left out of the selection, the same way the classic declarative form leaves it out (see
 * {@link com.top_logic.layout.form.values.Fields}). The configuration keeps it until the user
 * changes the selection.
 * </p>
 */
public class ConfigSelectFieldModel extends ConfigFieldModel implements SelectFieldModel {

	private List<?> _options;

	private final boolean _multiple;

	private final OptionMapping _mapping;

	private List<SelectOptionsListener> _optionsListeners = Collections.emptyList();

	/** Detaches {@link #trackOptions(DerivedProperty)}'s subscription, or {@code null} if none. */
	private ListenerBinding _optionsBinding;

	/**
	 * Creates a {@link ConfigSelectFieldModel} whose options are the values it stores.
	 *
	 * @param config
	 *        The configuration item.
	 * @param property
	 *        The property descriptor.
	 * @param options
	 *        The available options.
	 * @param multiple
	 *        Whether multiple values can be selected.
	 */
	public ConfigSelectFieldModel(ConfigurationItem config, PropertyDescriptor property, List<?> options,
			boolean multiple) {
		this(config, property, options, multiple, IdentityOptionMapping.INSTANCE);
	}

	/**
	 * Creates a {@link ConfigSelectFieldModel} translating between its options and the values the
	 * property stores.
	 *
	 * @param config
	 *        The configuration item.
	 * @param property
	 *        The property descriptor.
	 * @param options
	 *        The available options.
	 * @param multiple
	 *        Whether multiple values can be selected. The property then holds a collection, and the
	 *        mapping is applied to each of its elements.
	 * @param mapping
	 *        The translation between an option and the value the property stores for it. Must not
	 *        be {@code null}; pass {@link IdentityOptionMapping#INSTANCE} where the option is the
	 *        stored value.
	 */
	public ConfigSelectFieldModel(ConfigurationItem config, PropertyDescriptor property, List<?> options,
			boolean multiple, OptionMapping mapping) {
		super(config, property);
		_options = options;
		_multiple = multiple;
		_mapping = mapping;
		if (translates()) {
			// Both the cached value and the default value live in the option domain from here on -
			// ConfigFieldModel's constructor cached the stored value, which would otherwise never
			// compare equal to the options this model hands out (breaking isDirty()).
			Object selection = toOptions(config.value(property));
			setValueInternal(selection);
			setDefaultValue(selection);
		}
	}

	/**
	 * The translation between an option and the value the property stores for it.
	 */
	public OptionMapping getOptionMapping() {
		return _mapping;
	}

	/**
	 * Whether option and stored value differ, so that both directions have to be translated.
	 */
	private boolean translates() {
		return _mapping != IdentityOptionMapping.INSTANCE;
	}

	/**
	 * Follows the given option provider, so that options computed from other properties are
	 * recomputed while the user edits those.
	 *
	 * <p>
	 * Resolving the options once, when the field is built, leaves the user choosing from a list that
	 * describes a state the configuration has since left - and letting them pick a value the
	 * property no longer admits. {@link DerivedProperty#get(ConfigurationItem)} is that one-shot
	 * read; {@link DerivedProperty#getValue(ConfigurationItem)} is the same computation as an
	 * observable value that knows which properties it was derived from. The declarative form takes
	 * the second for this very reason, and so does this.
	 * </p>
	 *
	 * <p>
	 * Only worth calling for a property that has an option provider at all. A plain enum's constants
	 * are its options and cannot change, so nothing there needs following.
	 * </p>
	 *
	 * @param provider
	 *        The property's option provider.
	 */
	public void trackOptions(DerivedProperty<? extends Iterable<?>> provider) {
		Value<? extends Iterable<?>> value = provider.getValue(getConfig());
		_optionsBinding = value.addListener(sender -> setOptions(ConfigPropertyOptions.toList(value.get())));
	}

	/**
	 * Also drops the option subscription of {@link #trackOptions(DerivedProperty)}.
	 *
	 * <p>
	 * A field survives only until the next render cycle, while the configuration it was derived from
	 * outlives every one of them - so a subscription left behind would both keep a discarded field
	 * alive and recompute options nobody displays.
	 * </p>
	 */
	@Override
	public void detach() {
		if (_optionsBinding != null) {
			_optionsBinding.close();
			_optionsBinding = null;
		}
		super.detach();
	}

	/**
	 * The option(s) the stored value stands for, or the stored value itself where option and value
	 * coincide.
	 */
	@Override
	public Object getValue() {
		Object stored = super.getValue();
		return translates() ? toOptions(stored) : stored;
	}

	@Override
	public void setValue(Object value) {
		if (translates()) {
			setSelection(value);
			return;
		}
		// The client sends string values for select fields. If the property has a
		// ConfigurationValueProvider (e.g. for enums), use it to parse the string back to the
		// expected type.
		if (value instanceof String stringValue) {
			ConfigurationValueProvider<?> valueProvider = getProperty().getValueProvider();
			if (valueProvider != null) {
				try {
					value = valueProvider.getValue(getProperty().getPropertyName(), (CharSequence) stringValue);
				} catch (com.top_logic.basic.config.ConfigurationException ex) {
					throw new IllegalArgumentException(
						"Cannot parse value '" + stringValue + "' for property '"
							+ getProperty().getPropertyName() + "'",
						ex);
				}
			}
		}
		super.setValue(value);
	}

	/**
	 * Stores what the given option(s) stand for.
	 *
	 * <p>
	 * Written through the configuration API directly rather than through
	 * {@link ConfigFieldModel#setValue(Object)}: that method's redundant-write guard compares in
	 * the domain {@link #getValue()} answers in, which is the option domain here, while what is
	 * written is the stored one. The notification (and this model's cached value) still goes
	 * through {@link #onChange(ConfigurationChange)}.
	 * </p>
	 */
	private void setSelection(Object selected) {
		Object stored = toSelection(selected);

		if (stored == null && isTechnicallyMandatory(getProperty())) {
			// Same refusal as ConfigFieldModel#setValue(Object): a property that cannot actually
			// hold null keeps its last accepted value, reported as a field error.
			setError(I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY.fill(Labels.propertyLabel(getProperty(), false)));
			return;
		}

		// Clear a previously rejected value's error before the redundant-write guard below can
		// return early - see ConfigFieldModel#setValue(Object).
		setError(null);

		if (Objects.equals(getConfig().value(getProperty()), stored)) {
			return;
		}
		// A validation verdict describes the value it was passed - see ConfigFieldModel#setValue.
		setModelValidationError(null);
		setModelValidationWarnings(Collections.emptyList());
		getConfig().update(getProperty(), stored);
	}

	@Override
	public void onChange(ConfigurationChange change) {
		if (!translates()) {
			super.onChange(change);
			return;
		}
		if (change.getKind() == Kind.SET) {
			// The listener reports stored values, the control expects the options they stand for.
			Object oldSelection = toOptions(change.getOldValue());
			Object newSelection = toOptions(change.getNewValue());
			setValueInternal(newSelection);
			fireValueChanged(oldSelection, newSelection);
		}
	}

	/**
	 * The option(s) the given stored value stands for.
	 */
	private Object toOptions(Object stored) {
		if (_multiple) {
			List<Object> result = new ArrayList<>();
			for (Object entry : asCollection(stored)) {
				Object option = _mapping.asOption(_options, entry);
				if (option != null) {
					result.add(option);
				}
			}
			return result;
		}
		Object single = singleValue(stored);
		return single == null ? null : _mapping.asOption(_options, single);
	}

	/**
	 * The value to store for the given option(s).
	 *
	 * <p>
	 * A select control reports its selection as a list, whether or not the field takes more than
	 * one value, so a single-valued property takes the one element of that list.
	 * </p>
	 */
	private Object toSelection(Object selected) {
		if (_multiple) {
			List<Object> result = new ArrayList<>();
			for (Object option : asCollection(selected)) {
				Object stored = _mapping.toSelection(option);
				if (stored != null) {
					result.add(stored);
				}
			}
			return result;
		}
		Object single = singleValue(selected);
		return single == null ? null : _mapping.toSelection(single);
	}

	/**
	 * The given value as a {@link Collection}, treating a single value as a collection of one and
	 * no value as an empty one.
	 */
	private static Collection<?> asCollection(Object value) {
		if (value == null) {
			return Collections.emptyList();
		}
		if (value instanceof Collection<?> collection) {
			return collection;
		}
		return Collections.singletonList(value);
	}

	/**
	 * The one value the given value consists of, or {@code null} if it consists of none.
	 */
	private static Object singleValue(Object value) {
		if (value instanceof Collection<?> collection) {
			return collection.isEmpty() ? null : collection.iterator().next();
		}
		return value;
	}

	@Override
	public List<?> getOptions() {
		return _options;
	}

	@Override
	public boolean isMultiple() {
		return _multiple;
	}

	/**
	 * Also re-derives the displayed selection: which option a stored value stands for is answered
	 * against the options, so a different option list can make it a different option - or none.
	 */
	@Override
	public void setOptions(List<?> options) {
		Object oldSelection = translates() ? getValue() : null;
		_options = options;
		fireOptionsChanged(options);
		if (translates()) {
			Object newSelection = getValue();
			if (!Objects.equals(oldSelection, newSelection)) {
				setValueInternal(newSelection);
				fireValueChanged(oldSelection, newSelection);
			}
		}
	}

	@Override
	public void addOptionsListener(SelectOptionsListener listener) {
		if (_optionsListeners.isEmpty()) {
			_optionsListeners = new ArrayList<>();
		}
		_optionsListeners.add(listener);
	}

	@Override
	public void removeOptionsListener(SelectOptionsListener listener) {
		_optionsListeners.remove(listener);
	}

	private void fireOptionsChanged(List<?> newOptions) {
		SelectOptionsListener[] snapshot = _optionsListeners.toArray(new SelectOptionsListener[0]);
		for (SelectOptionsListener listener : snapshot) {
			listener.onOptionsChanged(this, newOptions);
		}
	}
}
