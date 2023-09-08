/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.LiteralTemplate;
import com.top_logic.html.template.expr.StringLiteral;
import com.top_logic.layout.DisplayContext;

/**
 * Template for rendering internationalized text.
 * 
 * @see StringLiteral
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Resource extends AbstractResourceTemplate implements LiteralTemplate {

	Resource(ResKey key) {
		super(key);
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out) throws IOException {
		out.writeText(displayContext.getResources().getString(getKey()));
	}

}