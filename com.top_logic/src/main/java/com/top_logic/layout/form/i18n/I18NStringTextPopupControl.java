/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.PopupEditControl;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.Scrolling;

/**
 * {@link PopupEditControl} for I18n attributes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStringTextPopupControl extends PopupEditControl {

	private int _rows;

	private int _columns;

	/**
	 * Creates a new {@link I18NStringTextPopupControl}.
	 */
	public I18NStringTextPopupControl(Settings settings, FormField model, int rows, int columns) {
		super(settings, model);
		_rows = rows;
		_columns = columns;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		super.valueChanged(field, oldValue, newValue);
		requestRepaint();
	}

	@Override
	protected FormField createEditField(FormField originalField) {
		Map<Locale, StringField> baseFields = Collections.emptyMap();
		if (originalField instanceof I18NStringField) {
			I18NStringField i18nField = (I18NStringField) originalField;
			baseFields = i18nField.getLanguageFieldsByLocale();
		}
		String name = originalField.getName() + POPUP_SUFFIX;
		boolean mandatory = originalField.isMandatory();
		boolean immutable = originalField.isImmutable();
		I18NStringField copy = I18NStringField.newI18NStringField(name, mandatory, immutable);
		copyConstraints(copy, baseFields);
		return copy;
	}

	private void copyConstraints(I18NField<?, ?, ?> copy, Map<Locale, ? extends FormField> baseFields) {
		for (Entry<Locale, ? extends FormField> field : copy.getLanguageFieldsByLocale().entrySet()) {
			FormField baseField = baseFields.get(field.getKey());
			if (baseField != null) {
				baseField.getConstraints().forEach(field.getValue()::addConstraint);
				baseField.getWarningConstraints().forEach(field.getValue()::addWarningConstraint);
			}
		}
	}

	@Override
	protected HTMLFragment createEditFragment(FormField editField) {
		return new I18NStringControlProvider(_rows, _columns).createFragment(editField);
	}

	@Override
	protected LayoutData getDialogLayout(FormField editField) {
		if (_rows > 0) {
			return new DefaultLayoutData(dim(720, PIXEL), 100, dim(300, PIXEL), 100, Scrolling.AUTO);
		}
		return new DefaultLayoutData(dim(400, PIXEL), 100, dim(150, PIXEL), 100, Scrolling.AUTO);
	}

}
