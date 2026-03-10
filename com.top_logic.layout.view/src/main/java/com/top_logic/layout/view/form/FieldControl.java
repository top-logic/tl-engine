/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Resources;

/**
 * Per-field session object that bridges a model attribute to a React input control wrapped in
 * {@link ReactFormFieldChromeControl chrome}.
 *
 * <p>
 * A {@link FieldControl} is not a {@link ReactControl} itself. It holds a reference to the
 * enclosing {@link FormControl}, resolves the attribute from the current object's type, and creates
 * the appropriate lean input control via {@link FieldControlFactory}. Value changes from the client
 * are written to the {@link TLObjectOverlay} and the chrome's dirty indicator is updated
 * accordingly.
 * </p>
 */
public class FieldControl {

	private final ReactContext _context;

	private final FormControl _form;

	private final String _attributeName;

	private final ResKey _labelOverride;

	private final boolean _forceReadonly;

	private TLStructuredTypePart _resolvedPart;

	private ReactFormFieldChromeControl _chrome;

	private ReactControl _innerControl;

	/**
	 * Creates a new {@link FieldControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param form
	 *        The enclosing form control.
	 * @param attributeName
	 *        The name of the model attribute to display.
	 * @param labelOverride
	 *        Optional label override, or {@code null} to derive the label from the model.
	 * @param forceReadonly
	 *        Whether the field should always be read-only regardless of form edit mode.
	 */
	public FieldControl(ReactContext context, FormControl form, String attributeName, ResKey labelOverride,
			boolean forceReadonly) {
		_context = context;
		_form = form;
		_attributeName = attributeName;
		_labelOverride = labelOverride;
		_forceReadonly = forceReadonly;
	}

	/**
	 * Creates the chrome-wrapped React control for this field.
	 *
	 * <p>
	 * Resolves the attribute from the current object's type, creates the appropriate inner input
	 * control, and wraps it in a {@link ReactFormFieldChromeControl}.
	 * </p>
	 *
	 * @return The chrome control, or {@code null} if no current object is available.
	 */
	public ReactFormFieldChromeControl createChromeControl() {
		TLObject current = _form.getCurrentObject();
		if (current == null) {
			// No object selected yet — create a disabled placeholder field.
			_innerControl = new ViewTextInputControl(_context, null, false);
			_chrome = new ReactFormFieldChromeControl(_context, _attributeName, false, false, null, null, null,
				false, true, _innerControl);
			return _chrome;
		}

		_resolvedPart = resolvePart(current);
		String label = resolveLabel();
		boolean editable = _form.isEditMode() && !_forceReadonly;
		Object value = current.tValue(_resolvedPart);

		_innerControl = FieldControlFactory.createFieldControl(_context, _resolvedPart, value, editable);
		setupValueCallback();

		boolean mandatory = _resolvedPart.isMandatory();
		_chrome = new ReactFormFieldChromeControl(_context, label, mandatory, false, null, null, null,
			false, true, _innerControl);

		return _chrome;
	}

	/**
	 * Refreshes the field after form state changes (e.g. entering/leaving edit mode, object
	 * change).
	 *
	 * <p>
	 * Updates the inner control's value and editability, and resets the chrome's dirty indicator
	 * based on the overlay state.
	 * </p>
	 */
	public void refresh() {
		if (_innerControl == null || _chrome == null) {
			return;
		}

		TLObject current = _form.getCurrentObject();
		if (current == null) {
			// Object gone — reset to placeholder state.
			_resolvedPart = null;
			updateInnerControl(null, false);
			_chrome.setDirty(false);
			return;
		}

		if (_resolvedPart == null) {
			// First object arrived — resolve the attribute and recreate the inner control.
			_resolvedPart = resolvePart(current);
			String label = resolveLabel();
			boolean editable = _form.isEditMode() && !_forceReadonly;
			Object value = current.tValue(_resolvedPart);

			_innerControl = FieldControlFactory.createFieldControl(_context, _resolvedPart, value, editable);
			setupValueCallback();

			_chrome.setLabel(label);
			_chrome.setRequired(_resolvedPart.isMandatory());
			_chrome.setField(_innerControl);
			_chrome.setDirty(false);
			return;
		}

		Object value = current.tValue(_resolvedPart);
		boolean editable = _form.isEditMode() && !_forceReadonly;

		updateInnerControl(value, editable);

		boolean dirty = isDirty();
		_chrome.setDirty(dirty);
	}

