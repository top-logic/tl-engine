/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.Map;

import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.runtime.action.CommandActionOpBase;
import com.top_logic.layout.scripting.runtime.action.CommandActionOp;
import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link ApplicationAction} invoking a {@link CommandHandler} by internal name.
 * 
 * @see CommandActionOp
 * @see CommandHandler#handleCommand(com.top_logic.layout.DisplayContext,
 *      com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CommandAction extends CommandActionBase {

	@Override
	@ClassDefault(CommandActionOp.class)
	Class<CommandActionOpBase<?>> getImplementationClass();

	/**
	 * The command name as defined in the {@link CommandHandlerFactory}.
	 * 
	 * <p>
	 * If not given, {@link #getCommandLabel()} is used to identify the command during replay.
	 * </p>
	 * 
	 * @see #getCommandLabel()
	 * @see BoundCommand#getID()
	 */
	@Nullable
	String getCommandName();

	/** @see #getCommandName() */
	void setCommandName(String value);
	
	/**
	 * The name of the class that is implements the command.
	 * 
	 * <p>
	 * This property is optional and only for debugging purpose. The value of this property must not
	 * be used for the action implementation.
	 * </p>
	 */
	String getCommandImplementationComment();

	/** @see #getCommandImplementationComment() */
	void setCommandImplementationComment(String value);

	/**
	 * Label of the command as displayed at the GUI.
	 * 
	 * <p>
	 * The label is only used to identify the command during replay, if {@link #getCommandName()} is
	 * not given.
	 * </p>
	 * 
	 * @see #getCommandName()
	 */
	@Nullable
	String getCommandLabel();

	/** @see #getCommandImplementationComment() */
	void setCommandLabel(String value);

}
