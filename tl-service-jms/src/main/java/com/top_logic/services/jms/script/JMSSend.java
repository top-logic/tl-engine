/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms.script;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.services.jms.JMSService;
import com.top_logic.services.jms.Producer;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} translating a string using the {@link TranslationService}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JMSSend extends GenericMethod {

	/**
	 * Creates a {@link JMSSend} expression.
	 */
	protected JMSSend(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new JMSSend(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		JMSService jmsService = JMSService.Module.INSTANCE.getImplementationInstance();
		String connectionName = asString(arguments[0]);
		Producer producer = jmsService.getProducer(connectionName);
		if (producer == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_CONNECTION__NAME_EXPR.fill(connectionName, this));
		}
		producer.send(ToString.toString(arguments[1]));
		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}


	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link JMSSend} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<JMSSend> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("connection")
			.mandatory("message")
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
		public JMSSend build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new JMSSend(getConfig().getName(), self, args);
		}

		@Override
		public boolean hasSelf() {
			return false;
		}
	}

}
