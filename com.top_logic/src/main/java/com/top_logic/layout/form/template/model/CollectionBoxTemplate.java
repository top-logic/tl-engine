/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.boxes.layout.BoxLayout;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} that consists of a linear list of content boxes with a custom
 * layout.
 * 
 * @see #getLayout()
 * @see #getContents()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CollectionBoxTemplate implements Template {

	private final BoxLayout _layout;

	private final HTMLTemplateFragment[] _contents;

	/**
	 * Creates a {@link CollectionBoxTemplate}.
	 * 
	 * @param layout
	 *        See {@link #getLayout()}.
	 * @param contents
	 *        See {@link #getContents()}.
	 * 
	 * @see Templates#collectionBox(BoxLayout, HTMLTemplateFragment...)
	 */
	CollectionBoxTemplate(BoxLayout layout, HTMLTemplateFragment[] contents) {
		_layout = layout;
		_contents = contents;
	}

	/**
	 * The {@link BoxLayout} to apply to the {@link #getContents()}.
	 */
	public BoxLayout getLayout() {
		return _layout;
	}

	/**
	 * The content boxes to layout.
	 */
	public HTMLTemplateFragment[] getContents() {
		return _contents;
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		for (HTMLTemplateFragment content : getContents()) {
			content.write(displayContext, out, properties);
		}
	}
}
