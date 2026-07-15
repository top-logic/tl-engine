/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.listener.EventType.Bubble;

/**
 * Adapter wrapping a {@link SelectField} as a {@link SelectFieldModel}.
 *
 * <p>
 * Extends {@link FormFieldAdapter} to inherit value, editability, and validation delegation. Adds
 * select-specific delegation for {@link SelectFieldModel#getOptions()},
 * {@link SelectFieldModel#isMultiple()}, and options listener management.
 * </p>
 */
public class SelectFieldAdapter extends FormFieldAdapter implements SelectFieldModel {

	private final SelectField _selectField;

	private List<SelectOptionsListener> _optionsListeners = Collections.emptyList();

	/**
	 * Creates an adapter wrapping the given {@link SelectField}.
	 *
	 * @param field
	 *        The select field to wrap.
	 */
	public SelectFieldAdapter(SelectField field) {
		super(field);
		_selectField = field;
		registerOptionsListener();
	}

	private void registerOptionsListener() {
		_selectField.addListener(SelectField.OPTIONS_PROPERTY, new OptionsListener() {
			@Override
			public Bubble handleOptionsChanged(SelectField sender) {
				List<?> options = sender.getOptions();
				SelectOptionsListener[] snapshot =
					_optionsListeners.toArray(new SelectOptionsListener[0]);
				for (SelectOptionsListener l : snapshot) {
					l.onOptionsChanged(SelectFieldAdapter.this, options);
				}
				return Bubble.BUBBLE;
			}
		});
	}

	@Override
	public List<?> getOptions() {
		return _selectField.getOptions();
	}

	@Override
	public boolean isMultiple() {
		return _selectField.isMultiple();
	}

	@Override
	public void setOptions(List<?> options) {
		// SelectField uses OptionModel, not direct list setting.
		throw new UnsupportedOperationException("Set options directly on the wrapped SelectField.");
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

	/**
	 * The wrapped {@link SelectField}.
	 */
	public SelectField getSelectField() {
		return _selectField;
	}
}
