/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.text.Format;
import java.util.Date;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * {@link TableFilterProvider} for {@link Date}-valued columns where the values represent whole days
 * (without time).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateTableFilterProvider extends ComparableTableFilterProvider {

	/**
	 * Creates a {@link DateTableFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DateTableFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
		checkNoLiteralFormat(context, config);
	}

	private void checkNoLiteralFormat(InstantiationContext context, Config config) {
		if (!StringServices.isEmpty(config.getFormat())) {
			context.error("Literal format is not allowed for date table filter in '"
				+ config.location().toString()
				+ "'. Use format references instead.");
		}
	}

	/**
	 * Creates a {@link DateTableFilterProvider}.
	 * 
	 * @param mandatory
	 *        Whether the column value cannot be empty.
	 */
	public DateTableFilterProvider(boolean mandatory) {
		super(mandatory, AllOperatorsProvider.INSTANCE);
	}

	@Override
	protected FilterFieldProvider createFieldProvider(ColumnConfiguration column) {
		Format format = getFormatProvider().getFormat(column);
		if (format == null) {
			return DateFieldProvider.INSTANCE;
		} else {
			return new DateFieldProvider(format);
		}
	}

	@Override
	protected FilterComparator getComparator() {
		return DateComparator.getInstance();
	}

}
