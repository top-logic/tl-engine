/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.tool.boundsec.AbstractSystemCommand;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link AJAXCommandHandler} with {@link SimpleBoundCommandGroup#SYSTEM_NAME system} command group.
 * 
 * @see AbstractSystemCommand
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSystemAjaxCommand extends AJAXCommandHandler {

	/**
	 * Configuration for {@link AbstractSystemCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AJAXCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a new {@link AbstractSystemAjaxCommand}.
	 */
	public AbstractSystemAjaxCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

}

