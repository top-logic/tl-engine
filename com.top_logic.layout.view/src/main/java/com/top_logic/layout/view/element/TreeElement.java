/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.MandatoryIfSet;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.controlprovider.MetaResourceControlProvider;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.dnd.DropArguments;
import com.top_logic.layout.view.dnd.DeclaredDrop;
import com.top_logic.layout.view.dnd.DragSourceBinding;
import com.top_logic.layout.view.model.ObservableTreeModel;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.TreeSelectionBinding;
import com.top_logic.layout.view.model.TreeSelectionDisplay;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.SelectionMode;

/**
 * Declarative {@link UIElement} that wraps a {@link ReactTreeControl}.
 *
 * <p>
 * The object the tree is built from and the children of a node are computed by TL-Script functions
 * over the values of the {@link ViewChannel}s the tree reads. A node's children are computed when
 * somebody opens it, so a tree of any extent costs no more than what is displayed of it, and a node
 * whose child list is empty is a leaf.
 * </p>
 *
 * <p>
 * The tree follows the model it displays: a deleted object loses its node, an object that appeared
 * in a child list gets one, and the subtrees the user opened stay open through it. An input naming
 * another object to build the tree from builds it anew, opening the subtrees that were open again
 * wherever the new tree holds their objects. See {@link ObservableTreeModel}.
 * </p>
 *
 * <p>
 * The selection channel is read as well as written, see {@link TreeSelectionBinding}: the tree
 * reveals and selects the node of an object another writer puts on it - the object a create command
 * just made, for instance - and writes what the user selects back.
 * </p>
 *
 * <p>
 * Nodes are dragged as a {@link Config#getDrag() drag} declares and dropped as the
 * {@link Config#getDrops() drops} declare: onto a node, on the tree as a whole, or inserted at a
 * place among the nodes - under a parent object, before one of its children.
 * </p>
 */
@InApp
public class TreeElement implements UIElement {

	/**
	 * Configuration for {@link TreeElement}.
	 */
	@TagName("tree")
	@TypeIcon("css:bi bi-diagram-3")
	public interface Config extends UIElement.Config, Inputs, TreeStructureConfig, ActionScopeDefinition {

	/** Access to the default methods of this interface for the configuration proxy. */
	Lookup LOOKUP = MethodHandles.lookup();

		/** Configuration name for {@link #getOnActivateAs()}. */
		String ACTIVATE_AS = "activate-as";

		/** Name of the value the business object of the activated node when no other name is given. */
		String DEFAULT_ACTIVATE_AS = "node";


		@Override
		@ClassDefault(TreeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getSelectionMode()}. */
		String SELECTION_MODE = "selection-mode";

		/** Configuration name for {@link #getNodeContent()}. */
		String NODE_CONTENT = "nodeContent";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getOnActivate()}. */
		String ON_ACTIVATE = "on-activate";

