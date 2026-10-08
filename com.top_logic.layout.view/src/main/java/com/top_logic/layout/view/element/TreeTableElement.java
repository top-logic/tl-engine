/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.dnd.DeclaredDrop;
import com.top_logic.layout.view.model.ObservableTreeModel;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.table.DropTargetMode;
import com.top_logic.layout.view.table.TableDropConfig;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.impl.TreeRowSource;

/**
 * Declarative {@link UIElement} displaying a tree of objects as a table (the {@code <tree-table>}
 * tag): each object is a row, its children are the rows below it, indented and opened and closed
 * by the toggle in front of the first column.
 *
 * <p>
 * The tree is computed as a {@link TreeElement tree} computes it - from the object the
 * {@link TreeStructureConfig#getRoot() root} function yields, by the
 * {@link TreeStructureConfig#getChildren() children} function - and the rows are the objects the
 * children function returns; the root object itself is not displayed, its children are the
 * top-level rows. It follows the model as a tree does, see {@link ObservableTreeModel}: a deleted
 * object loses its row, an object that appeared in a child list gets one, and the rows the user
 * opened stay open.
 * </p>
 *
 * <p>
 * Everything else - the columns, the selection, the filter bar, the activation and the dragging of
 * rows - is that of every {@link AbstractTableElement}. The {@link Config#getDrops() drops} take a
 * table's targets, and an insertion among the rows refers to the row it is made under as well as to
 * the row it is made before, as in a tree.
 * </p>
 *
 * @implNote A drop is compiled with the {@link DropTargetMode#treeSignature()} of its target.
 */
@InApp
public class TreeTableElement extends AbstractTableElement<TreeTableElement.Config> {

	/**
	 * Configuration for {@link TreeTableElement}.
	 */
	@TagName("tree-table")
	public interface Config extends AbstractTableElement.Config, TreeStructureConfig {

		@Override
		@ClassDefault(TreeTableElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getDrops()}. */
		String DROPS = "drops";

		/**
		 * Types to observe for object creation events.
		 *
		 * <p>
		 * When configured, the tree table checks every child list it computed whenever objects of
		 * these types are created, so that a new object appears as a row. When empty (default),
		 * only updates and deletes of the displayed objects are observed.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * What this tree table accepts a drop of, and what it does with the dropped objects.
		 *
		 * <p>
		 * A drop is applied by the first declared entry that accepts it. A drop on the table as a
		 * whole and one onto a row are those of a table; an insertion among the rows is made under
		 * a row - or among the top-level rows - before one of its children, and publishes both. Empty
		 * (default) leaves the tree table accepting no drop.
		 * </p>
		 */
		@Name(DROPS)
		@DefaultContainer
		List<TableDropConfig> getDrops();
	}

	/** The compiled functions computing the tree. */
	private final TreeFunctions _functions;

	/** The declared {@link Config#getDrops() drops} with their actions, in declaration order. */
	private final List<DeclaredDrop> _drops;

	/**
	 * Creates a {@link TreeTableElement} from configuration.
	 */
	@CalledByReflection
	public TreeTableElement(InstantiationContext context, Config config) {
		super(context, config);
		_functions = new TreeFunctions(config);
		_drops = compileDrops(context, config.getDrops(), dropConfig -> dropConfig.getTarget().treeSignature());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputChannels = ChannelInputs.resolve(context, _config.getInputs());
		TreeBuilder<DefaultTreeUINode> builder = _functions.builder(inputChannels);
		DefaultTreeUINodeModel treeModel = _functions.treeModel(builder, inputChannels);
		NodeStructure structure = new NodeStructure(treeModel, _config.getCanExpandAll());

		ReadOnlyTable<TreeRowSource<DefaultTreeUINode, Object>> table = createTable(context,
			resolveRowType(structure.topLevelObjects()), ChannelInputs.arguments(inputChannels),
			columns -> new TreeRowSource<>(structure, columns), true, GroupSpec.NONE, _drops);
		TableViewControl<Object> control = table.control();
		TreeRowSource<DefaultTreeUINode, Object> source = table.source();

		// The rows follow the tree, which follows the model and the input.
		Runnable refresh = () -> {
			source.structureChanged();
			control.refreshData();
			if (table.selectionBinding() != null) {
				table.selectionBinding().rowsRefreshed();
			}
		};
		ObservableTreeModel observableModel = new ObservableTreeModel(new ObservableTreeModel.Display() {
			@Override
			public void invalidateNode(DefaultTreeUINode node) {
				// Every invalidation is followed by nodesChanged(), which renders all rows anew.
			}

			@Override
			public void nodesChanged() {
				refresh.run();
			}

			@Override
			public void treeReplaced(DefaultTreeUINodeModel newModel) {
				structure.setModel(newModel);
				refreshPresets(table, ChannelInputs.arguments(inputChannels));
				refresh.run();
			}
		}, treeModel, _functions::root, builder, _functions.parentFunction(inputChannels),
			ObservedTypes.resolve(_config.getObservedTypes()), inputChannels);

		// Opening a row computes the children of its node, whose objects are followed from then on.
		source.addListener((from, to) -> observableModel.nodesComputed());

		// Observe the model only while the table is displayed.
		control.addAttachListener(() -> observableModel.attach(context.getModelScope()));
		control.addDetachListener(observableModel::detach);

		return control;
	}

	/**
	 * The tree a {@link DefaultTreeUINodeModel} holds, as the rows of a table see it: the children
	 * of the root node are the top-level rows, and a row is keyed by its business object, so that
	 * its expansion and its selection survive a rebuild of the tree.
	 */
	private static final class NodeStructure implements TreeStructure<DefaultTreeUINode, Object> {

		private DefaultTreeUINodeModel _model;

		private final boolean _finite;

		NodeStructure(DefaultTreeUINodeModel model, boolean finite) {
			_model = model;
			_finite = finite;
		}

		/**
		 * Makes the structure the one of the given model, which replaces the one held so far.
		 */
		void setModel(DefaultTreeUINodeModel model) {
			_model = model;
		}

		/**
		 * The business objects of the top-level rows.
		 */
		List<Object> topLevelObjects() {
			List<DefaultTreeUINode> roots = roots();
			List<Object> result = new ArrayList<>(roots.size());
			for (DefaultTreeUINode root : roots) {
				result.add(root.getBusinessObject());
			}
			return result;
		}

		@Override
		public List<DefaultTreeUINode> roots() {
			return _model.getRoot().getChildren();
		}

		@Override
		public List<DefaultTreeUINode> children(DefaultTreeUINode node) {
			return node.getChildren();
		}

		@Override
		public boolean isLeaf(DefaultTreeUINode node) {
			return node.getChildren().isEmpty();
		}

		@Override
		public boolean isFinite() {
			return _finite;
		}

		@Override
		public Object businessObject(DefaultTreeUINode node) {
			return node.getBusinessObject();
		}

		@Override
		public Object key(DefaultTreeUINode node) {
			return node.getBusinessObject();
		}

		@Override
		public Object rootParent() {
			return _model.getRoot().getBusinessObject();
		}

	}

}
