/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.layout.formeditor.implementation.additional;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.layout.formeditor.implementation.FieldDefinitionTemplateProvider;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.template.model.FormEditorElementTemplate;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.util.TLContext;

/**
 * Whether the edited {@link Person} is an technical administrator.
 * 
 * @see Person#isAdmin()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@Label("Is administrator")
public class IsAdminProvider implements FormElementTemplateProvider {

	private final FieldDefinitionTemplateProvider _delegate;

	/**
	 * Creates a new {@link IsAdminProvider}.
	 */
	public IsAdminProvider() {
		FieldDefinition definition = TypedConfiguration.newConfigItem(FieldDefinition.class);
		definition.setAttribute("admin");
		definition.setTypeSpec("tl.accounts:Person");
		_delegate = TypedConfigUtil.createInstance(definition);
	}

	@Override
	public HTMLTemplateFragment createTemplate(FormEditorContext context) {
		HTMLTemplateFragment result = _delegate.createTemplate(context);
		if (result instanceof FormEditorElementTemplate) {
			result = ((FormEditorElementTemplate) result).getElement();
		}
		FormMember displayingField = _delegate.getMember();

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
		return false;
	}

}

