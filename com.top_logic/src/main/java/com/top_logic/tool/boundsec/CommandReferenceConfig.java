/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Base configuration for referencing another {@link CommandHandler} by its
 * {@link CommandHandler.Config#getId()}.
 * 
 * @see #getCommandId()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CommandReferenceConfig extends ConfigurationItem {

	/** @see #getCommandId() */
	String COMMAND_ID = "command-id";

	/**
	 * Id of the referenced {@link CommandHandler}.
	 */
	@Name(COMMAND_ID)
	String getCommandId();

}
