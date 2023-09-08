/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.LabelProvider;

/**
 * A {@link Page} is a representation of a section of a list of elements.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
interface Page extends Filter<Object>, Comparable<Page> {

	public static final LabelProvider PAGE_LABEL_PROVIDER = new LabelProvider() {

		@Override
		public String getLabel(Object object) {
			return ((Page) object).getLabel();
		}
	};

	/** Representation of an empty page. */
	public static final Page EMPTY_PAGE = new Page() {

		@Override
		public int getStartIndex() {
			return 0;
		}

		@Override
		public int getSize() {
			return 0;
		}

		@Override
		public boolean contains(Object option) {
			return false;
		}
		
		@Override
		public String getLabel() {
			return "---";
		}

		@Override
		public boolean accept(Object anObject) {
			return false;
		}

		@Override
		public int compareTo(Page o) {
			return DefaultPage.compare(this, o);
		}

	};

	/**
	 * Number of elements contained in this page
	 */
	int getSize();

	/**
	 * Index of the first element in this page in the list which was used to create this page.
	 */
	int getStartIndex();

	/**
	 * Whether the given element is contained in this {@link Page}
	 * 
	 * @param option
	 *        element in the list which was used to create this page
	 */
	boolean contains(Object option);

	/**
	 * <code>true</code> iff {@link #contains(Object)} results in <code>true</code>.
	 * 
	 * @see com.top_logic.basic.col.Filter#accept(java.lang.Object)
	 */
	@Override
	boolean accept(Object anObject);

	/**
	 * Returns the label for this {@link Page}
	 */
	String getLabel();

}
