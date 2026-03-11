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
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.HasErrorChanged;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.MandatoryChangedListener;
import com.top_logic.layout.form.ValueListener;

/**
 * Adapter that wraps a {@link FormField} as a {@link FieldModel}.
 *
 * <p>
 * Translates {@link FormField} property listeners into {@link FieldModelListener} calls. Also
 * exposes display properties (label, tooltip, visibility) that are not part of {@link FieldModel}
 * but needed by controls in the traditional layout system.
 * </p>
 */
public class FormFieldAdapter implements FieldModel {

	private final FormField _field;

	private List<FieldModelListener> _listeners = Collections.emptyList();

	/**
	 * Creates an adapter wrapping the given form field.
	 */
	public FormFieldAdapter(FormField field) {
		_field = field;
		registerFieldListeners();
	}

	@Override
	public Object getValue() {
		return _field.getValue();
	}

	@Override
	public void setValue(Object value) {
		_field.setValue(value);
	}

	@Override
	public boolean isDirty() {
		return _field.isChanged();
	}

	@Override
	public boolean isEditable() {
		return !_field.isImmutable() && !_field.isDisabled();
	}

	@Override
	public boolean isMandatory() {
		return _field.isMandatory();
	}

	@Override
	public boolean hasError() {
		return _field.hasError();
	}

	@Override
	public ResKey getError() {
		// FormField.getError() returns an already-resolved String.
		// Wrap in ResKey.text() for the FieldModel API.
		return _field.hasError() ? ResKey.text(_field.getError()) : null;
	}

	@Override
	public boolean hasWarnings() {
		return _field.hasWarnings();
	}

	@Override
	public List<ResKey> getWarnings() {
		if (!_field.hasWarnings()) {
			return Collections.emptyList();
		}
		List<String> fieldWarnings = _field.getWarnings();
		List<ResKey> result = new ArrayList<>(fieldWarnings.size());
		for (String w : fieldWarnings) {
			result.add(ResKey.text(w));
		}
		return result;
	}

	@Override
	public void validate() {
		_field.check();
	}

	@Override
	public void addConstraint(FieldConstraint constraint) {
		throw new UnsupportedOperationException(
			"Add constraints directly to the wrapped FormField.");
	}

	@Override
	public void removeConstraint(FieldConstraint constraint) {
		throw new UnsupportedOperationException(
			"Remove constraints directly from the wrapped FormField.");
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

	// --- Display properties (not part of FieldModel) ---

	/**
	 * The field label.
	 */
	public String getLabel() {
		return _field.getLabel();
	}

	/**
	 * The field tooltip.
	 */
	public String getTooltip() {
		return _field.getTooltip();
	}

	/**
	 * Whether the field is visible.
	 */
	public boolean isVisible() {
		return _field.isVisible();
	}

	/**
	 * The wrapped {@link FormField}.
	 */
	public FormField getFormField() {
		return _field;
	}

	private void registerFieldListeners() {
		_field.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				for (FieldModelListener listener : _listeners) {
					listener.onValueChanged(FormFieldAdapter.this, oldValue, newValue);
				}
			}
		});

		_field.addListener(FormField.HAS_ERROR_PROPERTY, new HasErrorChanged() {
			@Override
			public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
				fireValidationChanged();
				return Bubble.BUBBLE;
			}
		});

		_field.addListener(FormField.MANDATORY_PROPERTY, new MandatoryChangedListener() {
			@Override
			public Bubble handleMandatoryChanged(FormField sender, Boolean oldMandatory, Boolean newMandatory) {
				fireValidationChanged();
				return Bubble.BUBBLE;
			}
		});

		_field.addListener(FormMember.IMMUTABLE_PROPERTY, new ImmutablePropertyListener() {
			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				fireEditabilityChanged();
				return Bubble.BUBBLE;
			}
		});

		_field.addListener(FormMember.DISABLED_PROPERTY, new DisabledPropertyListener() {
			@Override
			public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				fireEditabilityChanged();
				return Bubble.BUBBLE;
			}
		});
	}

	private void fireEditabilityChanged() {
		boolean editable = isEditable();
		for (FieldModelListener listener : _listeners) {
			listener.onEditabilityChanged(this, editable);
		}
	}

	private void fireValidationChanged() {
		for (FieldModelListener listener : _listeners) {
			listener.onValidationChanged(this);
		}
	}
}
