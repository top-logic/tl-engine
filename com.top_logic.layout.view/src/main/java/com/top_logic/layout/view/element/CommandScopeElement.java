/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CliqueRegistry;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ToolbarBuilder;
import com.top_logic.layout.view.command.ViewCommandModel;

/**
 * Base class for {@link UIElement}s that establish a {@link CommandScope} and render placed
 * commands as structured toolbars.
 *
 * <p>
 * Subclasses create their specific chrome (panel, window, etc.). This class adds, on top of
 * {@link CommandCarrierElement}:
 * </p>
 * <ul>
 * <li>Creating a {@link CommandScope} and derived {@link ViewContext}</li>
 * <li>Building clique-grouped toolbars for {@link CommandPlacement#TOOLBAR} and
 * {@link CommandPlacement#BUTTON_BAR} placed commands via {@link ToolbarBuilder}</li>
 * <li>Rebuilding the toolbars when the set of commands changes (implicit commands)</li>
 * </ul>
 *
 * @implNote Subclasses implement
 *           {@link #createChromeControl(ViewContext, ReactControl, ReactToolbarControl, ReactToolbarControl)}.
 */
public abstract class CommandScopeElement extends CommandCarrierElement {

	/**
	 * Configuration for {@link CommandScopeElement}.
	 *
	 * <p>
	 * Commands with {@link CommandPlacement#TOOLBAR toolbar} placement are automatically
	 * rendered in the element's toolbar. Commands are available to child elements via the command
	 * scope.
	 * </p>
	 */
	public interface Config extends CommandCarrierElement.Config {

		/** Configuration name for {@link #getCommandDisplay()}. */
		String COMMAND_DISPLAY = "command-display";

		/**
		 * How this scope's buttons display icon and label, e.g. {@code icon-only} for compact icon
		 * buttons whose labels become tooltips.
		 *
		 * <p>
		 * Applies to every button rendered from this scope - explicitly configured commands as
		 * well as commands contributed by child elements (e.g. a form's edit and save buttons). A
		 * command's own display setting takes precedence. If not set, buttons show the icon (when
		 * configured) together with the label.
		 * </p>
		 */
		@Name(COMMAND_DISPLAY)
		@Nullable
		@NullDefault
		ButtonDisplayMode getCommandDisplay();
	}

	private final ButtonDisplayMode _commandDisplay;

	/**
	 * Creates a new {@link CommandScopeElement}.
	 */
	protected CommandScopeElement(InstantiationContext context, Config config) {
		super(context, config);
		_commandDisplay = config.getCommandDisplay();
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

		// Phase 4: Build clique-grouped toolbars from placed commands. The controls are always
		// created (even when empty) so implicit commands added later have a target for the
		// reactive rebuild.
		CliqueRegistry registry = new CliqueRegistry();
		ReactToolbarControl toolbar =
			ToolbarBuilder.buildOrEmpty(context, scope, CommandPlacement.TOOLBAR, registry, _commandDisplay);
		ReactToolbarControl buttonBar =
			ToolbarBuilder.buildOrEmpty(context, scope, CommandPlacement.BUTTON_BAR, registry, _commandDisplay);

		// Phase 5: Let subclass create the chrome control.
		ToolbarControl chrome = createChromeControl(derivedContext, content, toolbar, buttonBar);

		// Phase 6: Rebuild toolbars when implicit commands change. Groups are replaced in place so
		// the existing toolbar controls keep their SSE registration.
		scope.addListener(() -> {
			toolbar.replaceGroups(
				ToolbarBuilder.buildOrEmpty(context, scope, CommandPlacement.TOOLBAR, registry, _commandDisplay));
			buttonBar.replaceGroups(
				ToolbarBuilder.buildOrEmpty(context, scope, CommandPlacement.BUTTON_BAR, registry,
					_commandDisplay));
		});

		// Phase 7: Lazy attach on render, cleanup on dispose.
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
	 * @param toolbar
	 *        The clique-grouped toolbar for {@link CommandPlacement#TOOLBAR} commands (empty
	 *        if there are none).
	 * @param buttonBar
	 *        The clique-grouped button bar for {@link CommandPlacement#BUTTON_BAR} commands
	 *        (empty if there are none).
	 * @return The chrome control (panel, window, etc.).
	 */
	protected abstract ToolbarControl createChromeControl(ViewContext context, ReactControl content,
			ReactToolbarControl toolbar, ReactToolbarControl buttonBar);
}
