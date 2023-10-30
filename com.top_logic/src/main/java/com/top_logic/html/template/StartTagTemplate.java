/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} for a HTML start tag.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class StartTagTemplate extends TemplateNode implements RawTemplateFragment {

	private final String _name;

	private boolean _isEmpty;

	private final List<TagAttributeTemplate> _attributes = new ArrayList<>();

	/** 
	 * Creates a {@link StartTagTemplate}.
	 * 
	 * @param name Tag name.
	 */
	public StartTagTemplate(int line, int column, String name) {
		super(line, column);
		_name = name;
	}

	/**
	 * The tag name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * All attributes of this tag.
	 */
	public List<TagAttributeTemplate> getAttributes() {
		return _attributes;
	}

	/**
	 * Whether this tag cannot have contents.
	 */
	public boolean isEmpty() {
		return _isEmpty;
	}

	/** 
	 * Whether the tag is an empty tag.
	 */
	public void setEmpty(boolean isEmpty) {
		_isEmpty = isEmpty;
	}

	/** 
	 * Adds the given HTML attribute to the tag.
	 */
	public void addAttribute(TagAttributeTemplate attribute) {
		_attributes.add(attribute);
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		out.beginBeginTag(_name);

		for (TagAttributeTemplate attribute : _attributes) {
			attribute.write(context, out, properties);
		}

		if (_isEmpty) {
			out.endEmptyTag();
		} else {
			out.endBeginTag();
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	/**
	 * Creates an empty copy of this start tag (without attributes).
	 */
	public StartTagTemplate copy() {
		return new StartTagTemplate(getLine(), getColumn(), getName());
	}

}
