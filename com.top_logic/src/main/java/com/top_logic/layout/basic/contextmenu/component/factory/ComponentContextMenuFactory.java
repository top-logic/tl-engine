/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.factory;

import static com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ContextMenuProvider} that creates context menus from component commands.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComponentContextMenuFactory<C extends ComponentContextMenuFactory.Config<?>>
		extends TypeBasedContextMenuFactory<C> {

	/**
	 * Creates a {@link ComponentContextMenuFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ComponentContextMenuFactory(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public ContextMenuProvider createContextMenuProvider(LayoutComponent component) {
		return new Provider(component);
	}

	/**
	 * {@link ContextMenuProvider} created by {@link ComponentContextMenuFactory}.
	 */
	protected class Provider extends TypeBasedContextMenuFactory<C>.Provider {

		private List<? extends CommandHandler> _componentCommands;

		/**
		 * Creates a {@link Provider}.
		 */
		public Provider(LayoutComponent component) {
			super(component);
		}

		@Override
		public boolean hasContextMenu(Object obj) {
			return super.hasContextMenu(obj) || hasComponentCommands();
		}

		/**
		 * Whether component commands can be displayed in the context menu.
		 */
		private boolean hasComponentCommands() {
			return !componentCommands().isEmpty();
		}

		private List<? extends CommandHandler> componentCommands() {
			// Must be lazy, since component commands are only available if components are fully
			// resolved.
			if (_componentCommands == null) {
				_componentCommands =
					CollectionUtil.nonNull(getComponent().getCommands()).stream().filter(this::acceptComponentCommand)
						.collect(Collectors.toList());
			}
			return _componentCommands;
		}

		@Override
		protected List<CommandModel> createButtons(Object model, Map<String, Object> arguments) {
			List<CommandModel> buttons = super.createButtons(model, arguments);
			List<CommandModel> componentButtons = createComponentCommandButtons(model, arguments);
			return CollectionUtil.concatNew(buttons, componentButtons);
		}

		/**
		 * Context menu entries from the component's commands, see {@link #componentCommands()}.
		 * 
		 * @param model
		 *        See {@link #createButtons(Object, Map)}.
		 * @param arguments
		 *        See {@link #createButtons(Object, Map)}.
		 */
		protected final List<CommandModel> createComponentCommandButtons(Object model,
				Map<String, Object> arguments) {
			return toButtons(getComponent(), arguments, componentCommands());
		}

		/**
		 * Whether a given command is accepted.
		 */
		protected boolean acceptComponentCommand(CommandHandler command) {
			return ContextMenuUtil.notHidden(command) && command.operatesOn(ModelChannel.NAME);
		}
	}

}