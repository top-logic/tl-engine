/*
* SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
* 
* SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
*/
package com.top_logic.model.search.expr.config.operations.binary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} for creating Excel formulas in TL-Script.
 * 
 * Used within excelCell() to define formula expressions for cells.
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class ExcelFormula extends GenericMethod {

	/**
	 * Creates a {@link ExcelFormula}.
	 */
	public ExcelFormula(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ExcelFormula(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String formula = asString(arguments[0]);
		if (formula == null) {
			formula = "";
		}

		// Return a map representing the formula
		Map<String, Object> formulaObj = new HashMap<>();
		formulaObj.put("formula", formula);
		
		return formulaObj;
	}

	/**
	 * {@link MethodBuilder} for {@link ExcelFormula}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ExcelFormula> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("formula")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public ExcelFormula build(Expr expr, SearchExpression[] args) {
			return new ExcelFormula(getName(), args);
		}

	}
}