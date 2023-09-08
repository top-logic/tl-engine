/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import java.util.List;

import org.jfree.chart.urls.CategoryURLGenerator;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;

/**
 * Allow a GOTO from {@link ChartGotoAware} via a {@link CategoryURLGenerator} and a {@link AttributedSearchResultSet}
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ChartGotoAwareGotoHandler extends AbstractChartButtonCommandHandler {

    /** 
     * Create a new ChartGotoAwareGotoHandler which will #showEmptyResult
     */
    public ChartGotoAwareGotoHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /** 
     * Return a search result set to fire as new selection for a component.
     * 
     * @param    aComponent    The component calling, must not be <code>null</code>.
     * @param    someItems     The objects to be displayed, must not be <code>null</code>.
     * @param    aName         The name of the used chart, must not be <code>null</code>.
     * @return   The search result set to be fired, never <code>null</code>.
     */
    protected AttributedSearchResultSet getSearchResultSet(ChartGotoAware aComponent, List someItems, String aName) {
		TLClass theME = (TLClass) ((Wrapper) someItems.get(0)).tType();

        return new AttributedSearchResultSet(someItems, theME, aComponent.getSearchResultColumns(), null);
    }

	@Override
	protected void showValues(ChartGotoAware theComponent, String theName, List theItems) {
        AttributedSearchResultSet theResult = this.getSearchResultSet(theComponent, theItems, theName);

		throw new UnsupportedOperationException("Make something visible.");
	}
}
