/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CommandPlacement;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewCommandModel;

/**
 * Base class for {@link UIElement}s that establish a {@link CommandScope} and synchronize toolbar
 * buttons with a {@link ToolbarControl}.
 *
 * <p>
 * Subclasses implement {@link #createChromeControl(ViewContext, ReactControl)} to create their
 * specific chrome (panel, window, etc.). This class adds, on top of
 * {@link CommandCarrierElement}:
 * </p>
 * <ul>
 * <li>Creating a {@link CommandScope} and derived {@link ViewContext}</li>
 * <li>Synchronizing toolbar-placed commands as buttons on the chrome control</li>
 * </ul>
 */
public abstract class CommandScopeElement extends CommandCarrierElement {

	/**
	 * Configuration for {@link CommandScopeElement}.
	 *
	 * <p>
	 * Commands with {@link CommandPlacement#TOOLBAR TOOLBAR} placement are automatically rendered
	 * in the element's toolbar. Commands are available to child elements via the command scope.
	 * </p>
	 */
	public interface Config extends CommandCarrierElement.Config {
		// Inherits getCommands() from CommandCarrierElement.Config.
	}

	/**
	 * Creates a new {@link CommandScopeElement}.
	 */
	protected CommandScopeElement(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// Phase 1: Build command models.
		List<ViewCommandModel> commandModels = buildCommandModels(context);

		// Phase 2: Create command scope and derived context.
		CommandScope scope = new CommandScope(commandModels);
		ViewContext derivedContext = context.withCommandScope(scope);

		// Phase 3: Create child content controls.
		ReactControl content = createContent(derivedContext);

		// Phase 4: Let subclass create the chrome control.
		ToolbarControl chrome = createChromeControl(derivedContext, content);

		// Phase 5: Sync toolbar buttons.
		Map<CommandModel, ReactButtonControl> toolbarButtons = new HashMap<>();
		syncToolbarButtons(chrome, context, scope, toolbarButtons);
		scope.addListener(() -> syncToolbarButtons(chrome, context, scope, toolbarButtons));

		// Phase 6: Lazy attach on render, cleanup on dispose.
		registerLifecycle(commandModels, chrome);

		return chrome;
	}

	/**
	 * Creates the chrome control wrapping the given content.
	 *
	 * @param context
	 *        The derived view context (with command scope set).
	 * @param content
	 *        The child content control.
	 * @return The chrome control (panel, window, etc.) that provides toolbar button support.
	 */
	protected abstract ToolbarControl createChromeControl(ViewContext context, ReactControl content);

	private void syncToolbarButtons(ToolbarControl chrome, ViewContext context,
			CommandScope scope, Map<CommandModel, ReactButtonControl> toolbarButtons) {
		List<CommandModel> currentCommands = scope.getAllCommands();

		toolbarButtons.entrySet().removeIf(entry -> {
			if (!currentCommands.contains(entry.getKey())) {
				chrome.removeToolbarButton(entry.getValue());
				return true;
			}
			return false;
		});

		for (CommandModel model : currentCommands) {
			if (!toolbarButtons.containsKey(model)
				&& CommandModel.PLACEMENT_TOOLBAR.equals(model.getPlacement())) {
				ReactButtonControl button = new ReactButtonControl(context, model);
				chrome.addToolbarButton(button);
				toolbarButtons.put(model, button);
			}
		}
	}
}
