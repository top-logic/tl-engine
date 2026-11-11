/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.function.Function;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import com.top_logic.layout.view.dnd.DropArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.MandatoryIfSet;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.dnd.DeclaredDrop;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.layout.view.model.ObservableTreeModel;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.TreeSelectionBinding;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.DropTargetMode;
import com.top_logic.layout.view.table.RowClassConfig;
import com.top_logic.layout.view.table.RowFunction;
import com.top_logic.layout.view.table.RowRoleConfig;
import com.top_logic.layout.view.table.TableDropConfig;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.impl.TreeRowSource;
import com.top_logic.util.Resources;

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
 * the row it is made before, as in a tree. As a table, it marks its rows with the
 * {@link Config#getRowClass() row class} and tints them in the {@link Config#getRowRole() row
 * role}.
 * </p>
 *
 * <p>
 * With {@link Config#getRowEdit() row-edit}, the tree table inside a form edits the values of its
 * rows, buffered until the form is saved: a row joins the edit when it is displayed, and the tree
 * shows the edited state while the form is edited, see {@link TreeRowArrangement}. With
 * {@link Config#getAddChild() add-child} it adds a child to the selected row, see
 * {@link ChildRowCreation}, and with {@link Config#getOnRemove() on-remove} it deletes rows.
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
	@TypeIcon("css:bi bi-list-nested")
	public interface Config extends AbstractTableElement.Config, TreeStructureConfig, RowClassConfig,
			RowRoleConfig {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@ClassDefault(TreeTableElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getDrops()}. */
		String DROPS = "drops";

		/** Configuration name for {@link #getAddChild()}. */
		String ADD_CHILD = "add-child";

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
		@TreeProperty
		List<TableDropConfig> getDrops();

		/**
		 * How a row is added while the tree table edits its {@link #getRowEdit() rows}: the type of
		 * a new child of the selected row, and how it is inserted.
		 *
		 * <p>
		 * When set, the add command of the form adds a child to the selected row, or a top-level
		 * row while no row is selected, see {@link AddChildConfig}. When unset (default), the tree
		 * table adds no rows. Requires {@link #getRowEdit() row-edit}.
		 * </p>
		 */
		@Name(ADD_CHILD)
		AddChildConfig getAddChild();

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

	/**
	 * How a {@link TreeTableElement} with {@link Config#getRowEdit() row-edit} adds a row: as a
	 * child of the selected row, or as a top-level row while no row is selected.
	 *
	 * <p>
	 * The parent of the new row is the object of the selected row - the buffer the form edits it
	 * through - or, while no row is selected, the object the tree's
	 * {@link TreeStructureConfig#getRoot() root} function computes. With several selected rows, no
	 * row is added.
	 * </p>
	 */
	public interface AddChildConfig extends ConfigPart {

		/** Configuration name for {@link #getCreateType()}. */
		String CREATE_TYPE = "create-type";

		/** Configuration name for {@link #getInsert()}. */
		String INSERT = "insert";

		/** Configuration name for {@link #getParentAs()}. */
		String PARENT_AS = "parent-as";

		/** Configuration name for {@link #getChildAs()}. */
		String CHILD_AS = "child-as";

		/** Name of the variable holding the parent when no other name is given. */
		String DEFAULT_PARENT_AS = "parent";

		/** Name of the variable holding the new row when no other name is given. */
		String DEFAULT_CHILD_AS = "child";

		/**
		 * TL-Script expression computing the type of a new child of the parent: a model class, or
		 * nothing.
		 *
		 * <p>
		 * A concrete class is created directly; an abstract one stands for its concrete
		 * specializations the user may create, which the add command offers in a menu. For nothing,
		 * the parent gets no child: the add command is not executable while it is selected.
		 * </p>
		 *
		 * <p>
		 * The expression sees the values of the inputs as variables named after their channels, or
		 * by their <code>as</code> names, and the parent as the variable
		 * {@value #DEFAULT_PARENT_AS}, or by the name given with {@value #PARENT_AS}. It reads the
		 * objects as they are edited in the form.
		 * </p>
		 */
		@Name(CREATE_TYPE)
		@Mandatory
		@Supplies(value = DEFAULT_PARENT_AS, renamedBy = PARENT_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getCreateType();

		/**
		 * TL-Script expression inserting the new row where the
		 * {@link TreeStructureConfig#getChildren() children} function finds it as a child of the
		 * parent, e.g. <code>$parent.add(`my:Folder#children`, $child)</code>.
		 *
		 * <p>
		 * The expression sees the values of the inputs, the parent as the variable
		 * {@value #DEFAULT_PARENT_AS} (or by the name given with {@value #PARENT_AS}) and the new row
		 * as the variable {@value #DEFAULT_CHILD_AS} (or by the name given with {@value #CHILD_AS}).
		 * Both are buffers of the form: what the expression writes into them is an edit like one
		 * of the user, stored when the form is saved and discarded when it is cancelled. A new row
		 * is stored as a part of the composition it is inserted into.
		 * </p>
		 */
		@Name(INSERT)
		@Mandatory
		@Supplies.List({
			@Supplies(value = DEFAULT_PARENT_AS, renamedBy = PARENT_AS),
			@Supplies(value = DEFAULT_CHILD_AS, renamedBy = CHILD_AS),
		})
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getInsert();

		/**
		 * The name of the variable holding the parent in {@link #getCreateType()} and
		 * {@link #getInsert()}.
		 *
		 * <p>
		 * Unset, the variable is named {@value #DEFAULT_PARENT_AS}. An input of the same name is a
		 * configuration error and is renamed with <code>as</code>.
		 * </p>
		 */
		@Name(PARENT_AS)
		@Nullable
		String getParentAs();

		/**
		 * The name of the variable holding the new row in {@link #getInsert()}.
		 *
		 * <p>
		 * Unset, the variable is named {@value #DEFAULT_CHILD_AS}.
		 * </p>
		 */
		@Name(CHILD_AS)
		@Nullable
		String getChildAs();

	}

	/** The compiled functions computing the tree. */
	private final TreeFunctions _functions;

	/** The declared {@link Config#getDrops() drops} with their actions, in declaration order. */
	private final List<DeclaredDrop> _drops;

	/** The compiled {@link Config#getRowClass() row class function}. */
	private final RowFunction<String> _rowClass;

	/** The compiled {@link Config#getRowRole() row role function}. */
	private final RowFunction<ValueColor> _rowRole;

	/** The compiled {@link AddChildConfig#getCreateType()}, {@code null} without {@link Config#getAddChild()}. */
	private final QueryExecutor _createType;

	/** The compiled {@link AddChildConfig#getInsert()}, {@code null} without {@link Config#getAddChild()}. */
	private final QueryExecutor _insert;

	/**
	 * Creates a {@link TreeTableElement} from configuration.
	 */
	@CalledByReflection
	public TreeTableElement(InstantiationContext context, Config config) {
		super(context, config);
		_functions = new TreeFunctions(config);
		_drops = compileDrops(context, config.getDrops(), dropConfig -> dropConfig.getTarget().treeSignature());
		_rowClass = RowFunction.rowClass(config);
		_rowRole = RowFunction.rowRole(config);
		checkRowEdit(context, _drops);
		AddChildConfig addChild = config.getAddChild();
		_createType = addChild == null ? null : InputScript.compile(addChild, AddChildConfig.CREATE_TYPE);
		_insert = addChild == null ? null : InputScript.compile(addChild, AddChildConfig.INSERT);
		checkRowChanges(context);
	}

	/**
	 * Reports adding and removing rows the tree table cannot offer: rows are added and removed only
	 * while they are edited, and removed by deleting them.
	 */
	private void checkRowChanges(InstantiationContext context) {
		if (_config.getAddChild() != null && _config.getRowEdit() == RowEditPolicy.NONE) {
			context.error("A <" + tagName() + "> adds rows (<" + Config.ADD_CHILD + ">) only with '"
				+ Config.ROW_EDIT + "'.");
		}
		RowSetBinding.RemoveMode onRemove = _config.getOnRemove();
		if (onRemove != null && onRemove != RowSetBinding.RemoveMode.NONE
			&& onRemove != RowSetBinding.RemoveMode.DELETE) {
			context.asI18NLog(Resources.getInstance()).error(
				I18NConstants.ERROR_TREE_TABLE_REMOVES_BY_DELETING__ATTRIBUTE_VALUE.fill(Config.ON_REMOVE,
					RowSetBinding.RemoveMode.DELETE.name()));
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputChannels = ChannelInputs.resolve(context, _config.getInputs());
		RowTree tree = new RowTree(_functions, inputChannels, _config.getCanExpandAll());
		if (_config.getRowEdit() != RowEditPolicy.NONE) {
			return createEditableControl(context, inputChannels, tree);
		}

		Function<Object, String> rowCssClass = _rowClass.bind(inputChannels);
		Function<Object, ValueColor> rowColor = _rowRole.bind(inputChannels);
		Object[] inputValues = ChannelInputs.arguments(inputChannels);
		RowTypes types = types(inputValues);
		ReadOnlyTable<TreeRowSource<DefaultTreeUINode, Object>, TreeSelectionBinding> table = createTable(context,
			types, resolveRowTypes(types, tree.topLevelObjects()), inputValues,
			tree::createSource, true, GroupSpec.NONE, _drops, rowCssClass, rowColor,
			tree::bindSelection);
		TableViewControl<Object> control = table.control();
		TreeRowSource<DefaultTreeUINode, Object> source = table.source();

		contributeExportCommand(context, control, table::view,
			() -> ColumnDeclarations.exportRenderers(table.setups()), inputChannels);

		// The rows follow the tree, which follows the model and the input.
		Runnable refresh = () -> {
			source.refresh();
			control.refreshData();
		};
		InputRoles roles = inputRoles(context, inputChannels);
		tree.observe(new ObservableTreeModel.Display() {
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
				refreshPresets(table, ChannelInputs.arguments(inputChannels));
				refresh.run();
			}

			@Override
			public void inputsChanged() {
				// The rows and their cells stay; the criteria of the presets, the row class and the row
				// role may read the inputs.
				if (refreshPresets(table, ChannelInputs.arguments(inputChannels))) {
					control.refreshFilters();
				}
				if (rowCssClass != null || rowColor != null) {
					control.repaintRows();
				}
			}
		}, ObservedTypes.resolve(_config.getObservedTypes()), roles.rowIndependent(), roles.columnOnly());

		// Observe the model only while the table is displayed.
		control.addAttachListener(() -> tree.attach(context.getModelScope()));
		control.addDetachListener(tree::detach);

		return control;
	}

	/**
	 * Creates the editable variant: the rows of the tree join the edit session of the enclosing form
	 * when they are displayed (see {@link TreeRowArrangement}), and their cells become editable
	 * according to {@link Config#getRowEdit()}.
	 *
	 * <p>
	 * Rows are added through {@link Config#getAddChild()} only (see {@link ChildRowCreation}), and
	 * removed as {@link Config#getOnRemove()} says.
	 * </p>
	 */
	private IReactControl createEditableControl(ViewContext context, List<ViewChannel> inputChannels,
			RowTree tree) {
		FormControl formControl = rowEditForm(context);

		RowTypes types = types(ChannelInputs.arguments(inputChannels));
		List<TLStructuredType> rowTypes = resolveRowTypes(types, tree.topLevelObjects());
		TLStructuredType rowType = rowTypes.isEmpty() ? null : rowTypes.get(0);
		// The session reads no rows: the arrangement lets it adopt the rows the tree displays. Rows
		// are created as children of other rows, never as the binding's create types.
		RowSetBinding.RemoveMode onRemove = _config.getOnRemove();
		RowSetBinding binding = new QueryRowSetBinding(List::of,
			rowType instanceof TLClass rowClass ? rowClass : null, List.of(),
			onRemove == null ? RowSetBinding.RemoveMode.NONE : onRemove);

		RowSetTableControl control = createRowEditControl(context, formControl, binding, types, rowTypes,
			inputChannels, _rowClass.bind(inputChannels));
		InputRoles roles = inputRoles(context, inputChannels);
		TreeRowArrangement arrangement = new TreeRowArrangement(tree,
			ObservedTypes.resolve(_config.getObservedTypes()), roles.rowIndependent(), roles.columnOnly());
		control.setRowArrangement(arrangement);
		boolean createsRows = _createType != null;
		if (createsRows) {
			control.setRowCreation(new ChildRowCreation(_createType, _insert, inputChannels, arrangement));
		}
		control.init();

		contributeRowCommands(context, formControl, binding, control, createsRows);
		contributeExportCommand(context, control, () -> control.getTableControl().getView(),
			control::getExportRenderers, inputChannels);

		return control;
	}

}
