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
 * {@link SearchExpression} evaluating to a {@link HTMLFragment} that can be rendered directly.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HtmlMacro extends RenderExpression {

	SearchExpression[] _contents;

	/**
	 * Creates a {@link HtmlMacro}.
	 */
	public HtmlMacro(SearchExpression[] contents) {
		_contents = contents;
	}

	/**
	 * The contents to render.
	 */
	public SearchExpression[] getContents() {
		return _contents;
	}

	/**
	 * @see #getContents()
	 */
	public void setContents(SearchExpression[] contents) {
		_contents = contents;
	}

	@Override
	protected void write(DisplayContext context, TagWriter out, Args args, EvalContext definitions) throws IOException {
		for (SearchExpression content : _contents) {
			Object result = content.evalWith(definitions, args);
			writeValue(context, out, definitions.getRenderer(), result);
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitHtml(this, arg);
	}

}
