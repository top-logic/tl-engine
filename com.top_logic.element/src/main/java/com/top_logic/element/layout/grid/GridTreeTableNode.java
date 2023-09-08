/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.layout.tree.model.TLTreeModelUtil;

/**
 * {@link AbstractTreeTableNode} for {@link GridTreeTableModel}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GridTreeTableNode extends AbstractTreeTableNode<GridTreeTableNode> {

	/**
	 * Creates a {@link GridTreeTableNode}.
	 * 
	 * @see AbstractTreeTableNode#AbstractTreeTableNode(AbstractMutableTLTreeModel, AbstractTreeTableNode, Object)
	 */
	public GridTreeTableNode(
			AbstractMutableTLTreeModel<GridTreeTableNode> model,
			GridTreeTableNode parent, Object businessObject) {
		super(model, parent, businessObject);
	}

	@Override
	public String toString() {
		return NameBuilder.buildName(this, toStringBusinessObject());
	}

	private String toStringBusinessObject() {
		Object businessObject = getBusinessObject();
		if (businessObject instanceof FormGroup) {
			return Objects.toString(GridComponent.getRowObject((FormGroup) businessObject));
		}
		return Objects.toString(businessObject);
	}

	/**
	 * {@link ModelNamingScheme} for {@link GridTreeTableNode}s.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class GridTreeTableNodeNaming extends
			AbstractModelNamingScheme<GridTreeTableNode, GridTreeTableNodeNaming.GridNodeName> {

		/**
		 * {@link ModelName} of {@link GridTreeTableNode}s.
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		public interface GridNodeName extends ModelName {

			/**
			 * Reference to the context component.
			 */
			ModelName getComponent();
			
			/**
			 * @see #getComponent()
			 */
			void setComponent(ModelName value);

			/**
			 * Path from the root of a tree grid display to the referenced nodes
			 * in model names for business objects.
			 */
			List<ModelName> getPath();
			
			/**
			 * @see #getPath()
			 */
			void setPath(List<ModelName> value);

		}
		
		@Override
		public Class<GridNodeName> getNameClass() {
			return GridNodeName.class;
		}

		@Override
		public Class<GridTreeTableNode> getModelClass() {
			return GridTreeTableNode.class;
		}

		@Override
		public GridTreeTableNode locateModel(ActionContext context, GridNodeName name) {
			GridComponent grid = (GridComponent) context.resolve(name.getComponent());

			List<Object> path = context.resolveCollection(Object.class, name.getPath());
			AbstractTreeGridBuilder<?>.TreeGridHandler handler = (AbstractTreeGridBuilder.TreeGridHandler) grid._handler;
			GridTreeTableNode root = handler._treeModel.getRoot();
			
			GridTreeTableNode node = TLTreeModelUtil.findNode(root, path, false);
			
			return node;
		}

		@Override
		protected void initName(GridNodeName name, GridTreeTableNode model) {
			GridTreeTableModel treeModel = (GridTreeTableModel) model.getModel();
			GridComponent grid = treeModel.getGrid();
			
			name.setComponent(ModelResolver.buildModelName(grid));

			List<?> path = TLTreeModelUtil.createPathToRootUserObject(model);
			Collections.reverse(path);
			path = path.subList(1, path.size());
			name.setPath(ModelResolver.buildModelNames(path));
		}
	}
}