		/** Configuration name for {@link #getDrag()}. */
		String DRAG = "drag";

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
		 * It is also where the tree places a created or changed object of the
		 * {@link #getObservedTypes() observed types} it has no node for, so it is required while
		 * {@link #getObservedTypes()} names a type.
		 * </p>
		 */
		@Override
		@Constraint(value = MandatoryIfSet.class, args = @Ref(OBSERVED_TYPES))
		Expr getParents();

		/**
		 * Types whose created or changed objects the tree places even while it has no node for
		 * them.
		 *
		 * <p>
		 * Such an object is placed through {@link #getParents()}: the node of what holds it now
		 * gets its child list computed again, so the object appears there - if that node is open.
		 * Where it is not, the object appears when somebody opens it. Requires
		 * {@link #getParents()}.
		 * </p>
		 *
		 * <p>
		 * When empty (default), the tree follows only the objects it displays: their changes and
		 * deletions.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * Optional reference to the {@link ViewChannel} holding the selection.
		 *
		 * <p>
		 * The business object of the selected node is written to it, and an object another writer
		 * puts on it is revealed and selected in the tree - an object the tree has no node for
		 * leaves both the channel and the selection alone.
		 * </p>
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Whether the user may select one node at a time, or any number of them.
		 *
		 * <p>
		 * {@link SelectionMode#SINGLE} (the default) replaces the selection with every click, and
		 * the arrow keys move the selection from node to node.
		 * </p>
		 *
		 * <p>
		 * {@link SelectionMode#MULTI} keeps a plain click replacing the selection, but a click with
		 * {@code Ctrl} adds a node to it or takes it out again, and a click with {@code Shift}
		 * selects the range from the node the selection started at. The arrow keys then move the
		 * keyboard cursor alone, leaving the selection where it is; {@code Space} adds the node the
		 * cursor is on to the selection or takes it out again, and an arrow with {@code Shift} grows
		 * the range from the node the selection started at.
		 * </p>
		 *
		 * <p>
		 * The {@link #getSelection() selection channel} holds the business object of the selected
		 * node while exactly one node is selected, the set of those objects while there are several,
		 * and nothing while there is none - so a display bound to the channel works with either
		 * mode, and only one that is to show several objects at once has to expect a set.
		 * </p>
		 */
		@Name(SELECTION_MODE)
		@ComplexDefault(SelectionMode.SingleDefault.class)
		SelectionMode getSelectionMode();

		/**
		 * The command a node activation runs - a double-click on the node, or {@code Enter} while
		 * the node carries the keyboard focus.
		 *
		 * <p>
		 * The activated node becomes the tree's selection first, then the command runs with that
		 * node's business object as its input. The command's own executability rules decide over
		 * that object, so a node the rules reject activates nothing. Without a command, activating a
		 * node only selects it.
		 * </p>
		 *
		 * <p>
		 * Configured as {@code <on-activate class="..." .../>} inside the {@code <tree>} element.
		 * </p>
		 */
		@Name(ON_ACTIVATE)
		@Nullable
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		PolymorphicConfiguration<? extends ViewCommand> getOnActivate();

		/**
		 * The name of the argument holding the business object of the activated node, the main argument of the
		 * {@link #getOnActivate() command}.
		 *
		 * <p>
		 * The command sees it in addition to its own inputs; its scripts refer to it by this name.
		 * </p>
		 */
		@Name(ACTIVATE_AS)
		@StringDefault(DEFAULT_ACTIVATE_AS)
		String getOnActivateAs();

		/**
		 * The {@link #getOnActivate() command} runs with the business object of the activated node
		 * as argument named by {@link #getOnActivateAs()}, a {@link #getDrops() drop} with the
		 * {@link DropArguments arguments} of its signature.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			if (part != null && part == getOnActivate()) {
				return ActionScope.single(getOnActivateAs());
			}
			ActionScope drop = DropArguments.scopeOf(part, getDrops(), dropConfig -> dropConfig.getTarget().signature());
			if (drop != null) {
				return drop;
			}
			return outer;
		}

		/**
		 * How a node of the tree is displayed.
		 *
		 * <p>
		 * By default, a node shows the icon and the label of the object it stands for and a click
		 * on it selects that node, see {@link NodeDisplay}. A node content configured explicitly is
		 * the general display of an object, which leads to the place the application shows that
		 * object at:
		 * </p>
		 *
		 * <pre>
		 * &lt;nodeContent class="com.top_logic.layout.react.controlprovider.MetaResourceControlProvider"/&gt;
		 * </pre>
		 */
		@Name(NODE_CONTENT)
		@ItemDefault(NodeDisplay.class)
		PolymorphicConfiguration<ReactControlProvider> getNodeContent();

		/**
		 * Makes the nodes of this tree draggable, so they can be dropped on a display that accepts
		 * the drag's kind.
		 *
		 * <p>
		 * What is dragged are the objects of the nodes. Dragging a selected node drags the whole
		 * selection, an unselected node drags itself. Unset (default) leaves the nodes undraggable.
		 * </p>
		 */
		@Name(DRAG)
		TreeDragConfig getDrag();

		/**
		 * What this tree accepts a drop of, and what it does with the dropped objects.
		 *
		 * <p>
		 * A drop is applied by the first declared entry that accepts it, so a tree can accept
		 * several kinds of object - and insert one of them among its nodes while dropping another
		 * onto a node. Empty (default) leaves the tree accepting no drop.
		 * </p>
		 */
		@Name(DROPS)
		@DefaultContainer
		@TreeProperty
		List<TreeDropConfig> getDrops();
	}

	/**
	 * The display a node of a tree gets unless the tree configures another one.
	 *
	 * <p>
	 * A node shows the icon and the label of the object it stands for.
	 * </p>
	 */
	public interface NodeDisplay extends MetaResourceControlProvider.Config {

		/**
		 * Whether the node leads to the place the application shows its object at.
		 *
		 * <p>
		 * A node is the object's own place in the view, and a click on it selects the object. A
		 * link leaving the tree on that click is in the way, so a node is plain unless the tree
		 * asks for the link. Opening the object a node stands for is what {@link Config#getOnActivate()}
		 * is for.
		 * </p>
		 */
		@Override
		@BooleanDefault(false)
		boolean getLink();
	}

	private final Config _config;

	/** The compiled functions computing the tree. */
	private final TreeFunctions _functions;

	private final ReactControlProvider _nodeContentProvider;

	/** The instantiated {@link Config#getOnActivate()} command, {@code null} without one. */
	private final ViewCommand _onActivate;

	/** The configuration {@link #_onActivate} was instantiated from, {@code null} without one. */
	private final ViewCommand.Config _onActivateConfig;

	/** The declared {@link Config#getDrops() drops} with their actions, in declaration order. */
	private final List<DeclaredDrop> _drops;

	/**
	 * Creates a new {@link TreeElement} from configuration.
	 *
	 * <p>
	 * Expressions are compiled once here and shared across all sessions, see
	 * {@link TreeFunctions}.
	 * </p>
	 */
	@CalledByReflection
	public TreeElement(InstantiationContext context, Config config) {
		_config = config;

		_functions = new TreeFunctions(config);

		_nodeContentProvider = context.getInstance(config.getNodeContent());

		PolymorphicConfiguration<? extends ViewCommand> onActivate = config.getOnActivate();
		_onActivateConfig = onActivate instanceof ViewCommand.Config activateConfig ? activateConfig : null;
		_onActivate = ViewCommands.instantiate(context, onActivate, config.getOnActivateAs());

		_drops = compileDrops(context, config.getDrops());
	}

	/**
	 * Instantiates the action chains of the declared drops, so that applying one only has to run
	 * them.
	 */
	private static List<DeclaredDrop> compileDrops(InstantiationContext context, List<TreeDropConfig> dropConfigs) {
		List<DeclaredDrop> result = new ArrayList<>(dropConfigs.size());
		for (TreeDropConfig dropConfig : dropConfigs) {
			result.add(DeclaredDrop.compile(context, dropConfig, dropConfig.getTarget().signature()));
		}
		return result;
	}

	/**
	 * Makes the nodes of the given control draggable as the {@link Config#getDrag() drag} declares,
	 * the {@link TreeDragConfig#getNodeExecutability() node rules} deciding per node object.
	 */
	private void installDragSource(ViewContext context, ReactTreeControl control) {
		TreeDragConfig drag = _config.getDrag();
		DragSourceBinding.Source source = new DragSourceBinding.Source() {
			@Override
			public boolean isDragEnabled() {
				return control.isDragEnabled();
			}

			@Override
			public void setDragSource(String dragKind, Predicate<Object> draggable) {
				control.setDragSource(dragKind, draggable);
			}

			@Override
			public void setDragEnabled(boolean enabled) {
				control.setDragEnabled(enabled);
			}

			@Override
			public void refreshDragSource() {
				control.refreshDragSource();
			}
		};
		DragSourceBinding.install(context, control, source, drag, drag.getKind(), TreeDragConfig.NODE_EXECUTABILITY);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve input channels.
		List<ViewChannel> inputChannels = ChannelInputs.resolve(context, _config.getInputs());

		// 2. Build the tree model from the root object the input names.
		TreeBuilder<DefaultTreeUINode> builder = _functions.builder(inputChannels);
		DefaultTreeUINodeModel treeModel = _functions.treeModel(builder, inputChannels);

		// 4. Create the selection model for the configured selection mode.
		SelectionMode selectionMode = _config.getSelectionMode();
		SelectionModel<Object> selectionModel = selectionMode == SelectionMode.MULTI
			? new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER)
			: new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);

