/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.DefaultExpansionModel;
import com.top_logic.layout.form.boxes.reactive_tag.DefaultGroupSettings;
import com.top_logic.layout.form.boxes.reactive_tag.GroupCellControl;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormEditorMapping;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;

/**
 * A toolbox with buttons to create elements for the form editor.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorToolboxControl extends FormEditorDisplayControl {
	
	private final static String CSS = "cFormEditorToolbox";

	private final static String INNER_CSS = ReactiveFormCSS.RF_LINE + " rf_buttons";

	private List<Class<? extends FormElement<? extends FormElementTemplateProvider>>> _buttonElements;

	private FormEditorMapping _formEditorMapping;

	/**
	 * Create a toolbox with buttons for a form editor.
	 * 
	 * @param model
	 *        Annotated template for this form.
	 * @param classType
	 *        {@link TLClass} to take the attributes of.
	 * @param resPrefix
	 *        The {@link ResPrefix}.
	 * @param isInEditMode
	 *        Whether the page is in edit mode or not.
	 * @param formEditorMapping
	 *        The current {@link FormEditorMapping}.
	 */
	protected FormEditorToolboxControl(FormDefinition model, TLStructuredType classType, ResPrefix resPrefix,
			boolean isInEditMode, FormEditorMapping formEditorMapping,
			List<Class<? extends FormElement<? extends FormElementTemplateProvider>>> buttonElements) {
		super(classType, model, resPrefix, isInEditMode);
		_formEditorMapping = formEditorMapping;
		_buttonElements = buttonElements;
	}
	
	@Override
	void writeContent(DisplayContext context, TagWriter out, AttributeFormContext formContext) throws IOException {
		if (_isInEditMode) {
			createTemplate(formContext);
			Control buttons = createControl(formContext);

			GroupCellControl group = new GroupCellControl(buttons, new DefaultExpansionModel(false),
				new DefaultGroupSettings().setColumns(1));
			group.setTitle(Fragments.message(I18NConstants.FORM_EDITOR__TOOLBOX));
			group.write(context, out);
		}
	}

	private void createTemplate(AttributeFormContext fc) {
		List<HTMLTemplateFragment> formFieldTemplates = new ArrayList<>();
		for (Class<? extends FormElement<? extends FormElementTemplateProvider>> elementType : _buttonElements) {
			FormElement<?> config = TypedConfiguration.newConfigItem(elementType);
			FormElementTemplateProvider templateProvider = TypedConfigUtil.createInstance(config);

			FormEditorContext formContext = new FormEditorContext.Builder()
				.formMode(FormMode.DESIGN)
				.formContext(fc)
				.contentGroup(fc)
				.frameScope(getFrameScope())
				.formEditorMapping(_formEditorMapping)
				.editMode(_isInEditMode)
				.build();

			formFieldTemplates.add(templateProvider.createTemplate(formContext));
		}

		FormEditorUtil.template(fc,
			div(css(INNER_CSS), horizontalBox(formFieldTemplates)));
	}

	@Override
	protected String getTypeCssClass() {
		return CSS;
	}
}