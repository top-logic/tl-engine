/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.definition.GroupDefinition;
import com.top_logic.element.layout.formeditor.definition.GroupProperties;
import com.top_logic.element.layout.formeditor.definition.TextDefinition;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.template.model.FieldSetBoxTemplate;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.implementation.AbstractFormContainerProvider;
import com.top_logic.model.form.implementation.FormEditorContext;

/**
 * Creates a template for a {@link GroupDefinition}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class GroupDefinitionTemplateProvider extends AbstractFormContainerProvider<GroupDefinition> {

	/** CSS class for the header of the group. */
	public static final String PDF_HEADER_CSS = "header";

	/** CSS class for PDF export. */
	public static final String PDF_EXPORT_CSS = "group";

	static final DisplayDimension HEIGHT = dim(600, DisplayUnit.PIXEL);

	private static final ImageProvider IMAGE_PROVIDER =
		ImageProvider.constantImageProvider(Icons.FORM_EDITOR__GROUP);

	private static final String CSS_GROUP = "rf_columnsLayout rf_innerTarget";

	/**
	 * Creates a {@link GroupDefinitionTemplateProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GroupDefinitionTemplateProvider(InstantiationContext context, GroupDefinition config) {
		super(context, config);
	}

	@Override
	public void addCssClassForContent(List<HTMLTemplateFragment> buffer) {
		buffer.add(getIdAttribute());
		fillAttributes(buffer, getConfig());
	}

	private static void fillAttributes(List<HTMLTemplateFragment> formFieldTemplates, GroupProperties<?> config) {
		formFieldTemplates.add(css(getCssClasses(config)));
	}

	private static String getCssClasses(GroupProperties<?> config) {
		String cssGroup = CSS_GROUP;

		Integer columns = getColumns(config);
		if (columns != null) {
			cssGroup += " cols" + columns;
		}

		return cssGroup;
	}

	private static Integer getColumns(GroupProperties<?> config) {
		return config.getColumns() != null ? config.getColumns().getValue() : null;
	}

	@Override
	public HTMLTemplateFragment decorateContainer(HTMLTemplateFragment content, FormEditorContext context) {
		ConfigKey configKey = getConfigKey(context);

		return wrapFieldSet(getConfig(), getID(), content, configKey);
	}

	private ConfigKey getConfigKey(FormEditorContext context) {
		ResKey label = getConfig().getLabel();
		if (label != null && label.hasKey()) {
			return ConfigKey.derived(ConfigKey.field(context.getContentGroup()), label.getKey());
		} else {
			return ConfigKey.none();
		}
	}

	static HTMLTemplateFragment wrapFieldSet(GroupProperties<?> config, String id, HTMLTemplateFragment content) {
		return wrapFieldSet(config, id, content, ConfigKey.none());
	}

	static FieldSetBoxTemplate wrapFieldSet(GroupProperties<?> config, String id, HTMLTemplateFragment content,
			ConfigKey personalizationKey) {
		return init(fieldsetBox(createHeader(config), content, personalizationKey), id, config)
			.setCssClass(config.getCssClass())
			.setInitiallyCollapsed(!config.getInitiallyOpened());
	}

	private static HTMLTemplateFragment createHeader(GroupProperties<?> config) {
		return resource(label(config));
	}

	static ResKey label(TextDefinition config) {
		return ResKey.fallback(config.getLabel(), ResKey.text(StringServices.EMPTY_STRING));
	}

	private static FieldSetBoxTemplate init(FieldSetBoxTemplate template, String id, GroupProperties<?> config) {
		if (config.getColumns().getValue() != null) {
			template.setColumns(config.getColumns().getValue());
		}
		template.setDataId(id);
		template.setHasBorder(config.getShowBorder());
		template.setHasLegend(config.getShowTitle());
		if (config.getLabelPlacement().getLabelAbove() != null) {
			template.setLabelAbove(config.getLabelPlacement().getLabelAbove());
		}
		template.setPreventCollapse(!config.getCollapsibleValue());
		if (config.getStyle() != null) {
			template.setStyle(config.getStyle());
		}
		template.setWholeLine(config.getWholeLine());
		if (config.getWidth() != null) {
			template.setWidth(config.getWidth());
		}
		return template;
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return getConfig() != null ? getConfig().getWholeLine() : true;
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
		return I18NConstants.FORM_EDITOR__TOOL_NEW_GROUP;
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return HEIGHT;
	}

	@Override
	public boolean openDialog() {
		return true;
	}

	@Override
	protected String pdfExportCssClass() {
		return PDF_EXPORT_CSS;
	}

	@Override
	protected void writeHeader(DisplayContext context, TagWriter out) throws IOException {
		HTMLUtil.beginDiv(out, PDF_HEADER_CSS);
		out.writeText(context.getResources().getString(label(getConfig())));
		out.endTag(HTMLConstants.DIV);
	}

}
