/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;


import org.w3c.dom.Element;

import com.top_logic.config.xdiff.util.Utils;

/**
 * {@link MSXDiffElementParser} that creates {@link NodeValue}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NodeValueBuilder implements MSXDiffElementParser {

	/**
	 * Singleton {@link NodeValueBuilder} instance.
	 */
	public static final NodeValueBuilder INSTANCE = new NodeValueBuilder();

	private NodeValueBuilder() {
		// Singleton constructor.
	}

	@Override
	public MSXDiff build(Element operationSpec) {
		Utils.checkEmpty(operationSpec);
		Utils.checkAttributes(MSXDiffSchema.NODE_VALUE_ATTRIBUTES, operationSpec);

		String component = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.NODE_VALUE__COMPONENT_ATTRIBUTE);
		String xpath = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.NODE_VALUE__XPATH_ATTRIBUTE);
		String value = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.NODE_VALUE__VALUE_ATTRIBUTE);

		return new NodeValue(component, xpath, value);
	}

}
