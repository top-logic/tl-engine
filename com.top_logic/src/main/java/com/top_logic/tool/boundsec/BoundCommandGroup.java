/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.io.Serializable;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Security classification of {@link CommandHandler}s.
 * 
 * <p>
 * There is a set of built-in {@link BoundCommandGroup}s, e.g. {@link SimpleBoundCommandGroup#READ}.
 * Custom {@link BoundCommandGroup}s can be defined in the {@link CommandGroupRegistry}.
 * </p>
 * 
 * @see CommandHandler.Config#getGroup()
 *
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
@Label("Command group")
@Format(SimpleBoundCommandGroup.ValueProvider.class)
public interface BoundCommandGroup extends Serializable {

    /**
	 * The ID of this command group.
	 */
	String getID();
    
    /**
	 * Coarse classification of this {@link BoundCommandGroup}.
	 */
	CommandGroupType getCommandType();

	/**
	 * Whether this {@link BoundCommandGroup} is an ordinary {@link CommandGroupType#READ} group.
	 */
	default boolean isReadGroup() {
		return !isSystemGroup() && getCommandType() == CommandGroupType.READ;
	}

	/**
	 * Whether this {@link BoundCommandGroup} has {@link CommandGroupType#WRITE} type.
	 */
	default boolean isWriteGroup() {
		return getCommandType() == CommandGroupType.WRITE;
	}

	/**
	 * Whether this {@link BoundCommandGroup} has {@link CommandGroupType#DELETE} type.
	 */
	default boolean isDeleteGroup() {
		return getCommandType() == CommandGroupType.DELETE;
	}

	/**
	 * Whether this {@link BoundCommandGroup} is the unique "system" command group.
	 */
	default boolean isSystemGroup() {
		return this == SimpleBoundCommandGroup.SYSTEM;
	}

}
