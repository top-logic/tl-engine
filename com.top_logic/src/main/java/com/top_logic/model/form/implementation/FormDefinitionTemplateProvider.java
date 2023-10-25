/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.ImageProvider;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * Creates a template for a {@link FormDefinition}.
 * 
 * @see #createDesignTemplate(FormEditorContext)
 * @see #createDisplayTemplate(FormEditorContext)
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormDefinitionTemplateProvider extends AbstractFormContainerProvider<FormDefinition> {

	/**
	 * Create a new {@link FormDefinitionTemplateProvider} for a given {@link FormDefinition} in a
	 * given {@link InstantiationContext}.
	 */
	public FormDefinitionTemplateProvider(InstantiationContext context, FormDefinition config) {
		super(context, config);
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return false;
	}

	@Override
	public boolean getIsTool() {
		return false;
	}

	@Override
	public ImageProvider getImageProvider() {
		return null;
	}

	@Override
	public ResKey getLabel(FormEditorContext editorContext) {
		return I18NConstants.FORM_DEFINITION_LABEL;
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		return decorateContainer(createContentTemplate(context), context);
	}

	@Override
	public HTMLTemplateFragment createDesignTemplate(FormEditorContext context) {
		context.getFormEditorMapping().putMapping(context.getFormEditorControl(), getConfig());

		return decorateContainer(createContentTemplate(context), context);
	}

	@Override
	public HTMLTemplateFragment decorateContainer(HTMLTemplateFragment content, FormEditorContext context) {
		return contentBox(content, getWholeLine(context.getFormType()));
	}

	@Override
	public void addCssClassForContent(List<HTMLTemplateFragment> buffer) {
		super.addCssClassForContent(buffer);
		buffer.add(css(ColumnsDefinitionTemplateProvider.cssClassForColumnsLayout(getConfig())));
	}

}