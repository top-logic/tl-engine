/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.i18n.I18NActiveLanguageControlProvider;
import com.top_logic.layout.form.i18n.I18NStringField;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.MultiLine;

/**
 * {@link DisplayProvider} for I18N attributes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStringTagProvider implements DisplayProvider {

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		I18NStringField i18n = (I18NStringField) member;
		int rows = rows(editContext);
		int columns = columns(editContext);
		return newControl(i18n, rows, columns);
	}

	static int columns(EditContext editContext) {
		return DisplayAnnotations.inputSize(editContext, TextInputControl.NO_COLUMNS);
	}

	static int rows(EditContext editContext) {
		int rows;
		if (AttributeOperations.isMultiline(editContext) && !editContext.isSearchUpdate()) {
			MultiLine annotation = editContext.getAnnotation(MultiLine.class);
			if (annotation != null) {
				rows = annotation.getRows();
			} else {
				rows = MultiLineText.DEFAULT_ROWS;
			}
		} else {
			rows = 0;
		}
		return rows;
	}

	private Control newControl(I18NStringField i18n, int rows, int columns) {
		return new I18NActiveLanguageControlProvider(rows, columns).createControl(i18n);
	}

}
