/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.model.FormGroup;

/**
 * Control for a {@link FormGroup} to be rendered using a {@link Document} as template.
 * 
 * <p>
 * If a member of the {@link FormGroup} of this {@link FormGroupControl} shall be rendered then the
 * document needs a tag of the form. <code>&lt;p:field name='field' style='style'/></code> where
 * <code>p</code> is bounded to the namespace {@link FormPatternConstants#PATTERN_NS}. The control
 * build for such an element is given by the {@link ControlProvider} of this control.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormGroupControl extends FormTemplateControl {

	public FormGroupControl(FormGroup model, FormTemplate template) {
		super(model, template);
	}
	
	public FormGroupControl(FormGroup model, ControlProvider provider, Element groupTemplateRoot, ResPrefix resPrefix) {
		this(model, new FormTemplate(resPrefix, provider, false, groupTemplateRoot));
	}
	
	public FormGroupControl(FormGroup model, ControlProvider provider, Document groupTemplate, ResPrefix resPrefix) {
		this(model, provider, groupTemplate.getDocumentElement(), resPrefix);
	}

	/**
	 * @deprecated Better use
	 *             {@link #FormGroupControl(FormGroup, ControlProvider, Document, ResPrefix)} with a
	 *             constant template build from {@link DOMUtil#parseThreadSafe(String)}.
	 */
	@Deprecated
	public FormGroupControl(FormGroup model, ControlProvider provider, String template, ResPrefix resPrefix) {
		this(model, provider, DOMUtil.parse(template), resPrefix);
	}

	/**
	 * @deprecated {@link ErrorControl}s should just be constructed if the template defines it so
	 *             define in template and call corresponding constructor without id and without the
	 *             boolean.
	 */
	@Deprecated
	public FormGroupControl(FormGroup model, ControlProvider provider, String template, boolean automaticErrorDisplay,
			ResPrefix resPrefix) {
		this(model, new FormTemplate(resPrefix, provider, automaticErrorDisplay, DOMUtil.parse(template)));
	}

}
