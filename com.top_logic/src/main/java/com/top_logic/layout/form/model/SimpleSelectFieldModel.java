/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A standalone {@link SelectFieldModel} not backed by any configuration property.
 *
 * <p>
 * This model manages a value and a list of options independently, without binding to a
 * {@link com.top_logic.basic.config.PropertyDescriptor}. It is useful for type selectors and other
 * UI elements where the options are computed programmatically.
 * </p>
 */
public class SimpleSelectFieldModel extends AbstractFieldModel implements SelectFieldModel {

	private List<?> _options;

	private final boolean _multiple;

	private List<SelectOptionsListener> _optionsListeners = Collections.emptyList();

	/**
	 * Creates a {@link SimpleSelectFieldModel}.
	 *
	 * @param initialValue
	 *        The initial selected value.
	 * @param options
	 *        The available options.
	 * @param multiple
	 *        Whether multiple values can be selected.
	 */
	public SimpleSelectFieldModel(Object initialValue, List<?> options, boolean multiple) {
		super(initialValue);
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
