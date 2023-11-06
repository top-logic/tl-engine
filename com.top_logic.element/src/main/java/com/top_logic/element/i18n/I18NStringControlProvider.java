/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import com.top_logic.basic.translation.TranslationService;
import com.top_logic.element.i18n.I18NStringTagProvider.I18NStringControlRenderer;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil.StringValuedFieldTranslator;

/**
 * {@link ControlProvider} creating a a suitable control for an {@link I18NStringField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStringControlProvider implements ControlProvider {

	private boolean _multiline;

	private int _rows;

	private int _columns;

	/**
	 * This constructor creates a new {@link I18NStringControlProvider}.
	 * 
	 * @param multiline
	 *        Whether the field should be displayed in multi-line mode.
	 * @param rows
	 *        If <code>multiline</code>, the number of rows to display.
	 * @param columns
	 *        Number of columns to display.
	 * 
	 * @see TextInputControl#NO_COLUMNS
	 */
	public I18NStringControlProvider(boolean multiline, int rows, int columns) {
		_multiline = multiline;
		_rows = rows;
		_columns = columns;
	}

	@Override
	public Control createControl(Object model, String style) {
		if (model instanceof I18NStringField) {
			I18NStringField member = (I18NStringField) model;
			return createControl(member);
		}
		return DefaultFormFieldControlProvider.INSTANCE.createControl(model);
	}

	private Control createControl(I18NStringField member) {
		OnVisibleControl block = new OnVisibleControl(member);
		for (FormField field : member.getLanguageFields()) {
			TextInputControl control = new TextInputControl(field);
			control.setMultiLine(_multiline);
			control.setColumns(_columns);
			if (_multiline) {
				control.setRows(_rows);
			}
			block.addChild(control);
			block.addChild(new ErrorControl(field, true));
			if (TranslationService.isActive()) {
				if (!I18NTranslationUtil.isSourceField(field)) {
					block.addChild(
						I18NTranslationUtil.getTranslateControl(field, member, StringValuedFieldTranslator.INSTANCE));
				}
			}
		}
		block.setRenderer(
			_multiline ? I18NStringControlRenderer.ABOVE_INSTANCE : I18NStringControlRenderer.INSTANCE);
		return block;
	}

}

