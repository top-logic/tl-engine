/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.layout.formeditor.implementation.additional;

import com.top_logic.element.layout.formeditor.implementation.FieldDefinitionTemplateProvider;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.template.model.FormEditorElementTemplate;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.util.TLContext;

/**
 * {@link FormElementTemplateProvider} that enabled fields only for {@link TLContext#isAdmin()
 * administrators} which does not edit themselves.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractIsAdminProvider implements FormElementTemplateProvider {

	@Override
	public HTMLTemplateFragment createTemplate(FormEditorContext context) {
		HTMLTemplateFragment result = getDelegate().createTemplate(context);
		if (result instanceof FormEditorElementTemplate) {
			result = ((FormEditorElementTemplate) result).getElement();
		}
		FormMember displayingField = getDelegate().getMember();

		TLObject model = context.getModel();
		if (model != null) {
			if (!TLContext.isAdmin() || model == TLContext.getContext().getCurrentPersonWrapper()) {
				displayingField.setMode(FieldMode.DISABLED);
			}
		}
		return result;
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return getDelegate().getWholeLine(modelType);
	}

	/**
	 * {@link FieldDefinitionTemplateProvider} creating the actual member.
	 */
	protected abstract FieldDefinitionTemplateProvider getDelegate();

}

