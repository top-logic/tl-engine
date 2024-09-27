/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.IndexPosition;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.scripting.action.ActionChain;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.DynamicAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder.Event;
import com.top_logic.layout.scripting.runtime.LiveActionContext;
import com.top_logic.layout.scripting.runtime.action.DynamicActionOp;
import com.top_logic.layout.scripting.runtime.execution.ScriptDriver;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.tree.DefaultTreeData;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.TreeDataOwner;
import com.top_logic.layout.tree.component.ExpandCollapseAll;
import com.top_logic.layout.tree.dnd.DefaultTreeDrag;
import com.top_logic.layout.tree.dnd.TreeDragSource;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultMutableTreeNodeBuilder;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.MutableTLTreeModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.tool.boundsec.AbstractSystemCommand;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.NoSelectionDisabled;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * Displays {@link ApplicationAction}s in a Tree. Provides organizing operations like grouping and
 * deleting actions. Furthermore, allows to record actions.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("synthetic-access")
public class ScriptRecorderTree extends BoundComponent implements TreeDataOwner, Selectable, ControlRepresentable {

	/**
	 * Configuration options for {@link ScriptRecorderTree}.
	 */
	public interface Config extends BoundComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** @see #getTreeDropTarget() */
		String TREE_DROP_TARGET = "treeDropTarget";

		/** @see #getTreeDragSource() */
		String TREE_DRAG_SOURCE = "treeDragSource";

		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		/**
		 * The {@link TreeDragSource} to use.
		 */
		@Name(TREE_DRAG_SOURCE)
		@ItemDefault(DefaultTreeDrag.class)
		PolymorphicConfiguration<TreeDragSource> getTreeDragSource();

