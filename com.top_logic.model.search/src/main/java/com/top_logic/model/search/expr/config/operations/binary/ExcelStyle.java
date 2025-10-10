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
 * {@link GenericMethod} for creating Excel style objects in TL-Script.
 * 
 * Creates reusable style objects that can be used with {@link ExcelCell}.
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class ExcelStyle extends GenericMethod {

	/**
	 * Property names in the same order as the descriptor
	 */
	private static final String[] STYLE_PROPERTIES = {
		"color", "background", "bold", "italic", "fontSize", "fontFamily",
		"borderTop", "borderBottom", "borderLeft", "borderRight", "align",
		"valign", "numberFormat", "rowSpan", "colSpan"
	};

	/**
	 * Creates a {@link ExcelStyle}.
	 */
	public ExcelStyle(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ExcelStyle(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		// Return a map representing the style
		Map<String, Object> style = new HashMap<>();

		for (int i = 0; i < STYLE_PROPERTIES.length; i++) {
			if (arguments[i] != null) {
				style.put(STYLE_PROPERTIES[i], arguments[i]);
			}
		}

		return style;
	}

	/**
	 * {@link MethodBuilder} for {@link ExcelStyle}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ExcelStyle> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("color")
			.optional("background")
			.optional("bold")
			.optional("italic")
			.optional("fontSize")
			.optional("fontFamily")
			.optional("borderTop")
			.optional("borderBottom")
			.optional("borderLeft")
			.optional("borderRight")
			.optional("align")
			.optional("valign")
			.optional("numberFormat")
			.optional("rowSpan", -1)
			.optional("colSpan", -1)
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
		public ExcelStyle build(Expr expr, SearchExpression[] args) {
			return new ExcelStyle(getName(), args);
		}

	}
}