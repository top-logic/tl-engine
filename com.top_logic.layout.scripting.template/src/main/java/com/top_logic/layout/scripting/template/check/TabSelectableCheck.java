/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.check;

import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.CheckVisibilityOp;
import com.top_logic.layout.scripting.runtime.action.ComponentActionOp;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ComponentActionOp} that checks whether a tab is selectable (or not).
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Check for selectable tab")
public class TabSelectableCheck extends ComponentActionOp<TabSelectableCheck.TabSelectableCheckConfig> {

	/**
	 * Configuration for {@link TabSelectableCheck}. 
	 * 
	 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface TabSelectableCheckConfig extends ComponentAction {

		@Override
		@ClassDefault(TabSelectableCheck.class)
		Class<CheckVisibilityOp> getImplementationClass();

		/**
		 * Name of the tab to check.
		 */
		@Mandatory
		String getCardName();

		/**
		 * Setter for {@link #getCardName()}.
		 */
		void setCardName(String name);

		/** 
		 * Whether the tab (the object to check) must be selectable.
		 */
		boolean isSelectable();

		/**
		 * Setter for {@link #isSelectable()}.
		 */
		void setSelectable(boolean b);
	}

	/**
	 * Creates a new {@link TabSelectableCheck} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TabSelectableCheck}.
	 * 
	 */
	public TabSelectableCheck(InstantiationContext context, TabSelectableCheckConfig config) {
		super(context, config);
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		TabComponent tab = (TabComponent) component;
		TabBarModel tabModel = tab.getTabBarModel();
		boolean isActive = !tabModel.isInactive(getCard(tabModel, getConfig().getCardName()));
		if (isActive != getConfig().isSelectable()) {
			StringBuilder failure = new StringBuilder();
			failure.append("Tab ");
			failure.append(component);
			failure.append(" is ");
			failure.append(isActive ? "active" : "inactive");
			failure.append(" but is asserted that not.");
			throw ApplicationAssertions.fail(getConfig(), failure.toString());
		}
		return argument;
	}

	private Card getCard(TabBarModel tabModel, String cardName) {
		for (Card card : tabModel.getAllCards()) {
			if (cardName.equals(card.getName())) {
				return card;
			}
		}
		throw ApplicationAssertions.fail(getConfig(), "No card with name " + cardName + ". Available: "
			+ tabModel.getAllCards().stream().map(Card::getName).collect(Collectors.joining(", ")));
	}

}

