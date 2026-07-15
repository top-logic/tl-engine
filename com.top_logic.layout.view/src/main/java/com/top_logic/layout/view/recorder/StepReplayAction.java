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
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.headless.ReactWindowReplay;
import com.top_logic.layout.react.headless.ScriptRecorder;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link ViewAction} replaying the currently selected recorded step on the opener window — the "step"
 * button of the recorder side-window's step debugger.
 *
 * <p>
 * Its input is the selected step's 1-based row key (written by the steps table to a {@code selection}
 * channel). It replays that step on the recorded (opener) window through {@link ReactWindowReplay}, so
 * the effect appears in the main browser window, and returns the <em>next</em> step's row key so the
 * chained {@code <write-channel>} advances the selection. When the last step has been replayed the
 * selection is left unchanged. A step that fails to replay (e.g. its address does not resolve in the
 * opener window's current state) reports the failure through the window's {@link ErrorSink} and keeps
 * the selection on the failed step, so the debugger stops where the script does.
 * </p>
 */
public class StepReplayAction implements ViewAction {

	/**
	 * Configuration for {@link StepReplayAction}.
	 *
	 * <p>
	 * App-specific action, referenced by {@code class=} in the recorder view rather than claiming a
	 * global {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<StepReplayAction> {

		@Override
		@ClassDefault(StepReplayAction.class)
		Class<? extends StepReplayAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link StepReplayAction} from configuration.
	 */
	@CalledByReflection
	public StepReplayAction(InstantiationContext context, Config config) {
		// No configuration.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		ScriptRecorder recorder = RecorderAccess.openerRecorder(context);
		String openerWindowId = RecorderAccess.openerWindowId(context);
		if (recorder == null || openerWindowId == null) {
			info(context, I18NConstants.ERROR_NO_RECORDER);
			return input;
		}

		List<ReactCommand> steps = recorder.steps();
		int index = selectedIndex(input, steps.size());
		if (index < 0) {
			info(context, I18NConstants.SELECT_STEP_TO_REPLAY);
			return input;
		}

		ReactCommand step = steps.get(index);
		ReactWindowRegistry registry = context.getWindowRegistry();
		try {
			HandlerResult result = ReactWindowReplay.act(registry, openerWindowId, step);
			if (!result.isSuccess()) {
				error(context, I18NConstants.ERROR_REPLAY_FAILED__MSG.fill(String.valueOf(step.getAddress())));
				return input;
			}
		} catch (RuntimeException ex) {
			error(context, I18NConstants.ERROR_REPLAY_FAILED__MSG.fill(String.valueOf(ex.getMessage())));
			return input;
		}

		// Advance to the next step's 1-based row key, or keep the current selection past the end.
		// A failed step (above) keeps the selection on that step instead, so the debugger stops
		// where the script does.
		int next = index + 1;
		return next < steps.size() ? rowKey(next) : input;
	}

	/**
	 * The 0-based step index designated by the selection input (a 1-based {@link #rowKey(int) row
	 * key}), or {@code -1} if nothing valid is selected.
	 */
	private static int selectedIndex(Object input, int stepCount) {
		if (input == null) {
			return -1;
		}
		try {
			int index = Integer.parseInt(input.toString()) - 1;
			return index >= 0 && index < stepCount ? index : -1;
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	/**
	 * The table row key (1-based step number as a string) for a 0-based step index, matching the key
	 * function of {@link RecordedStepsTable}.
	 */
	private static String rowKey(int index) {
		return Integer.toString(index + 1);
	}

	/**
	 * Shows an informational message in the side-window through the React {@link ErrorSink}, a no-op
	 * when the context has none.
	 */
	private static void info(ReactContext context, ResKey message) {
		ErrorSink errorSink = context.getErrorSink();
		if (errorSink != null) {
			errorSink.showInfo(Fragments.text(Resources.getInstance().getString(message)));
		}
	}

	/**
	 * Shows an error message in the side-window through the React {@link ErrorSink}, a no-op when
	 * the context has none.
	 */
	private static void error(ReactContext context, ResKey message) {
		ErrorSink errorSink = context.getErrorSink();
		if (errorSink != null) {
			errorSink.showError(Fragments.text(Resources.getInstance().getString(message)));
		}
	}
}
