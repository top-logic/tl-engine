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

		Integer aNum = asIntNum(arguments[4]);

		if (!css.isEmpty()) {
			Color cssColor = ColorUtil.cssColor(css);

			if (aNum != null) {
				return new Color(cssColor.getRed(), cssColor.getGreen(), cssColor.getBlue(), aNum.intValue());
			}

			return cssColor;
		}

		Integer rNum = asIntNum(arguments[1]);
		Integer gNum = asIntNum(arguments[2]);
		Integer bNum = asIntNum(arguments[3]);
		int a = aNum == null ? 255 : aNum.intValue();

		if (rNum != null || gNum != null || bNum != null) {
			int r = rNum == null ? 0 : rNum.intValue();
			int g = gNum == null ? 0 : gNum.intValue();
			int b = bNum == null ? 0 : bNum.intValue();
			return new Color(r, g, b, a);
		}

		HSLColor from = toColor(arguments[5]);

		Double h = asDoubleNum(arguments[6]);
		Double s = asDoubleNum(arguments[7]);
		Double l = asDoubleNum(arguments[8]);
		
		float hValue = h == null ? (from == null ? 0 : from.getHue()) : h.floatValue();
		float sValue = s == null ? (from == null ? 100 : from.getSaturation()) : s.floatValue();
		float lValue = l == null ? (from == null ? 50 : from.getLuminance()) : l.floatValue();
		
		return HSLColor.toRGB(hValue, sValue, lValue, a / 255.0f);
	}

	private HSLColor toColor(Object object) {
		if (object instanceof Color color) {
			return new HSLColor(color);
		} else if (object == null) {
			return null;
		}
		return new HSLColor(ColorUtil.cssColor(asString(object)));
	}

	private Integer asIntNum(Object object) {
		return object == null ? null : asInt(object);
	}

	private Double asDoubleNum(Object object) {
		return object == null ? null : asDouble(object);
	}

	/**
	 * {@link MethodBuilder} creating {@link ColorFunction}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ColorFunction> {
		/** Description of parameters. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("css")

			.optional("r")
			.optional("g")
			.optional("b")

			.optional("a")

			.optional("from")

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
