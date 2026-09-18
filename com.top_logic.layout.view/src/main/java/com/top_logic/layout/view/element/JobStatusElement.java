/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.JobDisplay;
import com.top_logic.layout.react.control.common.ReactJobStatusControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.job.JobPhase;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;
import com.top_logic.layout.view.job.PhaseStatus;

/**
 * {@link UIElement} displaying the long-running job its {@link Config#getInput() input} channel
 * reports about, via the {@link ReactJobStatusControl}.
 *
 * <p>
 * The channel holds the {@link JobState} snapshots the job publishes; a channel holding anything
 * else - nothing at all before a job was started - displays no job. Every text is resolved for the
 * reader here: the phases and the message by their {@link ResKey}, what the job produced through
 * the {@link MetaLabelProvider}, which is how any other value of a channel becomes a text.
 * </p>
 *
 * <p>
 * The display follows the channel alone. A snapshot is immutable, so a job that has something new
 * to report publishes a new one, and no object the display would have to observe changes.
 * </p>
 */
@InApp
public class JobStatusElement implements UIElement {

	/**
	 * Configuration for {@link JobStatusElement}.
	 */
	@TagName("job-status")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(JobStatusElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel the job reports on, holding the state of the job to display.
		 *
		 * <p>
		 * This is the channel a started job publishes its snapshots to. A channel holding no job
		 * displays nothing, so a view offering to start a job needs no case of its own for the time
		 * before the first start.
		 * </p>
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	/**
	 * Creates a new {@link JobStatusElement} from configuration.
	 */
	@CalledByReflection
	public JobStatusElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
	}

	/**
	 * Creates a {@link JobStatusElement} displaying the job of the given channel.
	 *
	 * @param input
	 *        The channel the job reports on.
	 */
	public JobStatusElement(ChannelRef input) {
		_inputRef = input;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = context.resolveChannel(_inputRef);
		ReactJobStatusControl control = new ReactJobStatusControl(context, display(channel.get()));

		ChannelListener listener = (sender, oldValue, newValue) -> control.setJob(display(newValue));
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));

		return control;
	}

	/**
	 * What the control displays for the given channel value.
	 *
	 * @param value
	 *        The value of the input channel.
	 * @return The description of the job, or {@code null} for a value that is no job state, which
	 *         displays nothing.
	 */
	public static JobDisplay display(Object value) {
		if (!(value instanceof JobState state)) {
			return null;
		}
		List<JobPhase> phases = state.phases();
		List<JobDisplay.Phase> displayed = new ArrayList<>(phases.size());
		for (int n = 0, size = phases.size(); n < size; n++) {
			displayed.add(new JobDisplay.Phase(ValueLabel.label(phases.get(n).label()),
				phaseState(state.phaseStatus(n))));
		}
		return new JobDisplay(status(state.status()), displayed, state.fraction(),
			ValueLabel.label(state.message()), state.startedAt(), state.finishedAt(),
			ValueLabel.label(state.result()), ValueLabel.label(state.error()),
			state.isCancelable() ? state.control()::cancel : null);
	}

	/**
	 * The given job status as the display names it.
	 */
	private static JobDisplay.Status status(JobStatus status) {
		return switch (status) {
			case RUNNING -> JobDisplay.Status.RUNNING;
			case COMPLETED -> JobDisplay.Status.COMPLETED;
			case FAILED -> JobDisplay.Status.FAILED;
			case CANCELLED -> JobDisplay.Status.CANCELLED;
		};
	}

	/**
	 * The given phase status as the display names it.
	 */
	private static JobDisplay.PhaseState phaseState(PhaseStatus status) {
		return switch (status) {
			case DONE -> JobDisplay.PhaseState.DONE;
			case ACTIVE -> JobDisplay.PhaseState.ACTIVE;
			case PENDING -> JobDisplay.PhaseState.PENDING;
		};
	}

}
