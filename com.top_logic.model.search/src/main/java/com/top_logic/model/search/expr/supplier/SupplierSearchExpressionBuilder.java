/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.supplier;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * {@link MethodBuilder} creating {@link SupplierSearchExpression}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class SupplierSearchExpressionBuilder extends AbstractSimpleMethodBuilder<SupplierSearchExpression> {

	/** The {@link ConfigurationItem} for the {@link SupplierSearchExpressionBuilder}. */
	public interface Config extends AbstractSimpleMethodBuilder.Config<SupplierSearchExpressionBuilder> {
		/* Nothing needed but the type itself. */
	}

	/** Creates a {@link SupplierSearchExpressionBuilder}. */
	public SupplierSearchExpressionBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public SupplierSearchExpression build(Expr expr, SearchExpression self, SearchExpression[] arguments)
			throws ConfigurationException {
		checkNoArguments(expr, arguments);
		return new SupplierSearchExpression(getName(), this);
	}

	@Override
	public Object getId() {
		return getClass();
	}

	/** The represented value. */
	public abstract Object getValue();

	/** The {@link TLType} of the {@link #getValue()}. */
	public abstract TLType getType();

	/** Whether the {@link #getValue()} is a single value or a collection of values. */
	public abstract boolean isMultiple();

}
