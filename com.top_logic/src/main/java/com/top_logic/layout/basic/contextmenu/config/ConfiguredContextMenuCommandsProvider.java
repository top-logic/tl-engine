/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.config;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.provider.LabelProviderService.ContextMenuConfig;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ContextMenuCommandsProvider} that allows adding custom commands to globally configured
 * ones.
 * 
 * @see LabelProviderService#getContextCommands(Object)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredContextMenuCommandsProvider<C extends ConfiguredContextMenuCommandsProvider.Config<?>>
		extends AbstractConfiguredInstance<C> implements ContextMenuCommandsProvider {

	/**
	 * Configuration options for {@link ConfiguredContextMenuCommandsProvider}.
	 */
	public interface Config<I extends ConfiguredContextMenuCommandsProvider<?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getEntries()
		 */
		String ENTRIES = "entries";

		/**
		 * @see #getOverride()
		 */
		String OVERRIDE = "override";

		/**
		 * Further commands added to the context menu.
		 */
		@Name(ENTRIES)
		@EntryTag("switch")
		@DefaultContainer
		List<ContextMenuConfig> getEntries();
	
		/**
		 * Whether to ignore globally configured commands.
		 * 
		 * @see LabelProviderService#getContextCommands(Object)
		 */
		@Name(OVERRIDE)
		boolean getOverride();
	}

	private final ContextMenuCommandsProvider _globalCommands;

	private final ContextMenuCommandsProvider _customCommands;

	/**
	 * Creates a {@link ConfiguredContextMenuCommandsProvider}.
	 */
	public ConfiguredContextMenuCommandsProvider(InstantiationContext context, C config) {
		super(context, config);

		NoContextMenuCommands contextMenuCommands = TypedConfigUtil.createInstance(NoContextMenuCommands.Config.class);
		_globalCommands = config.getOverride() ? contextMenuCommands : LabelProviderService.getInstance();
		_customCommands = config.getEntries().isEmpty() ? contextMenuCommands
			: new LabelProviderService.ContextCommandRegistry(context, config.getEntries());
	}

	@Override
	public boolean hasContextMenuCommands(Object obj) {
		return _globalCommands.hasContextMenuCommands(obj) || _customCommands.hasContextMenuCommands(obj);
	}

	@Override
	public List<CommandHandler> getContextCommands(Object obj) {
		List<CommandHandler> generalCommands = _globalCommands.getContextCommands(obj);
		List<CommandHandler> customCommands = _customCommands.getContextCommands(obj);
		return join(generalCommands, customCommands);
	}

	private static <T> List<T> join(List<T> generalCommands, List<T> customCommands) {
		if (generalCommands.isEmpty()) {
			return customCommands;
		}
		if (customCommands.isEmpty()) {
			return generalCommands;
		}
		List<T> result = new ArrayList<>(generalCommands.size() + customCommands.size());
		result.addAll(generalCommands);
		result.addAll(customCommands);
		return result;
	}

}
