/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.view.command.I18NConstants;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * The validation errors that keep an edit from being saved or applied, presented to the user: as
 * the details of a refusal, or as one text, e.g. the reason of a disabled command.
 *
 * <p>
 * Each error is a message that already names where it is, e.g. "Name: Value required.". A long
 * list names the first {@value #MAX_LISTED} errors and closes with the number of the others.
 * </p>
 */
public final class ValidationErrors {

	/**
	 * The number of errors that are listed before the list closes with the number of the others.
	 */
	public static final int MAX_LISTED = 10;

	private ValidationErrors() {
		// Utility class.
	}

	/**
	 * The given errors as they are listed: at most {@value #MAX_LISTED}, followed by the number of
	 * the others.
	 */
	public static List<ResKey> listed(List<ResKey> errors) {
		if (errors.size() <= MAX_LISTED) {
			return errors;
		}
		List<ResKey> result = new ArrayList<>(errors.subList(0, MAX_LISTED));
		result.add(I18NConstants.ERROR_FORM_MORE_ERRORS__COUNT.fill(errors.size() - MAX_LISTED));
		return result;
	}

	/**
	 * The refusal reporting the given errors: the given title, the {@link #listed(List) listed}
	 * errors below it, see {@link CommandErrors#error(ResKey, List)}.
	 *
	 * @param title
	 *        What the user has to do, e.g. fix the errors before saving.
	 * @param errors
	 *        The errors, each naming where it is.
	 * @return The exception to throw.
	 */
	public static TopLogicException refusal(ResKey title, List<ResKey> errors) {
		return CommandErrors.error(title, listed(errors));
	}

	/**
	 * The given title followed by the {@link #listed(List) listed} errors as one text, one per
	 * line.
	 *
	 * <p>
	 * The text is given in the language of the current user. It is meant for a place that shows a
	 * single text, such as the reason of a disabled command (see
	 * {@link ExecutableState#createDisabledState(ResKey)}), which a button shows as its tooltip.
	 * </p>
	 *
	 * @param title
	 *        What the user has to do, e.g. fix the errors before saving.
	 * @param errors
	 *        The errors, each naming where it is.
	 */
	public static ResKey text(ResKey title, List<ResKey> errors) {
		Resources resources = Resources.getInstance();
		StringBuilder text = new StringBuilder(resources.getString(title));
		for (ResKey error : listed(errors)) {
			text.append('\n').append(resources.getString(error));
		}
		return ResKey.text(text.toString());
	}

}
