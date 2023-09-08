/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.NotEqualsFilter;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Accessing helper for using the {@link com.top_logic.knowledge.analyze.AnalyzeService}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class AnalyseHelper {

    /**
     * Default CTor.
     */
    public AnalyseHelper() {
        super();
    }

    /**
     * Return the list of similar documents for the given object.
     *  
     * @param    aKO    The knowledge object to be analysed.
     * @return   The collection of similar information, will never be <code>null</code>.
     */
	public Collection<? extends KnowledgeObjectResult> searchSimilar(KnowledgeObject aKO) {
        return (this.searchSimilar(aKO, null));
    }

    /**
     * Return the list of similar documents for the given object.
     *  
     * @param    aKO        The knowledge object to be analysed.
     * @param    aFilter    The filter to be used additionaly for filtering.
     * @return   The collection of similar information, will never be <code>null</code>.
     */
	public Collection<? extends KnowledgeObjectResult> searchSimilar(KnowledgeObject aKO,
			Filter<? super KnowledgeObject> aFilter) {

        if (aKO != null) {
			AnalyzeService theService = DefaultAnalyzeService.getAnalyzeService();

            // perform search if service and object are available
            if (theService != null) {
                KnowledgeObjectResult theKOR;
                KnowledgeObject       theCurr;
				Filter<? super KnowledgeObject> theFilter = new NotEqualsFilter(aKO);

                if (aFilter != null) {
					theFilter = FilterFactory.and(theFilter, aFilter);
                }

                try {
					Collection<? extends KnowledgeObjectResult> result = theService.findSimilarRanked(aKO);
					Iterator<? extends KnowledgeObjectResult> theIt = result.iterator();
					while (theIt.hasNext()) {
						theKOR = theIt.next();
                        theCurr = theKOR.getKnowledgeObject();

                        if (!theFilter.accept(theCurr)) {
							theIt.remove();
                        }
                    }
					return result;
                }
                catch (Exception ex) {
                    Logger.error ("Unable to find similar data for" + 
                                  aKO.tId(), ex, this);
                }
            }
        }

		return Collections.emptyList();
    }

    /**
     * Search keywords for the given object.
     *
     * @param    aKO    The object to be analysed.
     * @return   The collection of found keywords.
     */
	public Collection<String> searchKeywords(KnowledgeObject aKO) {
        if (aKO != null) {
			AnalyzeService theService = DefaultAnalyzeService.getAnalyzeService();

            if (theService != null) {
                try {
					return theService.extractKeywords(aKO);
                }
                catch (Exception ex) {
                    Logger.error ("Unable to find keywords for " + 
                                  aKO.tId(), ex, this);
                }
            }
        }

		return new ArrayList<>();
    }

}
