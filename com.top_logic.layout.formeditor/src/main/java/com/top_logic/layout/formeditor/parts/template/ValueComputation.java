/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link VariableDefinition} that computes the variable's value through a <i>TL-Script</i>
 * function.
 */
public class ValueComputation extends AbstractConfiguredInstance<ValueComputation.Config>
		implements VariableDefinition {

	/**
	 * Definition of a local template variable.
	 */
	@DisplayOrder({
		Config.NAME,
		Config.INPUT,
		Config.FUNCTION,
	})
	@TagName("value-computation")
	public interface Config extends VariableDefinition.Config<ValueComputation> {

		/**
		 * @see #getInput()
		 */
		String INPUT = "input";

		/**
		 * Source of an additional input to the specified {@link #getFunction()}.
		 */
		@Name(INPUT)
		ModelSpec getInput();

		/**
		 * @see #getFunction()
		 */
		String FUNCTION = "function";

		/**
		 * Function taking the currently rendered object as first argument and the additionally
		 * specified input model (if one is given) as second argument. The value computed can be
		 * accessed from the template through the variable with the given {@link #getName()}.
		 */
		@Name(FUNCTION)
		@Mandatory
		Expr getFunction();
	}

	private QueryExecutor _function;

	private ChannelLinking _model;

	/**
	 * Creates a {@link ValueComputation} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ValueComputation(InstantiationContext context, Config config) {
		super(context, config);

		_function = QueryExecutor.compile(config.getFunction());
		_model = context.getInstance(config.getInput());
	}

	@Override
	public EvalResult eval(DisplayContext displayContext, LayoutComponent component, Object model) {
		if (_model == null) {
			return new ResultValue(_function.execute(displayContext, null, model));
		}

		ComponentChannel channel = _model.resolveChannel(null, component);

		return new DynamicEvalResult(_function, model, channel);
	}

	private static class DynamicEvalResult implements EvalResult, ChannelListener {

		private QueryExecutor _function;

		private Object _model;

		private ComponentChannel _channel;

		private InvalidateListener _listener;

		/**
		 * Creates a {@link DynamicEvalResult}.
		 */
		public DynamicEvalResult(QueryExecutor function, Object model, ComponentChannel channel) {
			_function = function;
			_model = model;
			_channel = channel;
		}

		@Override
		public Object getValue(DisplayContext displayContext) {
			Object input = _channel.get();
			return _function.executeWith(displayContext, null, Args.some(_model, input));
		}

		@Override
		public boolean addInvalidateListener(InvalidateListener listener) {
			if (_listener == listener) {
				return false;
			}

			if (_listener != null) {
				throw new IllegalStateException("Only a single listener supported.");
			}

			_listener = listener;
			_channel.addListener(this);

			return true;
		}

		@Override
		public boolean removeInvalidateListener(InvalidateListener listener) {
			if (_listener == listener) {
				detach();
				return true;
			}
			return false;
		}

		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			if (_listener != null) {
				_listener.handleValueInvalidation(this);
			}

			detach();
		}

		private void detach() {
			if (_listener != null) {
				_listener = null;
				_channel.removeListener(this);
			}
		}

	}

}
