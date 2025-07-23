/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;

/**
 * A selection model that supports multiple selections.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultMultiSelectionModel extends AbstractMultiSelectionModel {
	
	private final Set selected = new HashSet();
	private final Set selectedView = Collections.unmodifiableSet(selected);
	/**
	 * Create a new DefaultMultiSelectionModel, which allows to select all objects
	 *
	 */
	public DefaultMultiSelectionModel(SelectionModelOwner owner) {
		this(null, owner);
	}
	
	/**
	 * Create a new DefaultMultiSelectionModel, with a subset of non selectable objects
	 * 
	 * @param selectionFilter - the {@link Filter filter}, which defines, if an object is selectable, or not
	 */
	public DefaultMultiSelectionModel(Filter selectionFilter, SelectionModelOwner owner) {
		super(owner);
		setSelectionFilter(selectionFilter);
		setDeselectionFilter(FilterFactory.trueFilter());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSelected(Object obj) {
		return selected.contains(obj);
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSelected(Object obj, boolean select) {
		if (obj == null) {
			throw new IllegalArgumentException("Selected object may not be null.");
		}

		if (select) {
			if (selected.contains(obj)) {
				return;
			}
			if (!isSelectable(obj)) {
				return;
			}
			HashSet oldSelection = new HashSet(selected);
			selected.add(obj);
			setLastSelected(obj);
			fireSelectionChanged(oldSelection);
		} else {
			if (!isDeselectable(obj)) {
				return;
			}
			if (selected.contains(obj)) {
				HashSet oldSelection = new HashSet(selected);
				selected.remove(obj);
				setLastSelected(obj);
				fireSelectionChanged(oldSelection);
			}
		}
	}

	@Override
	public void addToSelection(Collection<?> objects) {
		switch (objects.size()) {
			case 0:
				return;
			case 1:
				setSelected(objects.iterator().next(), true);
				return;
			default:
				Iterator<?> it = objects.iterator();
				Object tmp = null;
				do {
					Object next = it.next();
					if (next == null) {
						continue;
					}
					if (!isSelectable(next)) {
						continue;
					}
					if (selected.contains(next)) {
						continue;
					}
					tmp = next;
					break;
				} while (it.hasNext());
				if (tmp == null) {
					return;
				}
				HashSet oldSelection = new HashSet(selected);
				selected.add(tmp);
				setLastSelected(tmp);
				while (it.hasNext()) {
					Object next = it.next();
					if (next == null) {
						continue;
					}
					if (!isSelectable(next)) {
						continue;
					}
					selected.add(next);
					setLastSelected(next);
				}
				fireSelectionChanged(oldSelection);
		}
	}

	@Override
	public void removeFromSelection(Collection<?> objects) {
		switch (objects.size()) {
			case 0:
				return;
			case 1:
				setSelected(objects.iterator().next(), false);
				return;
			default:
				Iterator<?> it = objects.iterator();
				Object tmp = null;
				do {
					Object next = it.next();
					if (next == null) {
						continue;
					}
					if (!isDeselectable(next)) {
						continue;
					}
					if (!selected.contains(next)) {
						continue;
					}
					tmp = next;
					break;
				} while (it.hasNext());
				if (tmp == null) {
					return;
				}
				HashSet oldSelection = new HashSet(selected);
				selected.remove(tmp);
				setLastSelected(tmp);
				while (it.hasNext()) {
					Object next = it.next();
					if (next == null) {
						continue;
					}
					if (!isDeselectable(next)) {
						continue;
					}
					selected.remove(next);
					setLastSelected(next);
				}
				fireSelectionChanged(oldSelection);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		if (isNonEmptySelection()) {
			HashSet<Object> oldSelection = new HashSet<>(selected);
			clearSelectableItems();
			fireSelectionChanged(oldSelection);
		}
	}

	private boolean isNonEmptySelection() {
		Set<Object> fixedSelection = getFixedSelection();
		return !CollectionUtil.containsSame(fixedSelection, selected);
	}

	@SuppressWarnings("unchecked")
	private void clearSelectableItems() {
		Set<Object> fixedSelection = getFixedSelection();
		selected.retainAll(fixedSelection);
	}

	private Set<Object> getFixedSelection() {
		if (FilterFactory.isTrue(getDeselectionFilter())) {
			return Collections.emptySet();
		}
		HashSet<Object> fixedSelection = new HashSet<>();
		for (Object item : selected) {
			if (!isDeselectable(item)) {
				fixedSelection.add(item);
			}
		}
		return fixedSelection;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setSelection(Set<?> newSelection, Object leadObject) {
		if (newSelection == null) {
			throw new IllegalArgumentException("Selection must not be null");
		}
		HashSet<Object> oldSelection = new HashSet<>(selected);
		Set<Object> fixedSelection = getFixedSelection();
		Set<?> retainedSelection;
		if (fixedSelection.isEmpty()) {
			retainedSelection = newSelection;
		} else {
			fixedSelection.addAll(newSelection);
			retainedSelection = fixedSelection;
		}
		boolean modified = selected.retainAll(retainedSelection);
		for (Object newSelected : newSelection) {
			if (isSelectable(newSelected)) {
				boolean added = selected.add(newSelected);
				modified |= added;
			}
		}
		if (modified) {
			setLastSelected(leadObject);
			fireSelectionChanged(oldSelection);
		}
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public Set<?> getSelection() {
		return selectedView;
	}

}
