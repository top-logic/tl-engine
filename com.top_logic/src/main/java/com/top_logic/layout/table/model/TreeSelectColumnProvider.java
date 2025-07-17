/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.table.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.control.TreeSelectionPartControl;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwner.MultipleAnnotatedModels;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.provider.ColumnProviderConfig;
import com.top_logic.layout.table.tree.TreeTableData;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.mig.html.GenericSelectionModelOwner;
import com.top_logic.mig.html.LazyTreeSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.SubtreeSelectionModel;
import com.top_logic.mig.html.TriState;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ValidationListener;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.util.Utils;

/**
 * {@link TableConfigurationProvider} adding a column that displays the selection of a tree
 * selection model.
 * 
 * <p>
 * A tree selection model is an additional special selection model for components that display a
 * tree structure. If a node is selected in a tree selection model, all nodes in the subtree are
 * selected; if a node is de-selected, all nodes in the subtree are de-selected. A node that
 * contains both selected and de-selected sub nodes is displayed as undecided.
 * </p>
 * 
 * <p>
 * In contrast to a normal selection model, a tree selection model does not contain all selected
 * nodes but only the nodes whose complete subtree is selected, e.g. a tree selection model in which
 * all nodes are selected only contains the root node.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(classifiers = { "treegrid", "treetable" })
@Label("Tree select column")
public class TreeSelectColumnProvider extends AbstractConfiguredInstance<TreeSelectColumnProvider.Config>
		implements TableConfigurationProvider {

	/**
	 * Typed configuration interface definition for {@link TreeSelectColumnProvider}.
	 */
	@TagName("tree-select-column")
	public interface Config extends PolymorphicConfiguration<TreeSelectColumnProvider>, ColumnProviderConfig {

		/**
		 * This is the channel in which the selection is stored.
		 * 
		 * <p>
		 * The stored value is a set of paths leading from the root node of the tree to the selected
		 * node in the tree selection model.
		 * </p>
		 * 
		 * <p>
		 * <b>Warning:</b> As the tree selection model does not contain all the selected nodes
		 * directly, but rather paths to the root nodes of the fully selected subtrees, it is
		 * advisable to define a separate channel rather than using the normal selection channel.
		 * </p>
		 */
		ModelSpec getChannel();

		/** Setter for {@link #getChannel()} */
		void setChannel(ModelSpec value);

		/**
		 * When this option is activated, the paths to the unselected nodes are saved in the channel
		 * rather than the paths to the selected nodes.
		 * 
		 * <p>
		 * For example, if no nodes are selected in the tree, the path containing the root node is
		 * saved in the channel. Conversely, if all nodes in the tree are selected, the empty set is
		 * saved.
		 * </p>
		 */
		boolean isInvertSelection();

	}

	private final TypedAnnotatable.Property<SelectionModelUpdater> _existingListener =
		TypedAnnotatable.property(SelectionModelUpdater.class, "listener");

	private LayoutComponent _component;

	private ComponentChannel _channel;

	private boolean _ignoreModelEvent;

	/**
	 * Create a {@link TreeSelectColumnProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TreeSelectColumnProvider(InstantiationContext context, Config config) {
		super(context, config);
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class,
			component -> _component = component);
	}

	private ComponentChannel channel() {
		if (_channel == null) {
			// The channel can not resolved lazy: This TreeSelectColumnProvider is
			// part of a *new* and when the component is created, it is not possible to resolve
			// channels from other components.
			BufferingProtocol log = new BufferingProtocol();
			_channel = ChannelLinking.resolveChannel(log, _component, getConfig().getChannel());
			log.checkErrors();
		}
		return _channel;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		ColumnConfiguration treeSelectColumn = table.declareColumn(getConfig().getColumnId());
		treeSelectColumn.setColumnLabelKey(getConfig().getColumnLabel());

		Supplier treeSupplier = () -> {
			/* Table data may change during lifetime of selection model, e.g. when the owner is a
			 * GridComponent. */
			return treeTableData().getTree();
		};
		MultipleAnnotatedModels<SelectionModel> algorithm =
			MultipleAnnotatedModels.newInstanceFor(getConfig().getColumnId());
		SelectionModelOwner owner = new GenericSelectionModelOwner(_component, algorithm);
		SubtreeSelectionModel selectionModel = new LazyTreeSelectionModel(owner,
			AbstractTreeTableModel.AbstractTreeTableNode.class, treeSupplier);
		algorithm.annotate(_component, selectionModel);

		listenToChannel(selectionModel, treeSupplier);

		/* During execution of the method "adaptConfigurationTo", the tree is created. Therefore it
		 * is not possible to initialize the selection model yet. Therefore the initialization is
		 * moved to the latest possible moment: when the first part control is rendered. */
		adaptColumnConfig(treeSelectColumn, selectionModel, () -> {
			connectWithChannel(selectionModel, treeSupplier);
		});
	}

	private TreeTableData treeTableData() {
		TableDataOwner treeTableDataOwner = (TableDataOwner) _component;
		return (TreeTableData) treeTableDataOwner.getTableData();
	}

	private <N> void listenToChannel(SubtreeSelectionModel<N> selectionModel,
			Supplier<? extends TLTreeModel<N>> treeSupplier) {
		@SuppressWarnings("unchecked")
		SelectionModelUpdater<N> listener = _component.get(_existingListener);
		if (listener == null) {
			listener = new SelectionModelUpdater<>();
			_component.set(_existingListener, listener);
			listener.attach(_component, channel());
		}

		listener.setModels(selectionModel, treeSupplier);
	}

	private <N> void connectWithChannel(SubtreeSelectionModel<N> selectionModel,
			Supplier<? extends TLTreeModel<N>> treeSupplier) {
		updateSelectionModelFromChannel(selectionModel, treeSupplier, channel().get());
		selectionModel.addSelectionListener(new SelectionListener() {
			@Override
			public void notifySelectionChanged(SelectionModel model, SelectionEvent event) {
				ComponentChannel channel = channel();
				updateChannelFromSelectionModel(channel, treeSupplier, selectionModel.getStates());
			}
		});
	}

	private <N> void adaptColumnConfig(ColumnConfiguration column, SubtreeSelectionModel<N> selectionModel,
			Runnable modelInitializer) {
		column.setAccessor(SimpleAccessor.INSTANCE);
		column.setFilterProvider(null);
		column.setFullTextProvider(null);
		column.setSelectable(false);
		column.setSortable(false);
		column.setDefaultColumnWidth("40px");
		column.setClassifiers(Collections.singletonList(ColumnConfig.CLASSIFIER_NO_EXPORT));
		column.setCellRenderer(new TreeSelectCellRenderer<>(selectionModel, modelInitializer));
	}

	private <N> void updateChannelFromSelectionModel(ComponentChannel channel,
			Supplier<? extends TLTreeModel<N>> treeSupplier, Map<N, TriState> states) {
		if (_ignoreModelEvent) {
			return;
		}
		TLTreeModel<N> treeModel = treeSupplier.get();
		TriState relevantState = !getConfig().isInvertSelection() ? TriState.SELECTED : TriState.NOT_SELECTED;
		Set<Object> newChannelValue;
		if (states.isEmpty() && relevantState == TriState.NOT_SELECTED) {
			// Special case nothing is selected in the selection model, i.e. the root node is
			// actually not selected: In this case the root node is not contained in the states with
			// state NOT_SELECTED. Therefore the default logic does not work.
			newChannelValue = Collections.singleton(createBOPathFromRoot(treeModel, treeModel.getRoot()));
		} else {
			newChannelValue = states.entrySet()
					.stream()
					.filter(e -> relevantState == e.getValue())
					.map(Map.Entry::getKey)
					.map(selected -> createBOPathFromRoot(treeModel, selected))
					.collect(Collectors.toSet());
		}
		channel.set(newChannelValue);
	}

	private <N> void updateSelectionModelFromChannel(SubtreeSelectionModel<N> selectionModel,
			Supplier<? extends TLTreeModel<N>> treeSupplier, Object channelValue) {
		TLTreeModel<N> treeModel = treeSupplier.get();
		Set<? extends N> newSelectionModelValue = ((Collection<?>) channelValue)
			.stream()
			.map(value -> findNode(treeModel, value))
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		assert _ignoreModelEvent == false;
		_ignoreModelEvent = true;
		try {
			if (!getConfig().isInvertSelection()) {
				selectionModel.setSelection(newSelectionModelValue);
			} else {
				selectionModel.setSelection(Collections.singleton(treeModel.getRoot()));
				selectionModel.removeFromSelection(newSelectionModelValue);
			}
		} finally {
			_ignoreModelEvent = false;
		}
	}

	private <N> N findNode(TLTreeModel<N> model, Object value) {
		Mapping<Object, ?> modelMapping = modelMapping();

		List<?> bosWithRoot = (List<?>) value;

		assert !bosWithRoot.isEmpty() : "Selection must not contain empty path";
		N node = model.getRoot();

		if (!Utils.equals(bosWithRoot.get(0), getBusinessObject(model, modelMapping, node))) {
			// Root has different user object.
			return null;
		}

		path:
		for (int i = 1; i < bosWithRoot.size(); i++) {
			Object userObject = bosWithRoot.get(i);
			for (N child : model.getChildren(node)) {
				if (Utils.equals(userObject, getBusinessObject(model, modelMapping, child))) {
					node = child;

					// Node was found, continue with next step in path.
					continue path;
				}
			}
			// No corresponding node found.
			return null;
		}
		return node;
	}

	private <N> List<Object> createBOPathFromRoot(TLTreeModel<N> model, N node) {
		Mapping<Object, ?> modelMapping = modelMapping();
		List<N> pathToRoot = model.createPathToRoot(node);
		List<Object> bos = new ArrayList<>();
		for (int i = pathToRoot.size() - 1; i >= 0; i--) {
			bos.add(getBusinessObject(model, modelMapping, pathToRoot.get(i)));
		}
		return bos;
	}

	private <N> Object getBusinessObject(TLTreeModel<N> model, Mapping<Object, ?> modelMapping, N node) {
		Object bo;
		if (modelMapping == null) {
			bo = model.getBusinessObject(node);
		} else {
			bo = modelMapping.apply(node);
		}
		return bo;
	}

	private Mapping<Object, ?> modelMapping() {
		TreeTableData tableData = treeTableData();
		return tableData.getTableModel().getTableConfiguration().getModelMapping();
	}

	private static class TreeSelectCellRenderer<N> extends AbstractCellRenderer {

		private final SubtreeSelectionModel<N> _selectionModel;

		private Runnable _modelInitializer;

		/**
		 * Creates a new {@link TreeSelectCellRenderer}.
		 */
		public TreeSelectCellRenderer(SubtreeSelectionModel<N> selectionModel, Runnable modelInitializer) {
			_selectionModel = selectionModel;
			_modelInitializer = modelInitializer;
		}

		@Override
		public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
			if (_modelInitializer != null) {
				_modelInitializer.run();
				_modelInitializer = null;
			}
			@SuppressWarnings("unchecked")
			N rowObject = (N) cell.getRowObject();
			new TreeSelectionPartControl<>(_selectionModel, rowObject).write(context, out);
		}

	}

	private class SelectionModelUpdater<N> implements ValidationListener, VisibilityListener, ChannelListener {

		private boolean _updateRequired;

		private SubtreeSelectionModel<N> _selectionModel;

		private Supplier<? extends TLTreeModel<N>> _treeSupplier;

		void setModels(SubtreeSelectionModel<N> selectionModel, Supplier<? extends TLTreeModel<N>> treeSupplier) {
			_selectionModel = selectionModel;
			_treeSupplier = treeSupplier;
			_updateRequired = false;
		}

		void attach(LayoutComponent component, ComponentChannel channel) {
			component.addValidationListener(this);
			component.addListener(LayoutComponent.VISIBILITY_EVENT, this);
			channel.addListener(this);
		}

		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			LayoutComponent component = sender.getComponent();
			if (component.isVisible() && component.isModelValid()) {
				updateSelectionModelFromChannel(_selectionModel, _treeSupplier, newValue);
			} else {
				_updateRequired = true;
			}
		}

		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
			LayoutComponent component = (LayoutComponent) sender;
			if (_updateRequired && component.isModelValid()) {
				updateSelectionModelFromChannel(_selectionModel, _treeSupplier, channel().get());
				_updateRequired = false;
			}
			return Bubble.BUBBLE;
		}

		@Override
		public void doValidateModel(DisplayContext context, LayoutComponent component) {
			if (_updateRequired && component.isVisible()) {
				updateSelectionModelFromChannel(_selectionModel, _treeSupplier, channel().get());
				_updateRequired = false;
			}
		}

	}

}
