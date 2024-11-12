/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.formeditor.definition.TitleDefinition;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;

/**
 * Creates a template for a {@link TitleDefinition} and stores the necessary information.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class TitleDefinitionTemplateProvider extends AbstractFormElementProvider<TitleDefinition> {

	private static final DisplayDimension HEIGHT = dim(350, DisplayUnit.PIXEL);

	private static final boolean IS_TOOL = true;

	private static final ResKey LABEL = I18NConstants.FORM_EDITOR__TOOL_NEW_TITLE;

	/**
	 * To instantiate a provider to create a template for a {@link TitleDefinition}.
	 */
	public TitleDefinitionTemplateProvider(InstantiationContext context, TitleDefinition config) {
		super(context, config);
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return true;
	}

	@Override
	public boolean getIsTool() {
		return IS_TOOL;
	}

	@Override
	public ImageProvider getImageProvider() {
		return (any, flavor) -> Icons.FORM_EDITOR__TITLE;
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return LABEL;
	}

	@Override
	protected HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		return contentBox(tag(level(), resource(title())));
	}

	private String level() {
		return getConfig().getLevel().toString().toLowerCase();
	}
	
	private ResKey title() {
		ResKey value = getConfig().getLabel();
		return ResKey.fallback(value, I18NConstants.FORM_EDITOR__TITLE_DEFAULT);
	}

	@Override
	protected DisplayDimension getDialogHeight() {
		return HEIGHT;
	}
}