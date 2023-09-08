/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import javax.swing.ListSelectionModel;

/**
* {@link ListSelectionModel} that prevents some of its indexes from becoming
* selected.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RestrictedListSelectionModel extends ListSelectionModel {

	/**
	 * Decide, whether the list item at the given index can be selected.
	 * 
	 * @return <code>true</code>, if the list item at the given index can be
	 *         selected, <code>false</code> otherwise.
	 */
	public boolean isSelectableIndex(int index);
	
}
