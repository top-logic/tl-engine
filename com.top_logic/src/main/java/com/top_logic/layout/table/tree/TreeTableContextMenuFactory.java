/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.component.factory.SelectableContextMenuFactory;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.TreeSelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link SelectableContextMenuFactory} mapping node objects to their respective business objects.
 * 
 * @implNote This factory is used by default to create the context menu of a
 *           {@link TreeTableComponent}.
 * @see TreeTableContextMenuProvider.Config#getContextMenuFactory()
 * @see TLTreeNode#getBusinessObject()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeTableContextMenuFactory<C extends TreeTableContextMenuFactory.Config<?>>
		extends SelectableContextMenuFactory<C> {

	/**
	 * Creates a {@link TreeTableContextMenuFactory}.
	 */
	public TreeTableContextMenuFactory(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public ContextMenuProvider createContextMenuProvider(LayoutComponent component) {
		return new Provider(component);
	}

	/**
	 * {@link SelectableContextMenuFactory} wrapping node objects to their respective business
	 * objects.
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

			LayoutComponent component = getComponent();
			if (component instanceof TableDataOwner table) {
				SelectionModel<?> selectionModel = table.getTableData().getSelectionModel();
				if (selectionModel instanceof TreeSelectionModel) {
					addTreeButton(buttons, component, arguments, SelectSubtree.SELECT_SUBTREE_ID);
					addTreeButton(buttons, component, arguments, SelectSubtree.DESELECT_SUBTREE_ID);
				}
			}

			return buttons;
		}

		private void addTreeButton(List<CommandModel> buttons, LayoutComponent component, Map<String, Object> arguments,
				String id) {
			CommandHandler handler =
				CommandHandlerFactory.getInstance().getHandler(id);
			if (handler != null) {
				CommandModel commandModel = CommandModelFactory.commandModel(handler, component, arguments);
				buttons.add(commandModel);
			}
		}

		@Override
		protected Object mapContextObject(Object object) {
			if (object instanceof TLTreeNode) {
				return ((TLTreeNode<?>) object).getBusinessObject();
			}

			return null;
		}
	}

}
