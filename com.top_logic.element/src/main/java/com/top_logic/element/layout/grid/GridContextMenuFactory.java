/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.element.layout.grid.GridComponent.*;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.component.factory.SelectableContextMenuFactory;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.table.tree.SelectSubtree;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.TreeSelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link SelectableContextMenuFactory} that maps grid rows to business objects.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Grid context menu commands")
public class GridContextMenuFactory<C extends GridContextMenuFactory.Config<?>>
		extends SelectableContextMenuFactory<C> {

	/**
	 * Configuration options for {@link GridContextMenuFactory}.
	 */
	public interface Config<I extends GridContextMenuFactory<?>> extends SelectableContextMenuFactory.Config<I> {
		/**
		 * IDs of commands that should be displayed in the context menu of the tree table.
		 * 
		 * <p>
		 * The commands must be registered in the application configuration at the service
		 * {@link CommandHandlerFactory}.
		 * </p>
		 */
		@FormattedDefault(SelectSubtree.SELECT_SUBTREE_ID + ", " + SelectSubtree.DESELECT_SUBTREE_ID)
		@Format(CommaSeparatedStrings.class)
		List<String> getCommandIds();
	}

	/**
	 * Creates a {@link GridContextMenuFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GridContextMenuFactory(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public ContextMenuProvider createContextMenuProvider(LayoutComponent component) {
		return new Provider(component, getConfig().getCommandIds());
	}

	/**
	 * {@link ContextMenuProvider} created by {@link GridContextMenuFactory}.
	 */
	protected class Provider extends SelectableContextMenuFactory<C>.Provider {

		private List<String> _commandIds;

		/**
		 * Creates a {@link Provider}.
		 */
		public Provider(LayoutComponent component, List<String> commandIds) {
			super(component);
			_commandIds = commandIds;
		}

		@Override
		protected List<CommandModel> createButtons(Object directTarget, Object model, Map<String, Object> arguments) {
			List<CommandModel> buttons = super.createButtons(directTarget, model, arguments);

			LayoutComponent component = getComponent();
			if (component instanceof TableDataOwner table) {
				SelectionModel<?> selectionModel = table.getTableData().getSelectionModel();
				if (selectionModel instanceof TreeSelectionModel) {
					for (String commandId : _commandIds) {
						addTreeButton(buttons, component, arguments, commandId);
					}
				}
			}

			return buttons;
		}

		private void addTreeButton(List<CommandModel> buttons, LayoutComponent component,
				Map<String, Object> arguments, String id) {
			CommandHandler handler = CommandHandlerFactory.getInstance().getHandler(id);
			if (handler != null) {
				CommandModel commandModel = CommandModelFactory.commandModel(handler, component, arguments);
				buttons.add(commandModel);
			}
		}

		@Override
		protected Object mapContextObject(Object obj) {
			GridComponent grid = (GridComponent) getComponent();
			return getRowObject(grid.getHandler().getGridRow(obj));
		}
	}

}
