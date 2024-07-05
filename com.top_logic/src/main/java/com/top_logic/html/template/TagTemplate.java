/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.template.model.Tag;
import com.top_logic.layout.template.WithProperties;

/**
 * A well-formed tag structure of begin tag, content and end tag.
 */
public class TagTemplate implements Tag {

	private final StartTagTemplate _start;

	private final HTMLTemplateFragment _content;

	/**
	 * Creates a {@link TagTemplate}.
	 */
	public TagTemplate(StartTagTemplate start, HTMLTemplateFragment content) {
		_start = start;
		_content = content;
	}

	@Override
	public StartTagTemplate getStart() {
		return _start;
	}

	@Override
	public HTMLTemplateFragment getContent() {
		return _content;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		_start.write(context, out, properties);
		if (_start.isEmpty()) {
			out.endEmptyTag();
		} else {
			int depth = out.getDepth();
			try {
				_content.write(context, out, properties);
			} catch (Throwable exception) {
				// Element content is expected, therefore the safe-point can be restored
				// unconditionally.
				out.endAll(depth);
				HTMLTemplateUtils.renderError(context, out, exception);
			}
			out.endTag(_start.getName());
		}
	}

}
