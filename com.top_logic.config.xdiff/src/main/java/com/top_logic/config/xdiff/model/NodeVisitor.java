/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

/**
 * Visitor of the {@link Node} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NodeVisitor<R, A> {

	/**
	 * Visit case for {@link Document} nodes.
	 */
	R visitDocument(Document node, A arg);

	/**
	 * Visit case for {@link Element} nodes.
	 */
	R visitElement(Element node, A arg);

	/**
	 * Visit case for {@link Text} nodes.
	 */
	R visitText(Text node, A arg);

	/**
	 * Visit case for {@link CData} nodes.
	 */
	R visitCData(CData node, A arg);

	/**
	 * Visit case for {@link Fragment} nodes.
	 */
	R visitFragment(Fragment node, A arg);

	/**
	 * Visit case for {@link Comment} nodes.
	 */
	R visitComment(Comment node, A arg);

	/**
	 * Visit case for {@link EntityReference} nodes.
	 */
	R visitEntityReference(EntityReference node, A arg);

}
