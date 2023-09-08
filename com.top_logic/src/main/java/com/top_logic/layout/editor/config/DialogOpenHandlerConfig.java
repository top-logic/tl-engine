package com.top_logic.layout.editor.config;
/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler.Config;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * {@link Config} for in-app defined dialogs.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface DialogOpenHandlerConfig extends OpenModalDialogCommandHandler.Config {

	/**
	 * Choose default implementation class.
	 */
	@Override
	@ClassDefault(OpenModalDialogCommandHandler.class)
	Class<? extends CommandHandler> getImplementationClass();

	/**
	 * Only needed when configuring a command to open an existing dialog.
	 */
	@Override
	@Hidden
	@NullDefault
	ComponentName getDialogName();

	/**
	 * Do not by default select a group.
	 * 
	 * <p>
	 * The inherited setting is legacy.
	 * </p>
	 */
	@Override
	@NullDefault
	CommandGroupReference getGroup();

	/**
	 * No executability by default.
	 */
	@Override
	@ListDefault(value = {})
	List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

}
