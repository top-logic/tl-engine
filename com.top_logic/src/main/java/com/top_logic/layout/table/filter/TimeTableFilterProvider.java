/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TimeComparator.SystemTimeComparator;
import com.top_logic.basic.col.TimeComparator.UserTimeComparator;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * {@link ComparableTableFilterProvider} for {@link Date} values where only the time value is
 * displayed.
 * 
 * @see DateTimeTableFilterProvider
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TimeTableFilterProvider extends ComparableTableFilterProvider
		implements FilterComparator, FilterFieldProvider {

	private static final List<Class<?>> DATE_TYPE = Collections.singletonList(Date.class);

	/**
	 * Typed configuration interface definition for {@link TimeTableFilterProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ComparableTableFilterProvider.Config {

		/**
		 * Whether the times are compared in system or in the user calendar.
		 * 
		 * @see CalendarUtil
		 */
		boolean isCompareInSystemCalendar();

	}

	private final Comparator<Date> _timeComparator;

	/**
	 * Creates a {@link TimeTableFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TimeTableFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
		_timeComparator =
			config.isCompareInSystemCalendar() ? SystemTimeComparator.INSTANCE : UserTimeComparator.INSTANCE;
	}

	@Override
	protected FilterComparator getComparator() {
		return this;
	}

	@Override
	protected FilterFieldProvider createFieldProvider(ColumnConfiguration column) {
		return this;
	}

	@Override
	public List<Class<?>> getSupportedObjectTypes() {
		return DATE_TYPE;
	}

	@Override
	public int compare(Object o1, Object o2) {
		return _timeComparator.compare((Date) o1, (Date) o2);
	}

	@Override
	public FormField createField(String name, Object value) {
		return FormFactory.newTimeField(name, value, !FormFactory.IMMUTABLE);
	}

}
