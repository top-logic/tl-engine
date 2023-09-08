/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.ChildrenCheckScopeProvider;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link DefaultTabSwitchVetoListener} is the default veto listener for tab bars.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultTabSwitchVetoListener implements TabSwitchVetoListener {
	
	/**
	 * Singleton {@link DefaultTabSwitchVetoListener} instance.
	 */
	public static final DefaultTabSwitchVetoListener INSTANCE = new DefaultTabSwitchVetoListener();

	private DefaultTabSwitchVetoListener() {
		// Singleton constructor.
	}

	@Override
	public void checkVeto(TabBarModel tabBar, int newTab) throws VetoException {
		Collection<? extends ChangeHandler> vetoHandlers = getVetoHandlers(tabBar);
		DirtyHandling.checkVeto(vetoHandlers);
    }

	private Collection<? extends ChangeHandler> getVetoHandlers(TabBarModel tabBarModel) {
		List<Card> allCards = tabBarModel.getAllCards();
    	int selectedIndex = tabBarModel.getSelectedIndex();
    	if (selectedIndex < 0 || allCards.size() <= selectedIndex) {
			return Collections.emptyList();
    	}
		LayoutComponent cardContent = (LayoutComponent) (allCards.get(selectedIndex)).getContent();
    	return ChildrenCheckScopeProvider.INSTANCE.getCheckScope(cardContent).getAffectedFormHandlers();
    }

}

