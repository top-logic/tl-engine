/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.resources;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.inspector.model.nodes.InspectProperty;
import com.top_logic.layout.provider.AbstractMappingResourceProvider;

/**
 * {@link ResourceProvider} for {@link com.top_logic.layout.inspector.model.nodes.InspectProperty}
 * values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InspectPropertyResourceProvider extends AbstractMappingResourceProvider {

	/**
	 * Create a {@link InspectPropertyResourceProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public InspectPropertyResourceProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String getLabel(Object object) {
		return property(object).name();
	}

	@Override
	protected Object mapValue(Object anObject) {
		return property(anObject).getValue();
	}

	private InspectProperty property(Object anObject) {
		return (InspectProperty) anObject;
	}

}
