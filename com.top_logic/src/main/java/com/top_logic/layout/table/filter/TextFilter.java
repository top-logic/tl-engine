/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;
import java.util.regex.Pattern;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.table.TableViewModel;

/**
 * A filter for string objects.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class TextFilter extends AbstractSelectionBasedFilter<TextFilterConfiguration> {

	/**
	 * Create a new TextFilter
	 * 
	 * @param config
	 *        The filter configuration, must not be null
	 * @param view
	 *        The view builder of this filter, must not be null
	 * @param labelProvider
	 *        The filter label provider, must not be null
	 * @param supportedTypes
	 *        See {@link #getSupportedObjectTypes()}.
	 */
	protected TextFilter(TextFilterConfiguration config, TextFilterViewBuilder view, LabelProvider labelProvider,
			List<Class<?>> supportedTypes, boolean showNonMatchingOptions) {
		super(config, view, supportedTypes, showNonMatchingOptions, new RuleFilter(config));
	}

	void setLabelProvider(LabelProvider labelProvider) {
		getTypedConfig().setOptionLabelProvider(labelProvider);
	}

	@Override
	protected boolean ruleBasedFilterActive() {
		return !getTypedConfig().getTextPattern().equals("");
	}

	/**
	 * Creates a default {@link TextFilter}.
	 */
	public static TextFilter createTextFilter(TableViewModel tableModel, String columnName, LabelProvider labelProvider, List<Class<?>> supportedTypes, int separateOptionDisplayTreshold, boolean showNonMatchingOptions, boolean showOptionEntries, boolean hasCollectionValues) {
		return new TextFilter(
			new TextFilterConfiguration(tableModel, columnName, labelProvider, hasCollectionValues, showOptionEntries),
			new TextFilterViewBuilder(separateOptionDisplayTreshold),
			labelProvider, supportedTypes, showNonMatchingOptions);
	}

	private static class RuleFilter implements Filter<Object> {

		private TextFilterConfiguration _config;

		RuleFilter(TextFilterConfiguration config) {
			_config = config;
		}

		@Override
		public boolean accept(Object anObject) {
			boolean match = false;

			// Label has been retrieved by configured label provider before this filter is called
			String filteredString = (String) (anObject);

			// bitmask: regExp = 100 wholeField = 010, caseSensitive = 001
			int filterOptions =
				(_config.isRegExp() ? 4 : 0) | (_config.isWholeField() ? 2 : 0) | (_config.isCaseSensitive() ? 1 : 0);

			// Filter string
			String filterString = _config.getTextPattern();

			// Filter cases
			switch (filterOptions) {

				// !regExp && !wholeField && !caseSensitive
				case 0: {
					match = filteredString.toLowerCase().indexOf(filterString.toLowerCase()) > -1;
					break;
				}

				// !regExp && !wholeField && caseSensitive
				case 1: {
					match = filteredString.indexOf(filterString) > -1;
					break;
				}

				// !regExp && wholeField && !caseSensitive
				case 2: {
					match = filteredString.toLowerCase().equals(filterString.toLowerCase());
					break;
				}

				// !regExp && wholeField && caseSensitive
				case 3: {
					match = filteredString.equals(filterString);
					break;
				}

				// regExp && !wholeField && !caseSensitive
				case 4: {
					match = Pattern.compile(filterString, Pattern.CASE_INSENSITIVE).matcher(filteredString).find();
					break;
				}

				// regExp && !wholeField && caseSensitive
				case 5: {
					match = Pattern.compile(filterString).matcher(filteredString).find();
					break;
				}

				// regExp && wholeField && !caseSensitive
				case 6: {
					match =
							Pattern.compile(filterString, Pattern.CASE_INSENSITIVE).matcher(filteredString).matches();
					break;
				}

				// regExp && wholeField && caseSensitive
				case 7: {
					match = Pattern.compile(filterString).matcher(filteredString).matches();
					break;
				}
			}
			return match;
		}
	}
}