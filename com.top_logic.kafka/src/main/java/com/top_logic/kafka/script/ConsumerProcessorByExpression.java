/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.script;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.util.Utils.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.kafka.services.consumer.ConsumerProcessor;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * A {@link ConsumerProcessor} that can be configured In-App to process text messages.
 * <p>
 * Messages are consumed in-order, as long as there is only one partition for the Kafka topic and
 * all messages come from the same sender.
 * </p>
 * <p>
 * Every message is processed at least once. Messages may be received in groups. When processing at
 * least one message in a group fails for at least one {@link ConsumerProcessor}, the entire group
 * will be processed again by every registered {@link ConsumerProcessor}.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class ConsumerProcessorByExpression extends AbstractConfiguredInstance<ConsumerProcessorByExpression.Config>
		implements ConsumerProcessor<String, String> {

	/** {@link ConfigurationItem} for the {@link ConsumerProcessorByExpression}. */
	public interface Config extends PolymorphicConfiguration<ConsumerProcessorByExpression> {

		/**
		 * The TL-Script that will be called for each received message.
		 * <p>
		 * The script receives the following parameters:
		 * <ol>
		 * <li>{@link ConsumerRecord#value() message}: String.</li>
		 * <li>{@link ConsumerRecord#key() key}: String</li>
		 * <li>{@link ConsumerRecord#headers() headers}: A list of key-value-pairs. Both are
		 * Strings. There can be multiple headers with the same key. Kafka header values are
		 * actually binary data. As they are usually Strings though, they are interpreted as UTF-8
		 * Strings here for convenience.</li>
		 * <li>{@link ConsumerRecord#topic() topic}: String</li>
		 * </ol>
		 * </p>
		 * <p>
		 * The script is executed within a {@link KnowledgeBase} transaction. To commit the changes,
		 * the script has to return <code>true</code>. To rollback the transaction, the script has
		 * to return <code>false</code>. Any other return value, including <code>null</code> will
		 * cause an exception to be thrown, the transaction to be rolled back and the message to be
		 * processed again.
		 * </p>
		 */
		Expr getProcessor();

	}

	private final QueryExecutor _processor;

	/** {@link TypedConfiguration} constructor for {@link ConsumerProcessorByExpression}. */
	public ConsumerProcessorByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_processor = QueryExecutor.compile(prefixWithParameters(config.getProcessor()));
	}

	private Define prefixWithParameters(Expr scriptBody) {
		Define withTopic = Define.create("topic", scriptBody);
		Define withHeaders = Define.create("headers", withTopic);
		Define withKey = Define.create("key", withHeaders);
		return Define.create("message", withKey);
	}

	@Override
	public void process(ConsumerRecords<String, String> records) {
		for (ConsumerRecord<String, String> record : records) {
			try (Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				boolean doCommit = processMessage(record);
				if (doCommit) {
					transaction.commit();
				}
			}
		}
	}

	private boolean processMessage(ConsumerRecord<String, String> message) {
		Object result = processMessageSafe(message);
		if (result instanceof Boolean) {
			return (boolean) result;
		}
		throw new RuntimeException("Script did neither return 'true' nor 'false' but: " + debug(result));
	}

	private Object processMessageSafe(ConsumerRecord<String, String> message) {
		try {
			return _processor.execute(message.value(), message.key(), toList(message.headers()), message.topic());
		} catch (RuntimeException | Error exception) {
			throw new RuntimeException("Script failed to process the message.", exception);
		}
	}

	private List<List<String>> toList(Headers headers) {
		/* This method does not create a Map, as the headers keys are not distinct. I.e. there can
		 * be multiple headers with the same key. */
		List<List<String>> list = list();
		for (Header header : headers) {
			String value = new String(header.value(), StandardCharsets.UTF_8);
			list.add(List.of(header.key(), value));
		}
		return list;
	}

}
