/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.Comparator;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLSortOrder;

/**
 * A {@link Comparator} for comparing {@link TLStructuredTypePart}s by their {@link TLSortOrder}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class TLStructuredTypePartComparator implements Comparator<TLStructuredTypePart> {

	/**
	 * The instance of the {@link TLStructuredTypePartComparator}.
	 */
	public static final TLStructuredTypePartComparator INSTANCE = new TLStructuredTypePartComparator();

	@Override
	public int compare(TLStructuredTypePart left, TLStructuredTypePart right) {
		double sortOrderLeft = DisplayAnnotations.getSortOrder(left);
		double sortOrderRight = DisplayAnnotations.getSortOrder(right);
		return Double.compare(sortOrderLeft, sortOrderRight);
	}

}
