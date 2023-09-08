/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.boxes.reactive_tag.FormEditorElementProperties;
import com.top_logic.model.form.definition.FormElement;

/**
 * Creates a form element template for a {@link FormElement} for a form editor.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorElementTemplateProvider {

	private static TypedAnnotatable createArgumentsFormEditor(FormElementTemplateProvider formElement, FormEditorContext editorContext) {
		TypedAnnotatable arguments = new LazyTypedAnnotatable();
		arguments.set(FormEditorElementProperties.FORM_EDITOR_DRAGGABLE, isDraggable(editorContext));
		arguments.set(FormEditorElementProperties.FORM_EDITOR_WHOLE_LINE,
			formElement.getWholeLine(editorContext.getFormType()));
		arguments.set(FormEditorElementProperties.FORM_EDITOR_DATA_ID, formElement.getID());
		arguments.set(FormEditorElementProperties.FORM_EDITOR_IS_TOOL, formElement.getIsTool());
		arguments.set(FormEditorElementProperties.FORM_EDITOR_FIRST_ELEMENT_HIDDEN, editorContext.isAttributeHidden());
		arguments.set(FormEditorElementProperties.FORM_EDITOR_LABEL, formElement.getLabel(editorContext));
		if (formElement.getName() != null)
			arguments.set(FormEditorElementProperties.FORM_EDITOR_NAME, formElement.getName());
		arguments.set(FormEditorElementProperties.FORM_EDITOR_CONTROL, editorContext.getFormEditorControl());
		arguments.set(FormEditorElementProperties.FORM_EDITOR_IMAGE_PROVIDER, formElement.getImageProvider());

		return arguments;
	}

	/**
	 * Creates a template for a form editor element. As content it takes the specific
	 * {@link FormElement} element template. It also creates an arguments mapping.
	 * 
	 * @see #createArgumentsFormEditor(FormElementTemplateProvider, FormEditorContext)
	 * 
	 * @return The {@link HTMLTemplateFragment} for this form editor element.
	 */
	public static HTMLTemplateFragment wrapFormEditorElement(HTMLTemplateFragment formTemplate,
			FormElementTemplateProvider formElement,
			FormEditorContext editorContext) {
		TypedAnnotatable arguments = createArgumentsFormEditor(formElement, editorContext);
		return formEditorElement(formTemplate, arguments);
	}

	private static boolean isDraggable(FormEditorContext editorContext) {
		return editorContext.isInEditMode() && !editorContext.isLocked();
	}
}