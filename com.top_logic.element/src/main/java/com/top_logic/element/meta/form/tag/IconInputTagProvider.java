/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.IconInputControl;
import com.top_logic.layout.form.tag.CustomInputTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * {@link DisplayProvider} using {@link CustomInputTag}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IconInputTagProvider implements DisplayProvider, ControlProvider {

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		return createControl(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

	@Override
	public HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		return createFragment(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

	@Override
	public Control createControl(Object model, String style) {
		if (style == null || FormTemplateConstants.STYLE_DIRECT_VALUE.equals(style)) {
			return new IconInputControl((FormField) model);
		} else {
			return DefaultFormFieldControlProvider.INSTANCE.createControl(model, style);
		}
	}

}
