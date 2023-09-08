/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.stylesheet;

import java.util.Set;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractStringConstraint;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Checks if the given stylesheet name is already used.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class AvailableStylesheetNameConstraint extends AbstractStringConstraint {

	/**
	 * Singleton {@link AvailableStylesheetNameConstraint} instance.
	 */
	public static final AvailableStylesheetNameConstraint INSTANCE = new AvailableStylesheetNameConstraint();

	@Override
	protected boolean checkString(String styleFilename) throws CheckException {
		Set<String> styleResourcePaths = FileUtilities.getAllResourcePaths(ThemeUtil.getThemeStylePath());

		boolean isDuplicateFilename = styleResourcePaths.stream().filter(path -> {
			int lastIndexOf = path.lastIndexOf(FileUtilities.PATH_SEPARATOR);

			if (lastIndexOf != -1) {
				return path.substring(lastIndexOf).equals(styleFilename);
			}

			return path.equals(styleFilename);
		}).findAny().isPresent();

		if (isDuplicateFilename) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.STYLESHEET_FILENAME_NOT_FREE));
		}

		return true;
	}

}
