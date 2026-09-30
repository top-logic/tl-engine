/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.logging.Level;
import com.top_logic.event.infoservice.InfoServiceItemMessageFragment;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Reports a message bubble to the UI at a selectable {@link Level}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Info extends GenericMethod {

	/**
	 * Creates a {@link Info}.
	 */
	protected Info(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Info(getName(), arguments);
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
		HTMLFragment message = toFragment(arguments[0]);
		if (message != null) {
			Level level = ScriptLog.parse(arguments[2]);
			Object detail = arguments[1];
			if (detail != null) {
				ScriptLog.showInUI(level, new InfoServiceItemMessageFragment(message, toFragment(detail)));
			} else {
				ScriptLog.showInUI(level, message);
			}
		}
		return null;
	}

	private static HTMLFragment toFragment(Object object) {
		if (object instanceof HTMLFragment) {
			return (HTMLFragment) object;
		}
		return Fragments.message(Throw.toResKey(object));
	}

	/**
	 * {@link MethodBuilder} creating {@link Info}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Info> {

		/**
		 * Description of the arguments of the <code>info</code> function.
		 */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("message")
			.optional("detail")
			.optional(ScriptLog.ARGUMENT, ScriptLog.defaultName())
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
		public Info build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Info(getName(), args);
		}

	}
}
