/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import static com.top_logic.layout.list.model.ListModelUtilities.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.list.model.DefaultRestrictedListSelectionModel;
import com.top_logic.layout.list.model.ListModelUtilities;
import com.top_logic.layout.provider.LabelResourceProvider;
import com.top_logic.util.Resources;

/**
 * Implementation of {@link SelectorContext} that directly implements all
 * updates to the models of a {@link ListSelectDialog} view.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OptimizedSelectorContext extends SelectorContext {
	
	/**
	 * Order of displayed options.
	 * 
	 * <p>
	 * The option order should match the option labels of the target select
	 * field and the results delivered by the label provider for option pages.
	 * </p>
	 */
	private Comparator optionOrder;

	/**
	 * Creates a {@link OptimizedSelectorContext}.
	 * 
	 * @see SelectorContext#SelectorContext(SelectField, LabelProvider, int, Command, boolean)
	 */
	public OptimizedSelectorContext(SelectField targetSelectField, LabelProvider newPageLabels, int optionsPerPage, 
	                               Command closeAction, boolean isLeftToRight) {
		super(targetSelectField, newPageLabels, optionsPerPage, closeAction, isLeftToRight);
	}

	@Override
	protected void init(final SelectField targetSelectField, int configuredOptionsPerPage) {

		this.optionOrder = targetSelectField.getOptionComparator();

		List<?> options = targetSelectField.getOptions();
		List<?> selection = getSelection(targetSelectField);
		
		final List allOptions = SelectFieldUtils.joinOrdered(this.optionOrder, options, selection);
		
		Object singleSelection;
		if (!selection.isEmpty()) {
			singleSelection = selection.get(0);
		} else {
			singleSelection = null;
		}

		if (configuredOptionsPerPage == ALL_OPTIONS_ON_ONE_PAGE) {
			// all options also contain current selection of the select field. These selection may
			// not be contained in the options, so the number of all option may be larger than the
			// option count of the select field.
			configuredOptionsPerPage = allOptions.size();
		}

		// Add selection to option list, if necessary
		final int displayedOptionsPerPage;
		if (!isMultiSelect()) {
			int extraOptionCount = allOptions.size() - configuredOptionsPerPage;
			if (extraOptionCount == 1) {
				displayedOptionsPerPage = configuredOptionsPerPage + 1;
			} else {
				displayedOptionsPerPage = configuredOptionsPerPage;
			}
		} else {
			displayedOptionsPerPage = configuredOptionsPerPage;
		}

		// Create list of all pages.
		final List<Page> allPages =
			createPageList(allOptions, displayedOptionsPerPage, this.optionOrder, pageLabels);
		optionPages = 
			FormFactory.newSelectField(PAGE_FIELD_NAME, allPages, false, true, false, GenericMandatoryConstraint.SINGLETON);
		optionPages.setOptionLabelProvider(Page.PAGE_LABEL_PROVIDER);

		// Choose initial page.
		final Page initialPage;
		if (isMultiSelect()) {
			initialPage = (Page) allPages.get(0);
		} else {
			if (singleSelection != null) {
				int selectedOptionIndex = Collections.binarySearch(allOptions, singleSelection, optionOrder);
				// Choose page that contains the current selection.
				initialPage = lookupPage(allPages, selectedOptionIndex);
			} else {
				initialPage = (Page) allPages.get(0);
			}
		}
		optionPages.setAsSingleSelection(initialPage);
		
		final DefaultListModel displayedOptions = new DefaultListModel();
		DefaultListSelectionModel markedOptions;
		Filter selectableOptionsFilter = null;
		if (targetSelectField.getFixedOptions() != null) {
			selectableOptionsFilter = FilterFactory.not(targetSelectField.getFixedOptions());
			markedOptions = new DefaultRestrictedListSelectionModel(displayedOptions, selectableOptionsFilter);
		} else {
			markedOptions = new DefaultListSelectionModel();
		}
		
		// Create a renderer that only renders plain labels, even if the
		// (converted) label provider would offer image, links or tool tips.
		Renderer<Object> optionLabelRenderer = ResourceRenderer.newResourceRenderer(
			LabelResourceProvider.toResourceProvider(targetSelectField.getOptionLabelProvider()),
			!ResourceRenderer.USE_LINK, ResourceRenderer.USE_TOOLTIP, !ResourceRenderer.USE_IMAGE);
		optionList = FormFactory.newListField(OPTIONS_FIELD_NAME, displayedOptions, markedOptions);
		optionList.setItemRenderer(optionLabelRenderer);

		if (isMultiSelect()) {
			DefaultListModel displayedSelection = new DefaultListModel();
			ListSelectionModel selectedSelection;
			if (selectableOptionsFilter != null && !targetSelectField.hasCustomOrder()) {
				selectedSelection = new DefaultRestrictedListSelectionModel(displayedSelection, selectableOptionsFilter);
			} else {
				selectedSelection = new DefaultListSelectionModel();
			}
			selectionList = FormFactory.newListField(SELECTION_FIELD_NAME, displayedSelection, selectedSelection);
			selectionList.setItemRenderer(optionLabelRenderer);
			
			// Ensure that the list of selected options uses the same sort order
			// as the list of all options. This is necessary to have a newly
			// selected option inserted at a defined place.
			List selectedOptions = new ArrayList(selection);
			if (!targetSelectField.hasCustomOrder()) {
				Collections.sort(selectedOptions, optionOrder);
			}

			ListModelUtilities.replaceAll(displayedSelection, selectedOptions);

			updateOptions(displayedOptions, allOptions, initialPage);
		} else {
			updateOptions(displayedOptions, allOptions, initialPage);
			
			ListSelectionModel selectedOptions = optionList.getSelectionModel();
			selectedOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			if (singleSelection != null) {
				int indexToSelect = displayedOptions.indexOf(singleSelection);
				selectedOptions.setSelectionInterval(indexToSelect, indexToSelect);
			}
		}
		
		final boolean[] updatingPattern = new boolean[] {false};
		final List[] filteredOptions = new List[] {null};
		final String[] filteredPattern = new String[] {null};
		
		// Note: Value listeners are called before checking constraints. Therefore, the constraint
		// can rely on the option list to be empty to signal a failure for a non-matching pattern.
		pattern.addWarningConstraint(new AbstractConstraint() {
			@Override
			public boolean check(Object value) throws CheckException {
				if (StringServices.isEmpty(value)) {
					return true;
				}

				if (optionList.getListModel().getSize() == 0) {
					throw new CheckException(
						Resources.getInstance().getString(I18NConstants.NO_OPTION_FOUND));
				}
				
				return true;
			}
		});

		pattern.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object value) {
				String patternString = (String) value;
				
				List matchingOptions;
				List newPages;
				if (StringServices.isEmpty(patternString)) {
					// All options are matched, do not filter.
					
					matchingOptions = allOptions;
					filteredOptions[0] = null;
					filteredPattern[0] = null;
					
					newPages = allPages;
				} else {
					Filter optionFilter = 
						createPatternFilter(targetSelectField, patternString);
					
					boolean isRestriction = 
						filteredOptions[0] != null && patternString.startsWith(filteredPattern[0]);
					
					matchingOptions = FilterUtil.filterList(optionFilter, (isRestriction ? filteredOptions[0] : allOptions));
					
					filteredOptions[0] = matchingOptions;
					filteredPattern[0] = patternString;
					
					LabelProvider labels = OptimizedSelectorContext.this.pageLabels;
					Comparator order = OptimizedSelectorContext.this.optionOrder;
					newPages = createPageList(matchingOptions, displayedOptionsPerPage, order, labels);
				}
				
				Object currentPage = optionPages.getSingleSelection();
				int searchResult = Collections.binarySearch(newPages, currentPage);
				int newPageIndex;
				if (searchResult >= 0) {
					newPageIndex = searchResult;
				} else {
					newPageIndex = -searchResult - 1;
					if (newPageIndex >= newPages.size()) {
						newPageIndex = newPages.size() - 1;
					}
				}
				
				Page newPage = newPageIndex >= 0 ? (Page) newPages.get(newPageIndex) : null;

				// Prevent optionPages from reacting on value change events.
				updatingPattern[0] = true;
				try {
					optionPages.setOptions(newPages);
					optionPages.setAsSingleSelection(newPage);
				} finally {
					updatingPattern[0] = false;
				}

				if (newPage != null) {
					updateOptions(displayedOptions, matchingOptions, newPage);
				}
				if (displayedOptions.getSize() == 1) {
					// Select unique element, if it is not a fixed option.
					Filter theFixedOptions = targetSelectField.getFixedOptions();
                    if (theFixedOptions == null || ! theFixedOptions.accept(displayedOptions.get(0))) {
						optionList.getSelectionModel().setSelectionInterval(0, 0);
					}
				}
			}
		});
		
		optionPages.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (updatingPattern[0]) {
					// The update must not be propagated to the options field.
					return;
				}
				
				List matchingOptions = filteredOptions[0] != null ? filteredOptions[0] : allOptions;
				
				Page newPage = currentPage();
				
				// Workaround: There have been reports about NullPointerException being thrown in updateOptions().  
				if (newPage != null) {
					updateOptions(displayedOptions, matchingOptions, newPage);
				}
			}
		});
		
	}

	private List<?> getSelection(final SelectField targetSelectField) {
		Object storedValue = FormFieldInternals.getStoredValue(targetSelectField);
		if (storedValue != null) {
			return (List<?>) storedValue;
		} else {
			return Collections.emptyList();
		}
	}

    /** 
     * Find the page in the given list of all pages that contains the option with the given index.
     */
    private Page lookupPage(final List allPages, int optionIndex) {
    	// TODO BHU: Inefficient linear search.
    	for (int n = 0, cnt = allPages.size(); n < cnt; n++) {
    		Page page = (Page) allPages.get(n);
    		if (optionIndex >= page.getStartIndex() && optionIndex < page.getStartIndex() + page.getSize()) {
                return page;
    		}
    	}
    	
    	// Must not be reached.
        return (Page) allPages.get(0);
    }

	/**
	 * Update the given list model with the sublist of options on the given
	 * page.
	 * 
	 * <p>
	 * The list of matching options is the subset of all options that match the
	 * current filter criterion.
	 * </p>
	 */
	private void updateOptions(final DefaultListModel listModel, List matchingOptions, Page newPage) {
		if (isMultiSelect()) {
			// Update with all options on the new page. Hide selected options.
			ListModelUtilities.replaceAll(
				listModel,
				FilterUtil.filterList(FilterFactory.not(new SetFilter(ListModelUtilities.asSet(selectionList.getListModel()))), matchingOptions.subList(
				newPage.getStartIndex(), 
				newPage.getStartIndex() + newPage.getSize())));
		} else {
			ListModelUtilities.replaceAll(
				listModel,
				matchingOptions.subList(
					newPage.getStartIndex(), 
					newPage.getStartIndex() + newPage.getSize()));
		}
	}

	@Override
	public void internalAddSelection(List delta) {
		if (!getSelectField().hasCustomOrder()) {
			DefaultListModel source = (DefaultListModel) optionList.getListModel();
			DefaultListModel destination = (DefaultListModel) selectionList.getListModel();
			moveOptions(source, destination, delta, FilterFactory.trueFilter());
		} else {
			ListModelUtilities.addAll(((DefaultListModel) selectionList.getListModel()), delta);
			removeElements((DefaultListModel) optionList.getListModel(), delta);
		}
	}

	@Override
	public void internalRemoveSelection(List delta) {
		DefaultListModel source = (DefaultListModel) selectionList.getListModel();
		DefaultListModel destination = (DefaultListModel) optionList.getListModel();
		List<? super Object> removedSelection = new ArrayList<>();
		Filter<? super Object> fixedOptionsFilter = getSelectField().getFixedOptionsNonNull();
		for (Object object : delta) {
			if (!fixedOptionsFilter.accept(object)) {
				removedSelection.add(object);
			}
		}
		Collections.sort(removedSelection, optionOrder);
		moveOptions(source, destination, removedSelection, getDisplayedOptionsFilter());
		checkEmptyOptionListByPattern();
	}

	private Filter getDisplayedOptionsFilter() {
		Filter currentPageFilter = currentPage();
		Filter matchingOptionsFilter = createPatternFilter(getSelectField(), pattern.getAsString());
		return FilterFactory.<Object>and(currentPageFilter, matchingOptionsFilter);
	}
	
	private void checkEmptyOptionListByPattern() {
		pattern.checkWithAllDependencies();
	}

	/**
	 * Moves all options in the given delta from the source model to the destination model
	 * 
	 * @param filter
	 *        Only options that are accept by this filter are added to the destination model.
	 */
	private void moveOptions(DefaultListModel source, DefaultListModel destination, List delta, Filter filter) {
		if (! delta.isEmpty()) {
			int insertIndex = 0;
			Object newlySelected = delta.get(insertIndex);
			merge:
			for (int n = 0, cnt = destination.getSize(); n < cnt; n++) {
				Object previouslySelected = destination.get(n);
				while (optionOrder.compare(newlySelected, previouslySelected) < 0) {
					if (filter.accept(newlySelected)) {
						destination.insertElementAt(newlySelected, n);
						n++;
						cnt++;
					}
					insertIndex++;
					if (insertIndex < delta.size()) {
						newlySelected = delta.get(insertIndex);
					} else {
						newlySelected = null;
						break merge;
					}
				}
			}
			while (insertIndex < delta.size()) {
				newlySelected = delta.get(insertIndex++);
				if (filter.accept(newlySelected)) {
					destination.addElement(newlySelected);
				}
			}
			
			removeElements(source, delta);
		}
	}

	/**
	 * Returns the currently selected page.
	 */
	Page currentPage() {
		return (Page) optionPages.getSingleSelection();
	}
}
