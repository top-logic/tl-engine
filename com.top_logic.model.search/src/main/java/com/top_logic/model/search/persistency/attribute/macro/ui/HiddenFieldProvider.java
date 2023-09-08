/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.macro.ui;

import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;

/**
 * {@link FieldProvider} that creates a display of a macro attribute displaying the rendering of a
 * search expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HiddenFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		return FormFactory.newHiddenField(fieldName);
	}

}
