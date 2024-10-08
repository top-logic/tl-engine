/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.formeditor.definition.GroupDefinition;
import com.top_logic.element.layout.formeditor.definition.GroupProperties;
import com.top_logic.element.layout.formeditor.definition.TextDefinition;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.template.model.FieldSetBoxTemplate;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.Columns;
import com.top_logic.model.form.implementation.AbstractFormContainerProvider;
import com.top_logic.model.form.implementation.FormEditorContext;

/**
 * Creates a template for a {@link GroupDefinition}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class GroupDefinitionTemplateProvider extends AbstractFormContainerProvider<GroupDefinition> {

	static final DisplayDimension HEIGHT = dim(600, DisplayUnit.PIXEL);

	private static final ImageProvider IMAGE_PROVIDER =
		(any, flavor) -> Icons.FORM_EDITOR__GROUP;

	private static final String CSS_GROUP =
		ReactiveFormCSS.RF_COLUMNS_LAYOUT + " " + ReactiveFormCSS.RF_INNER_TARGET;

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
		Columns columns = config.getColumns();
		return columns.appendColsCSSto(CSS_GROUP);
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
		template.setLabelPlacement(config.getLabelPlacement());
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
	protected DisplayDimension getDialogHeight() {
		return HEIGHT;
	}

	@Override
	public boolean openDialog() {
		return true;
	}

}
