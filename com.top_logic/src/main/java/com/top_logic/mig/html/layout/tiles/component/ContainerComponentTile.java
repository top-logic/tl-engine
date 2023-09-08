/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.scripting.DisplayTileActionOp;
import com.top_logic.mig.html.layout.tiles.scripting.TileLayoutNaming.TileLayoutName;

/**
 * {@link ComponentTile} displayed within a {@link TileContainerComponent}.
 * 
 * <p>
 * This class is a combination of a {@link TileLayout} which is used within a
 * {@link TileContainerComponent}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ContainerComponentTile extends AbstractComponentTile implements NamedModel, ComponentTileSupplier {

	private TileContainerComponent _container;

	/**
	 * Creates a new {@link ContainerComponentTile}.
	 * 
	 * @param container
	 *        The component in which the given {@link TileLayout} is displayed
	 * @param tile
	 *        A {@link TileLayout} of the given component.
	 */
	public ContainerComponentTile(TileContainerComponent container, TileLayout tile) {
		super(container.getTileComponent(tile), tile);
		_container = container;
	}

	@Override
	public TileLayoutName getModelName() {
		return (TileLayoutName) ModelResolver.buildModelName(this);
	}

	/**
	 * The {@link TileContainerComponent}, this {@link ComponentTile} belongs to.
	 */
	public TileContainerComponent container() {
		return _container;
	}

	@Override
	public void displayTile() {
		container().displayTileLayout(getBusinessObject());
		if (ScriptingRecorder.isRecordingActive()) {
			DisplayTileActionOp.recordDisplayAction(this);
		}
	}

	@Override
	public TileLayout getBusinessObject() {
		return (TileLayout) super.getBusinessObject();
	}

	@Override
	public TilePreview getPreview() {
		return container().getPreview(getBusinessObject());
	}

	@Override
	public boolean isAllowed() {
		return container().isTileAllowed(getBusinessObject());
	}

	@Override
	public Provider<Menu> getBurgerMenu() {
		return container().getCommandsProvider(getBusinessObject());
	}

	@Override
	public ResKey getTileLabel() {
		return container().getLabel(getBusinessObject());
	}

	@Override
	public TileContainerComponent getRootComponent() {
		return container();
	}

	@Override
	public List<ComponentTile> get() {
		List<ComponentTile> allTiles = new ArrayList<>();
		TileLayout businessObject = getBusinessObject();
		if (businessObject instanceof CompositeTile) {
			((CompositeTile) businessObject).getTiles()
				.stream()
				.map(tileLayout -> container().getComponentTiles(tileLayout))
				.forEach(tiles -> allTiles.addAll(tiles));
		}
		return allTiles;
	}

}
