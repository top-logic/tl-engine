/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter.transform;

import static com.top_logic.basic.treexf.TransformFactory.*;

import java.util.List;

import com.top_logic.basic.treexf.Node;
import com.top_logic.basic.treexf.TransformFactory;
import com.top_logic.basic.treexf.TreeMaterializer;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.visit.GenericDescendingVisitor;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link Visitor} that creates generic {@link Node} trees for transformation out of a concrete
 * {@link SearchExpression}.
 * 
 * @see TreeMaterializer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeBuilder extends GenericDescendingVisitor<Node, Void> {

	/**
	 * Singleton {@link TreeBuilder} instance.
	 */
	public static final TreeBuilder INSTANCE = new TreeBuilder();

	private static final Node[] NODE_ARRAY = {};

	private TreeBuilder() {
		// Singleton constructor.
	}

	@Override
	protected Node compose(SearchExpression expr, Void arg, List<Node> descendResult) {
		return node(expr, descendResult.toArray(new Node[descendResult.size()]));
	}

	@Override
	protected Node compose(Class<?> type, Void arg, List<Node> contents) {
		return expr(type, contents.toArray(NODE_ARRAY));
	}

	private Node node(SearchExpression expr, Node... contents) {
		return expr(id(expr), contents);
	}

	private Object id(SearchExpression expr) {
		return expr.getId();
	}

	@Override
	protected Node wrap(Object value) {
		return TransformFactory.value(value);
	}
}