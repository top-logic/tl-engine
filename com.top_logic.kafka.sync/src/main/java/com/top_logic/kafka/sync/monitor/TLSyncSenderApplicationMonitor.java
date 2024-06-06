/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.monitor;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
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
import com.top_logic.util.monitor.ConfiguredMonitorComponent;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage.Status;
import com.top_logic.util.monitor.MonitorResult;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * The {@link MonitorComponent} for TL-Sync for sending.
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
public class TLSyncSenderApplicationMonitor
		extends ConfiguredMonitorComponent<TLSyncSenderApplicationMonitor.Config> {

	/** {@link ConfigurationItem} for the {@link TLSyncSenderApplicationMonitor}. */
	public interface Config extends ConfiguredMonitorComponent.Config {

		@Override
		@StringDefault("TL-Sync sender")
		String getName();

		@Override
		@StringDefault("Whether the last send operation of TL-Sync succeeded.")
		String getDescription();

		/** The name of the {@link Task} which is used by TL-Sync for sending. */
		@Mandatory
		String getSenderName();

		/** How long a send operation can run, before it is considered to be "too long". */
		@LongDefault(10 * 60 * 1000)
		@Format(MillisFormat.class)
		long getMaxProcessingTime();

	}

	/** {@link TypedConfiguration} constructor for {@link TLSyncSenderApplicationMonitor}. */
	public TLSyncSenderApplicationMonitor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void checkStateInTryCatch(MonitorResult result) {
		if (!Scheduler.Module.INSTANCE.isActive()) {
			String message = "The scheduler is not started. Sending messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		Scheduler scheduler = Scheduler.getSchedulerInstance();
		if (!scheduler.isStarted()) {
			String message = "The scheduler is still starting or was shut down. Sending messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		Maybe<Task> sendingTask = getSendingTask(scheduler);
		if (!sendingTask.hasValue()) {
			String message = "The task for sending messages was not created yet. Sending messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		TaskLog taskLog = sendingTask.get().getLog();
		/* Don't use the current result, as it may seem okay until it crashes. The last completed
		 * result is better for detecting problems. */
		TaskResult lastResult = taskLog.getLastResult();
		if (lastResult == null) {
			String message = "The task for sending messages was not started yet. Sending messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		if (isTooLongAgo(lastResult)) {
			String maxTime = TimeUtil.formatMillisAsTime(getMaxProcessingTime());
			String message = "The last run finished too long ago: " + lastResult.getEndDate()
				+ ". It should not be more than a few minutes ago (" + maxTime
				+ "). Sending messages is therefore not possible.";
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		ResultType resultType = lastResult.getResultType();
		if (resultType != ResultType.SUCCESS) {
			String cause = getFirstExceptionLine(lastResult);
			String message = "Did not complete normally. Result type: " + resultType + " Cause: " + cause;
			result.addMessage(createMessage(Status.ERROR, message));
			return;
		}
		String message = "Working properly.";
		result.addMessage(createMessage(Status.INFO, message));
	}

	/** The {@link Task} which is used to send TL-Sync messages */
	protected Maybe<Task> getSendingTask(Scheduler scheduler) {
		return scheduler.getTask(getSenderName());
	}

	/** @see Config#getSenderName() */
	protected String getSenderName() {
		return getConfig().getSenderName();
	}

	private boolean isTooLongAgo(TaskResult lastResult) {
		long millisecondsAgo = System.currentTimeMillis() - lastResult.getEndDate().getTime();
		return millisecondsAgo > getMaxProcessingTime();
	}

	private long getMaxProcessingTime() {
		return getConfig().getMaxProcessingTime();
	}

	private String getFirstExceptionLine(TaskResult result) {
		if (StringServices.isEmpty(result.getExceptionDump())) {
			return "";
		}
		return result.getExceptionDump().substring(0, result.getExceptionDump().indexOf('\n')).trim();
	}

}
