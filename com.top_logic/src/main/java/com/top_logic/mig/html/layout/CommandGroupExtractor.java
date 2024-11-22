/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.NoProtocol;
import com.top_logic.basic.config.ConfigurationAccess;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.window.OpenWindowCommand;
import com.top_logic.layout.window.WindowTemplate;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CheckerProxyHandler;
import com.top_logic.tool.boundsec.CheckerProxyHandler.Config;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.CommandReferenceConfig;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Algorithm finding all relevant {@link BoundCommandGroup} references in a
 * {@link LayoutComponent.Config layout configuration}.
 */
public class CommandGroupExtractor {

	private final Set<BoundCommandGroup> _result = new HashSet<>();

	private static final Set<PropertyDescriptor> EXCLUDED;

	private LayoutComponent.Config _layoutConfig;

	static {
		HashSet<PropertyDescriptor> excluded = new HashSet<>();
		ConfigurationDescriptor subComponents = TypedConfiguration.getConfigurationDescriptor(SubComponentConfig.class);
		excluded.add(subComponents.getProperty(SubComponentConfig.COMPONENTS));

		ConfigurationDescriptor component = TypedConfiguration.getConfigurationDescriptor(LayoutComponent.Config.class);
		excluded.add(component.getProperty(LayoutComponent.XML_TAG_WINDOWS_NAME));
		excluded.add(component.getProperty(LayoutComponent.XML_TAG_DIALOGS_NAME));

		EXCLUDED = excluded;
	}

	/**
	 * Visits the given config to locate all command group usages.
	 */
	public CommandGroupExtractor visit(LayoutComponent.Config config) {
		_layoutConfig = config;
		
		// Implicit command group to show component.
		_result.add(SimpleBoundCommandGroup.READ);

		for (WindowTemplate.Config window : config.getWindows()) {
			extract(OpenWindowCommand.createWindowOpenHandler(window));
		}

		for (LayoutComponent.Config dialogRef : config.getDialogs()) {
			// Ignore error, because it is also reported during instantiation of the component.
			LayoutComponent.Config dialog = LayoutUtils.resolveComponentReference(NoProtocol.INSTANCE, dialogRef);
			if (dialog != null) {
				PolymorphicConfiguration<? extends CommandHandler> openHandler =
					OpenModalDialogCommandHandler.createDialogOpenHandler(NoProtocol.INSTANCE, config, dialog);
				if (openHandler != null) {
					extract(openHandler);
				}
			}
		}

		addIntrinsic(config);
		addAdditional(config);
		extract(config);
		return this;
	}

	private void extract(ConfigurationItem config) {
		if (config == null) {
			return;
		}

		if (config instanceof CommandHandler.Config handlerConfig) {
			addGroup(handlerConfig);
			return;
		} else if (config instanceof CommandReferenceConfig handlerConfig) {
			String handlerId = handlerConfig.getCommandId();
			addGroup(registeredHandler(handlerId));
			return;
		} else if (config instanceof CheckerProxyHandler.Config handlerConfig) {
			Config checkerConfig = handlerConfig;
			ComponentName checkerName = checkerConfig.getName();
			if (checkerName != null && !checkerName.equals(_layoutConfig.getName())) {
				/* The given layout component is not used for security checking, but the component
				 * with the checker name. Therefore the command group of the command is not relevant
				 * for the given component. */
				return;
			}
			extract(checkerConfig.getCommand());
			return;
		}

		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (EXCLUDED.contains(property)) {
				continue;
			}

			switch (property.kind()) {
				case ARRAY: {
					Object value = config.value(property);
					if (value != null) {
						extract(property.getConfigurationAccess(), Arrays.asList((Object[]) value));
					}
					break;
				}
				case LIST: {
					Object value = config.value(property);
					if (value != null) {
						extract(property.getConfigurationAccess(), (Collection<?>) value);
					}
					break;
				}
				case MAP: {
					Object value = config.value(property);
					if (value != null) {
						extract(property.getConfigurationAccess(), ((Map<?, ?>) value).values());
					}
					break;
				}
				case ITEM: {
					Object value = config.value(property);
					extract(property.getConfigurationAccess().getConfig(value));
					break;
				}

				default:
					// Ignore.
			}

		}
	}

	private void extract(ConfigurationAccess configurationAccess, Collection<?> configs) {
		for (Object entry : configs) {
			extract(configurationAccess.getConfig(entry));
		}
	}

	/**
	 * The set of extracted {@link BoundCommandGroup}s.
	 */
	public Set<BoundCommandGroup> getResult() {
		return _result;
	}

	private void addAdditional(LayoutComponent.Config config) {
		Set<BoundCommandGroup> additionalGroups = new HashSet<>();
		config.addAdditionalCommandGroups(additionalGroups);
		_result.addAll(additionalGroups);
	}

	private void addIntrinsic(LayoutComponent.Config config) {
		SimpleCommandRegistry registry = new SimpleCommandRegistry();
		config.modifyIntrinsicCommands(registry);
		addIntrinsicGroups(registry.getButtons());
		addIntrinsicGroups(registry.getCommands());
	}

	private void addGroup(CommandHandler.Config handler) {
		CommandGroupReference group = handler.getGroup();
		if (group == null) {
			_result.add(SimpleBoundCommandGroup.READ);
		} else {
			BoundCommandGroup resolvedGroup = group.resolve();
			if (resolvedGroup != null) {
				_result.add(resolvedGroup);
			}
		}
	}

	private void addGroup(CommandHandler handler) {
		if (handler != null) {
			_result.add(handler.getCommandGroup());
		}
	}

	private void addIntrinsicGroups(List<String> commandIds) {
		commandIds.forEach(commandId -> addGroup(registeredHandler(commandId)));
	}

	private CommandHandler registeredHandler(String commandId) {
		return CommandHandlerFactory.getInstance().getHandler(commandId);
	}

}