	/**
	 * The resolved model attribute part, or {@code null} if not yet resolved.
	 */
	public TLStructuredTypePart getResolvedPart() {
		return _resolvedPart;
	}

	/**
	 * The chrome control wrapping the inner input, or {@code null} if not yet created.
	 */
	public ReactFormFieldChromeControl getChromeControl() {
		return _chrome;
	}

	/**
	 * The inner input control, or {@code null} if not yet created.
	 */
	public ReactControl getInnerControl() {
		return _innerControl;
	}

	private TLStructuredTypePart resolvePart(TLObject obj) {
		TLStructuredTypePart part = obj.tType().getPart(_attributeName);
		if (part == null) {
			throw new IllegalArgumentException(
				"Attribute '" + _attributeName + "' not found in type " + obj.tType());
		}
		return part;
	}

	private String resolveLabel() {
		if (_labelOverride != null) {
			return Resources.getInstance().getString(_labelOverride);
		}
		return MetaLabelProvider.INSTANCE.getLabel(_resolvedPart);
	}

	private void setupValueCallback() {
		if (_innerControl instanceof ViewTextInputControl) {
			((ViewTextInputControl) _innerControl).setValueCallback(this::handleValueChange);
		} else if (_innerControl instanceof ViewCheckboxControl) {
			((ViewCheckboxControl) _innerControl).setValueCallback(this::handleValueChange);
		} else if (_innerControl instanceof ViewNumberInputControl) {
			((ViewNumberInputControl) _innerControl).setValueCallback(this::handleValueChange);
		} else if (_innerControl instanceof ViewDatePickerControl) {
			((ViewDatePickerControl) _innerControl).setValueCallback(this::handleValueChange);
		} else if (_innerControl instanceof ViewSelectControl) {
			((ViewSelectControl) _innerControl).setValueCallback(this::handleValueChange);
		} else if (_innerControl instanceof ViewColorInputControl) {
			((ViewColorInputControl) _innerControl).setValueCallback(this::handleValueChange);
		}
	}

	private void handleValueChange(Object newValue) {
		TLObjectOverlay overlay = _form.getOverlay();
		if (overlay != null) {
			overlay.tUpdate(_resolvedPart, newValue);
		}
		if (_chrome != null) {
			_chrome.setDirty(true);
		}
		_form.updateDirtyChannel();
	}

	private void updateInnerControl(Object value, boolean editable) {
		if (_innerControl instanceof ViewTextInputControl) {
			ViewTextInputControl text = (ViewTextInputControl) _innerControl;
			text.setValue(value != null ? value.toString() : null);
			text.setEditable(editable);
		} else if (_innerControl instanceof ViewCheckboxControl) {
			ViewCheckboxControl checkbox = (ViewCheckboxControl) _innerControl;
			checkbox.setValue(Boolean.TRUE.equals(value));
			checkbox.setEditable(editable);
		} else if (_innerControl instanceof ViewNumberInputControl) {
			ViewNumberInputControl number = (ViewNumberInputControl) _innerControl;
			number.setValue(value instanceof Number ? (Number) value : null);
			number.setEditable(editable);
		} else if (_innerControl instanceof ViewDatePickerControl) {
			ViewDatePickerControl date = (ViewDatePickerControl) _innerControl;
			date.setValue(value);
			date.setEditable(editable);
		} else if (_innerControl instanceof ViewSelectControl) {
			ViewSelectControl select = (ViewSelectControl) _innerControl;
			select.setValue(value);
			select.setEditable(editable);
		} else if (_innerControl instanceof ViewColorInputControl) {
			ViewColorInputControl color = (ViewColorInputControl) _innerControl;
			String hex = null;
			if (value instanceof java.awt.Color) {
				hex = ViewColorInputControl.colorToHex((java.awt.Color) value);
			} else if (value instanceof String) {
				hex = (String) value;
			}
			color.setValue(hex);
			color.setEditable(editable);
		}
	}

	private boolean isDirty() {
		TLObjectOverlay overlay = _form.getOverlay();
		if (overlay == null) {
			return false;
		}
		return overlay.isChanged(_resolvedPart);
	}
}
