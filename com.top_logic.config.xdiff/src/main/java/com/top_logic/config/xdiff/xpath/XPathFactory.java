/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.xpath;

import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.model.QName;

/**
 * Factory for {@link XPathNode}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XPathFactory {

	/**
	 * Creates a new {@link LocationPath}.
	 * 
	 * @param absolute
	 *        See {@link LocationPath#isAbsolute()}
	 */
	public static LocationPath locationPath(boolean absolute) {
		return new LocationPath(absolute);
	}

	/**
	 * Creates a new {@link NameTest}.
	 * 
	 * @param name
	 *        See {@link NameTest#getName()}.
	 */
	public static NameTest nameTest(QName name) {
		return new NameTest(name);
	}

	/**
	 * Creates a new {@link Step}.
	 * 
	 * @param axis
	 *        See {@link Step#getAxis()}.
	 * @param nodeTest
	 *        See {@link Step#getNodeTest()}.
	 * @param position
	 *        See {@link Step#getPosition()}.
	 */
	public static Step step(Axis axis, NodeTest nodeTest, int position) {
		return new Step(axis, nodeTest, position);
	}

	/**
	 * Creates a new {@link TypeTest}.
	 * 
	 * @param type
	 *        See {@link TypeTest#getType()}.
	 */
	public static TypeTest typeTest(NodeType type) {
		return new TypeTest(type);
	}

}
