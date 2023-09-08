/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.layout.basic.Listeners;
import com.top_logic.layout.form.control.DeckPaneModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.util.Utils;

/**
 * The class {@link DefaultDeckPaneModel} is a {@link DefaultSingleSelectionModel} which allows to select
 * from a given {@link List} of not <code>null</code> {@link Card}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultDeckPaneModel extends DefaultSingleSelectionModel implements DeckPaneModel {

	private final Listeners listeners = new Listeners();

	final List<Card> selectableObjects;

	private final List<Card> unmodifiableView;

	/**
	 * Creates a {@link DefaultDeckPaneModel} with an empty list of selectable
	 * objects.
	 */
	public DefaultDeckPaneModel() {
		// No SelectionModelOwner necessary, as selections of cards are recorded differently.
		// (Via TabSelection for example.)
		this(Collections.emptyList());
	}

	/**
	 * Creates a {@link DeckPaneModel}.
	 * 
	 * @param someSelectableObjects
	 *            a list of selectable objects
	 */
	public DefaultDeckPaneModel(List<? extends Card> someSelectableObjects) {
		// See constructor above
		super(SelectionModelOwner.NO_OWNER);
		checkSelectableObjects(someSelectableObjects);
		selectableObjects = new ArrayList<>(someSelectableObjects);
		unmodifiableView = Collections.unmodifiableList(selectableObjects);
	}

	private void checkSelectableObjects(List<? extends Card> someSelectableObjects) {
		if (someSelectableObjects == null) {
			throw new IllegalArgumentException("'someSelectableObjects' must not be 'null'.");
		}
	}

	/**
	 * An object is selectable iff it is contained in the {@link List} given by
	 * {@link #getSelectableCards()}.
	 * 
	 * @see com.top_logic.mig.html.DefaultSingleSelectionModel#isSelectable(java.lang.Object)
	 */
	@Override
	public boolean isSelectable(Object obj) {
		return getSelectableCards().contains(obj);
	}

	/**
	 * This method adds a new card to the list of selectable objects and informs the
	 * {@link ModelChangeListener}s.
	 * 
	 * @param index
	 *        Index in the {@link #getSelectableCards() list of cards } to insert the new card.
	 * @param aCard
	 *        The card to insert.
	 */
	public boolean addSelectableCard(int index, Card aCard) {
		List<Card> oldModel = new ArrayList<>(selectableObjects);
		selectableObjects.add(index, aCard);
		fireModelChangedEvent(oldModel, selectableObjects);
		return true;
	}

	/**
	 * This method adds a new card to the the end of the list of selectable objects and informs the
	 * {@link ModelChangeListener}s.
	 */
	public boolean addSelectableCard(Card aCard) {
		return addSelectableCard(selectableObjects.size(), aCard);
	}

	/**
	 * This method removes the given object from the list of selectable objects and informs the
	 * {@link ModelChangeListener}s.
	 * 
	 * @return <code>true</code> if the list of selectable objects contain <code>anObject</code>.
	 */
	public boolean removeSelectableCard(Card aCard) {
		if (!selectableObjects.contains(aCard)) {
			return false;
		}
		List<Card> oldModel = new ArrayList<>(selectableObjects);
		if (isSelected(aCard)) {
			setSingleSelection(null);
		}
		boolean result = selectableObjects.remove(aCard);
		fireModelChangedEvent(oldModel, selectableObjects);
		return result;
	}

	/**
	 * This method sets the List of selectable objects. If there is a current selection and the
	 * currently selected card is contained in the {@link List} of selectable objects
	 * <code>someSelectableObjects</code> the old selected card is also the new selection.
	 * 
	 * @param someSelectableObjects
	 *            must not be <code>null</code>
	 * @return <code>false</code> iff the given {@link List} is equal to the current selectable
	 *         objects.
	 */
	public boolean setSelectableObjects(List<? extends Card> someSelectableObjects) {
		if (selectableObjects.equals(someSelectableObjects)) {
			return false;
		}
		checkSelectableObjects(someSelectableObjects);
		Object theCurrentSelection = getSingleSelection();
		if (!someSelectableObjects.contains(theCurrentSelection)) {
			setSingleSelection(null);
		}
		List<Card> oldModel = new ArrayList<>(selectableObjects);
		selectableObjects.clear();
		selectableObjects.addAll(someSelectableObjects);
		fireModelChangedEvent(oldModel, selectableObjects);
		return true;
	}

	/**
	 * @see com.top_logic.mig.html.DefaultSingleSelectionModel#setSelected(java.lang.Object,
	 *      boolean)
	 * @throws IllegalArgumentException
	 *             if <code>touchedObject</code> is not selectable.
	 */
	@Override
	public void setSelected(Object touchedObject, boolean select) {
		if (!selectableObjects.contains(touchedObject)) {
			throw new IllegalArgumentException("Just objects from the contentList are selectable.");
		}
		super.setSelected(touchedObject, select);
	}

	@Override
	public List<Card> getSelectableCards() {
		return unmodifiableView;
	}

	@Override
	public void setSelectedIndex(int newSelection) {
		Object theOldSelection = getSingleSelection();
		if (getSelectableCards().indexOf(theOldSelection) == newSelection) {
			return;
		}
		if (newSelection == -1) {
			setSingleSelection(null);
			return;
		}
		Object theNewSelection = selectableObjects.get(newSelection);

		if (!Utils.equals(theNewSelection, theOldSelection)) {
			setSingleSelection(theNewSelection);
		}
	}

	@Override
	public boolean addModelChangedListener(ModelChangeListener aListener) {
		return getListeners().add(ModelChangeListener.class, aListener);
	}

	@Override
	public boolean removeModelChangedListener(ModelChangeListener aListener) {
		return getListeners().remove(ModelChangeListener.class, aListener);
	}

	/**
	 * This method informs all added {@link ModelChangeListener} that the model has changed.
	 * 
	 * @param oldModel
	 *            the model before the change.
	 * @param newModel
	 *            the model after the change.
	 */
	protected void fireModelChangedEvent(Object oldModel, Object newModel) {
		List<ModelChangeListener> modelChangeListeners = getListeners().get(ModelChangeListener.class);
		for (int index = 0, size = modelChangeListeners.size(); index < size; index++) {
			modelChangeListeners.get(index).modelChanged(this, oldModel, newModel);
		}
	}

	@Override
	public final int getSelectedIndex() {
		Object singleSelection = getSingleSelection();
		if (singleSelection == null) {
			return -1;
		}
		return getSelectableCards().indexOf(singleSelection);
	}

	/**
	 * This method returns the {@link Listeners} which hold all listeners of this
	 * {@link DefaultDeckPaneModel}.
	 */
	protected final Listeners getListeners() {
		return listeners;
	}

	/**
	 * An {@link Iterator} for the contents of the selectable cards.
	 * 
	 * <p>
	 * The returned {@link Iterator} does not support the {@link Iterator#remove()} method.
	 * </p>
	 * 
	 * @param <T>
	 *        The expected type of the {@link Card#getContent() card content}. The type is not
	 *        checked.
	 */
	public <T> Iterator<T> getContentIterator() {
		return new Iterator<>() {

			private Iterator<Card> theIterator = selectableObjects.iterator();

			@Override
			public boolean hasNext() {
				return theIterator.hasNext();
			}

			@Override
			public T next() {
				@SuppressWarnings("unchecked")
				T content = (T) theIterator.next().getContent();
				return content;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"This Iterator does not provide removing. Instead, find the corresponding card and delete it.");
			}

		};
	}
}
