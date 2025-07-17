/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.util.Utils;

/**
 * A selection model that only supports selecting single objects. 
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultSingleSelectionModel<T> extends AbstractRestrainedSelectionModel<T>
		implements SingleSelectionModel<T> {

	private T _selected;

	/**
	 * Create a new {@link DefaultSingleSelectionModel}.
	 */
	public DefaultSingleSelectionModel(SelectionModelOwner owner) {
		this(null, owner);
	}
	
	/**
	 * Creates a {@link DefaultSingleSelectionModel}.
	 * 
	 * @param selectionFilter
	 *        The {@link Filter filter}, which defines, whether an object is selectable.
	 */
	public DefaultSingleSelectionModel(Filter<? super T> selectionFilter, SelectionModelOwner owner) {
		super(owner);
		setSelectionFilter(selectionFilter);
	}

	@Override
	public boolean isMultiSelectionSupported() {
		return false;
	}

	@Override
	public boolean isSelectable(T obj) {
		return super.isSelectable(obj)
			// The current selection can be removed.
			&& (_selected == null || isDeselectable(_selected));
	}
	
	/**
	 * A setter for a selection filter
	 * 
	 * @param selectionFilter - the {@link Filter filter}, which defines, if an object is selectable, or not.
	 * If the filter is null, then every object is selectable.
	 */
	@Override
	public void setSelectionFilter(Filter<? super T> selectionFilter) {
		Filter<? super T> newFilter = nonNull(selectionFilter);

		T currentSelection = _selected;
		if ((currentSelection != null) && (!newFilter.accept(currentSelection))) {
			internalSetSelected(null);
		}

		super.setSelectionFilter(newFilter);
	}

	@Override
	public boolean isSelected(T obj) {
		return (_selected != null) && _selected.equals(obj);
	}

	@Override
	public void setSelected(T touchedObject, boolean select) {
		if (touchedObject == null) {
			throw new IllegalArgumentException("Selected object may not be null.");
		}
		
		if (isSelectionFixed()) {
			return;
		}

		if (select) {
			if (touchedObject.equals(_selected)) {
				// touchedObject is already the selected item
				return;
			}
			if (!isSelectable(touchedObject)) {
				// No valid new selection.

				// Note: Evaluating the selection filter must only happen on objects that are about
				// to be selected, since de-selection naturally occurs on deleted objects.
				return;
			}

			internalSetSelected(touchedObject);
		} else {
			if (touchedObject.equals(_selected)) {
				internalSetSelected(null);
			}
		}
	}

	@Override
	public void removeFromSelection(Collection<? extends T> objects) {
		if (_selected == null) {
			// Nothing selected
			return;
		}
		if (objects.contains(_selected)) {
			setSelected(_selected, false);
		}
	}

	@Override
	public void addToSelection(Collection<? extends T> objects) {
		switch (objects.size()) {
			case 0:
				break;
			case 1:
				if (_selected == null) {
					setSelected(objects.iterator().next(), true);
				} else if (_selected.equals(objects.iterator().next())) {
					// already selected.
					return;
				} else {
					throw new IllegalArgumentException(
						"Multiple selection not supported in a single selection model: " + objects);
				}
				break;
			default:
				throw new IllegalArgumentException(
					"Multiple selection not supported in a single selection model: " + objects);
		}
	}

	private boolean isSelectionFixed() {
		return _selected != null ? !isDeselectable(_selected) : false;
	}

	/**
	 * Set the given new selection without any further tests.
	 * 
	 * @param newSelection
	 *        the new selection to set. <code>null</code> means no selection.
	 */
	private void internalSetSelected(T newSelection) {
		Set<? extends T> formerlySelected = getSelection();
		_selected = newSelection;
		fireSelectionChanged(formerlySelected);
	}
	
	/**
	 * @see com.top_logic.layout.SingleSelectionModel#setSingleSelection(java.lang.Object)
	 */
	@Override
	public final void setSingleSelection(T obj) {
		if (obj == null) {
			clear();
		} else {
			setSelected(obj, true);
		}
	}

	@Override
	public void setSelection(Set<? extends T> newSelection) {
		if (newSelection == null) {
			throw new IllegalArgumentException("The selection set must not be null.");
		}
		switch (newSelection.size()) {
			case 0:
				clear();
				break;
			case 1:
				setSelected(newSelection.iterator().next(), true);
				break;
			default:
				throw new IllegalArgumentException(
					"Multiple selection not supported in a single selection model: " + newSelection);
		}

	}

	@Override
	public void clear() {
		if (_selected == null || isSelectionFixed()) {
			return;
		}
		internalSetSelected(null);
	}

	/**
	 * @see com.top_logic.layout.SingleSelectionModel#getSingleSelection()
	 */
	@Override
	public T getSingleSelection() {
		return _selected;
	}

	@Override
	public Set<? extends T> getSelection() {
		if (_selected != null) {
			return Collections.singleton(_selected);
		}
		else {
			return Collections.emptySet();
		}
	}
	
	/**
	 * @see com.top_logic.layout.SingleSelectionModel#addSingleSelectionListener(com.top_logic.layout.component.model.SingleSelectionListener)
	 */
	@Override
	public boolean addSingleSelectionListener(SingleSelectionListener listener) {
		return addSelectionListener(new SelectionListenerAdaptar(listener));
	}

	/**
	 * @see com.top_logic.layout.SingleSelectionModel#removeSingleSelectionListener(com.top_logic.layout.component.model.SingleSelectionListener)
	 */
	@Override
	public boolean removeSingleSelectionListener(SingleSelectionListener listener) {
		return removeSelectionListener(new SelectionListenerAdaptar(listener));
	}
	
	/**
	 * The class {@link DefaultSingleSelectionModel.SelectionListenerAdaptar} is an adapter class to use
	 * {@link SingleSelectionListener} as {@link SelectionListener}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static final class SelectionListenerAdaptar implements SelectionListener {

		private final SingleSelectionListener listener;

		public SelectionListenerAdaptar(SingleSelectionListener listener) {
			this.listener = listener;
		}

		@Override
		public void notifySelectionChanged(SelectionModel model, SelectionEvent event) {
			listener.notifySelectionChanged((DefaultSingleSelectionModel) model,
				CollectionUtil.getFirst(event.getOldSelection()), CollectionUtil
					.getFirst(event.getNewSelection()));
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof SelectionListenerAdaptar)) {
				return false;
			}
			return Utils.equals(listener, ((SelectionListenerAdaptar) obj).listener);
		}
		
		@Override
		public int hashCode() {
			return listener.hashCode();
		}

	}

}
