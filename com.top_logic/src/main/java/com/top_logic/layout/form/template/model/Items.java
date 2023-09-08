/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;
import java.util.Iterator;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.model.internal.TemplateRenderer;
import com.top_logic.layout.template.WithProperties;

/**
 * Template that is expanded uniformly for each item of a {@link FormContainer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Items extends AbstractView {
	Items(HTMLTemplateFragment template) {
		super(template);
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		FormContainer group = (FormContainer) TemplateRenderer.model(properties);
		HTMLTemplateFragment template = getTemplate();

		for (Iterator<? extends FormMember> members = group.getMembers(); members.hasNext();) {
			FormMember member = members.next();

			TemplateRenderer.renderField(displayContext, out,
				member, MemberStyle.NONE, template, null, properties);
		}
	}
}