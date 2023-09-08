/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import static com.top_logic.config.xdiff.ms.MSXDiffSchema.*;

import java.util.Map;

import org.w3c.dom.Element;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.config.xdiff.XApplyException;

/**
 * Parser that creates {@link MSXDiff} implementations from their corresponding DOM representations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MSXDiffParser {

	private static final Map<String, MSXDiffElementParser> BUILDERS =
	new MapBuilder<String, MSXDiffElementParser>()
		.put(NODE_DELETE_ELEMENT, NodeDeleteBuilder.INSTANCE)
		.put(NODE_ADD_ELEMENT, NodeAddBuilder.INSTANCE)
		.put(NODE_VALUE_ELEMENT, NodeValueBuilder.INSTANCE)
		.put(ATTRIBUTE_SET_ELEMENT, AttributeSetBuilder.INSTANCE)
		.toMap();

	/**
	 * Parse the given {@link Element} into a {@link MSXDiff} instance.
	 */
	public static MSXDiff parseElement(Element operationSpec) throws XApplyException {
		String operationName = operationSpec.getLocalName();
		MSXDiffElementParser builder = MSXDiffParser.BUILDERS.get(operationName);
		if (builder == null) {
			throw new XApplyException("Unsupported operation element '" + operationName + "'");
		}
		MSXDiff operation = builder.build(operationSpec);
		return operation;
	}

}
