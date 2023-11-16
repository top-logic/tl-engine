/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.util.model.ModelService;

/**
 * {@link SearchExpression} creating a new object of a given {@link TLClass} type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateObject extends AbstractObjectCreation {

	/**
	 * Creates a {@link CreateObject}.
	 * @param args
	 *        The optional create context (at most a single argument).
	 */
	CreateObject(String name, SearchExpression[] args) {
		super(name, args);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CreateObject(getName(), arguments);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLClass type = (TLClass) asStructuredTypeNonNull(arguments[0], getArguments()[0]);
		TLObject context = asTLObject(arguments[1]);
		boolean transientObject = asBoolean(arguments[2]);
		if (transientObject) {
			return TransientObjectFactory.INSTANCE.createObject(type, context);
		} else {
			return ModelService.getInstance().getFactory().createObject(type, context, null);
		}
	}

	/**
	 * Builder creating a {@link CreateObject} expression.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<CreateObject> {
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("type")
			.optional("context")
			.optional("transient", false)
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public CreateObject build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new CreateObject(getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}

}
