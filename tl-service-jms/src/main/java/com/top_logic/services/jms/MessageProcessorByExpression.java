/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.services.jms.JMSService.MessageProcessor;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

/**
 * Configurable {@link MessageProcessor} using TL-Script expressions.
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
 */
public class MessageProcessorByExpression extends AbstractConfiguredInstance<MessageProcessorByExpression.Config<?>>
		implements MessageProcessor {

	private QueryExecutor _processing;

	/**
	 * Configuration options for {@link MessageProcessorByExpression}.
	 */
	public interface Config<I extends MessageProcessorByExpression> extends PolymorphicConfiguration<I> {
		/**
		 * The function that will be used to process messages the consumer will receive.
		 */
		Expr getProcessing();
	}

	/**
	 * Creates a {@link MessageProcessorByExpression} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public MessageProcessorByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);
		_processing = QueryExecutor.compile(config.getProcessing());
	}

	@Override
	public void processMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				String msg = message.getBody(String.class);
				_processing.execute(msg);
			} else if (message instanceof BytesMessage) {
				byte[] msg = message.getBody(byte[].class);
				_processing.execute(msg);
			}
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}

}