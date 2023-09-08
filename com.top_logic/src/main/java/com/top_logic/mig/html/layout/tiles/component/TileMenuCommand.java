/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.scripting.recorder.ref.ui.button.LabeledButtonNaming;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileUtils;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandModel} to be executed in the menu of a {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TileMenuCommand<T extends TileLayout> extends AbstractCommandModel {

	/** {@link TileContainerComponent} holding the {@link #_tile}. */
	protected final TileContainerComponent _container;

	/** {@link TileLayout} to process. */
	protected final T _tile;

	/**
	 * Creates a new {@link TileMenuCommand}.
	 */
	public TileMenuCommand(TileContainerComponent container, T tile) {
		_container = container;
		_tile = tile;
		set(LabeledButtonNaming.BUSINESS_OBJECT, new ContainerComponentTile(container, tile));
	}

	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext context) {
		TileLayout tileContainer = TileUtils.getContainer(_tile);
		if (tileContainer == null) {
			return HandlerResult.error(I18NConstants.NO_CONTAINER_FOUND);
		}
		if (_container.displayedLayout() != tileContainer) {
			return HandlerResult.error(I18NConstants.CONTAINER_NOT_DISPLAYED);
		}
		return internalExecute(context);
	}

	/**
	 * Actual implementation of {@link #internalExecuteCommand(DisplayContext)}.
	 */
	protected abstract HandlerResult internalExecute(DisplayContext context);

}

