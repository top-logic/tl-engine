/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Set;

import com.top_logic.layout.Control;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link Control} to de- and select all table rows.
 * 
 * <p>
 * The {@link Control} displays a tristate checkbox to indicate the following states:
 * 
 * <ul>
 * <li>Empty box: Selection is empty.</li>
 * <li>Box in indeterminate state (usually represented by a dash in the box): A real non empty
 * subset of rows are selected.</li>
 * <li>Checked box: All rows are selected.</li>
 * </ul>
 * </p>
 * <p>
 * The appearance of the checkbox depends on the browser and may differ.
 * </p>
 * <p>
 * When the user (un-) checks the box, one of the following three actions will be performed:
 * <ul>
 * <li>Empty box: The box will be checked, all rows are selected.</li>
 * <li>Box in indeterminate state: The box will be checked, all rows are selected</li>
 * <li>Checked box: The box will be unchecked, none of the rows are selected.</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TableHeaderSelectionControl extends TriStateCheckboxControl implements SelectionListener {

	private SelectionModel _selectionModel;

	private Set<?> _options;

	/**
	 * Creates a {@link TableHeaderSelectionControl}.
	 */
	public TableHeaderSelectionControl(SelectionModel selectionModel, Set<?> options) {
		super();
		
		_selectionModel = selectionModel;
		_options = options;
	}

	Set<?> options() {
		return _options;
	}

	SelectionModel selectionModel() {
		return _selectionModel;
	}

	@Override
	public Object getModel() {
		return selectionModel();
	}

	@Override
	protected State currentState() {
		Set<?> selection = selectionModel().getSelection();

		if (selection.isEmpty()) {
			return State.UNCHECKED;
		} else if (selection.containsAll(options())) {
			return State.CHECKED;
		} else {
			return State.INDETERMINATE;
		}
	}

	@Override
	protected void updateSelection(boolean select) {
		SelectionModel selectionModel = selectionModel();
		if (select) {
			Set<?> options = options();
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordSelection(selectionModel, options, true, SelectionChangeKind.ABSOLUTE);
			}
			selectionModel.setSelection(options);
		} else {
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordSelection(selectionModel, ScriptingRecorder.NO_SELECTION, false,
					SelectionChangeKind.ABSOLUTE);
			}
			selectionModel.clear();
		}
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		selectionModel().addSelectionListener(this);
	}

	@Override
	protected void internalDetach() {
		selectionModel().removeSelectionListener(this);

		super.internalDetach();
	}

	@Override
	public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
		invalidate();
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		HTMLUtil.appendCSSClass(out, "tl-table__cell-checkbox");
		super.writeControlClassesContent(out);
	}

}
