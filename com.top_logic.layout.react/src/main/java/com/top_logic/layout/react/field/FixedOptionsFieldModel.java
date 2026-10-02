/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.model.form.ValidationResult;

/**
 * A {@link FieldModel} offered as a choice among a fixed list of options.
 *
 * <p>
 * The value, its editability, its validation and the notification of their changes are those of the
 * {@link #getField() wrapped field}: a select control editing this model reads from and writes to
 * that field. The model adds the {@link #getOptions() options} the value is chosen from, and it may
 * state on its own whether the choice is {@link #isMandatory() mandatory}: a choice between values
 * of which one is always set offers no choice of no value, even where the field itself would accept
 * none.
 * </p>
 *
 * <p>
 * A select control writes its selection as a list. Where this model chooses a single value, the
 * wrapped field receives the chosen option itself, or {@code null} for an empty selection.
 * </p>
 *
 * <p>
 * Listeners of this model are notified with this model as the source of a change, so that what they
 * read from the source - the mandatory state in particular - is what the choice states.
 * </p>
 */
public class FixedOptionsFieldModel implements SelectFieldModel {

	private final FieldModel _field;

	private final boolean _multiple;

	private final Boolean _mandatory;

	private List<?> _options;

	private final List<FieldModelListener> _listeners = new CopyOnWriteArrayList<>();

	private final List<SelectOptionsListener> _optionsListeners = new CopyOnWriteArrayList<>();

	/**
	 * Forwards the changes of {@link #_field} to the {@link #_listeners} of this model, registered
	 * with the field while this model has listeners.
	 */
	private final FieldModelListener _forward = new FieldModelListener() {
		@Override
		public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
			for (FieldModelListener listener : _listeners) {
				listener.onValueChanged(FixedOptionsFieldModel.this, oldValue, newValue);
			}
		}

		@Override
		public void onEditabilityChanged(FieldModel source, boolean editable) {
			for (FieldModelListener listener : _listeners) {
				listener.onEditabilityChanged(FixedOptionsFieldModel.this, editable);
			}
		}

		@Override
		public void onDisabledChanged(FieldModel source, boolean disabled) {
			for (FieldModelListener listener : _listeners) {
				listener.onDisabledChanged(FixedOptionsFieldModel.this, disabled);
			}
		}

		@Override
		public void onValidationChanged(FieldModel source) {
			for (FieldModelListener listener : _listeners) {
				listener.onValidationChanged(FixedOptionsFieldModel.this);
			}
		}
	};

	/**
	 * Creates a {@link FixedOptionsFieldModel} choosing a single value, mandatory where the field
	 * is.
	 *
	 * @param field
	 *        The field holding the chosen value, see {@link #getField()}.
	 * @param options
	 *        The values to choose from.
	 */
	public FixedOptionsFieldModel(FieldModel field, List<?> options) {
		this(field, options, false, null);
	}

	/**
	 * Creates a {@link FixedOptionsFieldModel}.
	 *
	 * @param field
	 *        The field holding the chosen value, see {@link #getField()}.
	 * @param options
	 *        The values to choose from.
	 * @param multiple
	 *        Whether several options are chosen, the field then holding a collection of them.
	 * @param mandatory
	 *        Whether an option must be chosen, see {@link #isMandatory()}; {@code null} to follow
	 *        the field.
	 */
	public FixedOptionsFieldModel(FieldModel field, List<?> options, boolean multiple, Boolean mandatory) {
		_field = field;
		_options = options;
		_multiple = multiple;
		_mandatory = mandatory;
	}

	/**
	 * The field holding the chosen value.
	 */
	public FieldModel getField() {
		return _field;
	}

	@Override
	public List<?> getOptions() {
		return _options;
	}

	@Override
	public void setOptions(List<?> options) {
		_options = options;
		for (SelectOptionsListener listener : _optionsListeners) {
			listener.onOptionsChanged(this, options);
		}
	}

	@Override
	public boolean isMultiple() {
		return _multiple;
	}

	@Override
	public void addOptionsListener(SelectOptionsListener listener) {
		_optionsListeners.add(listener);
	}

	@Override
	public void removeOptionsListener(SelectOptionsListener listener) {
		_optionsListeners.remove(listener);
	}

	@Override
	public Object getValue() {
		return _field.getValue();
	}

	/**
	 * Stores the given value in the {@link #getField() field}.
	 *
	 * <p>
	 * Where a single value is chosen, a collection given here stands for a selection: the field
	 * receives its first element, or {@code null} if it is empty.
	 * </p>
	 */
	@Override
	public void setValue(Object value) {
		_field.setValue(_multiple ? value : single(value));
	}

	private static Object single(Object value) {
		if (value instanceof Collection<?> selection) {
			return selection.isEmpty() ? null : selection.iterator().next();
		}
		return value;
	}

	@Override
	public boolean isDirty() {
		return _field.isDirty();
	}

	@Override
	public boolean isEditable() {
		return _field.isEditable();
	}

	@Override
	public boolean isDisabled() {
		return _field.isDisabled();
	}

	/**
	 * Whether an option must be chosen: what the choice states where it states anything, what the
	 * {@link #getField() field} says otherwise.
	 */
	@Override
	public boolean isMandatory() {
		return _mandatory == null ? _field.isMandatory() : _mandatory.booleanValue();
	}

	/**
	 * Whether no option may be chosen: never where the choice is stated to be
	 * {@link #isMandatory() mandatory}, what the {@link #getField() field} says otherwise.
	 */
	@Override
	public boolean isNullable() {
		return _mandatory == null ? _field.isNullable() : !_mandatory.booleanValue();
	}

	@Override
	public boolean hasError() {
		return _field.hasError();
	}

	@Override
	public ResKey getError() {
		return _field.getError();
	}

	@Override
	public boolean hasWarnings() {
		return _field.hasWarnings();
	}

	@Override
	public List<ResKey> getWarnings() {
		return _field.getWarnings();
	}

	@Override
	public void setModelValidationError(ResKey error) {
		_field.setModelValidationError(error);
	}

	@Override
	public void setModelValidationWarnings(List<ResKey> warnings) {
		_field.setModelValidationWarnings(warnings);
	}

	@Override
	public void applyValidationResult(ValidationResult result) {
		_field.applyValidationResult(result);
	}

	@Override
	public void addListener(FieldModelListener listener) {
		if (_listeners.isEmpty()) {
			_field.addListener(_forward);
		}
		_listeners.add(listener);
	}

	@Override
	public void removeListener(FieldModelListener listener) {
		if (_listeners.remove(listener) && _listeners.isEmpty()) {
			_field.removeListener(_forward);
		}
	}

}
