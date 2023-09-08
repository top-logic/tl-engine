/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.html;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link RenderExpression} producing a start tag.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TagMacro extends RenderExpression {

	String _tag;

	boolean _emptyTag;

	SearchExpression[] _attributes;

	/**
	 * Creates a {@link TagMacro}.
	 */
	public TagMacro(String tag, boolean emptyTag, SearchExpression[] attributes) {
		_tag = tag;
		_emptyTag = emptyTag;
		_attributes = attributes;
	}

	/**
	 * The tag name.
	 */
	public String getTag() {
		return _tag;
	}

	/**
	 * @see #getTag()
	 */
	public void setTag(String tag) {
		_tag = tag;
	}

	/**
	 * Whether the tag is empty (has no explicit end tag).
	 */
	public boolean isEmpty() {
		return _emptyTag;
	}

	/**
	 * @see #isEmpty()
	 */
	public void setEmpty(boolean value) {
		_emptyTag = value;
	}

	/**
	 * The tag's attributes.
	 * 
	 * <p>
	 * The attributes are expected to be of {@link AttributeMacro} type.
	 * </p>
	 */
	public SearchExpression[] getAttributes() {
		return _attributes;
	}

	/**
	 * See {@link #getAttributes()}.
	 */
	public void setAttributes(AttributeMacro[] attributes) {
		_attributes = attributes;
	}

	@Override
	protected void write(DisplayContext context, TagWriter out, Args args, EvalContext definitions) throws IOException {
		out.beginBeginTag(_tag);
		for (SearchExpression attr : _attributes) {
			Object result = attr.evalWith(definitions, args);
			if (result instanceof HTMLFragment) {
				((HTMLFragment) result).write(context, out);
			}
		}
		if (_emptyTag) {
			out.endEmptyTag();
		} else {
			out.endBeginTag();
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitTag(this, arg);
	}

}
