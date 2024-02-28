/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.session;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Method that allows to retrieve user-local configuration values.
 * 
 * @see StoreInSession
 */
public class LoadFromSession extends GenericMethod {

	/**
	 * Creates a {@link LoadFromSession}.
	 */
	protected LoadFromSession(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new LoadFromSession(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String key = asString(arguments[0]);
		boolean persist = asBoolean(arguments[1]);

		if (persist) {
			return PersonalConfiguration.getPersonalConfiguration().getJSONValue(key);
		} else {
			return DefaultDisplayContext.getDisplayContext().getSessionContext().get(StoreInSession.SCRIPT_STATE).get(key);
		}
	}

	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link LoadFromSession}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<LoadFromSession> {
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor
			.builder()
			.mandatory("key")
			.optional("persistent", false)
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
		public LoadFromSession build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new LoadFromSession(getConfig().getName(), args);
		}

	}

}
