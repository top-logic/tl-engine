/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.monitor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.time.TimeUtil;
import com.top_logic.kafka.services.consumer.ConsumerDispatcher;
import com.top_logic.kafka.services.consumer.KafkaConsumerService;
import com.top_logic.util.monitor.ConfiguredMonitorComponent;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorMessage.Status;
import com.top_logic.util.monitor.MonitorResult;

/**
 * The {@link MonitorComponent} for the Kafka binding for receiving.
 * <p>
 * It reports {@link Status#ERROR}, whenever receiving is not possible, irrespective of the reason.
 * Therefore, it will for example report {@link Status#ERROR} during the application startup, until
 * the relevant {@link ManagedClass services} have been started. Only the user can decide, whether
 * it is currently okay that this service is not operational. If this class reported "okay" in such
 * cases, it would mislead the problem analysis if it was not correct that this service is for
 * example not started or shut down.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class KafkaReceiverApplicationMonitor
		extends ConfiguredMonitorComponent<KafkaReceiverApplicationMonitor.Config> {

	/** {@link ConfigurationItem} for the {@link KafkaReceiverApplicationMonitor}. */
	public interface Config extends ConfiguredMonitorComponent.Config {

		@Override
		@StringDefault("Kafka receiver")
		String getName();

		@Override
		@StringDefault("Whether the last receive operation of the Kafka binding succeeded.")
		String getDescription();

		/** How long a receive operation can run, before it is considered to be "too long". */
		@LongDefault(10 * 60 * 1000)
		@Format(MillisFormat.class)
		long getMaxProcessingTime();

	}

	/** {@link TypedConfiguration} constructor for {@link KafkaReceiverApplicationMonitor}. */
	public KafkaReceiverApplicationMonitor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void checkStateInTryCatch(MonitorResult result) {
		if (!KafkaConsumerService.Module.INSTANCE.isActive()) {
			String message = "Not started. Receiving messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		KafkaConsumerService sender = KafkaConsumerService.getInstance();
		if (!sender.isStarted()) {
			String message = "Still starting or already shut down. Receiving messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		List<ConsumerDispatcher<?, ?>> consumers = sender.getConsumers();
		if (consumers.isEmpty()) {
			String message = "Started, but unused: There are no consumers.";
			result.addMessage(createMessage(Status.INFO, message));
			return;
		}
		for (ConsumerDispatcher<?, ?> consumer : consumers) {
			checkState(consumer, result);
		}
	}

	private void checkState(ConsumerDispatcher<?, ?> consumer, MonitorResult result) {
		String name = consumer.getName();
		Instant lastRunEndTimestamp = consumer.getLastRunEndTimestamp();
		if (lastRunEndTimestamp == null) {
			String message = "Not started yet. It is therefore not possible for it to receive messages.";
			result.addMessage(createMessage(Status.ERROR, message, name));
			return;
		}
		if (isTooLongAgo(lastRunEndTimestamp)) {
			String maxTime = TimeUtil.formatMillisAsTime(getMaxProcessingTime());
			String message = "The last run finished too long ago: " + lastRunEndTimestamp
					+ ". It should not be more than a few minutes ago (" + maxTime
					+ "). Receiving messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message, name));
			return;
		}
		Throwable lastRunError = consumer.getLastRunError();
		if (lastRunError != null) {
			String message = "Failed. Cause: " + lastRunError.getMessage();
			result.addMessage(createMessage(Status.ERROR, message, name));
			return;
		}
		String message = "Working properly.";
		result.addMessage(createMessage(Status.INFO, message, name));
	}

	private boolean isTooLongAgo(Instant lastRunEndTimestamp) {
		Duration timePassed = Duration.between(lastRunEndTimestamp, Instant.now());
		return timePassed.toMillis() > getMaxProcessingTime();
	}

	private long getMaxProcessingTime() {
		return getConfig().getMaxProcessingTime();
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
