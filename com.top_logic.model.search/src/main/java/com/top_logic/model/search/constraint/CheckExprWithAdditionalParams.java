/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.constraint;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.ui.ModelReferenceChecker;

/**
 * {@link GenericValueDependency} that checks that a (partial) expression is valid, when a given
 * list of parameters are supposed.
 * 
 * <p>
 * The check uses the {@link Expr} and enhances it with parameters given in the list of
 * {@link NamedConfiguration}. These enhanced {@link Expr} is compiled. When a problem occurs, it is
 * reported to the expression field.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CheckExprWithAdditionalParams extends GenericValueDependency<Expr, List<? extends NamedConfiguration>> {

	/**
	 * Creates a {@link CheckExprWithAdditionalParams}.
	 */
	public CheckExprWithAdditionalParams() {
		super(Expr.class, cast(List.class));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Class<List<? extends NamedConfiguration>> cast(Class listClass) {
		return listClass;
	}

	@Override
	protected void checkValue(PropertyModel<Expr> self,
			PropertyModel<List<? extends NamedConfiguration>> additionalVariables) {
		Expr testExpr = self.getValue();
		if (testExpr == null) {
			return;
		}
		List<? extends NamedConfiguration> additionals = CollectionUtil.nonNull(additionalVariables.getValue());
		for (int i = additionals.size() - 1; i >= 0; i--) {
			String name = additionals.get(i).getName();
			if (!name.isEmpty() && !name.isBlank()) {
				testExpr = Define.create(name, testExpr);
			}
		}
		try {
			ModelReferenceChecker.checkModelElements(testExpr);
		} catch (I18NRuntimeException ex) {
			self.setProblemDescription(ex.getErrorKey());
		}
	}

}
