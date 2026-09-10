/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.control.nav.ReactAppBarControl;
import com.top_logic.layout.react.control.nav.ReactAppBarControl.AppBarVariant;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.util.Resources;

/**
 * UIElement that wraps {@link ReactAppBarControl}.
 *
 * <p>
 * Renders a top-level application bar with a title and optional variant/color configuration.
 * Commands configured in the {@code <commands>} section with {@link CommandPlacement#TOOLBAR
 * TOOLBAR} placement are automatically rendered as trailing action buttons in the app bar.
 * </p>
 */
@InApp
public class AppBarElement implements UIElement {

	/**
	 * Configuration for {@link AppBarElement}.
	 */
	@TagName("app-bar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(AppBarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getVariant()}. */
		String VARIANT = "variant";

		/** Configuration name for {@link #getCommands()}. */
		String COMMANDS = "commands";

		/** Configuration name for {@link #getLeading()}. */
		String LEADING = "leading";

		/** Configuration name for {@link #getTrailing()}. */
		String TRAILING = "trailing";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * The bar title.
		 */
		@Name(TITLE)
		@Nullable
		ResKey getTitle();

		/**
		 * The visual variant.
		 */
		@Name(VARIANT)
		AppBarVariant getVariant();

		/**
		 * Commands to render as trailing action buttons in the app bar.
		 *
		 * <p>
		 * Commands with {@link CommandPlacement#TOOLBAR TOOLBAR} placement are rendered as
		 * icon/text buttons in the app bar's trailing actions area.
		 * </p>
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();

		/**
		 * Arbitrary inline children rendered between the title and the actions area.
		 *
		 * <p>
		 * Typically a single {@code <slot>} that lets deeper views project content into the app
		 * bar (e.g. a breadcrumb or a context-specific control group), but any UIElement can be
		 * placed here.
		 * </p>
		 */
		@Name(CHILDREN)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getChildren();

		/**
		 * Elements rendered in the leading slot before the title (e.g. a mobile drawer-toggle
		 * button contributed via {@code <slot name="appbar-leading"/>}).
		 *
		 * <p>
		 * Typically a single {@code <slot>} placeholder; if multiple elements are configured, they
		 * render in the order given inside the leading area.
		 * </p>
		 */
		@Name(LEADING)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getLeading();

		/**
		 * Elements closing the bar, right of the actions area (e.g. the account area).
		 *
		 * <p>
		 * This is the right end of the bar, past the buttons of toolbar-placed commands. Several
		 * elements render side by side in the order given.
		 * </p>
		 */
		@Name(TRAILING)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getTrailing();
	}

	private final ResKey _title;

	private final AppBarVariant _variant;

	private final List<ViewCommand> _commands;

	private final List<ViewCommand.Config> _commandConfigs;

	private final List<UIElement> _children;

	private final List<UIElement> _leading;

	private final List<UIElement> _trailing;

	/**
	 * Creates a new {@link AppBarElement} from configuration.
	 */
	@CalledByReflection
	public AppBarElement(InstantiationContext context, Config config) {
		_title = config.getTitle();
		_variant = config.getVariant();

		_commands = new ArrayList<>();
		_commandConfigs = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewCommand> cmdConfig : config.getCommands()) {
			ViewCommand cmd = context.getInstance(cmdConfig);
			if (cmd != null) {
				_commands.add(cmd);
				if (cmdConfig instanceof ViewCommand.Config) {
					_commandConfigs.add((ViewCommand.Config) cmdConfig);
				}
			}
		}

		_children = new ArrayList<>(config.getChildren().size());
		for (PolymorphicConfiguration<? extends UIElement> childConfig : config.getChildren()) {
			_children.add(context.getInstance(childConfig));
		}

		_leading = new ArrayList<>(config.getLeading().size());
		for (PolymorphicConfiguration<? extends UIElement> leadingConfig : config.getLeading()) {
			_leading.add(context.getInstance(leadingConfig));
		}

