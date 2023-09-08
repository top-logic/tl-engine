/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * A template that does not use template variables.
 */
public interface LiteralTemplate extends HTMLTemplateFragment, HTMLFragment {

	@Override
	default void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		write(context, out);
	}

}
