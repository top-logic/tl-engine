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
 * Used within {@link ExcelFile} to define individual sheets with their content.
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
		Object content = arguments[0];
		if (content == null) {
			content = new ArrayList<>();
		}
		String sheetName = asString(arguments[1]);

		// Return a map representing the sheet
		Map<String, Object> sheet = new HashMap<>();

		if (sheetName.isEmpty()) {
			sheet.put("autoGenerateName", true);
		} else {
			sheet.put("name", sheetName);
		}

		sheet.put("content", asList(content));
		
		if (arguments[2] != null) {
			sheet.put("colWidths", asMap(arguments[2]));
		}
		if (arguments[3] != null) {
			sheet.put("rowHeights", asMap(arguments[3]));
		}

		return sheet;
	}

	/**
	 * {@link MethodBuilder} for {@link ExcelSheet}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ExcelSheet> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("content")
			.optional("name")
			.optional("colWidths")
			.optional("rowHeights")
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