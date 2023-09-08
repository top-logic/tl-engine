/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.ComponentActionOp;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.PersonalizedTile;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.TileUtils;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;
import com.top_logic.util.Resources;

/**
 * {@link ComponentActionOp} for an {@link UpdateCompositeTileAction}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateCompositeTileActionOp extends ComponentActionOp<UpdateCompositeTileActionOp.UpdateCompositeTileAction> {

	/**
	 * {@link ComponentAction} updating the contents of a {@link CompositeTile}.
	 * 
	 * @see TileContainerComponent#updateDisplayedLayout(List)
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface UpdateCompositeTileAction extends ComponentAction {

		@Override
		@ClassDefault(UpdateCompositeTileActionOp.class)
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

		/**
		 * A list of identifier for {@link TileLayout} to be displayed in the current
		 * {@link TileLayout}.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getTiles();

		/**
		 * Setter for {@link #getTiles()}.
		 */
		void setTiles(List<String> tiles);

	}

	/**
	 * Creates a new {@link UpdateCompositeTileActionOp}.
	 */
	public UpdateCompositeTileActionOp(InstantiationContext context, UpdateCompositeTileActionOp.UpdateCompositeTileAction config) {
		super(context, config);
	}

	@Override
	public void checkVisible(ActionContext context, ConfigurationItem contextConfig, LayoutComponent component) {
		// No visibility check. The component is a tile container component which is the base for
		// the displayed path. The component itself is not treated as visible in this case.
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		TileContainerComponent tileContainer = (TileContainerComponent) component;
		tileContainer.updateDisplayedLayout(getNewLayouts(tileContainer));
		return argument;
	}

	private List<TileLayout> getNewLayouts(TileContainerComponent tileContainer) {
		List<String> layouts = config.getTiles();
		TileLayout currentLayout = tileContainer.displayedLayout();
		if (!(currentLayout instanceof CompositeTile)) {
			throw errorNotCompsiteTile(currentLayout);
		}
		Map<String, TileLayout> tilesByLabel = new HashMap<>();
		Resources resources = Resources.getInstance();
		for (TileLayout tile : ((CompositeTile) currentLayout).getTiles()) {
			if (tile instanceof TileRef) {
				// TileRef's are recorded by name
				tilesByLabel.put(((TileRef) tile).getName(), tile);
			} else {
				ResKey label = tileContainer.getLabel(tile);
				if (label == null) {
					continue;
				}
				tilesByLabel.put(resources.getString(label), tile);
			}
		}
		Map<String, TileRef> allTilesByName = TileUtils.allTilesByName(tileContainer);
		List<TileLayout> newTiles = new ArrayList<>();
		for (String label : layouts) {
			TileLayout tile = tilesByLabel.remove(label);
			if (tile != null) {
				TileLayout copy = TypedConfiguration.copy(tile);
				if (copy instanceof PersonalizedTile) {
					((PersonalizedTile) copy).setHidden(false);
				}
				newTiles.add(copy);
				continue;
			}
			TileRef tileRef = allTilesByName.get(label);
			if (tileRef != null) {
				newTiles.add(tileRef);
				continue;
			}
			throw ApplicationAssertions.fail(config, "Unknown tile: " + label);
		}
		for (TileLayout remainingLayout : tilesByLabel.values()) {
			if (remainingLayout instanceof PersonalizedTile) {
				PersonalizedTile copy = TypedConfiguration.copy((PersonalizedTile) remainingLayout);
				copy.setHidden(true);
				newTiles.add(copy);
			}
		}
		return newTiles;
	}

	private RuntimeException errorNotCompsiteTile(TileLayout currentLayout) {
		StringWriter out = new StringWriter();
		out.write("Expected current layout is ");
		out.write(CompositeTile.class.getName());
		out.write(" but was: ");
		TypedConfiguration.serialize(currentLayout, out);
		return ApplicationAssertions.fail(config, out.toString());
	}

	/**
	 * Records changing the currently displayed {@link CompositeTile} to the new list of
	 * {@link CompositeTile#getTiles() children}.
	 */
	public static void recordUpdateCompositeTile(TileContainerComponent component, List<TileLayout> newLayout) {
		UpdateCompositeTileAction action = TypedConfiguration.newConfigItem(UpdateCompositeTileAction.class);
		List<String> layouts = new ArrayList<>(newLayout.size());
		Resources resources = Resources.getInstance();
		for (TileLayout layout : newLayout) {
			if (layout instanceof TileRef) {
				// Record the name, because the name is unique
				layouts.add(((TileRef) layout).getName());
				continue;
			}
			layouts.add(resources.getString(component.getLabel(layout)));

		}
		action.setTiles(layouts);
		action.setComponentName(component.getName());
		action.setComponentImplementationComment(component.getClass().getName());
		ScriptingRecorder.recordAction(action);
	}

}
