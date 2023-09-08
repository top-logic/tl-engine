/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.resources;

import static com.top_logic.layout.inspector.resources.InspectorNodeResourceProvider.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.inspector.InspectorComponent;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;
import com.top_logic.layout.provider.AbstractMappingResourceProvider;

/**
 * {@link ResourceProvider} for the <code>value</code> column of the inspector.
 * 
 * @see InspectorComponent
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InspectorValueResourceProvider extends AbstractMappingResourceProvider {

	/**
	 * Create a {@link InspectorValueResourceProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public InspectorValueResourceProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String getLabel(Object object) {
		InspectorTreeNode node = node(object);
		Object value = node.getBusinessObject();
		return InspectorNodeResourceProvider.toString(node, value);
	}

	@Override
	protected Object mapValue(Object anObject) {
		return node(anObject).getBusinessObject();
	}

}
