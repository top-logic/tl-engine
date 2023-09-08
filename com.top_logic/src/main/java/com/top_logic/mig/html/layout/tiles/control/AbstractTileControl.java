/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.control;

import java.util.Map;

import com.top_logic.layout.basic.AbstractConstantControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.GuiInspectorPlugin;
import com.top_logic.mig.html.layout.tiles.scripting.TileInspectCommand;

/**
 * Abstract implementation for controls displaying tiles.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTileControl<T> extends AbstractConstantControlBase {

	/** Common commands for {@link AbstractTileControl}. */
	protected static final Map<String, ControlCommand> TILE_COMMANDS = createCommandMap(TileInspectCommand.INSTANCE);

	private final T _model;

	/**
	 * Creates a new {@link AbstractTileControl}.
	 * 
	 * @see AbstractConstantControlBase#AbstractConstantControlBase(Map)
	 */
	public AbstractTileControl(T model, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);

		_model = model;
	}

	@Override
	public T getModel() {
		return _model;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	/**
	 * Adds {@link GuiInspectorPlugin} to inspect {@link #getModel()}.
	 * 
	 * @param inspector
	 *        The {@link InspectorModel} to enhance.
	 */
	public abstract void buildInspector(InspectorModel inspector);

}

