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
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.model.internal.TemplateRenderer;
import com.top_logic.layout.template.WithProperties;

/**
 * Template for referencing the context member.
 * 
 * <p>
 * This template is in the {@link AbstractFragment#getContents()} of a {@link Member} or
 * {@link Items} template to actually render the input element.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Self extends AbstractMember {

	Self(MemberStyle style, HTMLTemplateFragment template, ControlProvider cp) {
		super(style, template, cp);
	}

	@Override
	public String getName() {
		return ".";
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		FormMember currentMember = (FormMember) TemplateRenderer.model(properties);
		TemplateRenderer.renderField(displayContext, out,
			currentMember, getStyle(), getTemplate(), getControlProvider(), properties);
	}
}