/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ViewAction} that hands its work to a worker thread and lets the command wait for it.
 *
 * <p>
 * Work that takes longer than a request may take does not belong in the request: the action starts
 * it, publishes what it reports as a {@link JobState} on the configured channel, and suspends the
 * command until the work has ended. A display bound to that channel follows the job while it runs -
 * the step it is in, how far it has come, what it is doing right now - and may ask it to stop.
 * </p>
 *
 * <p>
 * The command continues where it left off once the job has completed, with the job's result as its
 * value; a job that fails or is cancelled aborts the command, so the compensations of the actions
 * before it run and the failure stays visible in the last state of the job. The work runs outside
 * any transaction: persisting its result is the business of the actions that follow it.
 * </p>
 *
 * <pre>
 * &lt;start-job job="importState" cancelable="true"&gt;
 *   &lt;phases&gt;
 *     &lt;phase name="read"&gt;&lt;label&gt;&lt;en&gt;Reading&lt;/en&gt;&lt;/label&gt;&lt;/phase&gt;
 *     &lt;phase name="check"&gt;&lt;label&gt;&lt;en&gt;Checking&lt;/en&gt;&lt;/label&gt;&lt;/phase&gt;
 *   &lt;/phases&gt;
 *   &lt;body class="com.acme.ImportJob"/&gt;
 * &lt;/start-job&gt;
 * </pre>
 */
@InApp
public class StartJobAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link StartJobAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<StartJobAction>, Inputs {

		/** Configuration tag of a {@link StartJobAction}. */
		String TAG_NAME = "start-job";

		/** Configuration name for {@link #getJob()}. */
		String JOB = "job";

		/** Configuration name for {@link #isCancelable()}. */
		String CANCELABLE = "cancelable";

		/** Configuration name for {@link #getFunction()}. */
		String FUNCTION = "function";

		/** Configuration name for {@link #getBody()}. */
		String BODY = "body";

		/** Configuration name for {@link #getPhases()}. */
		String PHASES = "phases";

		/** Configuration name for {@link #getUpdateInterval()}. */
		String UPDATE_INTERVAL = "update-interval";

		@Override
		@ClassDefault(StartJobAction.class)
		Class<? extends StartJobAction> getImplementationClass();

		/**
		 * The channel the job publishes its state on.
		 *
		 * <p>
		 * It holds a {@link JobState} from the moment the job starts, and a new one on every report
		 * of the job. Whatever displays the job reads it from there.
		 * </p>
		 */
		@Name(JOB)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getJob();

		/**
		 * Whether the reader may ask this job to stop.
		 *
		 * <p>
		 * Only for work that may be given up half-done: the job is asked to stop and ends at the
		 * next point it reports from, having done part of what it was started for.
		 * </p>
		 */
		@Name(CANCELABLE)
		boolean isCancelable();

		/**
		 * TL-Script function doing the work of the job.
		 *
		 * <p>
		 * Called with the monitor of the job as its first argument, followed by the values of the
		 * input channels in declaration order and the command's current value as the last argument.
		 * </p>
		 *
		 * <p>
		 * Exactly one of this and a body of its own states what the job does.
		 * </p>
		 */
		@Name(FUNCTION)
		@Nullable
		Expr getFunction();

		/**
		 * The work of the job, written in Java.
		 *
		 * <p>
		 * The alternative to a TL-Script function for work that is not expressed as an expression.
		 * Exactly one of this and a function states what the job does.
		 * </p>
		 */
		@Name(BODY)
		@Nullable
		PolymorphicConfiguration<? extends JobBody> getBody();

		/**
		 * The steps the job goes through, in order.
		 *
		 * <p>
		 * Declared here so the display knows the whole way before the job has walked it. The job
		 * marks the step it enters by its name; a job that does not name its steps declares none.
		 * </p>
		 */
		@Name(PHASES)
		@Key(PhaseConfig.NAME)
		List<PhaseConfig> getPhases();

