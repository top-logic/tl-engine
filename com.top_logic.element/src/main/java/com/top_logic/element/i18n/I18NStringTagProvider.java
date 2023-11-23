/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.i18n.I18NStringControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
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
		return cp(editContext).createControl(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

	@Override
	public HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		return cp(editContext).createFragment(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

	private I18NStringControlProvider cp(EditContext editContext) {
		int rows;
		boolean multiline;
		if (AttributeOperations.isMultiline(editContext) && !editContext.isSearchUpdate()) {
			MultiLine annotation = editContext.getAnnotation(MultiLine.class);
			if (annotation != null) {
				rows = annotation.getRows();
			} else {
				rows = MultiLineText.DEFAULT_ROWS;
			}

			multiline = true;
		} else {
			rows = 0;
			multiline = false;
		}
		I18NStringControlProvider i18nStringControlProvider = new I18NStringControlProvider(multiline, rows,
			DisplayAnnotations.inputSize(editContext, TextInputControl.NO_COLUMNS));
		return i18nStringControlProvider;
	}

}
