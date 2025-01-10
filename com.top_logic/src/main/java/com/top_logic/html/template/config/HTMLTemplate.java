/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.config;

import java.io.IOException;
import java.util.Set;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.HTMLTemplateUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.LabelPositioning;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.model.annotate.LabelPosition;

/**
 * {@link HTMLTemplateFragment} that remembers its source code with all formatting.
 */
@Label("HTML Template")
@RenderWholeLine
@LabelPositioning(LabelPosition.ABOVE)
@Format(HTMLTemplateFragmentFormat.class)
@ControlProvider(MultiLineText.class)
public class HTMLTemplate implements HTMLTemplateFragment {

	private final HTMLTemplateFragment _template;

	private Set<String> _variables;

	private final String _html;

	/**
	 * Creates a {@link HTMLTemplate}.
	 * 
	 * <p>
	 * Internal API that should only be called from template parser, see
	 * {@link HTMLTemplateUtils#parse(String, String)}.
	 * </p>
	 */
	@FrameworkInternal
	public HTMLTemplate(HTMLTemplateFragment template, Set<String> variables, String html) {
		_template = template;
		_variables = variables;
		_html = html;
	}

	/**
	 * The template content.
	 */
	public HTMLTemplateFragment getTemplate() {
		return _template;
	}

	/**
	 * The template's source code.
	 */
	public String getHtml() {
		return _html;
	}

	/**
	 * Names of all accessed variables.
	 */
	public Set<String> getVariables() {
		return _variables;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		_template.write(context, out, properties);
	}

	@Override
	public int hashCode() {
		return _html.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		HTMLTemplate other = (HTMLTemplate) obj;
		return _html.equals(other._html);
	}

}
