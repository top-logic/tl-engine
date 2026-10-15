/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
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
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} reporting whether the current user may make a call of a configured
 * TL-Script function, without making it.
 *
 * <p>
 * The argument is the call itself, e.g. <code>canCall(approveMilestone($m))</code>. Only the
 * arguments of that call are evaluated; the result is
 * {@link ConfiguredTLScriptFunctions#isCallAllowed(String, Object[])}.
 * </p>
 *
 * @see PrivilegedScript
 */
public class CanCall extends GenericMethod {

	/**
	 * Creates a {@link CanCall}.
	 */
	CanCall(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CanCall(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE);
	}

	/**
	 * Evaluates only the arguments of the wrapped call, not the call itself.
	 */
	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		GenericMethod call = (GenericMethod) getArguments()[0];
		SearchExpression[] callArguments = call.getArguments();
		Object[] values = new Object[callArguments.length];
		for (int n = 0; n < values.length; n++) {
			values[n] = callArguments[n] == null ? null : callArguments[n].eval(definitions);
		}
		return eval(new Object[] { call.getName(), values }, definitions);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String scriptName = (String) arguments[0];
		Object[] callArguments = (Object[]) arguments[1];
		return Boolean.valueOf(ConfiguredTLScriptFunctions.Module.INSTANCE.getImplementationInstance()
			.isCallAllowed(scriptName, callArguments));
	}

	/**
	 * The check depends on the current user and can therefore not be evaluated at compile time.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating the {@code canCall} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<CanCall> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("call")
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
		public CanCall build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			if (!(args[0] instanceof QueryExecutorMethod || args[0] instanceof TracingScriptMethod)) {
				throw error(I18NConstants.ERROR_CAN_CALL_NO_CALL__EXPR.fill(toString(expr)));
			}
			return new CanCall(getName(), args);
		}
	}

}
