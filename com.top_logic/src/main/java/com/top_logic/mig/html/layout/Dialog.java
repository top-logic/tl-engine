/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.knowledge.gui.layout.ButtonComponent.Config;
import com.top_logic.layout.ResPrefix;
import com.top_logic.tool.boundsec.BoundLayout;

/**
 * Helper class to create Dialogs.
 *  
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public abstract class Dialog {

    /**
	 * TODO KBU this functions creates a LayoutComponent to ???
	 * 
	 * KHA: broken since I have no idea what this is needed for
	 * 
	 * @param context
	 *        Context for configuration instantiations.
	 * @param dialogInfo
	 *        The {@link DialogInfo} for the result component.
	 * @return A {@link LayoutComponent}.
	 */
	private static LayoutComponent createDefaultComponent(InstantiationContext context, ComponentName layoutName,
			ResPrefix aResPrefix, Class aComponentClass, DialogInfo dialogInfo)
			throws ConfigurationException {
		BoundLayout.Config layoutConfig = TypedConfiguration.newConfigItem(BoundLayout.Config.class);
		layoutConfig.setName(layoutName);
		layoutConfig.setDialogInfo(dialogInfo);
		BoundLayout theLayout = new BoundLayout(context, layoutConfig);
    
		LayoutComponent theComponent =
			createInnerComponent(context, aComponentClass, layoutName.append(".comp"), aResPrefix);
    
		Config buttonConfig = TypedConfiguration.newConfigItem(ButtonComponent.Config.class);
		buttonConfig.setName(layoutName.append(".button"));
		ButtonComponent theButtons = new ButtonComponent(context, buttonConfig);
    
		theComponent.setButtonComponent(theButtons);
    
		theLayout.addComponent(theComponent);
		theLayout.addComponent(theButtons);
        
        return theLayout;        
    }

    /**
	 * Create a LayoutComponent for a dialog not defined in the layout xml
	 * 
	 * @return A {@link LayoutComponent}.
	 * @see #createDefaultComponent(InstantiationContext, ComponentName, ResPrefix, Class, DialogInfo)
	 */
	private static LayoutComponent createInnerComponent(InstantiationContext context, Class aCompClass, ComponentName name,
			ResPrefix aResPrefix) throws ConfigurationException {
        Factory factory = DefaultConfigConstructorScheme.getFactory(aCompClass);
		Class<? extends LayoutComponent.Config> configurationInterface =
			(Class<? extends LayoutComponent.Config>) factory.getConfigurationInterface();
		LayoutComponent.Config config = TypedConfiguration.newConfigItem(configurationInterface);
		config.setImplementationClass(aCompClass);
		config.setName(name);
		config.setResPrefix(aResPrefix);
		return (LayoutComponent) factory.createInstance(context, config);
    }
}
