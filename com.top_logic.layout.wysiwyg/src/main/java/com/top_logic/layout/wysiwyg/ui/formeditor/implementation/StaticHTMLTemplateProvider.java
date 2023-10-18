/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.formeditor.implementation;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;

/**
 * {@link AbstractFormElementProvider} for {@link StaticHTML}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StaticHTMLTemplateProvider extends AbstractFormElementProvider<StaticHTML> {

	private static final ImageProvider IMAGE_PROVIDER =
		ImageProvider.constantImageProvider(Icons.STATIC_HTML_CONTENT);

	/**
	 * Creates a new {@link StaticHTMLTemplateProvider}.
	 */
	public StaticHTMLTemplateProvider(InstantiationContext context, StaticHTML config) {
		super(context, config);
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return getConfig().getWholeLine();
	}

	@Override
	public boolean getIsTool() {
		return true;
	}

	@Override
	public ImageProvider getImageProvider() {
		return IMAGE_PROVIDER;
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return I18NConstants.STATIC_HTML_LABEL;
	}

	@Override
	public DisplayDimension getDialogWidth() {
		return DisplayDimension.dim(680, DisplayUnit.PIXEL);
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return DisplayDimension.dim(470, DisplayUnit.PIXEL);
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		String content = getConfig().getContent();
		boolean wholeLine = getWholeLine(context.getFormType());

		if (!content.isEmpty()) {
			return contentBox(htmlSource(content), wholeLine);
		} else {
			return contentBox(div(
				css(inputCellCSS(context) + " " + ReactiveFormCSS.RF_EMPTY_CELL),
				resource(I18NConstants.STATIC_HTML_LABEL)), wholeLine);
		}
	}

}

