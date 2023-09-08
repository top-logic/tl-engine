/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;

/**
 * {@link Control} for a {@link FormContainer} to be rendered using a {@link Document} as template.
 * 
 * <p>
 * A template of the form
 * </p>
 * 
 * <xmp><t:items>[someContent]</t:items></xmp>
 * 
 * <p>
 * where <code>t</code> is bounded to the namespace {@link FormTemplateConstants#TEMPLATE_NS},
 * indicates that for each member of the model of this {@link FormListControl} the content
 * <code>someContent</code> is written.
 * </p>
 * 
 * <p>
 * If in <code>someContent</code> a tag of the form <code>&lt;p:self style='style'/></code> occurs
 * where <code>p</code> is bounded to the namespace {@link FormPatternConstants#PATTERN_NS}, then
 * the current member will be written by a control specified by the {@link ControlProvider} of this
 * {@link FormListControl}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormListControl extends FormTemplateControl {

	public FormListControl(FormContainer model, FormTemplate template) {
		super(model, template);
	}
	
	public FormListControl(FormContainer model, ControlProvider provider, Element listTemplateRoot, ResPrefix resPrefix) {
		this(model, new FormTemplate(resPrefix, provider, false, listTemplateRoot));
	}

	public FormListControl(FormContainer model, ControlProvider provider, Document template, ResPrefix resPrefix) {
		this(model, provider, template.getDocumentElement(), resPrefix);
	}

	/**
	 * @deprecated Better use
	 *             {@link #FormListControl(FormContainer, ControlProvider, Document, ResPrefix)}
	 *             with template constant created with {@link DOMUtil#parseThreadSafe(String)}.
	 */
	@Deprecated
	public FormListControl(FormContainer model, ControlProvider provider, String template, ResPrefix resPrefix) {
		this(model, provider, DOMUtil.parse(template), resPrefix);
	}

}
