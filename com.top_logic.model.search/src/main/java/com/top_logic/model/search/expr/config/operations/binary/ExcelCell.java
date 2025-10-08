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
 * {@link GenericMethod} for creating Excel cells with styling in TL-Script.
 * 
 * Used within excelSheet() to define individual cells with their content and styling.
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class ExcelCell extends GenericMethod {

	/**
	 * Creates a {@link ExcelCell}.
	 */
	public ExcelCell(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ExcelCell(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Integer row = asInt(arguments[0]);
		Integer col = asInt(arguments[1]);
		Object content = arguments[2];

		// Return a map representing the cell
		Map<String, Object> cell = new HashMap<>();
		cell.put("row", row);
		cell.put("col", col);
		cell.put("content", content);

		if (arguments.length > 3 && arguments[3] instanceof Map) {
			Map<String, Object> styleProps = (Map<String, Object>) arguments[3];
			cell.putAll(styleProps);
		}

		return cell;
	}

	/**
	 * {@link MethodBuilder} for {@link ExcelCell}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ExcelCell> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("row")
			.mandatory("col")
			.mandatory("content")
			.optional("style")
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
		public ExcelCell build(Expr expr, SearchExpression[] args) {
			return new ExcelCell(getName(), args);
		}

	}
}