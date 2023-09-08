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
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.Columns;
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
		return ImageProvider.constantImageProvider(Icons.FORM_EDITOR__FRAME);
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
				div(getIdAttribute(), css("rf_container rf_dropTarget"), element);
			return contentBox(div(attr("id", "columns-" + getID()), css("rf_wrapper"), container), wholeLine);
		} else {			
			return contentBox(content, wholeLine);
		}
	}

	@Override
	public void addCssClassForContent(List<HTMLTemplateFragment> buffer) {
		Integer columns = getColumns();

		String cssFrame = "rf_frame";
		String cssClass = "";
		String cssInnerTarget = "rf_innerTarget";
		String cssShowBorder = "showBorder";
		String cssWholeLine = "rf_line";
		String cssColumns = "rf_columnsLayout";
		cssColumns += columns != null ? " cols" + columns : "";

		if (getConfig() != null) {
			cssClass = getConfig().getCssClass();
			cssShowBorder = getConfig().getShowBorder().booleanValue() ? "showBorder" : "";
			cssWholeLine = getWholeLine() ? "rf_line" : "";
		}

		String cssClasses =
			CssUtil.joinCssClasses(cssFrame, cssClass, cssInnerTarget, cssShowBorder, cssWholeLine, cssColumns);

		buffer.add(css(cssClasses));
	}

	private Integer getColumns() {
		Columns columns = getConfig().getColumns();
		return Columns.DEFAULT.equals(columns) ? null : columns.getValue();
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return HEIGHT;
	}

	@Override
	protected String pdfExportCssClass() {
		return "frame";
	}
}