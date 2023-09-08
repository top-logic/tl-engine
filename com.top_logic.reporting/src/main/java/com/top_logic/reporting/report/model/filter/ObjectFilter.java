/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.NumberComparator;

/**
 * The ObjectFilter accepts objects that are included in a specified interval. It can
 * handle Number, Date or String objects.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class ObjectFilter implements Filter {

	private Object begin;
	private boolean includeBegin;
	private Object end;
	private boolean includeEnd;

	/**
	 * Creates a {@link ObjectFilter}.
	 */
	public ObjectFilter(Object aBegin, boolean includeBegin, Object anEnd, boolean includeEnd) {
		this.begin = aBegin;
		this.includeBegin = includeBegin;
		this.end = anEnd;
		this.includeEnd = includeEnd;
	}
	
	public ObjectFilter(Object aBegin, Object anEnd) {
		this(aBegin, true, anEnd, true);
	}

	@Override
	public boolean accept( Object anObject ) {

		boolean result = true;
		if (this.includeBegin) {
			result = result && (greaterEqualThan(anObject, begin));
		}
		else {
			result = result && (greaterThan(anObject, begin));
		}

		if (this.includeEnd) {
			result = result && (!greaterThan(anObject, end));
		}
		else {
			result = result && (!greaterEqualThan(anObject, end));
		}

		return result;
	}

	private boolean greaterThan( Object o1, Object o2 ) {
		if ((o1 instanceof Number) && (o2 instanceof Number)) {
			return (NumberComparator.INSTANCE.compare((Number) o1, (Number) o2) > 0);
		}
		else if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
			return ((Comparable) o1).compareTo(o2) > 0;
		}
		else if (o1 == null) {
			return (o2 == null);
		}
		else if (o2 == null) {
			return false;
		}
		else {
			throw new IllegalArgumentException("Given objects are not comparable.");
		}
	}

	private boolean greaterEqualThan( Object o1, Object o2 ) {
		if ((o1 instanceof Number) && (o2 instanceof Number)) {
			return (NumberComparator.INSTANCE.compare((Number) o1, (Number) o2) >= 0);
		}
		else if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
			return ((Comparable) o1).compareTo(o2) >= 0;
		}
		else if (o1 == null) {
			return (o2 == null);
		}
		else if (o2 == null) {
			return false;
		}
		else {
			throw new IllegalArgumentException("Given objects are not comparable.");
		}
	}
}
