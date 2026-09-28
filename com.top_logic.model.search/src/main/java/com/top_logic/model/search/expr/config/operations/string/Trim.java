/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * Removes white space from the start, the end, or both ends of a string.
 *
 * <p>
 * White space is Unicode white space in the sense of {@link Character#isWhitespace(int)}. White
 * space inside the string is kept. A <code>null</code> argument results in <code>null</code>,
 * other non-string values are converted to a string first.
 * </p>
 *
 * <p>
 * The {@link Side} to trim is a configuration option of the {@link Builder}, so that the functions
 * <code>trim</code>, <code>trimStart</code>, and <code>trimEnd</code> are registrations of the same
 * {@link Builder} with different {@link Builder.Config#SIDE sides}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Trim extends GenericMethod {

	/**
	 * The side(s) of a string from which {@link Trim} removes white space.
	 */
	public enum Side {

		/**
		 * Removes leading and trailing white space.
		 */
		BOTH {
			@Override
			public String trim(String str) {
				return str.strip();
			}
		},

		/**
		 * Removes leading white space only.
		 */
		START {
			@Override
			public String trim(String str) {
				return str.stripLeading();
			}
		},

		/**
		 * Removes trailing white space only.
		 */
		END {
			@Override
			public String trim(String str) {
				return str.stripTrailing();
			}
		};

		/**
		 * Removes the white space at this side of the given string.
		 */
		public abstract String trim(String str);
	}

	private final Side _side;

	/**
	 * Creates a {@link Trim}.
	 */
	protected Trim(String name, Side side, SearchExpression[] arguments) {
		super(name, arguments);
		_side = side;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Trim(getName(), _side, arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String str = asString(arguments[0], null);
		if (str == null) {
			return null;
		}
		return _side.trim(str);
	}

	@Override
	public Object getId() {
		return List.of(Trim.class, _side);
	}

	/**
	 * {@link MethodBuilder} creating a {@link Trim} for a configured {@link Side}.
	 */
	public static final class Builder extends AbstractMethodBuilder<Builder.Config, Trim> {

		/**
		 * Configuration options for {@link Trim.Builder}.
		 */
		public interface Config extends AbstractMethodBuilder.Config<Builder> {

			/**
			 * Configuration name for {@link #getSide()}.
			 */
			String SIDE = "side";

			/**
			 * The side(s) of the string from which the created function removes white space.
			 */
			@Name(SIDE)
			Side getSide();
		}

		/**
		 * Creates a {@link Builder}.
		 */
		@CalledByReflection
		public Builder(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public Trim build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			checkSingleArg(expr, args);
			return new Trim(getName(), getConfig().getSide(), args);
		}

		@Override
		public Object getId() {
			return List.of(Trim.class, getConfig().getSide());
		}
	}

}
