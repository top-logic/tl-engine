/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.StepOutCommand;
import com.top_logic.mig.html.layout.tiles.component.StepOutTileCommand;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Helper-Class to handle step-out of a tile.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class StepOutHelper {

	private LayoutComponent _container;

	private CommandHandler _legacyHandler;

	private CommandHandler _handler;

	public StepOutHelper(LayoutComponent component) {
		_container = findContainer(component);
		_legacyHandler = CommandHandlerFactory.getInstance().getHandler(StepOutTileCommand.COMMAND_ID);
		_handler = CommandHandlerFactory.getInstance().getHandler(StepOutCommand.COMMAND_ID);
	}

	private LayoutComponent findContainer(LayoutComponent component) {
		LayoutComponent container = component;
		while (container != null && !(container instanceof TileContainerComponent)
			&& !(container instanceof RootTileComponent)) {
			container = container.getParent();
		}
		if (container == null) {
			throw new IllegalArgumentException("Can not find tile-root for " + component);
		}
		return container;
	}

	public void stepOut(DisplayContext aContext) {
		if (_container instanceof RootTileComponent) {
			_handler.handleCommand(aContext, _container, null, null);
		} else {
			_legacyHandler.handleCommand(aContext, _container, null, null);
		}
	}

}
