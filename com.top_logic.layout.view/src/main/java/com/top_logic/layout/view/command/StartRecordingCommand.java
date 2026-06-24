/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.headless.ScriptRecorder;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Starts a fresh recording on the recorder of the window that opened this one — the command behind a
 * recorder side-window's start button.
 *
 * @see RecorderAccess
 */
public class StartRecordingCommand implements ViewCommand {

	/**
	 * Configuration for {@link StartRecordingCommand}.
	 */
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(StartRecordingCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	/**
	 * Creates a new {@link StartRecordingCommand}.
	 */
	@CalledByReflection
	public StartRecordingCommand(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		ErrorSink errorSink = context.getErrorSink();
		Resources resources = Resources.getInstance();
		ScriptRecorder recorder = RecorderAccess.openerRecorder(context);
		if (recorder == null) {
			if (errorSink != null) {
				errorSink.showError(Fragments.text(resources.getString(I18NConstants.ERROR_NO_RECORDER)));
			}
			return HandlerResult.DEFAULT_RESULT;
		}
		recorder.start();
		if (errorSink != null) {
			errorSink.showInfo(Fragments.text(resources.getString(I18NConstants.RECORDER_STARTED)));
		}
		return HandlerResult.DEFAULT_RESULT;
	}
}
