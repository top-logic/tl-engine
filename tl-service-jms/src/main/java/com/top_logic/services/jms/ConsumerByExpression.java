/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ComputationEx;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

/**
 * Consumer implementation that processes the message via a TL-Script function.
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
 */
public class ConsumerByExpression extends Consumer<ConsumerByExpression.Config> {

	private QueryExecutor _processing;

	/**
	 * Configuration options for {@link ConsumerByExpression}.
	 */
	public interface Config extends Consumer.Config<ConsumerByExpression> {

		boolean getTransaction();

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
	public ConsumerByExpression(InstantiationContext instContext, Config config) {
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

			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			if (getConfig().getTransaction()) {
				try (Transaction tx = kb.beginTransaction()) {
					internalProcess(message);
					tx.commit();
				}
			} else {
				kb.withoutModifications(new ComputationEx<Void, JMSException>() {
					@Override
					public Void run() throws JMSException {
						internalProcess(message);
						return null;
					}
				});
			}


		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}

	private void internalProcess(Message message) throws JMSException {
		Map<String, Object> properties = new HashMap<>();
		Enumeration<String> propertyNames = message.getPropertyNames();
		while (propertyNames.hasMoreElements()) {
			String key = propertyNames.nextElement();
			properties.put(key, message.getObjectProperty(key));
		}

		if (message instanceof TextMessage) {
			String msg = message.getBody(String.class);
			properties.put("message-type", "String");
			_processing.execute(msg, properties);
		} else if (message instanceof BytesMessage) {
			String contentType = (String) properties.get("ContentType");
			String fileName = (String) properties.get("FileName");
			BinaryData msg =
				BinaryDataFactory.createBinaryData(message.getBody(byte[].class), contentType, fileName);
			properties.put("message-type", "Binary");
			_processing.execute(msg, properties);
		} else if (message instanceof MapMessage) {
			MapMessage mMessage = (MapMessage) message;
			Map<String, Object> msg = new HashMap<>();
			Enumeration<String> en = mMessage.getMapNames();
			while (en.hasMoreElements()) {
				String key = en.nextElement();
				msg.put(key, mMessage.getObject(key));
			}
			properties.put("message-type", "Map");
			_processing.execute(msg, properties);
		}
	}
}
