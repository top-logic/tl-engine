/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.form.definition.ContainerDefinition;
import com.top_logic.model.form.definition.LabelPlacement;

/**
 * {@link FormElementTemplateProvider} which contains other {@link FormElementTemplateProvider}s within.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public abstract class AbstractFormContainerProvider<T extends ContainerDefinition<?>>
		extends AbstractFormElementProvider<T> {

	/** CSS class for the content area in PDF export. */
	public static final String PDF_CONTENT_CSS = "content";

	private static final int DEFAULT_COLUMN_COUNT = 2;

	List<FormElementTemplateProvider> _content;

	/**
	 * Create a new {@link AbstractFormContainerProvider} for a given {@link ContainerDefinition} in
	 * a given {@link InstantiationContext}. Holds the content of the container.
	 * 
	 * @see ContainerDefinition#getContent()
	 */
	public AbstractFormContainerProvider(InstantiationContext context, T config) {
		super(context, config);
		_content = TypedConfiguration.getInstanceList(context, config.getContent());
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		return decorateContainer(createContentTemplate(context), context);
	}

	/**
	 * Decorates the given {@link HTMLTemplateFragment} with visible properties for the concrete
	 * container type.
	 * 
	 * @param content
	 *        Elements inside the container.
	 * @param context
	 *        The {@link FormEditorContext} to create the template.
	 * 
	 * @return The created {@link HTMLTemplateFragment}.
	 */
	public abstract HTMLTemplateFragment decorateContainer(HTMLTemplateFragment content, FormEditorContext context);

	@Override
	public HTMLTemplateFragment createDesignTemplate(FormEditorContext context) {
		setID(context.getFrameScope().createNewID());
		context.getFormEditorMapping().putMapping(getID(), getConfig());
		HTMLTemplateFragment formTemplate = decorateContainer(createContentTemplate(context), context);
		// puts the created form template in a form editor element to display inside a form editor
		return FormEditorElementTemplateProvider.wrapFormEditorElement(formTemplate, this, context);
	}

	/**
	 * Create a {@link HTMLTemplateFragment} for the content of a
	 * {@link FormElementTemplateProvider}.
	 * 
	 * @param context
	 *        The {@link FormEditorContext} to create the template.
	 * 
	 * @see #addCssClassForContent(List)
	 * 
	 * @return The created {@link HTMLTemplateFragment}.
	 */
	public HTMLTemplateFragment createContentTemplate(FormEditorContext context) {
		List<HTMLTemplateFragment> formFieldTemplates = new ArrayList<>();

		addCssClassForContent(formFieldTemplates);

		LabelPlacement labelPlacement = getConfig().getLabelPlacement();
		for (FormElementTemplateProvider content : getContent()) {
			if (content.isVisible(context.getFormType(), context.getFormMode())) {
				HTMLTemplateFragment innerTemplate =
					context.withLabelPlacement(labelPlacement, content::createTemplate);
				formFieldTemplates.add(innerTemplate);
			}
		}

		return div(formFieldTemplates);
	}
	
	/**
	 * Adds the attribute tempates of the container element to the given list.
	 * 
	 * <p>
	 * The container element is rendered directly around the {@link FormElementTemplateProvider}s of
	 * the content.
	 * </p>
	 * 
	 * @param buffer
	 *        Buffer of {@link HTMLTemplateFragment}s to add attribute {@link HTMLTemplateFragment}s
	 *        to.
	 */
	public void addCssClassForContent(List<HTMLTemplateFragment> buffer) {
		// do nothing here, but needed for subclasses
	}

	@Override
	public void renderPDFExport(DisplayContext context, TagWriter out, FormEditorContext renderContext)
			throws IOException {
		HTMLUtil.beginDiv(out, pdfExportCssClass());

		writeHeader(context, out);
		writeContent(context, out, renderContext);

		out.endTag(HTMLConstants.DIV);
	}

	/**
	 * {@link FormElementTemplateProvider} specific CSS class for the PDF export.
	 * 
	 * @return CSS class for the top level HTML element. May be <code>null</code>.
	 * 
	 * @see #renderPDFExport(DisplayContext, TagWriter, FormEditorContext)
	 */
	protected String pdfExportCssClass() {
		return null;
	}

	private void writeContent(DisplayContext context, TagWriter out, FormEditorContext renderContext)
			throws IOException {
		HTMLUtil.beginDiv(out, PDF_CONTENT_CSS);

		Integer configuredCols = getConfig().getColumns().getValue();
		int numberColumns;
		if (configuredCols != null) {
			numberColumns = configuredCols.intValue();
		} else {
			numberColumns = DEFAULT_COLUMN_COUNT;
		}
		FormElementTemplateProvider[] visibleContent = getContent()
			.stream()
			.filter(content -> content.isVisible(renderContext.getFormType(), FormMode.EDIT))
			.toArray(FormElementTemplateProvider[]::new);
		int numberContent = visibleContent.length;
		int elementIndex = 0;
		if (elementIndex < numberContent) {
			beginTable(out);
			do {
				beginRow(out);
				for (int colIndex = 0; colIndex < numberColumns; colIndex++) {
					if (elementIndex == numberContent) {
						// allWritten;
						beginCell(out, numberColumns, numberColumns - colIndex);
						out.write(HTMLConstants.NBSP);
						endCell(out);
						break;
					}

					FormElementTemplateProvider content = visibleContent[elementIndex++];
					if (!content.getWholeLine(renderContext.getFormType())) {
						beginCell(out, numberColumns);
						content.renderPDFExport(context, out, renderContext);
						endCell(out);
					} else if (colIndex == 0) {
						beginCell(out, numberColumns, numberColumns);
						content.renderPDFExport(context, out, renderContext);
						endCell(out);
						break;
					} else {
						elementIndex--;
						// endRow for wholeLine;
						beginCell(out, numberColumns, numberColumns - colIndex);
						out.write(HTMLConstants.NBSP);
						endCell(out);
						break;
					}
				}
				endRow(out);

			} while (elementIndex < numberContent);
			endTable(out);
		}
		out.endTag(HTMLConstants.DIV);
	}

	/**
	 * Write the header for the written grouping {@link HTMLConstants#DIV}.
	 * 
	 * @param context
	 *        Render context.
	 * @param out
	 *        {@link TagWriter} to write header to.
	 * @throws IOException
	 *         When writing to given output fails.
	 */
	protected void writeHeader(DisplayContext context, TagWriter out) throws IOException {
		// Nothing to write here.
	}

	private static String columnClass(int numberColumns) {
		String colClass;
		switch (numberColumns) {
			case 1:
				colClass = "col1";
				break;
			case 2:
				colClass = "col2";
				break;
			case 3:
				colClass = "col3";
				break;
			case 4:
				colClass = "col4";
				break;
			case 5:
				colClass = "col5";
				break;
			default:
				colClass = null;
		}
		return colClass;
	}

	private static void beginRow(TagWriter out) {
		out.beginTag(HTMLConstants.TR);
	}

	private static void endRow(TagWriter out) {
		out.endTag(HTMLConstants.TR);
	}

	private static void beginTable(TagWriter out) {
		out.beginTag(HTMLConstants.TABLE);
	}

	private static void endTable(TagWriter out) {
		out.endTag(HTMLConstants.TABLE);
	}

	private static void beginCell(TagWriter out, int numberColumns, int colspan) {
		out.beginBeginTag(HTMLConstants.TD);
		if (colspan > 1) {
			out.writeAttribute(HTMLConstants.COLSPAN_ATTR, colspan);
		}
		out.writeAttribute(HTMLConstants.CLASS_ATTR, columnClass(numberColumns / colspan));
		out.endBeginTag();
	}

	private static void beginCell(TagWriter out, int numberColumns) {
		beginCell(out, numberColumns, 1);
	}

	private static void endCell(TagWriter out) {
		out.endTag(HTMLConstants.TD);
	}

	/**
	 * Returns the content of the configuration which is a {@link ContainerDefinition}.
	 * 
	 * @return The content as list.
	 */
	public List<FormElementTemplateProvider> getContent() {
		return _content;
	}

}