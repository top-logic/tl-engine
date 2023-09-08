/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.DateUtil;

/**
 * A {@link FilterComparator} for {@link Date}s.
 * 
 * <p>
 * <b>Note:</b>
 * Equality is based on day range.
 * </p>
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class DateComparator implements FilterComparator {

	private static final List<Class<?>> DATE_TYPE = Collections.singletonList(Date.class);

	public static DateComparator INSTANCE = new DateComparator(1);

	public static DateComparator INSTANCE_DESCENDING = new DateComparator(-1);

	private final int direction;
	
	/**
	 * Singleton
	 */
	private DateComparator(int aDirection) {
		this.direction = aDirection;
	}
	
	/**
	 * Getter for singleton instance of date comparator
	 * 
	 * @return instance of date comparator
	 */
	public static DateComparator getInstance() {
		return INSTANCE;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjectTypes() {
		return DATE_TYPE;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Object o1, Object o2) {
		int result = DateUtil.compareDatesByDay((Date) o1, (Date) o2);
		return this.direction * result;
	}
}
