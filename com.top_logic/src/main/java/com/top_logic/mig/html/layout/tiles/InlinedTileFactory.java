/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.BOComponentTile;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.InlinedTileComponent;
import com.top_logic.mig.html.layout.tiles.component.TilePreview;
import com.top_logic.mig.html.layout.tiles.scripting.InlinedTileComponentNaming;

/**
 * {@link TileFactory} that creates for each element in {@link InlinedTileComponent#getGUIModel()}
 * (interpreted or transformed as {@link Collection}) a {@link ComponentTile}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InlinedTileFactory implements TileFactory {

	/** Singleton {@link InlinedTileFactory} instance. */
	public static final InlinedTileFactory INSTANCE = new InlinedTileFactory();

	/**
	 * Creates a new {@link InlinedTileFactory}.
	 */
	protected InlinedTileFactory() {
		// singleton instance
	}

	@Override
	public List<ComponentTile> createTiles(LayoutComponent component) {
		InlinedTileComponent itemProvider = findInlinedComponent(component);
		if (itemProvider.hideReason() != null) {
			return Collections.emptyList();
		}

		TilePreview preview = TypedConfigUtil.createInstance(component.getConfig().getTileInfo().getPreview());

		return toList(itemProvider.getGUIModel())
			.stream()
			.map(bo -> newComponentTile(bo, itemProvider, preview))
			.collect(Collectors.toList());
	}

	private InlinedTileComponent findInlinedComponent(LayoutComponent component) {
		InlinedTileComponent inlinedComponent =
			TileComponentFinder.getFirstOfType(InlinedTileComponent.class, component);
		if (inlinedComponent == null) {
			throw new IllegalArgumentException("No InlinedTileComponent as child of " + component + " found.");
		}
		return inlinedComponent;
	}

	private static Collection<?> toList(Object guiModel) {
		if (guiModel instanceof Collection) {
			return (Collection<?>) guiModel;
		}
		return CollectionUtil.singletonOrEmptyList(guiModel);
	}

	private BOComponentTile newComponentTile(Object guiElement, InlinedTileComponent inlined, TilePreview preview) {
		return new BOComponentTile(inlined, inlined, guiElement) {

			@Override
			public TilePreview getPreview() {
				return preview;
			}

			@Override
			public void displayTile() {
				selectBusinessObject();
				if (ScriptingRecorder.isRecordingActive()) {
					ScriptingRecorder.recordSelection(this::inlinedTileName, getBusinessObject(), true,
						SelectionChangeKind.ABSOLUTE);
				}
			}

			private ModelName inlinedTileName() {
				return InlinedTileComponentNaming.INSTANCE.buildName(inlined()).get();
			}

			@Override
			public Provider<Menu> getBurgerMenu() {
				return BOComponentTile.getTileCommands(inlined(), inlined());
			}

			private InlinedTileComponent inlined() {
				return (InlinedTileComponent) selectable();
			}

		};
	}

}

