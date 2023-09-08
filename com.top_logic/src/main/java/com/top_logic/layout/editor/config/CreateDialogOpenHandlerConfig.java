/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler.Config;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link Config} for in-app defined create dialogs.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CreateDialogOpenHandlerConfig extends DialogOpenHandlerConfig {

	@Override
	@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
	CommandGroupReference getGroup();

	@Override
	@StringDefault(CommandHandlerFactory.CREATE_CLIQUE)
	String getClique();

}
