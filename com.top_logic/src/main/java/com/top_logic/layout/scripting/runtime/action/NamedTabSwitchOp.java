/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;


import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.action.NamedTabSwitch;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ApplicationActionOp} that switches a tab to a given card name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NamedTabSwitchOp extends ComponentActionOp<NamedTabSwitch> {

	/** If {@link #findCardIndexByName(TabComponent, String)} finds nothing, it returns this. */
	public static final int NOTHING_FOUND = -1;

	/**
	 * Creates a {@link NamedTabSwitchOp} from a {@link NamedTabSwitch}.
	 * <p>
	 * Is called by the {@link TypedConfiguration}.
	 * </p>
	 */
	@CalledByReflection
	public NamedTabSwitchOp(InstantiationContext context, NamedTabSwitch config) {
		super(context, config);
	}
	
	@Override
	public Object process(ActionContext context, LayoutComponent component, Object argument) {
		String cardName = config.getCardName();
		
		ApplicationAssertions.assertTrue(config,
			"Switched tab '" + config.getComponentName() + "' is not visible.", component.isVisible());
		
		TabComponent tabComponent = (TabComponent) component;
		int index = findCardIndexByName(tabComponent, cardName);
		if (index == NOTHING_FOUND) {
			ApplicationAssertions.fail(config,
				"No such card '" + cardName + "' in tab '" + config.getComponentName() + "', available cards are: "
					+ getCards(tabComponent).stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
		}
		TabBarModel model = tabComponent.getTabBarModel();
		Card card = model.getAllCards().get(index);
		if (model.isInactive(card)) {
			ApplicationAssertions.fail(config,
				"Card '" + cardName + "' in tab '" + config.getComponentName() + "' is not visible.");
		}
		model.setSelectedIndex(index);
		
		return argument;
	}

	/**
	 * Find the index of the tab with the given technical {@link Card#getName()} in the given
	 * {@link TabComponent}.
	 * 
	 * @return {@link #NOTHING_FOUND}, if none of the cards has the given name.
	 */
	public static int findCardIndexByName(TabComponent tabComponent, String cardName) {
		List<Card> allCards = getCards(tabComponent);
		for (int n = 0, cnt = allCards.size(); n < cnt; n++) {
			Card card = allCards.get(n);
			if (cardName.equals(card.getName())) {
				return n;
			}
		}
		return NOTHING_FOUND;
	}

	/**
	 * Find the index of the tab with the given {@link TabInfo#getLabel() label} in the given
	 * {@link TabComponent}.
	 * 
	 * @return {@link #NOTHING_FOUND}, if none of the cards has the given label.
	 */
	public static int findCardIndexByLabel(TabComponent tabComponent, String label) {
		Set<Integer> matches = set();
		List<Card> allCards = getCards(tabComponent);
		for (int i = 0; i < allCards.size(); i++) {
			Card card = allCards.get(i);
			if (tabComponent.getTabBar().isInactive(card)) {
				continue;
			}
			TabInfo tabInfo = (TabInfo) card.getCardInfo();
			if (label.equals(tabInfo.getLabel())) {
				matches.add(i);
			}
		}
		if (matches.isEmpty()) {
			return NOTHING_FOUND;
		}
		if (matches.size() > 1) {
			List<Card> collisions = list();
			matches.forEach(index -> collisions.add(allCards.get(index)));
			throw new RuntimeException("Found multiple visible tabs with the label '" + label + "': " + collisions);
		}
		return matches.iterator().next();
	}

	private static List<Card> getCards(TabComponent tabComponent) {
		TabBarModel tabModel = tabComponent.getTabBarModel();
		return tabModel.getAllCards();
	}

}
