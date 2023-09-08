/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.app.importDoc;

import java.util.List;
import java.util.Locale;

import org.jsoup.nodes.Document;

import com.top_logic.doc.app.I18NConstants;
import com.top_logic.doc.command.validation.PageValidator;
import com.top_logic.doc.model.Page;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.wysiwyg.ui.StructuredText;

/**
 * {@link PageValidator} validating the Import from Trac.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TracWikiValidator implements PageValidator {

	@Override
	public void validatePage(DisplayContext context, Page page, Locale language, boolean recursive) {
		TracWikiImporter formatter = new TracWikiImporter();
		boolean success;
		if (recursive) {
			success = validatePages(formatter, page, language);
		} else {
			success = validatePage(formatter, page, language);
		}
		if (success) {
			InfoService.showInfo(I18NConstants.IMPORT_VALID);
		} else {
			formatter.showMessages();
		}
	}

	/**
	 * Checks if the given page has errors that occurred during the import of it.
	 * 
	 * @param formatter
	 *        The {@link TracWikiImporter} that stores possible error messages.
	 * @param page
	 *        The {@link Page} to validate.
	 * @param language
	 *        The language to validate page for.
	 * @return <code>true</code> if the {@link Page} is valid. <code>false</code> if not so the user
	 *         has to manually correct the errors.
	 */
	protected boolean validatePage(TracWikiImporter formatter, Page page, Locale language) {
		StructuredText contents = page.getContent().localize(language);
		if (contents == null) {
			return true;
		}
		String sourceCode = contents.getSourceCode();
		Document doc = formatter.getParsedDocument(sourceCode);
		if (formatter.valid(page, doc)) {
			return true;
		}
		return false;
	}

	/**
	 * Validates all {@link Page}s within the root {@link Page}.
	 * 
	 * <p>
	 * Looks for errors that occurred during the import.
	 * </p>
	 * 
	 * @param formatter
	 *        The {@link TracWikiImporter} to format Trac Wiki pages.
	 * @param root
	 *        {@link Page} to validate (incl. its children)
	 * @param language
	 *        The language to validate pages for.
	 * @return <code>true</code> if no import error was found.
	 */
	private boolean validatePages(TracWikiImporter formatter, Page root, Locale language) {
		boolean valid = true;
		if (!validatePage(formatter, root, language)) {
			valid = false;
		}
		List<? extends Page> children = root.getChildren();
		for (Page child : children) {
			if (!validatePages(formatter, child, language)) {
				valid = false;
			}
		}
		return valid;
	}

}

