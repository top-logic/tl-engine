/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.xml.LayoutControlComponent;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * Generic component that can be displayed using a {@link LayoutControlProvider}.
 * 
 * <p>
 * In contrast to a {@link LayoutControlComponent}, this component takes part in access checks as
 * other regular components.
 * </p>
 * 
 * @see Config#getComponentControlProvider()
 * @see LayoutControlComponent
 */
public class DefaultComponent extends BoundComponent {

	/**
	 * Configuration options for {@link DefaultComponent}.
	 */
	@TagName("defaultComponent")
	public interface Config extends BoundComponent.Config {

		@Override
		@ClassDefault(DefaultComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

	}

	/**
	 * Creates a {@link DefaultComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

}