		// 5. Create ReactTreeControl.
		ReactTreeControl treeControl = new ReactTreeControl(context, treeModel, selectionModel, _nodeContentProvider);
		treeControl.setSelectionMode(selectionMode);
		treeControl.setCssClass(_config.getCssClass());

		// 6. Create ObservableTreeModel to forward model changes to the tree control. The function
		//    saying what holds an object serves the observation (where an object that moved went,
		//    where an object of an observed type belongs) and the selection (which node an object of
		//    the channel has).
		Function<Object, Object> parentFunction = _functions.parentFunction(inputChannels);
		Set<TLStructuredType> observedTypes = ObservedTypes.resolve(_config.getObservedTypes());
		ObservableTreeModel observableModel = new ObservableTreeModel(
			ObservableTreeModel.Display.of(treeControl),
			treeModel,
			_functions::root,
			builder,
			parentFunction,
			observedTypes,
			inputChannels
		);

		// 7. Wire the selection channel, which the tree reads as well as writes. The node of an
		//    object read from it is looked for in the tree displayed now, which is another one after
		//    a rebuild, and every change of the tree makes the binding express itself on it again.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			TreeSelectionBinding selectionBinding = new TreeSelectionBinding(
				TreeSelectionDisplay.of(treeControl, selectionModel), observableModel::getTreeModel,
				TreeFunctions.nodeLocator(parentFunction), selectionChannel);
			observableModel.addStructureListener(selectionBinding::structureChanged);

			// The channel and the selection outlive the control, so the binding is dropped with it.
			// It survives an attach/detach cycle, which only suspends the observation of the model:
			// a selection written while the tree is off screen is displayed when it returns.
			treeControl.addCleanupAction(selectionBinding::dispose);
		}

		// 8. Wire the activation command, which runs with the activated node's business object.
		if (_onActivate != null && _onActivateConfig != null) {
			ViewCommandModel activation =
				ViewCommandModel.forCommand(context, _onActivate, _onActivateConfig, _config.getOnActivateAs());
			treeControl.setActivationHandler(node -> activation.execute(context, TreeFunctions.businessObject(node)));
		}

		// 9. Wire dragging and dropping of nodes.
		if (_config.getDrag() != null) {
			installDragSource(context, treeControl);
		}
		if (!_drops.isEmpty()) {
			treeControl.setDropTarget(DeclaredDrop.bind(context, treeControl, treeControl::refreshDropTarget, _drops));
		}

		// 10. Observe the model only while the tree is displayed.
		treeControl.addAttachListener(() -> {
			observableModel.attach(context.getModelScope());
		});
		treeControl.addDetachListener(observableModel::detach);

		return treeControl;
	}

}
