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
public class DefaultMultiSelectionModel<T> extends AbstractRestrainedSelectionModel<T> {
	
	private static final Object NO_LEAD = new Object();

	private Object _lastSelected = NO_LEAD;

	private final Set<T> selected = new HashSet<>();

	private final Set<T> selectedView = Collections.unmodifiableSet(selected);
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
	public DefaultMultiSelectionModel(Filter<? super T> selectionFilter, SelectionModelOwner owner) {
		super(owner);
		setSelectionFilter(selectionFilter);
		setDeselectionFilter(FilterFactory.trueFilter());
	}
	
	@Override
	public boolean isSelectable(T obj) {
		return getSelectionFilter().accept(obj);
	}
	
	/**
	 * true, if the given object can be removed from set of selected objects.
	 */
	public boolean isDeselectable(T obj) {
		return getDeselectionFilter().accept(obj);
	}

	@Override
	public boolean isSelected(T obj) {
		return selected.contains(obj);
	}	
	
	@Override
	public void setSelected(T obj, boolean select) {
		if (obj == null) {
			throw new IllegalArgumentException("Selected object may not be null.");
		}

		
		if (select) {
			if (!isSelectable(obj)) {
				return;
			}
			if (! selected.contains(obj)) {
				HashSet<T> oldSelection = new HashSet<>(selected);
				selected.add(obj);
				setLastSelected(obj);
				fireSelectionChanged(oldSelection);
			}
		} else {
			if (!isDeselectable(obj)) {
				return;
			}
			if (selected.contains(obj)) {
				HashSet<T> oldSelection = new HashSet<>(selected);
				selected.remove(obj);
				setLastSelected(obj);
				fireSelectionChanged(oldSelection);
			}
		}
	}

	@Override
	public void addToSelection(Collection<? extends T> objects) {
		switch (objects.size()) {
			case 0:
				return;
			case 1:
				setSelected(objects.iterator().next(), true);
				return;
			default:
				Iterator<? extends T> it = objects.iterator();
				T tmp = null;
				do {
					T next = it.next();
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
				HashSet<T> oldSelection = new HashSet<>(selected);
				selected.add(tmp);
				setLastSelected(tmp);
				while (it.hasNext()) {
					T next = it.next();
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
	public void removeFromSelection(Collection<? extends T> objects) {
		switch (objects.size()) {
			case 0:
				return;
			case 1:
				setSelected(objects.iterator().next(), false);
				return;
			default:
				Iterator<? extends T> it = objects.iterator();
				T tmp = null;
				do {
					T next = it.next();
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
				HashSet<T> oldSelection = new HashSet<>(selected);
				selected.remove(tmp);
				setLastSelected(tmp);
				while (it.hasNext()) {
					T next = it.next();
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

	@Override
	public void clear() {
		if (isNonEmptySelection()) {
			HashSet<T> oldSelection = new HashSet<>(selected);
			clearSelectableItems();
			fireSelectionChanged(oldSelection);
		}
	}

	private boolean isNonEmptySelection() {
		Set<?> fixedSelection = getFixedSelection();
		return !CollectionUtil.containsSame(fixedSelection, selected);
	}

	private void clearSelectableItems() {
		Set<?> fixedSelection = getFixedSelection();
		selected.retainAll(fixedSelection);
	}

	private Set<T> getFixedSelection() {
		HashSet<T> fixedSelection = new HashSet<>();
		for (T item : selected) {
			if (!getDeselectionFilter().accept(item)) {
				fixedSelection.add(item);
			}
		}
		return fixedSelection;
	}

	/**
	 * Same as {@link #setSelected(Object, boolean)}, but without specifying a lead object.
	 * 
	 * @see com.top_logic.mig.html.SelectionModel#setSelection(Set)
	 */
	@Override
	public void setSelection(Set<? extends T> newSelection) {
		setSelection(newSelection, NO_LEAD);
	}

	/**
	 * Sets the new overall selection, whereby lead object specifies the selection item, that shall
	 * mark the most important part (e.g. scrolling to according table row).
	 */
	public void setSelection(Set<? extends T> newSelection, Object leadObject) {
		if (newSelection == null) {
			throw new IllegalArgumentException("Selection must not be null");
		}
		HashSet<? extends T> oldSelection = new HashSet<>(selected);
		Set<T> retainedSelection = getFixedSelection();
		retainedSelection.addAll(newSelection);
		boolean modified = selected.retainAll(retainedSelection);
		for (T newSelected : newSelection) {
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

	@Override
	public Set<? extends T> getSelection() {
		return selectedView;
	}

	/**
	 * object, that has been (de-)selected at last time of modification of this selection model.
	 * 
	 * <p>
	 * Returns null if the selection has no lead object.
	 * </p>
	 */
	public Object getLastSelected() {
		if (_lastSelected == NO_LEAD) {
			return null;
		}

		return _lastSelected;
	}

	private void setLastSelected(Object lastSelected) {
		_lastSelected = lastSelected;
	}

}
