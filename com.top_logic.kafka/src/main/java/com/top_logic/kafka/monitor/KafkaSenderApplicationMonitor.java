/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.monitor;

import java.time.Instant;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.kafka.services.producer.KafkaProducerService;
import com.top_logic.kafka.services.producer.TLKafkaProducer;
import com.top_logic.util.monitor.ConfiguredMonitorComponent;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorMessage.Status;
import com.top_logic.util.monitor.MonitorResult;

/**
 * The {@link MonitorComponent} for the Kafka binding for sending.
 * <p>
 * It reports {@link Status#ERROR}, whenever sending is not possible, irrespective of the reason.
 * Therefore, it will for example report {@link Status#ERROR} during the application startup, until
 * the relevant {@link ManagedClass services} have been started. Only the user can decide, whether
 * it is currently okay that this service is not operational. If this class reported "okay" in such
 * cases, it would mislead the problem analysis if it was not correct that this service is for
 * example not started or shut down.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class KafkaSenderApplicationMonitor extends ConfiguredMonitorComponent<KafkaSenderApplicationMonitor.Config> {

	/** {@link ConfigurationItem} for the {@link KafkaSenderApplicationMonitor}. */
	public interface Config extends ConfiguredMonitorComponent.Config {

		@Override
		@StringDefault("Kafka sender")
		String getName();

		@Override
		@StringDefault("Whether the last send operation of the Kafka binding succeeded.")
		String getDescription();

	}

	/** {@link TypedConfiguration} constructor for {@link KafkaSenderApplicationMonitor}. */
	public KafkaSenderApplicationMonitor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void checkStateInTryCatch(MonitorResult result) {
		if (!KafkaProducerService.Module.INSTANCE.isActive()) {
			String message = "Not started. Sending messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		KafkaProducerService sender = KafkaProducerService.getInstance();
		if (!sender.isStarted()) {
			String message = "Still starting or already shut down. Sending messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		Set<String> producerNames = sender.getConfig().getProducers().keySet();
		if (producerNames.isEmpty()) {
			String message = "Started, but unused: There are no producers.";
			result.addMessage(createMessage(Status.INFO, message));
			return;
		}
		for (String name : producerNames) {
			TLKafkaProducer<?, ?> producer = sender.getProducer(name);
			checkState(name, producer, result);
		}
	}

	private void checkState(String name, TLKafkaProducer<?, ?> producer, MonitorResult result) {
		if (producer == null) {
			String message = "It is configured but was not created during startup."
				+ " It is therefore not possible for it to send messages.";
			result.addMessage(createMessage(Status.ERROR, message, name));
			return;
		}
		Instant lastSendTimestamp = producer.getLastSendTimestamp();
		if (lastSendTimestamp == null) {
			String message = "Did not try to send something, yet.";
			result.addMessage(createMessage(Status.INFO, message, name));
			return;
		}
		Throwable lastRunError = producer.getLastError();
		if (lastRunError != null) {
			String message = "Failed. Cause: " + lastRunError.getMessage();
			result.addMessage(createMessage(Status.ERROR, message, name));
			return;
		}
		String message = "Working properly.";
		result.addMessage(createMessage(Status.INFO, message, name));
	}

	/**
	 * Creates a {@link MonitorMessage} for one of several parts monitored by this
	 * {@link MonitorComponent}.
	 * <p>
	 * Use this method when this {@link MonitorComponent} monitors several parts and produces
	 * messages a message for each of them. They will be displayed with the name of this
	 * {@link MonitorComponent}, followed by the part name.
	 * </p>
	 * 
	 * @param partName
	 *        The name of the part this message is about.
	 */
	protected MonitorMessage createMessage(Status statusType, String message, String partName) {
		return new MonitorMessage(statusType, message, getName() + " '" + partName + "'");
	}

}
