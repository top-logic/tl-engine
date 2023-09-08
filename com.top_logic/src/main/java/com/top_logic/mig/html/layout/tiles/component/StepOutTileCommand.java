/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * {@link PreconditionCommandHandler} that changes the display of a tile to its container tile.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StepOutTileCommand extends PreconditionCommandHandler {

	/**
	 * Command id to find the instance of the {@link StepOutTileCommand} within the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "stepOutTile";

	/**
	 * Creates a new {@link StepOutTileCommand}.
	 */
	public StepOutTileCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		TileContainerComponent tileContainerComponent = (TileContainerComponent) component;
		List<TileLayout> layoutPath = tileContainerComponent.displayedLayoutPath();
		TileLayout container = layoutPath.size() > 1 ? layoutPath.get(layoutPath.size() - 2) : null;
		if (container == null) {
			// Current layout is root. Therefore no step out possible
			return new Hide();
		}
		return new Success() {

			@Override
			protected void doExecute(DisplayContext context) {
				tileContainerComponent.displayTileLayout(container);
			}
		};
	}

}

