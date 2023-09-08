/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.runtime.action.RecordingFailedActionOp;

/**
 * This {@link ApplicationAction} is recorded, when the recording of the actual
 * {@link ApplicationAction} fails. Useful for printing a warning to the tester.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface RecordingFailedAction extends ApplicationAction {

	@Override
	@ClassDefault(RecordingFailedActionOp.class)
	Class<RecordingFailedActionOp> getImplementationClass();

	/**
	 * The {@link Throwable} that caused the recording to fail.
	 */
	public String getPrintedCause();

	/** @see #getPrintedCause() */
	public void setPrintedCause(String printedCause);

	/**
	 * The message of the {@link ScriptingRecorder}, what it tried to record when the error
	 * occurred.
	 */
	public String getMessage();

	/** @see #getMessage() */
	public void setMessage(String message);

}
