/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractStringConstraint;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Checks if the file path containts "..".
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NoParentPathTokenConstraint extends AbstractStringConstraint {

	/**
	 * Singleton {@link NoParentPathTokenConstraint} instance.
	 */
	public static final NoParentPathTokenConstraint INSTANCE = new NoParentPathTokenConstraint();

	@Override
	protected boolean checkString(String path) throws CheckException {

		if (path.contains(FileUtilities.PARENT_PATH_TOKEN)) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.PARENT_PATH_TOKEN_ERROR));
		}

		return true;
	}

}
