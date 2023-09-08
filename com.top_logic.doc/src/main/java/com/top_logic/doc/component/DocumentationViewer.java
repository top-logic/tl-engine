/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.component;

/**
 * Interface for views that can display a documentation page.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface DocumentationViewer {

	/**
	 * Show the documentation for the view with the given
	 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config#getHelpId()}.
	 * 
	 * @return Whether the page with the given help id could be shown.
	 */
	boolean showDocumentation(String helpId);

}
