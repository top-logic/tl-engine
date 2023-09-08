/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.connect.rest;

import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.kafka.services.connect.ConnectThreadOutgoing.KafkaSender;

/**
 * An abstract {@link KafkaSender} implementation for RESTful services.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public abstract class RESTSender implements KafkaSender {

	/**
	 * Typed configuration interface definition for {@link RESTSender}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	public interface Config extends PolymorphicConfiguration<RESTSender> {

		/**
		 * the URL to send data to
		 */
		@Mandatory
		String getUrl();
		
		/**
		 * @see RESTSender#_errorThreshold
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
	 * Create a {@link RESTSender}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public RESTSender(final InstantiationContext context, final Config config) {
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
	public final void send(final ConsumerRecords<Object, Object> data) {
		try {
			// try to transmit the given data
			execute(data);

			// reset the error count upon successful transmission
			_errorCount.set(0);
		} catch (Exception e) {
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
	 * @see #send(ConsumerRecords)
	 */
	protected abstract void execute(ConsumerRecords<Object, Object> data) throws Exception;
	
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
