/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.messagebox.AbstractFormDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractFormDialog} to create a new {@link Map} entry for a {@link PropertyDescriptor}
 * with a {@link MapBinding}.
 * 
 * @see ComplexEditor
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class MapEntryBuilderDialog extends AbstractFormDialog {

	FormGroup _group;

	private final Document _template;

	MapFormGroupBuilder _builder;

	/**
	 * Creates a new {@link AbstractFormDialog} to add map entries.
	 */
	public MapEntryBuilderDialog(MapFormGroupBuilder builder, ResKey propertyKey) {
		super(createDialogModel(propertyKey));

		_builder = builder;
		_group = builder.createDialogContentGroup();
		_template = createTemplate();
	}

	private Document createTemplate() {
		return DOMUtil.parseThreadSafe(
				"<t:group"
				+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
				+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
				+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
				+ ">"
				+ "<div>"
				+ "<p:field name='" + _group.getName() + "'>"
				+ "<t:list>"
				+ "<t:items>"
				+ "<p:self>"
				+ "<t:group>"
				+ "<div>"
				+ "<p/>"
				+ "<p:field style='" + FormTemplateConstants.STYLE_LABEL_WITH_COLON_VALUE + "' name='item.key' />"
				+ "<p:field style='error' name='item.key' />"
				+ "<p:field style='input' name='" + "item.key" + "'/>"
				+ "<p/>"
				+ "<p:field style='" + FormTemplateConstants.STYLE_LABEL_WITH_COLON_VALUE + "' name='item.value' />"
				+ "<p:field style='error' name='item.value' />"
				+ "<p:field style='input' name='" + "item.value" + "'/>"
				+ "</div>"
				+ "</t:group>"
				+ "</p:self>"
				+ "</t:items>"
				+ "</t:list>"
				+ "</p:field>"
				+ "</div>"
				+ "</t:group>");
	}

	private static DefaultDialogModel createDialogModel(ResKey propertyKey) {
		LayoutData dialogLayout = createDialogLayout();
		DisplayValue dialogTitle = createDialogTitle(propertyKey);

		return new DefaultDialogModel(dialogLayout, dialogTitle, true, true, null);
	}

	private static ResourceText createDialogTitle(ResKey propertyKey) {
		return new ResourceText(I18NConstants.ADD_ELEMENT__PROPERTY.fill(propertyKey));
	}

	private static DefaultLayoutData createDialogLayout() {
		DisplayDimension width = DisplayDimension.dim(30, DisplayUnit.PERCENT);
		DisplayDimension height = DisplayDimension.dim(200, DisplayUnit.PIXEL);

		return new DefaultLayoutData(width, 100, height, 100, Scrolling.AUTO);
	}

	@Override
	protected FormTemplate getTemplate() {
		return defaultTemplate(_template, true, I18NConstants.CREATE_MAP_ENTRY_DIALOG);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		Command addOperation = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext addContext) {
				FormContext group = getFormContext();
				boolean ok = group.checkAll();
				if (!ok) {
					return AbstractApplyCommandHandler.createErrorResult(group);
				}

				_builder.moveContentGroup(_group);
				return getDialogModel().getCloseAction().executeCommand(addContext);
			}
		};

		CommandModel okButton = MessageBox.button(ButtonType.OK, addOperation);
		buttons.add(okButton);
		getDialogModel().setDefaultCommand(okButton);
		buttons.add(MessageBox.button(ButtonType.CANCEL, getDialogModel().getCloseAction()));
	}

	@Override
	protected void fillFormContext(FormContext context) {
		context.addMember(_group);
	}

}
