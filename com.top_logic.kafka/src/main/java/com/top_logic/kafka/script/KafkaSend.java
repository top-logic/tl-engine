/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.script;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.kafka.services.producer.KafkaProducerService;
import com.top_logic.kafka.services.producer.TLKafkaProducer;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} that sends a message using Apache Kafka.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class KafkaSend extends GenericMethod {

	/** The default timeout when sending a message, in milliseconds. */
	private static final long DEFAULT_TIMEOUT = 10_000;

	/** Creates a {@link KafkaSend}. */
	KafkaSend(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public KafkaSend copy(SearchExpression[] arguments) {
		return new KafkaSend(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (!KafkaProducerService.Module.INSTANCE.isActive()) {
			throw new TopLogicException(I18NConstants.ERROR_KAFKA_SERVICE_NOT_STARTED);
		}
		String producerName = asString(arguments[0]);
		TLKafkaProducer<String, String> producer = getProducer(producerName);

		String message = asString(arguments[1]);
		String key = asString(arguments[2]);
		List<Header> headers = asHeaders(arguments[3]);
		String topic = asString(arguments[4], producer.getTopic());
		long timeout = asLong(arguments[5], DEFAULT_TIMEOUT);
		ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, key, message, headers);
		send(producer, timeout, record);
		return null;
	}

	private void send(TLKafkaProducer<String, String> producer, long timeout, ProducerRecord<String, String> record) {
		try {
			producer.send(record).get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException("Failed to send message to Kafka.", exception);
		}
	}

	@SuppressWarnings("unchecked")
	private TLKafkaProducer<String, String> getProducer(String producerName) {
		/* That cast is unsafe, but unavoidable. */
		return (TLKafkaProducer<String, String>) KafkaProducerService.getInstance().getProducer(producerName);
	}

	private List<Header> asHeaders(Object untypedHeaders) {
		List<Header> result = list();
		for (Object headerEntries : asCollection(untypedHeaders)) {
			result.add(asHeader(asList(headerEntries)));
		}
		return result;
	}

	private RecordHeader asHeader(List<?> keyValuePair) {
		if (keyValuePair.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_HEADER_IS_EMPTY);
		}
		if (keyValuePair.size() > 2) {
			throw new TopLogicException(I18NConstants.ERROR_HEADER_HAS_TOO_MANY_ENTRIES__HEADER.fill(keyValuePair));
		}
		String key = asString(keyValuePair.get(0));
		if (key.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_HEADER_KEY_IS_EMPTY);
		}
		/* Some headers might not need a value, for example flag like headers. */
		String value = keyValuePair.size() > 1 ? asString(keyValuePair.get(1)) : "";
		return new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/** {@link MethodBuilder} creating {@link KafkaSend}. */
	public static final class Builder extends AbstractSimpleMethodBuilder<KafkaSend> {

		/** Description of parameters for a {@link KafkaSend}. */
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("producer")
			.mandatory("message")
			.optional("key")
			.optional("headers")
			.optional("topic")
			.optional("timeout")
			.build();

		/** Creates a {@link Builder}. */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public KafkaSend build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new KafkaSend(getConfig().getName(), args);
		}
	}
}
