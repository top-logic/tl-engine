/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model.internal;

import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} that renders {@link FormMember}s based on {@link HTMLTemplateFragment}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateControlProvider extends AbstractTemplateControlProvider {

	private ControlProvider _controlProvider;

	/**
	 * Creates a {@link TemplateControlProvider}.
	 * 
	 * @param template
	 *        The template to use.
	 * @param controlProvider
	 *        The {@link ControlProvider} to use for rendering referenced {@link FormMember}s that
	 *        have no content template.
	 */
	public TemplateControlProvider(HTMLTemplateFragment template, ControlProvider controlProvider) {
		super(template);
		_controlProvider = controlProvider;
	}

	@Override
	public Control createControl(Object model, String style) {
		return new TemplateControl((FormMember) model, _controlProvider, getTemplate());
	}
}