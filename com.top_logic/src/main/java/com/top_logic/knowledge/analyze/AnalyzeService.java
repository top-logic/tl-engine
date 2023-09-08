/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze;

import java.util.Collection;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * This service knowas about similar Documents.
 * <p>
 * Profiles where once Collections of Documents rated positiv or negativ.
 * So Documents in the context of a Profile should match mostly
 * the positive Documents and supress Documents similar to the negative
 * rated ones.
 * </p><p>
 * In case the service failes (hopefully only transient) no Exceptions
 * will be thrown, instead we will return "empty" default results and
 * Log the failures. So Exceptions should ususally indicate logical
 * Problems in a otherwise correctly working service.
 * </p>
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public interface AnalyzeService {

    /** The name this service can be located in the class manager. */
    public static final String SERVICE_NAME = "AnalyzeService";

    /**
     * Get the knowledge base for the knowledge objects to be analyzed.
     * 
     * @return the knowledge base 
     */ 
    public KnowledgeBase getKnowledgeBase();

    /**
     * Check if the analyze service is available. In case the service 
     * is not available a fallback service could be used by the caller.
     * 
     * @return <code>true</code> if service is availabe
     */
    public boolean serviceAvailable();

    // Find similar

    /**
     * Find similar documents
     * 
     * @param document    The document to find similar documents for
     * @return a collection of knowledge objects
     *         or an empty collection in case nothing was found.
     * 
     * @throws AnalyzeException in case of a error
     * @throws UnsupportedOperationException in case operation is not supported
     */
    public Collection<? extends KnowledgeObject> findSimilar(KnowledgeObject document)
        throws AnalyzeException, UnsupportedOperationException;
    
    /**
     * Find similar documents with ranking information
     * 
     * @param   document    The document to find similar for
     * @return a collection of 
     *         {@link KnowledgeObjectResult}
     *         or an empty collection in case nothing was found.
     * 
     * @throws AnalyzeException in case of an error
     * @throws UnsupportedOperationException in case operation is not supported
     */
    public Collection<? extends KnowledgeObjectResult> findSimilarRanked(KnowledgeObject document)
        throws AnalyzeException, UnsupportedOperationException;
        

    // Keyword extraction
    
    /**
     * Extract keywords from a given document.
     * 
     * @param    document    The document to extract the keywords from.
     * @return   A collection of {@link String}
     *           for the given document or an empty collection in case 
     *           nothing was found. 
     * 
     * @throws AnalyzeException in case of an error
     * @throws UnsupportedOperationException in case operation is not supported
     */
    public Collection<String> extractKeywords(KnowledgeObject document)
        throws AnalyzeException, UnsupportedOperationException;

}
