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
 * Input data is provided via {@link ViewChannel}s. The root object and child lists are computed
 * using TL-Script expressions, and the tree structure is built lazily via a custom
 * {@link TreeBuilder}.
 * </p>
 *
 * <p>
 * Note: The optional config properties {@code isLeaf}, {@code supportsNode},
 * {@code modelForNode}, {@code parents}, and {@code nodesToUpdate} are declared for future use
 * and currently not wired into the runtime. They parse correctly but have no effect. Leaf status
 * is determined by whether {@code children} returns an empty list. Incremental update support
 * will be added when the view system gains model event integration.
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

		/** Configuration name for {@link #getIsLeaf()}. */
		String IS_LEAF = "isLeaf";

		/** Configuration name for {@link #getSupportsNode()}. */
		String SUPPORTS_NODE = "supportsNode";

		/** Configuration name for {@link #getModelForNode()}. */
		String MODEL_FOR_NODE = "modelForNode";

		/** Configuration name for {@link #getParents()}. */
		String PARENTS = "parents";

		/** Configuration name for {@link #getNodesToUpdate()}. */
		String NODES_TO_UPDATE = "nodesToUpdate";

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
		 * Optional TL-Script function determining whether a node is a leaf.
		 *
		 * <p>
		 * Takes the input channel values followed by the node business object as last argument.
		 * Returns a boolean. If not set, leaf status is determined by whether
		 * {@link #getChildren()} returns an empty list.
		 * </p>
		 */
		@Name(IS_LEAF)
		Expr getIsLeaf();

		/**
		 * Optional TL-Script function for incremental update decisions.
		 *
		 * <p>
		 * Takes the input channel values followed by a candidate object as last argument.
		 * </p>
		 */
		@Name(SUPPORTS_NODE)
		Expr getSupportsNode();

		/**
		 * Optional TL-Script function for reverse model lookup.
		 *
		 * <p>
		 * Takes the input channel values followed by a candidate object as last argument.
		 * </p>
		 */
		@Name(MODEL_FOR_NODE)
		Expr getModelForNode();

		/**
		 * Optional TL-Script function computing the parent chain.
		 *
		 * <p>
		 * Takes the input channel values followed by a node object as last argument.
		 * </p>
		 */
		@Name(PARENTS)
		Expr getParents();

		/**
		 * Optional TL-Script function computing nodes to update on a change.
		 *
		 * <p>
		 * Takes the input channel values followed by a changed object as last argument.
		 * </p>
		 */
		@Name(NODES_TO_UPDATE)
		Expr getNodesToUpdate();

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
		 * Optional reference to a {@link ViewChannel} to write the selected node's business
		 * object to.
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
		treeControl.setCssClass(_config.getCssClass());

		// 6. Wire selection channel.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			selectionModel.addSelectionListener(new TreeSelectionBinding<>(selectionChannel));
		}

		// 7. Wire the activation command, which runs with the activated node's business object.
		if (_onActivate != null && _onActivateConfig != null) {
			ViewCommandModel activation = ViewCommandModel.forCommand(context, _onActivate, _onActivateConfig);
			treeControl.setActivationHandler(node -> activation.execute(context, businessObject(node)));
		}

		// 8. Create ObservableTreeModel to forward model changes to the tree control.
		Set<TLStructuredType> observedTypes = ObservedTypes.resolve(_config.getObservedTypes());
		QueryExecutor rootExec = _rootExecutor;
		ObservableTreeModel observableModel = new ObservableTreeModel(
			treeControl,
			treeModel,
			args -> rootExec.execute(args),
			builder,
			observedTypes,
			inputChannels
		);

		// 9. Observe the model only while the tree is displayed.
		treeControl.addAttachListener(() -> {
			observableModel.attach(context.getModelScope());
		});
		treeControl.addDetachListener(observableModel::detach);

		return treeControl;
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
