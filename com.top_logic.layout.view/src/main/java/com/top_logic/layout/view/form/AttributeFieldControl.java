/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.form.ValidationResult;
import com.top_logic.util.Resources;

/**
 * Per-field session object that bridges a model attribute to a React input control wrapped in
 * {@link ReactFormFieldChromeControl chrome}.
 *
 * <p>
 * Implements {@link FormModelListener} and self-registers on the {@link FormModel}. When the form
 * state changes (object switch, edit mode toggle, apply), pulls the current object and edit mode
 * from the {@link FormModel} and updates the inner control and chrome accordingly.
 * </p>
 */
public class AttributeFieldControl implements FormModelListener, FormParticipant {

	private final ReactContext _context;

	private final FormModel _formModel;

	private final FormControl _formControl;

	private final String _attributeName;

	private final ResKey _labelOverride;

	private final boolean _forceReadonly;

	private AttributeFieldModel _model;

	private FieldModelListener _modelListener;

	private ConstraintValidationListener _validationListener;

	private ReactFormFieldChromeControl _chrome;

	private ReactControl _innerControl;

	/**
	 * Creates a new {@link AttributeFieldControl} and registers as listener on the form model.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formModel
	 *        The form model to observe.
	 * @param formControl
	 *        The concrete form control (for triggering dirty state updates).
	 * @param attributeName
	 *        The name of the model attribute to display.
	 * @param labelOverride
	 *        Optional label override, or {@code null} to derive the label from the model.
	 * @param forceReadonly
	 *        Whether the field should always be read-only regardless of form edit mode.
	 */
	public AttributeFieldControl(ReactContext context, FormModel formModel, FormControl formControl,
			String attributeName, ResKey labelOverride, boolean forceReadonly) {
		_context = context;
		_formModel = formModel;
		_formControl = formControl;
		_attributeName = attributeName;
		_labelOverride = labelOverride;
		_forceReadonly = forceReadonly;
		formModel.addFormModelListener(this);
	}

	/**
	 * Creates the chrome-wrapped React control for this field.
	 *
	 * @return The chrome control.
	 */
	public ReactFormFieldChromeControl createChromeControl() {
		TLObject current = _formModel.getCurrentObject();
		if (current == null) {
			_innerControl = new ReactTextInputControl(
				_context, new AbstractFieldModel(null) {
					// Placeholder model with default state.
				});
			_chrome = new ReactFormFieldChromeControl(_context, _attributeName,
				false, false, null, null, null, false, true, _innerControl);
			return _chrome;
		}

		TLStructuredTypePart part = resolvePart(current);
		if (part == null) {
			// Attribute not supported by this object's type - hide the field.
			_innerControl = new ReactTextInputControl(
				_context, new AbstractFieldModel(null) {
					// Placeholder model with default state.
				});
			_chrome = new ReactFormFieldChromeControl(_context, _attributeName,
				false, false, null, null, null, false, false, _innerControl);
			return _chrome;
		}

		_model = new AttributeFieldModel(current, part);
		_model.setEditable(_formModel.isEditMode() && !_forceReadonly);
		_formControl.registerParticipant(this);

		addModelListener();

		_innerControl = FieldControlService.getInstance().createFieldControl(_context, part, _model);

		String label = resolveLabel();
		String helpText = resolveHelpText(part);
		boolean mandatory = part.isMandatory();
		boolean dirty = _model.isDirty();

		_chrome = new ReactFormFieldChromeControl(_context, label, mandatory,
			dirty, null, helpText, null, false, true, _innerControl);

		return _chrome;
	}

