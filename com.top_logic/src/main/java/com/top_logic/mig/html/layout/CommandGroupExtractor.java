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

import com.top_logic.basic.NamedConstant;
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

	private static final Set<NamedConstant> EXCLUDED;

	private LayoutComponent.Config _layoutConfig;

	static {
		Set<NamedConstant> excluded = new HashSet<>();
		ConfigurationDescriptor subComponents = TypedConfiguration.getConfigurationDescriptor(SubComponentConfig.class);
		excluded.add(subComponents.getProperty(SubComponentConfig.COMPONENTS).identifier());

		ConfigurationDescriptor component = TypedConfiguration.getConfigurationDescriptor(LayoutComponent.Config.class);
		excluded.add(component.getProperty(LayoutComponent.XML_TAG_WINDOWS_NAME).identifier());
		excluded.add(component.getProperty(LayoutComponent.XML_TAG_DIALOGS_NAME).identifier());

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

	/**
	 * The set of extracted {@link BoundCommandGroup}s.
	 */
	public Set<BoundCommandGroup> getResult() {
		return _result;
	}

	private void extract(ConfigurationItem config) {
		if (config == null) {
			return;
		}

		if (config instanceof CommandHandler.Config handlerConfig) {
			addHandlerConfigGroup(handlerConfig);
			return;
		} else if (config instanceof CommandReferenceConfig ref) {
			addHandlerIdGroup(ref.getCommandId());
			return;
		} else if (config instanceof CheckerProxyHandler.Config proxy) {
			ComponentName checkerName = proxy.getName();
			if (checkerName != null && !checkerName.equals(_layoutConfig.getName())) {
				/* The given layout component is not used for security checking, but the component
				 * with the checker name. Therefore the command group of the command is not relevant
				 * for the given component. */
				return;
			}
			extract(proxy.getCommand());
			return;
		}

		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (EXCLUDED.contains(property.identifier())) {
				continue;
			}

			switch (property.kind()) {
				case ARRAY: {
					Object value = config.value(property);
					if (value != null) {
						extractAll(property.getConfigurationAccess(), Arrays.asList((Object[]) value));
					}
					break;
				}
				case LIST: {
					Object value = config.value(property);
					if (value != null) {
						extractAll(property.getConfigurationAccess(), (Collection<?>) value);
					}
					break;
				}
				case MAP: {
					Object value = config.value(property);
					if (value != null) {
						extractAll(property.getConfigurationAccess(), ((Map<?, ?>) value).values());
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

	private void extractAll(ConfigurationAccess configurationAccess, Collection<?> configs) {
		for (Object entry : configs) {
			extract(configurationAccess.getConfig(entry));
		}
	}

	private void addAdditional(LayoutComponent.Config config) {
		Set<BoundCommandGroup> additionalGroups = new HashSet<>();
		config.addAdditionalCommandGroups(additionalGroups);
		_result.addAll(additionalGroups);
	}

	private void addIntrinsic(LayoutComponent.Config config) {
		SimpleCommandRegistry registry = new SimpleCommandRegistry();
		config.modifyIntrinsicCommands(registry);
		addHandlerIdGroups(registry.getButtons());
		addHandlerIdGroups(registry.getCommands());
	}

	private void addHandlerConfigGroup(CommandHandler.Config handler) {
		CommandGroupReference group = handler.getGroup();
		if (group == null) {
			addGroup(SimpleBoundCommandGroup.READ);
		} else {
			BoundCommandGroup resolvedGroup = group.resolve();
			if (resolvedGroup != null) {
				addGroup(resolvedGroup);
			}
		}
	}

	private void addHandlerIdGroups(List<String> commandIds) {
		for (String commandId : commandIds) {
			addHandlerIdGroup(commandId);
		}
	}

	private void addHandlerIdGroup(String commandId) {
		CommandHandler handler = CommandHandlerFactory.getInstance().getHandler(commandId);
		addHandlerGroup(handler);
	}

	private void addHandlerGroup(CommandHandler handler) {
		if (handler != null) {
			addGroup(handler.getCommandGroup());
		}
	}

	private void addGroup(BoundCommandGroup group) {
		_result.add(group);
	}

}
