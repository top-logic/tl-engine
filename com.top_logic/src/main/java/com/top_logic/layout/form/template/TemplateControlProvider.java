/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.model.Templates;

/**
 * {@link ControlProvider} that renders a {@link FormMember} using a template.
 * 
 * @deprecated Use {@link com.top_logic.layout.form.template.model.internal.TemplateControlProvider}
 *             with a {@link TagTemplate} created by {@link Templates}.
 */
@Deprecated
public class TemplateControlProvider implements ControlProvider {

	private final ControlProvider _inner;

	private final Document _templateDocument;

	private boolean _automaticErrorDisplay;

	/**
	 * Create a {@link TemplateControlProvider} without automatic error display.
	 * 
	 * @param template
	 *        The template to use for rendering. When the template is a shared instance, it must be
	 *        constructed with {@link DOMUtil#parseThreadSafe(String)}.
	 * @param inner
	 *        The {@link ControlProvider} that is used within the given template.
	 * 
	 * @see #TemplateControlProvider(Document, ControlProvider, boolean)
	 */
	public TemplateControlProvider(Document template, ControlProvider inner) {
		this(template, inner, false);
	}

	/**
	 * Creates a {@link TemplateControlProvider}.
	 * 
	 * @param template
	 *        The template to use for rendering. When the template is a shared instance, it must be
	 *        constructed with {@link DOMUtil#parseThreadSafe(String)}.
	 * @param inner
	 *        The {@link ControlProvider} that is used within the given template.
	 * @param automaticErrorDisplay
	 *        Whether to append an error display view after each field in the template.
	 */
	public TemplateControlProvider(Document template, ControlProvider inner, boolean automaticErrorDisplay) {
		_templateDocument = template;
		_automaticErrorDisplay = automaticErrorDisplay;
		_inner = inner;
	}

	@Override
	public Control createControl(Object model, String style) {
		FormMember member = (FormMember) model;
		FormTemplate template =
			new FormTemplate(member.getResources(), _inner, _automaticErrorDisplay, _templateDocument);
		return new FormTemplateControl(member, template);
	}

}
