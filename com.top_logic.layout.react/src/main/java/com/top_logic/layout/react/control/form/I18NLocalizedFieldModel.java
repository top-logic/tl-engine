/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * {@link FieldModel} adapter exposing the {@link #editLocale() current session locale's} entry of
 * an internationalized value.
 *
 * <p>
 * The kind of internationalized value is described by an {@link I18NValueEditor}: the adapter
 * translates in both directions with it. {@link #getValue()} extracts the current locale's entry,
 * and {@link #setValue(Object)} merges the edited entry back into the underlying value, preserving
 * the entries of all other languages. This lets a control operating on single entries edit an
 * internationalized value inline.
 * </p>
 *
 * <p>
 * Listeners registered on the adapter receive events with the adapter as source and localized
 * values; the adapter is registered on the underlying model only while it has listeners itself.
 * </p>
 */
public class I18NLocalizedFieldModel implements FieldModel, FieldModelListener {

	private final FieldModel _delegate;

	private final I18NValueEditor _valueEditor;

	private final List<FieldModelListener> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link I18NLocalizedFieldModel}.
	 *
	 * @param delegate
	 *        The underlying model holding the internationalized value.
	 * @param valueEditor
	 *        The description of the internationalized value.
	 */
	public I18NLocalizedFieldModel(FieldModel delegate, I18NValueEditor valueEditor) {
		_delegate = delegate;
		_valueEditor = valueEditor;
	}

	/**
	 * The locale whose entry is edited inline: the resource-bundle locale for the current
	 * session's locale, so reading and writing use the same normalized key.
	 */
	public static Locale editLocale() {
		return Resources.getInstance(TLContext.getLocale()).getLocale();
	}

	/**
	 * The underlying model holding the internationalized value.
	 */
	public FieldModel getDelegate() {
		return _delegate;
	}

	/**
	 * The description of the internationalized value.
	 */
	public I18NValueEditor getValueEditor() {
		return _valueEditor;
	}

	/**
	 * The entry of the {@link #editLocale() edit locale} in the given internationalized value.
	 *
	 * <p>
	 * The entry is extracted strictly, so editing never silently copies another language's content.
	 * </p>
	 *
	 * @param i18nValue
	 *        The internationalized value, or {@code null}.
	 * @param editable
	 *        Whether the entry is extracted for editing or for display.
	 * @return The entry, {@code null} for none.
	 */
	protected Object localize(Object i18nValue, boolean editable) {
		return _valueEditor.localize(i18nValue, editLocale());
	}

	private Object localize(Object i18nValue) {
		return localize(i18nValue, _delegate.isEditable());
	}

	@Override
	public Object getValue() {
		return localize(_delegate.getValue());
	}

	@Override
	public void setValue(Object value) {
		Object old = _delegate.getValue();
		Locale editLocale = editLocale();
		Map<Locale, Object> entries = new HashMap<>();
		for (Locale locale : Resources.getInstance().getSupportedLocalesInDisplayOrder()) {
			if (!locale.getLanguage().equals(editLocale.getLanguage())) {
				entries.put(locale, _valueEditor.localize(old, locale));
			}
		}
		entries.put(editLocale, value);
		_delegate.setValue(_valueEditor.merge(old, entries));
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
	public boolean isDisabled() {
		return _delegate.isDisabled();
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
		Object oldEntry = localize(oldValue);
		Object newEntry = localize(newValue);
		for (FieldModelListener listener : _listeners) {
			listener.onValueChanged(this, oldEntry, newEntry);
		}
	}

	@Override
	public void onEditabilityChanged(FieldModel source, boolean editable) {
		for (FieldModelListener listener : _listeners) {
			listener.onEditabilityChanged(this, editable);
		}
		// The exposed value may depend on the mode, see localize(Object, boolean).
		Object i18nValue = _delegate.getValue();
		Object oldEntry = localize(i18nValue, !editable);
		Object newEntry = localize(i18nValue, editable);
		if (!Objects.equals(oldEntry, newEntry)) {
			for (FieldModelListener listener : _listeners) {
				listener.onValueChanged(this, oldEntry, newEntry);
			}
		}
	}

	@Override
	public void onDisabledChanged(FieldModel source, boolean disabled) {
		for (FieldModelListener listener : _listeners) {
			listener.onDisabledChanged(this, disabled);
		}
	}

	@Override
	public void onValidationChanged(FieldModel source) {
		for (FieldModelListener listener : _listeners) {
			listener.onValidationChanged(this);
		}
	}

}
