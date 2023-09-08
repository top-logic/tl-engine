/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import java.text.Format;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.filter.ComparableFilter;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;
import com.top_logic.layout.table.filter.ComparableFilterViewBuilder;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.FilterFieldProvider;
import com.top_logic.layout.table.filter.FloatValueOperatorsProvider;
import com.top_logic.layout.table.filter.NumberComparator;

/**
 * {@link TableFilterProvider} creating a filter for a time display <code>mm:ss,SSS</code> using a
 * {@link Long} nanoseconds value as model.
 * 
 * @see NanoSecondsFormat
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NanoSecondsFilterProvider extends AbstractTableFilterProvider implements FilterFieldProvider {

	/**
	 * Singleton {@link NanoSecondsFilterProvider} instance.
	 */
	public static final NanoSecondsFilterProvider INSTANCE = new NanoSecondsFilterProvider();

	private NanoSecondsFilterProvider() {
		// Singleton constructor.
	}

	/**
	 * Creates a {@link NanoSecondsFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NanoSecondsFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected List<ConfiguredFilter> createFilterList(TableViewModel tableViewModel, String filterPosition) {
		NumberComparator comparator = NumberComparator.getInstance();
		ComparableFilterConfiguration config =
			new ComparableFilterConfiguration(tableViewModel, filterPosition, FloatValueOperatorsProvider.INSTANCE,
				comparator, false, showSeparateOptionEntries());
		config.setOperator(Operators.GREATER_OR_EQUALS);
		ComparableFilterViewBuilder viewBuilder =
			new ComparableFilterViewBuilder(this, getSeparateOptionDisplayThreshold());
		ConfiguredFilter filter =
			new ComparableFilter(config, viewBuilder, showNonMatchingOptions());
		return Collections.singletonList(filter);
	}

	@Override
	public FormField createField(String name, Object value) {
		return FormFactory.newComplexField(name, createFormat(), value, false);
	}

	private Format createFormat() {
		return NanoSecondsFormat.INSTANCE;
	}

}
