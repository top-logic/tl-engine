/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;

/**
 * {@link FieldModel} presenting the value of another {@link FieldModel} in a different form.
 *
 * <p>
 * A control that edits values of one type can edit a model whose values have another type, as
 * long as the two can be converted into each other: {@link #getValue()} converts the value of the
 * underlying model, {@link #setValue(Object)} converts the given value back before storing it in
 * the underlying model, and a change of the underlying value is reported in the converted form.
 * Everything else - editability, validation, and mandatory state - is the one of the underlying
 * model.
 * </p>
 */
public class MappedFieldModel implements FieldModel, FieldModelListener {

	private final FieldModel _delegate;

	private final Function<Object, Object> _toView;

	private final Function<Object, Object> _toModel;

	private final List<FieldModelListener> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link MappedFieldModel}.
	 *
	 * @param delegate
	 *        The model that holds the value.
	 * @param toView
	 *        Converts a value of the underlying model into the form this model presents.
	 * @param toModel
	 *        Converts a value in the form this model presents into a value of the underlying model.
	 */
	public MappedFieldModel(FieldModel delegate, Function<Object, Object> toView, Function<Object, Object> toModel) {
		_delegate = delegate;
		_toView = toView;
		_toModel = toModel;
	}

	/**
	 * The model that holds the value.
	 */
	public FieldModel getDelegate() {
		return _delegate;
	}

	@Override
	public Object getValue() {
		return _toView.apply(_delegate.getValue());
	}

	@Override
	public void setValue(Object value) {
		_delegate.setValue(_toModel.apply(value));
	}

	@Override
	public boolean isDirty() {
		return _delegate.isDirty();
	}

	@Override
	public boolean isEditable() {
		return _delegate.isEditable();
	}

	@Override
	public boolean isDisabled() {
		return _delegate.isDisabled();
	}

	@Override
	public boolean isMandatory() {
		return _delegate.isMandatory();
	}

	@Override
	public boolean isNullable() {
		return _delegate.isNullable();
	}

	@Override
	public boolean hasError() {
		return _delegate.hasError();
	}

	@Override
	public ResKey getError() {
		return _delegate.getError();
	}

	@Override
	public boolean hasWarnings() {
		return _delegate.hasWarnings();
	}

	@Override
	public List<ResKey> getWarnings() {
		return _delegate.getWarnings();
	}

	@Override
	public void setModelValidationError(ResKey error) {
		_delegate.setModelValidationError(error);
	}

	@Override
	public void setModelValidationWarnings(List<ResKey> warnings) {
		_delegate.setModelValidationWarnings(warnings);
	}

	@Override
	public void addListener(FieldModelListener listener) {
		if (_listeners.isEmpty()) {
			_delegate.addListener(this);
		}
		_listeners.add(listener);
	}

	@Override
	public void removeListener(FieldModelListener listener) {
		_listeners.remove(listener);
		if (_listeners.isEmpty()) {
			_delegate.removeListener(this);
		}
	}

	@Override
	public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
		Object oldView = _toView.apply(oldValue);
		Object newView = _toView.apply(newValue);
		for (FieldModelListener listener : _listeners) {
			listener.onValueChanged(this, oldView, newView);
		}
	}

	@Override
	public void onEditabilityChanged(FieldModel source, boolean editable) {
		for (FieldModelListener listener : _listeners) {
			listener.onEditabilityChanged(this, editable);
		}
	}

	@Override
	public void onDisabledChanged(FieldModel source, boolean disabled) {
		for (FieldModelListener listener : _listeners) {
			listener.onDisabledChanged(this, disabled);
		}
	}

	@Override
	public void onValidationChanged(FieldModel source) {
		for (FieldModelListener listener : _listeners) {
			listener.onValidationChanged(this);
		}
	}

}
