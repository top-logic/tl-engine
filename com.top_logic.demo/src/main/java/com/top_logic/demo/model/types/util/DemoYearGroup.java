/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import static com.top_logic.basic.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.demo.model.plain.DemoPlainA;
import com.top_logic.demo.model.types.DemoTypesL;

/**
 * Groups the {@link DemoPlainA} {@link DemoTypesL#getPlainChildren() children} of a
 * {@link DemoTypesL} by the year of their {@link DemoPlainA#getDate() date}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoYearGroup {

	private final DemoTypesL _parent;

	private final Integer _year;

	private final Set<DemoPlainA> _children;

	/**
	 * Groups the children of a {@link DemoTypesL} per year.
	 * 
	 * @param year
	 *        Is allowed to be null, for the children that have no date.
	 */
	private DemoYearGroup(DemoTypesL parent, Integer year, Set<DemoPlainA> children) {
		_parent = parent;
		_year = year;
		_children = new HashSet<>(children);
	}

	/**
	 * The {@link DemoTypesL} for which this {@link DemoYearGroup} was created.
	 */
	public DemoTypesL getParent() {
		return _parent;
	}

	/**
	 * The {@link DemoPlainA#getDate year} of the {@link DemoTypesL#getPlainChildren() children} of
	 * the {@link DemoTypesL}.
	 * 
	 * @return null, if the {@link DemoPlainA} have no date.
	 */
	public Integer getYear() {
		return _year;
	}

	/**
	 * The {@link DemoTypesL#getPlainChildren() children} of the {@link DemoTypesL}.
	 */
	public Set<DemoPlainA> getChildren() {
		return new HashSet<>(_children);
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("parent", getParent())
			.add("year", getYear())
			.add("children", getChildren().size())
			.buildName();
	}

	/**
	 * Creates a {@link Set} of {@link DemoYearGroup} for the children of the {@link DemoTypesL}.
	 */
	public static Set<DemoYearGroup> createFrom(DemoTypesL demoTypesL) {
		List<DemoPlainA> children = dynamicCastView(DemoPlainA.class, demoTypesL.getPlainChildren());
		if (children.isEmpty()) {
			return emptySet();
		}
		Map<Integer, Set<DemoPlainA>> groupedChildren = groupChildren(children);
		Set<DemoYearGroup> result = createFrom(demoTypesL, groupedChildren);
		return result;
	}

	private static Set<DemoYearGroup> createFrom(DemoTypesL demoTypesL, Map<Integer, Set<DemoPlainA>> groupedChildren) {
		Set<DemoYearGroup> result = new HashSet<>();
		for (Map.Entry<Integer, Set<DemoPlainA>> entry : groupedChildren.entrySet()) {
			result.add(new DemoYearGroup(demoTypesL, entry.getKey(), entry.getValue()));
		}
		return result;
	}

	private static Map<Integer, Set<DemoPlainA>> groupChildren(List<DemoPlainA> children) {
		Map<Integer, Set<DemoPlainA>> groupedChildren = new HashMap<>();
		for (DemoPlainA child : children) {
			Integer year = getYear(child);
			if (!groupedChildren.containsKey(year)) {
				groupedChildren.put(year, new HashSet<>());
			}
			groupedChildren.get(year).add(child);
		}
		return groupedChildren;
	}

	private static Integer getYear(DemoPlainA demoPlainA) {
		Date date = demoPlainA.getDate();
		if (date == null) {
			return null;
		}
		return getYear(date);
	}

	private static int getYear(Date date) {
		return CalendarUtil.createCalendar(date).get(Calendar.YEAR);
	}

}