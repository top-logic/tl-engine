/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.logging.Level;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.Argument;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Method;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.I18NConstants;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.TLContext;

/**
 * Reports a message to the application log at a selectable {@link Level}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Log extends GenericMethod {

	/**
	 * Creates a {@link Log}.
	 *
	 * <p>
	 * The first argument selects the {@link Level}, the second is the message and any further
	 * arguments are the {@link MessageFormat} arguments filled into the message.
	 * </p>
	 */
	protected Log(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Log(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Level level = ScriptLog.parse(arguments[0]);
		String message = asString(arguments[1]);
		if (arguments.length > 2) {
			message = MessageFormat.format(message, Arrays.copyOfRange(arguments, 2, arguments.length));
		}
		Logger.log("Script (" + whoAmI() + ") reported: " + message, Log.class, level);
		return null;
	}

	private String whoAmI() {
		TLContext context = TLContext.getContext();
		if (context == null) {
			return "no context";
		}
		Person user = context.getCurrentPersonWrapper();
		if (user != null) {
			return user.getName();
		}
		return context.getContextId();
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link Log}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Log> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Log build(Method expr, Argument[] args) throws ConfigurationException {
			SearchExpression level = null;
			List<SearchExpression> positional = new ArrayList<>();
			for (Argument arg : args) {
				String name = arg.getName();
				if (name == null) {
					positional.add(arg.getValue());
				} else if (ScriptLog.ARGUMENT.equals(name)) {
					if (level != null) {
						throw error(I18NConstants.ERROR_AMBIGUOUS_ARGUMENT__FUN_NAME.fill(expr.getName(), name));
					}
					level = arg.getValue();
				} else {
					throw error(I18NConstants.ERROR_NO_SUCH_NAMED_ARGUMENT__FUN_NAME_AVAIL.fill(expr.getName(), name,
						Collections.singletonList(ScriptLog.ARGUMENT)));
				}
			}
			if (positional.isEmpty()) {
				throw error(I18NConstants.ERROR_AT_LEAST_ARGUMENTS_EXPECTED__CNT_EXPR.fill(1, toString(expr)));
			}
			if (level == null) {
				level = SearchExpressionFactory.literal(ScriptLog.defaultName());
			}

			SearchExpression[] result = new SearchExpression[positional.size() + 1];
			result[0] = level;
			for (int n = 0; n < positional.size(); n++) {
				result[n + 1] = positional.get(n);
			}
			return build(expr, result);
		}

		@Override
		public Log build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Log(getName(), args);
		}

	}
}
