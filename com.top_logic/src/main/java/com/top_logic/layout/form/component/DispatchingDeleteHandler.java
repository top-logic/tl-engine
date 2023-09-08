/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandHandler} that executes the delete command of an object as defined by the default
 * view of the object.
 * 
 * @see MainLayout#findDefaultFor(Object)
 * @see EditComponent#getDeleteCommandHandlerName()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Delete handler of default view")
public final class DispatchingDeleteHandler extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link DispatchingDeleteHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault("class.com.top_logic.layout.form.values.edit.editor.I18NConstants.DELETE_OBJECT")
		ResKey getResourceKey();

		@Override
		@FormattedDefault("theme:ICONS_DELETE_MENU")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:ICONS_DELETE_MENU_DISABLED")
		ThemeImage getDisabledImage();

		@Override
		@FormattedDefault(CommandHandlerFactory.DELETE_CLIQUE)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.DELETE_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault(TARGET_SELECTION_SELF)
		ModelSpec getTarget();

	}

	/**
	 * Creates a {@link DispatchingDeleteHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DispatchingDeleteHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		Boolean beforeDialogClose = displayContext.set(AbstractDeleteCommandHandler.PREVENT_DIALOG_CLOSE, Boolean.TRUE);
		try {
			if (model instanceof Set) {
				return deleteAll(displayContext, component, (Set<?>) model, arguments);
			}
			return delete(displayContext, component, model, arguments);
		} finally {
			displayContext.set(AbstractDeleteCommandHandler.PREVENT_DIALOG_CLOSE, beforeDialogClose);
		}
	}

	private HandlerResult deleteAll(DisplayContext displayContext, LayoutComponent component, Set<?> objects,
			Map<String, Object> arguments) {
		switch (objects.size()) {
			case 0:
				return HandlerResult.DEFAULT_RESULT;
			case 1:
				return delete(displayContext, component, objects.iterator().next(), arguments);
			default:
				break;
		}

		HandlerResult handleCommand = new HandlerResult();

		// Find common handler for all objects. This is the normal case.
		CommandHandler deleteCommand = null;
		for (Object object : objects) {
			CommandHandler deleteCommandForObject = findDeleteCommand(component, object);
			if (deleteCommand == null) {
				deleteCommand = deleteCommandForObject;
			} else if (deleteCommand != deleteCommandForObject) {
				// At least two objects use different handler. Partitioning is required.
				deleteCommand = null;
				break;
			}
		}
		if (deleteCommand != null) {
			// All objects have the same delete handler
			handleCommand.appendResult(deleteCommand.handleCommand(displayContext, component, objects, arguments));
		} else {
			// Not all objects have the same delete handler
			List<Object> toDelete = new ArrayList<>();
			CommandHandler currentHandler = null;
			for (Object object : objects) {
				CommandHandler nextHandler = findDeleteCommand(component, object);
				if (nextHandler == null) {
					// Can not delete object without delete command
					return HandlerResult.error(I18NConstants.ERROR_DELETE_NOT_POSSIBLE);
				}
				if (currentHandler != nextHandler && currentHandler != null) {
					handleCommand
						.appendResult(currentHandler.handleCommand(displayContext, component, toDelete, arguments));
					toDelete.clear();
				}
				toDelete.add(object);
				currentHandler = nextHandler;
			}
			if (!toDelete.isEmpty() && currentHandler != null) {
				handleCommand
					.appendResult(currentHandler.handleCommand(displayContext, component, toDelete, arguments));
			}
		}
		return handleCommand;
	}

	private HandlerResult delete(DisplayContext displayContext, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		CommandHandler deleteCommand = findDeleteCommand(component, model);
		if (deleteCommand == null) {
			return HandlerResult.error(I18NConstants.ERROR_DELETE_NOT_POSSIBLE);
		}
		return deleteCommand.handleCommand(displayContext, component, model, arguments);
	}

	private CommandHandler findDeleteCommand(LayoutComponent aComponent, Object model) {
		LayoutComponent defaultView = aComponent.getMainLayout().findDefaultFor(model);
		if (!(defaultView instanceof EditComponent)) {
			return null;
		}

		String commandId = ((EditComponent) defaultView).getDeleteCommandHandlerName();
		CommandHandler result = defaultView.getCommandById(commandId);
		if (result instanceof DispatchingDeleteHandler) {
			return null;
		}

		return result;
	}

	@Override
	public ExecutabilityRule createExecutabilityRule() {
		return (component, model, arguments) -> {
			List<CommandHandler> commandHandlers = findCommands(component, model);
			if (commandHandlers.contains(null)) {
				return ExecutableState.createDisabledState(I18NConstants.ERROR_DELETE_NOT_POSSIBLE);
			}
			return combineExecutabilities(component, model, arguments, commandHandlers);
		};
	}

	private List<CommandHandler> findCommands(LayoutComponent component, Object model) {
		if (model == null) {
			return emptyList();
		}
		if (model instanceof Collection) {
			return findCommands(component, (Collection<?>) model);
		}
		return singletonList(findDeleteCommand(component, model));
	}

	private List<CommandHandler> findCommands(LayoutComponent component, Collection<?> models) {
		List<CommandHandler> commandHandlers = new ArrayList<>();
		for (Object object : models) {
			commandHandlers.add(findDeleteCommand(component, object));
		}
		return commandHandlers;
	}

	private ExecutableState combineExecutabilities(LayoutComponent component, Object model,
			Map<String, Object> arguments, List<CommandHandler> commandHandlers) {
		if (commandHandlers.isEmpty()) {
			return ExecutableState.NO_EXEC_NO_MODEL;
		}
		ExecutableState state = ExecutableState.EXECUTABLE;
		for (CommandHandler commandHandler : commandHandlers) {
			state = state.combine(commandHandler.isExecutable(component, model, arguments));
		}
		return state;
	}

	@Override
	protected ResKey getDefaultConfirmKey(LayoutComponent component, Map<String, Object> arguments,
			Object targetModel) {
		return CommandHandlerUtil.defaultDeletionConfirmKey(targetModel);
	}

}
