/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.TracingScope;

/**
 * {@link com.top_logic.layout.form.model.FieldModel} that reads and writes a single attribute of a
 * {@link TLObject}.
 *
 * <p>
 * The object may be a base persistent object (view mode) or a {@link TLObjectOverlay} (edit mode).
 * Dirty tracking is handled by the inherited {@link BoundFieldModel} logic which compares the
 * current value to the default value.
 * </p>
 *
 * <p>
 * A {@link TLStructuredTypePart#isDerived() derived} attribute follows the edits of the form it is
 * displayed in: while the form is edited, its value is computed in a {@link TracingScope} over the
 * form's overlays, and the attributes read by that computation are recorded. When the field of one of those attributes changes, the value is
 * computed again and {@link #followObject() taken} as the field's value and as the value it is
 * dirty against. A derived field is therefore never dirty, while its value change is announced to
 * its listeners like any other, so that fields depending on the derived attribute follow in turn.
 * </p>
 */
public class AttributeFieldModel extends BoundFieldModel {

	private TLObject _object;

	private TLStructuredTypePart _part;

	private final FormControl _form;

	private Set<TLStructuredTypePart> _dependencies = Collections.emptySet();

	private final FormControl.FieldChangeListener _dependencyListener = this::onFieldChanged;

	private boolean _listening;

	/**
	 * Creates a new model for the given attribute, outside of a form.
	 *
	 * @param object
	 *        The object to read/write values from.
	 * @param part
	 *        The attribute to bind to.
	 */
	public AttributeFieldModel(TLObject object, TLStructuredTypePart part) {
		this(object, part, null);
	}

	/**
	 * Creates a new model for the given attribute, displayed in the given form.
	 *
	 * @param object
	 *        The object to read/write values from.
	 * @param part
	 *        The attribute to bind to.
	 * @param form
	 *        The form the field is displayed in, providing the overlays a derived value is computed
	 *        over and the notifications about changed fields. {@code null} for a field outside of
	 *        a form.
	 */
	public AttributeFieldModel(TLObject object, TLStructuredTypePart part, FormControl form) {
		super(object.tValue(part));
		_object = object;
		_part = part;
		_form = form;
		setMandatory(part.isMandatory());
		if (isTraced()) {
			Object value = readValue();
			setDefaultValue(value);
			setValueInternal(value);
		}
		updateDependencyListener();
	}

	/**
	 * The value of the attribute, computed over the form's overlays for a derived attribute of a
	 * form being edited.
	 *
	 * <p>
	 * Reading a computed value records the attributes the computation reads, whose changes make
	 * the field compute its value again.
	 * </p>
	 */
	@Override
	protected Object readValue() {
		OverlayLookup overlays = _form == null ? null : _form.getValidationModel();
		if (!isTraced() || overlays == null) {
			_dependencies = Collections.emptySet();
			return _object.tValue(_part);
		}
		Set<TLStructuredTypePart> dependencies = new HashSet<>();
		Object value;
		try (TracingScope scope = TracingScope.open(pointer -> dependencies.add(pointer.attribute()), overlays)) {
			value = _object.tValue(_part);
		}
		_dependencies = dependencies;
		return value;
	}

	/**
	 * Whether the value of the attribute is computed and the attributes the computation reads are
	 * traced, which is the case for a {@link TLStructuredTypePart#isDerived() derived} attribute.
	 */
	private boolean isTraced() {
		return _part.isDerived();
	}

	/**
	 * Registers for the changes of the form's fields while the attribute is
	 * {@link #isTraced() traced}, and deregisters otherwise.
	 */
	private void updateDependencyListener() {
		boolean listen = _form != null && isTraced();
		if (listen == _listening) {
			return;
		}
		if (listen) {
			_form.addFieldChangeListener(_dependencyListener);
		} else {
			_form.removeFieldChangeListener(_dependencyListener);
		}
		_listening = listen;
	}

	/**
	 * Computes the value again when an attribute the last computation read has changed.
	 */
	private void onFieldChanged(TLStructuredTypePart changedPart) {
		if (changedPart == _part) {
			// The own change of a derived field is no reason to compute it again.
			return;
		}
		if (_dependencies.contains(changedPart)) {
			followObject();
		}
	}

	@Override
	protected void writeValue(Object value) {
		_object.tUpdate(_part, value);
	}

	@Override
	protected void discardWrittenValue() {
		if (_object instanceof TLObjectOverlay overlay) {
			overlay.revert(_part);
		}
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
		updateDependencyListener();
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
	 * Detaches this model from the form's field-change notifications.
	 */
	@Override
	public void dispose() {
		if (_listening) {
			_form.removeFieldChangeListener(_dependencyListener);
			_listening = false;
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
	@Override
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
