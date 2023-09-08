/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.SimpleProxyConstraint;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.list.model.ListModelUtilities;

/**
 * Constraint checking that the list of selected elements in the given {@link ListField} fulfills
 * the given constraint.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class SelectionCheckingConstraint extends SimpleProxyConstraint {

	private final ListField _listfield;

	SelectionCheckingConstraint(Constraint c, ListField listfield) {
		super(c);
		_listfield = listfield;
	}

	@Override
	public boolean check(Object value) throws CheckException {
		return super.check(getValueToCheck());
	}

	private Object getValueToCheck() {
		List<Integer> selectedIndexes = getSelectedIndexes();
		List<Object> selectedObjects = getSelectedObjects(selectedIndexes);
		return selectedObjects;
	}

	private List<Integer> getSelectedIndexes() {
		ListSelectionModel selectionModel = _listfield.getSelectionModel();
		List<Integer> selectedIndexes = ListModelUtilities.getSelectedIndexes(selectionModel);
		return selectedIndexes;
	}

	private List<Object> getSelectedObjects(List<Integer> selectedIndexes) {
		List<Object> selection = new ArrayList<>(selectedIndexes.size());
		ListModel listModel = _listfield.getListModel();
		for (Integer index : selectedIndexes) {
			selection.add(listModel.getElementAt(index));
		}
		return selection;
	}
}
