/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider.path;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.SharedInstance;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;

/**
 * {@link IntermediatePath} delegating to a component identified by its name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SharedInstance
public final class Component extends IntermediatePath {

	/**
	 * Configuration of a {@link Component}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(PathSecurityObjectProvider.PATH_ELEMENT_COMPONENT)
	public interface Config extends PolymorphicConfiguration<Component> {

		/**
		 * The name of the component to step to.
		 */
		/* Note: This interface is not a com.top_logic.mig.html.layout.ComponentReference to avoid
		 * tag name clash with
		 * com.top_logic.layout.form.component.PostCreateAction$SetModel$Step$Component$Config#
		 * TAG_NAME */
		@Mandatory
		ComponentName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(ComponentName name);

	}

	private Config _config;

	/**
	 * Create a {@link Component}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public Component(InstantiationContext context, Component.Config config) {
		_config = config;
	}

	@Override
	public LayoutComponent nextComponent(LayoutComponent component, int pathIndex, int size) {
		return component.getMainLayout().getComponentByName(getConfig().getName());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}
