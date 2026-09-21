/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.controlprovider.MetaResourceControlProvider;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
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
import com.top_logic.layout.view.model.NodeLocator;
import com.top_logic.layout.view.model.ObservableTreeModel;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.TreeSelectionBinding;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
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
 */
@InApp
public class TreeElement implements UIElement {

	/**
	 * Configuration for {@link TreeElement}.
	 */
	@TagName("tree")
	public interface Config extends UIElement.Config, Inputs {

		@Override
		@ClassDefault(TreeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getRoot()}. */
		String ROOT = "root";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/** Configuration name for {@link #getParents()}. */
		String PARENTS = "parents";

		/** Configuration name for {@link #getCanExpandAll()}. */
		String CAN_EXPAND_ALL = "canExpandAll";

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

		/**
		 * TL-Script function computing the root object of the tree.
		 *
		 * <p>
		 * Takes the input channel values as positional arguments and returns a single object to be
		 * used as the tree root.
		 * </p>
		 */
		@Name(ROOT)
		@Mandatory
		@NonNullable
		Expr getRoot();

		/**
		 * TL-Script function computing the children of a node.
		 *
		 * <p>
		 * Takes the input channel values followed by the parent business object as last argument.
		 * Returns a {@link Collection} of child business objects.
		 * </p>
		 */
		@Name(CHILDREN)
		@Mandatory
		@NonNullable
		Expr getChildren();

		/**
		 * Optional TL-Script function computing what holds an object in the tree.
		 *
		 * <p>
		 * Takes the input channel values followed by an object as last argument, and returns the
		 * object whose child list holds it - nothing for the object the tree is built from, and for
		 * an object belonging to no tree at all.
		 * </p>
		 *
		 * <p>
		 * It is how the node of an object written to the {@link #getSelection() selection channel}
		 * is found: the tree walks from the object up to the one it is built from and descends along
		 * that chain, computing only the child lists on the way. Without it the node is searched for,
		 * which computes the child list of every node passed on the way - affordable for a tree of
		 * small extent, not for a large or an unbounded one.
		 * </p>
		 */
		@Name(PARENTS)
		Expr getParents();

		/**
		 * Types to observe for object creation events.
		 *
		 * <p>
		 * When configured, the tree re-evaluates its root function when objects of these types
		 * are created. This enables automatic node insertion for trees that query all instances
		 * of a type.
		 * </p>
		 *
		 * <p>
		 * When empty (default), only updates and deletes of currently displayed nodes are
		 * observed. This is correct for trees whose nodes come directly from a channel.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * Whether the tree supports expand-all.
		 */
		@Name(CAN_EXPAND_ALL)
		@BooleanDefault(true)
		boolean getCanExpandAll();

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
		PolymorphicConfiguration<? extends ViewCommand> getOnActivate();

		/**
		 * Optional provider for custom node content controls. If not set, nodes are rendered
		 * using a simple text label.
		 */
		@Name(NODE_CONTENT)
		PolymorphicConfiguration<ReactControlProvider> getNodeContent();
	}

	private final Config _config;

	private final QueryExecutor _rootExecutor;

	private final QueryExecutor _childrenExecutor;

	/** The compiled {@link Config#getParents()} function, {@code null} without one. */
	private final QueryExecutor _parentsExecutor;

	private final ReactControlProvider _nodeContentProvider;

	/** The instantiated {@link Config#getOnActivate()} command, {@code null} without one. */
	private final ViewCommand _onActivate;

	/** The configuration {@link #_onActivate} was instantiated from, {@code null} without one. */
	private final ViewCommand.Config _onActivateConfig;

