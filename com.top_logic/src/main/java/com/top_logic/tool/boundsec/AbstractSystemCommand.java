/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.base.services.simpleajax.AbstractSystemAjaxCommand;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link AbstractCommandHandler} with {@link SimpleBoundCommandGroup#SYSTEM_NAME system} command
 * group.
 * 
 * @see AbstractSystemAjaxCommand
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSystemCommand extends AbstractCommandHandler {

	/**
	 * Configuration for {@link AbstractSystemCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a new {@link AbstractSystemCommand}.
	 */
	public AbstractSystemCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

}

