/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.web;

import java.awt.Color;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.mig.util.ColorUtil;
import com.top_logic.mig.util.HSLColor;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.config.operations.string.EncodeXML;
import com.top_logic.model.util.TLModelUtil;

/**
 * Function creating a {@link Color} value.
 */
public class ColorFunction extends GenericMethod {

	/**
	 * Creates a {@link ColorFunction}.
	 */
	protected ColorFunction(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ColorFunction(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType("tl.util:Color");
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String css = asString(arguments[0]);

		if (!css.isEmpty()) {
			return ColorUtil.cssColor(css);
		}

		Integer r = asIntNum(arguments[1]);
		int g = asInt(arguments[2]);
		int b = asInt(arguments[3]);
		int a = asInt(arguments[4], 255);

		if (r != null) {
			return new Color(r.intValue(), g, b, a);
		}

		Double h = asDoubleNum(arguments[5]);
		Double s = asDoubleNum(arguments[6]);
		Double l = asDoubleNum(arguments[7]);
		
		return hslColor(h, s, l, a / 255.0f);
	}

	private Integer asIntNum(Object object) {
		return object == null ? null : asInt(object);
	}

	private Double asDoubleNum(Object object) {
		return object == null ? null : asDouble(object);
	}

	private static Color hslColor(Number h, Number s, Number l, float a) {
		float hValue = h == null ? 0 : h.floatValue();
		float sValue = s == null ? 100 : s.floatValue();
		float lValue = l == null ? 100 : l.floatValue();

		return HSLColor.toRGB(hValue, sValue, lValue, a);
	}

	/**
	 * {@link MethodBuilder} creating {@link ColorFunction}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ColorFunction> {
		/** Description of parameters for a {@link EncodeXML}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("css")

			.optional("r")
			.optional("g")
			.optional("b")
			.optional("a")

			.optional("h")
			.optional("s")
			.optional("l")
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
		public ColorFunction build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new ColorFunction(getConfig().getName(), args);
		}
	}
}
