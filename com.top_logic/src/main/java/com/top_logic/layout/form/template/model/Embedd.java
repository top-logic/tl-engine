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
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} to be used as {@link AbstractView#getTemplate() template} for
 * embedding a field's display without rendering a separate control.
 * 
 * <p>
 * If the contents of {@link Embedd} is empty, the embedded contents is resolved from the template
 * annotated to the context field. If the contents of {@link Embedd} is non-empty, this inlined
 * contents is rendered directly.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Embedd extends AbstractFragment {

	Embedd(HTMLTemplateFragment contents) {
		super(contents);
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		getContents().write(displayContext, out, properties);
	}
}