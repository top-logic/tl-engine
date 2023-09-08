/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.editor.config.LayoutEditorConfig;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.layout.tabbar.TabbedLayoutComponentResourceProvider;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutReference;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.SimpleCard;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;
import com.top_logic.util.Resources;
import com.top_logic.util.TLResKeyUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} to configure the tabs in a tab component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigureTabsCommand extends AbstractCommandHandler {

	/**
	 * {@link CommandHandler#getID() Command ID} under which an instance of
	 * {@link ConfigureTabsCommand} is registered in the {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "configureTabs";

	/**
	 * Creates a new {@link ConfigureTabsCommand} from the given configuration.
	 */
	public ConfigureTabsCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		TabComponent tabComponent = (TabComponent) component;
		MainLayout main = tabComponent.getMainLayout();
		TabBarModel tabBarModel = tabComponent.getTabBarModel();
		
		List<Card> unusedComponents = getUnusedCards(tabComponent);
		
		FormContext formContext = new FormContext(component);
		SelectField selectField = createConfiguringCardsField(unusedComponents);
		formContext.addMember(selectField);

		/* Do not record setting the value of the field, because no owner can be found. Configuring
		 * the tabs is recorded explicit. */
		ScriptingRecorder.annotateAsDontRecord(selectField);

		SelectFieldUtils.setOptionLabelProvider(selectField, new TabbedLayoutComponentResourceProvider());

		selectField.initializeField(tabBarModel.getAllCards());
		selectField.setFixedOptions(getNotRecreatableComponents(main, tabBarModel));
		selectField.addValueListener(createStoreTabsListener(component, tabComponent));
		selectField.setCustomOrder(true);
		selectField.setLabel(aContext.getResources().getString(I18NConstants.CONFIGURE_TABS_DIALOG_LABEL));

		return SelectionControl.openSelectorPopup(aContext, aContext.getWindowScope(), selectField);
	}

	private SelectField createConfiguringCardsField(List<Card> unusedComponents) {
		return FormFactory.newSelectField("configureCards", unusedComponents,
			FormFactory.MULTIPLE, !FormFactory.IMMUTABLE);
	}

	private ValueListener createStoreTabsListener(LayoutComponent component, TabComponent tabComponent) {
		String tabLayoutKey = LayoutTemplateUtils.getNonNullNameScope(component);
		TLLayout layout = LayoutTemplateUtils.getOrCreateLayout(tabLayoutKey);
		ConfigurationItem arguments = getTemplateCallArguments(layout);

		return createStoreTabsListener(tabComponent, tabLayoutKey, layout.getTemplateName(), arguments);
	}

	private ConfigurationItem getTemplateCallArguments(TLLayout layout) {
		ConfigurationItem arguments = null;

		try {
			arguments = layout.getArguments();
		} catch (ConfigurationException exception) {
			Logger.error(Resources.getInstance()
				.getString(
					com.top_logic.layout.editor.I18NConstants.ERROR_LAYOUT_CONFIGURATION_INVALID__LAYOUT
					.fill(layout.getTemplateName())),
				exception, this);
		}

		return arguments;
	}

	private List<Card> getNotRecreatableComponents(MainLayout main, TabBarModel tabBarModel) {
		return tabBarModel.getAllCards()
			.stream()
			.filter(card -> !isRecreatable(main, (LayoutComponent) card.getContent()))
			.collect(Collectors.toList());
	}

	private boolean isRecreatable(MainLayout main, LayoutComponent component) {
		return main.getLayoutKey(component) != null;
	}

	private List<Card> getUnusedCards(TabComponent tabComponent) {
		List<String> excludedLayouts = getExcludedLayouts();

		return LayoutStorage.getInstance()
			.getLayouts(ThemeFactory.getTheme())
			.entrySet()
			.stream()
			.filter(entry -> isUnusedAllowedComponent(tabComponent, excludedLayouts, entry.getKey()))
			.filter(entry -> hasTabInfoAndTitle(entry))
			.map(entry -> createNewCard(entry))
			.collect(Collectors.toList());
	}

	private boolean hasTabInfoAndTitle(Entry<String, TLLayout> layoutByKey) {
		try {
			LayoutComponent.Config config = layoutByKey.getValue().get();

			if (config.getTabInfo() == null) {
				return false;
			}

			if (!TLResKeyUtil.existsResource(LayoutComponent.Config.getEffectiveTitleKey(config))) {
				return false;
			}

			return true;
		} catch (ConfigurationException exception) {
			throw new TopLogicException(I18NConstants.RESOLVE_COMPONENT_ERROR__LAYOUT_KEY.fill(layoutByKey.getKey()),
				exception);
		}
	}

	private SimpleCard createNewCard(Entry<String, TLLayout> configByLayoutKey) {
		String layoutName = configByLayoutKey.getKey();
		TabInfo info = TabInfo.newTabInfo(getTitleKey(configByLayoutKey));
		LayoutReference reference = TypedConfiguration.newConfigItem(LayoutReference.class);
		reference.setResource(layoutName);
		return new SimpleCard(info, layoutName, reference);
	}

	private ResKey getTitleKey(Entry<String, TLLayout> entry) {
		try {
			return LayoutComponent.Config.getEffectiveTitleKey(entry.getValue().get());
		} catch (ConfigurationException exception) {
			throw new TopLogicException(I18NConstants.RESOLVE_COMPONENT_ERROR__LAYOUT_KEY.fill(entry.getKey()),
				exception);
		}
	}

	private boolean isUnusedAllowedComponent(TabComponent tabComponent, List<String> excludedLayouts, String layoutKey) {
		boolean isUnused = tabComponent.getMainLayout().getComponentForLayoutKey(layoutKey) == null;
		boolean isAllowed = !excludedLayouts.contains(layoutKey);

		return isAllowed && isUnused;
	}

	private List<String> getExcludedLayouts() {
		return ApplicationConfig.getInstance().getConfig(LayoutEditorConfig.class).getExcludedLayouts();
	}

	private ValueListener createStoreTabsListener(TabComponent tabComponent, String tabLayoutKey,
			String templateName, ConfigurationItem arguments) {
		return (field, oldValue, newValue) -> {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			try (Transaction tx = kb.beginTransaction()) {
				@SuppressWarnings("unchecked")
				List<? extends Card> newCards = (List<? extends Card>) newValue;

				try {
					LayoutTemplateUtils.updateTabbarCards(newCards, arguments, tabComponent, tabLayoutKey,
						templateName);
				} catch (ConfigurationException exception) {
					throw new TopLogicException(I18NConstants.CONFIGURE_TABS_ERROR__TABBAR.fill(tabComponent.getName()),
						exception);
				}

				tx.commit();
			}
		};
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(InDesignModeExecutable.INSTANCE, InsideTemplateLayout.INSTANCE);
	}
}

