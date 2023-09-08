/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.component.model.DefaultDeckPaneModel;
import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.util.Utils;

/**
 * The class {@link DefaultTabBarModel} is the default implementation of a {@link TabBarModel} which
 * based on a {@link DefaultDeckPaneModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultTabBarModel extends DefaultDeckPaneModel implements TabBarModel {

	private final HashSet<Card> _inactiveCards;

	private Menu _menu = null;

	/**
	 * Creates a {@link DefaultTabBarModel}.
	 *
	 * @param cards
	 *        See {@link #getAllCards()}.
	 */
	public DefaultTabBarModel(List<? extends Card> cards) {
		this(cards, Collections.<Card> emptySet());
	}

	/**
	 * Creates a {@link DefaultTabBarModel} with some {@link Card}s being inactive (not displayed).
	 *
	 * @param cards
	 *        See {@link #getAllCards()}
	 * @param inactiveCards
	 *        Subset of {@link #getAllCards()} that are currently hidden.
	 */
	public DefaultTabBarModel(List<? extends Card> cards, Set<? extends Card> inactiveCards) {
		super(cards);
		checkInactiveCards(cards, inactiveCards);
		_inactiveCards = new HashSet<>(inactiveCards);
	}

	/**
	 * Checks whether the given set <code>someInactiveCards</code> is a subset of
	 * <code>someCards</code>.
	 */
	private static void checkInactiveCards(List<? extends Card> allCards, Set<? extends Card> inactiveCards) {
		Set<Card> tempSet = new HashSet<>(inactiveCards);
		tempSet.removeAll(allCards);
		if (tempSet.size() != 0) {
			throw new IllegalArgumentException("The inactive cards must be a subset of the given list of cards!");
		}
	}

	@Override
	public boolean isInactive(Card card) {
		return _inactiveCards.contains(card);
	}

	/**
	 * This method adds a card to the inactive cards.
	 * 
	 * @param newInactiveCard
	 *            the card which shall be inactive.
	 * @return <code>true</code> if the card is selectable in general and it was not formerly
	 *         added as inactive.
	 */
	public boolean addInactiveCard(Card newInactiveCard) {
		if (!getAllCards().contains(newInactiveCard)) {
			return false;
		}
		boolean succeeded = _inactiveCards.add(newInactiveCard);
		if (succeeded) {
			if (Utils.equals(newInactiveCard, getSingleSelection())) {
				setSelectedIndex(-1);
			}
			fireInactiveCardChangedEvent(newInactiveCard);
		}
		return succeeded;
	}

	/**
	 * This method removes the given card form the set of inactive cards.
	 * 
	 * @param oldInactiveCard
	 *            the card which shall be no longer inactive
	 * @return <code>true</code> if the given card was formerly added as inactive card.
	 */
	public boolean removeInactiveCard(Card oldInactiveCard) {
		boolean succeeded = _inactiveCards.remove(oldInactiveCard);
		if (succeeded) {
			fireInactiveCardChangedEvent(oldInactiveCard);
		}
		return succeeded;
	}

	@Override
	public boolean removeSelectableCard(Card aCard) {
		boolean succedded = super.removeSelectableCard(aCard);
		if (succedded) {
			removeInactiveCard(aCard);
		}
		return succedded;
	}

	/**
	 * This method sets a new list of selectable cards. The cards in the old list which are also in
	 * the given list and marked as inactive remain inactive.
	 * 
	 * @see com.top_logic.layout.component.model.DefaultDeckPaneModel#setSelectableObjects(List)
	 */
	@Override
	public boolean setSelectableObjects(List<? extends Card> someCards) {
		boolean succeded = super.setSelectableObjects(someCards);
		if (succeded) {
			_inactiveCards.retainAll(someCards);
		}
		return succeded;
	}

	@Override
	public void setSelected(Object touchedObject, boolean select) {
		if (select && isInactive((Card) touchedObject)) {
			throw new IllegalArgumentException("The Object to select is currently inactive!");
		}
		super.setSelected(touchedObject, select);
	}

	/**
	 * @see com.top_logic.layout.component.model.DefaultDeckPaneModel#setSelectedIndex(int)
	 * @see com.top_logic.layout.tabbar.TabBarModel#setSelectedIndex(int)
	 */
	@Override
	public void setSelectedIndex(int newSelection) {
		if (newSelection >= 0 && newSelection < getAllCards().size()) {
			if (isInactive(getAllCards().get(newSelection))) {
				throw new IllegalArgumentException("The Object to select is currently inactive!");
			}
		}
		super.setSelectedIndex(newSelection);
	}

	@Override
	public boolean addTabBarListener(TabBarListener listener) {
		addSingleSelectionListener(listener);
		addModelChangedListener(new ModelChangeListenerAdapter(listener));
		return getListeners().add(TabBarListener.class, listener);
	}

	@Override
	public boolean removeTabBarListener(TabBarListener listener) {
		removeSingleSelectionListener(listener);
		removeModelChangedListener(new ModelChangeListenerAdapter(listener));
		return getListeners().remove(TabBarListener.class, listener);
	}

	/**
	 * This method informs all registered {@link TabBarModel.TabBarListener} about a change of the
	 * inactive &quot;state&quot; of the given card.
	 * 
	 * @param card
	 *        the card whose &quot;state&quot; changed
	 */
	protected void fireInactiveCardChangedEvent(Card card) {
		List<TabBarListener> tabBarListeners = tabBarListeners();
		for (int index = 0, size = tabBarListeners.size(); index < size; index++) {
			tabBarListeners.get(index).inactiveCardChanged(this, card);
		}
	}

	private List<TabBarListener> tabBarListeners() {
		return getListeners().get(TabBarListener.class);
	}

	@Override
	public final List<Card> getAllCards() {
		return getSelectableCards();
	}

	@Override
	public Menu getBurgerMenu() {
		return _menu;
	}

	/**
	 * Sets the commands for this {@link TabBarModel}.
	 * 
	 * @param newMenu
	 *        The new value of {@link #getBurgerMenu()}.
	 */
	public void setBurgerMenu(Menu newMenu) {
		if (Utils.equals(newMenu, _menu)) {
			return;
		}
		Menu oldMenu = _menu;
		_menu = newMenu;
		fireCommandsChangedEvent(oldMenu);

	}

	/**
	 * This method informs all registered {@link TabBarModel.TabBarListener} about a change of the
	 * value of {@link #getBurgerMenu()}.
	 * 
	 * @param oldCommands
	 *        Former commands.
	 */
	protected void fireCommandsChangedEvent(Menu oldCommands) {
		List<TabBarListener> tabBarListeners = tabBarListeners();
		for (int index = 0, size = tabBarListeners.size(); index < size; index++) {
			tabBarListeners.get(index).notifyCommandsChanged(this, oldCommands);
		}
	}

	/**
	 * The class {@link ModelChangeListenerAdapter} is an adapter class which wraps a
	 * {@link TabBarModel.TabBarListener}. Two {@link ModelChangeListenerAdapter} are equal if the
	 * wrapping {@link TabBarModel.TabBarListener} are equal. The class can be used if a
	 * {@link ModelChangeListener} is needed but a {@link TabBarModel.TabBarListener} is present.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private final static class ModelChangeListenerAdapter implements ModelChangeListener {

		private final TabBarListener tabBarListener;

		public ModelChangeListenerAdapter(TabBarListener tabBarListener) {
			if (tabBarListener == null) {
				throw new IllegalArgumentException("'tabBarListener' must not be 'null'.");
			}
			this.tabBarListener = tabBarListener;

		}

		/**
		 * Dispatches this method to
		 * {@link TabBarModel.TabBarListener#notifyCardsChanged(TabBarModel, List)}. It is expected
		 * that the sender in this model is a {@link TabBarModel} and the old model is a list of
		 * {@link Card}s.
		 * 
		 * @see com.top_logic.layout.component.model.ModelChangeListener#modelChanged(java.lang.Object,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
			@SuppressWarnings("unchecked")
			List<Card> oldCards = (List<Card>) oldModel;
			this.tabBarListener.notifyCardsChanged((TabBarModel) sender, oldCards);
			return Bubble.BUBBLE;
		}

		/**
		 * The given object is equal to <code>this</code> if it is a
		 * {@link ModelChangeListenerAdapter} and the wrapped {@link TabBarModel.TabBarListener} is
		 * equal to {@link #tabBarListener}.
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof ModelChangeListenerAdapter)) {
				return false;
			}
			return tabBarListener.equals(((ModelChangeListenerAdapter) obj).tabBarListener);
		}

		/**
		 * Returns the hash code of the wrapped {@link TabBarModel.TabBarListener}
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return tabBarListener.hashCode();
		}

	}

}
