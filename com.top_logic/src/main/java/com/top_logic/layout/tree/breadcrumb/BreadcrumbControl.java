/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.IdentifierSourceProxy;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.form.tag.js.JSString;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.selection.SingleSelectVetoListener;
import com.top_logic.layout.tree.TreeNodeIdentification;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.renderer.OnClickWriter;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link BreadcrumbControl} renders a {@link TLTreeModel} as bread crumb.
 * The model of an {@link BreadcrumbControl} contains a {@link TLTreeModel} to
 * display, a {@link SingleSelectionModel} to determine the path in the tree to
 * display and an {@link BreadcrumbRenderer} which actually prints the content
 * of the bread crumb.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BreadcrumbControl extends AbstractControlBase implements BreadcrumbDataListener,
		OnClickWriter {

	private static final Map<String, ControlCommand> BREADCRUMB_COMMANDS = createCommandMap(new ControlCommand[] { SelectAction.INSTANCE });

	/**
	 * Helper object which manages the id's of the nodes in the tree.
	 */
	private final TreeNodeIdentification _ids;

	/**
	 * Helper object which manages and accumulates the updates for the GUI
	 */
	final BreadcrumbUpdateListener _updateListener;

	/**
	 * Collection of listeners which wants to do some additional action, in case a non-selectable
	 * node was selected.
	 */
	private final List<InvalidSelectionHandler> invalidSelectionHandlers = new ArrayList<>();
	
	/**
	 * Collection of listeners which potentially wants to stop changing selection.
	 */
    private Collection<SingleSelectVetoListener> selectVetoListeners = new ArrayList<>();
    
	/**
	 * Listener which reacts on changes in the
	 * {@link BreadcrumbData#getSelectionModel() selection model} which
	 * determines which path in the tree is the currently displayed.
	 */
	private final SingleSelectionListener lastNodeListener = new SingleSelectionListener() {

		@Override
		public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
			_updateListener.handleLastNodeChanged(selectedObject);
		}
	};

	/**
	 * Listener which reacts on changes of the selection in the displayed tree.
	 */
	private final SingleSelectionListener viewSelectionListener = new SingleSelectionListener() {

		@Override
		public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
			_updateListener.handleSelectionChanged(formerlySelectedObject, selectedObject);
		}
	};

	/**
	 * The data this {@link BreadcrumbControl} based on
	 */
	private final BreadcrumbData _data;

	private BreadcrumbRenderer _renderer;

	/**
	 * Creates a {@link BreadcrumbControl}.
	 *
	 * @param data
	 *        See {@link #getModel()}.
	 */
	public BreadcrumbControl(BreadcrumbRenderer renderer, BreadcrumbData data) {
		super(BREADCRUMB_COMMANDS);

		_renderer = renderer;
		_data = data;

		_ids = new TreeNodeIdentification(new IdentifierSourceProxy(this));
		_ids.setModel(getTree());

		_updateListener = new BreadcrumbUpdateListener(this);
	}

	@Override
	public BreadcrumbData getModel() {
		return _data;
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		_ids.attach();
		_updateListener.attach();

		getDisplayModel().addSingleSelectionListener(lastNodeListener);
		getSelectionModel().addSingleSelectionListener(viewSelectionListener);
	}

	@Override
	protected void detachInvalidated() {
		getSelectionModel().removeSingleSelectionListener(viewSelectionListener);
		getDisplayModel().removeSingleSelectionListener(lastNodeListener);

		_updateListener.detach();
		_ids.detach();
		super.detachInvalidated();
	}

	@Override
	protected boolean hasUpdates() {
		return _updateListener.hasUpdates();
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		_updateListener.revalidate(actions);
	}

	/**
	 * returns the renderer of this {@link BreadcrumbControl}.
	 */
	public final BreadcrumbRenderer getRenderer() {
		return _renderer;
	}

	/**
	 * resolves the id to a node in the tree
	 */
	final Object getNodeById(String id) {
		return _ids.getObjectById(id);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		getRenderer().write(context, out, this);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		getRenderer().appendControlCSSClasses(out, this);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	/**
	 * Returns the selection model of this bread crumb control. The selection of
	 * the returned model is the node in the tree which is currently selected.
	 */
	public final SingleSelectionModel getSelectionModel() {
		return getModel().getSelectionModel();
	}
    
	/**
	 * Adds the given listener to ask before changing the selection.
	 * 
	 * @param listener
	 *        the listener to check whether the change of a selection can be
	 *        done. must not be <code>null</code>.
	 */
    public void addSelectionVetoListener(SingleSelectVetoListener listener) {
        assert selectVetoListeners.isEmpty() : "Sorry. Currently just one listener is supported";
        selectVetoListeners.add(listener);
    }

    /**
	 * Adds the given handler to manage invalid selections.
	 * 
	 * @param invalidSelectionHandler
	 *        the handler to perform additional actions in case of an selection attempt
	 *        of a non-selectable node. Must not be <code>null</code>.
	 */
    public void addInvalidSelectionHandler(InvalidSelectionHandler invalidSelectionHandler) {
		invalidSelectionHandlers.add(invalidSelectionHandler);
	}
    
    /**
     * Removes the given handler.
     * 
     * @param invalidSelectionHandler
     *        the handler to perform additional actions in case of an selection attempt
     *        of a non-selectable node. Must not be <code>null</code>.
     */
    public void removeInvalidSelectionHandler(InvalidSelectionHandler invalidSelectionHandler) {
    	invalidSelectionHandlers.remove(invalidSelectionHandler);
    }
    
	/**
	 * Returns the node in the tree which determines which path in the tree is
	 * displayed, i.e. the returned node is the last node in the path printed by
	 * this control.
	 */
	public Object getLastNode() {
		Object result = getDisplayModel().getSingleSelection();
		if (result != null) {
			return result;
		}

		// Note: A non-selection is not supported, at least the root node must be selected.
		return getTree().getRoot();
	}

	private SingleSelectionModel getDisplayModel() {
		return getModel().getDisplayModel();
	}

	/**
	 * Sets the node which serves as last node of the printed path.
	 * 
	 * @param obj
	 *        the new last node
	 * 
	 * @see #getLastNode()
	 */
	public void setLastNode(Object obj) {
		getDisplayModel().setSingleSelection(obj);
	}

	/**
	 * Returns the tree this {@link Control} is displaying
	 * 
	 * @return the {@link TLTreeModel} which is rendered by this control
	 */
	public TLTreeModel getTree() {
		return getModel().getTree();
	}

	/**
	 * Returns the client side id of the given node.
	 */
	public String getNodeId(Object node) {
		return _ids.getObjectId(node);
	}

	/**
	 * Write the js function to call to force this control to select the given
	 * node.
	 * 
	 * @param node
	 *        the node to select
	 */
	@Override
	public void writeOnClickSelect(TagWriter out, Object node) throws IOException {
		SelectAction.INSTANCE.writeOnClickSelect(out, this, node);
	}

	/**
	 * Returns the ID of the section command
	 */
	public String getSelectCommand() {
		return SelectAction.COMMAND_ID;
	}

	/**
	 * This method changes the selection of this control. Before changing the selection it checks
	 * for veto's.
	 * 
	 * @param node
	 *        The new node to be selected.
	 * @param programmaticUpdate
	 *        whether the change was triggered by programmatic changes
	 */
	void setNewSelection(Object node, boolean programmaticUpdate) throws VetoException {
		SingleSelectionModel selectionModel = getSelectionModel();

		for (SingleSelectVetoListener theListener : this.selectVetoListeners) {
			theListener.checkVeto(selectionModel, node, programmaticUpdate);
		}

		if(selectionModel.isSelectable(node)) {
			selectionModel.setSingleSelection(node);
		}
		else {
			for (int i = 0, size = invalidSelectionHandlers.size(); i < size; i++) {
				InvalidSelectionHandler handler = invalidSelectionHandlers.get(i);
				handler.handleInvalidSelection(getTree(), selectionModel, node);
			}
		}
	}

    /**
	 * The {@link SelectAction} used by the {@link BreadcrumbControl} to changed
	 * selection in the displayed tree.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static final class SelectAction extends ControlCommand {

		private static final String COMMAND_ID = "select";
		public static final SelectAction INSTANCE = new SelectAction();
		private static final String ID_PARAM = "id";

		private SelectAction() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			final BreadcrumbControl breadcrumb = (BreadcrumbControl) control;

			String id = (String) arguments.get(ID_PARAM);
			final Object node = breadcrumb.getNodeById(id);

			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordSelection(breadcrumb.getModel(), node, true, SelectionChangeKind.ABSOLUTE);
			}

			selectNode(breadcrumb, node);

			return HandlerResult.DEFAULT_RESULT;
		}

		final void selectNode(final BreadcrumbControl breadcrumb, final Object node) {
			try {
				breadcrumb.setNewSelection(node, false);
			} catch (VetoException ex) {
				ex.setContinuationCommand(new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						selectNode(breadcrumb, node);
						return HandlerResult.DEFAULT_RESULT;
					}

				});
				ex.process(breadcrumb.getWindowScope());
			}
		}

		/**
		 * Writes the JavaScript call to force the given control to select the given node.
		 * 
		 * @param control
		 *        the control which requested the action.
		 * @param node
		 *        the node to select
		 * 
		 * @see BreadcrumbControl#writeOnClickSelect(TagWriter, Object)
		 */
		void writeOnClickSelect(TagWriter out, BreadcrumbControl control, Object node) throws IOException {
			out.beginAttribute(ONCLICK_ATTR);
			writeOnClickSelectContent(out, control, node);
			out.endAttribute();
		}

		private void writeOnClickSelectContent(TagWriter out, BreadcrumbControl control, Object node)
				throws IOException {
			final JSObject argument = new JSObject(ID_PARAM, new JSString(control.getNodeId(node)));
			out.append("return ");
			writeInvokeExpression(out, control, argument);
			out.append(';');
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SELECT_ACTION;
		}

	}

	@Override
	public void notifyDisplayModelChanged(BreadcrumbData source, SingleSelectionModel oldModel, SingleSelectionModel newModel) {
		if (source != getModel()) {
			return;
		}

		if (isAttached() && !isRepaintRequested()) {
			// model has changed so we must remove listener manually since
			// #requestRepaint() would remove the listener from the new model implicitly found in
			// the breadcrumb data at the time the event is processed.
			oldModel.removeSingleSelectionListener(lastNodeListener);
			requestRepaint();
		}
	}

	@Override
	public void notifySelectionModelChanged(BreadcrumbData source,
			SingleSelectionModel oldModel, SingleSelectionModel newModel) {
		if (source != getModel()) {
			return;
		}

		if (isAttached() && !isRepaintRequested()) {
			oldModel.removeSingleSelectionListener(viewSelectionListener);
			requestRepaint();
		}
	}
	
	@Override
	public void notifyTreeChanged(BreadcrumbData source, TLTreeModel<?> oldTree, TLTreeModel<?> newTree) {
		if (source != getModel()) {
			return;
		}
		requestRepaint();
	}

}
