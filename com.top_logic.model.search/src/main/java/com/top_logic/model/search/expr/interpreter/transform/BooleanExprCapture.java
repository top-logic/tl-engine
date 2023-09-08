/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter.transform;

import com.top_logic.basic.treexf.Expr;
import com.top_logic.basic.treexf.ExprCapture;
import com.top_logic.basic.treexf.LiteralValue;
import com.top_logic.basic.treexf.Match;
import com.top_logic.basic.treexf.Node;
import com.top_logic.model.search.expr.BooleanExpression;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * {@link ExprCapture} that only matches {@link SearchExpression} nodes that are statically typed
 * {@link Boolean}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanExprCapture extends ExprCapture {

	/**
	 * Creates a {@link BooleanExprCapture}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	public BooleanExprCapture(String name) {
		super(name);
	}

	@Override
	protected boolean matchesNode(Match match, Expr node) {
		return super.matchesNode(match, node) && isBoolean(node);
	}

	private boolean isBoolean(Expr node) {
		Object type = node.getType();

		if (type instanceof Class<?> && BooleanExpression.class.isAssignableFrom((Class<?>) type)) {
			return true;
		}

		if (type == Literal.class) {
			Node valueNode = node.getChild(0);
			if (valueNode instanceof LiteralValue) {
				return ((LiteralValue) valueNode).getValue() instanceof Boolean;
			}
		}

		return false;
	}

}
