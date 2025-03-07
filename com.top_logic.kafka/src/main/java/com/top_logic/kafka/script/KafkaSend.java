/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.script;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.kafka.serialization.GenericSerializer;
import com.top_logic.kafka.services.producer.KafkaProducerService;
import com.top_logic.kafka.services.producer.TLKafkaProducer;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.IsEmpty;
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
		TLKafkaProducer<Object, Object> producer = getProducer(producerName);

		Object message = arguments[1];
		Object key = arguments[2];
		List<Header> headers = asHeaders(arguments[3]);
		String topic = asString(arguments[4], producer.getTopic());
		long timeout = asLong(arguments[5], DEFAULT_TIMEOUT);
		Integer partition = arguments[6] == null ? null : Integer.valueOf(asInt(arguments[6]));

		send(producer, timeout, new ProducerRecord<>(topic, partition, key, message, headers));
		return null;
	}

	private void send(TLKafkaProducer<Object, Object> producer, long timeout, ProducerRecord<Object, Object> record) {
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
	private TLKafkaProducer<Object, Object> getProducer(String producerName) {
		/* That cast is unsafe, but unavoidable. */
		return (TLKafkaProducer<Object, Object>) KafkaProducerService.getInstance().getProducer(producerName);
	}

	private List<Header> asHeaders(Object untypedHeaders) {
		List<Header> result = list();
		for (Object untypedHeader : asCollection(untypedHeaders)) {
			RecordHeader header = asHeader(untypedHeader);
			if (header != null) {
				result.add(header);
			}
		}
		return result;
	}

	private RecordHeader asHeader(Object untypedHeader) {
		Object key;
		Object value = null;
		if (untypedHeader instanceof Collection<?> collection) {
			// A key-value pair.
			if (collection.isEmpty()) {
				return null;
			}

			Iterator<?> it = collection.iterator();
			key = it.next();

			if (it.hasNext()) {
				// There is a value.
				value = it.next();
			}
			if (it.hasNext()) {
				// There is more than one value.
				ArrayList<Object> list = new ArrayList<>();
				list.add(value);
				while (it.hasNext()) {
					list.add(it.next());
				}

				value = list;
			}
		} else if (untypedHeader instanceof Map.Entry<?, ?> entry) {
			// A "canonical" key-value pair.
			key = entry.getKey();
			value = entry.getValue();
		} else {
			// Only a key.
			key = untypedHeader;
		}

		if (IsEmpty.compute(key)) {
			return null;
		}

		return new RecordHeader(asString(key), GenericSerializer.doSerialize(value, StandardCharsets.UTF_8));
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
			.optional("partition")
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
