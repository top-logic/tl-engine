/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Objects;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link com.top_logic.layout.form.model.FieldModel} that reads and writes a single attribute of a
 * {@link TLObject}.
 *
 * <p>
 * The object may be a base persistent object (view mode) or a {@link TLObjectOverlay} (edit mode).
 * Dirty tracking is handled by the inherited {@link BoundFieldModel} logic which compares the
 * current value to the default value.
 * </p>
 */
public class AttributeFieldModel extends BoundFieldModel {

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
	protected Object readValue() {
		return _object.tValue(_part);
	}

	@Override
	protected void writeValue(Object value) {
		_object.tUpdate(_part, value);
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
		Object oldValue = getCachedValue();
		boolean hadInputError = getInputError() != null;
		_object = newObject;
		_part = resolvePart(newObject);
		setMandatory(_part.isMandatory());
		Object newValue = getValue();
		setDefaultValue(newValue);
		setValueInternal(newValue);

		// Reset validation state from previous object. This includes an input error: the raw text
		// it rejected belongs to an edit that is over (saved, cancelled, or on another object).
		setRevealed(false);
		setError(null);
		setModelValidationError(null);
		setModelValidationWarnings(java.util.Collections.emptyList());

		// A rejected raw input left the value unchanged, so only the input control still shows it.
		// Push the value even though it did not change, to replace that text.
		if (hadInputError || !Objects.equals(oldValue, newValue)) {
			fireValueChanged(oldValue, newValue);
		}
	}

	/**
	 * The resolved attribute part.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	/**
	 * The object this model currently reads from and writes to (base object or overlay).
	 *
	 * <p>
	 * Public because a field control may have to look at what it is editing, not only at its value:
	 * {@link AnnotationsFieldControlProvider} decides from the element's kind which surroundings to
	 * build for an annotation.
	 * </p>
	 */
	public TLObject getObject() {
		return _object;
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
