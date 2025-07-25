/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.check.MasterSlaveCheckProvider;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.component.InAppSelectable;
import com.top_logic.layout.component.SelectableWithSelectionModel;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.structure.ContentLayouting;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.ComponentRowSource;
import com.top_logic.layout.table.component.ComponentSelectionVetoListener;
import com.top_logic.layout.table.component.ComponentTableConfigProvider;
import com.top_logic.layout.table.component.InvalidSelectionVeto;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.component.TableComponentTableConfigProvider;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.SetTableResPrefix;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.layout.tree.component.WithSelectionPath;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel.TreeUINode;
import com.top_logic.layout.tree.model.IndexedTLTreeModel;
import com.top_logic.layout.tree.model.IndexedTreeTableModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.layout.tree.model.TreeViewConfig;
import com.top_logic.layout.tree.model.UserObjectIndex;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelConfig;
import com.top_logic.mig.html.SelectionUtil;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Utils;
import com.top_logic.util.model.ModelService;

/**
 * A {@link LayoutComponent} for displaying a table with a tree model.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TreeTableComponent extends BoundComponent
		implements SelectableWithSelectionModel, InAppSelectable, ControlRepresentable, TreeTableDataOwner,
		ComponentRowSource, WithSelectionPath {

	/**
	 * Configuration options for {@link TreeTableComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config
			extends BoundComponent.Config, TreeViewConfig, SelectionModelConfig, InAppSelectable.InAppSelectableConfig {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Tag name for the {@link TreeTableComponent}.
		 */
		String TAG_NAME = "tree-table";

		/**
		 * Configuration attribute name for {@link #getTable()}.
		 */
		String TABLE = "table";

		/**
		 * @see #getComponentTableConfigProvider()
		 */
		String COMPONENT_TABLE_CONFIG_PROVIDER_NAME = "componentTableConfigProvider";

		/**
		 * Configuration attribute name for {@link #getAdditionalConfiguration()}.
		 */
		String ADDITIONAL_CONFIGURATION_ATTRIBUTE_NAME = "additionalConfiguration";

		/**
		 * Configuration attribute name for {@link #getTreeBuilder()}.
		 */
		String XML_CONF_KEY_TREE_BUILDER = "treeBuilder";

		@Override
		@ClassDefault(TreeTableComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Override
		@BooleanDefault(true)
		boolean hasToolbar();
		/**
		 * Full qualified type names that are displayed.
		 * 
		 * @see TLModelUtil#findType(String)
		 */
		@Name(TableComponent.XML_CONF_KEY_OBJECT_TYPE)
		@Format(CommaSeparatedStrings.class)
		List<String> getObjectType();

		/**
		 * Check for missing types in {@link #getObjectType()}.
		 * <p>
		 * Enable to find problems in existing tables when doing the migration for ticket #27174.
		 * They will be logged as errors when the table is used. <br />
		 * Disable it afterwards, as this check is time consuming and slows the application down.
		 * </p>
		 * 
		 * @implNote See {@link #logErrorMissingTypeConfiguration(Set)} for the exact message.
		 */
		@Name("checkMissingTypeConfiguration")
		boolean shouldCheckMissingTypeConfiguration();

		/**
		 * {@link TreeBuilder} creating the displayed tree from the business model.
		 */
		@Name(XML_CONF_KEY_TREE_BUILDER)
		@Mandatory
		PolymorphicConfiguration<? extends TreeBuilder<? extends AbstractMutableTLTreeNode<?>>> getTreeBuilder();

		@Override
		@BooleanDefault(false)
		boolean isRootVisible();

		/**
		 * provider of {@link TableConfiguration}, that will be applied after XML based
		 * configuration.
		 */
		@Name(ADDITIONAL_CONFIGURATION_ATTRIBUTE_NAME)
		@InstanceFormat
		@InstanceDefault(TreeNodeUnwrappingProvider.class)
		TableConfigurationProvider getAdditionalConfiguration();

		/**
		 * Tree {@link TableConfig}.
		 */
		@Name(TABLE)
		TreeTableConfig getTable();

		/**
		 * {@link TableConfigurationProvider} for dynamically changing the
		 * {@link TableConfiguration}.
		 */
		@InstanceFormat
		@InstanceDefault(TableComponentTableConfigProvider.class)
		@Name(COMPONENT_TABLE_CONFIG_PROVIDER_NAME)
		ComponentTableConfigProvider getComponentTableConfigProvider();

		@Override
		@InstanceDefault(ContentLayouting.class)
		Layouting getContentLayouting();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			BoundComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(TreeTableExpandCollapseAll.EXPAND_ID);
			registry.registerButton(TreeTableExpandCollapseAll.COLLAPSE_ID);
		}
	}

	private static final Property<TreeTableComponent> TREE_OWNER =
		TypedAnnotatable.property(TreeTableComponent.class, "owner");

	/**
	 * @see #channels()
	 */
	private static final Map<String, ChannelSPI> CHANNELS =
		channels(ComponentRowSource.MODEL_ROWS_AND_SELECTION_CHANNEL, WithSelectionPath.SELECTION_PATH_SPI);

	private final Set<TLType> _types;

	private final TreeBuilder<? extends AbstractMutableTLTreeNode<?>> _treeBuilder;

	private SelectionVetoListener _selectionVetoListener;

	private final SelectionListener _selectionListener = new SelectionListener() {

		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> oldSelection, Set<?> newSelection) {
			Collection<TreeUINode<?>> oldSelectedNodes = unsafeCast(oldSelection);
			Set<AbstractTreeTableNode<?>> newSelectedNodes = unsafeCast(newSelection);

			if (_expandSelected) {
				for (AbstractTreeTableNode<?> newSelectedNode : newSelectedNodes) {
					if (newSelectedNode != null) {
						newSelectedNode.setExpanded(true);
					}
				}
			}

			boolean changed = setSelectionPathToChannel(newSelectedNodes);
			if (!changed) {
				/* There are two possible reasons why the selection has not changed: The channel may
				 * reject the selection. In this case the selection must be reverted. The second is
				 * that the event is triggered by the selection channel itself. In this case the
				 * selection must not be reverted. */
				Set<Object> newSelectedObjects = TreeUIModelUtil.getBusinessObjects(newSelectedNodes);
				if (!CollectionUtil.equals(selectionFromChannel(), newSelectedObjects)) {
					_selectionModel.setSelection(oldSelection);
				}
			}
		}
	};

	private boolean _rootVisible;

	private boolean _expandSelected;

	private boolean _expandRoot;

	private boolean _hasDefaultSelection;

	private boolean _adjustSelectionWhenCollapsing;

	private TableControl _control;

	private TreeTableData _treeTableData;

	private TableConfigurationProvider _tableConfigProvider;

	private final SelectionModel _selectionModel;

	private boolean _isSelectionValid;

	private CommandHandler _onSelectionChange;

	/**
	 * Legacy constructor for creating an {@link TreeTableComponent} via {@link Config}.
	 */
	@CalledByReflection
	public TreeTableComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_types = resolveTypes(context, config);
		_rootVisible = config.isRootVisible();
		_treeBuilder = context.getInstance(config.getTreeBuilder());
		_expandSelected = config.getExpandSelected();
		_expandRoot = config.getExpandRoot();
		_hasDefaultSelection = config.getDefaultSelection();
		_selectionModel = initSelectionModel(config);
		_adjustSelectionWhenCollapsing = config.adjustSelectionWhenCollapsing();
		TableConfig table = config.getTable();
		if (table != null) {
			_tableConfigProvider = TableConfigurationFactory.toProvider(context, table);
		}
		_onSelectionChange = context.getInstance(config.getOnSelectionChange());
	}

	@Override
	public CommandHandler getOnSelectionHandler() {
		return _onSelectionChange;
	}

	private Set<TLType> resolveTypes(InstantiationContext context, Config config) {
		return Set.copyOf(TLModelUtil.findTypes(context, ModelService.getApplicationModel(), config.getObjectType()));
	}

	/**
	 * Initializes the components selection model
	 */
	protected SelectionModel initSelectionModel(Config config) {
		SelectionModel selectionModel = config.getSelectionModelFactory().newSelectionModel(this);

		selectionModel.addSelectionListener(_selectionListener);

		return selectionModel;
	}

	@Override
	public boolean isModelValid() {
		return super.isModelValid() && _isSelectionValid && hasTreeTableData();
	}

	/**
	 * True if the component has a {@link TreeTableData}.
	 */
	public boolean hasTreeTableData() {
		return _treeTableData != null;
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean result = super.validateModel(context);
		// Prevent lazy initialization of the table's toolbar during rendering.
		if (!hasTreeTableData()) {
			TreeTableData tableData = getTableData();
			AbstractTreeTableModel<?> treeModel = tableData.getTree();
			expandRoot(treeModel);
		}
		if (!_isSelectionValid) {
			_isSelectionValid = true;

			validateSelection();
		}
		if (shouldCheckMissingTypeConfiguration()) {
			checkMissingTypeConfiguration();
		}
		return result;
	}

	/** @see Config#shouldCheckMissingTypeConfiguration() */
	protected boolean shouldCheckMissingTypeConfiguration() {
		return ((Config) getConfig()).shouldCheckMissingTypeConfiguration();
	}

	private void checkMissingTypeConfiguration() {
		Collection<?> rows = getTableData().getTableModel().getAllRows();
		Set<TLType> types = new HashSet<>();
		for (Object row : rows) {
			/* Unpack tree nodes. Repeat it, as there are allegedly rare cases of trees with
			 * TLTreeNodes in TLTreeNodes. As this whole method is just a heuristic, this loop is
			 * good enough. */
			while (row instanceof TLTreeNode) {
				row = ((TLTreeNode<?>) row).getBusinessObject();
			}
			if (!(row instanceof TLObject)) {
				continue;
			}
			TLObject tlObject = (TLObject) row;
			if (!WrapperHistoryUtils.isCurrent(tlObject)) {
				continue;
			}
			if (tlObject.tTransient()) {
				continue;
			}
			if (tlObject.tType() == null) {
				continue;
			}
			if (getTypes().stream().anyMatch(type -> TLModelUtil.isCompatibleInstance(type, tlObject))) {
				continue;
			}
			types.add(tlObject.tType());
		}
		if (!types.isEmpty()) {
			logErrorMissingTypeConfiguration(types);
		}
	}

	private void logErrorMissingTypeConfiguration(Set<TLType> types) {
		logError("The table " + getName() + " contains persistent objects whose types are not configured at the table."
			+ " Configured types: " + qualifiedNames(getTypes()) + " Non-configured types: " + qualifiedNames(types));
	}

	private void logError(String message) {
		Logger.error(message, TreeTableComponent.class);
	}

	boolean setSelectionPathToChannel(Collection<? extends TLTreeNode<?>> newSelection) {
		Object newSelectionPath;
		if (isInMultiSelectionMode()) {
			newSelectionPath =
				newSelection.stream().map(TreeTableComponent::toBusinessObjectPath).collect(Collectors.toSet());
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

	private Collection<? extends List<?>> selectionPathFromChannel() {
		Object selected = getSelectionPath();
		if (isInMultiSelectionMode()) {
			return unsafeCast(selected);
		} else {
			return CollectionUtil.singletonOrEmptySet((List<?>) selected);
		}
	}

	private Collection<?> selectionFromChannel() {
		Object selected = getSelected();
		if (isInMultiSelectionMode()) {
			return (Collection<?>) selected;
		} else {
			return CollectionUtil.singletonOrEmptySet(selected);
		}
	}

	/**
	 * Whether this {@link TreeTableComponent} is in multi selection mode.
	 * 
	 * @return <code>true</code> if {@link #getSelected()} returns a collection of element (either
	 *         empty or not), and <code>false</code> if {@link #getSelected()} returns the selected
	 *         object or <code>null</code> if nothing is selected.
	 */
	protected boolean isInMultiSelectionMode() {
		return getSelectionModel().isMultiSelectionSupported();
	}

	@Override
	protected Set<? extends TLStructuredType> getTypesToObserve() {
		Set<TLStructuredType> result = new HashSet<>();
		if (getTreeModelBuilder() != null) {
			result.addAll(getTreeModelBuilder().getTypesToObserve());
		}
		FilterUtil.filterInto(result, TLStructuredType.class, getTypes());
		return result;
	}

	@Override
	protected boolean receiveModelChangedEvent(Object aModel, Object changedBy) {
		boolean receiveModelChangedEvent = super.receiveModelChangedEvent(aModel, changedBy);
		if (isInvalid() || _treeTableData == null) {
			// Component tree is completely rebuild.
			return receiveModelChangedEvent;
		}

		TreeModelBuilder<Object> treeBuilder = getTreeModelBuilder();
		if (treeBuilder == null) {
			return receiveModelChangedEvent;
		}
		TreeUIModel<AbstractTreeTableNode<?>> tree = (TreeUIModel<AbstractTreeTableNode<?>>) getTreeModel();
		if (!(tree instanceof IndexedTLTreeModel<?>)) {
			// Update only for indexed models
			return receiveModelChangedEvent;
		}

		updateNodeObjects(treeBuilder.getNodesToUpdate(this, aModel));
		updateNodeObject(aModel);

		invalidateSelection();

		return receiveModelChangedEvent;
	}

	private void invalidateSelection() {
		_isSelectionValid = false;
	}

	@Override
	protected boolean receiveModelCreatedEvent(Object model, Object changedBy) {
		boolean result = super.receiveModelCreatedEvent(model, changedBy);

		if (isInvalid() || _treeTableData == null) {
			return result;
		}

		TreeModelBuilder<Object> treeBuilder = getTreeModelBuilder();
		if (treeBuilder != null) {
			updateNodeObjects(treeBuilder.getNodesToUpdate(this, model));
			updateNodeObject(model);

			invalidateSelection();
		}

		return result;
	}

	private void updateNodeObjects(Collection<? extends Object> nodeObjects) {
		for (Object nodeObject : nodeObjects) {
			updateNodeObject(nodeObject);
		}
	}

	private void updateNodeObject(Object nodeObject) {
		TreeModelBuilder<Object> treeModelBuilder = getTreeModelBuilder();
		if (treeModelBuilder != null && treeModelBuilder.supportsNode(this, nodeObject)) {
			Maybe<AbstractTreeTableNode<?>> node = findNodeOfBusinessObject(nodeObject);
			if (node.hasValue()) {
				AbstractTreeTableNode<?> tableNode = node.get();

				updateOldParents(tableNode);
				updateChildrenInternal(tableNode);

				tableNode.updateNodeProperties();
			}

			updateNewParents(nodeObject);
		}
	}

	private void updateNewParents(Object nodeObject) {
		updateChildren(getParentObjects(nodeObject));
	}

	private Collection<? extends Object> getParentObjects(Object nodeObject) {
		TreeModelBuilder<Object> treeModelBuilder = getTreeModelBuilder();
		if (treeModelBuilder != null) {
			return treeModelBuilder.getParents(this, nodeObject);
		} else {
			return Collections.emptySet();
		}
	}

	private void updateOldParents(AbstractTreeTableNode<?> node) {
		AbstractTreeTableNode<?> parent = node.getParent();

		if (parent != null) {
			updateChildrenInternal(parent);
		}
	}

	private void updateChildren(Collection<? extends Object> nodeObjects) {
		for (Object parent : nodeObjects) {
			updateChildren(parent);
		}
	}

	private void updateChildren(Object nodeObject) {
		updateChildrenInternal(findNodes(nodeObject));
	}

	private List<AbstractTreeTableNode<?>> findNodes(Object nodeObject) {
		TLTreeModel<AbstractTreeTableNode<?>> treeModel = getTreeModel();
		TreeModelBuilder<Object> modelBuilder = getTreeModelBuilder();
		if (treeModel instanceof IndexedTLTreeModel<?>) {
			return ((IndexedTLTreeModel) treeModel).getIndex().getNodes(nodeObject);
		} else if (modelBuilder != null) {
			AbstractTreeTableNode<?> node =
				TLTreeModelUtil.findNode(treeModel, createPath(modelBuilder, nodeObject), false);
			return Collections.singletonList(node);
		} else {
			Maybe<AbstractTreeTableNode<?>> node = findNodeOfBusinessObject(nodeObject);
			if (node.hasValue()) {
				return Collections.singletonList(node.get());
			} else {
				return Collections.emptyList();
			}
		}
	}

	private void updateChildrenInternal(Collection<AbstractTreeTableNode<?>> nodes) {
		for (AbstractTreeTableNode<?> node : nodes) {
			updateChildrenInternal(node);
		}
	}

	private void updateChildrenInternal(AbstractTreeTableNode<?> node) {
		if (node != null) {
			TreeModelBuilder<Object> treeModelBuilder = getTreeModelBuilder();
			if (treeModelBuilder != null) {
				unregisterSelectionListener();
				TLTreeModelUtil.updateChildren((AbstractTreeTableNode) node,
					treeModelBuilder.getChildIterator(this, node.getBusinessObject()));
				registerSelectionListener();
			}
		}
	}

	private void validateSelection() {
		Collection<? extends List<?>> newSelectedPaths = selectionPathFromChannel();

		if (newSelectedPaths.isEmpty()) {
			setDefaultTreeSelection();
		} else {
			Set<AbstractTreeTableNode<?>> newSelectedNodes = getNodesForPaths(newSelectedPaths);

			if (newSelectedNodes.isEmpty()) {
				Collection<AbstractTreeTableNode<?>> oldSelectedNodes = getSelectedNodes();

				if (oldSelectedNodes.isEmpty() || !isSelectionValid(oldSelectedNodes)) {
					setDefaultTreeSelection();
				}
			} else {
				setSelection(newSelectedNodes);
			}
		}
	}

	private void setDefaultTreeSelection() {
		AbstractTreeTableNode<?> defaultSelectedNode = getDefaultSelection();

		if (defaultSelectedNode != null) {
			setSelection(Collections.singleton(defaultSelectedNode));
		} else {
			setSelection(Collections.emptySet());
		}
	}

	private Set<AbstractTreeTableNode<?>> getNodesForPaths(Collection<? extends List<?>> paths) {
		Set<AbstractTreeTableNode<?>> selectableNodes = new HashSet<>();

		Map<Object, AbstractTreeTableNode<?>> nonMatchingPathNodes = Collections.emptyMap();
		for (List<?> path : paths) {
			if (path.isEmpty()) {
				continue;
			}
			Object bo = path.get(path.size() - 1);
			List<AbstractTreeTableNode<?>> nodes = findNodes(bo);
			if (nodes.isEmpty()) {
				continue;
			}
			AbstractTreeTableNode<?> nodeWithDifferentPath = null;
			boolean noNodeFound = true;
			for (AbstractTreeTableNode<?> node : nodes) {
				if (!isSelectable(node)) {
					continue;
				}
				if (TLTreeModelUtil.sameBusinessObjectPath(node, path)) {
					selectableNodes.add(node);
					noNodeFound = false;
				} else {
					nodeWithDifferentPath = node;
				}
			}
			// There are nodes for the business object, but all have different paths.
			if (noNodeFound && nodeWithDifferentPath != null) {
				if (nonMatchingPathNodes.isEmpty()) {
					nonMatchingPathNodes = new HashMap<>();
				}
				nonMatchingPathNodes.put(bo, nodeWithDifferentPath);
			}
		}
		if (!nonMatchingPathNodes.isEmpty()) {
			selectableNodes.stream().map(TLTreeNode::getBusinessObject).forEach(nonMatchingPathNodes.keySet()::remove);
			// For the actual selected business objects, the paths are all invalid. Select other
			// nodes which represent the same selection.
			selectableNodes.addAll(nonMatchingPathNodes.values());
		}

		return selectableNodes;
	}

	private boolean isSelectionValid(Collection<AbstractTreeTableNode<?>> selection) {
		for (AbstractTreeTableNode<?> selectedNode : selection) {
			if (!selectedNode.isAlive() || selectedNode.getModel() != getTreeModel()) {
				return false;
			}
			if (!isSelectable(selectedNode)) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		boolean invalid = isInvalid();

		if (!invalid && hasTreeTableData()) {
			if (!containsRootNode(models)) {
				TLTreeModel<?> tree = getTreeModel();

				Collection<AbstractTreeTableNode<?>> oldSelection = getSelectedNodes();
				Set<TreeUINode<?>> newSelection = computeNewSelection(oldSelection, models);
				boolean selectionChanged = !CollectionUtil.containsSame(oldSelection, newSelection);

				if (tree instanceof IndexedTLTreeModel<?>) {
					UserObjectIndex<?> index = ((IndexedTLTreeModel<?>) tree).getIndex();
					for (TLObject deleted : models) {
						index.deleteNodesWithUserObject(deleted);
					}
				}
				/* Update Selection *after* deletes nodes are removed from the model, to ensure that
				 * the selected TreeNode's does not have children with invalid business objects. */
				if (selectionChanged) {
					updateSelectionModel(newSelection);
				}
			} else {
				invalidate();
				invalid = true;
			}
		}

		return invalid || super.receiveModelDeletedEvent(models, changedBy);
	}

	private boolean containsRootNode(Set<TLObject> models) {
		TLTreeModel<AbstractTreeTableNode<?>> tree = getTreeModel();
		return models.contains(tree.getBusinessObject(tree.getRoot()));
	}

	private Set<TreeUINode<?>> computeNewSelection(Collection<AbstractTreeTableNode<?>> oldSelection,
			Set<TLObject> deletedObjects) {
		Set<TreeUINode<?>> newSelection = new HashSet<>();

		for (AbstractTreeTableNode<?> selectedNode : oldSelection) {
			AbstractTreeTableNode<?> node = selectedNode;

			AbstractTreeTableNode<?> deletedAncestor = findDeletedAncestor(node, deletedObjects);

			if (deletedAncestor != null) {
				AbstractTreeTableNode<?> possibleSelectedNode = findNextSelectableNode(deletedAncestor);

				if (isSelectable(possibleSelectedNode)) {
					newSelection.add(possibleSelectedNode);
				}
			} else {
				newSelection.add(selectedNode);
			}
		}
		return newSelection;
	}

	private boolean isSelectable(TreeUINode<?> node) {
		return node != null && _selectionModel.isSelectable(node) && (_rootVisible || node != getTreeModel().getRoot());
	}

	private AbstractTreeTableNode<?> findDeletedAncestor(AbstractTreeTableNode<?> root, Set<TLObject> deletedObjects) {
		while (root != null) {
			AbstractTreeTableNode<?> parent = root.getParent();

			if (deletedObjects.contains(root.getBusinessObject()) && parent != null) {
				return root;
			}

			root = parent;
		}

		return null;
	}

	private AbstractTreeTableNode<?> findNextSelectableNode(AbstractTreeTableNode<?> node) {
		AbstractTreeTableNode<?> parent = node.getParent();

		int childCount = parent.getChildCount();
		int index = parent.getIndex(node);

		for (int i = index + 1; i < childCount; i++) {
			AbstractTreeTableNode<?> sibling = parent.getChildAt(i);
			if (isValid(sibling)) {
				return sibling;
			}
		}

		for (int i = index - 1; i >= 0; i--) {
			AbstractTreeTableNode<?> sibling = parent.getChildAt(i);
			if (isValid(sibling)) {
				return sibling;
			}
		}

		while (parent != null) {
			if (isValid(parent)) {
				return parent;
			}

			parent = parent.getParent();
		}

		return null;
	}

	private static boolean isValid(TLTreeNode<?> node) {
		return ComponentUtil.isValid(node.getBusinessObject());
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		rebuildTableModel();
	}

	/** Convenience getter for the {@link TableViewModel}. */
	public final TableViewModel getTableViewModel() {
		return getTableData().getViewModel();
	}

	/** Convenience getter for the {@link TableModel}. */
	public final TableModel getTableModel() {
		return getTableViewModel().getApplicationModel();
	}

	/** Convenience method for finding the row index of a business object. */
	public final Maybe<Integer> findRowOfBusinessObject(Object businessObject) {
		Maybe<AbstractTreeTableNode<?>> nodeOfObject = findNodeOfBusinessObject(businessObject);
		Maybe<Integer> result;
		if (nodeOfObject.hasValue()) {
			result = findRowOfNode(nodeOfObject.get());
		} else {
			result = Maybe.none();
		}
		return result;
	}

	private Maybe<Integer> findRowOfNode(AbstractTreeTableNode<?> node) {
		int rowOfObject = getTableViewModel().getRowOfObject(node);
		if (rowOfObject == TableViewModel.NO_ROW) {
			return Maybe.none();
		}
		return Maybe.some(rowOfObject);
	}

	/** Convenience method for finding the node of a business object. */
	public final Maybe<AbstractTreeTableNode<?>> findNodeOfBusinessObject(Object businessObject) {
		List<?> rows = getTableViewModel().getDisplayedRows();
		for (Object untypedRow : rows) {
			AbstractTreeTableNode<?> nodeOfRow = castRowObject(untypedRow);
			if (Utils.equals(nodeOfRow.getBusinessObject(), businessObject)) {
				return Maybe.some(nodeOfRow);
			}
		}
		return Maybe.none();
	}

	/**
	 * <code>null</code>, if nothing is selected
	 */
	protected final Collection<AbstractTreeTableNode<?>> getSelectedNodes() {
		return unsafeCast(getTableData().getSelectionModel().getSelection());
	}

	/**
	 * Convenience method for casting the untyped row objects to their actual type.
	 * <p>
	 * As this class defines what type is used for row objects, it also provides this method for
	 * subclasses to cast the row objects to that type. Casting is necessary as the table classes
	 * always return them untyped.
	 * </p>
	 */
	protected final AbstractTreeTableNode<?> castRowObject(Object rowObject) {
		return (AbstractTreeTableNode<?>) rowObject;
	}

	/** Create a new {@link TableModel} and replace the old one. */
	public void rebuildTableModel() {
		if (hasTreeTableData()) {
			AbstractTreeTableModel<?> treeModel = createTreeModel();
			configureTreeModel(treeModel);
			getTableData().setTree(treeModel);
			adjustTreeTableData(getTableData());
			invalidateSelection();
			if (shouldCheckMissingTypeConfiguration()) {
				checkMissingTypeConfiguration();
			}
		}
	}

	@Override
	public void invalidate() {
		clearTreeTableField();

		super.invalidate();
	}

	private void clearTreeTableField() {
		if (hasTreeTableData()) {
			_selectionModel.clear();
			removeToolbarButtons(_treeTableData);
			_treeTableData = null;

			if (_control != null) {
				_control.detach();
				_control = null;
			}
		}
	}

	/**
	 * Removes default table buttons for the toolbar of the given table.
	 */
	protected void removeToolbarButtons(TableData table) {
		ToolBar toolBar = table.getToolBar();
		if (toolBar != null) {
			TableUtil.removeTableButtons(toolBar, table);
		}
	}

	@Override
	public void writeBody(ServletContext context, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		getRenderingControl().write(DefaultDisplayContext.getDisplayContext(request), out);
	}

	@Override
	public final boolean isCompleteRenderer() {
		return false;
	}

	@Override
	public TableControl getRenderingControl() {
		TableControl result = _control;
		if (result == null) {
			result = new TableControl(getTableData(), getTableRenderer());
			_control = result;
		}
		return result;
	}

	/** {@link TableRenderer}, to use for display of tree table */
	protected ITableRenderer getTableRenderer() {
		return _treeTableData.getTableModel().getTableConfiguration().getTableRenderer();
	}

	/** The {@link ResourceView} is used for creating the {@link TableControl}. */
	protected ResourceView createResourceView() {
		return getResPrefix();
	}

	@Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);

		if (hasTreeTableData()) {
			_treeTableData.setToolBar(newValue);
			_treeTableData.getTableModel().getTableConfiguration().setShowTitle(false);
		}
	}

	/**
	 * Hook for sub classes for additional adjustments of table field after table model
	 * (re-)creation.
	 */
	@SuppressWarnings("unused")
	protected void adjustTreeTableData(TreeTableData treeTableData) {
		// Hook, do nothing by default
	}

	/**
	 * Hook to use a specific {@link TableConfiguration}.
	 */
	protected AbstractTreeTableModel<?> createTreeModel() {
		TableConfiguration tableConfig = createTableConfiguration();
		return createTreeModel(tableConfig, tableConfig.getDefaultColumns());
	}

	/**
	 * Creates a {@link TreeUIModel} for the given table configuration and columns.
	 * 
	 * @param tableConfig
	 *        Never null.
	 * @param columns
	 *        Null means: Use the column order and visibility from the {@link TableConfiguration}.
	 * @return Never null.
	 */
	protected <N extends AbstractTreeTableNode<N>> AbstractTreeTableModel<?> createTreeModel(
			TableConfiguration tableConfig, List<String> columns) {
		TreeBuilder<N> treeBuilder = getTreeBuilder();

		Object root = getModel();

		if (treeBuilder instanceof TreeBuilderAdaptor) {
			root = ((TreeBuilderAdaptor) treeBuilder).getTreeModelBuilder().getModel(root, this);
		}

		return new IndexedTreeTableModel<>(treeBuilder, root, columns, tableConfig);
	}

	/**
	 * Set root visibility and listeners on the given {@link TreeUIModel}.
	 * 
	 * @param treeModel
	 *        Never null.
	 */
	protected void configureTreeModel(AbstractTreeTableModel<?> treeModel) {
		AbstractTreeTableNode<?> root = treeModel.getRoot();
		root.set(TREE_OWNER, this);
		treeModel.setRootVisible(isRootVisible() && root.getBusinessObject() != null);
		treeModel.addTreeModelListener(this::onTreeModelEvent);
	}

	/**
	 * Determines the {@link TreeTableComponent} that created the given
	 * {@link AbstractTreeTableModel}.
	 * 
	 * @param treeModel
	 *        Tree model to get owner for.
	 * @return The owning {@link TreeTableComponent} of the given tree model. May be
	 *         <code>null</code> when the given model was not created by a
	 *         {@link TreeTableComponent}.
	 */
	@FrameworkInternal
	public static TreeTableComponent getOwner(AbstractTreeTableModel<?> treeModel) {
		AbstractTreeTableNode<?> root = treeModel.getRoot();
		return root.get(TREE_OWNER);
	}

	private void onTreeModelEvent(TreeModelEvent event) {
		if (event.getType() != TreeModelEvent.BEFORE_COLLAPSE || _treeTableData == null) {
			return;
		}
		if (_adjustSelectionWhenCollapsing) {
			AbstractTreeTableNode<?> node = castRowObject(event.getNode());
			Collection<AbstractTreeTableNode<?>> selectedNodes = getSelectedNodes();
			Set<TreeUINode<?>> newSelectedNodes = new HashSet<>();
			for (AbstractTreeTableNode<?> selectedNode : selectedNodes) {
				if (TLTreeModelUtil.isAncestor(getTreeModel(), node, selectedNode)) {
					newSelectedNodes.add(node);
				} else {
					newSelectedNodes.add(selectedNode);
				}
			}
			updateSelectionModel(newSelectedNodes);
		}
	}

	/**
	 * Whether the root node of the displayed tree is visible.
	 */
	public boolean isRootVisible() {
		return _rootVisible;
	}

	/**
	 * Sets value of {@link #isRootVisible()}.
	 */
	public void setRootVisible(boolean rootVisible) {
		if (rootVisible == _rootVisible) {
			// no op.
			return;
		}
		_rootVisible = rootVisible;
		invalidate();
	}

	private void registerSelectionListener() {
		if (hasTreeTableData()) {
			_treeTableData.getSelectionModel().addSelectionListener(_selectionListener);
		}
	}

	private void unregisterSelectionListener() {
		if (hasTreeTableData()) {
			_treeTableData.getSelectionModel().removeSelectionListener(_selectionListener);
		}
	}

	/**
	 * Creates the {@link TableConfiguration} with the help of {@link TableConfigurationProvider}s
	 * for the configured supported types and default columns among others.
	 */
	protected TableConfiguration createTableConfiguration() {
		TableConfiguration configuration = TableConfigurationFactory.build(createTableConfigurationProvider());
		if (getToolBar() != null) {
			configuration.setShowTitle(false);
		}
		configuration.setTree(true);
		return configuration;
	}

	private TableConfigurationProvider createTableConfigurationProvider() {
		Config config = (Config) getConfig();

		List<TableConfigurationProvider> providers = new ArrayList<>();

		providers.add(new SetTableResPrefix(createResourceView()));

		ComponentTableConfigProvider componentTableConfigProvider = config.getComponentTableConfigProvider();
		if (componentTableConfigProvider != null) {
			providers.add(componentTableConfigProvider.getTableConfigProvider(this, null));
		}

		Set<TLClass> tlClasses = getClasses();
		if (!tlClasses.isEmpty()) {
			providers.add(new GenericTableConfigurationProvider(tlClasses));
		}

		if (_tableConfigProvider != null) {
			providers.add(_tableConfigProvider);
		}

		providers.add(((Config) getConfig()).getAdditionalConfiguration());
		providers.add(GenericTableConfigurationProvider.showDefaultColumns());

		return TableConfigurationFactory.combine(providers);
	}

	/**
	 * The {@link TLType}s which are being displayed.
	 * <p>
	 * If this table displays non-TLObjects, the {@link Set} is empty.
	 * </p>
	 */
	protected Set<TLType> getTypes() {
		return _types;
	}

	/** The {@link #getTypes()} filtered for {@link TLClass}. */
	protected final Set<TLClass> getClasses() {
		if (getTypes().isEmpty()) {
			return emptySet();
		}
		Set<TLClass> result = new HashSet<>();
		for (TLType type : getTypes()) {
			if (type instanceof TLClass) {
				result.add((TLClass) type);
			}
		}
		return result;
	}

	/**
	 * The {@link TreeBuilder}.
	 * <p>
	 * The default implementation returns the configured one. <br/>
	 * <b>If none is configured, this method has to be overridden.</b>
	 * </p>
	 * 
	 * @return Never <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	protected <N extends AbstractMutableTLTreeNode<N>> TreeBuilder<N> getTreeBuilder() {
		if (_treeBuilder == null) {
			throw new NullPointerException();
		}

		return (TreeBuilder<N>) _treeBuilder;
	}

	/**
	 * Returns the inner {@link TreeModelBuilder} of the {@link TreeBuilder} if he exists, otherwise
	 * null.
	 */
	protected final TreeModelBuilder<Object> getTreeModelBuilder() {
		if (_treeBuilder instanceof TreeBuilderAdaptor) {
			return ((TreeBuilderAdaptor) _treeBuilder).getTreeModelBuilder();
		}

		return null;
	}

	private void updateSelectionModel(Set<TreeUINode<?>> nodes) {
		Set<TreeUINode<?>> newSelectedNodes = new HashSet<>();

		for (TreeUINode<?> node : nodes) {
			if (isSelectable(node)) {
				newSelectedNodes.add(node);
			}
		}

		if (newSelectedNodes.isEmpty()) {
			AbstractTreeTableNode<?> defaultSelection = getDefaultSelection();
			if (defaultSelection != null) {
				newSelectedNodes.add(defaultSelection);
			}
		}

		setSelection(newSelectedNodes);
	}

	private void setSelection(Set<? extends TreeUINode<?>> newSelectedNodes) {
		SelectionUtil.setTreeSelection(_selectionModel, newSelectedNodes);
	}

	private AbstractTreeTableNode<?> getDefaultSelection() {
		if (_hasDefaultSelection) {
			AbstractTreeTableModel<?> treeModel = getTableData().getTree();

			List<AbstractTreeTableNode<?>> displayedRows = treeModel.getTable().getDisplayedRows();

			if (!displayedRows.isEmpty()) {
				for (AbstractTreeTableNode<?> displayedRow : displayedRows) {
					if (isSelectable(displayedRow)) {
						return displayedRow;
					}
				}
			}
		}

		return null;
	}

	/* The cast is safe, as the tree model has this type parameter. The TableField just "forgets"
	 * that. */
	@SuppressWarnings("unchecked")
	private TLTreeModel<AbstractTreeTableNode<?>> getTreeModel() {
		return (TLTreeModel<AbstractTreeTableNode<?>>) getTableData().getTree();
	}

	private List<Object> createPath(TreeModelBuilder<Object> treeBuilder, Object node) {
		Set<Object> seen = new LinkedHashSet<>();
		while (true) {
			Collection<? extends Object> parents = treeBuilder.getParents(this, node);
			if (parents.isEmpty()) {
				break;
			}
			if (seen.contains(node)) {
				// Safety: Cycle detected. Stop before crashing the app.
				return Collections.emptyList();
			}
			seen.add(node);
			node = parents.iterator().next();
		}

		List<Object> result = new ArrayList<>(seen);
		// Root is found, but not included to the path.
		Collections.reverse(result);
		return result;
	}

	/**
	 * Cause the {@link TableModel} to {@link TableModel#updateRows(int, int) update the rows} of
	 * the given business object and all of its (recursive) children.
	 */
	protected boolean updateTableModel(Object changedBusinessObject) {
		Maybe<Integer> searchResult = findRowOfBusinessObject(changedBusinessObject);
		if (!searchResult.hasValue()) {
			return false;
		}
		int changedRow = searchResult.get();
		AbstractTreeTableNode<?> changedNode = castRowObject(getTableViewModel().getRowObject(changedRow));
		int affectedRows = changedNode.getVisibleSubtreeSize();
		getTableModel().updateRows(changedRow, changedRow + affectedRows - 1);
		return false;
	}

	@Override
	protected Map<String, ChannelSPI> programmaticChannels() {
		return CHANNELS;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		selectionChannel().addListener(TreeTableComponent::handleNewSelectionChannelValue);
		
		selectionPathChannel().addListener(TreeTableComponent::handleNewSelectionPathChannelValue);

		Object channelValue = selectionPathChannel().get();
		if (isInMultiSelectionMode()) {
			if (!((Collection<?>) channelValue).isEmpty()) {
				handleNewSelectionPathChannelValue(selectionPathChannel(), Collections.emptySet(), channelValue);
			} else {
				Object currentSelection = selectionChannel().get();
				if (!((Collection<?>) currentSelection).isEmpty()) {
					handleNewSelectionChannelValue(selectionChannel(), Collections.emptySet(), currentSelection);
				}
			}
		} else {
			if (channelValue != null) {
				handleNewSelectionPathChannelValue(selectionPathChannel(), null, channelValue);
			} else {
				Object currentSelection = selectionChannel().get();
				if (currentSelection != null) {
					handleNewSelectionChannelValue(selectionChannel(), null, currentSelection);
				}
			}
		}
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
		TreeTableComponent treeTable = (TreeTableComponent) sender.getComponent();

		Object selectionPath = treeTable.getSelectionPath();
		if (!treeTable.isInMultiSelectionMode()) {
			if (newValue == null) {
				if (selectionPath == null) {
					return;
				} else {
					treeTable.setSelectionPath(null);
				}
			} else {
				if (selectionPath == null) {
					treeTable.setSelectionPath(buildRandomPathForObject(treeTable, newValue));
				} else {
					List<?> path = unsafeCast(selectionPath);
					if (newValue.equals(getLast(path))) {
						return;
					} else {
						treeTable.setSelectionPath(buildRandomPathForObject(treeTable, newValue));
					}
				}
			}
		} else {
			Collection<?> newSetValue = (Collection<?>) newValue;
			Collection<List<?>> selectionPaths = unsafeCast(selectionPath);
			switch (newSetValue.size()) {
				case 0: {
					treeTable.setSelectionPath(Collections.emptySet());
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
							newSelectionPaths.add(buildRandomPathForObject(treeTable, newlySelected));
						}
						treeTable.setSelectionPath(newSelectionPaths);
					}
				}
			}
		}
	}

	private static List<Object> buildRandomPathForObject(TreeTableComponent treeTable, Object bo) {
		Set<Object> alreadySeen = new HashSet<>();
		List<Object> path = new ArrayList<>();
		pathStep:
		while (bo != null) {
			alreadySeen.add(bo);
			List<? extends TLTreeNode<?>> nodes = treeTable.findNodes(bo);
			if (!nodes.isEmpty()) {
				TLTreeNode<?> node = nodes.get(0);
				while (node != null) {
					path.add(node.getBusinessObject());
					node = node.getParent();
				}
				break;
			}
			path.add(bo);
			Collection<? extends Object> parentObjects = treeTable.getParentObjects(bo);
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
	 * Casts the given value to anything you want.
	 * 
	 * @return The given value.
	 */
	@SuppressWarnings("unchecked")
	static <T> T unsafeCast(Object value) {
		return (T) value;
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
		TreeTableComponent tree = (TreeTableComponent) sender.getComponent();

		Object selectionChannelValue;
		if (!tree.isInMultiSelectionMode()) {
			if (newValue == null) {
				selectionChannelValue = null;
			} else {
				List<?> selectedPath = (List<?>) newValue;
				selectionChannelValue = selectedPath.get(selectedPath.size() - 1);
				TreeModelBuilder<Object> treeModelBuilder = tree.getTreeModelBuilder();
				if (treeModelBuilder != null) {
					tree.setModel(retrieveModelFromPath(treeModelBuilder, selectedPath, tree));
				}
			}
		} else {
			Collection<?> newValueCollection = (Collection<?>) newValue;
			selectionChannelValue = newValueCollection.stream().map(path -> {
				List<?> selectedPath = (List<?>) path;
				return selectedPath.get(selectedPath.size() - 1);
			}).collect(Collectors.toSet());
			TreeModelBuilder<Object> treeModelBuilder = tree.getTreeModelBuilder();
			if (treeModelBuilder != null && !newValueCollection.isEmpty()) {
				tree.setModel(retrieveModelFromPath(treeModelBuilder, (List<?>) getFirst(newValueCollection), tree));
			}
		}

		tree.setSelected(selectionChannelValue);

		tree.invalidateSelection();
	}

	private static Object retrieveModelFromPath(TreeModelBuilder<Object> treeModelBuilder, List<?> path,
			TreeTableComponent tree) {
		Object actualSelected = getLast(path);
		if (!ComponentUtil.isValid(actualSelected)) {
			return tree.getModel();
		}
		return treeModelBuilder.retrieveModelFromNode(tree, actualSelected);
	}

	@Override
	public TreeTableData getTableData() {
		if (_treeTableData == null) {
			_treeTableData = createTableTableData(_selectionModel);
			invalidateSelection();
		}

		return _treeTableData;
	}

	private TreeTableData createTableTableData(SelectionModel selectionModel) {
		AbstractTreeTableModel<?> treeModel = createTreeModel();
		configureTreeModel(treeModel);

		TreeTableData treeTableData =
			DefaultTreeTableData.createTreeTableData(this, treeModel, ConfigKey.component(this));

		treeTableData.setSelectionModel(selectionModel);
		ToolBar toolbar = getToolBar();
		if (toolbar != null) {
			treeTableData.setToolBar(toolbar);
		}

		adjustTreeTableData(treeTableData);

		/* Side-effect programming: Fetch view model to trigger loading of personal configuration
		 * and sorting table. */
		TableViewModel viewModel = treeTableData.getViewModel();

		rowsChannel().set(new ArrayList<>(viewModel.getDisplayedRows()));
		updateRowsChannelOnTableUpdate(viewModel);

		treeTableData
			.addSelectionVetoListener(new InvalidSelectionVeto(invalidObject -> showErrorSelectedObjectDeleted()));
		treeTableData.addSelectionVetoListener(_selectionVetoListener);

		return treeTableData;
	}

	private void updateRowsChannelOnTableUpdate(TableViewModel viewModel) {
		viewModel.addTableModelListener(new TableModelListener() {

			@Override
			public void handleTableModelEvent(TableModelEvent event) {
				switch (event.getType()) {
					case TableModelEvent.INVALIDATE:
					case TableModelEvent.INSERT:
					case TableModelEvent.DELETE:
					case TableModelEvent.UPDATE:
						rowsChannel().set(getDisplayedRows());
						break;
				}
			}
		});
	}

	private void expandRoot(AbstractTreeTableModel<?> treeModel) {
		if (_expandRoot) {
			AbstractTreeTableNode<?> root = treeModel.getRoot();
			if (treeModel.isRootVisible()) {
				root.setExpanded(true);
			}
		}
	}

	@Override
	public void createSubComponents(InstantiationContext context) {
		super.createSubComponents(context);

		_selectionVetoListener = new ComponentSelectionVetoListener(MasterSlaveCheckProvider.INSTANCE, this);
	}

	@Override
	public SelectionModel getSelectionModel() {
		return _selectionModel;
	}

	@Override
	public List<?> getDisplayedRows() {
		return new ArrayList<>(_treeTableData.getViewModel().getDisplayedRows());
	}

}
