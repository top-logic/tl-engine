/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.List;
import java.util.Set;

import com.top_logic.model.TLClass;

/**
 * If an extended meta element search is used we need for every meta element a
 * list of result columns and a set of excluded meta attributes.
 * 
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface ExtendedSearchModelBuilder extends SearchModelBuilder {

	/**
	 * This method returns the path to the jsp (e.g.v
	 * '/jsp/project/search/SearchInput.jsp') of the given meta element and
	 * NEVER <code>null</code>.
	 * 
	 * @param aMetaElement
	 *            The meta element must NOT be <code>null</code>.
	 */
	public String getJspFor(TLClass aMetaElement);

	/**
	 * This method returns for the given meta element the result columns in list ({@link String}s)
	 * and NEVER <code>null</code>.
	 * 
	 * @param aMetaElement
	 *            The meta element must NOT be <code>null</code>.
	 */
	public List<String> getResultColumnsFor(TLClass aMetaElement);

	/**
	 * This method returns for the given meta element the excluded meta
	 * attributes in a list ({@link String}s) and NEVER <code>null</code>.
	 * This exclude list is only used for the search form and NOT for the
	 * columns choice.
	 * 
	 * @param aMetaElement
	 *            The meta element must NOT be <code>null</code>.
	 */
	public Set<String> getExcludedAttributesForSearch(TLClass aMetaElement);

	/**
	 * This method returns for the given meta element the excluded meta
	 * attributes in a list ({@link String}s) and NEVER <code>null</code>.
	 * This exclude list is only used for the column choice and NOT for the
	 * search form.
	 * 
	 * @param aMetaElement
	 *            The meta element must NOT be <code>null</code>.
	 */
	public Set<String> getExcludedAttributesForColumns(TLClass aMetaElement);
	
	/**
	 * This method returns for the given meta element the excluded meta
	 * attributes for the reporting in a list ({@link String}s) and NEVER
	 * <code>null</code>.
	 * 
	 * @param aMetaElement
	 *            The meta element must NOT be <code>null</code>.
	 */
	public Set<String> getExcludedAttributesForReporting(TLClass aMetaElement);

}
