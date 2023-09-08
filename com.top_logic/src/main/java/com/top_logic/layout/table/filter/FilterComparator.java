/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Comparator;
import java.util.List;

/**
 * An common interface, to allow {@link ConfiguredFilter}s comparisons between their {@link FilterConfiguration}
 * and filterable objects.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public interface FilterComparator extends Comparator<Object> {
	
	/**
	 * This method determines the object types, which can be handled by this comparator.
	 * 
	 * <p>
	 * <b>Note:</b>
	 * In case the comparator is used to handle unsupported object types {@link RuntimeException} may occur.
	 * </p>
	 * 
	 * @return list of supported object types.
	 */
	public List<Class<?>> getSupportedObjectTypes();
}