		_trailing = new ArrayList<>(config.getTrailing().size());
		for (PolymorphicConfiguration<? extends UIElement> trailingConfig : config.getTrailing()) {
			_trailing.add(context.getInstance(trailingConfig));
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		String title = _title != null ? Resources.getInstance().getString(_title) : "";

		// Build command models.
		List<ViewCommandModel> commandModels = buildCommandModels(context);

		// Use parent scope when available so that commands contributed by descendants
		// (e.g. form edit commands, dashboard layout edit) surface in the app bar.
		// Fall back to a private scope if the app bar is used standalone.
		CommandScope parentScope = context.getScope(CommandScope.class);
		CommandScope scope;
		ViewContext derivedContext;
		if (parentScope != null) {
			for (ViewCommandModel model : commandModels) {
				parentScope.addCommand(model);
			}
			scope = parentScope;
			derivedContext = context;
		} else {
			scope = new CommandScope(commandModels);
			derivedContext = context.withScope(CommandScope.class, scope);
		}

		// Build inline children (e.g. a <slot> for content projected by descendant views).
		List<ReactControl> childControls = new ArrayList<>(_children.size());
		for (int i = 0; i < _children.size(); i++) {
			ViewContext childContext = derivedContext.withChildSlotPath(Integer.toString(i));
			childControls.add((ReactControl) _children.get(i).createControl(childContext));
		}

		// Build the two ends of the bar.
		ReactControl leadingControl = buildBarEnd(derivedContext, _leading, "leading");
		ReactControl trailingControl = buildBarEnd(derivedContext, _trailing, "trailing");

		// Create the app bar control.
		ReactAppBarControl appBar = new ReactAppBarControl(derivedContext, title, _variant, leadingControl,
			List.of(), childControls, trailingControl);

		// Sync toolbar-placed commands as action buttons.
		Map<CommandModel, ReactButtonControl> actionButtons = new HashMap<>();
		syncActionButtons(appBar, derivedContext, scope, actionButtons);
		scope.addListener(() -> syncActionButtons(appBar, derivedContext, scope, actionButtons));

		// Register cleanup for command model lifecycle. When using a shared scope,
		// also remove our contributed commands.
		final CommandScope cleanupScope = scope;
		final boolean usingSharedScope = (parentScope != null);
		appBar.addCleanupAction(() -> {
			if (usingSharedScope) {
				for (ViewCommandModel model : commandModels) {
					cleanupScope.removeCommand(model);
				}
			}
			for (ViewCommandModel model : commandModels) {
				model.detach();
			}
		});

		return appBar;
	}

	/**
	 * Composes the elements placed at one end of the bar into a single control.
	 *
	 * <p>
	 * An app bar is a horizontal bar, so several elements at one end - a drawer toggle and the
	 * account area, say - stand side by side and centered on the bar's height, rather than stacked
	 * the way the generic content combination would arrange them.
	 * </p>
	 *
	 * @param elements
	 *        The elements configured for that end; empty yields {@code null}, i.e. no area at all.
	 * @param slot
	 *        Names the end in the child slot path, keeping the two ends' children distinguishable.
	 */
	private static ReactControl buildBarEnd(ViewContext context, List<UIElement> elements, String slot) {
		if (elements.isEmpty()) {
			return null;
		}
		List<ReactControl> controls = new ArrayList<>(elements.size());
		for (int i = 0; i < elements.size(); i++) {
			ViewContext elementContext = context.withChildSlotPath(slot + "-" + i);
			controls.add((ReactControl) elements.get(i).createControl(elementContext));
		}
		return controls.size() == 1
			? controls.get(0)
			: new ReactStackControl(context, StackDirection.ROW, StackGap.COMPACT, StackAlign.CENTER,
				false, controls);
	}

	private List<ViewCommandModel> buildCommandModels(ViewContext context) {
		List<ViewCommandModel> models = new ArrayList<>();
		for (int i = 0; i < _commands.size() && i < _commandConfigs.size(); i++) {
			ViewCommand cmd = _commands.get(i);
			ViewCommand.Config cmdConfig = _commandConfigs.get(i);

			ViewCommandModel model = ViewCommandModel.forCommand(context, cmd, cmdConfig);
			model.attach(context.getModelScope());
			models.add(model);
		}
		return models;
	}

	private static void syncActionButtons(ReactAppBarControl appBar, ViewContext context,
			CommandScope scope, Map<CommandModel, ReactButtonControl> actionButtons) {
		List<CommandModel> currentCommands = scope.getAllCommands();

		actionButtons.entrySet().removeIf(entry -> {
			if (!currentCommands.contains(entry.getKey())) {
				appBar.removeToolbarButton(entry.getValue());
				return true;
			}
			return false;
		});

		for (CommandModel model : currentCommands) {
			if (!actionButtons.containsKey(model)
				&& model.getPlacement() == CommandPlacement.TOOLBAR) {
				ReactButtonControl button = new ReactButtonControl(context, model);
				appBar.addToolbarButton(button);
				actionButtons.put(model, button);
			}
		}
	}
}
