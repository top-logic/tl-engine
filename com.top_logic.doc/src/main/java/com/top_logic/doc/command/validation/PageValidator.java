/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command.validation;

import java.util.Locale;

import com.top_logic.doc.model.Page;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;

/**
 * Callback to validate a given {@link Page}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PageValidator {

	/**
	 * Validates the given page. Validation results are typically delivered to the GUI using the
	 * {@link InfoService}.
	 * 
	 * @param context
	 *        {@link DisplayContext} in which the validation is executed.
	 * @param page
	 *        The {@link Page} to validate.
	 * @param language
	 *        The language to get contents of the page to validate.
	 * @param recursive
	 *        Whether not just the given {@link Page}, but the whole subtree starting with the given
	 *        page must be validated.
	 */
	void validatePage(DisplayContext context, Page page, Locale language, boolean recursive);

}