	/**
	 * Creates a new {@link TreeElement} from configuration.
	 *
	 * <p>
	 * Expressions are compiled once here and shared across all sessions. If services like
	 * {@code PersistencyLayer} are not yet active, {@link QueryExecutor#compile(Expr)} returns a
	 * {@code DeferredQueryExecutor} that lazily compiles on first execution.
	 * </p>
	 */
	@CalledByReflection
	public TreeElement(InstantiationContext context, Config config) {
		_config = config;

		_rootExecutor = QueryExecutor.compile(config.getRoot());
		_childrenExecutor = QueryExecutor.compile(config.getChildren());
		_parentsExecutor = QueryExecutor.compileOptional(config.getParents());

		ReactControlProvider configuredProvider = context.getInstance(config.getNodeContent());
		_nodeContentProvider = configuredProvider != null ? configuredProvider : MetaResourceControlProvider.INSTANCE;

		PolymorphicConfiguration<? extends ViewCommand> onActivate = config.getOnActivate();
		_onActivateConfig = onActivate instanceof ViewCommand.Config activateConfig ? activateConfig : null;
		_onActivate = context.getInstance(onActivate);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve input channels.
		List<ViewChannel> inputChannels = ChannelInputs.resolve(context, _config.getInputs());

		// 2. Execute initial root query.
		Object[] channelValues = ChannelInputs.arguments(inputChannels);
		Object rootObject = _rootExecutor.execute(channelValues);

		// 3. Build tree model with custom TreeBuilder.
		TreeBuilder<DefaultTreeUINode> builder = createTreeBuilder(inputChannels);
		DefaultTreeUINodeModel treeModel = new DefaultTreeUINodeModel(builder, rootObject);

		// 4. Create the selection model for the configured selection mode.
		SelectionMode selectionMode = _config.getSelectionMode();
		SelectionModel<Object> selectionModel = selectionMode == SelectionMode.MULTI
			? new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER)
			: new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);

		// 5. Create ReactTreeControl.
		ReactTreeControl treeControl = new ReactTreeControl(context, treeModel, selectionModel, _nodeContentProvider);
		treeControl.setSelectionMode(selectionMode);

		// 6. Create ObservableTreeModel to forward model changes to the tree control. The function
		//    saying what holds an object serves the observation (where an object that moved went)
		//    and the selection (which node an object of the channel has).
		Function<Object, Object> parentFunction = createParentFunction(inputChannels);
		Set<TLStructuredType> observedTypes = ObservedTypes.resolve(_config.getObservedTypes());
		QueryExecutor rootExec = _rootExecutor;
		ObservableTreeModel observableModel = new ObservableTreeModel(
			treeControl,
			treeModel,
			args -> rootExec.execute(args),
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
			TreeSelectionBinding selectionBinding = new TreeSelectionBinding(treeControl, selectionModel,
				observableModel::getTreeModel, nodeLocator(parentFunction), selectionChannel);
			observableModel.addStructureListener(selectionBinding::structureChanged);

			// The channel and the selection outlive the control, so the binding is dropped with it.
			// It survives an attach/detach cycle, which only suspends the observation of the model:
			// a selection written while the tree is off screen is displayed when it returns.
			treeControl.addCleanupAction(selectionBinding::dispose);
		}

		// 8. Wire the activation command, which runs with the activated node's business object.
		if (_onActivate != null && _onActivateConfig != null) {
			ViewCommandModel activation = ViewCommandModel.forCommand(context, _onActivate, _onActivateConfig);
			treeControl.setActivationHandler(node -> activation.execute(context, businessObject(node)));
		}

		// 9. Observe the model only while the tree is displayed.
		treeControl.addAttachListener(() -> {
			observableModel.attach(context.getModelScope());
		});
		treeControl.addDetachListener(observableModel::detach);

		return treeControl;
	}

	/**
	 * What holds a business object in the tree, {@code null} without a {@link Config#getParents()}
	 * function.
	 *
	 * @param inputChannels
	 *        The channels whose values the function is called with, followed by the object.
	 */
	private Function<Object, Object> createParentFunction(List<ViewChannel> inputChannels) {
		if (_parentsExecutor == null) {
			return null;
		}
		return businessObject -> singleObject(
			_parentsExecutor.execute(appendArg(ChannelInputs.arguments(inputChannels), businessObject)));
	}

	/**
	 * How the node of a business object is found in the tree.
	 *
	 * @param parentFunction
	 *        What holds an object in the tree, {@code null} where the tree does not say.
	 */
	private static NodeLocator nodeLocator(Function<Object, Object> parentFunction) {
		return parentFunction == null ? NodeLocator.SEARCHING : NodeLocator.byParents(parentFunction);
	}

	/**
	 * The object a function returning a single object yielded, taking the first element of a
	 * collection the script produced instead and {@code null} from an empty one.
	 */
	private static Object singleObject(Object result) {
		if (result instanceof Collection<?> collection) {
			return collection.isEmpty() ? null : collection.iterator().next();
		}
		return result;
	}

	private TreeBuilder<DefaultTreeUINode> createTreeBuilder(List<ViewChannel> inputChannels) {
		return new TreeBuilder<>() {

			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				Object[] channelValues = ChannelInputs.arguments(inputChannels);
				Object[] args = appendArg(channelValues, node.getBusinessObject());
				Object result = _childrenExecutor.execute(args);
				Collection<?> children = toCollection(result);

				List<DefaultTreeUINode> childNodes = new ArrayList<>(children.size());
				for (Object childObj : children) {
					DefaultTreeUINode childNode = createNode(node.getModel(), node, childObj);
					if (childNode != null) {
						childNodes.add(childNode);
					}
				}
				return childNodes;
			}

			@Override
			public boolean isFinite() {
				return _config.getCanExpandAll();
			}
		};
	}

	/**
	 * The business object a tree node stands for, the node itself when it is no
	 * {@link DefaultTreeUINode}.
	 */
	private static Object businessObject(Object node) {
		return node instanceof DefaultTreeUINode uiNode ? uiNode.getBusinessObject() : node;
	}

	private static Object[] appendArg(Object[] base, Object extra) {
		Object[] result = new Object[base.length + 1];
		System.arraycopy(base, 0, result, 0, base.length);
		result[base.length] = extra;
		return result;
	}

	private static Collection<?> toCollection(Object result) {
		if (result instanceof Collection<?>) {
			return (Collection<?>) result;
		}
		if (result == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(result);
	}

}
