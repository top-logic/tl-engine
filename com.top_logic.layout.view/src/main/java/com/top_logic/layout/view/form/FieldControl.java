/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Resources;

/**
 * Per-field session object that bridges a model attribute to a React input control wrapped in
 * {@link ReactFormFieldChromeControl chrome}.
 *
 * <p>
 * Creates an {@link OverlayFieldModel} for the attribute and passes it to the control. The model
 * handles two-way value sync. Edit mode and object switching are driven by
 * {@link #setEditMode(boolean)} and {@link #setOverlay(TLObjectOverlay)}.
 * </p>
 */
public class FieldControl {

	private final ReactContext _context;

	private final FormControl _form;

	private final String _attributeName;

	private final ResKey _labelOverride;

	private final boolean _forceReadonly;

	private OverlayFieldModel _model;

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
	 * control via {@link FieldControlFactory}, and wraps it in a
	 * {@link ReactFormFieldChromeControl}.
	 * </p>
	 *
	 * @return The chrome control.
	 */
	public ReactFormFieldChromeControl createChromeControl() {
		TLObject current = _form.getCurrentObject();
		if (current == null) {
			// No object selected yet - create a disabled placeholder.
			_innerControl = new ReactTextInputControl(
				_context, new AbstractFieldModel(null) {
					// Placeholder model with default state.
				});
			_chrome = new ReactFormFieldChromeControl(_context, _attributeName, false, false, null, null, null,
				false, true, _innerControl);
			return _chrome;
		}

		TLObjectOverlay overlay = _form.isEditMode() ? _form.getOverlay() : new TLObjectOverlay(current);
		_model = new OverlayFieldModel(overlay, _attributeName);
		_model.setEditable(_form.isEditMode() && !_forceReadonly);

		addModelListener();

		TLStructuredTypePart part = _model.getPart();
		_innerControl = FieldControlFactory.createFieldControl(_context, part, _model);

		String label = resolveLabel();
		boolean mandatory = part.isMandatory();
		boolean dirty = _model.isDirty();

		_chrome = new ReactFormFieldChromeControl(_context, label, mandatory, dirty, null, null, null,
			false, true, _innerControl);

		return _chrome;
	}

	/**
	 * The overlay field model, or {@code null} if not yet created.
	 */
	public OverlayFieldModel getModel() {
		return _model;
	}

	/**
	 * Updates editability when the form toggles edit mode.
	 */
	public void setEditMode(boolean editMode) {
		if (_model != null) {
			_model.setEditable(editMode && !_forceReadonly);
		}
	}

	/**
	 * Re-binds to a new overlay when the edited object changes.
	 */
	public void setOverlay(TLObjectOverlay newOverlay) {
		if (_model != null) {
			_model.setOverlay(newOverlay);
			if (_chrome != null) {
				_chrome.setDirty(_model.isDirty());
			}
		}
	}

	/**
	 * Refreshes the field display after state changes.
	 *
	 * <p>
	 * Called when the current object changes or when entering/leaving edit mode. For fields with an
	 * {@link OverlayFieldModel}, this is handled automatically via {@link #setEditMode(boolean)}
	 * and {@link #setOverlay(TLObjectOverlay)}. This method handles the case where no model exists
	 * yet (no object was selected initially) and the first object arrives.
	 * </p>
	 */
	public void refresh() {
		if (_model == null && _chrome != null) {
			// Model not yet created - check if an object is now available.
			TLObject current = _form.getCurrentObject();
			if (current != null) {
				// First object arrived. Need to recreate the inner control.
				TLObjectOverlay overlay = _form.isEditMode() ? _form.getOverlay()
					: new TLObjectOverlay(current);
				_model = new OverlayFieldModel(overlay, _attributeName);
				_model.setEditable(_form.isEditMode() && !_forceReadonly);

				addModelListener();

				TLStructuredTypePart part = _model.getPart();
				_innerControl = FieldControlFactory.createFieldControl(_context, part, _model);

				_chrome.setLabel(resolveLabel());
				_chrome.setRequired(part.isMandatory());
				_chrome.setField(_innerControl);
				_chrome.setDirty(false);
			}
		}
	}

	/**
	 * The resolved model attribute part, or {@code null} if not yet resolved.
	 */
	public TLStructuredTypePart getResolvedPart() {
		return _model != null ? _model.getPart() : null;
	}

	/**
	 * The chrome control wrapping the inner input.
	 */
	public ReactFormFieldChromeControl getChromeControl() {
		return _chrome;
	}

	/**
	 * The inner input control.
	 */
	public ReactControl getInnerControl() {
		return _innerControl;
	}

	private void addModelListener() {
		_model.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_form.updateDirtyChannel();
				if (_chrome != null) {
					_chrome.setDirty(_model.isDirty());
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Handled by the ReactFormFieldControl via its own FieldModel listener.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Handled by the ReactFormFieldControl via its own FieldModel listener.
			}
		});
	}

	private String resolveLabel() {
		if (_labelOverride != null) {
			return Resources.getInstance().getString(_labelOverride);
		}
		if (_model != null) {
			return MetaLabelProvider.INSTANCE.getLabel(_model.getPart());
		}
		return _attributeName;
	}
}
