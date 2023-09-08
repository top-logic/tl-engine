/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import org.w3c.dom.Element;

import com.top_logic.config.xdiff.util.Utils;

/**
 * {@link MSXDiffElementParser} that creates {@link NodeDelete}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NodeDeleteBuilder implements MSXDiffElementParser {

	/**
	 * Singleton {@link NodeDeleteBuilder} instance.
	 */
	public static final NodeDeleteBuilder INSTANCE = new NodeDeleteBuilder();

	private NodeDeleteBuilder() {
		// Singleton constructor.
	}

	@Override
	public MSXDiff build(Element operationSpec) {
		Utils.checkEmpty(operationSpec);
		Utils.checkAttributes(MSXDiffSchema.NODE_DELETE_ATTRIBUTES, operationSpec);

		String component = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.NODE_DELETE__COMPONENT_ATTRIBUTE);
		String xpath = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.NODE_DELETE__XPATH_ATTRIBUTE);

		return new NodeDelete(component, xpath);
	}

}
