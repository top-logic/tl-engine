/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model.internal;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.model.Embedd;

/**
 * Dummy {@link ControlProvider} to annotate an {@link Embedd} to a {@link FormMember} and use this
 * template without writing an additional control.
 * 
 * @implNote {@link #createControl(Object, String)} must not be called.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateAnnotation extends AbstractTemplateControlProvider {

	/**
	 * Creates a {@link TemplateAnnotation}.
	 * 
	 * @param template
	 *        The template to use.
	 */
	public TemplateAnnotation(Embedd template) {
		super(template);
	}

	@Override
	public Control createControl(Object model, String style) {
		throw new UnsupportedOperationException("Do not create control. The template must be rendered externally.");
	}
}