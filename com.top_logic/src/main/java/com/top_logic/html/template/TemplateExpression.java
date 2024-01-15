/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import com.top_logic.html.template.expr.AddExpression;
import com.top_logic.html.template.expr.AndExpression;
import com.top_logic.html.template.expr.DivExpression;
import com.top_logic.html.template.expr.EqExpression;
import com.top_logic.html.template.expr.GeExpression;
import com.top_logic.html.template.expr.GtExpression;
import com.top_logic.html.template.expr.LiteralExpression;
import com.top_logic.html.template.expr.ModExpression;
import com.top_logic.html.template.expr.MulExpression;
import com.top_logic.html.template.expr.NegExpression;
import com.top_logic.html.template.expr.NotExpression;
import com.top_logic.html.template.expr.NullExpression;
import com.top_logic.html.template.expr.OrExpression;
import com.top_logic.html.template.expr.StringLiteral;
import com.top_logic.html.template.expr.SubExpression;
import com.top_logic.html.template.expr.TestExpression;
import com.top_logic.html.template.expr.VariableExpression;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * Expression that can be evaluated while rendering a {@link HTMLTemplateFragment}.
 */
public interface TemplateExpression {

	/**
	 * Visit interface for the {@link TemplateExpression} hierarchy.
	 *
	 * @param <R>
	 *        The result type of the visit.
	 * @param <A>
	 *        The argument type for the visit.
	 */
	interface Visitor<R, A> {
		/** Visit method for {@link NotExpression}s. */
		R visit(NotExpression expr, A arg);

		/** Visit method for {@link AddExpression}s. */
		R visit(AddExpression expr, A arg);

		/** Visit method for {@link AndExpression}s. */
		R visit(AndExpression expr, A arg);

		/** Visit method for {@link DivExpression}s. */
		R visit(DivExpression expr, A arg);

		/** Visit method for {@link EqExpression}s. */
		R visit(EqExpression expr, A arg);

		/** Visit method for {@link GeExpression}s. */
		R visit(GeExpression expr, A arg);

		/** Visit method for {@link GtExpression}s. */
		R visit(GtExpression expr, A arg);

		/** Visit method for {@link LiteralExpression}s. */
		R visit(LiteralExpression expr, A arg);

		/** Visit method for {@link ModExpression}s. */
		R visit(ModExpression expr, A arg);

		/** Visit method for {@link MulExpression}s. */
		R visit(MulExpression expr, A arg);

		/** Visit method for {@link NegExpression}s. */
		R visit(NegExpression expr, A arg);

		/** Visit method for {@link NullExpression}s. */
		R visit(NullExpression expr, A arg);

		/** Visit method for {@link OrExpression}s. */
		R visit(OrExpression expr, A arg);

		/** Visit method for {@link StringLiteral}s. */
		R visit(StringLiteral expr, A arg);

		/** Visit method for {@link SubExpression}s. */
		R visit(SubExpression expr, A arg);

		/** Visit method for {@link TestExpression}s. */
		R visit(TestExpression expr, A arg);

		/** Visit method for {@link VariableExpression}s. */
		R visit(VariableExpression expr, A arg);
	}

	/**
	 * Evaluates this expression in the given context.
	 *
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param properties
	 *        The model object that is currently
	 *        {@link HTMLTemplateFragment#write(DisplayContext, com.top_logic.basic.xml.TagWriter, WithProperties)
	 *        rendered}.
	 * @return The evaluation result.
	 */
	Object eval(DisplayContext context, WithProperties properties);

	/**
	 * Converts this evaluation to a renderer for the evaluated value.
	 */
	default RawTemplateFragment toFragment() {
		return new ExpressionTemplate(this);
	}

	/**
	 * Visit method for {@link TemplateExpression}s
	 *
	 * @param v
	 *        The {@link Visitor} to accept.
	 * @param arg
	 *        The argument to the visit.
	 * @return The result of the visit.
	 */
	<R, A> R visit(Visitor<R, A> v, A arg);
}
