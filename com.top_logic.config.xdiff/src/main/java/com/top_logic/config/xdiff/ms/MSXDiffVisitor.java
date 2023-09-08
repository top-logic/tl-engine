/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

/**
 * Visitor of {@link MSXDiff} nodes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MSXDiffVisitor<R, A> {

	/**
	 * Visits a {@link NodeDelete}.
	 */
	R visitNodeDelete(NodeDelete diff, A arg);

	/**
	 * Visits a {@link ComplexNodeAdd}.
	 */
	R visitComplexNodeAdd(ComplexNodeAdd diff, A arg);

	/**
	 * Visits a {@link TextNodeAdd}.
	 */
	R visitTextNodeAdd(TextNodeAdd diff, A arg);

	/**
	 * Visits a {@link NodeValue}.
	 */
	R visitNodeValue(NodeValue diff, A arg);

	/**
	 * Visits a {@link AttributeSet}.
	 */
	R visitAttributeSet(AttributeSet diff, A arg);

}
