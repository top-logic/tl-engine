/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;


import org.w3c.dom.Element;

import com.top_logic.config.xdiff.util.Utils;

/**
 * {@link MSXDiffElementParser} that creates {@link NodeAdd}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NodeAddBuilder implements MSXDiffElementParser {

	/**
	 * Singleton {@link NodeAddBuilder} instance.
	 */
	public static final NodeAddBuilder INSTANCE = new NodeAddBuilder();

	private NodeAddBuilder() {
		// Singleton constructor.
	}

	@Override
	public MSXDiff build(Element operationSpec) {
		Utils.checkAttributes(MSXDiffSchema.NODE_ADD_ATTRIBUTES, operationSpec);

		String belowXpath = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.NODE_ADD__XPATH_ATTRIBUTE);
		String component = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.NODE_ADD__COMPONENT_ATTRIBUTE);
		String beforeXpath = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.NODE_ADD__BEFORE_XPATH_ATTRIBUTE);
		String nodeName = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.NODE_ADD__NODE_NAME_ATTRIBUTE);

		boolean complexAdd = nodeName == null;
		if (complexAdd) {
			return new ComplexNodeAdd(component, belowXpath, beforeXpath, operationSpec);
		} else {
			Utils.checkEmpty(operationSpec);

			String nodeText = operationSpec.getAttributeNS(null, MSXDiffSchema.NODE_ADD__NODE_TEXT_ATTRIBUTE);

			return new TextNodeAdd(component, belowXpath, beforeXpath, nodeName, nodeText);
		}
	}

}
