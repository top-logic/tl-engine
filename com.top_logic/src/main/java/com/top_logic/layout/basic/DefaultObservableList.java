/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.layout.component.model.ModelChangeObservable;

/**
 * The class {@link DefaultObservableList} is an implementation of {@link ObservableList} which
 * dispatches all {@link List} methods to an internal storage.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultObservableList extends ModelChangeObservable implements ObservableList {

	/** the list this ObservableList based on. All {@link List} methods are dispatched to this list. */
	private final List storage = new ArrayList();

	/** a view to the storage */
	private final List unmodifiableView = Collections.unmodifiableList(storage);

	/**
	 * This method returns a view to the list this {@link ObservableList} based on.
	 * 
	 * @return a view to the list this {@link ObservableList} based on.
	 */
	public List getModel() {
		return unmodifiableView;
	}

	@Override
	public void addModelChangedListener(ModelChangeListener listener) {
		addListener(listener);
	}

	@Override
	public void removeModelChangedListener(ModelChangeListener listener) {
		removeListener(listener);
	}

	/**
	 * This method informs all {@link ModelChangeListener} about a change of the list. Must be
	 * called <b>after</b> all changes of the list occurred.
	 * 
	 * @param oldList
	 *            the list before the change occurs.
	 */
	protected final void fireModelChanged(List oldList) {
		notifyListeners(this, oldList, getModel());
	}

	/**
	 * This method returns a new {@link List} containing the same elements the list this
	 * {@link ObservableList} is based on.
	 * 
	 * @see #storage
	 */
	private List cloneModel() {
		return new ArrayList(storage);
	}

	// List implementation

	@Override
	public boolean add(Object o) {
		List oldList = cloneModel();
		boolean succeded = storage.add(o);
		if (succeded) {
			fireModelChanged(oldList);
		}
		return succeded;
	}

	@Override
	public void add(int index, Object element) {
		List oldList = cloneModel();
		storage.add(index, element);
		fireModelChanged(oldList);
	}

	@Override
	public boolean addAll(Collection c) {
		List oldList = cloneModel();
		boolean succeded = storage.addAll(c);
		if (succeded) {
			fireModelChanged(oldList);
		}
		return succeded;
	}

	@Override
	public boolean addAll(int index, Collection c) {
		List oldList = cloneModel();
		boolean succeded = storage.addAll(index, c);
		if (succeded) {
			fireModelChanged(oldList);
		}
		return succeded;
	}

	@Override
	public void clear() {
		List oldList = cloneModel();
		storage.clear();
		fireModelChanged(oldList);
	}

	@Override
	public boolean contains(Object o) {
		return storage.contains(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		return storage.containsAll(c);
	}

	@Override
	public Object get(int index) {
		return storage.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return storage.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return storage.isEmpty();
	}

	@Override
	public Iterator iterator() {
		return storage.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return storage.lastIndexOf(o);
	}

	@Override
	public ListIterator listIterator() {
		return storage.listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		return storage.listIterator(index);
	}

	@Override
	public Object remove(int index) {
		List oldList = cloneModel();
		Object removedObj = storage.remove(index);
		fireModelChanged(oldList);
		return removedObj;
	}

	@Override
	public boolean remove(Object o) {
		List oldList = cloneModel();
		boolean succeded = storage.remove(o);
		if (succeded) {
			fireModelChanged(oldList);
		}
		return succeded;
	}

	@Override
	public boolean removeAll(Collection c) {
		List oldList = cloneModel();
		boolean succeded = storage.removeAll(c);
		if (succeded) {
			fireModelChanged(oldList);
		}
		return succeded;
	}

	@Override
	public boolean retainAll(Collection c) {
		List oldList = cloneModel();
		boolean succeded = storage.retainAll(c);
		if (succeded) {
			fireModelChanged(oldList);
		}
		return succeded;
	}

	@Override
	public Object set(int index, Object element) {
		List oldList = cloneModel();
		Object oldObj = storage.set(index, element);
		fireModelChanged(oldList);
		return oldObj;
	}

	@Override
	public int size() {
		return storage.size();
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return storage.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return storage.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return storage.toArray(a);
	}

	@Override
	public boolean equals(Object obj) { // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
		if (obj == this) {
			return true;
		}
		return storage.equals(obj);
	}

	@Override
	public int hashCode() {
		return storage.hashCode();
	}

}
