/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.util.ResKey;

/**
 * Base implementation of {@link FieldModel} with listener management, dirty tracking, and
 * constraint validation.
 *
 * <p>
 * Subclasses can override value storage by overriding {@link #getValue()} and
 * {@link #setValueInternal(Object)}.
 * </p>
 */
public class AbstractFieldModel implements FieldModel {

	private Object _value;

	private Object _defaultValue;

	private boolean _editable = true;

	private boolean _mandatory;

	private ResKey _error;

	private List<ResKey> _warnings = Collections.emptyList();

	private ResKey _modelError;

	private List<ResKey> _modelWarnings = Collections.emptyList();

	private List<FieldConstraint> _constraints = Collections.emptyList();

	private boolean _revealed;

	private List<FieldModelListener> _listeners = Collections.emptyList();

	/**
	 * Creates a new model with the given initial value.
	 *
	 * @param initialValue
	 *        The initial value, also used as default for dirty tracking.
	 */
	public AbstractFieldModel(Object initialValue) {
		_value = initialValue;
		_defaultValue = initialValue;
	}

	@Override
	public Object getValue() {
		return _value;
	}

	@Override
	public void setValue(Object value) {
		Object oldValue = _value;
		if (Objects.equals(oldValue, value)) {
			return;
		}
		_value = value;
		fireValueChanged(oldValue, value);
	}

	/**
	 * Internal setter that does not fire listeners. For use by subclasses.
	 */
	protected void setValueInternal(Object value) {
		_value = value;
	}

	@Override
	public boolean isDirty() {
		return !Objects.equals(_value, _defaultValue);
	}

	@Override
	public boolean isEditable() {
		return _editable;
	}

	/**
	 * Sets the editability state.
	 *
	 * <p>
	 * Fires {@link FieldModelListener#onEditabilityChanged(FieldModel, boolean)} if the state
	 * changes.
	 * </p>
	 */
	public void setEditable(boolean editable) {
		if (_editable == editable) {
			return;
		}
		_editable = editable;
		fireEditabilityChanged(editable);
		// Validation visibility depends on editable state.
		fireValidationChanged();
	}

	@Override
	public boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * Sets the mandatory flag.
	 *
	 * <p>
	 * Fires {@link FieldModelListener#onValidationChanged(FieldModel)}.
	 * </p>
	 */
	public void setMandatory(boolean mandatory) {
		if (_mandatory == mandatory) {
			return;
		}
		_mandatory = mandatory;
		fireValidationChanged();
	}

	@Override
	public boolean hasError() {
		if (_error != null) {
			return true;
		}
		// Model-level errors only visible when editable and revealed.
		return _editable && _revealed && _modelError != null;
	}

	@Override
	public ResKey getError() {
		if (_error != null) {
			return _error;
		}
		return (_editable && _revealed) ? _modelError : null;
	}

	@Override
	public boolean hasWarnings() {
		if (!_warnings.isEmpty()) {
			return true;
		}
		// Model-level warnings only visible when editable and revealed.
		return _editable && _revealed && !_modelWarnings.isEmpty();
	}

	@Override
	public List<ResKey> getWarnings() {
		List<ResKey> visibleModelWarnings = (_editable && _revealed) ? _modelWarnings : Collections.emptyList();
		if (_warnings.isEmpty()) return visibleModelWarnings;
		if (visibleModelWarnings.isEmpty()) return _warnings;
		List<ResKey> combined = new ArrayList<>(_warnings);
		combined.addAll(visibleModelWarnings);
		return combined;
	}

	/**
	 * Whether model-level validation errors are visible.
	 *
	 * <p>
	 * Initially {@code false} for create forms. Set to {@code true} when the user interacts with
	 * the field (blur) or attempts to submit.
	 * </p>
	 */
	public boolean isRevealed() {
		return _revealed;
	}

	/**
	 * Makes model-level validation errors visible and fires validation changed.
	 */
	public void setRevealed(boolean revealed) {
		if (_revealed != revealed) {
			_revealed = revealed;
			fireValidationChanged();
		}
	}

	/**
	 * Sets an error on this model directly (e.g. for parse errors).
	 *
	 * <p>
	 * Fires {@link FieldModelListener#onValidationChanged(FieldModel)}.
	 * </p>
	 *
	 * @param error
	 *        The error key, or {@code null} to clear.
	 */
	public void setError(ResKey error) {
		if (Objects.equals(_error, error)) {
			return;
		}
		_error = error;
		fireValidationChanged();
	}

	@Override
	public void setModelValidationError(ResKey error) {
		if (!Objects.equals(_modelError, error)) {
			_modelError = error;
			fireValidationChanged();
		}
	}

	@Override
	public void setModelValidationWarnings(List<ResKey> warnings) {
		List<ResKey> newWarnings = warnings.isEmpty() ? Collections.emptyList() : List.copyOf(warnings);
		if (!_modelWarnings.equals(newWarnings)) {
			_modelWarnings = newWarnings;
			fireValidationChanged();
		}
	}

	@Override
	public void validate() {
		ResKey firstError = null;
		List<ResKey> warnings = Collections.emptyList();

		for (FieldConstraint constraint : _constraints) {
			ResKey result = constraint.check(_value);
			if (result != null) {
				if (firstError == null) {
					firstError = result;
				}
			}
		}

		boolean changed = !Objects.equals(_error, firstError) || !_warnings.equals(warnings);
		_error = firstError;
		_warnings = warnings;
		if (changed) {
			fireValidationChanged();
		}
	}

	@Override
	public void addConstraint(FieldConstraint constraint) {
		if (_constraints.isEmpty()) {
			_constraints = new ArrayList<>();
		}
		_constraints.add(constraint);
	}

	@Override
	public void removeConstraint(FieldConstraint constraint) {
		_constraints.remove(constraint);
	}

	@Override
	public void addListener(FieldModelListener listener) {
		if (_listeners.isEmpty()) {
			_listeners = new ArrayList<>();
		}
		_listeners.add(listener);
	}

	@Override
	public void removeListener(FieldModelListener listener) {
		_listeners.remove(listener);
	}

	/**
	 * Resets the default value used for dirty tracking.
	 */
	public void setDefaultValue(Object defaultValue) {
		_defaultValue = defaultValue;
	}

	/**
	 * Fires {@link FieldModelListener#onValueChanged(FieldModel, Object, Object)}.
	 */
	protected void fireValueChanged(Object oldValue, Object newValue) {
		FieldModelListener[] snapshot = _listeners.toArray(new FieldModelListener[0]);
		for (FieldModelListener listener : snapshot) {
			listener.onValueChanged(this, oldValue, newValue);
		}
	}

	/**
	 * Fires {@link FieldModelListener#onEditabilityChanged(FieldModel, boolean)}.
	 */
	protected void fireEditabilityChanged(boolean editable) {
		FieldModelListener[] snapshot = _listeners.toArray(new FieldModelListener[0]);
		for (FieldModelListener listener : snapshot) {
			listener.onEditabilityChanged(this, editable);
		}
	}

	/**
	 * Fires {@link FieldModelListener#onValidationChanged(FieldModel)}.
	 */
	protected void fireValidationChanged() {
		FieldModelListener[] snapshot = _listeners.toArray(new FieldModelListener[0]);
		for (FieldModelListener listener : snapshot) {
			listener.onValidationChanged(this);
		}
	}
}
