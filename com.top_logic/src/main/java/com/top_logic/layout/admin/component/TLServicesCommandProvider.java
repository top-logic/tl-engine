/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarChangeListener;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.layout.toolbar.ToolBarOwner;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.Resources;

/**
 * {@link ComponentResolver} that enables configuration of commands for different
 * {@link ManagedClass}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLServicesCommandProvider extends ComponentResolver implements ChannelListener, ToolBarChangeListener {

	/** Name of the {@link ToolBarGroup} which contains displays the configured commands. */
	private static final String SERVICE_CLASS_COMMANDS_TOOLBAR_GROUP = "serviceClassCommands";

	private Map<Class<? extends ManagedClass>, List<CommandHandler>> _handlersByClass;

	private List<CommandModel> _installedCommandModels = null;

	private List<CommandHandler> _installedCommands = Collections.emptyList();

	/**
	 * Configuration of the {@link TLServicesCommandProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Configuration of the commands for a given service.
		 * 
		 * <p>
		 * For a concrete {@link ManagedClass}, all commands configured for the concrete
		 * implementation or a super class are displayed.
		 * </p>
		 */
		@DefaultContainer
		@Key(CommandsByServiceClass.SERVICE_CLASS)
		@EntryTag("commands-by-service-class")
		Map<Class<? extends ManagedClass>, CommandsByServiceClass> getCommandsByServiceClasses();

	}

	/**
	 * Configuration of the commands for a specific {@link ManagedClass}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface CommandsByServiceClass extends ConfigurationItem {

		/** Configuration name for {@link #getCommands()}. */
		String COMMANDS_NAME = "commands";

		/** Configuration name for {@link #getServiceClass()}. */
		String SERVICE_CLASS = "service-class";

		/**
		 * Configuration of the commands for the specified {@link #getServiceClass() service}.
		 */
		/**
		 * Configuration of the commands for the specified {@link #getServiceClass() service}.
		 */
		@Name(COMMANDS_NAME)
		@EntryTag("command")
		@DefaultContainer
		List<CommandHandler.ConfigBase<? extends CommandHandler>> getCommands();

		/**
		 * {@link ManagedClass} for which {@link #getCommands()} are configured.
		 */
		@Mandatory
		@Name(SERVICE_CLASS)
		Class<? extends ManagedClass> getServiceClass();
	}

	@Override
	public void resolveComponent(InstantiationContext context, LayoutComponent component) {
		Config configuration = ApplicationConfig.getInstance().getConfig(Config.class);
		_handlersByClass = new HashMap<>();

		Map<Class<? extends ManagedClass>, CommandsByServiceClass> commandsByServiceClass =
			configuration.getCommandsByServiceClasses();
		for (Class<? extends ManagedClass> serviceClass : commandsByServiceClass.keySet()) {
			processServiceClass(_handlersByClass, serviceClass, context, commandsByServiceClass);
		}
		component.modelChannel().addListener(this);
		component.addListener(ToolBarOwner.TOOLBAR_PROPERTY, this);
	}

	private List<CommandHandler> processServiceClass(
			Map<Class<? extends ManagedClass>, List<CommandHandler>> handlersByClass,
			Class<? extends ManagedClass> serviceClass, InstantiationContext context,
			Map<Class<? extends ManagedClass>, CommandsByServiceClass> configuredCommands) {
		List<CommandHandler> commandsForService = handlersByClass.get(serviceClass);
		if (commandsForService == null) {
			commandsForService = createHandlers(context, serviceClass, configuredCommands);
			handlersByClass.put(serviceClass, commandsForService);
			do {
				Class<?> superclass = (serviceClass).getSuperclass();
				if (superclass == null) {
					break;
				}
				if (!ManagedClass.class.isAssignableFrom(superclass)) {
					break;
				}
				serviceClass = superclass.asSubclass(ManagedClass.class);
				CommandsByServiceClass tmp = configuredCommands.get(serviceClass);
				if (tmp != null) {
					List<CommandHandler> superClassCommands =
						processServiceClass(handlersByClass, serviceClass, context, configuredCommands);
					commandsForService.addAll(superClassCommands);
					break;
				}
			} while (true);
		}
		return commandsForService;
	}

	private List<CommandHandler> createHandlers(InstantiationContext context,
			Class<? extends ManagedClass> serviceClass,
			Map<Class<? extends ManagedClass>, CommandsByServiceClass> configuredCommands) {
		return configuredCommands.get(serviceClass).getCommands()
			.stream()
			.map(handlerConfig -> CommandHandlerFactory.getInstance().getCommand(context, handlerConfig))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
		LayoutComponent component = sender.getComponent();
		List<CommandHandler> newHandlers = getCommandHandlers(newValue);
		updateCommandHandlers(component, newHandlers);
		ToolBar toolBar = component.getToolBar();
		if (toolBar == null) {
			return;
		}
		ToolBarGroup serviceClassesGroup = toolBar.defineGroup(SERVICE_CLASS_COMMANDS_TOOLBAR_GROUP);
		removeCommandModels(serviceClassesGroup);

		List<CommandModel> commandModels = getCommandModels(component, newHandlers);
		commandModels.forEach(serviceClassesGroup::addButton);
		_installedCommandModels = commandModels;
	}

	private void updateCommandHandlers(LayoutComponent component, List<CommandHandler> newValue) {
		_installedCommands.stream().map(CommandHandler::getID).forEach(component::unregisterCommand);
		newValue.forEach(component::registerCommand);
		_installedCommands = newValue;
	}

	private List<CommandModel> getCommandModels(LayoutComponent component, List<CommandHandler> handlers) {
		List<CommandModel> commandModels = new ArrayList<>(handlers.size());
		Resources res = Resources.getInstance();
		for (CommandHandler handler : handlers) {
			commandModels.add(component.createCommandModel(res, handler, Collections.emptyMap()));
		}
		return commandModels;
	}

	private List<CommandHandler> getCommandHandlers(Object newValue) {
		if (!(newValue instanceof BasicRuntimeModule<?>)) {
			return Collections.emptyList();
		}
		Class<? extends ManagedClass> implementation = ((BasicRuntimeModule<?>) newValue).getImplementation();
		ServiceConfiguration<? extends ManagedClass> currentConfig;
		try {
			currentConfig = ApplicationConfig.getInstance().getServiceConfiguration(implementation);
		} catch (ConfigurationException ex) {
			InfoService.showError(
				I18NConstants.ERROR_GETTING_SERVICE_CONFIGURATION__SERVICE
					.fill(MetaLabelProvider.INSTANCE.getLabel(newValue)),
				ex.getErrorKey());
			return Collections.emptyList();
		}

		Class<?> serviceClass = currentConfig.getImplementationClass();
		while (serviceClass != null) {
			List<CommandHandler> handlers = _handlersByClass.get(serviceClass);
			if (handlers != null) {
				return handlers;
			}
			serviceClass = serviceClass.getSuperclass();
		}

		return Collections.emptyList();

	}

	@Override
	public void notifyToolbarChange(ToolBarOwner sender, ToolBar oldValue, ToolBar newValue) {
		if (oldValue != null) {
			removeCommandModels(oldValue.removeGroup(SERVICE_CLASS_COMMANDS_TOOLBAR_GROUP));
		}
		if (newValue != null) {
			ToolBarGroup toolbarGroup = newValue.defineGroup(SERVICE_CLASS_COMMANDS_TOOLBAR_GROUP);
			if (_installedCommandModels != null) {
				_installedCommandModels.forEach(toolbarGroup::addButton);
			} else {
				LayoutComponent toolBarOwner = (LayoutComponent) sender;
				List<CommandHandler> handlers = getCommandHandlers(toolBarOwner.getModel());
				List<CommandModel> commandModels = getCommandModels(toolBarOwner, handlers);
				commandModels.forEach(toolbarGroup::addButton);
				_installedCommandModels = commandModels;
			}
		} else {
			_installedCommandModels = null;
		}
	}

	private void removeCommandModels(ToolBarGroup toolbarGroup) {
		if (toolbarGroup != null && _installedCommandModels != null) {
			_installedCommandModels.forEach(toolbarGroup::removeButton);
		}
	}

}

