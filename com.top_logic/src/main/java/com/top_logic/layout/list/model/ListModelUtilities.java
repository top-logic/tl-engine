/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.form.model.ListField;

/**
 * Service methods for easing the use and the implementation of the
 * {@link ListModel} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListModelUtilities {

	/**
	 * Returns the items of the given {@link ListModel} as {@link Set}.
	 */
	public static Set<Object> asSet(ListModel listModel) {
		HashSet<Object> result = new HashSet<>(listModel.getSize());
		for (int cnt = listModel.getSize(), n = 0; n < cnt; n++) {
			result.add(listModel.getElementAt(n));
		}
		return result;
	}

	/**
	 * Returns the items of the given {@link ListModel} as {@link Set}.
	 */
	public static <T> Set<T> asSet(Class<T> expectedType, ListModel listModel) {
		HashSet<T> result = new HashSet<>(listModel.getSize());
		for (int cnt = listModel.getSize(), n = 0; n < cnt; n++) {
			result.add(CollectionUtil.dynamicCast(expectedType, listModel.getElementAt(n)));
		}
		return result;
	}

	/**
	 * Returns the items of the given {@link ListModel} as {@link List}.
	 */
	public static <T> List<T> asList(Class<T> expectedType, ListModel listModel) {
		ArrayList<T> result = new ArrayList<>(listModel.getSize());
		for (int cnt = listModel.getSize(), n = 0; n < cnt; n++) {
			result.add(CollectionUtil.dynamicCast(expectedType, listModel.getElementAt(n)));
		}
		return result;
	}

	/**
	 * Returns the items of the given {@link ListModel} as {@link List}.
	 */
	public static List<Object> asList(ListModel listModel) {
		ArrayList<Object> result = new ArrayList<>(listModel.getSize());
		for (int cnt = listModel.getSize(), n = 0; n < cnt; n++) {
			result.add(listModel.getElementAt(n));
		}
		return result;
	}

	/**
	 * Workaround for missing <code>addAll(Collection)</code> in
	 * {@link DefaultListModel}.
	 * 
	 * @param listModel
	 *     The model to add elements to.
	 * @param source
	 *     List of elements to add to the given model.
	 */
	public static void addAll(DefaultListModel listModel, List<?> source) {
		for (int cnt = source.size(), n = 0; n < cnt; n++) {
			listModel.addElement(source.get(n));
		}
	}
	
	/**
	 * Inserts all elements from the given source list to the target model at the given index.
	 */
	public static void insertAll(DefaultListModel target, int index, List<?> source) {
		for (int cnt = source.size(), n = 0; n < cnt; n++) {
			target.add(index + n, source.get(n));
		}
	}

	/**
	 * Replaces all entries in the given {@link ListModel} with the given list
	 * of values.
	 */
	public static void replaceAll(DefaultListModel listModel, List<?> newValues) {
		listModel.clear();
		addAll(listModel, newValues);
	}


	/**
	 * Notifies all listeners of a {@link ListModel} about a complete update of
	 * the {@link ListModel}'s data.
	 * 
	 * <p>
	 * Access to the internal event dispatching methods of the {@link ListModel}
	 * is provided by the {@link ListEventHandlingAccessor}.
	 * </p>
	 */
	public static void fireCompleteUpdate(Object sender, ListEventHandlingAccessor accessor, int sizeBefore, int newSize) {
		int internalChangedCount = Math.min(newSize, sizeBefore);
		if (internalChangedCount > 0) {
			accessor.fireContentsChanged(sender, 0, internalChangedCount - 1);
		}
		
		if (newSize > sizeBefore) {
			// The list size did increase.
			accessor.fireIntervalAdded(sender, sizeBefore, newSize - 1);
		}
		else if (newSize < sizeBefore) {
			// The list dis shrink.
			accessor.fireIntervalRemoved(sender, newSize, sizeBefore - 1);
		}
	}

	/**
	 * Moves (and compacts) the selection in the given {@link ListField} by the given amount.
	 * 
	 * @param list
	 *        The {@link ListField} to modify.
	 * @param delta
	 *        The amount to move the selection down (negative values for moving up).
	 */
	public static void moveSelection(ListField list, int delta) {
		Set<?> selectedSet = list.getSelectionSet();
		if (selectedSet.isEmpty()) {
			return;
		}

		DefaultListModel model = (DefaultListModel) list.getListModel();

		List<?> selectedList = list.getSelectionList();

		int index = getInsertIndexAfterRemove(model, selectedSet, delta);
		removeElements(model, selectedSet);
		insertAll(model, index, selectedList);

		list.getSelectionModel().setSelectionInterval(index, index + selectedList.size() - 1);
	}

	private static int getInsertIndexAfterRemove(DefaultListModel model, Set<?> selectedSet, int delta) {
		boolean moveUp = delta < 0;
		if (moveUp) {
			return Math.max(0,
				getFirstIndex(model, selectedSet) + delta);
		} else {
			int selectedSize = selectedSet.size();
			return Math.min(model.getSize() - selectedSize,
				getLastIndex(model, selectedSet) - (selectedSize - 1) + delta);
		}
	}

	private static int getFirstIndex(DefaultListModel model, Set<?> selectedSet) {
		int index;
		int selectionSize = model.getSize();
		index = 0;
		for (int cnt = selectionSize; index < cnt; index++) {
			if (selectedSet.contains(model.getElementAt(index))) {
				break;
			}
		}
		return index;
	}

	private static int getLastIndex(DefaultListModel model, Set<?> selectedSet) {
		int index;
		for (index = model.getSize() - 1; index >= 0; index--) {
			if (selectedSet.contains(model.getElementAt(index))) {
				break;
			}
		}
		return index;
	}

	/**
	 * removes from the model the elements in the given {@link Collection}. If
	 * the model contains some element in the collection twice, only the first
	 * one will be removed.
	 */
	public static void removeElements(DefaultListModel model, Collection<?> elements) {
		Set<?> deltaSet = CollectionUtil.toSet(elements);
		for (int n = model.getSize() - 1; n >= 0; n--) {
			if (deltaSet.contains(model.get(n))) {
				model.remove(n);
			}
		}
	}

	/**
	 * Determines all indexes of the given model which are selected
	 */
	public static List<Integer> getSelectedIndexes(ListSelectionModel selectionModel) {
		ArrayList<Integer> selection = new ArrayList<>();
		if (!selectionModel.isSelectionEmpty()) {
			for (int i = selectionModel.getMinSelectionIndex(), end = selectionModel.getMaxSelectionIndex(); i <= end; i++) {
				if (selectionModel.isSelectedIndex(i)) {
					selection.add(i);
				}
			}
		}
		return selection;
	}

}
