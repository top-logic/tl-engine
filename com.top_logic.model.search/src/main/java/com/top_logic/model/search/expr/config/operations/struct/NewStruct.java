/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.struct;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Constructor function for a {@link StructValue}.
 * 
 * <p>
 * The self argument is expected to be a {@link StructType}, all following arguments are expected to
 * be the struct values.
 * </p>
 * 
 * @see StructTypeDef
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NewStruct extends SimpleGenericMethod {

	/**
	 * Creates a {@link NewStruct}.
	 */
	protected NewStruct(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new NewStruct(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	public Object eval(Object[] arguments) {
		if (!(arguments[0] instanceof StructType)) {
			throw new TopLogicException(
				I18NConstants.ERROR_STRUCT_TYPE_EXPECTED__VALUE_EXPR.fill(arguments[0], getArguments()[0]));
		}
		StructType type = (StructType) arguments[0];

		if (arguments.length != type.size() + 1) {
			throw new TopLogicException(I18NConstants.ERROR_ARGUMENT_COUNT_MISMATCH__EXPECTED_ACTUAL_EXPR
				.fill(type, arguments.length, this));
		}

		return new StructValue(type, Arrays.copyOfRange(arguments, 1, arguments.length));
	}

	/**
	 * {@link MethodBuilder} creating {@link NewStruct}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<NewStruct> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public NewStruct build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkMinArgs(expr, args, 1);
			return new NewStruct(getConfig().getName(), self, args);
		}
	}

}
