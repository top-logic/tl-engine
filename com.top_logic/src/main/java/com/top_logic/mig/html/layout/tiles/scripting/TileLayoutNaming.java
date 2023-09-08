/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import com.top_logic.layout.component.ComponentBasedNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link ModelNamingScheme} for a {@link TileLayoutName}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileLayoutNaming
		extends ComponentBasedNamingScheme<ContainerComponentTile, TileLayoutNaming.TileLayoutName> {

	/**
	 * {@link ModelName} that identifies a {@link TileLayout} within a
	 * {@link TileContainerComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface TileLayoutName extends ComponentBasedNamingScheme.ComponentBasedName, TilePathName {
		// Sum interface
	}

	/**
	 * Creates a new {@link TileLayoutNaming}.
	 */
	public TileLayoutNaming() {
		super(ContainerComponentTile.class, TileLayoutName.class);
	}

	@Override
	public ContainerComponentTile locateModel(ActionContext context, TileLayoutName name) {
		TileContainerComponent component = (TileContainerComponent) locateComponent(context, name);
		TileLayout tileLayout = DisplayTileActionOp.resolveTile(component, name.getTilePath(), name);
		return new ContainerComponentTile(component, tileLayout);
	}

	@Override
	protected void initName(TileLayoutName name, ContainerComponentTile model) {
		super.initName(name, model);
		name.setTilePath(model.container().getTilePath(model.getBusinessObject()));
	}

	@Override
	protected LayoutComponent getContextComponent(ContainerComponentTile model) {
		return model.container();
	}

}

