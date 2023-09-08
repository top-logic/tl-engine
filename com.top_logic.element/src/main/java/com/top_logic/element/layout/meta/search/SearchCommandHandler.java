/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * Command for executing a search.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SearchCommandHandler extends AJAXCommandHandler {

    public static final String COMMAND_ID = "searchAttributed";

    public SearchCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * @see com.top_logic.tool.boundsec.CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        HandlerResult theResult = new HandlerResult();
        SearchResultSet theSearchResult = null;
        
		AttributedSearchComponent theComp = (AttributedSearchComponent) aComponent;
        try {
            FormContext               theContext = theComp.getFormContext();

            if (theContext.checkAll()) {
                DefaultProgressInfo theInfo = new DefaultProgressInfo();

                theInfo.setExpected(5);

            	theSearchResult = this.search(theComp, theContext, someArguments, theInfo);
            }
            else {
				AbstractApplyCommandHandler.fillHandlerResultWithErrors(theContext, theResult);
            }
        }
        catch (Exception ex) {
            Logger.error("Unable to search in " + aComponent, ex, this);

			theResult.addErrorText(ex.toString());
        }
        if(theSearchResult != null) {
			theComp.displaySearchResult(theSearchResult);
        }

        return theResult;
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.START_SEARCH;
    }

    /** 
     * Execute a search on the information contained in the given context.
     * 
     * This method will call the {@link AttributedSearchComponent#getFilters(FormContext, Map)}
     * and {@link AttributedSearchComponent#getSearchElements()} methods to prepare the search.
     * 
     * @param    aComp       The search component, must not be <code>null</code>.
     * @param    aContext    The form context to be filled, must not be <code>null</code>.
     * @param    someArgs    Additional arguments to be used during search, must not be <code>null</code>.
     * @param    anInfo      The progress information for the search, must not be <code>null</code>.
     * @return   <code>null</code>, if search succeeds or a message describing the error.
     */
    public SearchResultSet search(AttributedSearchComponent aComp, FormContext aContext, Map someArgs, DefaultProgressInfo anInfo) throws TopLogicException {
        // Collect needed filters
		anInfo.incCurrentKey(I18NConstants.PHASE_LOADING_FILTERS);
        List<CollectionFilter> theFilters = aComp.getFilters(aContext, someArgs);

        // Now get the search elements
        anInfo.setExpected(anInfo.getExpected() + theFilters.size());
		anInfo.incCurrentKey(I18NConstants.PHASE_LOADING_DATA);
        Collection<? extends Wrapper> theElements = aComp.getSearchElements();

        // Get the result columns
		anInfo.incCurrentKey(I18NConstants.PHASE_BUILDING_RESULT_COLUMNS);
        List<String> theColumns = aComp.getResultColumns(aContext);

        // Get the search element
		anInfo.incCurrentKey(I18NConstants.PHASE_RESOLVING_SEARCH_TYPE);
        TLClass theME = aComp.getSearchMetaElement();

        // Do the real search
		anInfo.incCurrentKey(I18NConstants.PHASE_EXECUTING_SEARCH);
        return this.search(theFilters, theElements, theColumns, theME, anInfo);
    }

    /** 
     * Execute the search on the given attributes.
     * 
     * @param    someFilters       The filters to apply to the attributed, must not be <code>null</code>.
     * @param    someAttributes    The collection of attributed to be inspected, must not be <code>null</code>.
     * @param    someColumns       The list of column names to be displayed later on, may be <code>null</code>.
     * @return   The requested result set, never <code>null</code>.
     * @throws   TopLogicException    If search failed for a reason.
     * @deprecated    Use method {@link #search(List, Collection, List, TLClass, DefaultProgressInfo)}
     */
    @Deprecated
    public final SearchResultSet search(List<CollectionFilter> someFilters, Collection<? extends Wrapper> someAttributes, List<String> someColumns, TLClass aME) throws TopLogicException {
        return this.search(someFilters, someAttributes, someColumns, aME, new DefaultProgressInfo());
    }

    /** 
     * Execute the search on the given attributes.
     * 
     * @param    someFilters       The filters to apply to the attributed, must not be <code>null</code>.
     * @param    someAttributes    The collection of attributed to be inspected, must not be <code>null</code>.
     * @param    someColumns       The list of column names to be displayed later on, may be <code>null</code>.
     * @return   The requested result set, never <code>null</code>.
     * @throws   TopLogicException    If search failed for a reason.
     */
    public SearchResultSet search(List<CollectionFilter> someFilters, Collection<? extends Wrapper> someAttributes, List<String> someColumns, TLClass aME, DefaultProgressInfo anInfo) throws TopLogicException {
        try {
            Collection<Wrapper> theObjects = new ArrayList<>(someAttributes);
            int                    theSize    = someFilters.size();
            int                    thePos     = 1;

            Collections.sort(someFilters); // Sort the filters by relevance

            // TODO MGA/KBU update/reset current SQ???

            for (CollectionFilter theFilter : someFilters) {
				anInfo.incCurrentKey(I18NConstants.PHASE_FILTER__POS_SIZE.fill(
					thePos++, theSize));
                theObjects = theFilter.filter(theObjects);

                if (theObjects.isEmpty()) {
                    break;
                }
            }

            return this.createSearchResultSet(theObjects, aME, someColumns, Collections.emptyList());
        } 
        catch (Exception ex) {
            throw new TopLogicException(this.getClass(), "query.search.failed", ex);
        }
    }

    protected SearchResultSet createSearchResultSet(Collection<Wrapper> filteredObjects, TLClass aME, List<String> someColumns, List someMessage) {
    	return new AttributedSearchResultSet(filteredObjects, aME, someColumns, someMessage);
    }

}

