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
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.control.TreeSelectionPartControl;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwner.MultipleAnnotatedModels;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.provider.ColumnProviderConfig;
import com.top_logic.layout.table.tree.TreeTableDataOwner;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.mig.html.GenericSelectionModelOwner;
import com.top_logic.mig.html.LazyTreeSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.TreeSelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TableConfigurationProvider} adding a column that displays the selection of a tree
 * selection model.
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
		 * The {@link ModelSpec} to get the value for the selection model for.
		 * 
		 * <p>
		 * It is expected that the channel contains as value a set of paths of objects, started from
		 * the root node to the actually selected node.
		 * </p>
		 */
		ModelSpec getChannel();

		/** @see #getChannel() */
		void setChannel(ModelSpec value);

	}

	private final TypedAnnotatable.Property<ComponentChannel.ChannelListener> _existingListener =
		TypedAnnotatable.property(ComponentChannel.ChannelListener.class, "listener");

	private LayoutComponent _component;

	private ComponentChannel _channel;

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
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, component -> {
			_component = component;
			BufferingProtocol log = new BufferingProtocol();
			_channel = ChannelLinking.resolveChannel(log, _component, getConfig().getChannel());
			if (log.hasErrors()) {
				log.getErrors().forEach(context::error);
			}
		});
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		ColumnConfiguration treeSelectColumn = table.declareColumn(getConfig().getColumnId());
		treeSelectColumn.setColumnLabelKey(getConfig().getColumnLabel());

		TreeTableDataOwner treeOwner = (TreeTableDataOwner) _component;
		Supplier<TLTreeModel<?>> treeSupplier = () -> {
			/* Table data may change during lifetime of selection model , e.g. when the owner is a
			 * GridComponent. */
			return treeOwner.getTableData().getTree();
		};
		MultipleAnnotatedModels<SelectionModel> algorithm =
			MultipleAnnotatedModels.newInstanceFor(getConfig().getColumnId());
		SelectionModelOwner owner = new GenericSelectionModelOwner(_component, algorithm);
		TreeSelectionModel selectionModel = new LazyTreeSelectionModel(owner,
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

	private <N> void listenToChannel(TreeSelectionModel<N> selectionModel,
			Supplier<? extends TLTreeModel<N>> treeSupplier) {
		ChannelListener existingListener = _component.get(_existingListener);
		if (existingListener != null) {
			_channel.removeListener(existingListener);
			_component.reset(_existingListener);
		}
		ChannelListener cl = new ChannelListener() {

			@Override
			public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
				selectionModel.setSelection(createModelValueFromChannelValue(treeSupplier, newValue));
			}
		};
		_channel.addListener(cl);
		_component.set(_existingListener, cl);
	}

	private <N> void connectWithChannel(TreeSelectionModel<N> selectionModel,
			Supplier<? extends TLTreeModel<N>> treeSupplier) {
		selectionModel.setSelection(createModelValueFromChannelValue(treeSupplier, _channel.get()));
		selectionModel.addSelectionListener(new SelectionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects,
					Set<?> selectedObjects) {
				TLTreeModel<N> treeModel = treeSupplier.get();
				Set<Object> newChannelValue = selectedObjects.stream()
					.map(selected -> createBOPathFromRoot(treeModel, (N) selected))
					.collect(Collectors.toSet());
				_channel.set(newChannelValue);
			}

			List<Object> createBOPathFromRoot(TLTreeModel<N> model, N node) {
				List<N> pathToRoot = model.createPathToRoot(node);
				List<Object> bos = new ArrayList<>();
				for (int i = pathToRoot.size() - 1; i >= 0; i--) {
					bos.add(model.getBusinessObject(pathToRoot.get(i)));
				}
				return bos;
			}
		});
	}

	private <N> void adaptColumnConfig(ColumnConfiguration column, TreeSelectionModel<N> selectionModel,
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

	private <N> Set<Object> createModelValueFromChannelValue(Supplier<? extends TLTreeModel<N>> treeSupplier,
			Object channelValue) {
		TLTreeModel<N> treeModel = treeSupplier.get();
		Set<Object> newSelectionModelValue = ((Collection<?>) channelValue)
			.stream()
			.map(value -> {
				List bosWithRoot = (List) value;
				return TLTreeModelUtil.findNode(treeModel, bosWithRoot.subList(1, bosWithRoot.size()), false);
			})
			.collect(Collectors.toSet());
		return newSelectionModelValue;
	}

	private static class TreeSelectCellRenderer<N> extends AbstractCellRenderer {

		private final TreeSelectionModel<N> _selectionModel;

		private Runnable _modelInitializer;

		/**
		 * Creates a new {@link TreeSelectCellRenderer}.
		 */
		public TreeSelectCellRenderer(TreeSelectionModel<N> selectionModel, Runnable modelInitializer) {
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


}

