/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.factory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ComponentContextMenuFactory} that also adds commands of slave components to context menu
 * entries.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectableContextMenuFactory<C extends SelectableContextMenuFactory.Config<?>>
		extends ComponentContextMenuFactory<C> {

	/**
	 * Creates a {@link SelectableContextMenuFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SelectableContextMenuFactory(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public ContextMenuProvider createContextMenuProvider(LayoutComponent component) {
		return new Provider(component);
	}

	/**
	 * {@link ContextMenuProvider} created by {@link SelectableContextMenuFactory}.
	 */
	protected class Provider extends ComponentContextMenuFactory<C>.Provider {

		/**
		 * Creates a {@link Provider}.
		 */
		public Provider(LayoutComponent component) {
			super(component);
		}

		@Override
		public boolean hasContextMenu(Object obj) {
			// Cannot be computed per object in reasonable time.
			return true;
		}

		@Override
		protected List<CommandModel> createButtons(Object model, Map<String, Object> arguments) {
			List<CommandModel> buttons = super.createButtons(model, arguments);

			LayoutComponent self = getComponent();
			buttons.addAll(
				self.getSlaves().stream()
					.filter(component -> acceptSlave(component, model))
					.flatMap(component -> component.getCommands().stream()
						.filter(this::acceptSlaveCommand)
						.map(command -> CommandModelFactory.commandModel(command, component, arguments)))
					.collect(Collectors.toList()));

			return buttons;
		}

		@Override
		protected boolean acceptComponentCommand(CommandHandler command) {
			return ContextMenuUtil.notHidden(command) && command.operatesOn(SelectionChannel.NAME);
		}

		/**
		 * Whether a given slave component is considered for the given model.
		 */
		protected boolean acceptSlave(LayoutComponent component, Object model) {
			return component.isVisible() && component.supportsModel(model);
		}

		/**
		 * Whether a command of a slave component is accepted.
		 */
		protected boolean acceptSlaveCommand(CommandHandler command) {
			return ContextMenuUtil.notHidden(command) && command.operatesOn(ModelChannel.NAME);
		}
	}

}