/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.BOComponentTile;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TilePreview;
import com.top_logic.mig.html.layout.tiles.scripting.SelectTileActionOp;
import com.top_logic.tool.boundsec.BoundCheckerComponent;

/**
 * {@link TileFactory} creating one {@link ComponentTile} for a child of a
 * {@link GroupTileComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SingleTileFactory implements TileFactory {

	/** Singleton {@link SingleTileFactory} instance. */
	public static final SingleTileFactory INSTANCE = new SingleTileFactory();

	/**
	 * Creates a new {@link SingleTileFactory}.
	 */
	protected SingleTileFactory() {
		// singleton instance
	}

	@Override
	public List<ComponentTile> createTiles(LayoutComponent component) {
		GroupTileComponent group = (GroupTileComponent) component.getParent();
		return Collections.singletonList(new BOComponentTile(component, group, component) {

			@Override
			public TilePreview getPreview() {
				return TypedConfigUtil.createInstance(componentBO().getConfig().getTileInfo().getPreview());
			}

			/**
			 * Selects {@link #getBusinessObject()}. The {@link RootTileComponent} has listener
			 * which steps into the tile.
			 */
			@Override
			public void displayTile() {
				selectBusinessObject();
				if (ScriptingRecorder.isRecordingActive()) {
					SelectTileActionOp.recordSelectAction(tileGroup(), componentBO());
				}
			}
			
			@Override
			public boolean isAllowed() {
				LayoutComponent checker = componentBO();
				if (checker instanceof BoundCheckerComponent) {
					return ((BoundCheckerComponent) checker).hideReason() == null;
				}
				return super.isAllowed();
			}
			
			@Override
			public Provider<Menu> getBurgerMenu() {
				return BOComponentTile.getTileCommands(tileGroup(), componentBO());
			}
			
			private GroupTileComponent tileGroup() {
				return (GroupTileComponent) selectable();
			}

			LayoutComponent componentBO() {
				return (LayoutComponent) getBusinessObject();
			}

		});
	}

}

