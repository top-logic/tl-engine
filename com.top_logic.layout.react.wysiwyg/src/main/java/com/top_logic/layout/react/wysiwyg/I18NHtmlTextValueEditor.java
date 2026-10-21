/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.I18NStringValueEditor;
import com.top_logic.layout.react.control.form.I18NValueEditor;
import com.top_logic.layout.wysiwyg.ui.StructuredText;

/**
 * {@link I18NValueEditor} for an internationalized HTML text held as {@link ResKey}: each entry
 * is the HTML source of one language, edited in a {@link ReactWysiwygControl}
 * {@link ReactWysiwygControl#setTextOnly(boolean) for text only}.
 */
public class I18NHtmlTextValueEditor extends I18NStringValueEditor {

	private final boolean _sourceEditing;

	/**
	 * Creates a {@link I18NHtmlTextValueEditor}.
	 *
	 * @param sourceEditing
	 *        Whether the editor of an entry offers editing the HTML source, see
	 *        {@link ReactWysiwygControl#setSourceEditing(boolean)}.
	 */
	public I18NHtmlTextValueEditor(boolean sourceEditing) {
		super(0);
		_sourceEditing = sourceEditing;
	}

	/**
	 * An entry is empty if it displays nothing, see
	 * {@link WysiwygControlProvider#isEmptyHtml(StructuredText)}.
	 */
	@Override
	public boolean isEmpty(Object entry) {
		return entry == null || WysiwygControlProvider.isEmptyHtml(new StructuredText(entry.toString()));
	}

	@Override
	public ReactControl createEntryEditor(ReactContext context, FieldModel entryModel) {
		return ReactWysiwygControl.forText(context, entryModel, _sourceEditing);
	}

	@Override
	public boolean isTall() {
		return true;
	}

	@Override
	public DisplayDimension dialogWidth() {
		return DisplayDimension.px(720);
	}

}
