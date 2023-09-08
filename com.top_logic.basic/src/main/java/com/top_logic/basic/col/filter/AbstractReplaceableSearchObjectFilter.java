/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

/**
 * This class implements the {@link ReplaceableSearchObjectFilter}.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public abstract class AbstractReplaceableSearchObjectFilter implements ReplaceableSearchObjectFilter {

	private Object searchObject;
	
	@Override
	public Object getSearchObject() {
		return this.searchObject;
	}

	@Override
	public void setSearchObject(Object newSearchObject) {
		this.searchObject = newSearchObject;
	}

}
