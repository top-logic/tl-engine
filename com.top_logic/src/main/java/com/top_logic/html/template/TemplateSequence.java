/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * A concatenation of {@link HTMLTemplateFragment}s.
 */
public class TemplateSequence implements RawTemplateFragment {

	private final List<? extends HTMLTemplateFragment> _contents;

	/**
	 * Creates a {@link TemplateSequence}.
	 * 
	 * @param contents
	 *        The {@link HTMLTemplateFragment}s to render in sequence.
	 */
	public TemplateSequence(List<? extends HTMLTemplateFragment> contents) {
		_contents = contents;
	}

	/**
	 * The fragment contents.
	 */
	public List<? extends HTMLTemplateFragment> getContents() {
		return _contents;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		for (HTMLTemplateFragment fragment : _contents) {
			fragment.write(context, out, properties);
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
