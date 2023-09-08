/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.xpath;

/**
 * Visitor interface for {@link XPathNode}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface XPathVisitor<R, A> {

	/**
	 * Visit case for {@link LocationPath}s.
	 */
	R visitLocationPath(LocationPath path, A arg);

	/**
	 * Visit case for {@link Step}s.
	 */
	R visitStep(Step path, A arg);

	/**
	 * Visit case for {@link NameTest}s.
	 */
	R visitNameTest(NameTest path, A arg);

	/**
	 * Visit case for {@link TypeTest}s.
	 */
	R visitTypeTest(TypeTest path, A arg);

}
