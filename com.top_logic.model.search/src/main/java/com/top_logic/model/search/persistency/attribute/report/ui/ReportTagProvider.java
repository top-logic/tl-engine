/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.report.ui;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.ConstantProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * {@link DisplayProvider} for a report attribute.
 * 
 * <p>
 * A report attribute is an editable text attribute that allow embedding TL-Script expressions in
 * edit mode but displays the evaluation of the expressions in the context of the attribute's object
 * in view mode.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReportTagProvider implements DisplayProvider {

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		return createControlProvider(editContext).createControl(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

	@Override
	public HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		return createControlProvider(editContext).createFragment(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

	/**
	 * Creates the {@link ConstantProvider} to annotate the field with.
	 */
	protected ReportControlProvider createControlProvider(EditContext editContext) {
		return new ReportControlProvider(editContext.getObject());
	}

}
