/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui.handler;

import static com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.graphic.flow.callback.DiagramHandler;
import com.top_logic.graphic.flow.server.script.FlowFactory;
import com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder.Config.HandlerDefinition;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.AcceptableClassifiers;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link DiagramHandler} that creates a custom context menu for some diagram region.
 * 
 * @see FlowFactory#contextMenu(com.top_logic.graphic.flow.data.Box,
 *      com.top_logic.graphic.flow.callback.DiagramContextMenuProvider, String, Object)
 */
public class ContextMenuHandler extends AbstractConfiguredInstance<ContextMenuHandler.Config<?>>
		implements DiagramHandler, DiagramContextMenuProviderSPI {

	/**
	 * Configuration options for {@link ContextMenuHandler}.
	 */
	@DisplayOrder({
		Config.NAME_ATTRIBUTE,
		Config.COMMANDS,
	})
	public interface Config<I extends ContextMenuHandler> extends HandlerDefinition<I> {
		/**
		 * @see #getCommands()
		 */
		String COMMANDS = "commands";

		/**
		 * Commands for the custom context menu.
		 */
		@Name(COMMANDS)
		@DefaultContainer
		@Options(fun = InAppImplementations.class)
		@AcceptableClassifiers("contextMenu")
		List<CommandHandler.ConfigBase<? extends CommandHandler>> getCommands();
	}

	private LayoutComponent _component;
	private final List<CommandHandler> _handlers;

	/**
	 * Creates a {@link ContextMenuHandler}.
	 */
	public ContextMenuHandler(InstantiationContext context, Config<?> config) {
		super(context, config);

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);

		CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
		List<CommandHandler> handlers = new ArrayList<>();
		for (CommandHandler.ConfigBase<? extends CommandHandler> handlerConfig : getConfig().getCommands()) {
			CommandHandler handler = factory.getCommand(context, handlerConfig);
			handlers.add(handler);
		}
		_handlers = handlers;
	}

	@Override
	public boolean hasContextMenu(Object obj) {
		return true;
	}

	@Override
	public Menu getContextMenu(Object directTarget, Object model) {
		List<CommandModel> buttons =
			ContextMenuUtil.toButtons(_component, createArguments(directTarget, model), _handlers);
		Menu result = ContextMenuUtil.toContextMenu(buttons);
		String title = MetaLabelProvider.INSTANCE.getLabel(model);
		if (!StringServices.isEmpty(title)) {
			result.setTitle(Fragments.text(title));
		}
		return result;
	}

}
