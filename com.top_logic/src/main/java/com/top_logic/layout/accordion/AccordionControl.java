/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accordion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.selection.SingleSelectVetoListener;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreePath;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Control} displaying a single-selection tree where a node is expanded if it's on the path
 * from the root to the selected node.
 * 
 * <p>
 * In this view, the root node is never displayed.
 * </p>
 * 
 * @param <N>
 *        The node type of the underlying tree model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AccordionControl<N> extends AbstractControlBase implements SingleSelectionListener, TreeModelListener {

	/**
	 * CSS class of the control element (type class).
	 */
	public static final String ROOT_CSS_CLASS = "accRoot";

	/**
	 * CSS class of the container element of each node (surrounding the node entry and its potential
	 * children).
	 */
	public static final String NODE_CSS_CLASS = "accNode";

	/**
	 * CSS class of the visible atomic node entry element with optional icon and label.
	 */
	public static final String ENTRY_CSS_CLASS = "accEntry";

	/**
	 * Additional CSS class for an {@link #ENTRY_CSS_CLASS} element that is not a leaf node.
	 */
	public static final String CONTAINER_CSS_CLASS = "accContainer";

	/**
	 * Additional CSS class for an {@link #ENTRY_CSS_CLASS} element that is no the path to the
	 * selected node.
	 */
	public static final String SELECTED_CSS_CLASS = "accSelected";

	/**
	 * CSS class for the icon element of a node entry.
	 */
	public static final String ICON_CSS_CLASS = "accIcon";

	/**
	 * CSS class for the container element of the children of a non-leaf node.
	 */
	private static final String CHILDREN_CSS_CLASS = "accChildren";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new SelectNode());

	private final TLTreeModel<N> _tree;

	private final SingleSelectionModel _selection;

	private SingleSelectVetoListener _selectionVeto;

	private final ResourceProvider _resources;

	private final BidiMap<String, N> _nodeById = new BidiHashMap<>();

	/**
	 * The node that was selected before the selection changed.
	 * 
	 * <p>
	 * If this property is set, the UI selection is invalid and must be updated.
	 * </p>
	 */
	private N _formerlySelectedObject;

	/**
	 * The new pending selection that is not yet propagated to the UI.
	 */
	private N _newlySelectedObject;

	/**
	 * Roots of sub-trees that need to be repainted.
	 */
	private Set<N> _invalidNodes = new HashSet<>();

	/**
	 * Creates a {@link AccordionControl}.
	 * 
	 * @param tree
	 *        The {@link TLTreeModel} to display.
	 * @param selection
	 *        A selection model to decide the selection and expansion state of the tree nodes.
	 * @param resources
	 *        {@link ResourceProvider} to provide the display information for the nodes of the given
	 *        tree model.
	 */
	public AccordionControl(TLTreeModel<N> tree,
			SingleSelectionModel selection, ResourceProvider resources) {
		super(COMMANDS);
		_tree = tree;
		_selection = selection;
		_resources = resources;
	}

	@Override
	public TLTreeModel<N> getModel() {
		return _tree;
	}

	/**
	 * The {@link SingleSelectVetoListener} that must approve new selections.
	 */
	public SingleSelectVetoListener getSelectionVeto() {
		return _selectionVeto;
	}

	/**
	 * @see #getSelectionVeto()
	 */
	public void setSelectionVeto(SingleSelectVetoListener selectionVeto) {
		_selectionVeto = selectionVeto;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		_tree.addTreeModelListener(this);
		_selection.addSingleSelectionListener(this);
	}

	@Override
	protected void internalDetach() {
		_selection.removeSingleSelectionListener(this);
		_tree.removeTreeModelListener(this);
		_nodeById.clear();

		super.internalDetach();
	}

	@Override
	protected String getTypeCssClass() {
		return ROOT_CSS_CLASS;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		writeControlContents(context, out);

		out.endTag(DIV);
	}

	private void writeControlContents(DisplayContext context, TagWriter out) throws IOException {
		List<N> selectionPath;
		N selected = selection();
		selectionPath = createPathTo(selected);

		writeRootNode(context, out, selectionPath, 0);
	}

	private List<N> createPathTo(N selected) {
		if (selected != null) {
			return createPathToNode(selected);
		} else {
			return Collections.emptyList();
		}
	}

	private List<N> createPathToNode(N selected) {
		List<N> selectionPath;
		selectionPath = new ArrayList<>();

		N root = _tree.getRoot();
		N ancestor = selected;
		while (true) {
			N parent = _tree.getParent(ancestor);

			if (parent == null) {
				// Reached the root node.
				if (ancestor != root) {
					// The selection was not part of the model at all, drop path.
					selectionPath = Collections.emptyList();
				}
				break;
			}

			// The root node is not added to the list.
			selectionPath.add(ancestor);

			ancestor = parent;
		}

		// Convert path to root to the selection path starting with the node of level 1
		// (directly below the root node).
		Collections.reverse(selectionPath);
		return selectionPath;
	}

	private void writeRootNode(DisplayContext context, TagWriter out, List<N> selectionPath,
			int pathIndex) throws IOException {
		// No not write the node itself.
		List<? extends N> children = _tree.getChildren(_tree.getRoot());
		for (N child : children) {
			writeNode(context, out, child, selectionPath, pathIndex);
		}
	}

	private void writeChildren(DisplayContext context, TagWriter out, N parent, List<? extends N> selectionPath,
			int pathIndex) throws IOException {
		List<? extends N> children = _tree.getChildren(parent);
		if (!children.isEmpty()) {
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, CHILDREN_CSS_CLASS);
			out.endBeginTag();
			{
				for (N child : children) {
					writeNode(context, out, child, selectionPath, pathIndex);
				}
			}
			out.endTag(DIV);
		}
	}

	void writeNode(DisplayContext context, TagWriter out, N node, List<? extends N> selectionPath,
			int pathIndex) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, nodeID(node));
		out.writeAttribute(CLASS_ATTR, NODE_CSS_CLASS);
		out.endBeginTag();

		writeNodeContents(context, out, node, selectionPath, pathIndex);

		out.endTag(DIV);
	}

	private void writeNodeContents(DisplayContext context, TagWriter out, N node, List<? extends N> selectionPath,
			int pathIndex) throws IOException {
		Object selectedNode = pathIndex < selectionPath.size() ? selectionPath.get(pathIndex) : null;
		boolean selected = node == selectedNode;

		out.beginBeginTag(HTMLConstants.ANCHOR);
		writeNodeAttributes(context, out, node, selected);
		out.endBeginTag();
		{
			ThemeImage image = _resources.getImage(node, selected ? Flavor.EXPANDED : Flavor.DEFAULT);
			if (image != null) {
				image.writeWithCss(context, out, ICON_CSS_CLASS);
			}

			out.writeText(_resources.getLabel(node));
		}
		out.endTag(HTMLConstants.ANCHOR);

		if (selected) {
			writeChildren(context, out, node, selectionPath, pathIndex + 1);
		}
	}

	private void writeNodeAttributes(DisplayContext context, TagWriter out, N child, boolean selected)
			throws IOException {
		out.beginCssClasses();
		out.write(ENTRY_CSS_CLASS);
		if (selected) {
			out.write(SELECTED_CSS_CLASS);
		}
		if (!_tree.isLeaf(child)) {
			out.write(CONTAINER_CSS_CLASS);
		}
		out.endCssClasses();

		out.beginAttribute(ONCLICK_ATTR);
		writeOnClickNode(out, child);
		out.endAttribute();

		// No link navigation.
		out.writeAttribute(HREF_ATTR, "#");

		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, _resources.getTooltip(child));
	}

	private void writeOnClickNode(TagWriter out, N child) throws IOException {
		out.write("return services.form.AccordionControl.select(");
		out.writeJsString(getID());
		out.write(",");
		out.writeJsString(nodeID(child));
		out.write(");");
	}

	private String nodeID(N node) {
		String result = _nodeById.getKey(node);
		if (result == null) {
			result = getScope().getFrameScope().createNewID();
			_nodeById.put(result, node);
		}
		return result;
	}

	private static class SelectNode extends ControlCommand {

		private static final String COMMAND_ID = "select";

		/**
		 * Creates a {@link SelectNode}.
		 */
		public SelectNode() {
			super(COMMAND_ID);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.ACCORDION_CONTROL_SELECT;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			String nodeID = (String) arguments.get("nodeID");
			if (nodeID == null) {
				return HandlerResult.error(I18NConstants.ERROR_NODE_NOT_FOUND);
			}

			return ((AccordionControl<?>) control).selectNodeID(nodeID);
		}

	}

	HandlerResult selectNodeID(String nodeID) {
		N node = _nodeById.get(nodeID);
		if (node == null) {
			return HandlerResult.error(I18NConstants.ERROR_NODE_NOT_FOUND);
		}

		return select(node);
	}

	HandlerResult select(final N node) {
		try {
			if (_selectionVeto != null) {
				_selectionVeto.checkVeto(_selection, node, false);
			}

			_selection.setSingleSelection(TreePath.fromNode(_tree, node));
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					return select(node);
				}
			});
			ex.process(getWindowScope());
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
		if (_formerlySelectedObject == null) {
			_formerlySelectedObject = selectedNode(formerlySelectedObject);
		}
		_newlySelectedObject = selectedNode(selectedObject);
	}

	private N selectedNode(Object untypedPath) {
		TreePath path = (TreePath) untypedPath;
		if (path == null) {
			return null;
		}
		return path.toNode(_tree);
	}

	@SuppressWarnings("unchecked")
	private N node(Object untypedNode) {
		return (N) untypedNode;
	}

	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		N node = node(evt.getNode());
		switch (evt.getType()) {
			case TreeModelEvent.BEFORE_STRUCTURE_CHANGE:
			case TreeModelEvent.NODE_CHANGED:
				_invalidNodes.add(node);
				break;
			case TreeModelEvent.BEFORE_NODE_REMOVE:
			case TreeModelEvent.AFTER_NODE_ADD:
				_invalidNodes.add(_tree.getParent(node));
				break;
		}
	}

	@Override
	protected boolean hasUpdates() {
		return selectionChanged() || nodesInvalidated();
	}

	private boolean nodesInvalidated() {
		return !_invalidNodes.isEmpty();
	}

	private boolean selectionChanged() {
		return _formerlySelectedObject != null || _newlySelectedObject != null;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		if (selectionChanged()) {
			validateSelection(actions);
		}
		if (nodesInvalidated()) {
			validateNodes(actions);
		}
	}

	private void validateNodes(UpdateQueue actions) {
		List<N> selectionPath = createPathTo(selection());

		for (N node : _invalidNodes) {
			if (!_tree.containsNode(node)) {
				// Cannot repaint an invalid node.
				continue;
			}

			if (isParentRepainted(node)) {
				continue;
			}

			int selectionIndex = selectionPath.indexOf(node);
			if (selectionIndex >= 0) {
				// The repainted subtree contains the selected node.
				repaintNode(actions, node, selectionPath, selectionIndex);
			} else {
				repaintNode(actions, node, Collections.<N> emptyList(), 0);
			}
		}

		_invalidNodes.clear();
	}

	private boolean isParentRepainted(N node) {
		N anchestor = _tree.getParent(node);
		while (anchestor != null) {
			if (_invalidNodes.contains(anchestor)) {
				return true;
			}
			anchestor = _tree.getParent(anchestor);
		}
		return false;
	}

	private N selection() {
		return selectedNode(_selection.getSingleSelection());
	}

	private void validateSelection(UpdateQueue actions) {
		List<N> oldPath = createPathTo(_formerlySelectedObject);
		List<N> newPath = createPathTo(_newlySelectedObject);

		_formerlySelectedObject = null;
		_newlySelectedObject = null;

		// Find first index that differs.
		int n = 0;
		int cnt = Math.min(oldPath.size(), newPath.size());
		for (; n < cnt; n++) {
			if (oldPath.get(n) != newPath.get(n)) {
				break;
			}
		}

		// Remove invalid nodes that are repainted anyway.
		for (int i = n, oldCnt = oldPath.size(); i < oldCnt; i++) {
			_invalidNodes.remove(oldPath.get(i));
		}
		for (int i = n, newCnt = newPath.size(); i < newCnt; i++) {
			_invalidNodes.remove(newPath.get(i));
		}

		if (cnt > 0 && n == cnt) {
			// One of the nodes to update is an ancestor of the other one. Only repaint the top-most
			// node.
			if (n == oldPath.size()) {
				repaintNode(actions, oldPath.get(n - 1), newPath, n - 1);
				return;
			}
			else if (n == newPath.size()) {
				repaintNode(actions, newPath.get(n - 1), newPath, n - 1);
				return;
			}
		}

		// Repaint both top-level nodes, if they exist.
		if (n < oldPath.size()) {
			repaintNode(actions, oldPath.get(n), Collections.<N> emptyList(), 0);
		}
		if (n < newPath.size()) {
			repaintNode(actions, newPath.get(n), newPath, n);
		}

	}

	private void repaintNode(UpdateQueue actions, final N node, final List<N> selectionPath,
			final int pathIndex) {
		actions.add(new ElementReplacement(nodeID(node), new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				writeNode(context, out, node, selectionPath, pathIndex);
			}
		}));
	}

}
