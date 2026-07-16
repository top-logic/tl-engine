/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * {@link FieldModel} adapter exposing the current session locale's entry of an
 * {@link I18NStructuredText} value as a plain {@link StructuredText}.
 *
 * <p>
 * The adapter translates in both directions: {@link #getValue()} extracts the current locale's
 * {@link StructuredText} (without falling back to other languages, so editing never silently
 * copies another language's content), and {@link #setValue(Object)} merges the edited
 * {@link StructuredText} back into the underlying attribute value, preserving the entries of all
 * other languages. This lets a control operating on {@link StructuredText} values (e.g.
 * {@link ReactWysiwygControl}) edit an {@code I18NHtml} attribute.
 * </p>
 *
 * <p>
 * Listeners registered on the adapter receive events with the adapter as source and localized
 * values; the adapter is registered on the underlying model only while it has listeners itself.
 * </p>
 */
public class I18NLocalizedHtmlFieldModel implements FieldModel, FieldModelListener {

	private final FieldModel _delegate;

	private final List<FieldModelListener> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link I18NLocalizedHtmlFieldModel}.
	 *
	 * @param delegate
	 *        The underlying model holding the {@link I18NStructuredText} value.
	 */
	public I18NLocalizedHtmlFieldModel(FieldModel delegate) {
		_delegate = delegate;
	}

	/**
	 * The locale whose entry is edited: the resource-bundle locale for the current session's
	 * locale, so reading and writing use the same normalized key.
	 */
	private Locale editLocale() {
		return Resources.getInstance(TLContext.getLocale()).getLocale();
	}

	private StructuredText localize(Object i18nValue) {
		if (i18nValue instanceof I18NStructuredText i18n) {
			return i18n.localizeStrict(editLocale());
		}
		return null;
	}

	@Override
	public Object getValue() {
		return localize(_delegate.getValue());
	}

	@Override
	public void setValue(Object value) {
		StructuredText text = (StructuredText) value;
		I18NStructuredText old = (I18NStructuredText) _delegate.getValue();
		Map<Locale, StructuredText> entries =
			old == null ? new HashMap<>() : I18NStructuredTextUtil.copyEntries(old);
		if (text == null) {
			entries.remove(editLocale());
		} else {
			entries.put(editLocale(), text);
		}
		_delegate.setValue(entries.isEmpty() ? null : new I18NStructuredText(entries));
	}

	@Override
	public boolean isDirty() {
		return _delegate.isDirty();
	}

	@Override
	public boolean isEditable() {
		return _delegate.isEditable();
	}

	@Override
	public boolean isMandatory() {
		return _delegate.isMandatory();
	}

	@Override
	public boolean isNullable() {
		return _delegate.isNullable();
	}

	@Override
	public boolean hasError() {
		return _delegate.hasError();
	}

	@Override
	public ResKey getError() {
		return _delegate.getError();
	}

	@Override
	public boolean hasWarnings() {
		return _delegate.hasWarnings();
	}

	@Override
	public List<ResKey> getWarnings() {
		return _delegate.getWarnings();
	}

	@Override
	public void setModelValidationError(ResKey error) {
		_delegate.setModelValidationError(error);
	}

	@Override
	public void setModelValidationWarnings(List<ResKey> warnings) {
		_delegate.setModelValidationWarnings(warnings);
	}

	@Override
	public void addListener(FieldModelListener listener) {
		if (_listeners.isEmpty()) {
			_delegate.addListener(this);
		}
		_listeners.add(listener);
	}

	@Override
	public void removeListener(FieldModelListener listener) {
		_listeners.remove(listener);
		if (_listeners.isEmpty()) {
			_delegate.removeListener(this);
		}
	}

	@Override
	public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
		StructuredText oldText = localize(oldValue);
		StructuredText newText = localize(newValue);
		for (FieldModelListener listener : _listeners) {
			listener.onValueChanged(this, oldText, newText);
		}
	}

	@Override
	public void onEditabilityChanged(FieldModel source, boolean editable) {
		for (FieldModelListener listener : _listeners) {
			listener.onEditabilityChanged(this, editable);
		}
	}

	@Override
	public void onValidationChanged(FieldModel source) {
		for (FieldModelListener listener : _listeners) {
			listener.onValidationChanged(this);
		}
	}

}
