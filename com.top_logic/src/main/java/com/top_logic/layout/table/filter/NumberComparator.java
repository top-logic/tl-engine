/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.LinkedList;
import java.util.List;

import com.top_logic.knowledge.wrap.currency.Amount;

/**
 * A {@link FilterComparator} for {@link Number}s and {@link Amount}s.
 * 
 * <p>
 * <b>Note:</b>
 * Comparison is based on float precision (7 digits).
 * </p>
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class NumberComparator implements FilterComparator {

	public static NumberComparator INSTANCE = new NumberComparator(1);

	public static NumberComparator INSTANCE_DESCENDING = new NumberComparator(-1);

	private final int direction;
	
	/**
	 *	Singleton
	 */
	private NumberComparator(int aDirection) {
		direction = aDirection;
	}
	
	/**
	 * Getter for singleton instance of number comparator
	 * 
	 * @return instance of number comparator
	 */
	public static NumberComparator getInstance() {
		return INSTANCE;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjectTypes() {
		List<Class<?>> supportedTypes = new LinkedList<>();
		supportedTypes.add(Number.class);
		supportedTypes.add(Amount.class);
		return supportedTypes;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Object o1, Object o2) {
		
		int result = 0;
		
		/*
		 *  Conversion of o1 and o2 to float
		 */
		float firstParameter = 0f;
		float secondParameter = 0f;
		
		if(o1 instanceof Number) {
			firstParameter = ((Number)o1).floatValue();
		}
		else if(o1 != null) {
			firstParameter = (float)((Amount)o1).getValue();
		}
		
		if(o2 instanceof Number) {
			secondParameter = ((Number)o2).floatValue();
		}
		else if(o2 != null) {
			secondParameter = (float)((Amount)o2).getValue();
		}
		
		/*
		 *	Comparison 
		 */
		if(firstParameter > secondParameter) {
			result = 1;
		}
		else if(firstParameter < secondParameter) {
			result = -1;
		}

		return this.direction * result;
	}
}
