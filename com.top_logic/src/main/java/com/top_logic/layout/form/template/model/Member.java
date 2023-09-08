/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.model.internal.TemplateRenderer;
import com.top_logic.layout.template.WithProperties;

/**
 * Template for referencing an {@link FormField} or {@link FormContainer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Member extends AbstractMember {
	private final String _name;

	Member(String name, MemberStyle style, HTMLTemplateFragment template, ControlProvider cp) {
		super(style, template, cp);

		_name = name;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		TemplateRenderer.renderFieldByName(displayContext, out,
			getName(), getStyle(), getTemplate(), getControlProvider(), properties);
	}
}