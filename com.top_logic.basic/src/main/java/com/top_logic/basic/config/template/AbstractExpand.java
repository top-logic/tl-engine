/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.template.Eval.EvalException;
import com.top_logic.basic.config.template.Eval.IContextOverlay;
import com.top_logic.basic.config.template.TemplateExpression.ConfigExpression;
import com.top_logic.basic.config.template.TemplateExpression.Foreach;
import com.top_logic.basic.config.template.TemplateExpression.Template;
import com.top_logic.basic.config.template.TemplateExpression.TemplateReference;
import com.top_logic.basic.config.template.TemplateExpression.TemplateSequence;
import com.top_logic.basic.config.template.TemplateExpression.TemplateStructure;

/**
 * Common functionality for expanding {@link TemplateExpression}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AbstractExpand<R, E extends Throwable>
		extends GenericTemplateExpressionVisitor<R, Eval.IContext, E>, TemplateVisitor<R, Eval.IContext, E> {

	@Override
	default R visitTemplate(Template expr, Eval.IContext arg) throws E {
		return visitSequence(expr, arg);
	}

	/**
	 * Common functionality for {@link TemplateSequence} expressions visiting all
	 * {@link TemplateSequence#getExprs()} in sequence.
	 */
	default R visitSequence(TemplateSequence expr, Eval.IContext arg) throws E {
		for (TemplateExpression subExpr : expr.getExprs()) {
			subExpr.visit(this, arg);
		}
		return null;
	}

	@Override
	default R visitForeach(Foreach expr, Eval.IContext arg) throws E {
		expr.getStart().visit(this, arg);

		Object collectionResult = expr.getCollection().visitEvaluator(eval(), arg);
		if (collectionResult instanceof Map<?, ?>) {
			collectionResult = ((Map<?, ?>) collectionResult).values();
		}
		else if (collectionResult == null) {
			collectionResult = Collections.emptyList();
		}
		else if (collectionResult.getClass().isArray()) {
			collectionResult = Arrays.asList((Object[]) collectionResult);
		}
		else if (!(collectionResult instanceof Iterable<?>)) {
			throw new EvalException("Not a collection in foreach" + expr.location() + ": " + expr);
		}
		Iterable<?> collection = (Iterable<?>) collectionResult;

		IContextOverlay inner = arg.scope(expr.getVarName());
		{
			boolean first = true;
			for (Object element : collection) {
				if (first) {
					first = false;
				} else {
					expr.getSeparator().visit(this, arg);
				}

				inner.assign(element);
				expr.getIterator().visit(this, inner);
			}
		}

		expr.getStop().visit(this, arg);
		return null;
	}

	@Override
	default R vistTemplateReference(TemplateReference expr, Eval.IContext arg) throws E {
		String templateName = String.valueOf(expr.getTemplateName().visitEvaluator(eval(), arg));
		TemplateExpression template = getTemplate(templateName);
		if (template == null) {
			throw new EvalException("No such template '" + templateName + "'" + expr.location() + ".");
		}

		return template.visit(this, arg);
	}

	@Override
	default R visitConfigExpression(ConfigExpression expr, Eval.IContext arg) throws E {
		Object result = expr.visitEvaluator(eval(), arg);
		if (result instanceof TemplateStructure) {
			((TemplateStructure) result).visitStructure(this, arg);
		} else if (EvalUtil.isNonEmpty(expr, result)) {
			output(result);
		}
		return null;
	}

	/**
	 * Produces a literal value to be written to the output.
	 */
	void output(Object result) throws E;

	/**
	 * Lookup of a referenced template with the given name.
	 */
	TemplateExpression getTemplate(String templateName);

	/**
	 * The expression evaluator.
	 */
	Eval eval();

}
