/*
* SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
* 
* SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
*/
package com.top_logic.model.search.expr.config.operations.binary;

import java.util.ArrayList;
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
 * {@link GenericMethod} for creating Excel sheets in TL-Script.
 * 
 * Used within excelFile() to define individual sheets with their content.
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class ExcelSheet extends GenericMethod {

	/**
	 * Creates a {@link ExcelSheet}.
	 */
	public ExcelSheet(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ExcelSheet(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String sheetName = asString(arguments[0]);
		if (sheetName == null) {
			sheetName = "Sheet";
		}

		Object content = arguments[1];
		if (content == null) {
			content = new ArrayList<>();
		}

		// Return a map representing the sheet
		Map<String, Object> sheet = new HashMap<>();
		sheet.put("name", sheetName);
		sheet.put("content", asList(content));
		
		return sheet;
	}

	/**
	 * {@link MethodBuilder} for {@link ExcelSheet}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ExcelSheet> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("name")
			.mandatory("content")
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
		public ExcelSheet build(Expr expr, SearchExpression[] args) {
			return new ExcelSheet(getName(), args);
		}

	}
}