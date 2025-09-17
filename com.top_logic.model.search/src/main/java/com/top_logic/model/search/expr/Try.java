/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * TLScript method implementing try-catch functionality for exception handling.
 * 
 * <p>
 * Executes a try function and provides fallback behavior through a catch function if an exception
 * occurs.
 * </p>
 * 
 * <p>
 * Example:
 * </p>
 * 
 * <pre>
* $emailAddress.try(
*   emailAddr -> sendMail(subject: $subject, to: $emailAddr, body: $body),
*   catch: error -> originalArg -> log("Mail failed for " + $originalArg + ": " + $error)
* )
 * </pre>
 */
public class Try extends GenericMethod {

	/**
	 * Creates a new {@link Try}.
	 */
	protected Try(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Try(getName(), arguments);
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (arguments.length < 2) {
			throw new TopLogicException(
				I18NConstants.ERROR_TRY_METHOD_REQUIRES_TWO_ARGUMENTS);
		}

		Object argument = arguments[0];
		SearchExpression tryFunction = asSearchExpression(arguments[1]);
		SearchExpression catchFunction = null;

		// Check if catch function is provided and not null
		if (arguments.length > 2 && arguments[2] != null) {
			catchFunction = asSearchExpression(arguments[2]);
		}

		try {
			// Execute the try function with the given argument
			return tryFunction.eval(definitions, argument);
		} catch (Exception e) {
			// If no catch function provided, return null
			if (catchFunction == null) {
				return null;
			}
			if (e instanceof ScriptAbort) { // coming from a throw() call
				ScriptAbort scriptAbort = (ScriptAbort) e;
				Object value = scriptAbort.getValue();
				// Pass message, original argument, and value from ScriptAbort
				return catchFunction.eval(definitions, scriptAbort.getMessage(), argument, value);
			} else if (e instanceof I18NException) {
				ResKey errorKey = ((I18NException) e).getErrorKey();
				return catchFunction.eval(definitions, errorKey, argument);
			} else {
				return catchFunction.eval(definitions, e.getMessage(), argument);
			}
		}
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating a {@link Try} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Try> {

		/** Description of parameters for a {@link Try}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("argument")
			.mandatory("tryFunction")
			.optional("catchFunction")
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
		public Try build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Try(getConfig().getName(), args);
		}
	}
}
