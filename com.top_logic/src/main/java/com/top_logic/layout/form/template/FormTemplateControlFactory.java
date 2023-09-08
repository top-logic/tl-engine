/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import static com.top_logic.layout.form.template.FormTemplateConstants.*;

import java.io.IOException;

import org.w3c.dom.Element;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.model.internal.TemplateControl;

/**
 * Factory methods to create {@link Control}s for {@link FormTemplateConstants form templates}.
 * 
 * @deprecated Use {@link TagTemplate} with {@link TemplateControl}
 */
@Deprecated
public class FormTemplateControlFactory {

	/**
	 * Creates a {@link FormGroupControl} for
	 * {@link FormTemplateConstants#GROUP_TEMPLATE_ELEMENT}s and
	 * {@link FormListControl}s for
	 * {@link FormTemplateConstants#LIST_TEMPLATE_ELEMENT}s.
	 * 
	 * @param member
	 *        The underlying model.
	 * @param template
	 *        The template to render.
	 * @return The {@link Control} for rendering.
	 */
	public static HTMLFragment createFormTemplateControl(final FormMember member, FormTemplate template,
			Element rootElement) {
		if (! DOMUtil.hasNamespace(TEMPLATE_NS, rootElement)) {
			throw new UnsupportedOperationException("Template not supported: " + rootElement.getNamespaceURI() + "/" + rootElement.getLocalName());
		}
		
		// Evaluate inline template.
		if (DOMUtil.hasLocalName(GROUP_TEMPLATE_ELEMENT, rootElement)) {
			return new FormGroupControl((FormGroup) member, getInnerTemplate(template, rootElement));
		}
		else if (DOMUtil.hasLocalName(LIST_TEMPLATE_ELEMENT, rootElement)) {
			return new FormListControl((FormContainer) member, getInnerTemplate(template, rootElement));
		}
		else if (DOMUtil.hasLocalName(EMBEDD_TEMPLATE_ELEMENT, rootElement)) {
			FormTemplateControl control = (FormTemplateControl) template.getControlProvider().createControl(member);
			final FormTemplate childTemplate = control.getTemplate();
			final PatternRenderer renderer = childTemplate.getRenderer();
			return new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					renderer.writeContents(context, out, childTemplate, member, childTemplate.getRootElement());
				}
			};
		}
		else {
			throw new IllegalArgumentException("Template element expected, got: " + rootElement.getLocalName());
		}
	}

	private static FormTemplate getInnerTemplate(FormTemplate template, Element rootElement) {
		Element innerRootElement = DOMUtil.getFirstElementChild(rootElement);
		if (innerRootElement == null) {
			throw new IllegalArgumentException("The given rootElement must have at least one inner element");
		}
		return new FormTemplate(template.getResources(), template.getControlProvider(),
			template.hasAutomaticErrorDisplay(), innerRootElement);
	}

}
