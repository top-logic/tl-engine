/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

/**
 * Consumer implementation that processes the message via a TL-Script function.
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
 */
public class ConsumerByExpression extends Consumer {

	private QueryExecutor _processing;

	/**
	 * Configuration options for {@link ConsumerByExpression}.
	 */
	public interface Config<I extends ConsumerByExpression> extends Consumer.Config<I> {
		/**
		 * The function that will be used to process messages the consumer will receive.
		 */
		Expr getProcessing();
	}

	/**
	 * Constructor for the consumer that processes the message via a TL-Script function.
	 * 
	 * @param instContext
	 *        The context which can be used to instantiate inner configurations.
	 * @param config
	 *        The configuration for the consumer.
	 */
	public ConsumerByExpression(InstantiationContext instContext, Config<?> config) {
		super(instContext, config);
		_processing = QueryExecutor.compile(config.getProcessing());
	}

	/**
	 * Processes the received Message with the defined process expression.
	 * 
	 * @param message
	 *        The message to be processed
	 */
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
