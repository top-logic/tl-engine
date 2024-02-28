/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.session;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.TypedAnnotatable;
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
 * Method that allows to set user-local configuration values.
 * 
 * @see LoadFromSession
 */
public class StoreInSession extends GenericMethod {

	static final Property<Map<String, Object>> SCRIPT_STATE = TypedAnnotatable.propertyMap("scriptState");

	/**
	 * Creates a {@link StoreInSession}.
	 */
	protected StoreInSession(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new StoreInSession(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object value = arguments[0];
		String key = asString(arguments[1]);
		boolean persist = asBoolean(arguments[2]);

		if (persist) {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();

			Object before = pc.getJSONValue(key);
			pc.setJSONValue(key, value);
			return before;
		} else {
			return DefaultDisplayContext.getDisplayContext().getSessionContext().mkMap(SCRIPT_STATE).put(key, value);
		}
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link StoreInSession}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<StoreInSession> {
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor
			.builder()
			.mandatory("value")
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
		public StoreInSession build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new StoreInSession(getConfig().getName(), args);
		}

	}

}
