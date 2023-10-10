/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accordion;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.link.AbstractLinkRenderer;
import com.top_logic.layout.basic.link.Link;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.selection.SingleSelectVetoListener;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreePath;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Control} allowing to select a single leaf node of some tree.
 * 
 * <p>
 * Initially, the root-level (all direct children of the tree's root node) are displayed. If the
 * clicked node is a leaf node, it is selected in the #getSelectionModel()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeNavigatorControl<N> extends AbstractControlBase implements SingleSelectionListener, TreeModelListener {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new SelectNode());

	final TLTreeModel<N> _tree;

	private final SingleSelectionModel _levelSelection;

	private final SingleSelectionModel _entrySelection;

	final ResourceProvider _resourceProvider;

	private final BidiMap<String, TreePath> _nodeById = new BidiHashMap<>();

	private SingleSelectVetoListener _vetoListener;

	/**
	 * Caches the selected node during rendering.
	 * 
	 * <p>
	 * This is a performance optimization that prevents resolving the selection {@link TreePath} to
	 * a node while rendering each entry.
	 * </p>
	 */
	N _selectedNodeCache;

	private AbstractLinkRenderer _entryRenderer = new AccordionEntryRenderer() {
		@Override
		protected <B> void writeLinkContent(DisplayContext context, TagWriter out, Link button) throws IOException {
			super.writeLinkContent(context, out, button);
			N node = node((((ForwardAccessor) button).getNode()));
			if (!_tree.isLeaf(node)) {
				Icons.FORWARD22.writeWithCss(context, out, "accMarker");
			}
		}
	};

	/**
	 * Creates a {@link TreeNavigatorControl}.
	 * 
	 * @param tree
	 *        See {@link #getTree()}.
	 * @param selectionModel
	 *        See {@link #getLeafSelectionModel()}.
	 * @param resourceProvider
	 *        See {@link #getResourceProvider()}.
	 */
	public TreeNavigatorControl(TLTreeModel<N> tree, SingleSelectionModel selectionModel,
			ResourceProvider resourceProvider) {
		super(COMMANDS);
		_tree = tree;
		_resourceProvider = resourceProvider;
		_levelSelection = new DefaultSingleSelectionModel(null);
		_entrySelection = selectionModel;
		_levelSelection.setSingleSelection(TreePath.fromNode(_tree, _tree.getRoot()));
	}

	@Override
	public TLTreeModel<N> getModel() {
		return _tree;
	}

	/**
	 * The displayed/navigated {@link TLTreeModel}.
	 */
	public TLTreeModel<N> getTree() {
		return _tree;
	}

	/**
	 * The {@link SelectionModel} containing the selected root node in the {@link #getTree()}.
	 */
	public SingleSelectionModel getLeafSelectionModel() {
		return _entrySelection;
	}

	/**
	 * The {@link SelectionModel} containing the parent of the displayed tree level.
	 */
	public SingleSelectionModel getLevelSelection() {
		return _levelSelection;
	}

	/**
	 * The {@link ResourceProvider} for displaying aspects of tree nodes of {@link #getTree()}.
	 */
	public ResourceProvider getResourceProvider() {
		return _resourceProvider;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	/**
	 * The {@link SingleSelectVetoListener} that should be asked to verify a new selection in the UI
	 * before updating the {@link #getLeafSelectionModel()}.
	 */
	public SingleSelectVetoListener getVetoListener() {
		return _vetoListener;
	}

	/**
	 * @see #getVetoListener()
	 */
	public void setSelectionVeto(SingleSelectVetoListener vetoListener) {
		_vetoListener = vetoListener;
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	protected void internalAttach() {
		// Note: Adding listeners must happen before calling the super implementation: The super
		// implementation fires the "attached" event. If listeners to this event update the
		// selection models, those changes could not be observed be this control, if the listeners
		// are not attached early.
		_levelSelection.addSingleSelectionListener(this);
		_entrySelection.addSingleSelectionListener(this);
		_tree.addTreeModelListener(this);

		super.internalAttach();
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();

		_levelSelection.removeSingleSelectionListener(this);
		_entrySelection.removeSingleSelectionListener(this);
		_tree.removeTreeModelListener(this);
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
		if (selectedObject != null && model == _entrySelection) {
			TreePath selectedNode = path(selectedObject);
			TreePath parent = selectedNode.getParent();
			_levelSelection.setSingleSelection(parent);
		}
		requestRepaint();
	}

	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		switch (evt.getType()) {
			case TreeModelEvent.AFTER_NODE_ADD:
			case TreeModelEvent.BEFORE_NODE_REMOVE:
			case TreeModelEvent.NODE_CHANGED: {
				if (changedPath(evt).getParent().equals(selectedLevel())) {
					requestRepaint();
				}
				break;
			}
			case TreeModelEvent.BEFORE_STRUCTURE_CHANGE: {
				TreePath changedPath = changedPath(evt);
				// Check, whether the selected level is contained in the subtree of the parent of
				// the changed path. In that case, the changed path is either displayed as entry, or
				// it is an ancestor of the displayed entries. In both cases, the displayed entries
				// must be updated.
				if (isAncestorOrSelf(selectedLevel(), changedPath.isRoot() ? changedPath : changedPath.getParent())) {
					requestRepaint();
				}
				break;
			}
		}
	}

	/**
	 * Whether the other {@link TreePath} is an ancestor of the given path or equals the given path.
	 * 
	 * @param path
	 *        The base path.
	 * @param potentialAncestorOrSelf
	 *        The path to compare to.
	 * @return Whether the other path is the equal to the given path or an ancestor thereof.
	 */
	private static boolean isAncestorOrSelf(TreePath path, TreePath potentialAncestorOrSelf) {
		while (path.isValid()) {
			if (path.equals(potentialAncestorOrSelf)) {
				return true;
			}
			path = path.getParent();
		}
		return false;
	}

	private TreePath changedPath(TreeModelEvent evt) {
		N changed = node(evt.getNode());
		TreePath changedPath = TreePath.fromNode(_tree, changed);
		return changedPath;
	}

	/**
	 * Type conversion of the untyped node.
	 * 
	 * @param untypedNode
	 *        An untyped reference to a node object.
	 * @return The corresponding node in {@link #getTree()}.
	 */
	final N node(Object untypedNode) {
		@SuppressWarnings("unchecked")
		N selectedNode = (N) untypedNode;
		return selectedNode;
	}

	final N selectedNode(TreePath path) {
		if (path == null) {
			return null;
		}
		return path.toNode(_tree);
	}

	private TreePath selectedLevel() {
		return path(_levelSelection.getSingleSelection());
	}

	private TreePath selectedEntry() {
		return path(_entrySelection.getSingleSelection());
	}

	final TreePath path(Object selectedObject) {
		return (TreePath) selectedObject;
	}

	@Override
	protected void detachInvalidated() {
		_nodeById.clear();
		super.detachInvalidated();
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Only a complete redraw is executed, no incremental updates.
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		writeControlContents(context, out);

		out.endTag(DIV);
	}

	/**
	 * Writes the direct contents of the control tag.
	 */
	private void writeControlContents(DisplayContext context, TagWriter out) throws IOException {
		TreePath selectedLevel = selectedLevel();
		N level;
		if (selectedLevel == null) {
			level = _tree.getRoot();
		} else {
			level = selectedLevel.toNode(_tree);
		}
		if (level == null) {
			return;
		}

		_selectedNodeCache = selectedNode(selectedEntry());
		try {
			N parentLevel = _tree.getParent(level);
			if (parentLevel != null) {
				// Render back button.

				AccordionEntryRenderer.INSTANCE.write(context, out, new BackAccessor(level));
			}

			for (N child : _tree.getChildren(level)) {
				_entryRenderer.write(context, out, new ForwardAccessor(child));
			}
		} finally {
			_selectedNodeCache = null;
		}
	}

	/**
	 * Execute a selection request from the UI.
	 * 
	 * @param nodeID
	 *        The client-side identifier of the selected node.
	 * @return The result of the select action.
	 */
	HandlerResult selectNodeID(String nodeID) {
		final TreePath node = _nodeById.get(nodeID);
		if (node == null) {
			return HandlerResult.error(I18NConstants.ERROR_NODE_NOT_FOUND);
		}

		return select(node);
	}

	final HandlerResult select(final TreePath node) {
		try {
			return trySelect(node);
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					return select(node);
				}
			});
			ex.process(getWindowScope());
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Execute a selection request from the UI.
	 * 
	 * @param nodePath
	 *        The path to the clicked node.
	 * @return The result of the select action.
	 * @throws VetoException
	 *         If selection should be aborted.
	 */
	private HandlerResult trySelect(TreePath nodePath) throws VetoException {
		N node = nodePath.toNode(_tree);
		if (_tree.isLeaf(node)) {
			_vetoListener.checkVeto(_entrySelection, node, false);

			_levelSelection.setSingleSelection(nodePath.getParent());
			_entrySelection.setSingleSelection(nodePath);
		} else {
			_levelSelection.setSingleSelection(nodePath);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	String nodeID(N node) {
		TreePath path = TreePath.fromNode(_tree, node);
		String result = _nodeById.getKey(path);
		if (result == null) {
			result = getScope().getFrameScope().createNewID();
			_nodeById.put(result, path);
		}
		return result;
	}

	class BackAccessor extends NodeAccessor {

		/**
		 * Creates a {@link TreeNavigatorControl.BackAccessor}.
		 */
		public BackAccessor(N node) {
			super(node);
		}

		@Override
		public String getOnclick() {
			return getOnclick(_tree.getParent(getNode()));
		}

		@Override
		public ThemeImage getImage() {
			return Icons.BACK22;
		}

		@Override
		public void writeCssClassesContent(Appendable out) throws IOException {
			super.writeCssClassesContent(out);
			out.append("back");

			N node = getNode();
			N selectedEntryAnchestor = _selectedNodeCache;
			boolean found = false;
			while (selectedEntryAnchestor != null) {
				if (Utils.equals(selectedEntryAnchestor, node)) {
					found = true;
					break;
				}
				selectedEntryAnchestor = _tree.getParent(selectedEntryAnchestor);
			}
			if (!found) {
				out.append(AccordionControl.SELECTED_CSS_CLASS);
			}
		}

	}

	class ForwardAccessor extends NodeAccessor {

		/**
		 * Creates a {@link TreeNavigatorControl.ForwardAccessor}.
		 */
		public ForwardAccessor(N node) {
			super(node);
		}

		@Override
		public String getOnclick() {
			return getOnclick(getNode());
		}

		@Override
		public void writeCssClassesContent(Appendable out) throws IOException {
			super.writeCssClassesContent(out);
			N node = getNode();
			N selectedEntryAnchestor = _selectedNodeCache;
			while (selectedEntryAnchestor != null) {
				if (Utils.equals(selectedEntryAnchestor, node)) {
					out.append(AccordionControl.SELECTED_CSS_CLASS);
					break;
				}
				selectedEntryAnchestor = _tree.getParent(selectedEntryAnchestor);
			}
		}

	}

	abstract class NodeAccessor implements Link {

		private N _node;

		/**
		 * Creates a {@link TreeNavigatorControl.NodeAccessor}.
		 *
		 */
		public NodeAccessor(N node) {
			_node = node;
		}

		public N getNode() {
			return _node;
		}

		@Override
		public String getID() {
			return nodeID(_node);
		}

		protected final String getOnclick(N child) {
			StringBuilder out = new StringBuilder();
			out.append("return services.form.AccordionControl.select(");
			TagUtil.writeJsString(out, controlID());
			out.append(",");
			TagUtil.writeJsString(out, nodeID(child));
			out.append(");");
			return out.toString();
		}

		private CharSequence controlID() {
			return TreeNavigatorControl.this.getID();
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public boolean isDisabled() {
			return false;
		}

		@Override
		public ThemeImage getImage() {
			return _resourceProvider.getImage(_node, null);
		}

		@Override
		public String getAltText() {
			return null;
		}

		@Override
		public String getLabel() {
			return _resourceProvider.getLabel(_node);
		}

		@Override
		public String getTooltip() {
			return null;
		}

		@Override
		public String getTooltipCaption() {
			return null;
		}

		@Override
		public void writeCssClassesContent(Appendable out) throws IOException {
			out.append(_resourceProvider.getCssClass(_node));
		}

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

			return ((TreeNavigatorControl<?>) control).selectNodeID(nodeID);
		}

	}

}
