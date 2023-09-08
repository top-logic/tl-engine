/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.util;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.AbstractMappingResourceProvider;
import com.top_logic.model.resources.TLTypePartResourceProvider;

/**
 * {@link ResourceProvider} for directly displaying {@link MetaAttributeProvider}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaAttributeProviderLabels extends AbstractMappingResourceProvider<MetaAttributeProviderLabels.Config> {

	/**
	 * Typed configuration interface definition for {@link MetaAttributeProviderLabels}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractMappingResourceProvider.Config<MetaAttributeProviderLabels> {

		@Override
		@InstanceDefault(TLTypePartResourceProvider.class)
		ResourceProvider getTargetResourceProvider();

	}

	/**
	 * Create a {@link MetaAttributeProviderLabels}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public MetaAttributeProviderLabels(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object mapValue(Object object) {
		return ((MetaAttributeProvider) object).get();
	}

}
