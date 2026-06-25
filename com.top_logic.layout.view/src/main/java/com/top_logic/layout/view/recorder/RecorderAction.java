/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.recorder;

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
import com.top_logic.layout.react.headless.ScriptRecorder;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.util.Resources;

/**
 * {@link ViewAction} starting and stopping the {@link ScriptRecorder} of the window that opened this
 * side-window.
 *
 * <p>
 * The start/stop chain flips a boolean {@code recording} channel via the reusable
 * {@code <execute-script>} + {@code <write-channel>} actions (driving the start/stop button
 * visibility). The captured steps themselves are shown live by {@link RecordedStepsTable}, which
 * listens to the recorder directly. The recorder lives on the opener window's queue (see
 * {@link RecorderAccess}), so this side-window's own commands are never captured.
 * </p>
 */
public class RecorderAction implements ViewAction {

	/**
	 * What a {@link RecorderAction} does to the recording state.
	 */
	public enum Mode {
		/** Begin a fresh recording, discarding previous steps. */
		START,

		/** Stop recording. The captured steps remain shown until the next start. */
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
			return input;
		}

		switch (_mode) {
			case START:
				recorder.start();
				info(context, I18NConstants.RECORDER_STARTED);
				break;
			case STOP:
				recorder.stop();
				info(context, I18NConstants.RECORDER_STOPPED__COUNT.fill(Integer.valueOf(recorder.steps().size())));
				break;
		}
		return input;
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