		/**
		 * The shortest time in milliseconds between two states published by a job.
		 *
		 * <p>
		 * A job reporting faster than this is followed at this pace, so that a job counting
		 * thousands of items does not flood the browser with updates.
		 * </p>
		 */
		@Name(UPDATE_INTERVAL)
		@LongDefault(250)
		long getUpdateInterval();
	}

	/**
	 * Configuration of one step a job announces it goes through.
	 */
	@TagName(PhaseConfig.TAG_NAME)
	public interface PhaseConfig extends ConfigurationItem {

		/** Configuration tag of a phase. */
		String TAG_NAME = "phase";

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The identifier the job names this step by; unique among the steps of one job.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * What the reader sees for this step; its name where none is given.
		 */
		@Name(LABEL)
		ResKey getLabel();
	}

	private final ChannelRef _job;

	private final List<ChannelRef> _inputs;

	private final List<JobPhase> _phases;

	private final boolean _cancelable;

	private final long _updateInterval;

	private final JobBody _body;

	/**
	 * Names the operation a failure is reported for: the command taken up again after the job has
	 * ended.
	 */
	private static final String SETTLE_DESCRIPTION = "Continuing the command after the job";

	/**
	 * Creates a {@link StartJobAction} from configuration.
	 */
	@CalledByReflection
	public StartJobAction(InstantiationContext context, Config config) {
		_job = config.getJob();
		_inputs = config.getInputs();
		_phases = phases(config.getPhases());
		_cancelable = config.isCancelable();
		_updateInterval = config.getUpdateInterval();
		_body = body(context, config);
	}

	/**
	 * Creates a {@link StartJobAction}.
	 *
	 * @param job
	 *        The channel the job publishes its state on.
	 * @param inputs
	 *        The channels whose values are handed to the job ahead of the command's value.
	 * @param phases
	 *        The steps the job announces it goes through; empty for a job that does not name them.
	 * @param cancelable
	 *        Whether the reader may ask the job to stop.
	 * @param updateInterval
	 *        The shortest time in milliseconds between two published states.
	 * @param body
	 *        The work of the job.
	 */
	public StartJobAction(ChannelRef job, List<ChannelRef> inputs, List<JobPhase> phases, boolean cancelable,
			long updateInterval, JobBody body) {
		_job = job;
		_inputs = inputs;
		_phases = List.copyOf(phases);
		_cancelable = cancelable;
		_updateInterval = updateInterval;
		_body = body;
	}

	private static JobBody body(InstantiationContext context, Config config) {
		Expr function = config.getFunction();
		PolymorphicConfiguration<? extends JobBody> body = config.getBody();
		if (function != null) {
			if (body != null) {
				context.error("A <" + Config.TAG_NAME + "> states its work either as '" + Config.FUNCTION
					+ "' or as '" + Config.BODY + "', not both.");
			}
			return new ScriptJobBody(function);
		}
		if (body == null) {
			context.error("A <" + Config.TAG_NAME + "> requires either '" + Config.FUNCTION + "' or '"
				+ Config.BODY + "' to state its work.");
			return null;
		}
		return context.getInstance(body);
	}

	private static List<JobPhase> phases(List<PhaseConfig> configs) {
		List<JobPhase> result = new ArrayList<>(configs.size());
		for (PhaseConfig phase : configs) {
			ResKey label = phase.getLabel();
			result.add(label == null ? JobPhase.named(phase.getName()) : new JobPhase(phase.getName(), label));
		}
		return result;
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		ViewContext viewContext = (ViewContext) context;
		ViewChannel target = viewContext.resolveChannel(_job);
		List<ViewChannel> inputs = ChannelInputs.resolve(viewContext, _inputs);
		List<Object> arguments =
			Collections.unmodifiableList(Arrays.asList(ChannelInputs.arguments(inputs, input)));

		JobRunner runner = new JobRunner(context, target, _phases, _cancelable, _updateInterval,
			state -> settle(context, state, continuation));
		runner.start(_body, arguments);
	}

	/**
	 * Takes the command up again where the job left it: a completed job hands its result on, a
	 * failed or cancelled one ends the command.
	 *
	 * <p>
	 * What the rest of the command throws is reported the way a failed command reports it - an
	 * internal error logged as one and shown generically, a user-level failure carrying its own
	 * message into the window's snackbar - because nothing else answers for it: the job has already
	 * ended, and the thread taking the command up serves no request.
	 * </p>
	 */
	private static void settle(ReactContext context, JobState state, Continuation continuation) {
		try {
			if (state.status() == JobStatus.COMPLETED) {
				continuation.resume(state.result());
			} else {
				continuation.abort();
			}
		} catch (Throwable ex) {
			HandlerResult failure = CommandErrors.failure(ex, SETTLE_DESCRIPTION, StartJobAction.class);
			CommandErrors.show(context.getErrorSink(), failure);
		}
	}

}
