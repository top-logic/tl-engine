/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms.script;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DefaultDisplayContext;
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
 * {@link GenericMethod} sending a message to a JMS Message Queue System.
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
 */
public class JMSSend extends GenericMethod {

	/**
	 * Creates a {@link JMSSend} expression.
	 */
	protected JMSSend(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new JMSSend(getName(), arguments);
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

		Producer producer = jmsService.getProducer(connectionName);
		if (producer == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_CONNECTION__NAME_EXPR.fill(connectionName, this));
		}
		Object rawData = arguments[1];
		if (rawData instanceof HTMLFragment) {
			StringWriter sw = new StringWriter();
			TagWriter tw = new TagWriter(sw);
			HTMLFragment html = (HTMLFragment) rawData;
			try {
				html.write(DefaultDisplayContext.getDisplayContext(), tw);
			} catch (IOException ex) {
				throw new TopLogicException(I18NConstants.ERROR_WRITING_XML, ex);
			}
			producer.send(sw.toString());
		} else if (rawData instanceof BinaryDataSource) {
			producer.send((BinaryDataSource) rawData);
		} else if (rawData instanceof Map) {
			producer.send((Map) rawData);
		} else {
			producer.send(ToString.toString(rawData));
		}

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
		public JMSSend build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new JMSSend(getConfig().getName(), args);
		}
	}

}
