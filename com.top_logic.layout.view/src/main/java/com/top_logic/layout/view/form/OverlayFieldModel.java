/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link com.top_logic.layout.form.model.FieldModel} that reads/writes a single attribute of a
 * {@link TLObjectOverlay}.
 */
public class OverlayFieldModel extends AbstractFieldModel {

	private TLObjectOverlay _overlay;

	private String _attributeName;

	private TLStructuredTypePart _part;

	/**
	 * Creates a new model for the given attribute.
	 *
	 * @param overlay
	 *        The overlay to read/write values from.
	 * @param attributeName
	 *        The attribute name to resolve against the overlay's type.
	 */
	public OverlayFieldModel(TLObjectOverlay overlay, String attributeName) {
		super(resolveValue(overlay, attributeName));
		_overlay = overlay;
		_attributeName = attributeName;
		_part = resolvePart(overlay);
		setMandatory(_part.isMandatory());
	}

	@Override
	public Object getValue() {
		if (_overlay == null) {
			return null;
		}
		return _overlay.tValue(_part);
	}

	@Override
	public void setValue(Object value) {
		if (_overlay == null) {
			return;
		}
		Object oldValue = _overlay.tValue(_part);
		_overlay.tUpdate(_part, value);
		fireValueChanged(oldValue, value);
	}

	@Override
	public boolean isDirty() {
		if (_overlay == null) {
			return false;
		}
		return _overlay.isChanged(_part);
	}

	/**
	 * Re-binds this model to a new overlay (when the edited object changes).
	 *
	 * <p>
	 * Re-resolves the attribute from the new object's type and fires value changed with the new
	 * value.
	 * </p>
	 */
	public void setOverlay(TLObjectOverlay newOverlay) {
		Object oldValue = getValue();
		_overlay = newOverlay;
		if (newOverlay != null) {
			_part = resolvePart(newOverlay);
			setMandatory(_part.isMandatory());
		}
		Object newValue = getValue();
		fireValueChanged(oldValue, newValue);
	}

	/**
	 * The resolved attribute part.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	/**
	 * The attribute name.
	 */
	public String getAttributeName() {
		return _attributeName;
	}

	private TLStructuredTypePart resolvePart(TLObjectOverlay overlay) {
		TLStructuredType type = overlay.tType();
		TLStructuredTypePart part = type.getPart(_attributeName);
		if (part == null) {
			throw new IllegalArgumentException(
				"Attribute '" + _attributeName + "' not found in type '" + type + "'.");
		}
		return part;
	}

	private static Object resolveValue(TLObjectOverlay overlay, String attributeName) {
		TLStructuredType type = overlay.tType();
		TLStructuredTypePart part = type.getPart(attributeName);
		if (part == null) {
			return null;
		}
		return overlay.tValue(part);
	}
}
