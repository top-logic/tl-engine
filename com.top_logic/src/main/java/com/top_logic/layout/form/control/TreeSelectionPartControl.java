/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.TreeSelectionModel;
import com.top_logic.mig.html.TreeSelectionModel.NodeSelectionState;

/**
 * {@link SelectionPartControl} that displays special states of a {@link TreeSelectionModel}.
 */
public class TreeSelectionPartControl extends SelectionPartControl {

	/**
	 * Creates a {@link TreeSelectionPartControl}.
	 */
	public TreeSelectionPartControl(TreeSelectionModel<?> treeSelection, Object part) {
		super(treeSelection, part);
	}

	@Override
	protected String getTypeCssClass() {
		return "tl-radio-checkbox-container tl-treeSelect";
	}

	@Override
	protected void writeInputCssClassesContent(TagWriter out) throws IOException {
		super.writeInputCssClassesContent(out);

		NodeSelectionState state = selectionState();
		out.write(switch (state.descendants()) {
			case ALL -> "tl-treeSelect__allChildren";
			case SOME -> "tl-treeSelect__someChildren";
			case NONE -> "tl-treeSelect__noChildren";
		});
	}

	@Override
	public void updateSelection(boolean selected) {
		boolean doSelect;
		SelectionChangeKind kind;
		NodeSelectionState state = selectionState();
		if (state.descendants().isHomogeneous()) {
			if (state == NodeSelectionState.ALL_DESCENDANTS) {
				// Deselect all.
				selectionModel().setSelectedSubtree(getSelectionPart(), doSelect = false);
				kind = SelectionChangeKind.SUBTREE;
			} else if (state == NodeSelectionState.NONE) {
				// Only select node itself
				selectionModel().setSelected(getSelectionPart(), doSelect = true);
				kind = SelectionChangeKind.INCREMENTAL;
			} else if (state == NodeSelectionState.FULL) {
				// Only select node itself
				selectionModel().setSelected(getSelectionPart(), doSelect = false);
				kind = SelectionChangeKind.INCREMENTAL;
			} else {
				// Select all.
				selectionModel().setSelectedSubtree(getSelectionPart(), doSelect = true);
				kind = SelectionChangeKind.SUBTREE;
			}
		} else {
			selectionModel().setSelected(getSelectionPart(), doSelect = !state.isSelected());
			kind = SelectionChangeKind.INCREMENTAL;
		}

		if (ScriptingRecorder.isRecordingActive()) {
			ScriptingRecorder.recordSelection(getSelectionModel(), getSelectionPart(), doSelect, kind);
		}
	}

	private NodeSelectionState selectionState() {
		return selectionModel().getNodeSelectionState(getSelectionPart());
	}

	private TreeSelectionModel selectionModel() {
		return (TreeSelectionModel) getSelectionModel();
	}

}
