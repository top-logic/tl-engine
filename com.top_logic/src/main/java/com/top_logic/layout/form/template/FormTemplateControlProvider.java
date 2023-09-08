/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.model.Templates;

/**
 * A {@link ControlProvider} that creates {@link FormTemplateControl}s.
 * <p>
 * The model given in {@link #createControl(Object, String)} has to be a {@link FormMember}.
 * </p>
 *
 * @deprecated Use {@link TemplateControlProvider} with a {@link TagTemplate} created by
 *             {@link Templates}.
 */
@Deprecated
public class FormTemplateControlProvider implements ControlProvider {

	private final FormTemplate _template;

	/** Creates a {@link FormTemplateControlProvider}. */
	public FormTemplateControlProvider(FormTemplate template) {
		_template = template;
	}

	@Override
	public Control createControl(Object model, String style) {
		return new FormTemplateControl((FormMember) model, _template);
	}

}
