/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Parsed object contains all informations sent by the client to identify the requested command.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandRequest {

	/** Constant to indicate that no submit number is sent by the client */
	private static final int NO_SUBMIT_NUMBER = -1;

	/**
	 * @see #getContextComponentID()
	 */
	ComponentName sourceComponentID;

	/**
	 * @see #getTargetComponentID()
	 */
	ComponentName targetComponentID;

	/**
	 * @see #getCommand()
	 */
	String command;

	/**
	 * @see #getArguments()
	 */
	HashMap<String, Object> arguments = new HashMap<>();

	/**
	 * The submit number of the component that issued the request (at the time of the creation of the
	 * request).
	 * 
	 * @see LayoutComponent#getSubmitNumber()
	 */
	Integer submitNumber;

	CommandRequest() {
		super();
	}

	/**
	 * An arbitrary set of arguments for the command (mapping of argument names
	 * to argument values).
	 * 
	 * <p>
	 * The value associated with an parameter name might be any type supported
	 * by the {@link XMLValueDecoder}. In contrast to regular commands, which
	 * receive their arguments through
	 * {@link jakarta.servlet.ServletRequest#getParameterValues(String)}
	 * and are restricted to arguments of type <code>String[]</code>,
	 * AJAX commands may receive complex values as arguments.
	 * </p>
	 */
	public Map<String, Object> getArguments() {
		return arguments;
	}

	/**
	 * The name of the command that should be executed on the target component.
	 * An AJAX command is expected to be
	 * {@link com.top_logic.mig.html.layout.LayoutComponent#registerCommand(CommandHandler) registered}
	 * under that name at the target component. 
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * The {@link LayoutComponent#getName() identifier} of the component that sent the request.
	 * 
	 * <p>
	 * The context component is the one that is will process the answer to the current request. For
	 * composing the answer to a request, it may be necessary to know the context, in which the
	 * answer will be processed. Knowing the source component allows computing relative
	 * (client-side) references to other components and include them in the answer message.
	 * </p>
	 * 
	 * @return The identifier of the component that sent the request. May not be <code>null</code>.
	 */
	public ComponentName getContextComponentID() {
		return sourceComponentID;
	}

	/**
	 * The {@link LayoutComponent#getName() identifier} of the target component,
	 * to which this message is directed to.
	 * 
	 * <p>
	 * An {@link AJAXRequest} is not required to have a target component. In
	 * that case, the {@link #getTargetComponentID() target component identifier} is
	 * <code>null</code>, and the corresponding AJAX command is
	 * processed "statically" (not within the context of a component). Only
	 * commands that are
	 * {@link AJAXServlet#registerStaticCommand(CommandHandler) directly registered}
	 * with the {@link AJAXServlet} can be processed statically.
	 * </p>
	 * 
	 * @return The identifier of the target component, or <code>null</code>,
	 *         if this is a "static" command (see above).
	 */
	public ComponentName getTargetComponentID() {
		return targetComponentID;
	}

	/**
	 * Whether the requested command is a static command
	 */
	public boolean isStatic() {
		return targetComponentID == null;
	}

	/**
	 * The {@link LayoutComponent#getSubmitNumber() submit number} of the component that issued this
	 * request.
	 * 
	 * @return The submit number or <code>-1</code>, if no submit number was passed by the client.
	 */
	public int getSubmitNumber() {
		return this.submitNumber != null ? this.submitNumber.intValue() : NO_SUBMIT_NUMBER;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RequestPart [sourceComponentID=");
		builder.append(sourceComponentID);
		builder.append(", targetComponentID=");
		builder.append(targetComponentID);
		builder.append(", command=");
		builder.append(command);
		builder.append(", arguments=");
		builder.append(arguments);
		builder.append(", submitNumber=");
		builder.append(submitNumber);
		builder.append(']');
		return builder.toString();
	}

}
