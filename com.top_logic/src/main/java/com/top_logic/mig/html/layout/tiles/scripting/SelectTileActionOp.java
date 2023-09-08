/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.GroupTileComponent;
import com.top_logic.util.Resources;

/**
 * {@link AbstractApplicationActionOp} for {@link SelectTileAction}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelectTileActionOp extends AbstractApplicationActionOp<SelectTileActionOp.SelectTileAction> {

	/**
	 * {@link ApplicationAction} that selects a tile identified by its label in a
	 * {@link GroupTileComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface SelectTileAction extends ApplicationAction {
		
		@Override
		@ClassDefault(SelectTileActionOp.class)
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();
		
		/**
		 * Label of the tile to select.
		 */
		@Mandatory
		String getTileLabel();

		/**
		 * Setter for {@link #getTileLabel()}.
		 */
		void setTileLabel(String label);

		/**
		 * {@link ModelName} identifying the {@link GroupTileComponent} in which the tile is
		 * selected.
		 */
		@Mandatory
		ModelName getGroup();

		/**
		 * Setter for {@link #getGroup()}.
		 */
		void setGroup(ModelName group);

	}
	
	/**
	 * Creates a new {@link SelectTileActionOp}.
	 */
	public SelectTileActionOp(InstantiationContext context, SelectTileAction config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) {
		String expectedTileLabel = getConfig().getTileLabel();
		Resources resources = context.getDisplayContext().getResources();
		GroupTileComponent groupTile = findGroup(context);
		for (LayoutComponent child : groupTile.getChildList()) {
			if (expectedTileLabel.equals(resources.getString(child.getConfig().getTileInfo().getLabel()))) {
				groupTile.setSelected(child);
				return argument;
			}
		}
		StringBuilder error = new StringBuilder();
		error.append("Tile with label '");
		error.append(expectedTileLabel);
		error.append("' not found in '");
		error.append(groupTile);
		error.append("'.");
		throw ApplicationAssertions.fail(getConfig(), error.toString());
	}

	private GroupTileComponent findGroup(ActionContext context) {
		return (GroupTileComponent) ModelResolver.locateModel(context, getConfig().getGroup());
	}

	/**
	 * Records the selection of the given tile in the given group.
	 * 
	 * @param group
	 *        The {@link GroupTileComponent} selecting the given tile.
	 * @param tile
	 *        The newly selected tile.
	 */
	public static void recordSelectAction(GroupTileComponent group, LayoutComponent tile) {
		ScriptingRecorder.recordAction(() -> {
			String tileLabel = Resources.getInstance().getString(tile.getConfig().getTileInfo().getLabel());
			return createSelectAction(group, tileLabel);
		});
	}

	/**
	 * Creates a new {@link SelectTileAction} with given parameters.
	 */
	public static SelectTileAction createSelectAction(GroupTileComponent group, String tileLabel) {
		SelectTileAction action =
			ActionFactory.newApplicationAction(SelectTileAction.class, SelectTileActionOp.class);
		action.setGroup(ModelResolver.buildModelName(group));
		action.setTileLabel(tileLabel);
		return action;
	}


}

