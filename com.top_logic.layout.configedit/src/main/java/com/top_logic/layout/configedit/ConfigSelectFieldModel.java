/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.ListenerBinding;
import com.top_logic.layout.form.values.Value;

/**
 * A {@link ConfigFieldModel} that also implements {@link SelectFieldModel} for properties whose
 * value must come from a fixed set of options (e.g. enums).
 */
public class ConfigSelectFieldModel extends ConfigFieldModel implements SelectFieldModel {

	private List<?> _options;

	private final boolean _multiple;

	private List<SelectOptionsListener> _optionsListeners = Collections.emptyList();

	/** Detaches {@link #trackOptions(DerivedProperty)}'s subscription, or {@code null} if none. */
	private ListenerBinding _optionsBinding;

	/**
	 * Creates a {@link ConfigSelectFieldModel}.
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
		super(config, property);
		_options = options;
		_multiple = multiple;
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

	@Override
	public void setValue(Object value) {
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

	@Override
	public List<?> getOptions() {
		return _options;
	}

	@Override
	public boolean isMultiple() {
		return _multiple;
	}

	@Override
	public void setOptions(List<?> options) {
		_options = options;
		fireOptionsChanged(options);
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
