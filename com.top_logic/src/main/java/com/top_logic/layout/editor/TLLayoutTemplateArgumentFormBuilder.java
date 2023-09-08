/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.editor.AbstractComponentConfigurationDialogBuilder.GlobalConfig;
import com.top_logic.layout.editor.ComponentConfigurationDialogBuilder.GlobalEditConfig;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutTemplateCall;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * Builds the {@link FormContext} for the template arguments of a {@link TLLayout}.
 * 
 * @see LayoutTemplateCall
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLLayoutTemplateArgumentFormBuilder extends TLLayoutFormBuilder {

	/**
	 * Configuration of {@link TLLayoutTemplateArgumentFormBuilder}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface Config extends TLLayoutFormBuilder.Config {

		/**
		 * @see com.top_logic.basic.reflect.DefaultMethodInvoker
		 */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default Map<String, CustomizationConfig> getCustomizations() {
			Map<String, CustomizationConfig> customizations = new HashMap<>();

			customizations.putAll(ApplicationConfig.getInstance().getConfig(GlobalConfig.class).getCustomizations());
			customizations.putAll(ApplicationConfig.getInstance().getConfig(GlobalEditConfig.class).getCustomizations());

			return customizations;
		}

	}
	
	/**
	 * Creates a {@link TLLayoutTemplateArgumentFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLLayoutTemplateArgumentFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Class<? extends TLLayout> getModelType() {
		return LayoutTemplateCall.class;
	}

	@Override
	ConfigurationItem getConfiguration(TLLayout layout) throws ConfigurationException {
		return layout.getArguments();
	}


}
