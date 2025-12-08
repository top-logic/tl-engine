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
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} for creating Excel cells with styling in TL-Script.
 * 
 * Used within {@link ExcelSheet} to define individual cells with their content and styling.
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
		Object content = arguments[0];

		// -1 indicates that no row was specified
		Integer row = asInt(arguments[1], -1);
		Integer col = asInt(arguments[2], -1);

		Map<String, Object> styleProps = null;
		if (arguments[3] != null) {
			if (arguments[3] instanceof Map) {
				styleProps = (Map<String, Object>) arguments[3];
			} else {
				throw new TopLogicException(I18NConstants.ERROR_INVALID_STYLE_ATTRIBUTE);
			}
		}

		// Return a map representing the cell
		Map<String, Object> cell = new HashMap<>();
		cell.put("content", content);
		cell.put("row", row);
		cell.put("col", col);
		if (styleProps != null) {
			cell.putAll(styleProps);
		}
		return cell;
	}

	/**
	 * {@link MethodBuilder} for {@link ExcelCell}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ExcelCell> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("content")
			.optional("row")
			.optional("col")
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