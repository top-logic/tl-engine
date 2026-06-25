/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.recorder;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.headless.RecordedStep;
import com.top_logic.layout.react.headless.ScriptRecorder;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.util.Resources;

/**
 * {@link ViewAction} controlling the {@link ScriptRecorder} of the window that opened this side-window
 * and returning its captured {@link RecordedStep steps} for display.
 *
 * <p>
 * Modelled on the SQL monitor: the returned step list is meant to be written to a channel feeding a
 * {@link RecordedStepsTable}, and the start/stop chain flips a boolean {@code recording} channel via
 * the reusable {@code <execute-script>} + {@code <write-channel>} actions. The recorder lives on the
 * opener window's queue (see {@link RecorderAccess}), so this side-window's own commands are never
 * captured.
 * </p>
 */
public class RecorderAction implements ViewAction {

	/**
	 * What a {@link RecorderAction} does to the recording state.
	 */
	public enum Mode {
		/** Begin a fresh recording (discarding previous steps); returns the now-empty step list. */
		START,

		/** Return a snapshot of the captured steps, leaving recording running. */
		REFRESH,

		/** Stop recording and return the captured steps. */
		STOP;
	}

	/**
	 * Configuration for {@link RecorderAction}.
	 *
	 * <p>
	 * App-specific action, referenced by {@code class=} in the recorder view rather than claiming a
	 * global {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<RecorderAction> {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		@Override
		@ClassDefault(RecorderAction.class)
		Class<? extends RecorderAction> getImplementationClass();

		/**
		 * What the action does to the recording state.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();
	}

	private final Mode _mode;

	/**
	 * Creates a new {@link RecorderAction} from configuration.
	 */
	@CalledByReflection
	public RecorderAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		ScriptRecorder recorder = RecorderAccess.openerRecorder(context);
		if (recorder == null) {
			info(context, I18NConstants.ERROR_NO_RECORDER);
			return List.of();
		}

		switch (_mode) {
			case START:
				recorder.start();
				info(context, I18NConstants.RECORDER_STARTED);
				return recorder.steps();
			case REFRESH:
				return recorder.steps();
			case STOP:
				recorder.stop();
				List<RecordedStep> steps = recorder.steps();
				info(context, I18NConstants.RECORDER_STOPPED__COUNT.fill(Integer.valueOf(steps.size())));
				return steps;
		}
		return List.of();
	}

	/**
	 * Shows an informational message in the side-window through the React {@link ErrorSink} (the
	 * view-layer info channel), a no-op when the context has none.
	 */
	private static void info(ReactContext context, ResKey message) {
		ErrorSink errorSink = context.getErrorSink();
		if (errorSink != null) {
			errorSink.showInfo(Fragments.text(Resources.getInstance().getString(message)));
		}
	}
}
