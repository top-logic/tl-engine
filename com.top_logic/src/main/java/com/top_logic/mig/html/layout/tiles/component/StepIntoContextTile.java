/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * {@link AbstractCommandHandler} that navigates into {@link TileRef#getContentTile() content} of a
 * {@link TileRef}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StepIntoContextTile extends AbstractCommandHandler {

	/**
	 * Command id to find the instance of the {@link StepIntoContextTile} within the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "stepIntoTile";

	/**
	 * Creates a new {@link StepIntoContextTile}.
	 */
	public StepIntoContextTile(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		TileContainerComponent container = (TileContainerComponent) aComponent;
		container.displayTileLayout(((TileRef) container.displayedLayout()).getContentTile());
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), HasTileRefLayout.INSTANCE);
	}

}

