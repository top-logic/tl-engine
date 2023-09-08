/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.List;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.mig.html.layout.Card;

/**
 * Model for a {@link DeckPaneControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DeckPaneModel extends SingleSelectionModel {

	/**
	 * This method returns an unmodifiable view to the list of selectable cards.
	 */
	List<Card> getSelectableCards();

	/**
	 * This method returns the index of the currently selected object in the list of selectable
	 * objects.
	 * 
	 * @return the index of the currently selected object or -1 if no Object is selected.
	 */
	int getSelectedIndex();

	/**
	 * This method sets the <code>newSelection</code>^th card of the selectable cards as new
	 * selection.
	 * 
	 * @param newSelection
	 *        the index of the card which should be selected. must be >= -1 and < than the size of
	 *        the list given by {@link #getSelectableCards()}. A value <code>-1</code> indicates
	 *        that the current selection shall be deleted.
	 */
	void setSelectedIndex(int newSelection);

	/**
	 * Adds a {@link ModelChangeListener} that is informed if the list of selectable objects change.
	 */
	boolean addModelChangedListener(ModelChangeListener listener);

	/**
	 * Removes the given {@link ModelChangeListener} from the list of listeners.
	 * 
	 * @return <code>true</code> if the listener was among the listeners of this model before.
	 */
	boolean removeModelChangedListener(ModelChangeListener listener);

}
