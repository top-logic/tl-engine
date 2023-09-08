/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.AbstractSystemCommand;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * An {@link AbstractCommandHandler} that updates its {@link TreeTableComponent}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class UpdateTreeTableCommandHandler extends AbstractSystemCommand {

	/**
	 * ID of this command.
	 * 
	 * @see CommandHandlerFactory
	 */
	public static final String COMMAND_ID = "updateTreeTable";

	/**
	 * Configuration for {@link UpdateTreeTableCommandHandler}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface Config extends AbstractSystemCommand.Config {

		@StringDefault(UpdateTreeTableCommandHandler.COMMAND_ID)
		@Override
		String getId();

	}

	/** Called by the typed configuration for creating an {@link UpdateTreeTableCommandHandler}. */
	@CalledByReflection
	public UpdateTreeTableCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {

		TreeTableComponent treeTableComponent = (TreeTableComponent) component;
		treeTableComponent.rebuildTableModel();

		return HandlerResult.DEFAULT_RESULT;
	}

}