	@Override
	public void onFormStateChanged(FormModel source) {
		if (_chrome == null) {
			return;
		}

		TLObject current = source.getCurrentObject();

		if (current == null) {
			// Object gone - hide field.
			_chrome.setVisible(false);
			clearModel();
			return;
		}

		TLStructuredTypePart part = resolvePart(current);

		if (part == null) {
			// Attribute not supported by this object's type - hide field.
			_chrome.setVisible(false);
			clearModel();
			return;
		}

		// Attribute exists - ensure field is visible.
		_chrome.setVisible(true);

		if (_model == null) {
			// First compatible object arrived or re-appearing after hide.
			_model = new AttributeFieldModel(current, part);
			_model.setEditable(source.isEditMode() && !_forceReadonly);
			_formControl.registerParticipant(this);

			addModelListener();

			_innerControl = FieldControlService.getInstance().createFieldControl(_context, part, _model);

			_chrome.setLabel(resolveLabel());
			_chrome.setHelpText(resolveHelpText(part));
			_chrome.setRequired(part.isMandatory());
			_chrome.setField(_innerControl);
			_chrome.setDirty(false);
			return;
		}

		// Rebind existing model to the current object.
		_model.setObject(current);
		_model.setEditable(source.isEditMode() && !_forceReadonly);
		_formControl.registerParticipant(this);
		_chrome.setDirty(_model.isDirty());

		// Re-wire validation: the overlay changed, so the old listener (bound to the
		// previous overlay by identity) won't match anymore. Remove it and re-create.
		unwireValidation();
		wireValidation();
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

	@Override
	public boolean validate() {
		return _model == null || !_model.hasError();
	}

	@Override
	public void persist(Transaction tx) {
		// No-op: the main overlay handles primitive attribute changes.
	}

	@Override
	public void cancel() {
		// No-op: FormControl discards the overlay, model rebinds on form state change.
	}

	@Override
	public void revealAll() {
		if (_model != null) {
			_model.setRevealed(true);
		}
	}

	@Override
	public boolean isDirty() {
		return _model != null && _model.isDirty();
	}

	private void clearModel() {
		unwireValidation();
		if (_model != null) {
			if (_modelListener != null) {
				_model.removeListener(_modelListener);
				_modelListener = null;
			}
			_formControl.unregisterParticipant(this);
		}
		_model = null;
	}

	/**
	 * Removes the validation listener from the FormValidationModel.
	 */
	private void unwireValidation() {
		if (_validationListener != null) {
			FormValidationModel validationModel = _formControl.getValidationModel();
			if (validationModel != null) {
				validationModel.removeConstraintValidationListener(_validationListener);
			}
			_validationListener = null;
		}
	}

	private void addModelListener() {
		if (_modelListener != null) {
			_model.removeListener(_modelListener);
		}
		_modelListener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_formControl.updateDirtyState();
				if (_chrome != null) {
					_chrome.setDirty(_model.isDirty());
				}

				// Reveal validation errors after user interaction.
				_model.setRevealed(true);

				// Trigger constraint validation.
				FormValidationModel validationModel = _formControl.getValidationModel();
				TLObjectOverlay overlay = _formControl.getOverlay();
				if (validationModel != null && _model != null && overlay != null) {
					validationModel.onValueChanged(overlay, _model.getPart());
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Handled by the ReactFormFieldControl via its own FieldModel listener.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				if (_chrome == null) {
					return;
				}
				if (source.hasError()) {
					_chrome.setError(Resources.getInstance().getString(source.getError()));
				} else {
					_chrome.setError(null);
				}
				if (source.hasWarnings()) {
					List<String> msgs = source.getWarnings().stream()
						.map(key -> Resources.getInstance().getString(key))
						.collect(Collectors.toList());
					_chrome.setWarnings(msgs);
				} else {
					_chrome.setWarnings(null);
				}
				_chrome.setRequired(source.isMandatory());
			}
		};
		_model.addListener(_modelListener);

		wireValidation();
	}

	/**
	 * Registers the validation listener on the {@link FormValidationModel} if available and not
	 * yet registered. Reads initial validation state.
	 */
	private void wireValidation() {
		if (_validationListener != null || _model == null) {
			return; // Already wired or no model.
		}
		FormValidationModel validationModel = _formControl.getValidationModel();
		if (validationModel == null) {
			return; // Not in edit mode yet.
		}

		TLStructuredTypePart part = _model.getPart();
		TLObject overlay = _formModel.getCurrentObject();

		// Read initial validation state.
		_model.applyValidationResult(validationModel.getValidation(overlay, part));

		// Listen for future changes.
		_validationListener = (o, attr, result) -> {
			if (attr.equals(part)) {
				_model.applyValidationResult(result);
			}
		};
		validationModel.addConstraintValidationListener(_validationListener);
	}

	private TLStructuredTypePart resolvePart(TLObject obj) {
		TLStructuredType type = obj.tType();
		return type.getPart(_attributeName);
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

	private String resolveHelpText(TLStructuredTypePart part) {
		ResKey labelKey = TLModelI18N.getI18NKey(part);
		return Resources.getInstance().getString(labelKey.tooltipOptional());
	}
}
