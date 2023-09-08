/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.jsp;

import java.io.File;
import java.util.Collection;

/**
 * Checker for the content of a JSP.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface JSPContentChecker {

	/**
	 * Checks the content of a JSP.
	 * 
	 * @param errors
	 *        Output to add errors to.
	 * @param jsp
	 *        The checked JSP.
	 * @param contents
	 *        Content of the given JSP.
	 */
	void checkContent(Collection<String> errors, File jsp, String contents);

}

