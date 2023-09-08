/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.config;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} that remembers its source code with all formatting.
 */
public class ConfiguredTemplate implements HTMLTemplateFragment {

	private final HTMLTemplateFragment _template;

	private final String _html;

	/**
	 * Creates a {@link ConfiguredTemplate}.
	 */
	public ConfiguredTemplate(HTMLTemplateFragment template, String html) {
		_template = template;
		_html = html;
	}

	/**
	 * The template's source code.
	 */
	public String getHtml() {
		return _html;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		_template.write(context, out, properties);
	}

}
