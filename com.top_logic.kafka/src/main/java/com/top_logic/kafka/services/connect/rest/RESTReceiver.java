/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.connect.rest;

import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.kafka.clients.producer.KafkaProducer;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.kafka.services.connect.ConnectThreadIncoming.KafkaReceiver;

/**
 * An abstract {@link KafkaReceiver} implementation for RESTful services.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public abstract class RESTReceiver implements KafkaReceiver {

	/**
	 * Typed configuration interface definition for {@link RESTReceiver}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	public interface Config extends PolymorphicConfiguration<RESTReceiver> {

		/**
		 * the URL to receive data from
		 */
		@Mandatory
		String getUrl();
		
		/**
		 * the name of the topic to store received data in
		 */
		@Mandatory
		String getTopic();
		
		/**
		 * the index of the partition within {@link #getTopic()} to store received data in
		 */
		@IntDefault(0)
		int getPartition();
		
		/**
		 * @see RESTReceiver#_errorThreshold
		 */
		@LongDefault(3)
		long getErrorThreshold();
	}

	/**
	 * @see #getConfig()
	 */
	private final Config _config;
	
	/**
	 * @see #getClient()
	 */
	private final Client _client;
	
	/**
	 * The maximum number of successive errors after which no log messages are
	 * written.
	 */
	private final long _errorThreshold;
	
	/**
	 * The number of successive errors.
	 */
	private final AtomicLong _errorCount;
	
	/**
	 * Create a {@link RESTReceiver}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public RESTReceiver(final InstantiationContext context, final Config config) {
		_config = config;
		_client = ClientBuilder.newClient();
		
		_errorThreshold = config.getErrorThreshold();
		_errorCount = new AtomicLong(0);
	}
	
	@Override
	public void close() throws Exception {
		_client.close();
	}
	
	@Override
	public final void receive(final KafkaProducer<Object, Object> sink) {
		try {
			// try to transmit the given data
			execute(sink);

			// reset the error count upon successful transmission
			_errorCount.set(0);
		} catch (final Exception e) {
			final long count = _errorCount.incrementAndGet();
			
			if(count < _errorThreshold) {
				// log the error
				Logger.error(String.format("Request failed: %s", e.getMessage()), e, getClass());
			} else if(count == _errorThreshold) {
				// log special message once the error threshold has been reached
				Logger.error(String.format("Request failed: %s", e.getMessage()), e, getClass());
				Logger.error(String.format("Maximum number of successive error messages (%d) reached. No more errors will be logged until successful exection.", _errorThreshold), getClass());
			} else {
				// don't log after error threshold has been reached
			}
		}
	}
	
	/**
	 * @see #receive(KafkaProducer)
	 */
	protected abstract void execute(KafkaProducer<Object, Object> sink) throws Exception;
	
	/**
	 * the {@link Config} this {@link RESTReceiver} was instantiated
	 *         with
	 */
	protected Config getConfig() {
		return _config;
	}
	
	/**
	 * the {@link Client} to be used for sending RESTful requests
	 */
	protected Client getClient() {
		return _client;
	}
}
