/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.model.annotate.ui.FormatBase;

/**
 * {@link TableFilterProvider} for {@link Comparable}-valued columns.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ComparableTableFilterProvider extends AbstractTableFilterProvider {

	private final boolean _mandatory;

	private final ComparisonOperatorsProvider _operatorsProvider;

	private FormatProvider _formatProvider;

	@SuppressWarnings("javadoc")
	public interface Config extends AbstractTableFilterProvider.Config, FormatBase {

		/** Whether the column contains mandatory values, or not */
		@Name("mandatory")
		@BooleanDefault(false)
		boolean isMandatory();

		/** Provider of available operators for the given filter */
		@Name("comparison-operators-provider")
		@InstanceFormat
		@InstanceDefault(AllOperatorsProvider.class)
		ComparisonOperatorsProvider getComparisonOperatorsProvider();
	}

	/**
	 * Creates a {@link ComparableTableFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ComparableTableFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
		_formatProvider = createFormatProvider(context, config);
		_mandatory = config.isMandatory();
		_operatorsProvider = config.getComparisonOperatorsProvider();
	}

	private static FormatProvider createFormatProvider(InstantiationContext context, Config config) {
		String formatReference = config.getFormatReference();
		if (!StringServices.isEmpty(formatReference)) {
			checkNoLiteralFormat(context, config);
			ReferenceFormatProvider referenceFormatProvider = new ReferenceFormatProvider(formatReference);
			referenceFormatProvider.setReferenceSource(config.location().toString());
			return referenceFormatProvider;
		}

		String literalFormat = config.getFormat();
		if (!StringServices.isEmpty(literalFormat)) {
			PatternFormatProvider patternFormatProvider = new PatternFormatProvider(literalFormat);
			patternFormatProvider.setPatternSource(config.location().toString());
			return patternFormatProvider;
		}
		return GenericFormatProvider.INSTANCE;
	}

	private static void checkNoLiteralFormat(InstantiationContext context, Config config) {
		if (!StringServices.isEmpty(config.getFormat())) {
			context.error("Either a reference or a literal format must be given for table filter in '"
				+ config.location().toString()
					+ "', not both.");
		}
	}

	/**
	 * Creates a {@link ComparableTableFilterProvider}.
	 * 
	 * @param mandatory
	 *        Whether the column value cannot be empty.
	 * @param operatorsProvider
	 *        {@link ComparisonOperatorsProvider} of available comparison options (e.g. equals,
	 *        smaller than, etc.)
	 */
	public ComparableTableFilterProvider(boolean mandatory, ComparisonOperatorsProvider operatorsProvider) {
		_formatProvider = GenericFormatProvider.INSTANCE;
		_mandatory = mandatory;
		_operatorsProvider = operatorsProvider;
	}

	/**
	 * Provider for the format to use in input fields.
	 */
	public FormatProvider getFormatProvider() {
		return _formatProvider;
	}

	@Override
	protected List<ConfiguredFilter> createFilterList(TableViewModel model, String filterPosition) {
		ColumnConfiguration column = model.getColumnDescription(filterPosition);

		ComparableFilterConfiguration filterConfiguration =
			new ComparableFilterConfiguration(model, filterPosition, getOperatorsProvider(),
				getComparator(), false, showSeparateOptionEntries());

		ConfiguredFilter filter = new ComparableFilter(filterConfiguration,
			new ComparableFilterViewBuilder(createFieldProvider(column), getSeparateOptionDisplayThreshold()),
			showNonMatchingOptions());

		if (isMandatory()) {
			return Collections.singletonList(filter);
		} else {
			return TableFilterProviderUtil.includeNoValueOption(filter);
		}
	}

	protected abstract FilterComparator getComparator();

	protected abstract FilterFieldProvider createFieldProvider(ColumnConfiguration column);

	/** @see Config#isMandatory() */
	protected boolean isMandatory() {
		return _mandatory;
	}

	/** @see Config#getComparisonOperatorsProvider() */
	protected ComparisonOperatorsProvider getOperatorsProvider() {
		return _operatorsProvider;
	}

}
