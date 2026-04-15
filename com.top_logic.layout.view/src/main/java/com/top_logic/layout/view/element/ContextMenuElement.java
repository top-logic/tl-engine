/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.command.ContextMenuRegionControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ContextMenuContribution;
import com.top_logic.layout.view.command.ContextMenuOpener;
import com.top_logic.layout.view.command.ViewCommandModel;

/**
 * A {@link CommandScopeElement} that exposes its contained commands as entries of a context menu
 * rather than toolbar buttons.
 *
 * <p>
 * Commands declared inside a {@code <context-menu>} element with
 * {@link CommandModel#PLACEMENT_CONTEXT_MENU placement="contextMenu"} are collected into a single
 * {@link ContextMenuContribution} bound to an implicit {@code contextTarget} channel (or to the
 * channel referenced via the optional {@code input} attribute). The element's chrome is a
 * {@link ContextMenuRegionControl} that relays {@code openContextMenu} client events to the
 * enclosing frame's {@link ContextMenuOpener}.
 * </p>
 */
public class ContextMenuElement extends CommandScopeElement {

	/**
	 * Default channel name used when no explicit {@code input} reference is configured.
	 */
	public static final String DEFAULT_TARGET_CHANNEL = "contextTarget";

	/**
	 * Configuration for {@link ContextMenuElement}.
	 */
	@TagName("context-menu")
	public interface Config extends CommandScopeElement.Config {

		@Override
		@ClassDefault(ContextMenuElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/**
		 * Optional reference to an existing channel that carries the context-menu target value.
		 *
		 * <p>
		 * When unset, a new {@link #DEFAULT_TARGET_CHANNEL} channel is registered on the current
		 * context and used as the target channel of the built
		 * {@link ContextMenuContribution}.
		 * </p>
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final Config _config;

	/**
	 * Creates a new {@link ContextMenuElement}.
	 */
	public ContextMenuElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ContextMenuOpener opener = context.getContextMenuOpener();
		if (opener == null) {
			throw new IllegalStateException(
				"No ContextMenuOpener is available in the current context; "
					+ "<context-menu> must be nested inside a frame that provides one.");
		}

		ViewChannel targetChannel = resolveOrRegisterTargetChannel(context);

		// Phase 1: Build command models.
		List<ViewCommandModel> commandModels = buildCommandModels(context);

		// Phase 2: Create command scope and derived context.
		CommandScope scope = new CommandScope(commandModels);
		ViewContext derivedContext = context.withCommandScope(scope);

		// Phase 3: Create child content.
		ReactControl content = createContent(derivedContext);

		// Phase 4: Collect context-menu commands into a single contribution.
		List<CommandModel> contextMenuCommands = new ArrayList<>();
		for (CommandModel cmd : scope.getAllCommands()) {
			if (CommandModel.PLACEMENT_CONTEXT_MENU.equals(cmd.getPlacement())) {
				contextMenuCommands.add(cmd);
			}
		}
		ContextMenuContribution contribution = new ContextMenuContribution(targetChannel, contextMenuCommands);

		// Phase 5: Create the region chrome wrapping the content.
		ContextMenuRegionControl region = new ContextMenuRegionControl(context, content, contribution, opener);

		// Phase 6: Lazy attach on render, cleanup on dispose.
		region.addBeforeWriteAction(() -> {
			for (ViewCommandModel model : commandModels) {
				model.attach();
			}
		});
		region.addCleanupAction(() -> {
			for (ViewCommandModel model : commandModels) {
				model.detach();
			}
		});

		return region;
	}

	private ViewChannel resolveOrRegisterTargetChannel(ViewContext context) {
		ChannelRef inputRef = _config.getInput();
		if (inputRef != null) {
			return context.resolveChannel(inputRef);
		}
		ViewChannel channel = new DefaultViewChannel(DEFAULT_TARGET_CHANNEL);
		context.registerChannel(DEFAULT_TARGET_CHANNEL, channel);
		return channel;
	}

	/**
	 * Unused for {@link ContextMenuElement}; {@link #createControl(ViewContext)} is overridden to
	 * produce a non-toolbar chrome control directly.
	 */
	@Override
	protected com.top_logic.layout.react.control.ToolbarControl createChromeControl(ViewContext context,
			ReactControl content) {
		throw new UnsupportedOperationException(
			"ContextMenuElement does not use a ToolbarControl chrome; createControl() is overridden.");
	}
}
