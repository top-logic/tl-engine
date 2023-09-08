/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import java.util.List;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.ObservableBase;
import com.top_logic.layout.component.model.DefaultDeckPaneModel;
import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.form.control.DeckPaneModel;
import com.top_logic.mig.html.layout.Card;

/**
 * {@link TabBarModel} based on a {@link DefaultDeckPaneModel} which has no inactive {@link Card}s.
 * 
 * <p>
 * All possible methods are dispatched to its {@link DefaultDeckPaneModel}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TabBarModelAdapter extends ObservableBase implements TabBarModel, SingleSelectionModel {

	private final DeckPaneListener deckPaneListener = new DeckPaneListener();

	@Override
	public boolean addTabBarListener(TabBarListener listener) {
		boolean mustAddListener = !hasListeners(TabBarListener.class);
		boolean succeded = addListener(TabBarListener.class, listener);
		if (mustAddListener && succeded) {
			getDeck().addModelChangedListener(deckPaneListener);
			getDeck().addSingleSelectionListener(deckPaneListener);
		}
		return succeded;
	}

	@Override
	public boolean removeTabBarListener(TabBarListener listener) {
		boolean succeded = removeListener(TabBarListener.class, listener);
		if (succeded && !hasListener(TabBarListener.class, listener)) {
			getDeck().removeModelChangedListener(deckPaneListener);
			getDeck().removeSingleSelectionListener(deckPaneListener);
		}
		return succeded;
	}

	@Override
	public final List<Card> getAllCards() {
		return getDeck().getSelectableCards();
	}

	@Override
	public final int getSelectedIndex() {
		return getDeck().getSelectedIndex();
	}

	@Override
	public void setSelectedIndex(int index) {
		getDeck().setSelectedIndex(index);
	}

	@Override
	public boolean isInactive(Card card) {
		return false;
	}

	@Override
	public Object getSingleSelection() {
		return getDeck().getSingleSelection();
	}

	@Override
	public boolean addSingleSelectionListener(SingleSelectionListener listener) {
		throw new UnsupportedOperationException(
			"can not delegate to inner deck, because sender of event would be incorrect.");
	}

	@Override
	public boolean removeSingleSelectionListener(SingleSelectionListener listener) {
		throw new UnsupportedOperationException(
			"can not delegate to inner deck, because sender of event would be incorrect.");
	}

	@Override
	public void setSingleSelection(Object obj) {
		getDeck().setSingleSelection(obj);
	}

	@Override
	public boolean isSelectable(Object obj) {
		return getDeck().isSelectable(obj);
	}

	/**
	 * The {@link DeckPaneModel} to take {@link #getAllCards()} from.
	 */
	public abstract DefaultDeckPaneModel getDeck();

	final List<TabBarListener> getTabBarListeners() {
		return getListeners(TabBarListener.class);
	}

	/*package protected*/ final class DeckPaneListener implements ModelChangeListener, SingleSelectionListener {

		@Override
		public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
			assert sender == getDeck();
			List<TabBarListener> tabBarListener = getTabBarListeners();
			@SuppressWarnings("unchecked")
			List<Card> oldCards = (List<Card>) oldModel;
			for (int index = 0, size = tabBarListener.size(); index < size; index++) {
				tabBarListener.get(index).notifyCardsChanged(TabBarModelAdapter.this, oldCards);
			}
			return Bubble.BUBBLE;
		}

		@Override
		public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
			assert model == getDeck();
			List<TabBarListener> tabBarListener = getTabBarListeners();
			for (int index = 0, size = tabBarListener.size(); index < size; index++) {
				tabBarListener.get(index).notifySelectionChanged(TabBarModelAdapter.this, formerlySelectedObject,
					selectedObject);
			}
		}

	}

}
