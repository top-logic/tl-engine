/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.InlineSet;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.MappingBasedFilter;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.ScrollContainerControl;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.check.MasterSlaveCheckProvider;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.config.WithContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.TreeContextMenuFactory;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.ComponentChannel.ChannelValueFilter;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.component.SelectableWithSelectionModel;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.NoPrepare;
import com.top_logic.layout.tree.ChangeCheckNodeSelectionVetoListener;
import com.top_logic.layout.tree.DefaultTreeData;
import com.top_logic.layout.tree.ExpansionVetoListener;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.TreeDataOwner;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.dnd.DefaultTreeDrag;
import com.top_logic.layout.tree.dnd.TreeDragSource;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel.TreeUINode;
import com.top_logic.layout.tree.model.BusinessObjectMapping;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TLTreeNodeResourceProvider;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeViewConfig;
import com.top_logic.layout.tree.model.UserObjectIndex;
import com.top_logic.layout.tree.renderer.ConfigurableTreeContentRenderer;
import com.top_logic.layout.tree.renderer.ConfigurableTreeRenderer;
import com.top_logic.layout.tree.renderer.DefaultTreeImageProvider;
import com.top_logic.mig.html.AbstractRestrainedSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelConfig;
import com.top_logic.mig.html.SelectionUtil;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.AccessContext;
import com.top_logic.model.export.NoPreload;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.compound.CompoundSecurityBoundChecker;
import com.top_logic.util.Utils;

