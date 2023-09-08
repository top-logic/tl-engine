/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.EmptyCellDefinition;

/**
 * Creates a template for a {@link EmptyCellDefinition} and stores the necessary information.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class EmptyCellDefinitionTemplateProvider extends AbstractFormElementProvider<EmptyCellDefinition> {

	private static final boolean IS_TOOL = true;

	private static final ResKey LABEL = I18NConstants.FORM_EDITOR__TOOL_NEW_EMPTY_CELL;

	/**
	 * Create a new {@link EmptyCellDefinitionTemplateProvider} for a given
	 * {@link EmptyCellDefinition} in a given {@link InstantiationContext}.
	 */
	public EmptyCellDefinitionTemplateProvider(InstantiationContext context, EmptyCellDefinition config) {
		super(context, config);
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return false;
	}

	@Override
	public boolean getIsTool() {
		return IS_TOOL;
	}

	@Override
	public ImageProvider getImageProvider() {
		return ImageProvider.constantImageProvider(Icons.FORM_EDITOR__EMPTY_CELL);
	}

	@Override
	public ResKey getLabel(FormEditorContext editorContext) {
		return LABEL;
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		return contentBox(div(css("rf_inputCellOneLine rf_emptyCell"), empty()), getWholeLine(context.getFormType()));
	}

	@Override
	public void renderPDFExport(DisplayContext context, TagWriter out, FormEditorContext renderContext) throws IOException {
		// Nothing to render here
	}
}