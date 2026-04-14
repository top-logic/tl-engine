/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandModel} that delegates to a {@link ReactDashboardControl} for edit-mode
 * lifecycle operations.
 *
 * <p>
 * Factories create an {@code edit} command (visible when the dashboard is not in edit mode)
 * and a {@code done} command (visible when the dashboard is in edit mode). Both carry
 * {@link CommandModel#PLACEMENT_TOOLBAR} so that a surrounding
 * {@link com.top_logic.layout.view.element.CommandScopeElement CommandScopeElement}
 * (panel, window, app bar) renders them in its chrome.
 * </p>
 */
public class DashboardCommandModel implements CommandModel {

	private final String _name;

	private final ResKey _labelKey;

	private final ThemeImage _image;

	private final Consumer<ReactContext> _action;

	private final Predicate<ReactDashboardControl> _visibleWhen;

	private final ReactDashboardControl _dashboard;

	private boolean _visible;

	private final List<Runnable> _stateChangeListeners = new ArrayList<>();

	private final Runnable _dashboardListener = this::handleDashboardStateChanged;

	private DashboardCommandModel(String name, ResKey labelKey, ThemeImage image,
			ReactDashboardControl dashboard, Consumer<ReactContext> action,
			Predicate<ReactDashboardControl> visibleWhen) {
		_name = name;
		_labelKey = labelKey;
		_image = image;
		_dashboard = dashboard;
		_action = action;
		_visibleWhen = visibleWhen;
		_visible = visibleWhen.test(dashboard);
	}

	/**
	 * Creates the "Edit Layout" command: visible when the dashboard is not in edit mode.
	 */
	public static DashboardCommandModel editCommand(ReactDashboardControl dashboard) {
		return new DashboardCommandModel("dashboardEdit", I18NConstants.DASHBOARD_EDIT,
			Icons.DASHBOARD_EDIT, dashboard,
			ctx -> dashboard.enterEditMode(),
			d -> !d.isEditMode());
	}

	/**
	 * Creates the "Done" command: visible when the dashboard is in edit mode.
	 */
	public static DashboardCommandModel doneCommand(ReactDashboardControl dashboard) {
		return new DashboardCommandModel("dashboardDone", I18NConstants.DASHBOARD_DONE,
			Icons.DASHBOARD_DONE, dashboard,
			ctx -> dashboard.exitEditMode(),
			d -> d.isEditMode());
	}

	/**
	 * Attaches to the dashboard's state change listener.
	 */
	public void attach() {
		_dashboard.addEditModeListener(_dashboardListener);
	}

	/**
	 * Detaches from the dashboard.
	 */
	public void detach() {
		_dashboard.removeEditModeListener(_dashboardListener);
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
		return ResourcesModule.getInstance()
			.getBundle(Locale.getDefault())
			.getString(_labelKey);
	}

	@Override
	public ThemeImage getImage() {
		return _image;
	}

	@Override
	public String getPlacement() {
		return PLACEMENT_TOOLBAR;
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
	public HandlerResult executeCommand(ReactContext context) {
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
