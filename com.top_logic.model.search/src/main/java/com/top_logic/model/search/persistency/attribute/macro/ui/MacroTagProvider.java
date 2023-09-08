/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.macro.ui;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.model.search.persistency.attribute.macro.MacroAttribute;

/**
 * {@link DisplayProvider} displaying a {@link MacroAttribute}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MacroTagProvider implements DisplayProvider {

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		return new MacroControlProvider(editContext.getObject()).createControl(member,
			FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

}
