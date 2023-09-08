/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.breadcrumb;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.xml.LayoutControlComponent;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;
import com.top_logic.mig.html.layout.tiles.component.TileListComponent;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.compound.CompoundSecurityProjectLayout;

/**
 * Creates the {@link TileBreadcrumbTreeModel}.
 * <p>
 * When something changes, like the selected tile or business object, the whole tree is rebuild
 * ({@link TileBreadcrumbTreeModel#updateTree()}. This is much more simple and robust than
 * incremental updates. And the amount of time is irrelevant (<10ms).
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TileBreadcrumbTreeModelBuilder implements TreeBuilder<TileBreadcrumbTreeNode> {

	private static final Set<Class<? extends LayoutComponent>> SELECTION_COMPONENT_TYPES = unmodifiableSet(set(
		TableComponent.class,
		TreeTableComponent.class,
		TileContainerComponent.class,
		TileListComponent.class));

	/**
	 * {@link LayoutComponent}s that are invisible to the user: It doesn't matter, whether the
	 * components are siblings in the component tree or separated by one of these components.
	 */
	private static final Set<Class<? extends LayoutContainer>> TRANSPARENT_COMPONENT_CONTAINER = unmodifiableSet(set(
		Layout.class,
		BoundLayout.class,
		CompoundSecurityLayout.class,
		CompoundSecurityProjectLayout.class));

	@Override
	public boolean isFinite() {
		/* The tree is infinite, if one of the components or tiles displays an infinite tree in
		 * which the user the can make a selection. As it is potentially very expensive to ask every
		 * component and tile whether they display an infinite tree, this method assumes that it
		 * might be infinite. This means, ui operations such as "expand all" or
		 * "filter the whole tree" won't be available. But that is okay. */
		return false;
	}

	@Override
	public List<TileBreadcrumbTreeNode> createChildList(TileBreadcrumbTreeNode parentNode) {
		/* Make sure the gui isn't destroyed by a bug in the breadcrumb. */
		return ExceptionUtil.runAndLogErrors(
			() -> createMutableChildrenList(parentNode),
			error -> list(),
			TileBreadcrumbTreeModelBuilder.class);
	}

	private List<TileBreadcrumbTreeNode> createMutableChildrenList(TileBreadcrumbTreeNode parentNode) {
		/* Always copy the list to make sure it is mutable and resizable. The result list is used
		 * without being copied as the internal data structure of the parent node. And an immutable
		 * list causes exceptions in a "MUTABLE TLTreeModel". */
		return list(createChildrenInternal(parentNode));
	}

	private List<TileBreadcrumbTreeNode> createChildrenInternal(TileBreadcrumbTreeNode parentNode) {
		TileBreadcrumbTreeNode enclosingTileNode = parentNode.getEnclosingTileNode();
		if (enclosingTileNode != null) {
			TreeTableComponent representedTreeTable = getRepresentedTreeTable(parentNode);
			if (representedTreeTable != null) {
				return createChildren(parentNode, representedTreeTable);
			}
			ContainerComponentTile componentTile = enclosingTileNode.getTile();
			TileLayout tileLayout = componentTile.getBusinessObject();
			if (tileLayout instanceof TileRef) {
				return createChildren(parentNode, unpackSelectionComponent(componentTile.getTileComponent()));
			}
			if (tileLayout instanceof CompositeTile) {
				return createTileNodes(parentNode, componentTile);
			}
			return emptyList();
		}
		LayoutComponent componentBusinessObject = getComponent(parentNode.getBusinessObject());
		if (componentBusinessObject == null) {
			return emptyList();
		}
		Object selectionComponent = unpackSelectionComponent(componentBusinessObject);
		return createChildren(parentNode, selectionComponent);
	}

	private TreeTableComponent getRepresentedTreeTable(TileBreadcrumbTreeNode node) {
		TileBreadcrumbTreeNode enclosingTileNode = node.getEnclosingTileNode();
		if (enclosingTileNode == null) {
			return null;
		}
		if (enclosingTileNode.getParent() == null) {
			return null;
		}
		ContainerComponentTile tile = enclosingTileNode.getParent().getTile();
		if (tile == null) {
			return null;
		}
		if (tile.getTileComponent() == null) {
			return null;
		}
		LayoutComponent selectionComponent = unpackSelectionComponent(tile.getTileComponent());
		if (!(selectionComponent instanceof TreeTableComponent)) {
			return null;
		}
		return (TreeTableComponent) selectionComponent;
	}

	private List<TileBreadcrumbTreeNode> createChildren(TileBreadcrumbTreeNode parentNode,
			Object selectionContainer) {
		if (selectionContainer == null) {
			return emptyList();
		}
		if (isTransparentComponentContainer(selectionContainer)) {
			Object parentSelectionContainer = parentNode.getSelectionContainer();
			if (parentSelectionContainer == null) {
				return null;
			}
			return createChildren(parentNode, parentSelectionContainer);
		}
		if (selectionContainer instanceof TileContainerComponent) {
			TileContainerComponent tileContainer = (TileContainerComponent) selectionContainer;
			List<TileBreadcrumbTreeNode> tileNodes = createTileNodes(parentNode, tileContainer);
			return tileNodes;
		}
		if (selectionContainer instanceof TableComponent) {
			if (isParentSelectionContainer(parentNode, selectionContainer)) {
				return null;
			}
			TableComponent tableComponent = (TableComponent) selectionContainer;
			List<TileBreadcrumbTreeNode> tableNodes = createTableComponentNodes(parentNode, tableComponent);
			addIncomingEventListener(parentNode.getModel(), tableComponent);
			return tableNodes;
		}
		if (selectionContainer instanceof TileListComponent) {
			if (isParentSelectionContainer(parentNode, selectionContainer)) {
				return null;
			}
			TileListComponent tileListComponent = (TileListComponent) selectionContainer;
			List<TileBreadcrumbTreeNode> tileNodes = createTileListNodes(parentNode, tileListComponent);
			addIncomingEventListener(parentNode.getModel(), tileListComponent);
			return tileNodes;
		}
		if (selectionContainer instanceof TreeTableComponent) {
			TreeTableComponent treeTable = (TreeTableComponent) selectionContainer;
			List<TileBreadcrumbTreeNode> treeTableNodes = createTreeTableChildren(parentNode, treeTable);
			if (isTreeTableRootLevel(parentNode, treeTable)) {
				/* Register the listeners only for the top-level of nodes in the tree table, not for
				 * each level again. */
				addIncomingEventListener(parentNode.getModel(), treeTable);
			}
			return treeTableNodes;
		}
		return null;
	}

	private boolean isParentSelectionContainer(TileBreadcrumbTreeNode parentNode, Object selectionContainer) {
		return (parentNode.getParent() != null)
			&& Objects.equals(selectionContainer, parentNode.getParent().getSelectionContainer());
	}

	static LayoutComponent unpackSelectionComponent(LayoutComponent selectionContainer) {
		List<LayoutComponent> selectionComponents = getSelectionComponents(selectionContainer);
		if (selectionComponents.isEmpty()) {
			return null;
		}
		if (selectionComponents.size() > 1) {
			throw new UnsupportedOperationException(
				"Multiple selection container next to each other: " + selectionComponents);
		}
		return selectionComponents.get(0);
	}

	/**
	 * Whether the object is "just a list of inner {@link LayoutComponent}s" for this tree.
	 */
	static boolean isTransparentComponentContainer(Object selectionContainer) {
		if (selectionContainer instanceof MainLayout) {
			return true;
		}
		if (selectionContainer instanceof LayoutControlComponent) {
			return true;
		}
		/* Check with equality and not with "instanceof", as some of the subtypes, like the
		 * CockpitLayout are not transparent component containers but behave differently. */
		return TRANSPARENT_COMPONENT_CONTAINER.contains(selectionContainer.getClass());
	}

	private List<TileBreadcrumbTreeNode> createTileNodes(TileBreadcrumbTreeNode parentNode,
			TileContainerComponent tileContainer) {
		TileLayout rootTile = getRootTile(tileContainer.displayedLayout());
		LayoutComponent rootTileComponent = tileContainer.getTileComponent(rootTile);
		if (rootTileComponent != null) {
			return createChildren(parentNode, unpackSelectionComponent(rootTileComponent));
		}
		List<TileLayout> tileChildren = getTileChildren(rootTile);
		return createSelectionNodes(parentNode, toComponentTiles(tileContainer, tileChildren), tileContainer);
	}

	static TileLayout getRootTile(TileLayout tile) {
		if (tile.container() == null) {
			return tile;
		}
		return getRootTile((TileLayout) tile.container());
	}

	private List<TileBreadcrumbTreeNode> createTileNodes(TileBreadcrumbTreeNode parentNode,
			ContainerComponentTile parentTile) {
		List<TileLayout> tiles = getTileChildren(parentTile.getBusinessObject());
		TileContainerComponent tileComponent = parentTile.container();
		List<ComponentTile> componentTiles = toComponentTiles(tileComponent, tiles);
		return createSelectionNodes(parentNode, componentTiles, parentTile);
	}

	private List<ComponentTile> toComponentTiles(TileContainerComponent tileComponent,
			Collection<? extends TileLayout> tiles) {
		return tiles.stream()
			.map(tile -> new ContainerComponentTile(tileComponent, tile))
			.collect(toList());
	}

	private List<TileBreadcrumbTreeNode> createTableComponentNodes(TileBreadcrumbTreeNode parentNode,
			TableComponent tableComponent) {
		return createSelectionNodes(parentNode, getSelection(tableComponent), tableComponent);
	}

	private void addIncomingEventListener(TileBreadcrumbTreeModel tree, LayoutComponent component) {
		ChannelListener listener = tree::onIncomingComponentSelection;
		addListener(component, listener);
		tree.addListenerCleanup(() -> removeListener(component, listener));
	}

	private List<TileBreadcrumbTreeNode> createTileListNodes(TileBreadcrumbTreeNode parentNode,
			TileListComponent tileListComponent) {
		return createSelectionNodes(parentNode, getSelection(tileListComponent), tileListComponent);
	}

	private List<Object> getSelection(Selectable component) {
		Object selected = component.getSelected();
		if (selected == null) {
			return emptyList();
		}
		return singletonList(selected);
	}

	List<TileBreadcrumbTreeNode> createTreeTableChildren(TileBreadcrumbTreeNode parentNode,
			TreeTableComponent treeTable) {
		List<TileBreadcrumbTreeNode> allChildren = list();
		allChildren.addAll(createTreeTableOwnChildren(parentNode, treeTable));
		ContainerComponentTile componentTile = parentNode.getEnclosingTileNode().getTile();
		allChildren.addAll(createTileNodes(parentNode, componentTile));
		return allChildren;
	}

	private List<TileBreadcrumbTreeNode> createTreeTableOwnChildren(TileBreadcrumbTreeNode parentNode,
			TreeTableComponent treeTable) {
		/* The business objects of the TileBreadcrumbTreeNodes are the business objects of the
		 * TreeTableComponent, not its nodes. The nodes cannot be used, as the tree and its nodes
		 * are sometimes rebuild. */
		List<?> childBusinessObjects = getBusinessObjects(createTreeTableChildrenInternal(parentNode, treeTable));
		return createSelectionNodes(parentNode, childBusinessObjects, treeTable);
	}

	private List<? extends TLTreeNode<?>> createTreeTableChildrenInternal(TileBreadcrumbTreeNode parentNode,
			TreeTableComponent treeTable) {
		Maybe<? extends TLTreeNode<?>> tableNodeMaybe =
			treeTable.findNodeOfBusinessObject(parentNode.getBusinessObject());
		boolean isRootLevel = !tableNodeMaybe.hasValue();
		if (isRootLevel) {
			AbstractTreeTableModel<?> tree = getTreeModel(treeTable);
			if (tree.isRootVisible()) {
				return singletonList(tree.getRoot());
			}
			return tree.getRoot().getChildren();
		}
		return tableNodeMaybe.get().getChildren();
	}

	/**
	 * Whether the child of the given node will be the root of the given {@link TreeTableComponent}.
	 */
	private boolean isTreeTableRootLevel(TileBreadcrumbTreeNode parent, TreeTableComponent treeTable) {
		if (parent.getParent() == null) {
			return true;
		}
		return !Objects.equals(parent.getParent().getSelectionContainer(), treeTable);
	}

	static AbstractTreeTableModel<?> getTreeModel(TreeTableComponent treeTable) {
		return treeTable.getTableData().getTree();
	}

	private void addListener(LayoutComponent component, ChannelListener listener) {
		component.getChannel(SelectionChannel.NAME).addListener(listener);
	}

	private void removeListener(LayoutComponent component, ChannelListener listener) {
		component.getChannel(SelectionChannel.NAME).removeListener(listener);
	}

	private List<TileBreadcrumbTreeNode> createSelectionNodes(TileBreadcrumbTreeNode parent, List<?> selections,
			Object parentContainer) {
		List<TileBreadcrumbTreeNode> result = list();
		for (Object selection : selections) {
			result.add(createNode(parent, selection, parentContainer));
		}
		return result;
	}

	private TileBreadcrumbTreeNode createNode(TileBreadcrumbTreeNode parent, Object businessObject,
			Object parentContainer) {
		TileBreadcrumbTreeNode node = createNodeWithoutInit(parent, businessObject);
		node.init(parentContainer);
		return node;
	}

	private TileBreadcrumbTreeNode createNodeWithoutInit(TileBreadcrumbTreeNode parent, Object businessObject) {
		/* Most of the time this code is called to create the children of uninitialized nodes. But
		 * sometimes it is reused by listeners to create new children for already initialized nodes.
		 * These two cases require different methods for the node creation. */
		if (parent.isInitialized()) {
			return parent.createChild(businessObject);
		} else {
			return createNode(parent.getModel(), parent, businessObject);
		}
	}

	static LayoutComponent getComponent(Object businessObject) {
		if (businessObject instanceof LayoutComponent) {
			return (LayoutComponent) businessObject;
		}
		if (businessObject instanceof ContainerComponentTile) {
			LayoutComponent tileComponent = ((ContainerComponentTile) businessObject).getTileComponent();
			if (tileComponent != null) {
				return tileComponent;
			}
		}
		return null;
	}

	@Override
	public TileBreadcrumbTreeNode createNode(AbstractMutableTLTreeModel<TileBreadcrumbTreeNode> model,
			TileBreadcrumbTreeNode parent, Object businessObject) {
		return new TileBreadcrumbTreeNode((TileBreadcrumbTreeModel) model, parent, businessObject);
	}

	static List<LayoutComponent> getSelectionComponents(LayoutComponent component) {
		if (component == null) {
			return emptyList();
		}
		if (component instanceof TileContainerComponent) {
			TileContainerComponent tileContainerComponent = (TileContainerComponent) component;
			TileLayout rootTile = getRootTile(tileContainerComponent.displayedLayout());
			LayoutComponent rootTileComponent = tileContainerComponent.getTileComponent(rootTile);
			if (rootTileComponent != null) {
				return getSelectionComponents(rootTileComponent);
			}
		}
		if (isSelectionComponent(component)) {
			return singletonList(component);
		}
		if (isTransparentComponentContainer(component)) {
			return getSelectionComponents((Layout) component);
		}
		return emptyList();
	}

	/**
	 * The (indirect) children of the given {@link Layout} that have something for the user to
	 * select.
	 */
	private static List<LayoutComponent> getSelectionComponents(Layout parent) {
		List<LayoutComponent> unfilteredChildren = getComponentsOnSameLevel(parent);
		return FilterUtil.filterList(TileBreadcrumbTreeModelBuilder::isSelectionComponent, unfilteredChildren);
	}

	private static List<LayoutComponent> getComponentsOnSameLevel(LayoutComponent parent) {
		List<LayoutComponent> list = list();
		list.add(parent);
		if (isTransparentComponentContainer(parent)) {
			if (parent instanceof LayoutControlComponent) {
				LayoutComponent referencedComponent = ((LayoutControlComponent) parent).getReferencedComponent();
				if (referencedComponent != parent) {
					list.addAll(getComponentsOnSameLevel(referencedComponent));
				}
			} else {
				LayoutContainer container = (LayoutContainer) parent;
				for (LayoutComponent child : container.getChildList()) {
					list.addAll(getComponentsOnSameLevel(child));
				}
			}
		}
		return removeDuplicates(list);
	}

	private static List<LayoutComponent> removeDuplicates(List<LayoutComponent> list) {
		return list(linkedSet(list));
	}

	private static boolean isSelectionComponent(LayoutComponent component) {
		if (component instanceof Layout) {
			return false;
		}
		Class<?> componentClass = component.getClass();
		return SELECTION_COMPONENT_TYPES.stream().anyMatch(type -> type.isAssignableFrom(componentClass));
	}

	private static List<?> getBusinessObjects(List<? extends TLTreeNode<?>> nodes) {
		List<Object> businessObjects = list();
		for (TLTreeNode<?> node : nodes) {
			businessObjects.add(node.getBusinessObject());
		}
		return businessObjects;
	}

	private static List<TileLayout> getTileChildren(TileLayout tile) {
		if (tile instanceof TileRef) {
			TileRef tileRef = (TileRef) tile;
			return singletonList(tileRef.getContentTile());
		}
		if (tile instanceof CompositeTile) {
			CompositeTile compositeTile = (CompositeTile) tile;
			return compositeTile.getTiles();
		}
		return emptyList();
	}

}
