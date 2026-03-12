/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Objects;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link com.top_logic.layout.form.model.FieldModel} that reads and writes a single attribute of a
 * {@link TLObject}.
 *
 * <p>
 * The object may be a base persistent object (view mode) or a {@link TLObjectOverlay} (edit mode).
 * Dirty tracking is handled by the inherited {@link AbstractFieldModel} logic which compares the
 * current value to the default value.
 * </p>
 */
public class AttributeFieldModel extends AbstractFieldModel {

	private TLObject _object;

	private TLStructuredTypePart _part;

	/**
	 * Creates a new model for the given attribute.
	 *
	 * @param object
	 *        The object to read/write values from.
	 * @param part
	 *        The attribute to bind to.
	 */
	public AttributeFieldModel(TLObject object, TLStructuredTypePart part) {
		super(object.tValue(part));
		_object = object;
		_part = part;
		setMandatory(part.isMandatory());
	}

	@Override
	public Object getValue() {
		return _object.tValue(_part);
	}

	@Override
	public void setValue(Object value) {
		Object oldValue = getValue();
		if (Objects.equals(oldValue, value)) {
			return;
		}
		_object.tUpdate(_part, value);
		setValueInternal(value);
		fireValueChanged(oldValue, value);
	}

	/**
	 * Rebinds this model to a different object.
	 *
	 * <p>
	 * Called when the form switches objects or transitions between view and edit mode. Re-resolves
	 * the attribute from the new object's type, updates the default value (clearing dirty state),
	 * and fires value changed if the value differs.
	 * </p>
	 *
	 * @param newObject
	 *        The new object to bind to.
	 */
	public void setObject(TLObject newObject) {
		Object oldValue = getValue();
		_object = newObject;
		_part = resolvePart(newObject);
		setMandatory(_part.isMandatory());
		Object newValue = getValue();
		setDefaultValue(newValue);
		setValueInternal(newValue);
		if (!Objects.equals(oldValue, newValue)) {
			fireValueChanged(oldValue, newValue);
		}
	}

	/**
	 * The resolved attribute part.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	private TLStructuredTypePart resolvePart(TLObject obj) {
		TLStructuredType type = obj.tType();
		TLStructuredTypePart part = type.getPart(_part.getName());
		if (part == null) {
			throw new IllegalArgumentException(
				"Attribute '" + _part.getName() + "' not found in type '" + type + "'.");
		}
		return part;
	}
}
