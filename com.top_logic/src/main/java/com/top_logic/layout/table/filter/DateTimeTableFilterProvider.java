/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * {@link ComparableTableFilterProvider} for {@link Date} values where the time value is
 * significant.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateTimeTableFilterProvider extends ComparableTableFilterProvider
		implements FilterComparator, FilterFieldProvider {

	private static final List<Class<?>> DATE_TYPE = Collections.singletonList(Date.class);

	/**
	 * Creates a {@link DateTimeTableFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DateTimeTableFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
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
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (o2 == null) {
				return -1;
			} else {
				return ((Date) o1).compareTo((Date) o2);
			}
		}
	}

	@Override
	public FormField createField(String name, Object value) {
		DateTimeField result = new DateTimeField(name, (Date) value, false);
		return result;
	}

}
