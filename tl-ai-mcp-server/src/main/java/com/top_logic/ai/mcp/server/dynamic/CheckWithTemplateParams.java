/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.dynamic;

import java.util.Collections;
import java.util.List;

import com.top_logic.ai.mcp.server.UriPattern;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ModelReferenceChecker;

/**
 * Constraint that validates a TL-Script expression with additional parameters extracted from a
 * URI template.
 *
 * <p>
 * This constraint extracts parameter names from a URI template (e.g.,
 * {@code "myapp://item/{itemId}/version/{version}"}) and validates the TL-Script expression as
 * if those parameters were available as implicit variables (e.g., {@code $itemId},
 * {@code $version}).
 * </p>
 *
 * <p>
 * The expression is automatically wrapped in nested functions that declare the parameters,
 * allowing the syntax checker to recognize and validate the parameter references within the
 * script.
 * </p>
 *
 * @author Bernhard Haumacher
 */
public class CheckWithTemplateParams extends GenericValueDependency<Expr, String> {

	/**
	 * Creates a {@link CheckWithTemplateParams}.
	 */
	public CheckWithTemplateParams() {
		super(Expr.class, String.class);
	}

	@Override
	protected void checkValue(PropertyModel<Expr> exprProperty, PropertyModel<String> uriTemplateProperty) {
		Expr expr = exprProperty.getValue();
		if (expr == null) {
			// No expression to validate
			return;
		}

		String uriTemplate = uriTemplateProperty.getValue();
		if (uriTemplate == null || uriTemplate.isEmpty()) {
			// No URI template - validate expression without additional parameters
			validateExpression(exprProperty, expr, Collections.emptyList());
			return;
		}

		// Extract parameter names from URI template
		List<String> parameterNames;
		try {
			UriPattern pattern = UriPattern.compile(uriTemplate);
			parameterNames = pattern.getParameterNames();
		} catch (IllegalArgumentException ex) {
			// Invalid URI template - skip validation of expression
			// (the URI template itself should be validated by a separate constraint)
			return;
		}

		// Validate expression with additional parameters
		validateExpression(exprProperty, expr, parameterNames);
	}

	/**
	 * Validates the TL-Script expression by wrapping it in functions that declare the given
	 * parameters.
	 *
	 * @param exprProperty
	 *        The property model for error reporting.
	 * @param bodyExpr
	 *        The expression to validate.
	 * @param parameterNames
	 *        The parameter names to make available as implicit variables.
	 */
	private void validateExpression(PropertyModel<Expr> exprProperty, Expr bodyExpr,
			List<String> parameterNames) {
		// Wrap the expression in nested functions, one for each parameter
		Expr wrappedExpr = bodyExpr;
		for (int i = parameterNames.size() - 1; i >= 0; i--) {
			String paramName = parameterNames.get(i);
			if (!paramName.isEmpty() && !paramName.isBlank()) {
				wrappedExpr = Expr.Define.create(paramName, wrappedExpr);
			}
		}

		try {
			// Check model references in the wrapped expression
			ModelReferenceChecker.checkModelElements(wrappedExpr);
		} catch (I18NRuntimeException ex) {
			// Report validation error with I18N key for proper localization
			exprProperty.setProblemDescription(ex.getErrorKey());
		}
	}

}
