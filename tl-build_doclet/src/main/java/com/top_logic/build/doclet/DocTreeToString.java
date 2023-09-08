/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.util.Collection;
import java.util.Iterator;

import com.sun.source.doctree.DocTree;
import com.sun.source.util.SimpleDocTreeVisitor;

/**
 * Visitor serializing a {@link DocTree}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DocTreeToString extends SimpleDocTreeVisitor<Void, StringBuilder> {

	/** Singleton {@link DocTreeToString} instance. */
	public static final DocTreeToString INSTANCE = new DocTreeToString();

	/**
	 * Creates a new {@link DocTreeToString}.
	 */
	protected DocTreeToString() {
		// singleton instance
	}

	/**
	 * Serializes the given {@link DocTree}.
	 */
	public static String toString(DocTree doc) {
		StringBuilder toString = new StringBuilder();
		doc.accept(INSTANCE, toString);
		return toString.toString();
	}

	/**
	 * Service method to serializes a sequence of {@link DocTree}s.
	 */
	public static String toString(Collection<? extends DocTree> docs) {
		switch (docs.size()) {
			case 0:
				return "";
			case 1:
				return toString(docs.iterator().next());
			default: {
				StringBuilder toString = new StringBuilder();
				Iterator<? extends DocTree> it = docs.iterator();
				it.next().accept(INSTANCE, toString);
				while (it.hasNext()) {
					it.next().accept(INSTANCE, toString);
				}
				return toString.toString();
			}
		}
	}

	@Override
	protected Void defaultAction(DocTree node, StringBuilder p) {
		p.append(node.toString());
		return null;
	}

}

