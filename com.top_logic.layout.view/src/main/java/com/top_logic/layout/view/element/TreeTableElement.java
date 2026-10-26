/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import com.top_logic.layout.view.dnd.DropArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.MandatoryIfSet;
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
import com.top_logic.layout.view.model.TreeSelectionBinding;
import com.top_logic.layout.view.model.TreeSelectionDisplay;
import com.top_logic.layout.view.table.DropTargetMode;
import com.top_logic.layout.view.table.TableDropConfig;
import com.top_logic.model.search.expr.config.dom.Expr;
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
 * The selection is that of a tree, see {@link TreeSelectionBinding}: an object written to the
 * selection channel is revealed - the rows above it are opened and its row is scrolled into view -,
 * and when the selected object loses its row, the row it hung in becomes the selection.
 * </p>
 *
 * <p>
 * Everything else - the columns, the filter bar, the activation and the dragging of rows - is that
 * of every {@link AbstractTableElement}. The {@link Config#getDrops() drops} take a
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

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@ClassDefault(TreeTableElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getDrops()}. */
		String DROPS = "drops";

		/**
		 * Optional TL-Script expression computing what holds an object in the tree.
		 *
		 * <p>
		 * The expression sees the values of the inputs as variables named after their channels, or
		 * by their <code>as</code> names, and the object as the variable
		 * {@value NodeAsConfig#DEFAULT_NODE_AS}, or by the name given with
		 * {@value NodeAsConfig#NODE_AS}. It returns the object whose child list holds it - nothing
		 * for the object the tree is built from, and for an object belonging to no tree at all.
		 * </p>
		 *
		 * <p>
		 * It is how the node of an object written to the selection channel is found: the tree walks
		 * from the object up to the one it is built from and descends along that chain, computing only
		 * the child lists on the way. Without it the node is searched for, which computes the child list
		 * of every node passed on the way - affordable for a tree of small extent, not for a large or an
		 * unbounded one.
		 * </p>
		 *
		 * <p>
		 * It is also where the tree table places a created or changed object of the
		 * {@link #getObservedTypes() observed types} it has no row for, so it is required while
		 * {@link #getObservedTypes()} names a type.
		 * </p>
		 */
		@Override
		@Constraint(value = MandatoryIfSet.class, args = @Ref(OBSERVED_TYPES))
		Expr getParents();

		/**
		 * Types whose created or changed objects the tree table places even while it has no row for
		 * them.
		 *
		 * <p>
		 * Such an object is placed through {@link #getParents()}: the row of what holds it now gets
		 * its child list computed again, so the object appears there - if that row is expanded.
		 * Where it is not, the object appears when somebody expands it. Requires
		 * {@link #getParents()}.
		 * </p>
		 *
		 * <p>
		 * When empty (default), the tree table follows only the objects it displays: their changes
		 * and deletions.
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

		/**
		 * A {@link #getDrops() drop} runs its actions with the {@link DropArguments arguments} of
		 * its signature.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			ActionScope drop = DropArguments.scopeOf(part, getDrops(), config -> config.getTarget().treeSignature());
			if (drop != null) {
				return drop;
			}
			return AbstractTableElement.Config.super.actionScope(part, outer);
		}
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

		// The function saying what holds an object serves the observation (where an object that
		// moved went, where an object of an observed type belongs) and the selection (which row an
		// object of the channel has). The node of an object read from the channel is looked for in
		// the tree displayed now, which is another one after a rebuild.
		Function<Object, Object> parentFunction = _functions.parentFunction(inputChannels);
		SelectionBinder<TreeSelectionBinding> selectionBinder = (tableControl, channel) -> {
			TreeSelectionBinding binding = new TreeSelectionBinding(
				TreeSelectionDisplay.of(tableControl, structure::model), structure::model,
				TreeFunctions.nodeLocator(parentFunction), channel);
			// The channel and the selection outlive the control, so the binding is dropped with it.
			// It survives an attach/detach cycle, which only suspends the observation of the model:
			// a selection written while the table is off screen is displayed when it returns.
			tableControl.addCleanupAction(binding::dispose);
			return binding;
		};

		ReadOnlyTable<TreeRowSource<DefaultTreeUINode, Object>, TreeSelectionBinding> table = createTable(context,
			resolveRowType(structure.topLevelObjects()), ChannelInputs.arguments(inputChannels),
			columns -> new TreeRowSource<>(structure, columns), true, GroupSpec.NONE, _drops, null, selectionBinder);
		TableViewControl<Object> control = table.control();
		TreeRowSource<DefaultTreeUINode, Object> source = table.source();

		// The rows follow the tree, which follows the model and the input.
		Runnable refresh = () -> {
			source.refresh();
			control.refreshData();
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

			@Override
			public void inputsChanged() {
				// The rows and their cells stay; the criteria of the presets may read the inputs.
				refreshPresets(table, ChannelInputs.arguments(inputChannels));
			}
		}, treeModel, _functions::root, builder, parentFunction,
			ObservedTypes.resolve(_config.getObservedTypes()), inputChannels)
				.withRowIndependentInputs(inputRoles(context, inputChannels).rowIndependent());

		// Every change of the tree makes the selection express itself on the rows displayed now:
		// the rows are refreshed before, so a selected object that lost its row is gone from them.
		TreeSelectionBinding selectionBinding = table.selectionBinding();
		if (selectionBinding != null) {
			observableModel.addStructureListener(selectionBinding::structureChanged);
		}

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
		 * The model whose tree this structure is.
		 */
		DefaultTreeUINodeModel model() {
			return _model;
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
