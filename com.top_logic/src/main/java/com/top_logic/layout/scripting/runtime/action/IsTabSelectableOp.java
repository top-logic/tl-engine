/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.ConditionalAction;
import com.top_logic.layout.scripting.action.NamedTabSwitch;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ApplicationActionOp} that computes whether a given tab in a given tab component can be
 * selected.
 * 
 * <p>
 * This action is only useful as {@link ConditionalAction#getCondition() condition} in an
 * {@link ConditionalAction}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class IsTabSelectableOp extends ComponentActionOp<NamedTabSwitch> {

	/**
	 * Creates a {@link IsTabSelectableOp} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public IsTabSelectableOp(InstantiationContext context, NamedTabSwitch config) {
		super(context, config);
	}

	@Override
	public Object process(ActionContext context, LayoutComponent component, Object argument) {
		TabComponent tabComponent = (TabComponent) component;
		if (!tabComponent.isVisible()) {
			return false;
		}

		String cardName = config.getCardName();
		for (Card card : tabComponent.getCards()) {
			if (cardName.equals(card.getName())) {
				return tabComponent.canBeVisible((LayoutComponent) card.getContent());
			}
		}

		throw ApplicationAssertions.fail(config, "Cannot no such card '" + config.getCardName()
			+ "' in tab component '" + config.getComment() + "'.");
	}

	/**
	 * Creates a {@link IsTabSelectableOp} configuration.
	 * 
	 * @param name
	 *        The name of the tab component.
	 * @param cardName
	 *        The name of the card to make visible.
	 * @return The {@link ApplicationAction} that tests switching the tab.
	 */
	public static NamedTabSwitch newInstance(ComponentName name, String cardName) {
		NamedTabSwitch config = TypedConfiguration.newConfigItem(NamedTabSwitch.class);
		config.setComponentName(name);
		config.setCardName(cardName);
		config.setImplementationClass(IsTabSelectableOp.class);
		return config;
	}
}
