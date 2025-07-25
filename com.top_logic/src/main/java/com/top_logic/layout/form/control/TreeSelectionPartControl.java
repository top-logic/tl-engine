/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
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
		NodeSelectionState state = selectionState();
		if (state.descendants().isHomogeneous()) {
			if (state == NodeSelectionState.FULL) {
				// Deselect all.
				selectionModel().setSelectedSubtree(getSelectionPart(), false);
			} else if (state == NodeSelectionState.NONE) {
				// Only select node itself
				selectionModel().setSelected(getSelectionPart(), true);
			} else {
				// Select all.
				selectionModel().setSelectedSubtree(getSelectionPart(), true);
			}
		} else {
			selectionModel().setSelected(getSelectionPart(), !state.isSelected());
		}
	}

	private NodeSelectionState selectionState() {
		return selectionModel().getNodeSelectionState(getSelectionPart());
	}

	private TreeSelectionModel selectionModel() {
		return (TreeSelectionModel) getSelectionModel();
	}

}
