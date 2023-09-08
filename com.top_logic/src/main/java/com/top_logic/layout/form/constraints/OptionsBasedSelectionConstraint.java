/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.util.Resources;

/**
 * {@link Constraint}, which checks, whether a given {@link SelectField} has a valid selection
 * (selection is fully part of current options), or not.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class OptionsBasedSelectionConstraint implements Constraint {

	public static final ResKey SMALL_INVALID_SELECTION_MESSAGE = I18NConstants.ERROR_INVALID_SELECTION__DETAIL;
	public static final ResKey BIG_INVALID_SELECTION_MESSAGE = I18NConstants.ERROR_INVALID_SELECTION__DETAIL_COUNT;
	public static final int MAX_SHOWN_INVALID_SELECTION_ITEMS = 5;

	private SelectField field;

	public OptionsBasedSelectionConstraint(SelectField field) {
		this.field = field;
	}

	@Override
	public boolean check(Object value) throws CheckException {
		if (value == null || ((List<?>) value).isEmpty()) {
			return true;
		}

		List<?> invalidSelection = determineInvalidSelection(value);
		
		if (invalidSelection.size() > 0) {
			if (field.isImmutable()) {
				Logger.warn("Field '" + field.getName() +
					"' has invalid selection items, but selection cannot be changed by user," +
					" because it has been marked as immutable! Skipping UI error generation!",
					OptionsBasedSelectionConstraint.class);
				return true;
			}

			String errorMessage = getErrorMessage(invalidSelection);
			throw new CheckException(errorMessage);
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private List<?> determineInvalidSelection(Object selection) {
		Comparator<? super Object> comparator = field.getOptionComparator();
		List<?> currentSelectionOrdered = getCurrentSelectionOrdered(selection);
		List<?> optionsOrdered = SelectFieldUtils.sortCopy(field.getOptionComparator(), field.getOptions());
		List<Object> invalidSelection = new LinkedList<>();

		int selectionIndex = 0;
		for (Object option : optionsOrdered) {
			if (selectionIndex >= currentSelectionOrdered.size()) {
				break;
			} else if (comparator.compare(option, currentSelectionOrdered.get(selectionIndex)) < 0) {
				continue;
			} else if (comparator.compare(option, currentSelectionOrdered.get(selectionIndex)) == 0) {
				selectionIndex++;
			} else if ((comparator.compare(option, currentSelectionOrdered.get(selectionIndex)) > 0)) {
				invalidSelection.add(currentSelectionOrdered.get(selectionIndex));
				selectionIndex++;
			}
		}

		if (selectionIndex < currentSelectionOrdered.size()) {
			invalidSelection.addAll(currentSelectionOrdered.subList(selectionIndex, currentSelectionOrdered.size()));
		}

		return invalidSelection;
	}

	@SuppressWarnings("unchecked")
	private List<?> getCurrentSelectionOrdered(Object value) {
		List<?> currentSelection = new ArrayList<Object>((List<?>) value);
		Collections.sort(currentSelection, field.getOptionComparator());
		return currentSelection;
	}

	private String getErrorMessage(List<?> invalidSelection) {
		String errorMessageItems = getInvalidItemsForMessage(invalidSelection);
		if (invalidSelection.size() < MAX_SHOWN_INVALID_SELECTION_ITEMS) {
			return Resources.getInstance().getMessage(SMALL_INVALID_SELECTION_MESSAGE, errorMessageItems);
		} else {
			int nonDisplayedInvalidSelectionItemCount = invalidSelection.size() - MAX_SHOWN_INVALID_SELECTION_ITEMS;
			return Resources.getInstance().getMessage(BIG_INVALID_SELECTION_MESSAGE, errorMessageItems,
				nonDisplayedInvalidSelectionItemCount);
		}
	}

	private String getInvalidItemsForMessage(List<?> invalidSelection) {
		StringBuilder messageItems = new StringBuilder();
		for (int i = 0; i < invalidSelection.size() && i < MAX_SHOWN_INVALID_SELECTION_ITEMS; i++) {
			if (i > 0) {
				messageItems.append(", ");
			}
			messageItems.append(field.getOptionLabel(invalidSelection.get(i)));
		}

		return messageItems.toString();
	}
}
