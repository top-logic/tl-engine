/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.accounts;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.formeditor.implementation.additional.I18NConstants;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.model.FormEditorElementTemplate;
import com.top_logic.layout.formeditor.parts.ForeignAttributeDefinition;
import com.top_logic.layout.formeditor.parts.ForeignAttributeTemplateProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.util.Resources;

/**
 * The field showing the members of the representative groups of an account.
 * 
 * <p>
 * Special handling is required, because this foreign field requires a special label and special
 * options.
 * </p>
 */
public class RepresentativeField extends ForeignAttributeTemplateProvider {

	/**
	 * Creates a {@link RepresentativeField} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RepresentativeField(InstantiationContext context, ForeignAttributeDefinition config) {
		super(context, config);
	}

	@Override
	public HTMLTemplateFragment createTemplate(FormEditorContext context) {
		HTMLTemplateFragment result = super.createTemplate(context);
		if (result instanceof FormEditorElementTemplate) {
			result = ((FormEditorElementTemplate) result).getElement();
		}

		FormMember displayingField = getDelegate().getMember();
		if (displayingField != null) {
			displayingField.setLabel(Resources.getInstance().getString(I18NConstants.REPRESENTATIVES));
			displayingField.setTooltip(Resources.getInstance().getString(I18NConstants.REPRESENTATIVES.tooltip()));
		}

		return result;
	}

}

