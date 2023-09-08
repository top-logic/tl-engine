/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

/**
 * Describes where the search expression is created.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public enum SearchType {

	/**
	 * Created in the standard search expression GUI.
	 */
	NORMAL,

	/**
	 * Created in the expert TLScript ACE editor.
	 */
	EXPERT

}