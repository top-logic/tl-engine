/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.scripting.runtime.execution.ScriptDriver;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command to resume the execution of a scripted test.
 * 
 * <p>
 * Uses the {@link ScriptDriver} set on {@link ScriptRecorderTree#SCRIPT_DRIVER} in
 * {@link ScriptRecorderTree} to continue the scripted test execution. If no script driver is set
 * then this command is a noop.
 * </p>
 * 
 * @see ScriptRecorderTree#SCRIPT_DRIVER
 * @see ActionTreeRenderer
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ResumeScriptExecutionCommand extends ControlCommand {

	/**
	 * Command name for the {@link ResumeScriptExecutionCommand}.
	 */
	public static final String COMMAND_NAME = "resumeScriptExecution";

	/**
	 * Singleton instance.
	 */
	public static final ResumeScriptExecutionCommand INSTANCE = new ResumeScriptExecutionCommand();

	private ResumeScriptExecutionCommand() {
		super(COMMAND_NAME);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.RESUME_SCRIPT_EXECUTION;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		ScriptDriver scriptDriver = getScriptDriver((ScriptRecorderTreeControl) control);

		if (scriptDriver != null) {
			scriptDriver.next(commandContext);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private ScriptDriver getScriptDriver(ScriptRecorderTreeControl control) {
		return control.getScriptRecorderTree().get(ScriptRecorderTree.SCRIPT_DRIVER);
	}

}
