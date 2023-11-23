/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.EmptyRenderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.control.PopupEditControl.Settings;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.control.TextInputWithButtonControl;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil;

/**
 * {@link ControlProvider} for {@link I18NStringField} whose control renders an input field for the
 * active language and a popup dialog to edit other languages.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NActiveLanguageControlProvider implements ControlProvider {

	private int _rows;

	private int _columns;

	/**
	 * Creates a single line {@link I18NActiveLanguageControlProvider} with default columns value.
	 * 
	 * @see TextInputControl#NO_COLUMNS
	 */
	public I18NActiveLanguageControlProvider() {
		this(0, TextInputControl.NO_COLUMNS);
	}

	/**
	 * Creates a {@link I18NActiveLanguageControlProvider}.
	 * 
	 * @param rows
	 *        Number of written rows. If <code>&gt;0</code> a multi-line input field is rendered.
	 * @param columns
	 *        Number of columns in the input fields.
	 */
	public I18NActiveLanguageControlProvider(int rows, int columns) {
		super();
		_rows = rows;
		_columns = columns;
	}

	@Override
	public Control createControl(Object model, String style) {
		I18NStringField i18n = (I18NStringField) model;
		FormField sourceField = I18NTranslationUtil.getSourceField(i18n.getLanguageFields());
		TextInputWithButtonControl sourceControl =
			new TextInputWithButtonControl(sourceField, openPopup(i18n));
		sourceControl.setMultiLine(multiline());
		sourceControl.setColumns(_columns);
		if (multiline()) {
			sourceControl.setRows(_rows);
		}
		OnVisibleControl block = new OnVisibleControl(i18n);
		block.addChild(sourceControl);
		return block;
	}

	private boolean multiline() {
		return _rows > 0;
	}

	private HTMLFragment openPopup(I18NStringField i18n) {
		Settings settings = new Settings()
			.setFirstLineRenderer(EmptyRenderer.getInstance());
		return new I18NStringTextPopupControl(settings, i18n, _rows, _columns);
	}

}
