/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.web;

import java.awt.Color;
import java.util.HashMap;
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

/**
 * Function creating a {@link Color} value.
 */
public class ColorValues extends GenericMethod {

	/**
	 * Creates a {@link ColorValues}.
	 */
	protected ColorValues(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ColorValues(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object arg = arguments[0];
		Color color;
		if (arg instanceof Color col) {
			color = col;
		} else if (arg == null) {
			color = Color.black;
		} else {
			color = ColorUtil.cssColor(asString(arg));
		}

		HSLColor hsl = new HSLColor(color);
		HashMap<String, Object> values = new HashMap<>();
		values.put("h", Double.valueOf(hsl.getHue()));
		values.put("s", Double.valueOf(hsl.getSaturation()));
		values.put("l", Double.valueOf(hsl.getLuminance()));
		values.put("r", Double.valueOf(color.getRed()));
		values.put("g", Double.valueOf(color.getGreen()));
		values.put("b", Double.valueOf(color.getBlue()));
		values.put("a", Double.valueOf(color.getAlpha()));
		return values;
	}

	/**
	 * {@link MethodBuilder} creating {@link ColorValues}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ColorValues> {
		/** Description of parameters for a {@link ColorValues}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("color")
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
		public ColorValues build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new ColorValues(getConfig().getName(), args);
		}
	}
}
