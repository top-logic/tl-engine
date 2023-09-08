/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;


/**
 * Default implementation of {@link com.top_logic.mig.html.SelectionSetModel}
 * that is organized as array of {@link com.top_logic.mig.html.SelectionModel}s.
 * 
 * This implementation supports selection sets that are composed from single and
 * multiple selection models.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultSelectionSetModel implements SelectionSetModel {
	/**
	 * The array of selection models that this selection set consists of. 
	 */
	SelectionModel[] selectionModels;
	
	/**
	 * Construct a selection set composed of default selection models.
	 * 
	 * @param isMulti the selection at index <code>k</code> of the resulting 
	 *    selection set is capable of storing multiple objects, iff 
	 *    <code>isMulti[k} == true</code>. 
	 */
	public DefaultSelectionSetModel(boolean[] isMulti) {
		int cnt = isMulti.length;
		if ((cnt < 0) || (cnt > SelectionSetModel.MAX_SELECTION_MODEL_INDEX))
			throw new IllegalArgumentException("Number of selections is out of range: " + cnt);
		
		selectionModels = new SelectionModel[cnt];
		for (int n = 0; n < cnt; n++) {
			selectionModels[n] = isMulti[n] ? 
				(SelectionModel) new DefaultMultiSelectionModel(SelectionModelOwner.NO_OWNER) :
				(SelectionModel) new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		}
	}

	/**
	 * Create a selection set consisting of the given selection models. 
	 */
	public DefaultSelectionSetModel(SelectionModel[] models) {
		this.selectionModels = models;
	}

	@Override
	public void setSelection(Object obj, int selectionIndex, boolean select) {
		if ((selectionIndex < 0) || (selectionIndex > selectionModels.length) || (selectionModels[selectionIndex] == null))
			throw new IllegalArgumentException("No such selection: " + selectionIndex);

		selectionModels[selectionIndex].setSelected(obj, select);
	}

	@Override
	public int getSelections(Object obj) {
		int result = 0;
		for (int n = 0; n < selectionModels.length; n++) {
			if (selectionModels[n] == null) continue;
			
			int mask = 1 << n;
			if (selectionModels[n].isSelected(obj)) 
				result |= mask;
		}
		return result;
	}

	@Override
	public SelectionModel getSelectionModel(int selectionIndex) {
		if ((selectionIndex < 0) || (selectionIndex > selectionModels.length) || (selectionModels[selectionIndex] == null))
			throw new IllegalArgumentException("No such selection: " + selectionIndex);
		
		return selectionModels[selectionIndex];
	}

	/**
	 * Add a new selection model or replace an old selection model in this selection set.
	 *   
	 * @param selectionIndex the index of the selection model to add or replace. 
	 * @param model the new selection model.
	 */
	public void setSelectionModel(int selectionIndex, SelectionModel model) {
		if ((selectionIndex < 0) || (selectionIndex > MAX_SELECTION_MODEL_INDEX))
			throw new IllegalArgumentException("Selection index out of range: " + selectionIndex);
		
		if (selectionIndex >= selectionModels.length) {
			// Expand array of selection models to exactly fit the minimum required size.  
			SelectionModel[] newSelectionModels = new SelectionModel[selectionIndex + 1];
			System.arraycopy(selectionModels, 0, newSelectionModels, 0, selectionModels.length);
			selectionModels = newSelectionModels;
		}
		
		selectionModels[selectionIndex] = model;
	}

	@Override
	public void removeAllSelections(Object obj) {
		for (int n = 0; n < selectionModels.length; n++) {
			if (selectionModels[n] == null) continue;
			
			selectionModels[n].setSelected(obj, false);
		}
	}

	@Override
	public void clear(int selectionIndex) {
		if ((selectionIndex < 0) || (selectionIndex > selectionModels.length) || (selectionModels[selectionIndex] == null))
			throw new IllegalArgumentException("No such selection: " + selectionIndex);

		selectionModels[selectionIndex].clear();
	}

	@Override
	public void clear() {
		for (int n = 0; n < selectionModels.length; n++) {
			if (selectionModels[n] == null) continue;
			
			selectionModels[n].clear();
		}
	}
}
