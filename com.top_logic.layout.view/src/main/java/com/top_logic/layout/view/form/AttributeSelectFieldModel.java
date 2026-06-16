/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.col.Sink;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link AttributeFieldModel} for attributes whose value is chosen from a set of options (enums,
 * references, option-bearing datatypes, or attributes with a configured options provider).
 *
 * <p>
 * The value is adapted between the attribute's single/multiple representation and the {@code List}
 * representation expected by the select control: {@link #getValue()} exposes a {@link List} for
 * multi-valued attributes (and the raw value for single-valued ones, which the control wraps), while
 * {@link #setValue(Object)} accepts the control's selection list and stores it back as the proper
 * single value or collection.
 * </p>
 *
 * <p>
 * Options are recomputed whenever a dependency of the (TL-Script) options expression changes in the
 * form, so that options depending on other fields stay up to date.
 * </p>
 */
public class AttributeSelectFieldModel extends AttributeFieldModel implements SelectFieldModel {

	private final FormControl _formControl;

	private final boolean _multiple;

	private List<?> _options = Collections.emptyList();

	private List<SelectOptionsListener> _optionsListeners = Collections.emptyList();

	private Set<TLStructuredTypePart> _dependencies = Collections.emptySet();

	private final FormControl.FieldChangeListener _fieldChangeListener = this::onFieldChanged;

	/**
	 * Creates a new {@link AttributeSelectFieldModel} and computes its initial options.
	 *
	 * @param object
	 *        See {@link AttributeFieldModel#AttributeFieldModel(TLObject, TLStructuredTypePart)}.
	 * @param part
	 *        See {@link AttributeFieldModel#AttributeFieldModel(TLObject, TLStructuredTypePart)}.
	 * @param formControl
	 *        The enclosing form, providing the overlay lookup and field-change notifications.
	 */
	public AttributeSelectFieldModel(TLObject object, TLStructuredTypePart part, FormControl formControl) {
		super(object, part);
		_formControl = formControl;
		_multiple = part.isMultiple();
		recomputeOptions();
		_formControl.addFieldChangeListener(_fieldChangeListener);
	}

	@Override
	public boolean isMultiple() {
		return _multiple;
	}

	@Override
	public List<?> getOptions() {
		return _options;
	}

	@Override
	public void setOptions(List<?> options) {
		_options = options == null ? Collections.emptyList() : options;
		for (SelectOptionsListener listener : _optionsListeners) {
			listener.onOptionsChanged(this, _options);
		}
	}

	@Override
	public void addOptionsListener(SelectOptionsListener listener) {
		if (_optionsListeners.isEmpty()) {
			_optionsListeners = new ArrayList<>(2);
		}
		_optionsListeners.add(listener);
	}

	@Override
	public void removeOptionsListener(SelectOptionsListener listener) {
		_optionsListeners.remove(listener);
	}

	@Override
	public Object getValue() {
		Object raw = super.getValue();
		if (_multiple) {
			if (raw instanceof Collection) {
				return new ArrayList<>((Collection<?>) raw);
			}
			return raw == null ? Collections.emptyList() : Collections.singletonList(raw);
		}
		return raw;
	}

	@Override
	public void setValue(Object selection) {
		Object newValue = toAttributeValue(selection);
		Object oldValue = super.getValue();
		if (Objects.equals(oldValue, newValue)) {
			return;
		}
		getObject().tUpdate(getPart(), newValue);
		setValueInternal(newValue);
		fireValueChanged(oldValue, newValue);
	}

	private Object toAttributeValue(Object selection) {
		List<?> values;
		if (selection instanceof List) {
			values = (List<?>) selection;
		} else if (selection == null) {
			values = Collections.emptyList();
		} else {
			values = Collections.singletonList(selection);
		}
		if (_multiple) {
			return new ArrayList<>(values);
		}
		return values.isEmpty() ? null : values.get(0);
	}

	@Override
	public void setObject(TLObject newObject) {
		super.setObject(newObject);
		recomputeOptions();
	}

	private void onFieldChanged(TLStructuredTypePart changedPart) {
		if (changedPart == getPart()) {
			// Changing this field does not affect its own options.
			return;
		}
		if (_dependencies.contains(changedPart)) {
			recomputeOptions();
		}
	}

	private void recomputeOptions() {
		Set<TLStructuredTypePart> dependencies = new HashSet<>();
		Sink<Pointer> sink = pointer -> dependencies.add(pointer.attribute());
		OverlayLookup overlays = _formControl.getValidationModel();
		List<?> options = AttributeOptions.optionsFor(getObject(), getPart(), overlays, sink);
		_dependencies = dependencies;
		setOptions(options);
	}

	/**
	 * Detaches this model from the form's field-change notifications.
	 */
	public void dispose() {
		_formControl.removeFieldChangeListener(_fieldChangeListener);
	}

}
