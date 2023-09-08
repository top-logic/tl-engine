/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.stylesheet;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractStringConstraint;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Checks if the filename ends with ".css".
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class StylesheetFilenameSuffixConstraint extends AbstractStringConstraint {

	/**
	 * Singleton {@link StylesheetFilenameSuffixConstraint} instance.
	 */
	public static final StylesheetFilenameSuffixConstraint INSTANCE = new StylesheetFilenameSuffixConstraint();

	@Override
	protected boolean checkString(String filename) throws CheckException {

		if (!filename.endsWith(FileUtilities.CSS_FILE_ENDING)) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.ERROR_STYLESHEET_FILENAME_SUFFIX));
		}

		return true;
	}

}
