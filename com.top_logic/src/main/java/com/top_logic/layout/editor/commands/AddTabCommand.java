/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.editor.components.ComponentPlaceholder;
import com.top_logic.layout.editor.scripting.Identifiers;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutReference;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.SimpleCard;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;
import com.top_logic.util.error.TopLogicException;

/**
 * Adds a new tab to a tab bar.
 * 
 * <p>
 * Existing tabs can be rearranged using the command {@link ConfigureTabsCommand}.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@Label("Add tab")
public class AddTabCommand extends AbstractComponentConfigurationCommandHandler {

	/**
	 * Identifier under which an instance of this class is configured in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "addTabCommand";

	/**
	 * Creates a {@link AddTabCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AddTabCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult createComponent(LayoutComponent component, DynamicComponentDefinition definition,
			ConfigurationItem componentConfig, List<AdditionalComponentDefinition> additional) {
		HandlerResult result = HandlerResult.DEFAULT_RESULT;

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			Identifiers identifiers = LayoutTemplateUtils.replaceInnerTemplates(componentConfig);

			String tabIdentifier = LayoutTemplateUtils.createNewComponentLayoutKey();
			identifiers.addComponentKey(tabIdentifier);
			LayoutTemplateUtils.recordComponentCreation(ButtonType.OK.getButtonLabelKey(), identifiers, component);
			try {
				createTab((TabComponent) component, definition.definitionFile(), componentConfig, tabIdentifier);
			} catch (ConfigurationException exception) {
				result = HandlerResult.error(I18NConstants.ADD_TAB_ERROR__TABBAR.fill(component.getName()), exception);
			}

			tx.commit();
		}

		return result;
	}

	@Override
	protected List<DynamicComponentDefinition> getComponentTemplates(LayoutComponent context) {
		return LayoutTemplateUtils.getTemplates(Collections.singleton(TabComponent.TABBAR_TEMPLATE_GROUP), context);
	}
	
	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(InDesignModeExecutable.INSTANCE, IsTemplateLayout.INSTANCE);
	}

	/**
	 * Creates a new tab layout (containing mainly the {@link TabInfo}), a
	 * {@link ComponentPlaceholder} layout for the tab content and updates the tabbar.
	 */
	private void createTab(TabComponent component, String template, ConfigurationItem arguments, String key)
			throws ConfigurationException {
		LayoutTemplateUtils.storeLayout(key, template, arguments);

		updateTabbar(component, key);
	}

	private void updateTabbar(TabComponent tabbar, String layoutKey) throws ConfigurationException {
		ArrayList<Card> newCards = getNewCards(tabbar, LayoutTemplateUtils.createLayoutReference(layoutKey));
		String tabbarNameScope = LayoutTemplateUtils.getNonNullNameScope(tabbar);
		MainLayout mainLayout = tabbar.getMainLayout();
	
		TLLayout layout = LayoutTemplateUtils.getOrCreateLayout(tabbarNameScope);
		String templateName = layout.getTemplateName();
		ConfigurationItem arguments = getTemplateArguments(layout, templateName);
	
		LayoutTemplateUtils.updateTabbarCards(newCards, arguments, tabbar, tabbarNameScope, templateName);
		TabComponent newTabbar = (TabComponent) mainLayout.getComponentByName(tabbar.getName());
	
		int newCardSize = newCards.size();
		if (newCardSize == newTabbar.getChildCount()) {
			newTabbar.setSelectedIndex(newCardSize - 1);
		}
	}

	private ArrayList<Card> getNewCards(TabComponent tabbar, LayoutReference referenceToTab) {
		ArrayList<Card> cardsCopy = getAllCards(tabbar);

		cardsCopy.add(new SimpleCard("new tab", referenceToTab));

		return cardsCopy;
	}

	private ArrayList<Card> getAllCards(TabComponent tabbar) {
		return new ArrayList<>(tabbar.getTabBar().getAllCards());
	}

	private ConfigurationItem getTemplateArguments(TLLayout layout, String template) {
		try {
			return layout.getArguments();
		} catch (ConfigurationException exception) {
			ResKey1 invalidTemplateConfig =
				com.top_logic.layout.editor.I18NConstants.ERROR_LAYOUT_CONFIGURATION_INVALID__LAYOUT;

			throw new TopLogicException(invalidTemplateConfig.fill(template), exception);
		}
	}
}
