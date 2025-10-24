/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.element.layout.grid.GridComponent.*;
import static com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil.*;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
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
public class GridContextMenuFactory<C extends GridContextMenuFactory.Config<?>>
		extends SelectableContextMenuFactory<C> {

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
		return new Provider(component);
	}

	/**
	 * {@link ContextMenuProvider} created by {@link GridContextMenuFactory}.
	 */
	protected class Provider extends SelectableContextMenuFactory<C>.Provider {

		/**
		 * Creates a {@link Provider}.
		 */
		public Provider(LayoutComponent component) {
			super(component);
		}

		@Override
		protected List<CommandModel> createButtons(Object directTarget, Object model, Map<String, Object> arguments) {
			List<CommandModel> buttons = super.createButtons(directTarget, model, arguments);

			Map<String, Object> treeArgs = createArguments(directTarget, directTarget);

			LayoutComponent component = getComponent();
			if (component instanceof TableDataOwner table) {
				SelectionModel<?> selectionModel = table.getTableData().getSelectionModel();
				if (selectionModel instanceof TreeSelectionModel treeSelect) {
					addTreeButton(buttons, component, treeArgs, SelectSubtree.SELECT_SUBTREE_ID);
					addTreeButton(buttons, component, treeArgs, SelectSubtree.DESELECT_SUBTREE_ID);
				}
			}

			return buttons;
		}

		private void addTreeButton(List<CommandModel> buttons, LayoutComponent component,
				Map<String, Object> treeArgs, String id) {
			CommandHandler handler =
				CommandHandlerFactory.getInstance().getHandler(id);
			if (handler != null) {
				CommandModel commandModel = CommandModelFactory.commandModel(handler, component, treeArgs);
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
