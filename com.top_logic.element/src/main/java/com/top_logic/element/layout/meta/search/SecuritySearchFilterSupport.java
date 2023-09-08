/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.element.meta.query.SecurityFilter;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundHelper;

/**
 * Add {@link SecurityFilter} that delegates security checks to the default checker for objects of
 * the selected {@link TLClass}.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class SecuritySearchFilterSupport extends SearchFilterSupport {

	/**
	 * Singleton of {@link SecuritySearchFilterSupport}
	 */
	@SuppressWarnings("hiding")
	public static final SearchFilterSupport INSTANCE = new SecuritySearchFilterSupport();

	/**
	 * Create a new {@link SecuritySearchFilterSupport}
	 */
	public SecuritySearchFilterSupport() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<CollectionFilter> getFilters(AttributeFormContext context, boolean extendedSearch) {
		List<CollectionFilter> filters = super.getFilters(context, extendedSearch);
		SecurityFilter<?> securityFilter = getSecurityFilter(context);

		if (securityFilter != null) {
			filters.add(securityFilter);
		}

		return filters;
	}

	/**
	 * Return the security filter to be used.
	 * 
	 * @param context
	 *        The context to get additional information from.
	 * @return The requested security filter, <code>null</code> will ignore the security.
	 */
	protected SecurityFilter<?> getSecurityFilter(AttributeFormContext context) {
		FormHandler form = context.getOwningModel();

		if (form instanceof AttributedSearchComponent) {
			AttributedSearchComponent searchComponent = (AttributedSearchComponent) form;
			TLClass type = searchComponent.getSearchMetaElement();

			return (type != null) ? SecuritySearchFilterSupport.getSecurityFilter(searchComponent.getMainLayout(), type)
				: null;
		}

		return null;
	}

	/**
	 * Return {@link SecurityFilter} with default checker for objects of {@link TLClass}-type
	 */
	public static SecurityFilter<?> getSecurityFilter(MainLayout rootLayout, TLClass type) {
		Collection<BoundChecker> defaultCheckers = BoundHelper.getInstance().getDefaultCheckersForType(type);
		BoundChecker checker = CollectionUtil.getFirst(defaultCheckers);

		return (checker != null) ? new SecurityFilter<>(checker) : null;
	}
}
