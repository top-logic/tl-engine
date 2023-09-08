/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import java.util.List;

import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.mig.html.layout.Card;

/**
 * The class {@link TabBarModel} is a model for using to render some {@link Card}s as tabs.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TabBarModel extends NamedModel {

	/**
	 * The burger menu to display in the tab bar.
	 */
	default Menu getBurgerMenu() {
		return null;
	}

	/**
	 * This method returns the list of {@link Card}s which are possible to select in general.
	 * 
	 * @return a list of {@link Card}s. never <code>null</code>.
	 */
	public List<Card> getAllCards();

	/**
	 * This method returns the index of the current selected card.
	 * 
	 * @return the index of the currently selected card, or <code>-1</code> if currently no card
	 *         can be selected.
	 */
	public int getSelectedIndex();

	/**
	 * This method returns whether the given {@link Card} is currently inactive, i.e. not
	 * selectable.
	 * 
	 * @param aCard
	 *            the {@link Card} whose active state shall be checked
	 * @return <code>true</code> iff the given {@link Card} can not be selected.
	 */
	public boolean isInactive(Card aCard);

	/**
	 * This method sets the new selected index of this {@link TabBarModel}.
	 * 
	 * @param index
	 *            the new selected index. must be larger or equal to <code>0</code> and less than
	 *            the length of the slectable objects.
	 * @throws IllegalArgumentException
	 *             if the index is out of range, or the card to select is currently inactive.
	 */
	public void setSelectedIndex(int index);

	/**
	 * This method adds a {@link TabBarListener} to this {@link TabBarModel}.
	 * 
	 * @param listener
	 *            must not be <code>null</code>
	 * @return <code>true</code> if this listener was successfully added
	 */
	public boolean addTabBarListener(TabBarListener listener);

	/**
	 * This method removes the given {@link TabBarListener} from this {@link TabBarModel}.
	 * 
	 * @return <code>true</code> if this listener was successfully removed
	 */
	public boolean removeTabBarListener(TabBarListener listener);

	/**
	 * The interface {@link TabBarModel.TabBarListener} is a cumulative listener interface.
	 * {@link TabBarModel.TabBarListener} added to some {@link TabBarModel} will be informed about
	 * the inner state of the model.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public interface TabBarListener extends SingleSelectionListener {

		/**
		 * This method notifies that the list of general selectable objects have changed.
		 * 
		 * @param sender
		 *            the {@link TabBarModel} whose list has changed
		 * @param oldAllCards
		 *            the list of selectable objects before the change
		 */
		public void notifyCardsChanged(TabBarModel sender, List<Card> oldAllCards);

		/**
		 * This method notifies that the inactive &quot;state&quot; of the given {@link Card} has changed
		 * 
		 * @param sender
		 *            the {@link TabBarModel} whose inactive objects have changed
		 * @param aCard
		 *            the {@link Card} whose inactive &quot;state&quot; has changed
		 */
		public void inactiveCardChanged(TabBarModel sender, Card aCard);

		/**
		 * Notifies the {@link TabBarListener} about the change of
		 * {@link TabBarModel#getBurgerMenu()}.
		 * 
		 * @param sender
		 *        The {@link TabBarModel} with changed {@link TabBarModel#getBurgerMenu()}.
		 * @param oldMenu
		 *        The former commands.
		 */
		default void notifyCommandsChanged(TabBarModel sender, Menu oldMenu) {
			// Don't care about command change.
		}
	}
}
