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
	public static final Void none = null;
	
	R visitTemplate(Template node, A arg);
	R visitReference(Reference node, A arg);
	R visitLiteralText(LiteralText node, A arg);
	R visitBinaryExpression(BinaryExpression node, A arg);
	R visitUnaryExpression(UnaryExpression node, A arg);
	R visitFunctionCall(FunctionCall node, A arg);
	R visitConstantExpression(ConstantExpression node, A arg);
	R visitIfStatement(IfStatement node, A arg);
	R visitForeachStatement(ForeachStatement node, A arg);
	R visitAssignStatement(AssignStatement node, A arg);
	R visitDefineStatement(DefineStatement node, A arg);
	R visitInvokeStatement(InvokeStatement node, A arg);
	R visitAttributeValue(AttributeValue node, A arg);

}
