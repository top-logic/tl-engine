/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.DirectLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * {@link SecurityObjectProvider} that uses a configured model as security object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class ConfiguredModelSecurityProvider extends AbstractConfiguredInstance<ConfiguredModelSecurityProvider.Config>
		implements SecurityObjectProvider {

	/**
	 * Typed configuration interface definition for {@link ConfiguredModelSecurityProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<ConfiguredModelSecurityProvider> {

		/** Configuration name for the value {@link #getModel()}. */
		String MODEL = "model";

		/**
		 * Model which is used as security object.
		 */
		@Mandatory
		@Name(MODEL)
		@ImplementationClassDefault(DirectLinking.class)
		ModelSpec getModel();

		/**
		 * Setter for {@link #getModel()}.
		 */
		void setModel(ModelSpec model);
	}

	/**
	 * Create a {@link ConfiguredModelSecurityProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ConfiguredModelSecurityProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		Object securityObject = ChannelLinking.eval((LayoutComponent) aChecker, getConfig().getModel());
		if (securityObject instanceof BoundObject) {
			return (BoundObject) securityObject;
		}
		return null;
	}

}