		/**
		 * The {@link TreeDropTarget} handler to use.
		 */
		@Name(TREE_DROP_TARGET)
		@ItemDefault(ScriptingRecorderDropTarget.class)
		PolymorphicConfiguration<TreeDropTarget> getTreeDropTarget();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			BoundComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(ExpandCollapseAll.EXPAND_ID);
			registry.registerButton(ExpandCollapseAll.COLLAPSE_ID);
		}

	}

	/**
	 * Component property to store the last cut node.
	 */
	public static final Property<DefaultMutableTLTreeNode> CLIPBOARD =
		TypedAnnotatable.property(DefaultMutableTLTreeNode.class, "clipboard");

	/**
	 * {@link CommandHandler} deleting the selected action.
	 */
	public static final class Delete extends AbstractSystemCommand {

		/**
		 * Creates a {@link ScriptRecorderTree.Delete} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Delete(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model, Map<String, Object> arguments) {
			ScriptRecorderTree tree = (ScriptRecorderTree) component;
			if (tree.isNothingSelected()) {
				return HandlerResult.error(I18NConstants.ERROR_NO_ACTION_SELECTED);
			}

			DefaultMutableTLTreeNode node;
			if (tree.isRootSelected()) {
				DefaultMutableTLTreeNode root = tree.getRoot();

				// Copy root node.
				node = root.createChild(null);
				root.removeChild(root.getIndex(node));
				node.setBusinessObject(root.getBusinessObject());

				tree.setAction(root, ActionFactory.actionChain());
			} else {
				node = tree.getSelectedNode();
				tree.deleteNode(node);
				tree.fireModelEvent(model, ModelEventListener.MODEL_MODIFIED);
			}
			component.set(CLIPBOARD, node);

			return HandlerResult.DEFAULT_RESULT;
		}
		
		@Override
		public ExecutabilityRule createExecutabilityRule() {
			return DerivedSelectionDisabled.INSTANCE;
		}
	}

	public static final class Paste extends AbstractSystemCommand {

		public Paste(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component,
				Object model, Map<String, Object> arguments) {
			ScriptRecorderTree tree = (ScriptRecorderTree) component;
			if (tree.isNothingSelected()) {
				return HandlerResult.error(I18NConstants.ERROR_NO_ACTION_SELECTED);
			}

			DefaultMutableTLTreeNode node = component.get(CLIPBOARD);
			if (node == null) {
				HandlerResult error = new HandlerResult();
				error.addError(I18NConstants.CLIPBOARD_EMPTY);
				return error;
			}

			ApplicationAction clipboardAction = ScriptRecorderTree.action(node);
			/* Need to insert a copy of the action in clipboard: Otherwise with multiple paste's the
			 * same action is multiple time displayed in the tree. If one of the actions is
			 * modified, also all others are modified. Moreover an action can be pasted as child of
			 * itself which leads to an overflow error. */
			ApplicationAction newAction = TypedConfiguration.copy(clipboardAction);
			tree.insertNodeForAction(newAction);

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ExecutabilityRule createExecutabilityRule() {
			return CombinedExecutabilityRule.combine(DerivedSelectionDisabled.INSTANCE, new ExecutabilityRule() {
				@Override
				public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
					if (aComponent.get(CLIPBOARD) == null) {
						return ExecutableState.createDisabledState(I18NConstants.CLIPBOARD_EMPTY);
					}
					return ExecutableState.EXECUTABLE;
				}
			});
		}
	}

	public static final class UnGroup extends AbstractSystemCommand {

		public UnGroup(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model, Map<String, Object> arguments) {
			ScriptRecorderTree tree = (ScriptRecorderTree) component;
			if (tree.isNothingSelected()) {
				return HandlerResult.error(I18NConstants.ERROR_NO_ACTION_SELECTED);
			}

			if (tree.isRootSelected()) {
				return HandlerResult.error(I18NConstants.ERROR_CANNOT_UNGROUP_ROOT);
			}

			DefaultMutableTLTreeNode selectedNode = tree.getSelectedNode();
			DefaultMutableTLTreeNode parent = selectedNode.getParent();
			DefaultMutableTLTreeNode grandParent = parent.getParent();
			if (grandParent == null) {
				return HandlerResult.error(I18NConstants.ERROR_CANNOT_UNGROUP_TOPLEVEL_NODE);
			}

			int selectedNodeIndex = parent.getIndex(selectedNode);
			int parentIndex = grandParent.getIndex(parent);

			List<ApplicationAction> parentActions = actions(parent);
			List<ApplicationAction> grandParentActions = actions(grandParent);

			// All children with index greater or equal to selectedNodeIndex are moved from its
			// parent its grandparent.
			for (int i = 0; parent.getChildCount() > selectedNodeIndex; i++) {
				DefaultMutableTLTreeNode movedNode = parent.getChildAt(selectedNodeIndex);
				int grandParentIndex = parentIndex + 1 + i;
				movedNode.moveTo(grandParent, grandParentIndex);

				// Reflect change in the action model.
				ApplicationAction movedAction = parentActions.remove(selectedNodeIndex);
				grandParentActions.add(grandParentIndex, movedAction);
				
				assert movedAction == action(movedNode);
			}

			if (parent.getChildren().isEmpty()) {
				tree.deleteNode(parent);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ExecutabilityRule createExecutabilityRule() {
			return CombinedExecutabilityRule.combine(DerivedSelectionDisabled.INSTANCE, new ExecutabilityRule() {
				@Override
				public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
					ScriptRecorderTree tree = (ScriptRecorderTree) component;
					DefaultMutableTLTreeNode selectedNode = tree.getSelectedNode();
					if (selectedNode == null) {
						return ExecutableState.createDisabledState(I18NConstants.ERROR_NO_ACTION_SELECTED);
					}
					DefaultMutableTLTreeNode parent = selectedNode.getParent();
					if (parent == null) {
						return ExecutableState.createDisabledState(I18NConstants.ERROR_CANNOT_UNGROUP_ROOT);
					}
					DefaultMutableTLTreeNode grandParent = parent.getParent();
					if (grandParent == null) {
						return ExecutableState.createDisabledState(I18NConstants.ERROR_CANNOT_UNGROUP_TOPLEVEL_NODE);
					}
					return ExecutableState.EXECUTABLE;
				}
			});
		}
	}

	public static final class Group extends AbstractSystemCommand {

		public Group(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model, Map<String, Object> arguments) {
			ScriptRecorderTree tree = (ScriptRecorderTree) component;
			if (tree.isNothingSelected()) {
				return HandlerResult.error(I18NConstants.ERROR_NO_ACTION_SELECTED);
			}

			if (tree.isRootSelected()) {
				return HandlerResult.error(I18NConstants.ERROR_CANNOT_GROUP_ROOT);
			}

			DefaultMutableTLTreeNode selectedNode = tree.getSelectedNode();
			DefaultMutableTLTreeNode parent = selectedNode.getParent();
			int groupIndex = parent.getIndex(selectedNode);
			ActionChain newChain = ActionFactory.actionChain();
			DefaultMutableTLTreeNode newParentNode =
				tree.createNode(parent, IndexPosition.before(groupIndex), newChain);

			List<ApplicationAction> parentActions = actions(parent);
			List<ApplicationAction> newActions = newChain.getActions();

			int indexAfterGroup = groupIndex + 1;
			while (parent.getChildCount() > indexAfterGroup) {
				DefaultMutableTLTreeNode movedNode = parent.getChildAt(indexAfterGroup);
				movedNode.moveTo(newParentNode);

				// Reflect change in action model.
				ApplicationAction movedAction = parentActions.remove(indexAfterGroup);
				newActions.add(movedAction);

				assert movedAction == action(movedNode);
			}
			tree.getTreeUiModel().setExpanded(newParentNode, true);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ExecutabilityRule createExecutabilityRule() {
			return CombinedExecutabilityRule.combine(DerivedSelectionDisabled.INSTANCE, new RootSelectionDisabled(I18NConstants.ERROR_CANNOT_GROUP_ROOT));
		}
	}

	/**
	 * {@link ExecutabilityRule} that disables commands on
	 * {@link ScriptRecorderTree#isDerived(TLTreeNode) derived} nodes.
	 */
	public static final class DerivedSelectionDisabled implements ExecutabilityRule {
		/**
		 * Singleton {@link ScriptRecorderTree.DerivedSelectionDisabled} instance.
		 */
		public static final ExecutabilityRule INSTANCE = CombinedExecutabilityRule.combine(NoSelectionDisabled.INSTANCE, new DerivedSelectionDisabled());

		private DerivedSelectionDisabled() {
			// Singleton constructor.
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			Selectable selectable = (Selectable) aComponent;
			TLTreeNode<?> selection = (TLTreeNode<?>) selectable.getSelected();
			if (selection == null) {
				return ExecutableState.NO_EXEC_NO_MODEL;
			}
			if (isDerived(selection)) {
				return ExecutableState.createDisabledState(I18NConstants.ERROR_DERIVED_NODE_CANNOT_BE_EDITED);
			}
			return ExecutableState.EXECUTABLE;
		}
	}

	/**
	 * {@link ExecutabilityRule} that disables commands on
	 * {@link ScriptRecorderTree#isDerived(TLTreeNode) derived} nodes.
	 */
	public static final class RootSelectionDisabled implements ExecutabilityRule {
		private final ResKey _error;

		public RootSelectionDisabled(ResKey error) {
			_error = error;
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			ScriptRecorderTree selectable = (ScriptRecorderTree) aComponent;
			if (selectable.isRootSelected()) {
				return ExecutableState.createDisabledState(_error);
			}
			return ExecutableState.EXECUTABLE;
		}
	}

	public static final class Start extends AbstractSystemCommand {

		private final Config _config;

		public interface Config extends AbstractSystemCommand.Config {

			ThemeImage getPauseImage();
			ResKey getPauseLabel();

		}

		public Start(InstantiationContext context, Config config) {
			super(context, config);
			_config = config;
		}

		@Override
		public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model, Map<String, Object> arguments) {
			ScriptRecorderTree tree = (ScriptRecorderTree) component;
			if (tree.isRecording()) {
				tree.stopRecording();
			} else {
				tree.startRecording();
			}
			ResKey labelKey = tree.isRecording() ? _config.getPauseLabel() : getResourceKey(component);
			CommandModel commandModel = getCommandModel(arguments);
			commandModel.setLabel(labelKey);
			commandModel.setImage(tree.isRecording() ? _config.getPauseImage() : getImage(component));
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private static final Property<Boolean> DERIVED = TypedAnnotatable.property(Boolean.class, "derived", Boolean.FALSE);

	private static final Property<String> ERROR = TypedAnnotatable.property(String.class, "error");

	private final SelectionListener uiSelectionForwarder = new SelectionListener() {
		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
			TLTreeNode<?> selectedNode = (TLTreeNode<?>) CollectionUtil.getSingleValueFromCollection(selectedObjects);
			setSelected(selectedNode);
		}
	};

	private final TreeModelListener selectCollapsedNodeListener = new TreeModelListener() {

		@Override
		public void handleTreeUIModelEvent(TreeModelEvent event) {
			if (event.getType() != TreeModelEvent.BEFORE_COLLAPSE) {
				return;
			}
			TLTreeNode<?> collapsedNode = (TLTreeNode<?>) event.getNode();
			if ((!isNothingSelected()) && isAncestor(collapsedNode, getSelectedNode())) {
				setSelected(collapsedNode);
			}
		}
	};

	/**
	 * Currently used {@link ScriptDriver}.
	 * 
	 * <p>
	 * When the scripted test execution is started by a user, the driver is cached in this property.
	 * The driver itself knows which action it has processed and which still need to be executed.
	 * </p>
	 * 
	 * <p>
	 * This allow the application to resume the scripted test execution after a page reload.
	 * </p>
	 * 
	 * @see ResumeScriptExecutionCommand
	 * @see ActionTreeRenderer
	 */
	public static TypedAnnotatable.Property<ScriptDriver> SCRIPT_DRIVER =
		TypedAnnotatable.property(ScriptDriver.class, "scriptDriver");

	private Iterator<Event> eventSource;

	private TreeData _uiModel;

	private boolean _isRecording = false;

	private final TreeDragSource _dragSource;

	private final List<TreeDropTarget> _dropTargets;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ScriptRecorderTree}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ScriptRecorderTree(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_dragSource = context.getInstance(config.getTreeDragSource());
		_dropTargets = Arrays.asList(context.getInstance(config.getTreeDropTarget()));
	}

	@Override
	public TreeData getTreeData() {
		return getUIModel();
	}

	public boolean isRecording() {
		return _isRecording;
	}

	private void startRecording() {
		ScriptingRecorder.getInstance().setRecordingActive(true);
		_isRecording = true;
		enableEventQueue();
	}

	private void stopRecording() {
		ScriptingRecorder.getInstance().setRecordingActive(false);
		_isRecording = false;
		freezeEventQueue();
	}

	private void enableEventQueue() {
		eventSource = ScriptingRecorder.getInstance().getEventPointer();
		assert eventSource != null : "Failed to attach to ScriptRecorder.";
	}

	private void freezeEventQueue() {
		if (eventSource != null) {
			eventSource = CollectionUtil.toList(eventSource).iterator();
		}
	}

	@Override
	public Control getRenderingControl() {
		return new ScriptRecorderTreeControl(getUIModel(), this);
	}
	
	@Override
	public boolean isModelValid() {
		return _uiModel != null && (eventSource == null || !eventSource.hasNext()) && super.isModelValid();
	}
	
	@Override
	public boolean validateModel(DisplayContext context) {
		boolean result = super.validateModel(context);
		
		if (getModel() == null) {
			setModel(ScriptContainer.createTransient(ActionFactory.actionChain()));
			result = true;
		}

		DefaultMutableTLTreeNode newNode = null;
		while (eventSource != null && eventSource.hasNext()) {
			Event event = eventSource.next();
			if (!(Utils.equals(event.getSessionId(), TLContext.getContext().getSessionContext().getId()))) {
				continue;
			}

			newNode = insertAction(event.getAction());
		}
		if (newNode != null) {
			// Inform the masters:
			fireModelEvent(getModel(), ModelEventListener.MODEL_MODIFIED);
		}
		
		if (_uiModel == null) {
			TLTreeModel<?> treeModel =
				new DefaultMutableTLTreeModel(DefaultMutableTreeNodeBuilder.INSTANCE, getModelTyped().getAction());
			TreeUIModel<?> treeUiModel = new DefaultStructureTreeUIModel(treeModel);
			treeUiModel.addTreeModelListener(selectCollapsedNodeListener);
			// No SelectionModelOwner is needed, as it is only needed for script recording,
			// and the whole scripting gui is excluded from script recording.
			// Therefore, SelectionModelOwner.NO_OWNER is good enough.
			DefaultSingleSelectionModel selectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
			ActionTreeRenderer renderer = new ActionTreeRenderer();
			_uiModel =
				new DefaultTreeData(Maybe.some(this), treeUiModel, selectionModel, renderer, _dragSource, _dropTargets);
			ScriptingRecorder.annotateAsDontRecord(_uiModel);
			selectionModel.addSelectionListener(uiSelectionForwarder);
			setSelected(getRoot());
		}
		return result;
	}

	@Override
	protected boolean receiveModelCreatedEvent(Object model, Object changedBy) {
		boolean result = super.receiveModelCreatedEvent(model, changedBy);

		// Note: ApplicationAction are sub-session-local. Therefore, these models can by inserted
		// without further checks.
		if (model instanceof ApplicationAction) {
			/* The TemplateInstantiationCommand sends such events. There might be further senders. */
			insertAction((ApplicationAction) model);
		}

		return result;
	}

	@Override
	protected boolean receiveModelChangedEvent(Object model, Object changedBy) {
		if (model instanceof TemplateResource) {
			// Update nodes that potentially use the changed template. Since templates may call
			// other templates, this change might affect every node containing a template reference.

			updateChildren(getRoot(), (ApplicationAction) getRoot().getBusinessObject(), false);
		}
		else if (getSelected() == model) {
			DefaultMutableTLTreeNode node = (DefaultMutableTLTreeNode) model;
			setAction(node, (ApplicationAction) node.getBusinessObject());
		}

		return super.receiveModelChangedEvent(model, changedBy);
	}

	DefaultMutableTLTreeNode insertAction(ApplicationAction newAction) {
		DefaultMutableTLTreeNode newNode;
		int insertIndex;
		DefaultMutableTLTreeNode parent;
		DefaultMutableTLTreeNode reference = getSelectedNode();
		while (reference != null && isDerived(reference)) {
			reference = reference.getParent();
		}
		if (reference == null || reference == getRoot()) {
			parent = getRoot();
			insertIndex = parent.getChildCount();
		} else {
			parent = reference.getParent();
			insertIndex = 1 + parent.getIndex(reference);
		}

		IndexPosition position = IndexPosition.before(insertIndex++);

		newNode = createNode(parent, position, newAction);
		setSelected(newNode);
		return newNode;
	}

	private ScriptContainer getModelTyped() {
		return (ScriptContainer) getModel();
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		if (getUIModel() == null) {
			return;
		}
		ScriptContainer newScriptContainer = (ScriptContainer) newModel;
		enforceActionChainAsRoot(newScriptContainer);

		setAction(getRoot(), newScriptContainer.getAction());
		setSelected(getRoot());
		fireModelModifiedEvent(getRoot(), this);
	}

	private void enforceActionChainAsRoot(ScriptContainer newScriptContainer) {
		if (newScriptContainer.getAction() instanceof ActionChain) {
			return;
		}
		newScriptContainer.setAction(ActionFactory.actionChain(newScriptContainer.getAction()));
		fireModelModifiedEvent();
	}

	private void fireModelModifiedEvent() {
		fireModelModifiedEvent(getModel(), this);
	}

	private TreeData getUIModel() {
		return _uiModel;
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof ScriptContainer;
	}

	static boolean isDerived(TLTreeNode<?> node) {
		return node.get(DERIVED);
	}

	static boolean hasError(TLTreeNode<?> node) {
		return getError(node) != null;
	}

	static String getError(TLTreeNode<?> node) {
		return node.get(ERROR);
	}

	private void expandPathToNode(TLTreeNode<?> node) {
		if (node == null || Utils.equals(node, getRoot())) {
			return;
		}
		TLTreeNode<?> parent = node;
		do {
			parent = parent.getParent();
			if (parent == null) {
				// Node not found.
				break;
			}
			getTreeUiModel().setExpanded(parent, true);
		} while (!Utils.equals(parent, getRoot()));
	}

	private static List<ApplicationAction> actions(DefaultMutableTLTreeNode parent) {
		return actionChain(parent).getActions();
	}

	static ActionChain actionChain(DefaultMutableTLTreeNode parent) {
		return (ActionChain) action(parent);
	}

	public static Boolean isActionNode(Object node) {
		return action(node) != null;
	}

	public static ApplicationAction action(Object node) {
		if (!(node instanceof TLTreeNode<?>)) {
			return null;
		}

		return action((TLTreeNode<?>) node);
	}

	public static ApplicationAction action(TLTreeNode<?> node) {
		if (node == null) {
			return null;
		}

		Object businessObject = node.getBusinessObject();
		if (!(businessObject instanceof ApplicationAction)) {
			return null;
		}
		return (ApplicationAction) businessObject;
	}

	private void setAction(DefaultMutableTLTreeNode node, ApplicationAction action) {
		DefaultMutableTLTreeNode parent = node.getParent();
		if (parent != null) {
			// Link to parent action.
			int parentIndex = parent.getIndex(node);
			actions(parent).set(parentIndex, action);
		}
		node.setBusinessObject(action);
		if (node == getRoot()) {
			getModelTyped().setAction(action);
			fireModelModifiedEvent();
		}
		fireModelModifiedEvent(node, this);
		updateChildren(node, action, isDerived(node));
	}

	private void updateChildren(DefaultMutableTLTreeNode node, ApplicationAction action, boolean derived) {
		node.clearChildren();
		if (action instanceof ActionChain) {
			// Distribute chained actions to child nodes.
			ActionChain chain = (ActionChain) action;
			List<ApplicationAction> parts = chain.getActions();

			importChildren(node, parts, derived);
		}
		else if (action instanceof DynamicAction) {
			DynamicActionOp<?> actionOp =
				(DynamicActionOp<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(action);
			List<ApplicationAction> children;
			try {
				children = actionOp.createActions(new LiveActionContext(DefaultDisplayContext.getDisplayContext(), this));
				importChildren(node, children, true);
				node.set(ERROR, null);
			} catch (Exception ex) {
				String message = ex.getMessage();
				Throwable cause = ex.getCause();
				while (cause != null) {
					int last = message.length() - 1;
					while (last >= 0 && !Character.isLetterOrDigit(message.charAt(last))) {
						last--;
					}
					String prefix = message.substring(0, last + 1);
					message = prefix + ": " + cause.getClass().getSimpleName() + ": " + cause.getMessage();
					cause = cause.getCause();
				}
				node.set(ERROR, message);
			}
		}
	}

	private void importChildren(DefaultMutableTLTreeNode target, List<ApplicationAction> innerActions, boolean derived) {
		target.clearChildren();
		for (ApplicationAction action : innerActions) {
			DefaultMutableTLTreeNode child = target.createChild(action);
			if (derived) {
				child.set(DERIVED, Boolean.TRUE);
			}
			updateChildren(child, action, derived);
		}
	}

	// Selection

	private boolean isRootSelected() {
		return getSelectedNode() == getRoot();
	}

	private boolean isNothingSelected() {
		return getSelectedNode() == null;
	}

	final DefaultMutableTLTreeNode getSelectedNode() {
		return (DefaultMutableTLTreeNode) getSelected();
	}

	/**
	 * Finds the node of the given action and selects it.
	 * 
	 * @param newSelectedAction
	 *        The action of the node to select.
	 * @return See the result of {@link #setSelected(Object)}.
	 */
	public boolean setSelectedAction(ApplicationAction newSelectedAction) {
		ApplicationAction oldSelectedAction = action(getSelectedNode());
		boolean change = (newSelectedAction != oldSelectedAction);
		if (!change) {
			return false;
		}
		TLTreeNode<?> newSelectedNode =
			(newSelectedAction == null) ? null : findNodeWithBusinessObject(newSelectedAction).getElse(null);
		return setSelected(newSelectedNode);
	}

	private SelectionModel getSelectionModel() {
		TreeData uiModel = getUIModel();
		if (uiModel == null) {
			return null;
		}
		return uiModel.getSelectionModel();
	}

	// Tree Operations

	/**
	 * The indexes of the parents of the given node. From root (not included) to the node itself
	 * (last index). Empty list means: The given node is the root node.
	 * 
	 * @param node
	 *        Must not be <code>null</code>!
	 */
	private List<Integer> createIndexPathToNode(TLTreeNode<?> node) {
		List<Integer> indexPathToNode = new ArrayList<>();
		while (!Utils.equals(node, getRoot())) {
			indexPathToNode.add(node.getParent().getIndex(node));
			node = node.getParent();
		}
		Collections.reverse(indexPathToNode);
		return indexPathToNode;
	}

	/**
	 * Expects the given list to contain the indexes of the parents of the wanted node, from root
	 * (not included) to the wanted node (last index). Empty list means: The wanted node is the root
	 * node.
	 * 
	 * @see #createIndexPathToNode(TLTreeNode)
	 * 
	 * @param indexPath
	 *        Must not be <code>null</code>!
	 */
	private TLTreeNode<?> getNodeByIndexPath(List<Integer> indexPath) {
		TLTreeNode<?> node = getRoot();
		for (Integer index : indexPath) {
			node = node.getChildAt(index);
		}
		return node;
	}

	DefaultMutableTLTreeNode createNode(DefaultMutableTLTreeNode parent, IndexPosition position,
			ApplicationAction newAction) {
		DefaultMutableTLTreeNode newNode = parent.createChild(position, newAction);
		updateActionChain(parent, position, newAction, newNode);
		return newNode;
	}

	/**
	 * Updates the chain of {@link ApplicationAction ApplicationActions} stored in the given parent node
	 * with the ApplicationAction of the given newNode.
	 */
	void updateActionChain(DefaultMutableTLTreeNode parent, IndexPosition position, ApplicationAction newAction, DefaultMutableTLTreeNode newNode) {
		List<ApplicationAction> parentActions = actions(parent);

		// Reflect change in the action model.
		parentActions.add(position.beforeIndex(parentActions), newAction);

		updateChildren(newNode, newAction, false);
	}

	/**
	 * Deletes the given node from the tree and clears the selection if the deleted node was
	 * selected.
	 */
	private void deleteNode(DefaultMutableTLTreeNode node) {
		boolean isSelected = Utils.equals(getSelectedNode(), node);
		if (isSelected) {
			setSelected(findAfterDeleteSelection(node));
		}
		internalDeleteNode(node);
	}

	private void internalDeleteNode(DefaultMutableTLTreeNode node) {
		DefaultMutableTLTreeNode parent = node.getParent();
		int removedIndex = parent.getIndex(node);
		parent.removeChild(removedIndex);

		// Reflect change in action model.
		actions(parent).remove(removedIndex);
	}

	void insertNodeForAction(ApplicationAction action) {
		DefaultMutableTLTreeNode selected = getSelectedNode();
		DefaultMutableTLTreeNode parent = selected.getParent();

		DefaultMutableTLTreeNode newChild;
		if (parent == null) {
			newChild = createNode(selected, IndexPosition.END, action);
		} else {
			newChild = createNode(parent, IndexPosition.after(parent.getIndex(selected)), action);
		}

		setSelected(newChild);
	}

	private DefaultMutableTLTreeNode findAfterDeleteSelection(DefaultMutableTLTreeNode node) {
		DefaultMutableTLTreeNode parent = node.getParent();
		int nodeIndex = parent.getIndex(node);
		if (parent.getChildCount() > (nodeIndex + 1)) {
			return parent.getChildAt(nodeIndex + 1);
		}
		if (nodeIndex > 0) {
			return parent.getChildAt(nodeIndex - 1);
		}
		return parent;
	}

	private boolean isAncestor(TLTreeNode<?> possibleAncestor, TLTreeNode<?> node) {
		return getAncestors(node).contains(possibleAncestor);
	}

	/**
	 * The list of ancestors of the node. <br/>
	 * First node: The parent of the node <br/>
	 * Last node: The root
	 */
	private List<TLTreeNode<?>> getAncestors(TLTreeNode<?> node) {
		List<TLTreeNode<?>> ancestorList = new ArrayList<>();
		for (TLTreeNode<?> ancestor = node.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
			ancestorList.add(ancestor);
		}
		return ancestorList;
	}

	private Maybe<TLTreeNode<?>> findNodeWithBusinessObject(Object businessObject) {
		DescendantDFSIterator<?> iterator =
			new DescendantDFSIterator<>(getTreeModel(), getTreeModel().getRoot());
		return findNodeWithBusinessObject(businessObject, iterator);
	}

	private Maybe<TLTreeNode<?>> findNodeWithBusinessObject(Object businessObject, Iterator<?> treeNodeIterator) {
		while (treeNodeIterator.hasNext()) {
			TLTreeNode<?> node = (TLTreeNode<?>) treeNodeIterator.next();
			if (node.getBusinessObject() == businessObject) {
				return Maybe.<TLTreeNode<?>> some(node);
			}
		}
		return Maybe.none();
	}

	private DefaultMutableTLTreeNode getRoot() {
		return getTreeModel().getRoot();
	}

	private MutableTLTreeModel getTreeModel() {
		return (MutableTLTreeModel) getTreeUiModel().getWrappedModel();
	}

	private DefaultStructureTreeUIModel getTreeUiModel() {
		return (DefaultStructureTreeUIModel) getUIModel().getTreeModel();
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		selectionChannel().addListener(
			(sender, oldValue, newValue) -> ((ScriptRecorderTree) sender.getComponent()).onSelectionChange(newValue));
	}

	private void onSelectionChange(Object newValue) {
		SelectionModel selectionModel = getSelectionModel();
		if (selectionModel != null) {
			expandPathToNode((TLTreeNode<?>) newValue);

			Set<Object> selection = newValue == null ? Collections.emptySet() : Collections.singleton(newValue);
			selectionModel.setSelection(selection);
		}
	}

	@Override
	protected void becomingInvisible() {
		stopRecording();
		super.becomingInvisible();
	}

}
