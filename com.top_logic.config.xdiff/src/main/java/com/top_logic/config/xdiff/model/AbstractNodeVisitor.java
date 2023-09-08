/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;


/**
 * {@link NodeVisitor} with fallback cases.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractNodeVisitor<R, A> implements NodeVisitor<R, A> {

	@Override
	public R visitDocument(Document node, A arg) {
		return visitFragmentBase(node, arg);
	}

	@Override
	public R visitElement(Element node, A arg) {
		return visitFragmentBase(node, arg);
	}

	@Override
	public R visitFragment(Fragment node, A arg) {
		return visitFragmentBase(node, arg);
	}

	@Override
	public R visitText(Text node, A arg) {
		return visitNode(node, arg);
	}

	@Override
	public R visitCData(CData node, A arg) {
		return visitText(node, arg);
	}

	@Override
	public R visitComment(Comment node, A arg) {
		return visitNode(node, arg);
	}

	@Override
	public R visitEntityReference(EntityReference node, A arg) {
		return visitNode(node, arg);
	}

	/**
	 * Common case for {@link Document}, {@link Fragment}, and {@link Element}.
	 */
	protected R visitFragmentBase(FragmentBase node, A arg) {
		return visitNode(node, arg);
	}

	/**
	 * Common case for all {@link Node}s.
	 */
	protected abstract R visitNode(Node node, A arg);

}
