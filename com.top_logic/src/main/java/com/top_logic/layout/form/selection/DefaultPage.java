/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.Comparator;

import com.top_logic.layout.LabelProvider;

/**
 * Page in a large selection dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class DefaultPage implements Page {

	/** @see #getStartIndex() */
	private final int startIndex;

	/** @see #getSize() */
	private final int size;

	/** First element of the page */
	private final Object first;

	/** Last element in the page */
	private final Object last;

	/**
	 * Comparator to decide whether an object is contained in this page.
	 * 
	 * @see #contains(Object)
	 */
	private final Comparator<Object> comparator;

	/** {@link #toString() String} representation of this page. */
	private final String label;

	public DefaultPage(int startIndex, int size, Object first, Object last, Comparator<Object> comparator, LabelProvider labels) {
		if (first == null) {
			throw new IllegalArgumentException("The first object must not be null");
		}
		if (last == null) {
			throw new IllegalArgumentException("The last object must not be null");
		}
		if (labels == null) {
			throw new NullPointerException("'labels' must not be 'null'.");
		}
		if (comparator == null) {
			throw new NullPointerException("'comparator' must not be 'null'.");
		}
		if (comparator.compare(last, first) < 0) {
			throw new IllegalArgumentException("First element must not be larger than the last element.");
		}
		this.startIndex = startIndex;
		this.size = size;
		this.first = first;
		this.last = last;
		this.comparator = comparator;
		this.label = createLabel(labels);
	}

	private String createLabel(LabelProvider labels) {
		return labels.getLabel(this.first) + " - " + labels.getLabel(this.last);
	}

	/**
	 * @see com.top_logic.layout.form.selection.Page#getSize()
	 */
	@Override
	public int getSize() {
		return size;
	}

	/**
	 * @see com.top_logic.layout.form.selection.Page#getStartIndex()
	 */
	@Override
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * @see com.top_logic.layout.form.selection.Page#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object option) {
		return comparator.compare(first, option) <= 0 && comparator.compare(option, last) <= 0;
	}


	@Override
	public boolean accept(Object anObject) {
		return contains(anObject);
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + first.hashCode();
		result = prime * result + last.hashCode();
		result = prime * result + size;
		result = prime * result + startIndex;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultPage other = (DefaultPage) obj;
		if (!first.equals(other.first))
			return false;
		if (!last.equals(other.last))
			return false;
		if (size != other.size)
			return false;
		if (startIndex != other.startIndex)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public int compareTo(Page o) {
		return compare(this, o);
	}

	static int compare(Page page1, Page page2) {
		if (page1 == page2) {
			return 0;
		}
		if (page1 == EMPTY_PAGE) {
			return -1;
		}
		if (page2 == EMPTY_PAGE) {
			return 1;
		}
		DefaultPage firstDefaultPage = (DefaultPage) page1;
		DefaultPage secondDefaultPage = (DefaultPage) page2;
		assert firstDefaultPage.comparator.equals(secondDefaultPage.comparator) : "Pages with different comparators.";
		return firstDefaultPage.comparator.compare(firstDefaultPage.first, secondDefaultPage.first);
	}

}
