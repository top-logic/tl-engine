/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * Container to describe processing information in a {@link DisplayContext}
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class ProcessingInfo {

	private String commandID;

	private ResKey commandName;

	private ComponentName componentName;

	private String jspName;

	private ProcessingKind kind;

	/**
	 * Create a new DefaultProcessingInfo. All parameters are optional.
	 * 
	 * @param aCommandName
	 *        a command name
	 * @param aCommandID
	 *        a command id
	 * @param componentName
	 *        a component name
	 * @param aJSPName
	 *        a jsp name
	 * @param aKind
	 *        a processing kind
	 */
	public ProcessingInfo(ResKey aCommandName, String aCommandID, ComponentName componentName, String aJSPName,
			ProcessingKind aKind) {
		assert aCommandName != null : "Command name must not be null!";
		this.commandName = aCommandName;
		this.commandID = aCommandID;
		this.componentName = componentName;
		this.jspName = aJSPName;
		this.kind = aKind;
	}

	/**
	 * Create a new DefaultProcessingInfo with empty values
	 */
	public ProcessingInfo() {
		this(I18NConstants.UNKNOWN_COMMAND, null, null, null, null);
	}

	/**
	 * the command id. May be <code>null</code>.
	 */
	public String getCommandID() {
		return commandID;
	}

	/**
	 * the command name. Never <code>null</code>.
	 */
	public ResKey getCommandName() {
		return commandName;
	}

	/**
	 * the component name. May be <code>null</code>.
	 */
	public ComponentName getComponentName() {
		return componentName;
	}

	/**
	 * the JSP name. May be <code>null</code>.
	 */
	public String getJSPName() {
		return jspName;
	}

	/**
	 * the processing kind. May not <code>null</code>.
	 */
	public ProcessingKind getProcessingKind() {
		return kind;
	}

	/**
	 * @param commandID
	 *        The commandID to set.
	 */
	public void setCommandID(String commandID) {
		this.commandID = commandID;
	}

	/**
	 * @param commandName
	 *        The commandName to set, <b>must not</b> be <code>null</code>.
	 */
	public void setCommandName(ResKey commandName) {
		assert commandName != null : "Command name must not be null!";
		this.commandName = commandName;
	}

	/**
	 * @param componentName
	 *        The componentName to set.
	 */
	public void setComponentName(ComponentName componentName) {
		this.componentName = componentName;
	}

	/**
	 * @param jspName
	 *        The jspName to set.
	 */
	public void setJSPName(String jspName) {
		this.jspName = jspName;
	}

	/**
	 * @param kind
	 *        The kind to set.
	 */
	public void setProcessingKind(ProcessingKind kind) {
		this.kind = kind;
	}
}
