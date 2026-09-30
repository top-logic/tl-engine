/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.monitor;

import java.time.Duration;
import java.time.Instant;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.time.TimeUtil;
import com.top_logic.kafka.services.consumer.ConsumerDispatcher;
import com.top_logic.kafka.services.consumer.KafkaConsumerService;
import com.top_logic.util.monitor.ConfiguredMonitorComponent;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage.Status;
import com.top_logic.util.monitor.MonitorResult;

/**
 * The {@link MonitorComponent} for TL-Sync receiving.
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
public class TLSyncReceiverApplicationMonitor
		extends ConfiguredMonitorComponent<TLSyncReceiverApplicationMonitor.Config> {

	/** {@link ConfigurationItem} for the {@link TLSyncReceiverApplicationMonitor}. */
	public interface Config extends ConfiguredMonitorComponent.Config {

		@Override
		@StringDefault("TL-Sync receiver")
		String getName();

		@Override
		@StringDefault("Whether the last receive operation of TL-Sync succeeded.")
		String getDescription();

		/** The name of the {@link ConsumerDispatcher} which is used by TL-Sync for receiving. */
		@Mandatory
		String getReceiverName();

		/** How long a receive operation can run, before it is considered to be "too long". */
		@LongDefault(10 * 60 * 1000)
		@Format(MillisFormat.class)
		long getMaxProcessingTime();

	}

	/** {@link TypedConfiguration} constructor for {@link TLSyncReceiverApplicationMonitor}. */
	public TLSyncReceiverApplicationMonitor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void checkStateInTryCatch(MonitorResult result) {
		if (!KafkaConsumerService.Module.INSTANCE.isActive()) {
			String message = "The Kafka receiver is not started. Receiving messages is therefore not possible for TL-Sync.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		KafkaConsumerService receiver = KafkaConsumerService.getInstance();
		if (!receiver.isStarted()) {
			String message = "The Kafka receiver is still starting or was shut down. Receiving messages is therefore not possible for TL-Sync.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		ConsumerDispatcher<?, ?> consumer = receiver.getConsumer(getReceiverName());
		Instant lastRunEndTimestamp = consumer.getLastRunEndTimestamp();
		if (lastRunEndTimestamp == null) {
			String message = "The TL-Sync receiver was not started yet. Receiving messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		if (isTooLongAgo(lastRunEndTimestamp)) {
			String maxTime = TimeUtil.formatMillisAsTime(getMaxProcessingTime());
			String message = "The last run finished too long ago: " + lastRunEndTimestamp
				+ ". It should not be more than a few minutes ago (" + maxTime
				+ "). Receiving messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		Throwable lastRunError = consumer.getLastRunError();
		if (lastRunError != null) {
			String message = "Failed. Cause: " + lastRunError.getMessage();
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		String message = "Working properly.";
		result.addMessage(createMessage(Status.INFO, message));
	}

	/** @see Config#getReceiverName() */
	protected String getReceiverName() {
		return getConfig().getReceiverName();
	}

	private boolean isTooLongAgo(Instant lastRunEndTimestamp) {
		Duration timePassed = Duration.between(lastRunEndTimestamp, Instant.now());
		return timePassed.toMillis() > getMaxProcessingTime();
	}

	private long getMaxProcessingTime() {
		return getConfig().getMaxProcessingTime();
	}

}
