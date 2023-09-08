/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;

/**
 * A filter for comparable objects.
 *   
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class ComparableFilter extends AbstractSelectionBasedFilter<ComparableFilterConfiguration> {

	/**
	 * Create a new ComparableFilter
	 * 
	 * @param filterConfiguration - the filter configuration, must not be null
	 */
	public ComparableFilter(ComparableFilterConfiguration filterConfiguration, ComparableFilterViewBuilder viewBuilder,
			boolean showNonMatchingOptions) {
		super(filterConfiguration, viewBuilder, filterConfiguration.getFilterComparator().getSupportedObjectTypes(),
			showNonMatchingOptions,
			new RuleFilter(filterConfiguration));
	}
	
	/**
	 * Create a new ComparableFilter with a {@link ComparableFilterView}.
	 * 
	 * @param filterConfiguration - the filter configuration, must not be null
	 */
	public ComparableFilter(ComparableFilterConfiguration filterConfiguration, int separateOptionDisplayTreshold,
			boolean showNonMatchingOptions) {
		this(filterConfiguration,
			new ComparableFilterViewBuilder(NumberFieldProvider.INSTANCE, separateOptionDisplayTreshold),
			showNonMatchingOptions);
	}
	
	@Override
	protected boolean ruleBasedFilterActive() {
		if (getTypedConfig().getOperator() == Operators.BETWEEN) {
			return (getTypedConfig().getPrimaryFilterPattern() != null)
				|| (getTypedConfig().getSecondaryFilterPattern() != null);
		} else {
			return getTypedConfig().getPrimaryFilterPattern() != null;
		}
	}

	private static class RuleFilter implements Filter<Object> {

		private ComparableFilterConfiguration _config;

		RuleFilter(ComparableFilterConfiguration config) {
			_config = config;
		}

		@Override
		public boolean accept(Object comparatorComparable) {
			boolean match = false;
			FilterComparator filterComparator = _config.getFilterComparator();

			// Filter cases
			switch (_config.getOperator()) {

				case GREATER_THAN: {
					match = filterComparator.compare(_config.getPrimaryFilterPattern(),
						comparatorComparable) < 0;
					break;
				}

				case GREATER_OR_EQUALS: {
					match = filterComparator.compare(_config.getPrimaryFilterPattern(),
						comparatorComparable) < 1;
					break;
				}

				case LESS_OR_EQUALS: {
					match = filterComparator.compare(_config.getPrimaryFilterPattern(),
						comparatorComparable) > -1;
					break;
				}

				case LESS_THAN: {
					match = filterComparator.compare(_config.getPrimaryFilterPattern(),
						comparatorComparable) > 0;
					break;
				}

				case NOT_EQUAL: {
					match = filterComparator.compare(_config.getPrimaryFilterPattern(),
						comparatorComparable) != 0;
					break;
				}

				case BETWEEN: {
					boolean lowerBoundMatch = true;
					boolean upperBoundMatch = true;
					if (_config.getPrimaryFilterPattern() != null) {
						lowerBoundMatch =
							filterComparator.compare(_config.getPrimaryFilterPattern(), comparatorComparable) < 1;
					}
					if (_config.getSecondaryFilterPattern() != null) {
						upperBoundMatch =
							filterComparator.compare(_config.getSecondaryFilterPattern(), comparatorComparable) > -1;
					}
					match = lowerBoundMatch && upperBoundMatch;
					break;
				}

				// If no valid operator was set, using equals as default
				default: {
					Logger.warn("Operator ID is invalid, using equals!", this);
				}

				//$FALL-THROUGH$
				case EQUALS: {
					match = filterComparator.compare(_config.getPrimaryFilterPattern(), comparatorComparable) == 0;
				}
			}
			return match;
		}
	}
}