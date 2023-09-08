/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import com.top_logic.basic.config.template.TemplateExpression.Alternative;
import com.top_logic.basic.config.template.TemplateExpression.Choice;
import com.top_logic.basic.config.template.TemplateExpression.CollectionAccess;
import com.top_logic.basic.config.template.TemplateExpression.ConfigExpression;
import com.top_logic.basic.config.template.TemplateExpression.FunctionCall;
import com.top_logic.basic.config.template.TemplateExpression.LiteralInt;
import com.top_logic.basic.config.template.TemplateExpression.LiteralText;
import com.top_logic.basic.config.template.TemplateExpression.PropertyAccess;
import com.top_logic.basic.config.template.TemplateExpression.SelfAccess;
import com.top_logic.basic.config.template.TemplateExpression.VariableAccess;

/**
 * Visitor interface for {@link ConfigExpression}s.
 * 
 * @param <R>
 *        The visit result.
 * @param <A>
 *        The visit argument.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigExpressionVisitor<R, A, E extends Throwable> {

	/**
	 * Visit case for {@link LiteralText}.
	 */
	R visitLiteralText(LiteralText expr, A arg) throws E;

	/**
	 * Visit case for {@link LiteralInt}.
	 */
	R visitLiteralInt(LiteralInt expr, A arg) throws E;

	/**
	 * Visit case for {@link PropertyAccess}.
	 */
	R visitPropertyAccess(PropertyAccess expr, A arg) throws E;

	/**
	 * Visit case for {@link VariableAccess}.
	 */
	R visitVariableAccess(VariableAccess expr, A arg) throws E;

	/**
	 * Visit case for {@link FunctionCall}.
	 */
	R visitFunctionCall(FunctionCall expr, A arg) throws E;

	/**
	 * Visit case for {@link CollectionAccess}.
	 */
	R visitCollectionAccess(CollectionAccess expr, A arg) throws E;

	/**
	 * Visit case for {@link Alternative}.
	 */
	R visitAlternative(Alternative expr, A arg) throws E;

	/**
	 * Visit case for {@link Choice}.
	 */
	R visitChoice(Choice expr, A arg) throws E;

	/**
	 * Visit case for {@link SelfAccess}.
	 */
	R visitSelfAccess(SelfAccess expr, A arg) throws E;

}
