/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.SeparatorDefinition;

/**
 * Creates a template for a {@link SeparatorDefinition} and stores the necessary information.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class SeparatorDefinitionTemplateProvider extends AbstractFormElementProvider<SeparatorDefinition> {

	private static final DisplayDimension HEIGHT = dim(120, DisplayUnit.PIXEL);

	private static final boolean IS_TOOL = true;

	private static final ResKey LABEL = I18NConstants.FORM_EDITOR__TOOL_NEW_SEPARATOR;

	/**
	 * Create a new {@link SeparatorDefinitionTemplateProvider} for a given
	 * {@link SeparatorDefinition} in a given {@link InstantiationContext}.
	 */
	public SeparatorDefinitionTemplateProvider(InstantiationContext context, SeparatorDefinition config) {
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
		return ImageProvider.constantImageProvider(Icons.FORM_EDITOR__SEPARATOR);
	}

	@Override
	public ResKey getLabel(FormEditorContext editorContext) {
		return LABEL;
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		boolean visible = getConfig() != null ? getConfig().getVisible().booleanValue() : true;
		String visibleCss = visible ? " visible" : "";

		return contentBox(div(css("rf_hr " + ReactiveFormCSS.RF_LINE + visibleCss)));
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return HEIGHT;
	}

}