/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

/** 
 * Additional filter the result set, which will be applied after the search
 * succeeds.
 * 
 * The filter will be used to filter out information, which can only be done,
 * when all search engines have finished their work.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface Filter {

    /**
     * Filter the given result set and eliminate all elements, which are
     * not conform to this filter.
     * 
     * @param    aResultSet    The set to be filtered.
     * @return   The filtered result set.
     */
    public SearchResultSet filter(SearchResultSet aResultSet);
}
