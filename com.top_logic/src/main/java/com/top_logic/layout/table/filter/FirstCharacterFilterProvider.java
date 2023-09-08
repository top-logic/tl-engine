/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.DisplayDimension.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.filter.PatternFilter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.AbstractLocalizableComparator;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.I18NConstants;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.model.export.AccessContext;

/**
 * Base class for all {@link TableFilterProvider}, which create {@link TableFilter} out of
 * the table entries first character.
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FirstCharacterFilterProvider implements TableFilterProvider {

	private static final String NUMBER_PATTERN = "^\\d.*";

	/** a filter that filters elements matching number characters, uses pattern "^\\d.*" */
	public static final PatternFilter NUMBER_FILTER = new PatternFilter(NUMBER_PATTERN);

    private static final Pattern DEFAULT_CHARACTERS = Pattern.compile("[A-Za-z]");

	private static final Map<String, String> UMLAUT_MAPPING =
		(new MapBuilder<String, String>()).put("Ä", "A").put("Ü", "U").put("Ö", "O").toMap();

	LabelProvider _labelProvider;

	/**
	 * Create a new FirstCharacterFilterProvider
	 * 
	 * @param labelProvider
	 *        - the mapping to the label
	 */
	public FirstCharacterFilterProvider(LabelProvider labelProvider) {
		_labelProvider = labelProvider;
	}

	/**
	 * This method generates TableFilters based on StartsWithFilters for a given column.
	 * aLabelMapping is used to transform the table cell values into Strings
	 * 
	 * @param aTableViewModel
	 *        - the view model of the table
	 * @param filterPosition
	 *        - unique name of column
	 * @see com.top_logic.layout.table.component.TableFilterProvider#createTableFilter(com.top_logic.layout.table.TableViewModel,
	 *      String)
	 */
	@Override
	public TableFilter createTableFilter(TableViewModel aTableViewModel, String filterPosition) {

		Set<String> theIdx = getInitialFirstCharacters(aTableViewModel, filterPosition);

		boolean numbersAdded = false;
		Set<String> addedChars = new HashSet<>();
		List<WrappedCharacterFilter> characterFilters = new LinkedList<>();

		for (Iterator<String> theIter = theIdx.iterator(); theIter.hasNext();) {
			String theChar = theIter.next();

			if (NUMBER_FILTER.accept(theChar)) {
				if (!numbersAdded) { // add number filter only once
					characterFilters.add(new WrappedCharacterFilter(
						NUMBER_FILTER, new ResourceText(I18NConstants.FILTER_NUMBERS)));
					numbersAdded = true;
					theIter.remove();
				}
			} else if (UMLAUT_MAPPING.containsKey(theChar) || DEFAULT_CHARACTERS.matcher(theChar).matches()) {
				if (!addedChars.contains(theChar)) {
					Set<String> theMatchSet = getMatchingCharacters(theChar);
					characterFilters.add(new WrappedCharacterFilter(
						new FirstCharacterFilter(theMatchSet, /* ignoreCase */true, _labelProvider),
						new ConstantDisplayValue(theChar)));
					addedChars.addAll(theMatchSet);
					theIter.remove();
				}
			}
		}

		// Sort character filters
		Collections.sort(characterFilters, new WrapperFilterComparator(DefaultDisplayContext.getDisplayContext()));

		// Add filter for non recognizable characters
		if (!theIdx.isEmpty()) {
			characterFilters.add(new WrappedCharacterFilter(
				new FirstCharacterFilter(theIdx, /* ignoreCase */true, _labelProvider),
				new ResourceText(I18NConstants.FILTER_SPECIAL_CHARS)));
		}

		// Create table filter
		PopupDialogModel dialogModel =
			new DefaultPopupDialogModel(
				new ResourceText(PopupFilterDialogBuilder.STANDARD_TITLE),
				new DefaultLayoutData(
					dim(ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FILTER_DIALOG_WIDTH),
						DisplayUnit.PIXEL),
					100, ZERO, 100,
					Scrolling.NO),
				1);
		FilterDialogBuilder manager = new PopupFilterDialogBuilder(dialogModel);

		TableFilter tableFilter = new TableFilter(manager, characterFilters, /* sameGroup */true);
		return tableFilter;
	}

	private Set<String> getInitialFirstCharacters(TableViewModel viewModel, String filterPosition) {
		/* The table filter receives the value of the table after application of the sort key
		 * provider. Therefore during creation of the TableFilter the SortKeyProvider must be
		 * applied manually. */
		ColumnConfiguration columnConfig = viewModel.getTableConfiguration().getCol(filterPosition);
		final Mapping<Object, ?> sortKeyProvider = columnConfig.getSortKeyProvider();
		LabelProvider wrappedProvider;
		if (sortKeyProvider != Mappings.identity()) {
			wrappedProvider = new LabelProvider() {
				
				@Override
				public String getLabel(Object object) {
					return _labelProvider.getLabel(sortKeyProvider.map(object));
				}

			};
		} else {
			wrappedProvider = _labelProvider;
		}

		return getFirstCharacters(viewModel, filterPosition, wrappedProvider);
	}

	private Set<String> getFirstCharacters(TableViewModel viewModel, String filterPosition, LabelProvider labels) {
		Set<String> theIdx = new HashSet<>(26); // Range A-Z to suppress duplicates only.
		List displayedRows = viewModel.getDisplayedRows();
		AccessContext context = viewModel.prepareRows(displayedRows, Collections.singletonList(filterPosition));
		for (Object displayedRow : displayedRows) {
			String theString = labels.getLabel(viewModel.getValueAt(displayedRow, filterPosition));
			if (!StringServices.isEmpty(theString)) {
				String theFirst = theString.substring(0, 1).toUpperCase();
				theIdx.add(theFirst);
			}
		}
		context.close();
		return theIdx;
	}

	private static Set<String> getMatchingCharacters(String aChar) {
		String theBase = UMLAUT_MAPPING.get(aChar);

		if (theBase == null) {
			theBase = aChar;
		}

		Set<String> theMatchSet = new HashSet<>();
		theMatchSet.add(theBase);
		for (Iterator<Map.Entry<String, String>> theIter = UMLAUT_MAPPING.entrySet().iterator(); theIter.hasNext();) {
			Map.Entry<String, String> theEntry = theIter.next();
			String theVal = theEntry.getKey();
			String theMatch = theEntry.getValue();

			if (theMatch.equals(theBase)) {
				theMatchSet.add(theVal);
			}
		}
		return theMatchSet;
	}

	private class WrappedCharacterFilter extends StaticFilterWrapper {

		private WrappedCharacterFilter(Filter<Object> wrappedFilter, DisplayValue label) {
			super(wrappedFilter, label);
		}

		public DisplayValue getLabel() {
			return ((StaticFilterWrapperConfiguration) getFilterConfiguration()).getFilterDescription();
		}
	}
	
	private class FirstCharacterFilter implements Filter<Object> {

		private Set<String> match;

		private boolean ignoreCase;

		private LabelProvider _labelProvider;

		public FirstCharacterFilter(Set<String> aMatch, boolean ignoreCase, LabelProvider labelProvider) {

			this.ignoreCase = ignoreCase;
			if (this.ignoreCase) {
				match = new HashSet<>(aMatch.size());
				for (Iterator<String> theIter = aMatch.iterator(); theIter.hasNext();) {
					String theChar = theIter.next();
					match.add(theChar.toUpperCase());
				}
			} else {
				match = aMatch;

			}
			_labelProvider = labelProvider;
		}

		@Override
		public boolean accept(Object object) {
			if (object != null) {
				String theString = _labelProvider.getLabel(object);
				if (theString.length() > 0) {
					if (this.ignoreCase) {
						return this.match.contains(theString.substring(0, 1).toUpperCase());
					} else {
						return this.match.contains(theString.substring(0, 1));
					}
				}
			}
			return false;
		}
	}

	/**
	 * {@link Comparator} that compares {@link TableFilter}s according to their localized
	 * description message.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private class WrapperFilterComparator extends AbstractLocalizableComparator {

		WrapperFilterComparator(DisplayContext context) {
			super(context);
		}

		@Override
		protected final DisplayValue getLocalizable(Object object) {
			return ((WrappedCharacterFilter) object).getLabel();
		}
	}
}
