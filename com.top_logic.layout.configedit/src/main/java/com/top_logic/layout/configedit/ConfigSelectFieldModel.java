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
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.model.SelectFieldModel;

/**
 * A {@link ConfigFieldModel} that also implements {@link SelectFieldModel} for properties whose
 * value must come from a fixed set of options (e.g. enums).
 */
public class ConfigSelectFieldModel extends ConfigFieldModel implements SelectFieldModel {

	private List<?> _options;

	private final boolean _multiple;

	private List<SelectOptionsListener> _optionsListeners = Collections.emptyList();

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
