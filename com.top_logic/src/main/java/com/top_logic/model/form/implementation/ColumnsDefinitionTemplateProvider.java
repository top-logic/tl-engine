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
import com.top_logic.model.form.definition.ColumnsDefinition;
import com.top_logic.model.form.definition.ContainerProperties;

/**
 * Creates a template for a {@link ColumnsDefinition} and stores the necessary information.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ColumnsDefinitionTemplateProvider extends AbstractFormContainerProvider<ColumnsDefinition> {

	private static final DisplayDimension HEIGHT = dim(220, DisplayUnit.PIXEL);

	private static final boolean IS_TOOL = true;

	private static final ImageProvider IMAGE_PROVIDER = ImageProvider.constantImageProvider(Icons.FORM_EDITOR__COLUMNS);

	private static final ResKey LABEL = I18NConstants.FORM_EDITOR__TOOL_NEW_COLUMNSLAYOUT;

	private static final String CSS_COLUMNS = "rf_columnsLayout rf_innerTarget";

	/**
	 * Create a new {@link ColumnsDefinitionTemplateProvider} for a given {@link ColumnsDefinition}
	 * in a given {@link InstantiationContext}.
	 */
	public ColumnsDefinitionTemplateProvider(InstantiationContext context, ColumnsDefinition config) {
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
		return IMAGE_PROVIDER;
	}

	@Override
	public ResKey getLabel(FormEditorContext editorContext) {
		return LABEL;
	}

	@Override
	public HTMLTemplateFragment decorateContainer(HTMLTemplateFragment content, FormEditorContext context) {
		HTMLTemplateFragment container =
			div(getIdAttribute(), css("rf_container rf_dropTarget"), content);
		HTMLTemplateFragment elementWrapper = div(attr("id", "columns-" + getID()), css("rf_wrapper"), container);

		return contentBox(elementWrapper);
	}

	@Override
	public void addCssClassForContent(List<HTMLTemplateFragment> buffer) {
		buffer.add(getIdAttribute());
		buffer.add(css(getCssClass()));
	}

	String getCssClass() {
		ColumnsDefinition config = getConfig();
		String cssGroup = cssClassForColumnsLayout(config);

		cssGroup += !config.getLineBreak().booleanValue() ? " keep" : "";

		return cssGroup;
	}

	/**
	 * Constructs a CSS class that enables client-side form layouting in reactive columns.
	 */
	public static String cssClassForColumnsLayout(ContainerProperties<?> config) {
		String cssGroup = CSS_COLUMNS;

		Integer columns = getColumns(config);
		if (columns != null) {
			cssGroup += " cols" + columns;
		}
		return cssGroup;
	}

	static private Integer getColumns(ContainerProperties<?> config) {
		Columns columns = config.getColumns();
		return columns != null ? columns.getValue() : null;
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return HEIGHT;
	}

	@Override
	protected String pdfExportCssClass() {
		return "columns";
	}
}