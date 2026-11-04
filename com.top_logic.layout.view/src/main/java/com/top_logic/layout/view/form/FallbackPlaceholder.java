/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.fallback.StorageWithFallback;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.TracingScope;

/**
 * Displays the fallback value of an attribute with a {@link StorageWithFallback} as placeholder of
 * its form field.
 *
 * <p>
 * An editable field of such an attribute holds only the explicit value of the attribute (see
 * {@link #fieldValue(TLObject, TLStructuredTypePart, boolean)}): an empty field stores no value,
 * the fallback applies. While the field is empty, the fallback value is shown as its
 * {@link ReactFormFieldControl#setPlaceholder(String) placeholder}, written the way the input
 * control writes its values ({@link ReactFormFieldControl#formatValue(Object)}).
 * </p>
 *
 * <p>
 * The fallback value is computed over the overlays of the form in a {@link TracingScope}, which
 * records the attributes the computation reads. When the field of one of those attributes changes
 * in the form, the placeholder is computed again. This includes a fallback attribute that is
 * itself derived from other attributes of the form.
 * </p>
 *
 * <p>
 * A field that is not editable displays the value of the attribute as it is, the explicit value or
 * the fallback, and shows no placeholder.
 * </p>
 *
 * <p>
 * The field container is marked with the {@link StorageWithFallback#getCssFallback() fallback CSS
 * class} while the attribute has no explicit value and with the
 * {@link StorageWithFallback#getCssExplicit() explicit CSS class} otherwise, see
 * {@link ReactFormFieldChromeControl#setStateCssClass(String)}.
 * </p>
 *
 * <p>
 * A field of an attribute without a {@link StorageWithFallback} is left as it is.
 * </p>
 */
public class FallbackPlaceholder {

	private final FormControl _form;

	private final AttributeFieldModel _model;

	private final ReactControl _input;

	private final ReactFormFieldChromeControl _chrome;

	private final FormControl.FieldChangeListener _dependencyListener = this::onFieldChanged;

	private final FieldModelListener _modelListener = new FieldModelListener() {
		@Override
		public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
			update();
		}

		@Override
		public void onEditabilityChanged(FieldModel source, boolean editable) {
			update();
		}

		@Override
		public void onValidationChanged(FieldModel source) {
			// The validation of the value has no say in its fallback.
		}
	};

	/**
	 * The attributes the last computation of the fallback value read.
	 */
	private Set<TLStructuredTypePart> _dependencies = Collections.emptySet();

	/**
	 * Whether this instance has set the placeholder and the CSS class of the field, which it has
	 * to reset when the field no longer displays an attribute with a fallback.
	 */
	private boolean _applied;

	/**
	 * Creates a {@link FallbackPlaceholder} and displays the current fallback value.
	 *
	 * @param form
	 *        The form the field is displayed in, providing the overlays the fallback is computed
	 *        over and the notifications about changed fields. {@code null} for a field outside of
	 *        a form.
	 * @param model
	 *        The model of the field.
	 * @param input
	 *        The input control displaying the model, receiving the placeholder.
	 * @param chrome
	 *        The container of the field, receiving the CSS class.
	 */
	public FallbackPlaceholder(FormControl form, AttributeFieldModel model, ReactControl input,
			ReactFormFieldChromeControl chrome) {
		_form = form;
		_model = model;
		_input = input;
		_chrome = chrome;
		_model.addListener(_modelListener);
		if (_form != null) {
			_form.addFieldChangeListener(_dependencyListener);
		}
		update();
	}

	/**
	 * The {@link StorageWithFallback} of the given attribute, {@code null} if it has none.
	 */
	public static StorageWithFallback storage(TLStructuredTypePart part) {
		return part.getStorageImplementation() instanceof StorageWithFallback fallback ? fallback : null;
	}

	/**
	 * The value a field displays for the given attribute of the given object.
	 *
	 * <p>
	 * An editable field of an attribute with a {@link StorageWithFallback} displays the
	 * {@link StorageWithFallback#getExplicitValue(TLObject, TLStructuredTypePart) explicit value}
	 * only, any other field the value of the attribute.
	 * </p>
	 *
	 * @param object
	 *        The object displayed.
	 * @param part
	 *        The attribute displayed.
	 * @param editable
	 *        Whether the field is editable.
	 */
	public static Object fieldValue(TLObject object, TLStructuredTypePart part, boolean editable) {
		if (editable) {
			StorageWithFallback storage = storage(part);
			if (storage != null) {
				return storage.getExplicitValue(object, part);
			}
		}
		return object.tValue(part);
	}

	/**
	 * Computes the placeholder and the CSS class of the field from the object the field currently
	 * displays.
	 *
	 * <p>
	 * Called when the field is bound to another object, or something else has changed the object.
	 * </p>
	 */
	public void update() {
		TLStructuredTypePart part = _model.getPart();
		StorageWithFallback storage = storage(part);
		TLObject object = _model.getObject();
		if (storage == null || object == null || !object.tValid()) {
			reset();
			return;
		}

		boolean explicit = !Utils.isEmpty(storage.getExplicitValue(object, part));

		String placeholder = null;
		if (_model.isEditable()) {
			Set<TLStructuredTypePart> dependencies = new HashSet<>();
			OverlayLookup overlays = _form == null ? null : _form.getValidationModel();
			Object fallback;
			try (TracingScope scope = TracingScope.open(pointer -> dependencies.add(pointer.attribute()), overlays)) {
				fallback = storage.getFallbackValue(object, part);
			}
			_dependencies = dependencies;
			placeholder = format(fallback);
		} else {
			_dependencies = Collections.emptySet();
		}

		setPlaceholder(placeholder);
		_chrome.setStateCssClass(explicit ? storage.getCssExplicit() : storage.getCssFallback());
		_applied = true;
	}

	private String format(Object value) {
		if (_input instanceof ReactFormFieldControl field) {
			return field.formatValue(value);
		}
		return null;
	}

	private void setPlaceholder(String placeholder) {
		if (_input instanceof ReactFormFieldControl field) {
			field.setPlaceholder(placeholder);
		}
	}

	/**
	 * Removes the placeholder and the CSS class this instance has set.
	 */
	private void reset() {
		_dependencies = Collections.emptySet();
		if (_applied) {
			setPlaceholder(null);
			_chrome.setStateCssClass(null);
			_applied = false;
		}
	}

	/**
	 * Computes the fallback again when an attribute its last computation read has changed.
	 */
	private void onFieldChanged(TLStructuredTypePart changedPart) {
		if (changedPart != _model.getPart() && _dependencies.contains(changedPart)) {
			update();
		}
	}

	/**
	 * Detaches from the field model and the form's field-change notifications.
	 */
	public void dispose() {
		_model.removeListener(_modelListener);
		if (_form != null) {
			_form.removeFieldChangeListener(_dependencyListener);
		}
	}

}
