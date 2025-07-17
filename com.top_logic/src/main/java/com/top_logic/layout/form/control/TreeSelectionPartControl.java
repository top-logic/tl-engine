/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Iterator;

import com.top_logic.basic.col.InlineList;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl.SelectionType;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SubtreeSelectionModel;
import com.top_logic.mig.html.SubtreeSelectionModel.StateChanged;
import com.top_logic.mig.html.SubtreeSelectionModel.TreeSelectionListener;
import com.top_logic.mig.html.TriState;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link TriStateCheckboxControl} displaying the selection state of on tree node.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeSelectionPartControl<N> extends TriStateCheckboxControl implements TreeSelectionListener<N> {

	private SubtreeSelectionModel<N> _selectionModel;

	private N _node;

	private Object _vetoListeners = InlineList.newInlineList();

	/**
	 * Creates a {@link TreeSelectionPartControl}.
	 */
	public TreeSelectionPartControl(SubtreeSelectionModel<N> selectionModel, N part) {
		_selectionModel = selectionModel;
		_node = part;
	}

	@Override
	public SubtreeSelectionModel<N> getModel() {
		return _selectionModel;
	}

	/**
	 * Registers the given {@link SelectionVetoListener}.
	 * 
	 * @param listener
	 *        Listener to check, before selection changes.
	 */
	public void addSelectionVetoListener(SelectionVetoListener listener) {
		_vetoListeners = InlineList.add(SelectionVetoListener.class, _vetoListeners, listener);
	}

	/**
	 * Removes the given {@link SelectionVetoListener}.
	 * 
	 * @param listener
	 *        Listener to remove.
	 */
	public void removeSelectionVetoListener(SelectionVetoListener listener) {
		_vetoListeners = InlineList.remove(_vetoListeners, listener);
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		getModel().addTreeSelectionListener(this);
	}

	@Override
	protected void internalDetach() {
		getModel().removeTreeSelectionListener(this);
		super.internalDetach();
	}

	@Override
	protected State currentState() {
		TriState selectionState = _selectionModel.getState(_node);
		switch (selectionState) {
			case INDETERMINATE:
				return State.INDETERMINATE;
			case NOT_SELECTED:
				return State.UNCHECKED;
			case SELECTED:
				return State.CHECKED;
		}
		throw new IllegalArgumentException("Unexpected value: " + selectionState);
	}

	@Override
	protected void updateSelection(boolean select) {
		if (InlineList.isEmpty(_vetoListeners)) {
			internalUpdateSelection(select);
		} else {
			try {
				for (Iterator<SelectionVetoListener> it =
					InlineList.iterator(SelectionVetoListener.class, _vetoListeners); it.hasNext();) {
					it.next().checkVeto(getModel(), _node, SelectionType.TOGGLE_SINGLE);
				}
				internalUpdateSelection(select);
			} catch (VetoException ex) {
				ex.setContinuationCommand(new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						updateSelection(select);
						return HandlerResult.DEFAULT_RESULT;
					}
				});
				ex.process(getWindowScope());
			}
		}
	}

	private void internalUpdateSelection(boolean select) {
		SelectionModel selectionModel = getModel();
		if (ScriptingRecorder.isRecordingActive()) {
			ScriptingRecorder.recordSelection(selectionModel, _node, select, SelectionChangeKind.INCREMENTAL);
		}

		selectionModel.setSelected(_node, select);
	}

	@Override
	public void handleStateChanged(StateChanged<N> event) {
		TriState formerState = event.formerState(_node);
		TriState state = event.state(_node);
		if (state != formerState) {
			invalidate();
		}
	}
}
