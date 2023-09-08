/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.Drop.DropException;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.list.DropAcceptor;
import com.top_logic.layout.list.ListControl;

/**
 * {@link DropAcceptor} to receive some drag and drop operation between the {@link ListControl}
 * representing the current selection of a pop up select and the {@link ListControl} representing
 * the options.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class SelectionDropAccessor implements DropAcceptor<Object> {

	private final SelectField _targetField;

	public SelectionDropAccessor(SelectField targetField) {
		_targetField = targetField;
	}

	@Override
	public boolean accept(List<? extends Object> businessObjects, final ListModel listModel, ListSelectionModel selectionModel, int index)
			throws DropException {
		Comparator comp = _targetField.getOptionComparator();

		if (businessObjects.isEmpty()) {
			return true;
		}
		final DefaultListModel defListModel = (DefaultListModel) listModel;

		final Object[] copiedBusinessObjects = checkMovedObjects(businessObjects, _targetField.getFixedOptions());
		boolean businessObjectsOrdered = sortObjects(copiedBusinessObjects, comp);

		final int businessObjectSize = businessObjects.size();
		// just for optimization
		defListModel.ensureCapacity(defListModel.getSize() + businessObjectSize);
		if (defListModel.getSize() == 0) {
			for (Object businessObject : copiedBusinessObjects) {
				defListModel.addElement(businessObject);
			}
			selectionModel.setSelectionInterval(0, copiedBusinessObjects.length - 1);
			assert index == 0 : "ListModel is empty, but the new objects are inserted at index " + index + " (!=0)";
			return businessObjectsOrdered;
		} else {
			boolean inserted = checkBorderCases(index, comp, defListModel, selectionModel, copiedBusinessObjects);
			if (inserted) {
				return businessObjectsOrdered;
			}

			int expectedInsertIndex = index;
			boolean orderCorrect = businessObjectsOrdered;

			int i = 0;
			Object currentNewObject = copiedBusinessObjects[i];

			int j;
			if (index > 0 && comp.compare(defListModel.getElementAt(index - 1), currentNewObject) < 0) {
				j = index - 1;
			} else {
				j = 0;
			}
			Object currentLMObject = defListModel.getElementAt(j);

			while (true) {
				if (comp.compare(currentLMObject, currentNewObject) < 0) {
					j++;
					// can not cache size as list model is adopted
					if (j == defListModel.getSize()) {
						// The new objects are not inserted at the end of the list model
						// (index < listModelSize) but some new object is greater then last
						// element in list model
						orderCorrect = false;
						while (i < businessObjectSize) {
							defListModel.addElement(copiedBusinessObjects[i]);
							i++;
						}
						break;
					}
					currentLMObject = defListModel.getElementAt(j);
				} else {
					if (expectedInsertIndex != j) {
						orderCorrect = false;
					}
					defListModel.add(j, currentNewObject);
					i++;
					j++;
					expectedInsertIndex++;
					if (i == businessObjectSize) {
						break;
					}
					currentNewObject = copiedBusinessObjects[i];
				}
			}
			selectionModel.clearSelection();
			HashSet<Object> selectedObjects = new HashSet<>(Arrays.asList(copiedBusinessObjects));
			for (int listIndex = 0; listIndex < listModel.getSize(); listIndex++) {
				if (selectedObjects.contains(listModel.getElementAt(listIndex))) {
					selectionModel.addSelectionInterval(listIndex, listIndex);
				}

			}
			return orderCorrect;
		}
	}

	private Object[] checkMovedObjects(List<? extends Object> movedObjects,
			final Filter<? super Object> fixedOptions)
			throws DropException {
		final Object[] copiedBusinessObjects = new Object[movedObjects.size()];
		if (fixedOptions == null || fixedOptions == FilterFactory.falseFilter()) {
			// no fixed options
			movedObjects.toArray(copiedBusinessObjects);
		} else {
			int i = 0;
			for (Object newObject : movedObjects) {
				if (fixedOptions.accept(newObject)) {
					throw new DropException("Object " + newObject + " is fixed and can not be moved.");
				}
				copiedBusinessObjects[i++] = newObject;

			}
		}
		return copiedBusinessObjects;
	}

	private boolean checkBorderCases(int index, Comparator<Object> comp, final DefaultListModel listModel,
			ListSelectionModel selectionModel, final Object[] copiedBusinessObjects) {
		int indexOfLastNewElem = copiedBusinessObjects.length - 1;
		// Try insert at start of list model
		if (index == 0
			&& comp.compare(copiedBusinessObjects[indexOfLastNewElem], listModel.getElementAt(0)) < 0) {
			// all new elements are smaller then all in the list model
			for (int i = indexOfLastNewElem; i >= 0; i--) {
				listModel.add(0, copiedBusinessObjects[i]);
			}
			selectionModel.setSelectionInterval(0, indexOfLastNewElem);
			return true;
		}

		final int listModelSize = listModel.size();
		// Try insert at end of list model
		if (index == listModelSize
			&& comp.compare(listModel.getElementAt(listModelSize - 1), copiedBusinessObjects[0]) < 0) {
			// all new elements are greater then all in the list model
			for (Object businessObject : copiedBusinessObjects) {
				listModel.addElement(businessObject);
			}
			selectionModel.setSelectionInterval(listModelSize, listModelSize + indexOfLastNewElem);
			return true;
		}
		return false;
	}

	private <T> boolean sortObjects(final T[] objects, Comparator<? super T> comp) {
		int numberObjects = objects.length;
		if (numberObjects == 0) {
			return true;
		}
		boolean alreadyOrdered = true;
		for (int i = 1; i < numberObjects; i++) {
			if (!(comp.compare(objects[i - 1], objects[i]) < 0)) {
				alreadyOrdered = false;
				break;
			}
		}
		if (!alreadyOrdered) {
			Arrays.sort(objects, 0, objects.length, comp);
		}
		return alreadyOrdered;
	}
}
