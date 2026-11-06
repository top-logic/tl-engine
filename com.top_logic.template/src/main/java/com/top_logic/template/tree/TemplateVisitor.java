/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;


/**
 * This class defines the interface for all visitors who work on a template syntax tree.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public interface TemplateVisitor <R, A> {
	/**
	 * Result of visit methods with result type {@link Void}.
	 */
	public static final Void none = null;
	
	/**
	 * Visits a {@link Template} node.
	 */
	R visitTemplate(Template node, A arg);
	/**
	 * Visits a {@link Reference} node.
	 */
	R visitReference(Reference node, A arg);
	/**
	 * Visits a {@link LiteralText} node.
	 */
	R visitLiteralText(LiteralText node, A arg);
	/**
	 * Visits a {@link BinaryExpression} node.
	 */
	R visitBinaryExpression(BinaryExpression node, A arg);
	/**
	 * Visits a {@link UnaryExpression} node.
	 */
	R visitUnaryExpression(UnaryExpression node, A arg);
	/**
	 * Visits a {@link FunctionCall} node.
	 */
	R visitFunctionCall(FunctionCall node, A arg);
	/**
	 * Visits a {@link ConstantExpression} node.
	 */
	R visitConstantExpression(ConstantExpression node, A arg);
	/**
	 * Visits a {@link IfStatement} node.
	 */
	R visitIfStatement(IfStatement node, A arg);
	/**
	 * Visits a {@link ForeachStatement} node.
	 */
	R visitForeachStatement(ForeachStatement node, A arg);
	/**
	 * Visits a {@link AssignStatement} node.
	 */
	R visitAssignStatement(AssignStatement node, A arg);
	/**
	 * Visits a {@link DefineStatement} node.
	 */
	R visitDefineStatement(DefineStatement node, A arg);
	/**
	 * Visits a {@link InvokeStatement} node.
	 */
	R visitInvokeStatement(InvokeStatement node, A arg);
	/**
	 * Visits a {@link AttributeValue} node.
	 */
	R visitAttributeValue(AttributeValue node, A arg);

}
