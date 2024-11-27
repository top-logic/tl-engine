/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.FormEditorCSS;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.FrameDefinition;
import com.top_logic.util.css.CssUtil;

/**
 * Creates a template for a {@link FrameDefinition} and stores the necessary information.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FrameDefinitionTemplateProvider extends AbstractFormContainerProvider<FrameDefinition> {

	private static final DisplayDimension HEIGHT = dim(320, DisplayUnit.PIXEL);

	private static final boolean IS_TOOL = true;

	private static final ResKey LABEL = I18NConstants.FORM_EDITOR__TOOL_NEW_FRAME;

	/**
	 * Create a new {@link FrameDefinitionTemplateProvider} for a given {@link FrameDefinition} in a
	 * given {@link InstantiationContext}.
	 */
	public FrameDefinitionTemplateProvider(InstantiationContext context, FrameDefinition config) {
		super(context, config);
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return getWholeLine();
	}

	/**
	 * Whether {@link FrameDefinition#getWholeLine()} is configured.
	 */
	protected boolean getWholeLine() {
		return getConfig() != null ? getConfig().getWholeLine().booleanValue() : true;
	}

	@Override
	public boolean getIsTool() {
		return IS_TOOL;
	}

	@Override
	public ImageProvider getImageProvider() {
		return (any, flavor) -> Icons.FORM_EDITOR__FRAME;
	}

	@Override
	public ResKey getLabel(FormEditorContext editorContext) {
		return LABEL;
	}

	@Override
	public HTMLTemplateFragment decorateContainer(HTMLTemplateFragment content, FormEditorContext context) {
		boolean wholeLine = getWholeLine(context.getFormType());

		if (getID() != null) {
			HTMLTemplateFragment element = content;
			HTMLTemplateFragment container =
				div(getIdAttribute(), css(ReactiveFormCSS.RF_CONTAINER + " " + FormEditorCSS.FE_DROP_TARGET),
					element);
			return contentBox(div(attr("id", "columns-" + getID()), css(FormEditorCSS.FE_WRAPPER), container),
				wholeLine);
		} else {			
			return contentBox(content, wholeLine);
		}
	}

	@Override
	public void addCssClassForContent(List<HTMLTemplateFragment> buffer) {
		String cssFrame = "rf_frame";
		String cssClass = "";
		String cssInnerTarget = FormEditorCSS.FE_CONTAINER;
		String cssShowBorder = "showBorder";
		String cssWholeLine = ReactiveFormCSS.RF_LINE + " " + FormConstants.OVERFLOW_AUTO_CLASS;
		String cssColumns = ReactiveFormCSS.RF_COLUMNS_LAYOUT;
		cssColumns = getConfig().getColumns().appendColsCSSto(cssColumns);

		if (getConfig() != null) {
			cssClass = getConfig().getCssClass();
			cssShowBorder = getConfig().getShowBorder().booleanValue() ? "showBorder" : "";
			cssWholeLine = getWholeLine() ? cssWholeLine : "";
		}

		String cssClasses =
			CssUtil.joinCssClasses(cssFrame, cssClass, cssInnerTarget, cssShowBorder, cssWholeLine, cssColumns);

		buffer.add(css(cssClasses));
	}

	@Override
	protected DisplayDimension getDialogHeight() {
		return HEIGHT;
	}

}