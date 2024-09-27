/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.tabbar.TabInfo.TabConfig;
import com.top_logic.layout.tabbar.TabSwitchCommandModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link TabSwitchCommandModelConfiguration} creates a view to a
 * {@link TabSwitchCommandModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TabSwitchCommandModelConfiguration<C extends TabSwitchCommandModelConfiguration.Config<?>>
		extends AbstractConfiguredInstance<C> implements CommandModelConfiguration {

	/**
	 * Configuration options for {@link TabSwitchCommandModelConfiguration}.
	 */
	public interface Config<I extends TabSwitchCommandModelConfiguration<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Name of {@link #getTabName()}
		 */
		String XML_ATTRIBUTE_TABNAME = "tabName";

		/**
		 * Tab name the command switch to.
		 */
		@Name(XML_ATTRIBUTE_TABNAME)
		@Mandatory
		String getTabName();

	}
	
	private String tabName;
	
	/**
	 * Creates a {@link TabSwitchCommandModelConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TabSwitchCommandModelConfiguration(InstantiationContext context, C config) {
		super(context, config);

		tabName = config.getTabName();
	}

	@Override
	public CommandModel createCommandModel(LayoutComponent component) {
		TabComponent tabbar = (TabComponent) component;

		for (LayoutComponent child : tabbar.getChildList()) {
			if (tabName.equals(child.getName().localName())) {
				return createTabSwitchCommand(tabbar, child);
			}
		}

		return null;
	}

	private CommandModel createTabSwitchCommand(TabComponent tabbar, LayoutComponent child) {
		TabSwitchCommandModel command = TabSwitchCommandModel.newInstance(tabbar, tabName);

		applyTabInfo(command, child.getConfig().getTabInfo());
		
		return command;
	}

	private void applyTabInfo(TabSwitchCommandModel command, TabConfig tabInfo) {
		command.setImage(tabInfo.getImage());
		ResKey label = tabInfo.getLabel();
		command.setLabel(label);
		command.setTooltip(label.tooltipOptional());
		command.setActiveImage(tabInfo.getImageSelected());
	}

}
