/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.basic.component.ControlComponent.DispatchAction;
import com.top_logic.layout.form.tag.js.JSBoolean;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.form.tag.js.JSString;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ControlCommand} is sent to a {@link com.top_logic.layout.Control} of a
 * {@link ControlComponent} instead of a component as a whole.
 * 
 * <p>
 * {@link ControlCommand}s are used to react on user interactions with active
 * elements on a page.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ControlCommand {

	/**
	 * Parameter name that identifies the {@link com.top_logic.layout.Control} within a
	 * {@link ControlComponent} to which this {@link ControlCommand} is sent.
	 */
	public static final String CONTROL_ID_PARAM = "controlID";

	private final String _commandId;

	public ControlCommand(String aCommand) {
		_commandId = aCommand;
	}

	public final String getID() {
		return _commandId;
	}

	/**
	 * Return the I18N key used for translation of buttons (when available)
	 * 
	 * @return The default I18N key
	 * 
	 * @deprecated Use {@link #getI18NKey()} instead.
	 */
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return getI18NKey();
	}

	/**
	 * Return the I18N key used for translation of buttons / commands.
	 * 
	 * @return The I18N key. Not <code>null</code>.
	 */
	public abstract ResKey getI18NKey();

	/**
	 * Second way to execute a {@link ControlCommand} using the {@link ControlComponent}'s command
	 * dispatch.
	 */
	protected abstract HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments);

	public final void writeInvokeExpression(Appendable out, Control control) throws IOException {
		writeInvokeExpression(out, control, new JSObject());
	}

	public final void writeInvokeExpression(Appendable out, Control control, JSObject argumentObject)
			throws IOException {
		argumentObject.addProperty(CONTROL_ID_PARAM, new JSString(control.getID()));
		if (isSystemCommand()) {
			argumentObject.addProperty(AJAXCommandHandler.SYSTEM_COMMAND_ID, new JSBoolean(true));
		}
		appendInvokeExpression(out, argumentObject);
	}

	/**
	 * true, if this is a system command and therefore no external model update events
	 *         (which may have occurred in other sessions) shall be processed at call time, false
	 *         otherwise.
	 */
	protected boolean isSystemCommand() {
		return false;
	}

	protected void appendInvokeExpression(Appendable out, JSObject argumentObject) throws IOException {
		// Make sure that this command invokation can be routed to the target
		// control at the server side. To ensure thia, a command invokation
		// needs the control id as additional argument.
		assert argumentObject.hasProperty(CONTROL_ID_PARAM);
		
		// Add the name of this command to the argumentObject and actually invoke
		// the DispatchAction on the ControlComponent (instead of this command)
		// to route the command invokation to the target control.
		argumentObject.addProperty(DispatchAction.CONTROL_COMMAND_PARAM, new JSString(getID()));

		AJAXCommandHandler.appendInvokeExpression(out, DispatchAction.COMMAND_NAME, argumentObject);
	}
	
	/**
	 * Whether the command should be executed even if the view of the control is currently disabled.
	 * This should not be the case for commands which are triggered by some direct user action (such
	 * as filling a field or pushing a button) but <code>true</code> for commands which are
	 * technical system commands.
	 */
	protected boolean executeCommandIfViewDisabled() {
		return false;
	}

	/**
	 * Returns the {@link CommandModel} which executes this {@link ControlCommand}.
	 * 
	 * <p>
	 * This method must only be called during
	 * {@link ControlCommand#execute(DisplayContext, Control, Map)}. Otherwise the result is
	 * undefined.
	 * </p>
	 * 
	 * @param someArguments
	 *        Arguments of the {@link ControlCommand} in
	 *        {@link ControlCommand#execute(DisplayContext, Control, Map)}.
	 * 
	 * @return The {@link CommandModel} executing this command.
	 */
	protected CommandModel getCommandModel(Map<String, Object> someArguments) {
		return (CommandModel) someArguments.get(ControlCommandModel.COMMAND_MODEL_KEY);
	}

}