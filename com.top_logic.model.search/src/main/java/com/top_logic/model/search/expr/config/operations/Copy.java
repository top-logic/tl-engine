/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.model.copy.CopyConstructor;
import com.top_logic.element.model.copy.CopyFilter;
import com.top_logic.element.model.copy.CopyOperation;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.GenericMethodWithSecurity;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Deep object graph copy operation.
 * 
 * <p>
 * Besides the original, the operation takes the following optional arguments:
 * </p>
 * 
 * <ul>
 * <li>A context object in which to allocate the top-level object(s) of the copied graph.</li>
 * <li>A filter function deciding whether to copy a specific value.</li>
 * <li>A constructor function allocating a copy of a specific original.</li>
 * <li>Whether to create transient copies.</li>
 * <li>Whether persistent copies keep the defaults computed in their creating transaction, see
 * {@link CopyOperation#skipTransactionDefaults(boolean)}.</li>
 * </ul>
 * 
 * @see CopyFilter The filter function.
 * @see CopyConstructor The constructor function.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Copy extends GenericMethodWithSecurity implements WithFlatMapSemantics<CopyOperation> {

	private static final NamedConstant COPY_OPERATION = new NamedConstant("copyOperation");

	/**
	 * Creates a {@link Copy} operation.
	 */
	protected Copy(String name, SearchExpression[] arguments, boolean usesSecurity) {
		super(name, arguments, usesSecurity);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Copy(getName(), arguments, usesSecurity());
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		CopyOperation outerOperation = (CopyOperation) definitions.getVarOrNull(COPY_OPERATION);

		CopyOperation operation;
		if (outerOperation == null) {
			operation = CopyOperation.initial();
		} else {
			operation = CopyOperation.nested(outerOperation);
		}
		definitions.defineVar(COPY_OPERATION, operation);
		try {
			Object copyContext = arguments[1];
			Object filterArgument = arguments[2];
			Object constructor = arguments[3];
			Boolean transientCopy = asBooleanOrNull(arguments[4]);
			boolean skipTransactionDefaults = asBoolean(arguments[5]);

			if (copyContext != null) {
				operation.setContext(asTLObject(copyContext), null);
			}

			if (filterArgument != null) {
				if (filterArgument instanceof Boolean) {
					// Interpret as constant function.
					if (!((Boolean) filterArgument).booleanValue()) {
						operation.setFilter((part, value, context) -> false);
					} else {
						// The true filter is default.
					}
				} else {
					SearchExpression filterExpr = asSearchExpression(filterArgument);
					operation.setFilter((part, value, context) -> {
						return asBoolean(filterExpr.eval(definitions, part, value, context));
					});
				}
			}

			if (constructor != null) {
				SearchExpression constructorExpr = asSearchExpression(constructor);
				operation.setConstructor((orig, reference, context) -> {
					return asTLObject(constructorExpr,
						constructorExpr.eval(definitions, orig, reference, context));
				});
			}

			operation.setTransient(transientCopy);
			operation.withSecurity(usesSecurity());
			operation.skipTransactionDefaults(skipTransactionDefaults);

			Object result = evalPotentialFlatMap(definitions, arguments[0], operation);

			operation.finish();
			return result;
		} finally {
			if (outerOperation != null) {
				definitions.defineVar(COPY_OPERATION, outerOperation);
			} else {
				definitions.deleteVar(COPY_OPERATION);
			}
		}
	}

	private static Boolean asBooleanOrNull(Object object) {
		return object == null ? null : asBoolean(object);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, CopyOperation operation) {
		if (singletonValue instanceof TLObject) {
			return operation.copyReference((TLObject) singletonValue);
		}
		if (singletonValue instanceof ConfigurationItem) {
			return TypedConfiguration.copy((ConfigurationItem) singletonValue);
		}
		return singletonValue;
	}

	/**
	 * {@link MethodBuilder} creating {@link Copy}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Copy> {
		/** Name of the argument holding the original to copy. */
		public static final String ORIG = "orig";

		/** Name of the argument holding the context object of the top-level copies. */
		public static final String CONTEXT = "context";

		/** Name of the argument holding the {@link CopyFilter} function. */
		public static final String FILTER = "filter";

		/** Name of the argument holding the function allocating a copy. */
		public static final String CONSTRUCTOR = "constructor";

		/** Name of the argument deciding whether transient copies are created. */
		public static final String TRANSIENT = "transient";

		/**
		 * Name of the argument deciding whether persistent copies keep the defaults computed in their
		 * creating transaction, see {@link CopyOperation#skipTransactionDefaults(boolean)}.
		 */
		public static final String SKIP_TRANSACTION_DEFAULTS = "skipTransactionDefaults";

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory(ORIG)
			.optional(CONTEXT)
			.optional(FILTER)
			.optional(CONSTRUCTOR)
			.optional(TRANSIENT)
			.optional(SKIP_TRANSACTION_DEFAULTS, false)
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Copy build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Copy(getConfig().getName(), args, true);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}

}
