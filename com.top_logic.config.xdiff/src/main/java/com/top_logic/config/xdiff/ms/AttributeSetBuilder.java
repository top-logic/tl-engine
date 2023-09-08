/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;


import org.w3c.dom.Element;

import com.top_logic.config.xdiff.util.Utils;

/**
 * {@link MSXDiffElementParser} that creates {@link AttributeSet}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeSetBuilder implements MSXDiffElementParser {

	/**
	 * Singleton {@link AttributeSetBuilder} instance.
	 */
	public static final AttributeSetBuilder INSTANCE = new AttributeSetBuilder();

	private AttributeSetBuilder() {
		// Singleton constructor.
	}

	@Override
	public MSXDiff build(Element operationSpec) {
		Utils.checkEmpty(operationSpec);
		Utils.checkAttributes(MSXDiffSchema.ATTRIBUTE_SET_ATTRIBUTES, operationSpec);

		String component = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.ATTRIBUTE_SET__COMPONENT_ATTRIBUTE);
		String elementXpath = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.ATTRIBUTE_SET__XPATH_ATTRIBUTE);
		String attributeName = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.ATTRIBUTE_SET__NAME_ATTRIBUTE);
		String attributeValue = Utils.getAttributeOrNull(operationSpec, MSXDiffSchema.ATTRIBUTE_SET__VALUE_ATTRIBUTE);

		return new AttributeSet(component, elementXpath, attributeName, attributeValue);
	}

}
