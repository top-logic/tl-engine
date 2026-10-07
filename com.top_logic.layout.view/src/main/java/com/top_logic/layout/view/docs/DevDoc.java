/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.docs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A node of the developer documentation ({@link DevDocs}): an article, a chapter grouping
 * articles, or the root of the documentation.
 *
 * <p>
 * Two nodes are equal if they have the same {@link #getName() name}, so a node found in a newly
 * loaded documentation stands for the node of the same name found before.
 * </p>
 */
public final class DevDoc {

	private final String _name;

	private final boolean _chapter;

	private String _title;

	private String _description = "";

	private Integer _order;

	private String _text;

	private DevDoc _parent;

	private final List<DevDoc> _children = new ArrayList<>();

	DevDoc(String name, boolean chapter) {
		_name = name;
		_chapter = chapter;
		_title = lastSegment(name);
	}

	/**
	 * The name of the node: the path of the article file relative to {@link DevDocs#DOCS_DIR}
	 * without {@code .md}, the path of the folder for a chapter, and the empty string for the root.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Whether this node is a chapter (a folder of articles) rather than an article.
	 */
	public boolean isChapter() {
		return _chapter;
	}

	/**
	 * The title: the text of the first top-level heading of the article (for a chapter: of its
	 * {@code index.md}), the last segment of the name if there is none.
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * When to read the article, from its front matter; empty if it gives none.
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * The Markdown text without front matter; for a chapter the text of its {@code index.md},
	 * {@code null} if it has none.
	 */
	public String getText() {
		return _text;
	}

	/**
	 * The chapter this node belongs to, {@code null} for the root.
	 */
	public DevDoc getParent() {
		return _parent;
	}

	/**
	 * The chapters and articles of this chapter, ordered by their front matter {@code order}, then
	 * by title. Empty for an article.
	 */
	public List<DevDoc> getChildren() {
		return Collections.unmodifiableList(_children);
	}

	/**
	 * The order key from the front matter, {@code null} if none is given.
	 */
	Integer getOrder() {
		return _order;
	}

	void setContent(String title, String description, Integer order, String text) {
		if (title != null) {
			_title = title;
		}
		_description = description;
		_order = order;
		_text = text;
	}

	void addChild(DevDoc child) {
		child._parent = this;
		_children.add(child);
	}

	void sortChildren() {
		_children.sort((a, b) -> {
			Integer orderA = a.getOrder();
			Integer orderB = b.getOrder();
			if (orderA != null || orderB != null) {
				if (orderA == null) {
					return 1;
				}
				if (orderB == null) {
					return -1;
				}
				int result = Integer.compare(orderA, orderB);
				if (result != 0) {
					return result;
				}
			}
			return String.CASE_INSENSITIVE_ORDER.compare(a.getTitle(), b.getTitle());
		});
		for (DevDoc child : _children) {
			child.sortChildren();
		}
	}

	private static String lastSegment(String name) {
		return name.substring(name.lastIndexOf('/') + 1);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DevDoc other && other._name.equals(_name);
	}

	@Override
	public int hashCode() {
		return _name.hashCode();
	}

	@Override
	public String toString() {
		return _name;
	}

}
