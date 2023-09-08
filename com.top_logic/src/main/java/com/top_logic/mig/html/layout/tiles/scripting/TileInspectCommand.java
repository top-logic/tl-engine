/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.util.Map;

import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.control.AbstractTileControl;

/**
 * {@link GuiInspectorCommand} for inspection of {@link TileLayout}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileInspectCommand extends GuiInspectorCommand<AbstractTileControl<?>, AbstractTileControl<?>> {

	/** Singleton {@link TileInspectCommand} instance. */
	public static final TileInspectCommand INSTANCE = new TileInspectCommand();

	private TileInspectCommand() {
		// singleton instance
	}

	@Override
	protected AbstractTileControl<?> findInspectedGuiElement(AbstractTileControl<?> control,
			Map<String, Object> arguments) throws AssertionError {
		return control;
	}

	@Override
	protected void buildInspector(InspectorModel inspector, AbstractTileControl<?> model) {
		model.buildInspector(inspector);
	}

}

