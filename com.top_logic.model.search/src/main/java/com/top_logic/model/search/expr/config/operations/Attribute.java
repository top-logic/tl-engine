/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link GenericMethod} looking up an {@link TLStructuredTypePart} of a {@link TLStructuredType}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Attribute extends SimpleGenericMethod {

	/**
	 * Creates a {@link Attribute}.
	 */
	protected Attribute(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Attribute(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TlModelFactory.getTLPropertyType();
	}

	@Override
	public Object eval(Object[] arguments) {
		if (arguments[0] instanceof TLStructuredType) {
			return ((TLStructuredType) arguments[0]).getPart(asString(arguments[1]));
		} else {
			return null;
		}
	}

	/**
	 * {@link Attribute} can not be evaluated at compile time, because the attribute with the given
	 * name could have been deleted or not yet created.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return !(arguments[0] instanceof TLStructuredType);
	}

	/**
	 * {@link MethodBuilder} creating {@link Attribute}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Attribute> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Attribute build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkTwoArgs(expr, args);
			return new Attribute(getConfig().getName(), args);
		}

	}
}
