/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.component.TableFilterProvider;

/**
 * {@link TableFilterProvider} for label-based filters.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelFilterProvider extends AbstractTableFilterProvider {
	
	/** Instance of non mandatory {@link LabelFilterProvider} */
	public static final TableFilterProvider INSTANCE = new LabelFilterProvider(false);

	@SuppressWarnings("javadoc")
	public interface Config extends AbstractTableFilterProvider.Config {
		@Name(MANDATORY_PROPERTY)
		@BooleanDefault(false)
		boolean isMandatory();
	}

	private static final String MANDATORY_PROPERTY = "mandatory";
	
	/** Supporting classes (here all instances of object). */
	protected static final List<Class<?>> ALL_TYPES = Collections.<Class<?>> singletonList(Object.class);

	private final boolean _mandatory;

	/**
	 * Creates a {@link LabelFilterProvider} form configuration.
	 */
	@CalledByReflection
	public LabelFilterProvider(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_mandatory = mandatory(context, config);
	}

	private static boolean mandatory(InstantiationContext context, Config config) {
		return config.isMandatory();
	}

	/**
	 * Creates a {@link LabelFilterProvider}.
	 * 
	 * @param mandatory
	 *        Whether the column value cannot be empty.
	 */
	public LabelFilterProvider(boolean mandatory) {
		_mandatory = mandatory;
    }

    @Override
	protected final List<ConfiguredFilter> createFilterList(TableViewModel tableModel, String filterPosition) {
		LabelProvider labelProvider = getLabelProvider(tableModel, filterPosition);
		assertValidLabelProvider(labelProvider);
		if (_mandatory) {
			return Collections.singletonList(createTextFilter(tableModel, filterPosition, labelProvider));
		} else {
			return TableFilterProviderUtil.includeNoValueOption(createTextFilter(tableModel, filterPosition, labelProvider));
		}
    }

	static void assertValidLabelProvider(LabelProvider labelProvider) {
		assert isValidLabelProvider(
			labelProvider) : "Given label provider must not be null or instance of NoFullTextLabelProvider.";
	}

	static boolean isValidLabelProvider(LabelProvider labelProvider) {
		return labelProvider != null;
	}

	private static LabelProvider getLabelProvider(TableViewModel tableModel, String filterPosition) {
		return tableModel.getColumnDescription(filterPosition).getFullTextProvider();
	}

	private ConfiguredFilter createTextFilter(TableViewModel tableModel, String columnName, LabelProvider labelProvider) {
		return TextFilter.createTextFilter(tableModel, columnName, labelProvider, ALL_TYPES,
			getSeparateOptionDisplayThreshold(),
			showNonMatchingOptions(), showSeparateOptionEntries(), false);
	}
}