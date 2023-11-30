/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms.script;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.services.jms.Consumer;
import com.top_logic.services.jms.JMSService;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} receiving a message from a JMS Message Queue System.
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
 */
public class JMSReceive extends GenericMethod {

	/**
	 * Creates a {@link JMSReceive} expression.
	 */
	protected JMSReceive(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new JMSReceive(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (!JMSService.Module.INSTANCE.isActive()) {
			throw new TopLogicException(I18NConstants.ERROR_JMS_SERVICE_NOT_STARTED__EXPR.fill(this));
		}
		JMSService jmsService = JMSService.Module.INSTANCE.getImplementationInstance();
		String connectionName = asString(arguments[0]);

		Consumer consumer = jmsService.getConsumer(connectionName);
		if (consumer == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_CONNECTION__NAME_EXPR.fill(connectionName, this));
		}
		consumer.receive();

		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}


	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link JMSReceive} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<JMSReceive> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("connection")
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
		public JMSReceive build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new JMSReceive(getConfig().getName(), args);
		}
	}

}
