/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.breadcrumb;

import static com.top_logic.basic.StringServices.debug;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static com.top_logic.basic.util.Utils.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.component.ComponentTileSupplier;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;
import com.top_logic.mig.html.layout.tiles.component.TileListComponent;

/**
 * The type of {@link TLTreeNode} built by the {@link TileBreadcrumbTreeModelBuilder}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class TileBreadcrumbTreeNode extends AbstractMutableTLTreeNode<TileBreadcrumbTreeNode>
		implements SelectionModelOwner {

	private final DefaultSingleSelectionModel _childSelection;

	private ContainerComponentTile _tile;

	private Object _selectionContainer;

	private boolean _selectionContainerInitialized = false;

	private boolean _childrenInitialized = false;

	/**
	 * Creates a {@link TileBreadcrumbTreeNode}.
	 * 
	 * @param model
	 *        Is not allowed to be null.
	 * @param parent
	 *        Is not allowed to be null, except for the root node: It has to be null for root.
	 * @param businessObject
	 *        Is allowed to be null, if this node represents the null object.
	 */
	public TileBreadcrumbTreeNode(TileBreadcrumbTreeModel model, TileBreadcrumbTreeNode parent,
			Object businessObject) {
		super(requireNonNull(model), parent, businessObject);
		checkRootNodeIsTileRoot(parent, businessObject);
		/* The SelectionModel for the child selection is created in the constructor, but the
		 * child selection is lazily initialized: It cannot be initialized before the children
		 * exist: There is no child selection without children. And the children must not be
		 * initialized in the constructor, as the node is not yet a part of the tree until the
		 * constructor has returned the new node object. And it would force the whole tree to be
		 * created when the first node is created, which is unwanted, too. */
		_childSelection = createNodeSelectionModel();
		/* All nodes are explicitly initialized from the outside, except for root. As the
		 * creation of root is not under the control of the TreeBuilder. It is therefore
		 * necessary to initialize root here. */
		if (parent == null) {
			init(null);
		}
	}

	private void checkRootNodeIsTileRoot(TileBreadcrumbTreeNode parent, Object businessObject) {
		if (parent == null) {
			if (!(businessObject instanceof TileContainerComponent)) {
				throw new IllegalArgumentException("The root node has to represent a "
					+ TileContainerComponent.class.getSimpleName() + ".");
			}
		}
	}

	/**
	 * Sets the "parent container" and initializes this node based on this information.
	 * <p>
	 * This cannot be done without the "parent container". And the "parent container" cannot be
	 * calculated from the parent node, without doing a lot of work twice: Once for creating the
	 * node and once for initializing it. The lazy initialization is therefore a performance
	 * optimization.
	 * </p>
	 */
	void init(Object parentContainer) {
		_tile = calcTile();
		_selectionContainer = calcSelectionContainer(parentContainer);
		_selectionContainerInitialized = true;
	}

	private DefaultSingleSelectionModel createNodeSelectionModel() {
		return new DefaultSingleSelectionModel(this);
	}

	private Object calcSelectionContainer(Object parentContainer) {
		Object businessObject = getBusinessObject();
		LayoutComponent component = TileBreadcrumbTreeModelBuilder.getComponent(businessObject);
		if (component == null) {
			return parentContainer;
		}
		List<LayoutComponent> ancestors = TileBreadcrumbTreeModelBuilder.getSelectionComponents(component);
		if (ancestors.isEmpty()) {
			return null;
		}
		LayoutComponent ancestor = ancestors.get(0);
		if (!(ancestor instanceof TileContainerComponent)) {
			return ancestor;
		}
		List<LayoutComponent> rootTileContent = TileBreadcrumbTreeModelBuilder.getSelectionComponents(ancestor);
		if (rootTileContent.isEmpty()) {
			return ancestor;
		}
		if (rootTileContent.size() > 1) {
			throw new UnsupportedOperationException(
				"Multiple selection container next to each other: " + rootTileContent);
		}
		return rootTileContent.get(0);
	}

	private ContainerComponentTile calcTile() {
		return findTile(getBusinessObject());
	}

	private ContainerComponentTile findTile(Object businessObject) {
		if (businessObject == null) {
			return null;
		}
		if (businessObject instanceof ContainerComponentTile) {
			return (ContainerComponentTile) businessObject;
		}
		if (businessObject instanceof TileContainerComponent) {
			return createRootTile((TileContainerComponent) businessObject);
		}
		if (TileBreadcrumbTreeModelBuilder.isTransparentComponentContainer(businessObject)) {
			return findTile(TileBreadcrumbTreeModelBuilder.unpackSelectionComponent((LayoutComponent) businessObject));
		}
		if (getParent() != null) {
			return getParent().findInnerTile();
		}
		return null;
	}

	private static ContainerComponentTile createRootTile(TileContainerComponent component) {
		TileLayout rootTile = TileBreadcrumbTreeModelBuilder.getRootTile(component.displayedLayout());
		return new ContainerComponentTile(component, rootTile);
	}

	/**
	 * The {@link TileRef#getContentTile() content tile} of the {@link #getEnclosingTileNode()
	 * enclosing tile}.
	 * 
	 * @return Null if the {@link #getEnclosingTileNode() enclosing tile} does not represent a
	 *         {@link TileRef}.
	 */
	private ContainerComponentTile findInnerTile() {
		TileBreadcrumbTreeNode enclosingTileNode = getEnclosingTileNode();
		if (enclosingTileNode == null) {
			return null;
		}
		return getTileRefContent(enclosingTileNode.getTile());
	}

	private ContainerComponentTile getTileRefContent(ContainerComponentTile componentTile) {
		TileLayout tile = componentTile.getBusinessObject();
		if (!(tile instanceof TileRef)) {
			return null;
		}
		TileRef enclosingTileRef = (TileRef) tile;
		TileContainerComponent tileComponent = componentTile.container();
		TileLayout tileLayout = enclosingTileRef.getContentTile();
		return new ContainerComponentTile(tileComponent, tileLayout);
	}

	void updateChildSelectionRecursively() {
		updateChildSelection();
		TileBreadcrumbTreeNode selectedChild = getSelectedChild();
		if (selectedChild != null) {
			selectedChild.updateChildSelectionRecursively();
		}
	}

	private void updateChildSelection(TileBreadcrumbTreeNode selection) {
		_childSelection.setSingleSelection(selection);
	}

	/**
	 * The object containing the children of this node.
	 * <p>
	 * This is not always the same as {@link #getBusinessObject()}.
	 * </p>
	 */
	public Object getSelectionContainer() {
		checkSelectionContainerInitialized();
		return _selectionContainer;
	}

	private Object getNextSelectionContainer() {
		TileBreadcrumbTreeNode enclosingTileNode = getEnclosingTileNode();
		if (enclosingTileNode == null) {
			return null;
		}
		ContainerComponentTile componentTile = enclosingTileNode.getTile();
		TileLayout tileLayout = enclosingTileNode.getTile().getBusinessObject();
		if (tileLayout instanceof TileRef) {
			return TileBreadcrumbTreeModelBuilder.unpackSelectionComponent(componentTile.getTileComponent());
		}
		if (tileLayout instanceof CompositeTile) {
			return componentTile;
		}
		return null;
	}

	private void checkSelectionContainerInitialized() {
		if (!_selectionContainerInitialized) {
			throw new IllegalStateException("The parent container is not initialized.");
		}
	}

	/**
	 * The tile represented by this node.
	 * 
	 * @return Null, if this node does not represent a tile but for example a selection in a
	 *         table.
	 * 
	 * @see #getEnclosingTileNode()
	 */
	public ContainerComponentTile getTile() {
		return _tile;
	}

	/**
	 * The {@link TileBreadcrumbTreeNode} representing the enclosing {@link TileLayout}.
	 * <p>
	 * If this node represents a {@link TileLayout} the result is this node.
	 * </p>
	 * 
	 * @return Null, if this node is outside a {@link TileContainerComponent}. "this" if this
	 *         node represents a tile.
	 * 
	 * @see #getTile()
	 */
	public TileBreadcrumbTreeNode getEnclosingTileNode() {
		if (getTile() != null) {
			return this;
		}
		if (getParent() == null) {
			return null;
		}
		return getParent().getEnclosingTileNode();
	}

	/**
	 * The {@link SingleSelectionModel} contains the {@link TLTreeNode}, not the business
	 * object, which is selected.
	 */
	public SingleSelectionModel getChildSelection() {
		ensureInitialized();
		return _childSelection;
	}

	@Override
	public SelectionModel getSelectionModel() {
		ensureInitialized();
		return _childSelection;
	}

	/**
	 * The selected child {@link TileBreadcrumbTreeNode}.
	 * 
	 * @return Null, if no child is selected.
	 * 
	 * @see #getChildSelection()
	 */
	public TileBreadcrumbTreeNode getSelectedChild() {
		ensureInitialized();
		return (TileBreadcrumbTreeNode) getChildSelection().getSingleSelection();
	}

	private void ensureInitialized() {
		if (!_childrenInitialized) {
			_childrenInitialized = true;
			enforceInitChildren();
			updateChildSelection();
			addChildSelectionListener();
		}
	}

	private void addChildSelectionListener() {
		Object childSelectionReceiver = getChildSelectionReceiver();
		if (childSelectionReceiver != null) {
			addChildSelectionListener(childSelectionReceiver);
		}
	}

	private Object getChildSelectionReceiver() {
		if (getSelectionContainer() instanceof Selectable) {
			return getSelectionContainer();
		}
		if (getTile() != null) {
			return getTile().container();
		}
		return null;
	}

	private void addChildSelectionListener(Object component) {
		SingleSelectionListener listener =
			(model, oldSelection, newSelection) -> getModel().processOutgoingEvent(
				() -> onSelectionChange(component, oldSelection, newSelection));
		getChildSelection().addSingleSelectionListener(listener);
		getModel().addListenerCleanup(() -> getChildSelection().removeSingleSelectionListener(listener));
	}

	private void onSelectionChange(Object component, Object oldSelection, Object newSelection) {
		logDebug(() -> "Changing selection on component '" + label(component) + "' from '" + label(oldSelection)
			+ "' to '" + label(newSelection) + "'.");
		TileBreadcrumbTreeNode newSelectionNode = (TileBreadcrumbTreeNode) newSelection;
		if (component instanceof TileContainerComponent) {
			onSelectionChange((TileContainerComponent) component, newSelectionNode);
			return;
		}
		if (component instanceof Selectable) {
			onSelectionChange((Selectable) component, newSelectionNode);
			return;
		}
		throw new UnsupportedOperationException("Unsupported selection receiver: " + debug(component));
	}

	private static String label(Object object) {
		return MetaLabelProvider.INSTANCE.getLabel(object);
	}

	private static void logDebug(Supplier<String> message) {
		Logger.debug(message, TileBreadcrumbTreeModel.class);
	}

	private void onSelectionChange(TileContainerComponent tileComponent, TileBreadcrumbTreeNode newSelection) {
		if (newSelection == null) {
			tileComponent.displayTileLayout(getTileLayout());
			return;
		}
		ContainerComponentTile componentTile = (ContainerComponentTile) newSelection.getBusinessObject();
		tileComponent.displayTileLayout(componentTile.getBusinessObject());
	}

	private void onSelectionChange(Selectable selectableComponent, TileBreadcrumbTreeNode newSelection) {
		if (newSelection != null) {
			Object newBusinessObjectSelection = newSelection.getBusinessObject();
			selectableComponent.setSelected(newBusinessObjectSelection);
			return;
		}
		selectableComponent.setSelected(getNewSelectableSelection());
		TileBreadcrumbTreeNode enclosingTileNode = getEnclosingTileNode();
		if (enclosingTileNode == null) {
			return;
		}
		ContainerComponentTile childTile = getEnclosingTileNode().getTile();
		TileContainerComponent tileComponent = childTile.container();
		tileComponent.displayTileLayout(childTile.getBusinessObject());
	}

	private Object getNewSelectableSelection() {
		if (isProbablySelectable(getBusinessObject())) {
			return getBusinessObject();
		}
		return null;
	}

	private boolean isProbablySelectable(Object businessObject) {
		if (businessObject instanceof LayoutComponent) {
			return false;
		}
		if (businessObject instanceof ComponentTileSupplier) {
			return false;
		}
		return true;
	}

	@Override
	public TileBreadcrumbTreeModel getModel() {
		/* The cast is safe, as it is guarded by the type of the constructor parameter
		 * "model". */
		return (TileBreadcrumbTreeModel) super.getModel();
	}

	private TileBreadcrumbTreeNode getChildByBusinessObject(Object businessObject) {
		return getChildByPredicate(child -> Objects.equals(child.getBusinessObject(), businessObject));
	}

	private TileBreadcrumbTreeNode getChildByPredicate(Predicate<TileBreadcrumbTreeNode> predicate) {
		SearchResult<TileBreadcrumbTreeNode> searchResult = new SearchResult<>();
		for (TileBreadcrumbTreeNode child : getChildren()) {
			searchResult.addCandidate(child);
			if (predicate.test(child)) {
				searchResult.add(child);
			}
		}
		if (searchResult.getResults().isEmpty()) {
			return null;
		}
		return searchResult
			.getSingleResult("Failed to find the child node matching the given predicate: " + debug(predicate));
	}

	private void enforceInitChildren() {
		getChildren();
	}

	void updateChildSelection() {
		updateChildSelection(calcChildSelection());
	}

	private TileBreadcrumbTreeNode calcChildSelection() {
		return calcChildSelection(getSelectionContainer(), false);
	}

	private TileBreadcrumbTreeNode calcChildSelection(Object selectionContainer, boolean isNextSelectionContainer) {
		if (!isAlive()) {
			return null;
		}
		if (selectionContainer == null) {
			return null;
		}
		if (TileBreadcrumbTreeModelBuilder.isTransparentComponentContainer(selectionContainer)) {
			LayoutComponent unpackedSelectionComponent =
				TileBreadcrumbTreeModelBuilder.unpackSelectionComponent((LayoutComponent) selectionContainer);
			return calcChildSelection(unpackedSelectionComponent, isNextSelectionContainer);
		}
		if (selectionContainer instanceof TileContainerComponent) {
			TileContainerComponent tileComponent = (TileContainerComponent) selectionContainer;
			TileLayout rootTile = TileBreadcrumbTreeModelBuilder.getRootTile(tileComponent.displayedLayout());
			LayoutComponent rootTileComponent = tileComponent.getTileComponent(rootTile);
			/* If the root tile represents a component, look for the next child there. The inner
			 * tiles are only relevant after the children in the components have been
			 * exhausted. */
			if (rootTileComponent != null) {
				return calcChildSelection(rootTileComponent, isNextSelectionContainer);
			}
			List<TileLayout> tilePath = getPathToTile(tileComponent.displayedLayout());
			return getChildByPredicate(child -> tilePath.contains(child.getTileLayout()));
		}
		if (selectionContainer instanceof ContainerComponentTile) {
			ContainerComponentTile containerComponentTile = (ContainerComponentTile) selectionContainer;
			TileLayout tileLayout = containerComponentTile.getBusinessObject();
			if (!(tileLayout instanceof CompositeTile)) {
				return calcChildSelection(containerComponentTile.getTileComponent(), isNextSelectionContainer);
			}
			TileContainerComponent tileComponent = containerComponentTile.container();
			List<TileLayout> tilePath = getPathToTile(tileComponent.displayedLayout());
			if (tilePath.size() == 1) {
				/* The path consists only of the root node. But there is no node in the
				 * breadcrumb representing the root node (as there is no alternative to the root
				 * tile). That means, there is no selected tile. */
				return null;
			}
			return getChildByPredicate(child -> tilePath.contains(child.getTileLayout()));
		}
		if (selectionContainer instanceof TableComponent) {
			return calcChildSelection((Selectable) selectionContainer, isNextSelectionContainer);
		}
		if (selectionContainer instanceof TileListComponent) {
			return calcChildSelection((Selectable) selectionContainer, isNextSelectionContainer);
		}
		if (selectionContainer instanceof TreeTableComponent) {
			return calcChildSelectionInTreeTable(selectionContainer, isNextSelectionContainer);
		}
		return null;
	}

	private TileBreadcrumbTreeNode calcChildSelection(Selectable selectable, boolean isNextSelectionContainer) {
		Object selected = selectable.getSelected();
		if (selected == null) {
			return null;
		}
		if ((getParent() != null) && selectable.equals(getParent().getSelectionContainer())) {
			return calcChildSelectionFromNextSelectionContainer(isNextSelectionContainer);
		}
		return getChildByBusinessObject(selected);
	}

	private TileBreadcrumbTreeNode calcChildSelectionInTreeTable(Object selectionContainer,
			boolean isNextSelectionContainer) {
		TreeTableComponent treeTableComponent = (TreeTableComponent) selectionContainer;
		Object selection = treeTableComponent.getSelected();
		if (selection == null) {
			return calcChildSelectionFromNextSelectionContainer(isNextSelectionContainer);
		}
		if (selection.equals(getBusinessObject())) {
			return calcChildSelectionFromNextSelectionContainer(isNextSelectionContainer);
		}
		/* The Maybe always has a value, as there is always a node in the TreeTableComponent for
		 * its own selection. */
		TLTreeNode<?> selectedTreeTableNode = treeTableComponent.findNodeOfBusinessObject(selection).get();
		List<?> pathToRoot = TLTreeModelUtil.createPathToRootUserObject(selectedTreeTableNode);
		return calcSelectedTreeTableChild(pathToRoot);
	}

	private TileBreadcrumbTreeNode calcSelectedTreeTableChild(List<?> pathToRoot) {
		return getChildByPredicate(child -> pathToRoot.contains(child.getBusinessObject()));
	}

	/**
	 * The {@link TileLayout} represented by this node.
	 * 
	 * @return Null, if this node does not represent a tile.
	 */
	private TileLayout getTileLayout() {
		if (getTile() == null) {
			return null;
		}
		return getTile().getBusinessObject();
	}

	private TileBreadcrumbTreeNode calcChildSelectionFromNextSelectionContainer(boolean isNextSelectionContainer) {
		if (isNextSelectionContainer) {
			/* The search for a selection recursed into searching the next selection container.
			 * That means, there is no next selection. */
			return null;
		}
		return calcChildSelection(getNextSelectionContainer(), true);
	}

	private static List<TileLayout> getPathToTile(TileLayout displayedLayout) {
		TileLayout currentTile = requireNonNull(displayedLayout);
		List<TileLayout> path = list();
		while (currentTile != null) {
			path.add(currentTile);
			currentTile = getTileParent(currentTile);
		}
		// Switch order from "root last" to "root first".
		reverse(path);
		return path;
	}

	private static TileLayout getTileParent(TileLayout tile) {
		return (TileLayout) tile.container();
	}

	@Override
	protected void notifyRemoved() {
		cleanupParentSelection();
		super.notifyRemoved();
	}

	private void cleanupParentSelection() {
		if (getParent() == null) {
			return;
		}
		if (!Objects.equals(getParent().getSelectedChild(), this)) {
			return;
		}
		getParent().getChildSelection().setSingleSelection(null);
	}

	@Override
	protected void resetChildren() {
		_childrenInitialized = false;
		super.resetChildren();
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("business object", getBusinessObject())
			/* Use just the business object of the parent to prevent a recursive toString(). */
			.add("parent (business object)", getParent() == null ? "[none]" : getParent().getBusinessObject())
			.add("selection container", _selectionContainer)
			.build();
	}

}
