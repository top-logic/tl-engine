/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * Turns configured {@link ViewCommand}s into the {@link ViewCommandModel}s a display builds its
 * buttons from, and ties their lifetime to that display.
 *
 * <p>
 * Used by everything that offers configured commands: the {@link UIElement} that declares a
 * {@code <commands>} section, and a control that lets its configuration contribute commands of its
 * own.
 * </p>
 *
 * @see ToolbarBuilder Arranging the resulting models into a toolbar.
 */
public class ViewCommands {

	/**
	 * Builds {@link ViewCommandModel}s for the given commands, resolving per-command input
	 * channels, executability rules and confirmations.
	 *
	 * @param context
	 *        The context the commands run in; decides which channels their configuration can
	 *        name.
	 * @param commands
	 *        The instantiated commands.
	 * @param commandConfigs
	 *        Their configurations, in the same order.
	 */
	public static List<ViewCommandModel> buildCommandModels(ViewContext context,
			List<ViewCommand> commands, List<ViewCommand.Config> commandConfigs) {
		List<ViewCommandModel> models = new ArrayList<>();
		for (int i = 0; i < commands.size() && i < commandConfigs.size(); i++) {
			ViewCommand cmd = commands.get(i);
			ViewCommand.Config cmdConfig = commandConfigs.get(i);

			models.add(ViewCommandModel.forCommand(context, cmd, cmdConfig));
		}
		return models;
	}

	/**
	 * Registers attach/detach hooks for the given command models, so that they follow their input -
	 * the channel value and the object it holds - while the host is displayed.
	 *
	 * @param context
	 *        The context whose {@link ViewContext#getModelScope() model scope} carries the object
	 *        observation; read when the host attaches.
	 * @param models
	 *        The command models to keep up to date.
	 * @param host
	 *        The control displaying the commands.
	 */
	public static void registerLifecycle(ViewContext context, List<ViewCommandModel> models, ReactControl host) {
		host.addAttachListener(() -> {
			for (ViewCommandModel model : models) {
				model.attach(context.getModelScope());
			}
		});
		host.addDetachListener(() -> {
			for (ViewCommandModel model : models) {
				model.detach();
			}
		});
	}

}
