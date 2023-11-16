/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprPrinter;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Base class for {@link MethodBuilder}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMethodBuilder<C extends AbstractMethodBuilder.Config<?>, E extends SearchExpression>
		extends AbstractConfiguredInstance<C> implements MethodBuilder<E> {

	/**
	 * Creates a {@link AbstractMethodBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractMethodBuilder(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * The name of the builder as given in the configuration.
	 * 
	 * @see MethodBuilder.Config#getName()
	 */
	public final String getName() {
		return getConfig().getName();
	}

	/**
	 * The given {@link Expr} in readable form.
	 */
	public static final String toString(Expr expr) {
		return expr == null ? "<no-source>" : ExprPrinter.toString(expr);
	}

	/**
	 * Throws the given error message.
	 */
	public static final ConfigurationException error(ResKey errorKey) throws ConfigurationException {
		throw new ConfigurationException(errorKey, null, null);
	}

	/**
	 * Checks the number of arguments.
	 *
	 * @param expr
	 *        The context expression.
	 * @param args
	 *        The actual arguments.
	 * @param min
	 *        The minimum number of arguments.
	 * @param max
	 *        The maximum number of arguments.
	 */
	public static void checkArgs(Expr expr, SearchExpression[] args, int min, int max)
			throws ConfigurationException {
		checkMinArgs(expr, args, min);
		checkMaxArgs(expr, args, max);
	}

	/**
	 * Checks the number of arguments.
	 *
	 * @param expr
	 *        The context expression.
	 * @param args
	 *        The actual arguments.
	 * @param max
	 *        The maximum number of arguments.
	 */
	public static void checkMaxArgs(Expr expr, SearchExpression[] args, int max)
			throws ConfigurationException {
		if (args.length > max) {
			throw error(I18NConstants.ERROR_AT_MOST_ARGUMENTS_EXPECTED__CNT_EXPR.fill(max, toString(expr)));
		}
	}

	/**
	 * Checks the number of arguments.
	 *
	 * @param expr
	 *        The context expression.
	 * @param args
	 *        The actual arguments.
	 * @param min
	 *        The minimum number of arguments.
	 */
	public static void checkMinArgs(Expr expr, SearchExpression[] args, int min)
			throws ConfigurationException {
		if (args.length < min) {
			throw error(I18NConstants.ERROR_AT_LEAST_ARGUMENTS_EXPECTED__CNT_EXPR.fill(min, toString(expr)));
		}
	}

	/**
	 * Checks that the self expression is <code>null</code>.
	 */
	public static void checkNoTarget(Expr expr, SearchExpression self) throws ConfigurationException {
		if (self != null) {
			throw error(I18NConstants.ERROR_MUST_BE_INVOKED_WITHOUT_TARGET__EXPR.fill(toString(expr)));
		}
	}

	/**
	 * Checks that no arguments are given (but a target expression).
	 */
	public static void checkNoArguments(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		checkHasTarget(expr, self);
		checkNoArguments(expr, args);
	}

	/**
	 * Checks that no arguments and not target is given.
	 */
	protected static void checkNoArguments(Expr expr, SearchExpression[] args) throws ConfigurationException {
		if (args.length != 0) {
			throw error(I18NConstants.ERROR_NO_ARGUMENTS_EXPECTED__EXPR.fill(toString(expr)));
		}
	}

	/**
	 * Checks that a target expression is given.
	 */
	public static void checkHasTarget(Expr expr, SearchExpression self) throws ConfigurationException {
		if (self == null) {
			throw error(I18NConstants.ERROR_EXPECTING_A_TARGET__EXPR.fill(toString(expr)));
		}
	}

	/**
	 * Checks that only one argument is given.
	 */
	public static void checkSingleArg(Expr expr, SearchExpression[] args) throws ConfigurationException {
		if (args.length != 1) {
			throw error(I18NConstants.ERROR_SINGLE_ARGUMENT_EXPECTED__CNT_EXPR.fill(args.length, toString(expr)));
		}
	}

	/**
	 * Checks that exactly two arguments are given.
	 */
	public static void checkTwoArgs(Expr expr, SearchExpression[] args) throws ConfigurationException {
		if (args.length != 2) {
			throw error(I18NConstants.ERROR_TWO_ARGUMENTS_EXPECTED__CNT_EXPR.fill(args.length, toString(expr)));
		}
	}

	/**
	 * Checks that exactly three arguments are given.
	 */
	public static void checkThreeArgs(Expr expr, SearchExpression[] args) throws ConfigurationException {
		if (args.length != 3) {
			throw error(I18NConstants.ERROR_THREE_ARGUMENTS_EXPECTED__CNT_EXPR.fill(args.length, toString(expr)));
		}
	}

	/**
	 * Resolves a {@link TLStructuredTypePart} from a literal argument.
	 */
	protected static TLReference resolveReference(Expr expr, SearchExpression arg)
			throws ConfigurationException {
		TLStructuredTypePart result = resolvePart(expr, arg);
		if (result instanceof TLReference) {
			return (TLReference) result;
		} else {
			throw error(I18NConstants.ERROR_EXPECTED_REFERENCE_LITERAL__EXPR.fill(toString(expr)));
		}
	}

	/**
	 * Resolves a {@link TLStructuredTypePart} from a literal argument.
	 */
	protected static TLStructuredTypePart resolvePart(Expr expr, SearchExpression arg)
			throws ConfigurationException {
		Object value = resolveLiteral(expr, arg).getValue();
		if (value instanceof TLStructuredTypePart) {
			return (TLStructuredTypePart) value;
		} else {
			throw error(I18NConstants.ERROR_EXPECTED_TYPE_PART_LITERAL__EXPR.fill(toString(expr)));
		}
	}

	/**
	 * Resolves a {@link TLAssociationEnd} from a literal argument.
	 */
	protected static TLAssociationEnd resolveEnd(Expr expr, SearchExpression arg)
			throws ConfigurationException {
		Object value = resolveLiteral(expr, arg).getValue();
		if (value instanceof TLAssociationEnd) {
			return (TLAssociationEnd) value;
		} else {
			throw error(I18NConstants.ERROR_EXPECTED_ASSOCIATION_END_LITERAL__EXPR.fill(toString(expr)));
		}
	}

	/**
	 * Resolves a {@link TLStructuredTypePart} from a literal argument.
	 */
	protected static TLStructuredType resolveStructuredType(Expr expr, SearchExpression arg)
			throws ConfigurationException {
		Object value = resolveLiteral(expr, arg).getValue();
		if (value instanceof TLStructuredType) {
			TLStructuredType result = (TLStructuredType) value;
			return result;
		} else {
			throw error(I18NConstants.ERROR_EXPECTED_STRUCTURED_TYPE_LITERAL__EXPR.fill(toString(expr)));
		}
	}

	private static Literal resolveLiteral(Expr expr, SearchExpression arg) throws ConfigurationException {
		if (arg instanceof Literal) {
			return (Literal) arg;
		} else {
			throw error(I18NConstants.ERROR_LITERAL_ARGUMENT__EXPR.fill(toString(expr)));
		}
	}

}
