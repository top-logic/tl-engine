/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.MenuRegionControl;
import com.top_logic.layout.view.command.MenuTrigger;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommandSource;

/**
 * Base for {@link UIElement}s that offer their commands as entries of a popup menu instead of
 * buttons.
 *
 * <p>
 * The element's children are its menu region: the DOM they render is what the user acts on to open
 * the menu. The commands become a single {@link ContextMenuContribution} bound to a target channel,
 * and the enclosing frame's {@link ContextMenuOpener} renders and dispatches them.
 * </p>
 *
 * @implNote A subclass contributes the two decisions that separate the concrete menus:
 *           {@link #getTrigger()} names the gesture opening the menu, and
 *           {@link #menuCommands(List)} picks which of the built models become entries.
 *
 * @see ContextMenuElement
 * @see MenuElement
 */
public abstract class AbstractMenuElement extends CommandCarrierElement {

	/**
	 * Default channel name used when no explicit {@code input} reference is configured.
	 */
	public static final String DEFAULT_TARGET_CHANNEL = "contextTarget";

	/**
	 * Configuration for {@link AbstractMenuElement}.
	 */
	public interface Config extends CommandCarrierElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/**
		 * Optional reference to an existing channel that carries the menu target value.
		 *
		 * <p>
		 * When unset, a new {@link AbstractMenuElement#DEFAULT_TARGET_CHANNEL} channel is
		 * registered on the current context and used as the target channel of the built
		 * {@link ContextMenuContribution}.
		 * </p>
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/** Configuration name for {@link #getCommandSources()}. */
		String COMMAND_SOURCES = "command-sources";

		/**
		 * Sources of further entries, whose commands are not written in the view but derived at
		 * runtime.
		 *
		 * <p>
		 * Their entries follow the ones declared in {@link #getCommands()}, each source's in the
		 * order it supplies them.
		 * </p>
		 */
		@Name(COMMAND_SOURCES)
		@EntryTag("command-source")
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewCommandSource>> getCommandSources();
	}

	private final Config _config;

	private final List<ViewCommandSource> _commandSources;

	/**
	 * Creates a new {@link AbstractMenuElement}.
	 */
	protected AbstractMenuElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
		_commandSources = new ArrayList<>(config.getCommandSources().size());
		for (PolymorphicConfiguration<? extends ViewCommandSource> sourceConfig : config.getCommandSources()) {
			ViewCommandSource source = context.getInstance(sourceConfig);
			if (source != null) {
				_commandSources.add(source);
			}
		}
	}

	/**
	 * The gesture on the region built from this element's children that opens the menu.
	 */
	protected abstract MenuTrigger getTrigger();

	/**
	 * The subset of this element's command models to offer as menu entries.
	 *
	 * @param models
	 *        All command models built from the configuration.
	 */
	protected abstract List<CommandModel> menuCommands(List<ViewCommandModel> models);

	@Override
	public IReactControl createControl(ViewContext context) {
		ContextMenuOpener opener = context.getContextMenuOpener();
		if (opener == null) {
			throw new IllegalStateException(
				"No ContextMenuOpener is available in the current context; "
					+ "a menu element must be nested inside a frame that provides one.");
		}

		ViewChannel targetChannel = resolveOrRegisterTargetChannel(context);

		List<ViewCommandModel> commandModels = buildCommandModels(context);

		ReactControl content = createRegionContent(context);

		List<CommandModel> entries = new ArrayList<>(menuCommands(commandModels));
		for (ViewCommandSource source : _commandSources) {
			entries.addAll(source.getCommands(context));
		}

		Consumer<Object> setter = target -> targetChannel.set(target);
		Supplier<Object> targetSupplier = () -> targetChannel.get();
		ContextMenuContribution contribution = new ContextMenuContribution(setter, entries);

		MenuRegionControl region = new MenuRegionControl(context, content, contribution, targetSupplier,
			opener, getTrigger());

		// Lazy attach on render, cleanup on dispose.
		registerLifecycle(commandModels, region);

		return region;
	}

	/**
	 * The single control built from this element's children, which becomes the menu region.
	 *
	 * <p>
	 * Defaults to the standard combination of children, which stacks several of them in a column.
	 * </p>
	 */
	protected ReactControl createRegionContent(ViewContext context) {
		return createContent(context);
	}

	/**
	 * All command models, unfiltered - for a menu whose commands are declared on the element itself
	 * and are therefore menu entries by construction.
	 */
	protected static List<CommandModel> allCommands(List<ViewCommandModel> models) {
		return new ArrayList<>(models);
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
}