/**
 * Component for displaying and working with trees.
 * 
 * <p>
 * This class needs a {@link TreeModelBuilder} which defines the business tree to display. This
 * component than translates this tree into a {@link TreeUIModel} and holds it in sync by reacting
 * on model change, delete, and create events.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeComponent extends BuilderComponent implements SelectableWithSelectionModel,
		TreeBuilder<DefaultTreeUINodeModel.DefaultTreeUINode>, TreeModelListener,
		CompoundSecurityBoundChecker, TreeDataOwner, WithSelectionPath {

	/**
	 * Default renderer for a {@link TreeComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class TreeComponentRenderer extends ConfigurableTreeRenderer {

		/**
		 * Configuration options for {@link TreeComponent.TreeComponentRenderer}.
		 */
		public interface Config<I extends TreeComponentRenderer> extends ConfigurableTreeRenderer.Config<I> {

			/** @see #getCssClass() */
			String CSS_CLASS = "cssClass";

			/**
			 * Additional CSS class to add to the tree element.
			 */
			@Name(CSS_CLASS)
			@StringDefault(TREE_COMP_CSS_CLASS)
			String getCssClass();
		}

		/** CSS class identifying the */
		public static final String TREE_COMP_CSS_CLASS = "treeComp";

		private String _cssClass = TREE_COMP_CSS_CLASS;

		/**
		 * Creates a {@link TreeComponentRenderer} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public TreeComponentRenderer(InstantiationContext context, Config<?> config) {
			super(context, config);
			_cssClass = config.getCssClass();
		}

		/**
		 * Creates a new {@link TreeComponentRenderer}.
		 * 
		 * @param resourceProvider
		 *        {@link ResourceProvider} for the business objects displayed in the tree.
		 */
		public TreeComponentRenderer(ResourceProvider resourceProvider) {
			super(HTMLConstants.DIV, HTMLConstants.DIV, createContentRenderer(resourceProvider));
		}

		private static ConfigurableTreeContentRenderer createContentRenderer(ResourceProvider resourceProvider) {
			TLTreeNodeResourceProvider treeResourceProvider =
				TLTreeNodeResourceProvider.newTLTreeNodeResourceProvider(resourceProvider);
			return new ConfigurableTreeContentRenderer(treeResourceProvider, DefaultTreeImageProvider.INSTANCE);
		}

		@Override
		public void appendControlCSSClasses(Appendable out, TreeControl control) throws IOException {
			super.appendControlCSSClasses(out, control);
			HTMLUtil.appendCSSClass(out, _cssClass);
		}

	}

	/**
	 * Configuration of an {@link TreeComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config
			extends BuilderComponent.Config, TreeViewConfig, SelectionModelConfig, WithContextMenuFactory {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Short-cut name of {@link TreeComponent}.
		 * 
		 * <p>
		 * Note, this is not <code>tree</code> because <code>table</code> is not possible for
		 * {@link TableComponent}.
		 * </p>
		 * 
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "treeView";

		/** Name of the {@link #getTreeRenderer()} property. */
		String TREE_RENDERER_ATTRIBUTE_NAME = "treeRenderer";

		/** Name of the {@link #getFocusSelection()} property. */
		String AUTOFOCUS_SELECTION_ATTRIBUTE_NAME = "autoFocusSelection";

		/** Name of the {@link #getResourceProvider()} property. */
		String RESOURCE_PROVIDER_ATTRIBUTE = "resource-provider";

		/** @see #getDropTargets() */
		String DROP_TARGETS_ATTRIBUTE_NAME = "dropTargets";

		/** @see #getDropTargets() */
		String DROP_TARGETS_ENTRY_TAG_NAME = "dropTarget";

		/** @see #getDragSource() */
		String DRAG_SOURCE = "dragSource";

		/**
		 * The configuration parameter for {@link #getSelectionFilter()}.
		 */
		String SELECTION_FILTER_ATTRIBUTE = "selection-filter";

		/**
		 * The configuration parameter for {@link #isShowOnlySelectableNodes()}.
		 */
		String SHOW_ONLY_SELECTABLE_NODES_ATTRIBUTE = "show-only-selectable-nodes";

		@Override
		@ClassDefault(TreeComponent.class)
		public Class<? extends LayoutComponent> getImplementationClass();

		@Override
		PolymorphicConfiguration<? extends TreeModelBuilder<Object>> getModelBuilder();
		
		/**
		 * Filter for the business objects displayed in the tree. Only elements that are accepted by
		 * the filter can be selected.
		 */
		@Name(SELECTION_FILTER_ATTRIBUTE)
		@InstanceFormat
		@InstanceDefault(TrueFilter.class)
		Filter getSelectionFilter();

		/**
		 * Configuration of the {@link SecuritySelectionFilter} which is used to delegate check
		 * whether a node can be selected (for security reasons) to.
		 */
		@Name("security-filter")
		@ItemDefault
		SecuritySelectionFilter.Config getSecurityFilter();

		/**
		 * This configuration allows to hide non selectable nodes.
		 * 
		 * <p>
		 * If this configuration is <code>true</code> only those nodes are displayed which can be
		 * selected, or which have a selectable node in the subtree with the node as root.
		 * </p>
		 */
		@Name(SHOW_ONLY_SELECTABLE_NODES_ATTRIBUTE)
		boolean isShowOnlySelectableNodes();

		@Override
		@BooleanDefault(true)
		boolean hasToolbar();

		/**
		 * The configuration of the {@link TreeRenderer} used to render the tree.
		 * 
		 * <p>
		 * If not set, a {@link TreeRenderer} is created using the {@link #getResourceProvider()}.
		 * </p>
		 * 
		 * @see #getResourceProvider()
		 */
		@Name(TREE_RENDERER_ATTRIBUTE_NAME)
		PolymorphicConfiguration<TreeRenderer> getTreeRenderer();

		/**
		 * The configuration of the {@link ResourceProvider} used to display the business objects.
		 * 
		 * <p>
		 * This method is used, if no {@link #getTreeRenderer() tree renderer} was set explicit.
		 * </p>
		 * 
		 * @see #getTreeRenderer()
		 */
		@ItemDefault(MetaResourceProvider.class)
		@Name(RESOURCE_PROVIDER_ATTRIBUTE)
		PolymorphicConfiguration<ResourceProvider> getResourceProvider();

		/**
		 * Whether the selected node should be in focus, when component is displayed or selection
		 * changed (programmatic).
		 */
		@Name(AUTOFOCUS_SELECTION_ATTRIBUTE_NAME)
		@BooleanDefault(true)
		boolean getFocusSelection();

		/**
		 * Operation that controls dragging data from a tree.
		 */
		@Name(DRAG_SOURCE)
		@ItemDefault(DefaultTreeDrag.class)
		PolymorphicConfiguration<TreeDragSource> getDragSource();

		/**
		 * Operations that control element drops in a tree.
		 */
		@Name(DROP_TARGETS_ATTRIBUTE_NAME)
		@EntryTag(DROP_TARGETS_ENTRY_TAG_NAME)
		List<PolymorphicConfiguration<TreeDropTarget>> getDropTargets();

		@Override
		@ItemDefault(TreeContextMenuFactory.class)
		@ImplementationClassDefault(TreeContextMenuFactory.class)
		PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			BuilderComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(ExpandCollapseAll.EXPAND_ID);
			registry.registerButton(ExpandCollapseAll.COLLAPSE_ID);
		}

	}
	
	private static final Property<AccessContext> PRELOAD_CONTEXT_PROPERTY = TypedAnnotatable.property(
		AccessContext.class, "expansionContext", NoPrepare.INSTANCE);

	private final SelectionModel _selectionModel;

	private IndexedTreeUINodeModel _treeModel;

	/** @see Config#getExpandSelected() */
	private final boolean _expandSelected;

	/** @see Config#getExpandRoot() */
	private final boolean _expandRoot;

	/** @see Config#isRootVisible() */
	private final boolean _rootVisible;

	/** @see Config#adjustSelectionWhenCollapsing() */
	final boolean _adjustSelectionWhenCollapsing;

	/** @see Config#getSelectionFilter() */
	private final Filter _selectionFilter;
	
	/** @see Config#isShowOnlySelectableNodes() */
	private final boolean _showOnlySelectableNodes;

	private final SecuritySelectionFilter _securitySelectionFilter;

	private boolean _isSelectionValid;

	private TreeControl _treeControl;

	private ScrollContainerControl _scrollContainer;

	private final TreeRenderer _treeRenderer;

	private final boolean _focusSelection;

	private final TreeModelListener _focusExpanded = new TreeModelListener() {

		@Override
		public void handleTreeUIModelEvent(TreeModelEvent evt) {
			if (evt.getType() == TreeModelEvent.AFTER_EXPAND) {
				getScrollContainer().scrollToRange(new TreeNodeRange(getTreeControl(), evt.getNode()));
			}
		}
	};

	/**
	 * Indicates that the model of this component is invalid and a validation is needed. This is
	 * done because the selection can only be focused when the tree model is valid.
	 */
	private boolean _focusAfterValidation;

	private TreeDragSource _dragSource;

	private List<TreeDropTarget> _dropTargets;

	private ContextMenuFactory _contextMenuFactory;

	/**
	 * Creates a new {@link TreeComponent} from the given configuration.
	 */
	public TreeComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_securitySelectionFilter = createSecuritySelectionFilter(context, config);
		_selectionFilter = createSelectionFilter(config);
		_selectionModel = createSelectionModel(config);
		_expandSelected = config.getExpandSelected();
		_expandRoot = config.getExpandRoot();
		_rootVisible = config.isRootVisible();
		_adjustSelectionWhenCollapsing = config.adjustSelectionWhenCollapsing();
		_showOnlySelectableNodes = config.isShowOnlySelectableNodes();
		_treeRenderer = buildRenderer(context, config);
		_focusSelection = config.getFocusSelection();
		_dragSource = context.getInstance(config.getDragSource());
		_dropTargets = TypedConfiguration.getInstanceList(context, config.getDropTargets());
		_contextMenuFactory = context.getInstance(config.getContextMenuFactory());
	}

	private static TreeRenderer buildRenderer(InstantiationContext context, Config config) {
		TreeRenderer treeRenderer = context.getInstance(config.getTreeRenderer());
		if (treeRenderer == null) {
			return new TreeComponentRenderer(context.getInstance(config.getResourceProvider()));
		}
		return treeRenderer;
	}

	private SelectionModel createSelectionModel(Config config) {
		Filter<TLTreeNode<?>> treeNodeFilter =
			new MappingBasedFilter<>(BusinessObjectMapping.INSTANCE, _selectionFilter);
		AbstractRestrainedSelectionModel selectionModel =
			(AbstractRestrainedSelectionModel) config.getSelectionModelFactory().newSelectionModel(this);
		selectionModel.setSelectionFilter(treeNodeFilter);
		selectionModel.addSelectionListener(new SelectionModelListener());
		return selectionModel;
	}

	private SecuritySelectionFilter createSecuritySelectionFilter(InstantiationContext context, Config config) {
		SecuritySelectionFilter filter = context.getInstance(config.getSecurityFilter());
		filter.setTree(this);
		return filter;
	}

	private Filter createSelectionFilter(Config config) {
		Filter<Object> nodeSupportedFilter = new Filter<>() {
			@Override
			public boolean accept(Object node) {
				return nodeSupported(node);
			}
		};
		// Filter that transports the security.
		Filter selectionAllowedFilter = _securitySelectionFilter;
		// Programmatic defined mandatory filter
		Filter mandatoryFilter = getSelectionFilterMandatory();
		// In component configuration defined filter
		Filter configuredFilter = config.getSelectionFilter();
		return FilterFactory.and(nodeSupportedFilter, selectionAllowedFilter, mandatoryFilter, configuredFilter);
	}

	/**
	 * Selection listener to the displayed tree nodes.
	 *
	 * This will be called when the user changes the selected nodes.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public class SelectionModelListener implements SelectionListener {

		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> oldSelection, Set<?> newSelection) {
			{
				Set<DefaultTreeUINode> newSelectedNodes = unsafeCast(newSelection);
				if (_expandSelected) {
					for (DefaultTreeUINode selectedNode : newSelectedNodes) {
						if (selectedNode != null) {
							_treeModel.setExpanded(selectedNode, true);
						}
					}
				}

				setSelectionPathToChannel(newSelectedNodes);

				if (isVisible()) {
					displaySelection();
				}
			}
		}

	}

	boolean setSelectionPathToChannel(Collection<? extends TLTreeNode<?>> newSelection) {
		Object newSelectionPath;
		if (isInMultiSelectionMode()) {
			newSelectionPath =
				newSelection.stream().map(TreeComponent::toBusinessObjectPath).collect(Collectors.toSet());
		} else {
			switch (newSelection.size()) {
				case 0:
					newSelectionPath = null;
					break;
				case 1:
					newSelectionPath = toBusinessObjectPath(CollectionUtils.extractSingleton(newSelection));
					break;
				default:
					throw new IllegalArgumentException(
						"Multiple selection " + newSelection + " for single selection tree: " + this);
			}
		}
		return setSelectionPath(newSelectionPath);
	}

	private static List<Object> toBusinessObjectPath(TLTreeNode<?> node) {
		List<Object> path = TLTreeModelUtil.createPathToRootUserObject(node);
		Collections.reverse(path);
		return path;
	}

	private Collection<? extends List<?>> selectionPathsFromChannel() {
		Object selectionPath = getSelectionPath();
		if (isInMultiSelectionMode()) {
			return unsafeCast(selectionPath);
		} else {
			return CollectionUtil.singletonOrEmptySet((List<?>) selectionPath);
		}
	}

	/**
	 * Whether this {@link TreeComponent} is in multi selection mode.
	 * 
	 * @return <code>true</code> if {@link #getSelected()} returns a collection of element (either
	 *         empty or not), and <code>false</code> if {@link #getSelected()} returns the selected
	 *         object or <code>null</code> if nothing is selected.
	 */
	protected boolean isInMultiSelectionMode() {
		return getSelectionModel().isMultiSelectionSupported();
	}

	/**
	 * Casts {@link BuilderComponent#getBuilder()} to {@link TreeModelBuilder}.
	 */
	protected final TreeModelBuilder<Object> getTreeModelBuilder() {
		return unsafeCast(getBuilder());
	}

	/**
	 * Provide a mandatory selection filter for this tree, which should be invoked at any case,
	 * regardless of the configuration.
	 * 
	 * <p>
	 * Must be {@link FilterFactory#trueFilter()} if the tree doesn't demand such a behaviour.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This method is called from constructor, so it must be constant.
	 * </p>
	 */
	protected Filter getSelectionFilterMandatory() {
		return FilterFactory.trueFilter();
	}

	/**
	 * Returns a filter that decides, whether a given node can be selected in this tree.
	 * 
	 * <p>
	 * If the {@link #getSelectionFilter()} returns <code>true</code> for a given node, this node
	 * becomes clickable. Otherwise, the corresponding node is rendered as plain text.
	 * </p>
	 */
	public final Filter getSelectionFilter() {
		return _selectionFilter;
	}

	private boolean isSelectable(Object businessNode) {
		return getSelectionFilter().accept(businessNode);
	}

	private boolean isSelectableNode(TLTreeNode<?> node) {
		return node != null && _selectionModel.isSelectable(node) && (_rootVisible || node != _treeModel.getRoot());
	}

	final boolean needsSelectionChange(Collection<?> newSelectedObjects) {
		Collection<DefaultTreeUINode> selectedNodes = getSelection();
		if (CollectionUtils.isEmpty(selectedNodes)) {
			return !CollectionUtils.isEmpty(newSelectedObjects);
		}
		DefaultTreeUINode selectedNode = getFirst(selectedNodes);
		if (selectedNode.getModel() != _treeModel) {
			/* when tree model was reset, the selection model is not adapted. Therefore the tree
			 * model of the selected node may be of a foreign tree. In such a case the selected node
			 * must be adapted. */
			return true;
		}
		return !nodesHasSameBusinessObject(selectedNodes, newSelectedObjects);
	}

	private boolean internalSetSelectedPaths(Collection<? extends List<?>> paths) {
		if (CollectionUtils.isEmpty(paths)) {
			// Selection of null is as clearSelection.
			return internalClearSelection();
		}
		
		Set<TreeUINode<?>> selectableNodes = new HashSet<>();
		boolean hasSelectableObjects = false;

		for (List<?> path : paths) {
			Object businessObject = getLast(path);
			if (!isSelectable(businessObject)) {
				continue;
			}

			if (!hasSelectableObjects) {
				hasSelectableObjects = true;
				initTreeModel();
			}
			List<DefaultTreeUINode> touchedNodes = findNodes(businessObject);
			if (touchedNodes.isEmpty()) {
				DefaultTreeUINode node = createNodeForBusinessNode(businessObject);
				if (node != null) {
					selectableNodes.add(node);
				}
			} else {
				for (DefaultTreeUINode touchedNode : touchedNodes) {
					if (TLTreeModelUtil.sameBusinessObjectPath(touchedNode, path)) {
						selectableNodes.add(touchedNode);
					}
				}
			}
		}
		if (hasSelectableObjects) {
			if (selectableNodes.isEmpty()) {
				return installDefaultSelection();
			} else {
				Collection<DefaultTreeUINode> oldSelection = getSelection();

				setSelection(selectableNodes);

				return hasSameBusinessObject(oldSelection, selectableNodes);
			}
		} else {
			return internalClearSelection();
		}
	}

	private boolean hasSameBusinessObject(Collection<? extends TreeUINode<?>> nodes1,
			Collection<? extends TreeUINode<?>> nodes2) {
		return nodesHasSameBusinessObject(nodes1, getBusinessObjects(nodes2));
	}

	private boolean nodesHasSameBusinessObject(Collection<? extends TreeUINode<?>> nodes,
			Collection<?> businessObjects) {
		return CollectionUtilShared.containsSame(getBusinessObjects(nodes), businessObjects);
	}

	private Set<Object> getBusinessObjects(Collection<? extends TreeUINode<?>> nodes) {
		Set<Object> nodeBusinessObjects = new HashSet<>();

		for (TreeUINode<?> node : nodes) {
			nodeBusinessObjects.add(getBusinessObject(node));
		}

		return nodeBusinessObjects;
	}

	private boolean containsNodeWithSameBusinessObject(Collection<? extends TreeUINode<?>> nodes, TLTreeNode<?> node) {
		return getBusinessObjects(nodes).contains(getBusinessObject(node));
	}

	private void setSelection(Set<? extends TreeUINode<?>> nodes) {
		SelectionUtil.setTreeSelection(_selectionModel, nodes);
	}

	private Collection<DefaultTreeUINode> getSelection() {
		return unsafeCast(_selectionModel.getSelection());
	}

	/**
	 * Creates a node for a business object. Returns <code>null</code> if the given business object
	 * is not part of the tree created by the {@link #getTreeModelBuilder()}.
	 */
	private DefaultTreeUINode createNodeForBusinessNode(Object businessObject) {
		if (Utils.equals(_treeModel.getRoot().getBusinessObject(), businessObject)) {
			return _treeModel.getRoot();
		}
		Collection<? extends Object> parents = getParentObjects(businessObject);
		if (parents.isEmpty()) {
			// business object is root of a different tree.
			return null;
		}
		Object parent = parents.iterator().next();
		DefaultTreeUINode parentNode = createNodeForBusinessNode(parent);
		if (parentNode != null) {
			// trigger creation of child nodes.
			parentNode.getChildren();
			List<DefaultTreeUINode> nodes = findNodes(businessObject);
			if (nodes.isEmpty()) {
				// Not found. An example is deleting a template in the scripting UI.
				return null;
			}
			return nodes.get(0);
		} else {
			return null;
		}
	}

	@Override
	protected void becomingVisible() {
		super.becomingVisible();
		initTreeModel();
		displaySelection();
	}

	private void displaySelection() {
		Collection<DefaultTreeUINode> selection = getSelection();
		if (CollectionUtils.isEmpty(selection)) {
			// no selection to display.
			return;
		}
		for (DefaultTreeUINode selectedNode : selection) {
			DefaultTreeUINode node = selectedNode;
			while (true) {
				if (node.isDisplayed()) {
					// node is displayed
					break;
				}
				DefaultTreeUINode parent = node.getParent();
				if (parent == null) {
					break;
				}
				parent.setExpanded(true);
				node = parent;
			}
			assert selectedNode
				.isDisplayed() : "Each parent node up to a displayed ancestor is expanded, therefore selection is displayed.";
		}

	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);
		_securitySelectionFilter.ensureDelegationDestination(context, getMainLayout());
	}

	@Override
	public final void setDelegationDestination(BoundChecker checker) {
		_securitySelectionFilter.setDelegationDestination(checker);
	}

	private Object getBusinessObject(TLTreeNode<?> node) {
		if (node != null) {
			return node.getBusinessObject();
		} else {
			return null;
		}
	}

	@Override
	public void clearSelection() {
		internalClearSelection();
	}

	private boolean internalClearSelection() {
		if (!CollectionUtils.isEmpty(selectionPathsFromChannel())) {
			getSelectionModel().clear();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public final SelectionModel getSelectionModel() {
		return _selectionModel;
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		if (_treeModel != null) {
			resetTreeModel();
		}
	}

	/**
	 * Resets the {@link #getTreeModel()} in reaction on a model change.
	 */
	public void resetTreeModel() {
		_treeModel.removeTreeModelListener(_focusExpanded);
		_treeModel.removeTreeModelListener(this);
		_treeModel = null;
		removeViews();
		invalidate();
	}

	private void removeViews() {
		_scrollContainer = null;
		_treeControl = null;
	}

	@Override
	public boolean isModelValid() {
		return super.isModelValid() && _isSelectionValid && _treeModel != null && !_focusAfterValidation;
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean eventsSent = super.validateModel(context);
		eventsSent |= initTreeModel();
		if (!_isSelectionValid) {
			validateSelection();

			_isSelectionValid = true;
		}

		if (_focusAfterValidation) {
			/* set variable to false before focus selection, because the method calls
			 * isModelValid */
			_focusAfterValidation = false;
			focusSelection();
		}
		return eventsSent;
	}

	@Override
	public void writeBody(ServletContext aContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		focusSelection();
		getScrollContainer().write(DefaultDisplayContext.getDisplayContext(request), out);
	}

	/**
	 * The {@link ScrollContainerControl} displaying the tree as contents.
	 */
	protected final ScrollContainerControl getScrollContainer() {
		if (_scrollContainer == null) {
			ScrollContainerControl container = new ScrollContainerControl();
			container.setContent(getTreeControl());
			_scrollContainer = container;
		}
		return _scrollContainer;
	}

	/**
	 * The {@link TreeControl} displaying the tree.
	 */
	protected final TreeControl getTreeControl() {
		if (_treeControl == null) {
			TreeControl tree = createTreeControl();
			getTreeModel().addTreeModelListener(_focusExpanded);
			_treeControl = tree;
		}
		return _treeControl;
	}

	/**
	 * Creates the {@link TreeControl} for displaying the tree.
	 * 
	 * @see #getTreeControl()
	 */
	protected TreeControl createTreeControl() {
		DefaultTreeData treeData =
			new DefaultTreeData(Maybe.some(this), getTreeModel(), getSelectionModel(), _treeRenderer, _dragSource,
				_dropTargets);

		if (ScriptingRecorder.mustNotRecord(this)) {
			ScriptingRecorder.annotateAsDontRecord(treeData);
		}

		TreeControl control = createTreeControl(treeData);
		CheckScope checkScope = MasterSlaveCheckProvider.INSTANCE.getCheckScope(this);
		control.setNodeSelectionVetoListener(new ChangeCheckNodeSelectionVetoListener(checkScope));
		control.setNodeExpansionVetoListener(getExpansionVetoListener());
		control.setContextMenuProvider(_contextMenuFactory.createContextMenuProvider(this));
		return control;
	}

	/**
	 * Hook for creating the {@link TreeControl} instance for the given model.
	 * 
	 * @see #createTreeControl()
	 * @see #getTreeControl()
	 */
	protected TreeControl createTreeControl(DefaultTreeData treeData) {
		return new TreeControl(treeData);
	}

	/**
	 * Scrolls to make the selected node visible.
	 */
	protected final void focusSelection() {
		if (_focusSelection) {
			if (!isVisible()) {
				/* No need to scroll if not visible, because tree is automatically scrolled when
				 * component is rendered. Moreover it may be that currently no tree model is
				 * available, because the component is never displayed (e.g. HelpTree in external
				 * window). */
				return;
			}
			if (isModelValid()) {
				internalFocusSelection();
			} else {
				/* Execute focus later. */
				_focusAfterValidation = true;
			}
		}
	}

	/**
	 * Actually trigges the scroll operation.
	 * 
	 * @see #focusSelection()
	 */
	protected void internalFocusSelection() {
		TreeControl treeControl = getTreeControl();
		Set<?> selection = treeControl.getSelectionModel().getSelection();
		if (selection.isEmpty()) {
			// nothing to focus.
			getScrollContainer().resetScrollRange();
		} else {
			Object selectedNode = selection.iterator().next();
			getScrollContainer().scrollToRange(new TreeNodeRange(treeControl, selectedNode));
		}
	}

	/**
	 * Returns the {@link TreeUIModel} created from the {@link #getTreeModelBuilder()}.
	 * 
	 * @return May be <code>null</code> if accessed after model change and before validation.
	 */
	protected final DefaultTreeUINodeModel getTreeModel() {
		return _treeModel;
	}

	@Override
	public TreeData getTreeData() {
		return getTreeControl().getData();
	}

	/**
	 * Initialises the {@link #getTreeModel()} with the root business object received from the model
	 * builder.
	 */
	private boolean initTreeModel() {
		if (_treeModel != null) {
			return false;
		}

		Collection<? extends List<?>> selectedPaths = selectionPathsFromChannel();
		Object root = getTreeModelBuilder().getModel(getModel(), this);
		_treeModel = new IndexedTreeUINodeModel(this, root);
		_treeModel.setRootVisible(_rootVisible && root != null);
		/* Add listener before expanding node to ensure that preload is triggered. */
		_treeModel.addTreeModelListener(this);
		_treeModel.setExpanded(_treeModel.getRoot(), _expandRoot);
		if (CollectionUtils.isEmpty(selectedPaths)) {
			return installDefaultSelection();
		} else {
			return internalSetSelectedPaths(selectedPaths);
		}
	}

	private boolean installDefaultSelection() {
		TreeUINode<?> defaultSelection = getDefaultSelection(_treeModel);
		if (isSelectableNode(defaultSelection)) {
			Collection<DefaultTreeUINode> oldSelection = getSelection();

			setSelection(Collections.singleton(defaultSelection));

			return containsNodeWithSameBusinessObject(oldSelection, defaultSelection);
		} else {
			return internalClearSelection();
		}
	}

	private boolean isAncestorOrSelf(Object node, Object ancestor) {
		TreeModelBuilder<Object> treeModelBuilder = getTreeModelBuilder();
		/* Special handling for the case, that there is only one parent from the node to ancestor. */
		while (true) {
			if (node == ancestor) {
				return true;
			}
			Collection<? extends Object> parents = treeModelBuilder.getParents(this, node);
			switch(parents.size()) {
				case 0: {
					return false;
				}
				case 1: {
					node = parents.iterator().next();
					break;
				}
				default: {
					/* The node has more than one parent. A complex state handling must occur.
					 * 
					 * This is necessary, because in a cyclic tree there may be more than parent,
					 * e.g. for Root -> A -> A .... The node A would have both A and Root as parent. */
					return isAncestorOrSelf(parents, ancestor, new HashSet<>());
				}
			}
		}
	}

	private boolean isAncestorOrSelf(Collection<? extends Object> nodes, Object ancestor, Set<Object> processed) {
		for (Object node : nodes) {
			if (node == ancestor) {
				return true;
			}
			boolean alreadyProcessed = !processed.add(node);
			if (alreadyProcessed) {
				continue;
			}
			if (isAncestorOrSelf(getParentObjects(node), ancestor, processed)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the default selected node if current selection is <code>null</code> or not
	 * <code>null</code> but not supported.
	 * 
	 * @see #isSelectable(Object)
	 */
	protected DefaultTreeUINode getDefaultSelection(DefaultTreeUINodeModel treeModel) {
		if (!((Config) getConfig()).getDefaultSelection()) {
			return null;
		}
		DefaultTreeUINode root = treeModel.getRoot();
		if (treeModel.isRootVisible()) {
			return firstSelectableNode(root);
		} else {
			List<DefaultTreeUINode> children = root.getChildren();
			if (!children.isEmpty()) {
				return firstSelectableNode(children.get(0));
			} else {
				// Just root but root not visible.
				return null;
			}
		}
	}

	private DefaultTreeUINode firstSelectableNode(DefaultTreeUINode node) {
		if (isSelectableNode(node)) {
			return node;
		}
		for (DefaultTreeUINode child : node.getChildren()) {
			DefaultTreeUINode selectable = firstSelectableNode(child);
			if (selectable != null) {
				return selectable;
			}
		}
		return null;
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelChangedEvent(Object model, Object someChangedBy) {
		_securitySelectionFilter.handleModelChangedEvent(model, someChangedBy);
		if (_treeModel != null) {
			updateNodeObjects(getTreeModelBuilder().getNodesToUpdate(this, model));
			updateNodeObject(model);

			invalidateSelection();
		}

		return super.receiveModelChangedEvent(model, someChangedBy);
	}

	private void invalidateSelection() {
		_isSelectionValid = false;
	}

	private void validateSelection() {
		Collection<DefaultTreeUINode> selection = getSelection();
		if (CollectionUtils.isEmpty(selection)) {
			installDefaultSelection();
		} else {
			Set<TreeUINode<?>> newSelection = new HashSet<>();
			for (TreeUINode<?> selectedNode : selection) {
				if (isSelectableNode(selectedNode)) {
					if (selectedNode.isAlive()) {
						newSelection.add(selectedNode);
					} else {
						newSelection.addAll(findNodes(selectedNode.getBusinessObject()));
					}
				}
			}

			if (newSelection.isEmpty()) {
				installDefaultSelection();
			} else {
				setSelection(newSelection);
			}
		}
	}

	private List<DefaultTreeUINode> findAllNodes(Collection<?> businessObject) {
		return businessObject.stream()
			.map(_treeModel.getIndex()::getNodes)
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	private List<DefaultTreeUINode> findNodes(Object businessObject) {
		return _treeModel.getIndex().getNodes(businessObject);
	}

	private void updateNodeObjects(Collection<? extends Object> nodeObjects) {
		for (Object nodeObject : nodeObjects) {
			updateNodeObject(nodeObject);
		}
	}

	private void updateNodeObject(Object nodeObject) {
		if (nodeSupported(nodeObject)) {
			Collection<DefaultTreeUINode> newParentNodes = getParentNodes(nodeObject);
			List<DefaultTreeUINode> nodes = findNodes(nodeObject);

			if (newParentNodes.size() == 1 && nodes.size() == 1) {
				moveNode(CollectionUtils.extractSingleton(nodes), CollectionUtils.extractSingleton(newParentNodes));
			} else {
				updateOldAndNewParents(nodes, newParentNodes);
			}
		}
	}

	private void updateOldAndNewParents(List<DefaultTreeUINode> nodes, Collection<DefaultTreeUINode> parentNodes) {
		updateChildrenInternal(parentNodes);

		for (DefaultTreeUINode node : nodes) {
			node.updateNodeProperties();

			if (_showOnlySelectableNodes && !isSelectableNode(node)) {
				updateNextSelectableAncestors(node.getBusinessObject());
			} else {
				updateOldParent(node);
				updateChildrenInternal(node);
			}
		}
	}

	private void moveNode(DefaultTreeUINode node, DefaultTreeUINode newParentNode) {
		node.updateNodeProperties();

		if (_showOnlySelectableNodes && !isSelectableNode(node)) {
			updateNextSelectableAncestors(node.getBusinessObject());
		} else {
			if (node.getParent() != newParentNode) {
				node.moveTo(newParentNode);
			}

			updateChildrenInternal(node);
		}
	}

	private void updateOldParent(DefaultTreeUINode node) {
		DefaultTreeUINode oldParentNode = node.getParent();
		
		if (oldParentNode != null) {
			updateChildrenInternal(oldParentNode);
		}
	}

	private void updateChildrenInternal(Collection<DefaultTreeUINode> nodes) {
		for (DefaultTreeUINode node : nodes) {
			updateChildrenInternal(node);
		}
	}

	private void updateChildrenInternal(DefaultTreeUINode node) {
		if (node.isInitialized()) {
			TLTreeModelUtil.updateChildren(node, getChildrenObjects(node.getBusinessObject()));
		}
	}

	private Collection<DefaultTreeUINode> getParentNodes(Object nodeObject) {
		Collection<DefaultTreeUINode> nodes = new HashSet<>();

		for (Object parentObject : getParentObjects(nodeObject)) {
			nodes.addAll(findNodes(parentObject));
		}

		return nodes;
	}

	private Collection<? extends Object> getParentObjects(Object businessObject) {
		return getTreeModelBuilder().getParents(this, businessObject);
	}

	private Iterator<? extends Object> getChildrenObjects(Object businessObject) {
		return getTreeModelBuilder().getChildIterator(this, businessObject);
	}

	@Override
	protected boolean receiveModelCreatedEvent(Object model, Object changedBy) {
		_securitySelectionFilter.handleModelCreatedEvent(model, changedBy);
		if (_treeModel != null) {
			updateNodeObjects(getTreeModelBuilder().getNodesToUpdate(this, model));
			if (nodeSupported(model)) {
				if (_showOnlySelectableNodes) {
					updateNextSelectableAncestors(model);
				} else {
					Collection<? extends Object> parents = getParentObjects(model);
					if (parents.isEmpty()) {
						resetTreeModel();
					} else {
						updateChildren(parents);
					}
				}
			}

			invalidateSelection();
		}
		return super.receiveModelCreatedEvent(model, changedBy);
	}

	private void updateChildren(Collection<? extends Object> businessObjects) {
		for (Object businessObject : businessObjects) {
			updateChildrenInternal(findNodes(businessObject));
		}
	}

	private void updateNextSelectableAncestors(Object model) {
		TreeModelBuilder<Object> modelBuilder = getTreeModelBuilder();
		Set<Object> selectableAncestors =
			InlineSet.toSet(Object.class, addSelectableAncestors(modelBuilder, InlineSet.newInlineSet(), model));
		if (selectableAncestors.isEmpty()) {
			selectableAncestors = Collections.singleton(_treeModel.getRoot().getBusinessObject());
		}
		updateChildren(selectableAncestors);
	}

	/**
	 * Adds each next selectable ancestor of the given business node to the given inline set.
	 * 
	 * @param modelBuilder
	 *        Builder to get business children.
	 * @param inlineSet
	 *        The inline set to add selectable ancestor node to.
	 * @param model
	 *        The business node to get selectable ancestors of.
	 * 
	 * @return The given inline set.
	 */
	private Object addSelectableAncestors(TreeModelBuilder<Object> modelBuilder, Object inlineSet, Object model) {
		Collection<? extends Object> parents = modelBuilder.getParents(this, model);
		for (Object parent : parents) {
			if (isSelectable(parent)) {
				inlineSet = InlineSet.add(Object.class, inlineSet, parent);
			} else {
				inlineSet = addSelectableAncestors(modelBuilder, inlineSet, parent);
			}
		}
		return inlineSet;
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		_securitySelectionFilter.handleModelDeletedEvent(models, changedBy);
		boolean becameInvalid;
		if (_treeModel != null) {
			if (models.contains(_treeModel.getRoot().getBusinessObject())) {
				resetTreeModel();
				becameInvalid = true;
			} else {
				List<DefaultTreeUINode> knownNodes = findAllNodes(models);
				if (knownNodes.isEmpty()) {
					// Nodes never displayed: No adaption necessary.
					becameInvalid = false;
				} else {
					boolean selectionChanged = selectNextPossibleParent(models);

					for (DefaultTreeUINode node : knownNodes) {
						if (!node.isAlive()) {
							/* It may happen that a parent node was processed before. In this case
							 * the parent (and therefore the child node) are already removed from
							 * parent. */
							assert _treeModel.getIndex().getNodes(node.getBusinessObject()).isEmpty();
							continue;
						}
						removeNodeFromParent(node);
					}
					becameInvalid = selectionChanged;
				}
			}
		} else {
			becameInvalid = false;
		}
		boolean superInvalidated = super.receiveModelDeletedEvent(models, changedBy);
		return becameInvalid || superInvalidated;
	}

	private void removeNodeFromParent(DefaultTreeUINode node) {
		DefaultTreeUINode parent = node.getParent();
		if (_showOnlySelectableNodes) {
			/* Remove not just the node itself but also the parent when it is not selectable itself
			 * and there are no selectable siblings. */
			while (true) {
				if (parent.getChildCount() > 1) {
					/* There is a sibling of the node to remove. Therefore there is still a reason
					 * for the parent to be in the tree, regardless whether it is selectable or not. */
					break;
				}
				if (isSelectableNode(parent)) {
					/* Selectable parents must not be removed. */
					break;
				}
				DefaultTreeUINode grandfather = parent.getParent();
				if (grandfather == null) {
					// parent is root which can not be removed
					break;
				}
				node = parent;
				parent = grandfather;
			}
		}
		parent.removeChild(parent.getIndex(node));
	}

	/**
	 * Finds a valid node next to the currently selected one that is not deleted.
	 * 
	 * @return <code>true</code> if selected business object changed.
	 */
	private boolean selectNextPossibleParent(Set<?> deleted) {
		final Collection<? extends TreeUINode<?>> selection = getSelection();
		if (CollectionUtils.isEmpty(selection)) {
			// no selection nothing to adapt.
			return false;
		}
		if (selection.size() > 1) {
			_selectionModel.removeFromSelection(selection);
			return true;
		} else {
			TreeUINode<?> selectedNode = CollectionUtils.extractSingleton(selection);
			TreeUINode<?> ancestor = topmostDeletedAncestor(deleted, selectedNode);
			if (ancestor == null) {
				// Deletion does not interfere with selection.
				return false;
			}

			TreeUINode<?> validParent = ancestor.getParent();
			if (validParent == null) {
				// Root was deleted, cannot repair.
				clearSelection();
				return true;
			}

			final int deletedIndex = validParent.getIndex(ancestor);
			int count = validParent.getChildCount();
			// Search for following index that is not deleted.
			int newIndex = deletedIndex + 1;
			while (newIndex < count && !ComponentUtil.isValid(validParent.getChildAt(newIndex))) {
				newIndex++;
			}
			if (newIndex == count) {
				// Search for preceding index that is not deleted.
				newIndex = deletedIndex - 1;
				while (newIndex >= 0 && !ComponentUtil.isValid(validParent.getChildAt(newIndex))) {
					newIndex--;
				}
			}

			TreeUINode<?> newSelection;
			if (newIndex >= 0) {
				newSelection = validParent.getChildAt(newIndex);
			} else {
				newSelection = validParent;
			}

			if (isSelectableNode(newSelection)) {
				setSelection(Collections.singleton(newSelection));

				return !Utils.equals(getBusinessObject(selectedNode), getBusinessObject(newSelection));
			} else {
				return internalClearSelection();
			}
		}
	}

	private TreeUINode<?> topmostDeletedAncestor(Set<?> deleted, TreeUINode<?> selection) {
		TreeUINode<?> result = null;

		TreeUINode<?> ancestor = selection;
		do {
			if (deleted.contains(ancestor) || !ComponentUtil.isValid(ancestor.getBusinessObject())) {
				result = ancestor;
			}

			ancestor = ancestor.getParent();
		} while (ancestor != null);
		return result;
	}

	boolean nodeSupported(Object businessNode) {
		if (!ComponentUtil.isValid(businessNode)) {
			return false;
		}
		return getTreeModelBuilder().supportsNode(this, businessNode);
	}

	/**
	 * This method eventually returns <code>null</code> if {@link #_showOnlySelectableNodes}.
	 * 
	 * <p>
	 * If {@link #_showOnlySelectableNodes} than a return value of <code>null</code> means that
	 * neither the given business object nor any object in the business sub tree is selectable.
	 * </p>
	 * 
	 * @see com.top_logic.layout.tree.model.TreeBuilder#createNode(com.top_logic.layout.tree.model.AbstractMutableTLTreeModel,
	 *      com.top_logic.layout.tree.model.AbstractMutableTLTreeNode, java.lang.Object)
	 */
	@Override
	public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model, DefaultTreeUINode parent,
			Object userObject) {
		DefaultTreeUINode newNode = new DefaultTreeUINode(model, parent, userObject);

		if (!_showOnlySelectableNodes) {
			// No special handling in default case
			return newNode;
		}
		if (parent == null) {
			// Do not filter root node to ensure the tree contains at least one node.
			return newNode;
		}
		if (isSelectable(userObject)) {
			// Node can be selected and therefore displayed.
			return newNode;
		}
		/* Fetching children recursively builds the tree. If the returned list is empty than each
		 * call to this method with the business children return null. I.e. in each subtree of each
		 * child there is no node displayed, therefore also this node is not displayed. */
		List<? extends DefaultTreeUINode> children = model.getChildren(newNode);
		if (children.isEmpty()) {
			return null;
		}
		return newNode;
	}

	@Override
	public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
		Object businessObject = node.getBusinessObject();

		TreeModelBuilder<Object> treeModelBuilder = getTreeModelBuilder();
		if (treeModelBuilder.isLeaf(this, node.getBusinessObject())) {
			return new ArrayList<>(0);
		}
		ArrayList<DefaultTreeUINode> childNodes = new ArrayList<>();
		Iterator<? extends Object> childIterator = treeModelBuilder.getChildIterator(this, businessObject);
		while (childIterator.hasNext()) {
			DefaultTreeUINode childNode = createNode(node.getModel(), node, childIterator.next());
			if (childNode == null) {
				/* This may happen in case only selectable nodes shall be shown. As non leaf nodes
				 * must be displayed also if they are not selectable, and the TreeBuilder API is
				 * used a return value of null means no node within the business subtree can be
				 * selected. */
				continue;
			}
			childNodes.add(childNode);
		}
		return childNodes;
	}

	@Override
	public boolean isFinite() {
		return getTreeModelBuilder().canExpandAll();
	}

	private static class IndexedTreeUINodeModel extends DefaultTreeUINodeModel {

		private final UserObjectIndex<DefaultTreeUINode> _index = new UserObjectIndex<>();

		public IndexedTreeUINodeModel(TreeBuilder<DefaultTreeUINode> builder, Object rootUserObject) {
			super(builder, rootUserObject);
		}

		public UserObjectIndex<DefaultTreeUINode> getIndex() {
			return _index;
		}

		@Override
		protected void handleInitNode(DefaultTreeUINode node) {
			super.handleInitNode(node);
			_index.handleInitNode(node);
		}

		@Override
		protected void handleRemoveNode(DefaultTreeUINode subtreeRootParent, DefaultTreeUINode node) {
			super.handleRemoveNode(subtreeRootParent, node);
			_index.handleRemoveNode(subtreeRootParent, node);
		}

	}

	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		/* Changes the selection if collapsing would hide selection. This is done before actually
		 * collapsing the node, because if this is done after collapsing, setting selection may
		 * re-expand the currently collapsed node. */
		switch (evt.getType()) {
			case TreeModelEvent.BEFORE_COLLAPSE: {
				if (_adjustSelectionWhenCollapsing) {
					DefaultTreeUINode nodeToCollapse = (DefaultTreeUINode) evt.getNode();
					Collection<TLTreeNode<?>> selectionInsideCollapsedTree =
						getSelectionInsideCollapsedTree(nodeToCollapse);
					Collection<DefaultTreeUINode> oldSelection = getSelection();

					Set<TreeUINode<?>> newSelection = new HashSet<>(oldSelection);
					newSelection.removeAll(selectionInsideCollapsedTree);
					if (!CollectionUtils.isEmpty(oldSelection) && !selectionInsideCollapsedTree.isEmpty()) {
						if (isSelectableNode(nodeToCollapse)) {
							newSelection.add(nodeToCollapse);
						}
						setSelection(newSelection);
					}
				}
				break;
			}
			case TreeModelEvent.BEFORE_EXPAND:{
				PreloadOperation loadForExpansion = getTreeModelBuilder().loadForExpansion();
				if (loadForExpansion != NoPreload.INSTANCE) {
					PreloadContext expansionContext = new PreloadContext();
					TLTreeNode<?> nodeToExpand = (TLTreeNode<?>) evt.getNode();
					nodeToExpand.set(PRELOAD_CONTEXT_PROPERTY, expansionContext);
					loadForExpansion.prepare(expansionContext,
						Collections.singletonList(nodeToExpand.getBusinessObject()));
				}
				break;
			}
			case TreeModelEvent.AFTER_EXPAND: {
				TLTreeNode<?> expandedNode = (TLTreeNode<?>) evt.getNode();
				expandedNode.reset(PRELOAD_CONTEXT_PROPERTY).close();
			}
		}
	}

	/**
	 * Returns an {@link ExpansionVetoListener} that must be used by subclasses to ensure no
	 * relevant is dirty when collapsing a node would hide the current selection.
	 */
	protected ExpansionVetoListener getExpansionVetoListener() {
		return new ExpansionVetoListener() {

			@Override
			public void checkVeto(TreeControl tree, Object node, boolean expanded) throws VetoException {
				TreeComponent comp = TreeComponent.this;
				if (expanded || !comp._adjustSelectionWhenCollapsing) {
					return;
				}
				Collection<TLTreeNode<?>> selectionInsideCollapsedTree = getSelectionInsideCollapsedTree(node);
				if (CollectionUtils.isEmpty(getSelection()) || selectionInsideCollapsedTree.isEmpty()) {
					return;
				}
				CheckScope checkScope = MasterSlaveCheckProvider.INSTANCE.getCheckScope(comp);
				DirtyHandling.checkVeto(checkScope);
			}
		};
	}

	Collection<TLTreeNode<?>> getSelectionInsideCollapsedTree(Object nodeToCollapse) {
		Collection<TLTreeNode<?>> selectionInsideCollapsedTree = new HashSet<>();

		for (TLTreeNode<?> selectedNode : getSelection()) {
			TLTreeNode<?> parent = selectedNode.getParent();
			while (parent != null) {
				if (parent.equals(nodeToCollapse)) {
					selectionInsideCollapsedTree.add(selectedNode);
					break;
				}

				parent = parent.getParent();
			}
		}

		return selectionInsideCollapsedTree;
	}

	@Override
	protected Map<String, ChannelSPI> channels() {
		return LayoutComponent.channels(super.channels(),
			isInMultiSelectionMode() ? MULTI_SELECTION_PATH_SPI : SINGLE_SELECTION_PATH_SPI);
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		selectionChannel().addListener(TreeComponent::handleNewSelectionChannelValue);
		selectionChannel().addVetoListener(TreeComponent::isValidSelectionChannelChange);

		selectionPathChannel().addListener(TreeComponent::handleNewSelectionPathChannelValue);
		selectionPathChannel().addVetoListener(TreeComponent::isValidSelectionPathChannelChange);
	}

	/**
	 * {@link ChannelListener} for {@link #selectionChannel()}.
	 *
	 * @param sender
	 *        The changed channel.
	 * @param oldValue
	 *        Old value of the channel.
	 * @param newValue
	 *        New value for the channel.
	 */
	private static void handleNewSelectionChannelValue(ComponentChannel sender, Object oldValue, Object newValue) {
		TreeComponent tree = (TreeComponent) sender.getComponent();

		Object selectionPath = tree.getSelectionPath();
		if (!tree.isInMultiSelectionMode()) {
			if (newValue == null) {
				if (selectionPath == null) {
					return;
				} else {
					tree.setSelectionPath(null);
				}
			} else {
				if (selectionPath == null) {
					tree.setSelectionPath(buildRandomPathForObject(tree, newValue));
				} else {
					List<?> path = unsafeCast(selectionPath);
					if (newValue.equals(getLast(path))) {
						return;
					} else {
						tree.setSelectionPath(buildRandomPathForObject(tree, newValue));
					}
				}
			}
		} else {
			Collection<?> newSetValue = (Collection<?>) newValue;
			Collection<List<?>> selectionPaths = unsafeCast(selectionPath);
			switch (newSetValue.size()) {
				case 0: {
					tree.setSelectionPath(Collections.emptySet());
					break;
				}
				default: {
					Set<Object> newSelectionPaths = new HashSet<>();
					Set<Object> newSelection = new HashSet<>();
					boolean withDeselection = false;
					for (List<?> currentPath : selectionPaths) {
						Object selected = getLast(currentPath);
						if (newSetValue.contains(selected)) {
							newSelectionPaths.add(currentPath);
							newSelection.add(selected);
						} else {
							// Element was deselected
							withDeselection = true;
						}
					}
					if (!withDeselection && newSelection.containsAll(newSetValue)) {
						// same selected objects
						return;
					} else {
						List<?> tmp = new ArrayList<>(newSetValue);
						tmp.removeAll(newSelection);
						for (Object newlySelected : tmp) {
							newSelectionPaths.add(buildRandomPathForObject(tree, newlySelected));
						}
						tree.setSelectionPath(newSelectionPaths);
					}
				}
			}
		}
	}

	private static List<Object> buildRandomPathForObject(TreeComponent tree, Object bo) {
		Set<Object> alreadySeen = new HashSet<>();
		List<Object> path = new ArrayList<>();
		pathStep:
		while (bo != null) {
			alreadySeen.add(bo);
			List<? extends TLTreeNode<?>> nodes = tree.findNodes(bo);
			if (!nodes.isEmpty()) {
				TLTreeNode<?> node = nodes.get(0);
				while (node != null) {
					path.add(node.getBusinessObject());
					node = node.getParent();
				}
				break;
			}
			path.add(bo);
			Collection<? extends Object> parentObjects = tree.getParentObjects(bo);
			if (parentObjects.isEmpty()) {
				// reached root
				break;
			}
			for (Object parent : parentObjects) {
				if (!alreadySeen.contains(parent)) {
					bo = parent;
					continue pathStep;
				}
			}
			throw new IllegalArgumentException("Can not create path. All parents of " + bo
					+ " already contained in path " + path + ". Parents: " + parentObjects);
		}
		Collections.reverse(path);
		return path;

	}

	/**
	 * {@link ChannelListener} for {@link #selectionPathChannel()}.
	 *
	 * @param sender
	 *        The changed channel.
	 * @param oldValue
	 *        Old value of the channel.
	 * @param newValue
	 *        New value for the channel.
	 */
	private static void handleNewSelectionPathChannelValue(ComponentChannel sender, Object oldValue, Object newValue) {
		TreeComponent tree = (TreeComponent) sender.getComponent();
		
		Collection<? extends List<?>> selectionPaths;
		Object selectionChannelValue;
		if (!tree.isInMultiSelectionMode()) {
			if (newValue == null) {
				selectionChannelValue = null;
				selectionPaths = Collections.emptySet();
			} else {
				List<?> selectedPath = (List<?>) newValue;
				selectionChannelValue = getLast(selectedPath);
				selectionPaths = Collections.singleton(selectedPath);
				tree.setModel(retrieveModelFromPath(tree, selectedPath));
			}
		} else {
			selectionPaths = unsafeCast(newValue);
			if (!selectionPaths.isEmpty()) {
				Set<Object> lastElements = new HashSet<>();
				for (List<?> path : selectionPaths) {
					lastElements.add(getLast(path));
				}
				selectionChannelValue = lastElements;
				// all have the same model
				tree.setModel(retrieveModelFromPath(tree, getFirst(selectionPaths)));
			} else {
				selectionChannelValue = Collections.emptySet();
			}
		}
		tree.setSelected(selectionChannelValue);
		
		boolean changed = tree.internalSetSelectedPaths(selectionPaths);
		if (changed) {
			tree.focusSelection();
		}
	}

	private static Object retrieveModelFromPath(TreeComponent tree, List<?> path) {
		return tree.getTreeModelBuilder().retrieveModelFromNode(tree, getLast(path));
	}

	/**
	 * Casts the given value to anything you want.
	 * 
	 * @return The given value.
	 */
	@SuppressWarnings("unchecked")
	static <T> T unsafeCast(Object value) {
		return (T) value;
	}

	/**
	 * {@link ChannelValueFilter} for {@link #selectionChannel()}.
	 *
	 * @param sender
	 *        Channel about to change.
	 * @param oldValue
	 *        Current value of the channel.
	 * @param newValue
	 *        Potential new value for the channel.
	 * @return Whether the value is a valid channel value.
	 */
	private static boolean isValidSelectionChannelChange(ComponentChannel sender, Object oldValue, Object newValue) {
		TreeComponent tree = (TreeComponent) sender.getComponent();

		Collection<?> newSelection = CollectionUtilShared.asSet(newValue);
		for (Object selectedObject : newSelection) {
			if (selectedObject != null && !tree.nodeSupported(selectedObject)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@link ChannelValueFilter} for {@link #selectionPathChannel()}.
	 *
	 * @param sender
	 *        Channel about to change.
	 * @param oldValue
	 *        Current value of the channel.
	 * @param newValue
	 *        Potential new value for the channel.
	 * @return Whether the value is a valid channel value.
	 */
	private static boolean isValidSelectionPathChannelChange(ComponentChannel sender, Object oldValue,
			Object newValue) {
		TreeComponent tree = (TreeComponent) sender.getComponent();

		if (!tree.isInMultiSelectionMode()) {
			if (newValue == null) {
				return true;
			}
			return isValidPath(tree, newValue);
		} else {
			if (!(newValue instanceof Collection<?>)) {
				return false;
			}
			Collection<?> potentialPaths = (Collection<?>) newValue;
			switch(potentialPaths.size()) {
				case 0: return true;
				case 1: return isValidPath(tree, potentialPaths.iterator().next());
				default:
					Iterator<?> it = potentialPaths.iterator();
					Object firstPath = it.next();
					if (!isValidPath(tree, firstPath)) {
						return false;
					}
					Object newComponentModel = retrieveModelFromPath(tree, (List<?>) firstPath);
					do {
						Object nextPath = it.next();
						if (!isValidPath(tree, nextPath)) {
							return false;
						}
						if (!Utils.equals(newComponentModel, retrieveModelFromPath(tree, (List<?>) nextPath))) {
							// different models
							return false;
						}
					} while (it.hasNext());
					return true;
			}
		}
	}

	private static boolean isValidPath(TreeComponent tree, Object path) {
		if (path instanceof List<?>) {
			List<?> l = (List<?>) path;
			if (l.isEmpty()) {
				return false;
			}
			for (int i = l.size() - 1; i >= 0; i--) {
				Object node = l.get(i);
				if (!tree.nodeSupported(node)) {
					return false;
				}
				if (i > 0) {
					Collection<?> parents = tree.getParentObjects(node);
					if (!parents.contains(l.get(i - 1))) {
						return false;
					}
				}
			}
			if (!tree.isSelectable(getLast(l))) {
				return false;
			}
			return true;
		}
		return false;
	}

}