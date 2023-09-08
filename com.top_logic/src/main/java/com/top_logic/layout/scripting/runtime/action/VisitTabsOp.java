/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.action.ActionChain;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.ActionUtil;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.NamedTabSwitch;
import com.top_logic.layout.scripting.action.VisitTabs;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * {@link ApplicationActionOp} that visits all of the {@link LayoutContainer}s tabs and subtabs. <br/>
 * The given {@link LayoutContainer} should either be a {@link TabComponent} or the
 * {@link MainLayout}.
 * 
 * @see VisitTabs
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class VisitTabsOp extends DynamicActionOp<VisitTabs> {

	private final Set<ComponentName> _ignoredComponents;

	/**
	 * Creates a {@link VisitTabsOp} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public VisitTabsOp(InstantiationContext context, VisitTabs config) {
		super(context, config);
		_ignoredComponents = new HashSet<>(config.getIgnoreComponents());
	}

	@Override
	public List<ApplicationAction> createActions(ActionContext context) {
		ArrayList<ApplicationAction> result = new ArrayList<>();
		LayoutComponent component =
			ActionUtil.getComponentByName(getConfig().getComponentName(), context.getMainLayout(), getConfig());

		assert component instanceof LayoutContainer : "This action was called on something that is no LayoutContainer. But only LayoutContainer can contain TabComponents! Component: "
			+ StringServices.getObjectDescription(component) + "; Action: " + getConfig();
		assert !_ignoredComponents.contains(component.getName()) : "This action was called on a component "
			+ StringServices.getObjectDescription(component) + " that should be ignored; Action: " + getConfig();

		makeVisible(result, (LayoutContainer) component);
		visitContainer(result, (LayoutContainer) component);
		return result;
	}

	private void makeVisible(List<ApplicationAction> result, LayoutContainer container) {
		if (container.getParent() != null) {
			makeVisible(result, (LayoutContainer) container.getParent());
		}
		if (container.getParent() instanceof TabComponent) {
			TabComponent parentTabBar = (TabComponent) container.getParent();
			result.add(ActionFactory.makeVisible(parentTabBar.getName()));
			result.add(ActionFactory.tabSwitchNamed(parentTabBar, parentTabBar.getIndexOfChild(container)));
		}
	}

	private void visitContainer(List<ApplicationAction> result, LayoutContainer container) {
		if (ignore(container)) {
			return;
		}
		if (container instanceof TabComponent) {
			visitTabbar(result, (TabComponent) container);
		} else {
			for (LayoutComponent child : container.getChildList()) {
				if (child instanceof LayoutContainer) {
					visitContainer(result, (LayoutContainer) child);
				}
			}
		}
	}

	private boolean ignore(LayoutComponent comp) {
		return _ignoredComponents.contains(comp.getName());
	}

	private void visitTabbar(List<ApplicationAction> result, TabComponent tabComponent) {
		Logger.debug("Visiting tab bar '" + tabComponent.getName() + "'.", VisitTabsOp.class);
		for (int i = 0; i < tabComponent.getChildCount(); i++) {
			LayoutComponent child = tabComponent.getChild(i);
			if (ignore(child)) {
				continue;
			}

			// Note: The inner tabs must only be processed, if the top-level tab can be made
			// visible. Depending on the component models, not all tabs can be selected.
			List<ApplicationAction> conditionalActions = new ArrayList<>();

			conditionalActions.add(ActionFactory.tabSwitchNamed(tabComponent, i));
			if (child instanceof LayoutContainer) {
				visitContainer(conditionalActions, (LayoutContainer) child);
			}

			NamedTabSwitch condition =
				IsTabSelectableOp.newInstance(tabComponent.getName(), tabComponent.getTabName(i));
			ActionChain conditionalChain = ActionFactory.conditionalAction(condition, conditionalActions);

			result.add(conditionalChain);
		}
	}

}
