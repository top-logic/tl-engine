/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * Container to describe processing information in a {@link DisplayContext}.
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
	 * Create a new {@link ProcessingInfo}. All parameters are optional.
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
	 * Create a new {@link ProcessingInfo} with empty values.
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
	 * Setter for {@link #getCommandID()}.
	 * 
	 * @param commandID
	 *        The command id to set.
	 */
	public void setCommandID(String commandID) {
		this.commandID = commandID;
	}

	/**
	 * Setter for {@link #getCommandName()}.
	 * 
	 * @param commandName
	 *        The command name to set, <b>must not</b> be <code>null</code>.
	 */
	public void setCommandName(ResKey commandName) {
		assert commandName != null : "Command name must not be null!";
		this.commandName = commandName;
	}

	/**
	 * Setter for {@link #getComponentName()}.
	 * 
	 * @param componentName
	 *        The component name to set.
	 */
	public void setComponentName(ComponentName componentName) {
		this.componentName = componentName;
	}

	/**
	 * Setter for {@link #getJSPName()}.
	 * 
	 * @param jspName
	 *        The JSP name to set.
	 */
	public void setJSPName(String jspName) {
		this.jspName = jspName;
	}

	/**
	 * Setter for {@link #getProcessingKind()}.
	 * 
	 * @param kind
	 *        The kind to set.
	 */
	public void setProcessingKind(ProcessingKind kind) {
		this.kind = kind;
	}
}
