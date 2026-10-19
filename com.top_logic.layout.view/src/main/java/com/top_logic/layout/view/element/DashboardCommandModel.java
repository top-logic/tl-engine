/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.command.CommandCliques;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandModel} that delegates to a {@link ReactDashboardControl} for operations on its
 * layout.
 *
 * <p>
 * Factories create an {@link #editCommand(ReactDashboardControl) edit} command (visible when the
 * dashboard is not in edit mode), a {@link #doneCommand(ReactDashboardControl) done} command
 * (visible when the dashboard is in edit mode), and for each tile the user may hide a
 * {@link #showTileCommand(ReactDashboardControl, String, ResKey) show tile} command (visible in
 * edit mode while the tile is hidden). All carry {@link CommandPlacement#TOOLBAR} so that a
 * surrounding {@link com.top_logic.layout.view.element.CommandScopeElement CommandScopeElement}
 * (panel, window, app bar) renders them in its chrome. A command follows the
 * {@link ReactDashboardControl#addStateChangeListener(Runnable) state} of the dashboard while it is
 * {@link #attach() attached}.
 * </p>
 */
public class DashboardCommandModel implements CommandModel {

	/** The {@link #getName() name} of the {@link #editCommand(ReactDashboardControl) edit} command. */
	public static final String EDIT_COMMAND = "dashboardEdit";

	/** The {@link #getName() name} of the {@link #doneCommand(ReactDashboardControl) done} command. */
	public static final String DONE_COMMAND = "dashboardDone";

	/**
	 * Prefix of the {@link #getName() name} of a
	 * {@link #showTileCommand(ReactDashboardControl, String, ResKey) show tile} command, followed by
	 * the id of the tile.
	 */
	public static final String SHOW_TILE_COMMAND_PREFIX = "dashboardShowTile.";

	private final String _name;

	private final ResKey _labelKey;

	private final ThemeImage _image;

	private final String _clique;

	private final Consumer<ReactContext> _action;

	private final Predicate<ReactDashboardControl> _visibleWhen;

	private final ReactDashboardControl _dashboard;

	private boolean _visible;

	private final List<Runnable> _stateChangeListeners = new ArrayList<>();

	private final Runnable _dashboardListener = this::handleDashboardStateChanged;

	private DashboardCommandModel(String name, ResKey labelKey, ThemeImage image, String clique,
			ReactDashboardControl dashboard, Consumer<ReactContext> action,
			Predicate<ReactDashboardControl> visibleWhen) {
		_name = name;
		_labelKey = labelKey;
		_image = image;
		_clique = clique;
		_dashboard = dashboard;
		_action = action;
		_visibleWhen = visibleWhen;
		_visible = visibleWhen.test(dashboard);
	}

	/**
	 * Creates the "Edit Layout" command: visible when the dashboard is not in edit mode.
	 */
	public static DashboardCommandModel editCommand(ReactDashboardControl dashboard) {
		return new DashboardCommandModel(EDIT_COMMAND, I18NConstants.DASHBOARD_EDIT,
			Icons.DASHBOARD_EDIT, null, dashboard,
			ctx -> dashboard.enterEditMode(),
			d -> !d.isEditMode());
	}

	/**
	 * Creates the "Done" command: visible when the dashboard is in edit mode.
	 */
	public static DashboardCommandModel doneCommand(ReactDashboardControl dashboard) {
		return new DashboardCommandModel(DONE_COMMAND, I18NConstants.DASHBOARD_DONE,
			Icons.DASHBOARD_DONE, null, dashboard,
			ctx -> dashboard.exitEditMode(),
			d -> d.isEditMode());
	}

	/**
	 * Creates the command that adds a hidden tile to the dashboard again: visible when the
	 * dashboard is in edit mode and the tile is hidden.
	 *
	 * <p>
	 * The commands of all tiles of a dashboard form the clique {@link CommandCliques#DASHBOARD_TILES}.
	 * </p>
	 *
	 * @param tileId
	 *        The id of the tile to show.
	 * @param title
	 *        The name of the tile, the label of the command.
	 */
	public static DashboardCommandModel showTileCommand(ReactDashboardControl dashboard, String tileId,
			ResKey title) {
		return new DashboardCommandModel(SHOW_TILE_COMMAND_PREFIX + tileId, title, null,
			CommandCliques.DASHBOARD_TILES, dashboard,
			ctx -> dashboard.showTile(tileId),
			d -> d.isEditMode() && !d.isTileVisible(tileId));
	}

	/**
	 * Attaches to the dashboard's state change listener.
	 */
	public void attach() {
		_dashboard.addStateChangeListener(_dashboardListener);
		handleDashboardStateChanged();
	}

	/**
	 * Detaches from the dashboard.
	 */
	public void detach() {
		_dashboard.removeStateChangeListener(_dashboardListener);
	}

	private void handleDashboardStateChanged() {
		boolean newVisible = _visibleWhen.test(_dashboard);
		if (newVisible != _visible) {
			_visible = newVisible;
			fireStateChange();
		}
	}

	private void fireStateChange() {
		for (Runnable l : _stateChangeListeners) {
			l.run();
		}
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getLabel() {
		return Resources.getInstance().getString(_labelKey);
	}

	@Override
	public ThemeImage getImage() {
		return _image;
	}

	@Override
	public CommandPlacement getPlacement() {
		return CommandPlacement.TOOLBAR;
	}

	@Override
	public String getClique() {
		return _clique;
	}

	@Override
	public boolean isExecutable() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return _visible;
	}

	@Override
	public HandlerResult perform(ReactContext context) {
		_action.accept(context);
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public void addStateChangeListener(Runnable listener) {
		_stateChangeListeners.add(listener);
	}

	@Override
	public void removeStateChangeListener(Runnable listener) {
		_stateChangeListeners.remove(listener);
	}
}
