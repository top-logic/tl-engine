/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.text.ParseException;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.ComponentActionOp;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link ComponentActionOp} for a {@link DisplayTileAction}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayTileActionOp extends ComponentActionOp<DisplayTileActionOp.DisplayTileAction> {

	/**
	 * {@link ComponentAction} that displays a {@link TileLayout} in a
	 * {@link TileContainerComponent}, identified by its path in
	 * {@link TileContainerComponent#displayedLayout()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface DisplayTileAction extends ComponentAction, TilePathName {

		@Override
		@ClassDefault(DisplayTileActionOp.class)
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	}

	/**
	 * Creates a new {@link DisplayTileActionOp}.
	 */
	public DisplayTileActionOp(InstantiationContext context, DisplayTileActionOp.DisplayTileAction config) {
		super(context, config);
	}

	@Override
	public void checkVisible(ActionContext context, ConfigurationItem contextConfig, LayoutComponent component) {
		// No visibility check. The component is a tile container component which is the base for
		// the displayed path. The component itself is not treated as visible in this case.
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		List<String> tilePath = getConfig().getTilePath();
		TileContainerComponent container = (TileContainerComponent) component;
		container.displayTileLayout(resolveTile(container, tilePath, getConfig()));
		return argument;
	}

	static TileLayout resolveTile(TileContainerComponent component, List<String> tilePath,
			ConfigurationItem errorContext) {
		try {
			return component.resolveTilePath(tilePath);
		} catch (ParseException ex) {
			int errorOffset = ex.getErrorOffset();
			StringBuilder error = new StringBuilder();
			error.append("Unable to display tile with path '");
			error.append(BreadcrumbStrings.INSTANCE.getSpecification(tilePath));
			error.append("'. No tile '");
			error.append(tilePath.get(ex.getErrorOffset()));
			error.append("' found within tile '");
			error.append(BreadcrumbStrings.INSTANCE.getSpecification(tilePath.subList(0, errorOffset)));
			error.append("'.");
			throw ApplicationAssertions.fail(errorContext, error.toString(), ex);
		}
	}

	/**
	 * Records displaying the given {@link ContainerComponentTile}.
	 */
	public static void recordDisplayAction(ContainerComponentTile tile) {
		TileContainerComponent component = tile.container();
		DisplayTileAction action =
			ActionFactory.newComponentAction(DisplayTileAction.class, DisplayTileActionOp.class, component);
		action.setTilePath(component.getTilePath(tile.getBusinessObject()));
		ScriptingRecorder.recordAction(action);
	}
	
}
