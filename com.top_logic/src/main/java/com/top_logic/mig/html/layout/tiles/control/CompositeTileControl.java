/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.control;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.PersonalizedTile;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.ComponentTileSupplier;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;
import com.top_logic.mig.html.layout.tiles.scripting.DisplayedPathAssertion;
import com.top_logic.mig.html.layout.tiles.scripting.TilePathAssertion;
import com.top_logic.mig.html.layout.tiles.scripting.TilePathInfoPlugin;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link Control} displaying the a sequence of {@link ComponentTile}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompositeTileControl extends AbstractTileControl<Supplier<List<ComponentTile>>> {

	private ResKey _noCardsKey;

	/**
	 * Creates a {@link CompositeTileControl}.
	 */
	public CompositeTileControl(Supplier<List<ComponentTile>> tiles) {
		this(tiles, I18NConstants.NO_CARDS_AVAILABLE);
	}

	/**
	 * Creates a {@link CompositeTileControl}.
	 * 
	 * @param noCardsKey
	 *        The {@link ResKey} to use when no cards are visible.
	 */
	public CompositeTileControl(Supplier<List<ComponentTile>> tiles, ResKey noCardsKey) {
		super(tiles, TILE_COMMANDS);
		_noCardsKey = noCardsKey;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "tileContainer");
		out.endBeginTag();

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "tileRow");
		out.endBeginTag();


		boolean allInvisible = true;
		for (ComponentTile tile : getModel().get()) {
			if (hideTile(tile)) {
				continue;
			}
			allInvisible = false;
			new TileDescriptionControl(tile).write(context, out);
		}

		if (allInvisible) {
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "tileNoCards");
			out.endBeginTag();
			out.writeText(context.getResources().getString(_noCardsKey));
			out.endTag(DIV);
		}

		out.endTag(DIV);

		out.endTag(DIV);

		out.endTag(DIV);
	}

	private boolean hideTile(ComponentTile tile) {
		if (ExecutableState.allVisible()) {
			return false;
		}
		if (!tile.isAllowed()) {
			return true;
		}
		if (tile.getBusinessObject() instanceof PersonalizedTile) {
			if (((PersonalizedTile) tile.getBusinessObject()).isHidden()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void buildInspector(InspectorModel inspector) {
		Object model = getModel();
		if (model instanceof ContainerComponentTile) {
			ContainerComponentTile containerModel = (ContainerComponentTile) model;
			inspector.add(new TilePathAssertion(containerModel.container()));
			inspector.add(new TilePathInfoPlugin(containerModel));
		} else if (model instanceof ComponentTileSupplier) {
			LayoutComponent tileContainer = ((ComponentTileSupplier) model).getRootComponent();
			if (tileContainer instanceof TileContainerComponent) {
				inspector.add(new TilePathAssertion((TileContainerComponent) tileContainer));
			} else {
				inspector.add(new DisplayedPathAssertion((RootTileComponent) tileContainer));
			}
		}
		if (model instanceof ComponentTile) {
			LayoutComponent component = ((ComponentTile) model).getTileComponent();
			if (component != null) {
				GuiInspectorPluginFactory.createComponentInformation(inspector, component);
			}
		}
	}

}
