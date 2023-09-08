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
	protected Attribute(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Attribute(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TlModelFactory.getTLPropertyType();
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		if (self instanceof TLStructuredType) {
			return ((TLStructuredType) self).getPart(asString(arguments[0]));
		} else {
			return null;
		}
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
		public Attribute build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new Attribute(getConfig().getName(), self, args);
		}
	}
}
