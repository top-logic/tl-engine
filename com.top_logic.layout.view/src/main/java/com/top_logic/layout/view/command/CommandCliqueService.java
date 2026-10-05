/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.layout.ToolbarGroupDisplay;
import com.top_logic.layout.view.command.CliqueRegistry.CliqueInfo;

/**
 * The cliques by which the toolbars of the views group their commands.
 *
 * <p>
 * Each {@link ViewCommand} names the clique it belongs to; all commands of a clique form one group
 * of the toolbar. The cliques configured here decide the order of these
 * groups and how each one is displayed: inline, or folded into a menu with a label and an icon.
 * An application adds a clique or relabels one by configuring an entry of the same name.
 * </p>
 *
 * <p>
 * A clique that is not configured is displayed inline after the configured ones.
 * </p>
 *
 * @implNote A command declares its clique in {@link ViewCommand.Config#getClique()}.
 */
@Label("Toolbar cliques")
public class CommandCliqueService extends ConfiguredManagedClass<CommandCliqueService.Config> {

	/**
	 * Configuration of the {@link CommandCliqueService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<CommandCliqueService> {

		/** Property name of {@link #getCliques()}. */
		String CLIQUES = "cliques";

		/**
		 * The cliques in the order their groups are displayed in a toolbar.
		 */
		@Name(CLIQUES)
		@Key(CliqueConfig.NAME)
		@EntryTag("clique")
		List<CliqueConfig> getCliques();

	}

	/**
	 * Definition of a single clique of toolbar commands.
	 */
	public interface CliqueConfig extends ConfigurationItem {

		/** Property name of {@link #getName()}. */
		String NAME = "name";

		/** Property name of {@link #getDisplay()}. */
		String DISPLAY = "display";

		/** Property name of {@link #getLabel()}. */
		String LABEL = "label";

		/** Property name of {@link #getIcon()}. */
		String ICON = "icon";

		/**
		 * The name of the clique, by which a command declares that it belongs to it.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * How the group of the clique's commands is displayed: inline, side by side with the other
		 * groups, or folded into a menu.
		 */
		@Name(DISPLAY)
		ToolbarGroupDisplay getDisplay();

		/**
		 * The label of the menu the commands are folded into.
		 *
		 * <p>
		 * Only a clique displayed as menu shows its label: as the text of the button opening the
		 * menu, or, if the clique has an icon, as its accessible name.
		 * </p>
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * The icon of the button opening the menu the commands are folded into.
		 *
		 * <p>
		 * Only a clique displayed as menu shows its icon.
		 * </p>
		 */
		@Name(ICON)
		ThemeImage getIcon();

	}

	private final CliqueRegistry _registry;

	/**
	 * Creates a {@link CommandCliqueService} from configuration.
	 */
	@CalledByReflection
	public CommandCliqueService(InstantiationContext context, Config config) {
		super(context, config);

		List<CliqueInfo> cliques = new ArrayList<>();
		for (CliqueConfig clique : config.getCliques()) {
			cliques.add(new CliqueInfo(clique.getName(), clique.getDisplay(), clique.getLabel(), clique.getIcon()));
		}
		_registry = new CliqueRegistry(cliques);
	}

	/**
	 * The configured cliques.
	 */
	public CliqueRegistry getRegistry() {
		return _registry;
	}

	/**
	 * The {@link CommandCliqueService} singleton.
	 */
	public static CommandCliqueService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link CommandCliqueService}.
	 */
	public static final class Module extends TypedRuntimeModule<CommandCliqueService> {

		/** Singleton {@link CommandCliqueService.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<CommandCliqueService> getImplementation() {
			return CommandCliqueService.class;
		}

	}

}
