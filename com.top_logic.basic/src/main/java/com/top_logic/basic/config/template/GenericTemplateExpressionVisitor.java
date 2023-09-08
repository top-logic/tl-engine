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
 * Adapter implementation for {@link ConfigExpressionVisitor}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GenericTemplateExpressionVisitor<R, A, E extends Throwable>
		extends ConfigExpressionVisitor<R, A, E> {

	@Override
	default R visitLiteralText(LiteralText expr, A arg) throws E {
		return visitConfigExpression(expr, arg);
	}

	@Override
	default R visitLiteralInt(LiteralInt expr, A arg) throws E {
		return visitConfigExpression(expr, arg);
	}

	@Override
	default R visitPropertyAccess(PropertyAccess expr, A arg) throws E {
		return visitConfigExpression(expr, arg);
	}

	@Override
	default R visitVariableAccess(VariableAccess expr, A arg) throws E {
		return visitConfigExpression(expr, arg);
	}

	@Override
	default R visitFunctionCall(FunctionCall expr, A arg) throws E {
		return visitConfigExpression(expr, arg);
	}

	@Override
	default R visitCollectionAccess(CollectionAccess expr, A arg) throws E {
		return visitConfigExpression(expr, arg);
	}

	@Override
	default R visitAlternative(Alternative expr, A arg) throws E {
		return visitConfigExpression(expr, arg);
	}

	@Override
	default R visitChoice(Choice expr, A arg) throws E {
		return visitConfigExpression(expr, arg);
	}

	@Override
	default R visitSelfAccess(SelfAccess expr, A arg) throws E {
		return visitConfigExpression(expr, arg);
	}

	/**
	 * Catch-all clause for {@link ConfigExpression} of any type.
	 * 
	 * @param expr
	 *        The visited expression.
	 * @param arg
	 *        The argument to the visit.
	 * @return The visit result.
	 */
	R visitConfigExpression(ConfigExpression expr, A arg) throws E;

}
