/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.boxes.reactive_tag.FormEditorElementControl;
import com.top_logic.layout.form.boxes.reactive_tag.FormEditorElementProperties;
import com.top_logic.layout.form.template.model.internal.TemplateRenderer;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} creating a tuple of an icon-label-pair and an element (e.g. a
 * descriptionBox or a fieldSet for the form editor.
 * 
 * @see #getElement()
 * @see #getArguments()
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorElementTemplate implements Template {

	private final HTMLTemplateFragment _element;

	private final TypedAnnotatable _arguments;

	/**
	 * Create a new element for the form editor with given arguments.
	 * 
	 * @param element
	 *        The element which is rendered
	 * @param arguments
	 *        See {@link #getArguments()}.
	 */
	public FormEditorElementTemplate(HTMLTemplateFragment element, TypedAnnotatable arguments) {
		_element = element;
		_arguments = arguments;
	}

	/**
	 * Returns the element which is rendered in the tuple.
	 */
	public HTMLTemplateFragment getElement() {
		return _element;
	}

	/**
	 * Returns the arguments-map.
	 */
	public TypedAnnotatable getArguments() {
		return _arguments;
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		FormEditorElementControl formAttribute =
			new FormEditorElementControl(TemplateRenderer.toFragment(properties, getElement()));

		TypedAnnotatable arguments = getArguments();
		if (arguments.isSet(FormEditorElementProperties.FORM_EDITOR_FIRST_ELEMENT_HIDDEN)) {
			formAttribute
				.setAttributeHidden(arguments.get(FormEditorElementProperties.FORM_EDITOR_FIRST_ELEMENT_HIDDEN));
		}

		if (arguments.get(FormEditorElementProperties.FORM_EDITOR_DRAGGABLE) != null) {
			formAttribute.setDraggable(arguments.get(FormEditorElementProperties.FORM_EDITOR_DRAGGABLE));
		}

		if (arguments.isSet(FormEditorElementProperties.FORM_EDITOR_WHOLE_LINE)) {
			formAttribute.setRenderWholeLine(arguments.get(FormEditorElementProperties.FORM_EDITOR_WHOLE_LINE));
		}

		if (arguments.isSet(FormEditorElementProperties.FORM_EDITOR_IS_TOOL)) {
			formAttribute.setIsTool(arguments.get(FormEditorElementProperties.FORM_EDITOR_IS_TOOL));
		}

		if (arguments.isSet(FormEditorElementProperties.FORM_EDITOR_CONTROL)) {
			formAttribute.setFormEditorControl(arguments.get(FormEditorElementProperties.FORM_EDITOR_CONTROL));
		}

		if (arguments.isSet(FormEditorElementProperties.FORM_EDITOR_DATA_ID)) {
			formAttribute.setID(arguments.get(FormEditorElementProperties.FORM_EDITOR_DATA_ID));
		}

		if (arguments.isSet(FormEditorElementProperties.FORM_EDITOR_IMAGE_PROVIDER)) {
			formAttribute.setImageProvider(arguments.get(FormEditorElementProperties.FORM_EDITOR_IMAGE_PROVIDER));
		}

		if (arguments.isSet(FormEditorElementProperties.FORM_EDITOR_LABEL)) {
			formAttribute.setValue(arguments.get(FormEditorElementProperties.FORM_EDITOR_LABEL));
		}

		TemplateRenderer.renderFragment(displayContext, out, formAttribute);
	}
}