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

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TreeProperty;
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
import com.top_logic.layout.view.command.ViewCommand;
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

		/** Configuration name for {@link #getGroups()}. */
		String GROUPS = "groups";

		/**
		 * Further groups of entries, each set off from the one before it by a separator.
		 *
		 * <p>
		 * The entries declared in {@link #getCommands()} are the first group; these follow it, and
		 * the {@link #getCommandSources() sources} follow them. A group none of whose entries are
		 * currently available is left out together with its separator, so a menu never opens on a
		 * dividing line with nothing beneath it.
		 * </p>
		 */
		@Name(GROUPS)
		@EntryTag("group")
		List<CommandGroup> getGroups();

		/**
		 * A group of menu entries, separated from the groups around it.
		 */
		interface CommandGroup extends ConfigurationItem {

			/** Configuration name for {@link #getCommands()}. */
			String COMMANDS = "commands";

			/**
			 * The commands offered as this group's entries.
			 */
			@Name(COMMANDS)
			@DefaultContainer
			@EntryTag("command")
			@TreeProperty
			@Options(fun = AllInAppImplementations.class)
			List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
		}

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

	private final List<Group> _groups;

	private final List<ViewCommandSource> _commandSources;

	/**
	 * Creates a new {@link AbstractMenuElement}.
	 */
	protected AbstractMenuElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
		_groups = new ArrayList<>(config.getGroups().size());
		for (Config.CommandGroup groupConfig : config.getGroups()) {
			_groups.add(new Group(context, groupConfig.getCommands()));
		}

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

		Consumer<Object> setter = target -> targetChannel.set(target);
		Supplier<Object> targetSupplier = () -> targetChannel.get();

		// One contribution per group of entries, because the opener draws a separator between
		// contributions: the commands written in the view are one group, each configured group is
		// another, and so is each source. A group that currently offers nothing is skipped along
		// with its separator.
		List<ContextMenuContribution> contributions =
			new ArrayList<>(1 + _groups.size() + _commandSources.size());
		contributions.add(new ContextMenuContribution(setter, menuCommands(commandModels)));

		List<ViewCommandModel> groupModels = new ArrayList<>();
		for (Group group : _groups) {
			List<ViewCommandModel> models = group.buildModels(context);
			groupModels.addAll(models);
			contributions.add(new ContextMenuContribution(setter, menuCommands(models)));
		}

		for (ViewCommandSource source : _commandSources) {
			contributions.add(new ContextMenuContribution(setter, source.getCommands(context)));
		}

		MenuRegionControl region = new MenuRegionControl(context, content, contributions, targetSupplier,
			opener, getTrigger());

		// Lazy attach on render, cleanup on dispose. The grouped entries are commands of this
		// element like the ungrouped ones, so their models follow the same lifecycle.
		List<ViewCommandModel> allModels = new ArrayList<>(commandModels);
		allModels.addAll(groupModels);
		registerLifecycle(allModels, region);

		return region;
	}

	/**
	 * The commands of one configured group, kept beside their configurations so that their models
	 * can be built per rendering.
	 */
	private static class Group {

		private final List<ViewCommand> _commands = new ArrayList<>();

		private final List<ViewCommand.Config> _configs = new ArrayList<>();

		Group(InstantiationContext context, List<PolymorphicConfiguration<? extends ViewCommand>> configs) {
			for (PolymorphicConfiguration<? extends ViewCommand> cmdConfig : configs) {
				ViewCommand cmd = context.getInstance(cmdConfig);
				if (cmd != null) {
					_commands.add(cmd);
					if (cmdConfig instanceof ViewCommand.Config) {
						_configs.add((ViewCommand.Config) cmdConfig);
					}
				}
			}
		}

		List<ViewCommandModel> buildModels(ViewContext context) {
			return buildCommandModels(context, _commands, _configs);
		}
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
