/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.supplier;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * A {@link SearchExpression} creating a value.
 * <p>
 * This is the {@link SearchExpression} equivalent of the Java type {@link Supplier}.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class SupplierSearchExpression extends GenericMethod {

	private static final SearchExpression NO_SELF = null;

	private static final SearchExpression[] NO_ARGUMENTS = new SearchExpression[0];

	private final SupplierSearchExpressionBuilder _builder;

	/** Creates a {@link SupplierSearchExpression}. */
	protected SupplierSearchExpression(String name, SupplierSearchExpressionBuilder builder) {
		super(name, NO_SELF, NO_ARGUMENTS);
		_builder = builder;
	}

	@Override
	public Object getId() {
		return _builder.getId();
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new SupplierSearchExpression(getName(), _builder);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return _builder.getType();
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		return _builder.getValue();
	}

}
