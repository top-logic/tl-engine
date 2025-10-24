/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.Display;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.CommandHandlerUtil;

/**
 * Utilities for creating context menus from various sources.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContextMenuUtil {

	/**
	 * Command argument that provides the object on which a context menu was opened, even if the
	 * context menu was opened on a multi-selection where the target model of the command would
	 * normally be the whole set of selected objects.
	 */
	public static String DIRECT_TARGET = "_directTarget";

	/**
	 * Creates a context menu from a collection of {@link CommandModel}s by grouping them by their
	 * command cliques.
	 * 
	 * @see CommandHandler#getClique()
	 */
	public static Menu toContextMenu(Collection<CommandModel> buttons) {
		return toContextMenu(buttons.stream());
	}

	/**
	 * Creates a context menu from a collection of {@link CommandModel}s by grouping them by their
	 * command cliques.
	 * 
	 * @see CommandHandler#getClique()
	 */
	public static Menu toContextMenu(Stream<CommandModel> buttonsStream) {
		return Menu.create(groupAndSort(buttonsStream).collect(Collectors.toList()));
	}

	/**
	 * Creates a stream based on the given buttons that delivers content grouped by their command
	 * cliques.
	 */
	public static Stream<List<? extends CommandModel>> groupAndSort(
			Stream<CommandModel> buttons) {
		return buttons
			.collect(Collectors.groupingBy(ContextMenuUtil::groupButtons, LinkedHashMap::new, Collectors.toList()))
			.entrySet()
			.stream()
			.sorted(CommandHandlerFactory.getInstance().getCliqueToolBarOrder())
			.map(Entry::getValue);
	}

	/**
	 * Creates {@link CommandModel}s for a given collection of {@link CommandHandler}s in the
	 * context of some {@link LayoutComponent}
	 * 
	 * @param component
	 *        The component the context of which the commands are executed.
	 * @param arguments
	 *        The encoded target model, see {@link #createArguments(Object, Object)}.
	 * @param commands
	 *        The commands to wrap.
	 */
	public static List<CommandModel> toButtons(LayoutComponent component, Map<String, Object> arguments,
			Collection<? extends CommandHandler> commands) {
		return toButtonsStream(component, arguments, commands).collect(Collectors.toList());
	}

	/**
	 * Creates {@link CommandModel}s for a given collection of {@link CommandHandler}s in the
	 * context of some {@link LayoutComponent}
	 * 
	 * @param component
	 *        The component the context of which the commands are executed.
	 * @param arguments
	 *        The encoded target model, see {@link #createArguments(Object, Object)}.
	 * @param commands
	 *        The commands to wrap.
	 */
	public static Stream<CommandModel> toButtonsStream(LayoutComponent component,
			Map<String, Object> arguments, Collection<? extends CommandHandler> commands) {
		return commands.stream().map(command -> CommandModelFactory.commandModel(command, component, arguments));
	}

	/**
	 * Grouping function of created context menu entries.
	 */
	private static String groupButtons(CommandModel button) {
		String clique =
			button instanceof ComponentCommandModel ? ((ComponentCommandModel) button).getCommandHandler().getClique()
				: CommandHandlerFactory.ADDITIONAL_GROUP;
		return CommandHandlerFactory.getInstance().getCliqueGroup(clique);
	}

	/**
	 * Wraps the given command target model into an arguments map for
	 * {@link #toButtons(LayoutComponent, Map, Collection)}.
	 * 
	 * <p>
	 * Only for cases, where a context menu is opened on a single object.
	 * </p>
	 * 
	 * @param model
	 *        The target model, on which a regular command would be invoked.
	 * 
	 * @see #createArguments(Object, Object) The command arguments to be used, when a context menu
	 *      is opened on a multi-selection.
	 */
	public static Map<String, Object> createSingleObjectArguments(Object model) {
		return createArguments(model, model);
	}

	/**
	 * Wraps the given command target model into an arguments map for
	 * {@link #toButtons(LayoutComponent, Map, Collection)}.
	 * 
	 * @param directTarget
	 *        The object on which the context menu was opened (even if the context menu was opened
	 *        on a multi-selection).
	 * @param model
	 *        The target model, on which a regular command would be invoked.
	 * 
	 * @see #createSingleObjectArguments(Object) To be used in cases, where a context menu is opened
	 *      on a single object.
	 */
	public static Map<String, Object> createArguments(Object directTarget, Object model) {
		HashMap<String, Object> result = new HashMap<>();
		result.put(DIRECT_TARGET, directTarget);
		result.put(CommandHandlerUtil.TARGET_MODEL_ARGUMENT, model);
		return result;
	}

	/**
	 * Whether the given {@link CommandHandler} is not configured to be hidden.
	 */
	public static boolean notHidden(CommandHandler command) {
		CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
		return factory.getDisplay(factory.getCliqueGroup(command.getClique())) != Display.HIDDEN;
	}

}
