/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.lucene;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.QueryConfig;
import com.top_logic.knowledge.searching.SearchAttribute;
import com.top_logic.knowledge.searching.SearchException;
import com.top_logic.knowledge.searching.SearchResult;
import com.top_logic.knowledge.searching.SearchResultAttribute;
import com.top_logic.knowledge.searching.SearchResultSetSPI;
import com.top_logic.knowledge.searching.index.IndexQueryVisitor;
import com.top_logic.knowledge.searching.index.IndexSearch;

/** 
 * Searcher thread for the 
 * {@link com.top_logic.knowledge.searching.lucene.LuceneSearchEngine}.
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class LuceneSearch extends IndexSearch {

	private static final boolean DEBUG = Logger.isDebugEnabled(LuceneSearch.class);
    
    /** The Lucene index */
    private LuceneIndex index;
    
    
    
    /**
     * Constructor
     */
    public LuceneSearch(QueryConfig aConfig, SearchResultSetSPI aSet, 
                              LuceneSearchEngine anEngine, 
                              LuceneIndex anIndex) {
        super(aConfig, aSet, anEngine, new IndexQueryVisitor());
        this.index = anIndex;
    } 
    

    /**
	 * Perform the Lucene search for the given Lucene query. If the {@link KnowledgeObject} array is
	 * not empty, only the given {@link KnowledgeObject} will be searched in, otherwise the whole
	 * Lucene index will be searched.
	 * 
	 * @param aQuery
	 *        The query in a Lucene understandable form
	 * @param objects
	 *        The objects to be searched in, can be null
	 * @param searchAttributes
	 *        The list of search attributes, never null
	 */
    @Override
	protected void search(String aQuery, KnowledgeObject[] objects, List searchAttributes) 
        throws SearchException {
        
        String escapedQuery = aQuery;
        
        String[] types = this.config.getKOTypesForLucene();
        if (types != null && types.length > 0) {
            StringBuffer append = new StringBuffer(escapedQuery);
            append.append(" AND (");
            for (int i=0; i<types.length; i++) {
                append.append(" ").append(LuceneIndex.FIELD_DOCTYPE).append(":").append(types[i]).append(" ");
                if (i < types.length-1) {
                    append.append(" OR");
                }
            }
            append.append(")");
            escapedQuery = append.toString();
        }
		IndexSearcher theSearcher = this.index.getSearcher();
		try {
        	// 1. convert query expression to a Lucene understandable query
			QueryParser queryParser = this.index.getQueryParser(LuceneIndex.FIELD_CONTENTS);
			Query parserQuery = queryParser.parse(escapedQuery);
			
			// 2. perform search
            TopDocs theHits = theSearcher.search(parserQuery, Integer.MAX_VALUE);

            // 3. add search attributes and convert hits to list of SearchResults
            if (objects == null || objects.length == 0) {
                this.convertHits(theHits, searchAttributes, theSearcher);
            }
            else {
                this.convertHitsFiltered(theHits, searchAttributes, Arrays.asList(objects), theSearcher);
            }
        }
        catch (IOException ioe) {
            Logger.error("Could not find expression in Lucene index.", ioe, this);
            throw new SearchException(ioe.toString());
        }
        catch (ParseException pe) {
            Logger.error("Could not parse the search expression '" + escapedQuery + "'.", pe, this);
            throw new SearchException(pe.toString());
        } 
        catch (Exception ex) {
            Logger.error("An error occured during seach process.", ex, this);
            throw new SearchException(ex.toString());
		}
    }
    
    /**
	 * Convert the found Lucene hits to search results and add search result attributes depending on
	 * the given list of search attributes.
	 *
	 * @param hits
	 *        The hits collection with the found Lucene documents
	 * @param searchAttributes
	 *        The search (result) attributes to be included for each hit
	 * @param searcher
	 *        the {@link IndexSearcher} used to find the given hits
	 * @throws SearchException
	 *         in case the hits cannot be resolved
	 */
	protected void convertHits(TopDocs hits, List<? extends SearchResultAttribute> searchAttributes,
			IndexSearcher searcher)
        throws SearchException {
            
        try {
            Float minScore = null;
            int idxRanking = searchAttributes.indexOf(SearchAttribute.RANKING);
            if (idxRanking != -1) {
            	SearchResultAttribute theRankingAttr = searchAttributes.get(idxRanking);
                minScore = (Float)theRankingAttr.getValue();
            }
            int idxDescr = searchAttributes.indexOf(SearchAttribute.DESCRIPTION);
            SearchResultAttribute descrAttribute = null;
            if (idxDescr != -1) {
                descrAttribute = searchAttributes.get(idxDescr);
            }
            StringBuffer description = null; // recycling Buffer for descriptions
            final ScoreDoc[] scoreDocs = hits.scoreDocs;
            for (int i = 0, size = scoreDocs.length; i < size; i++) {
				Document theDoc = searcher.doc(scoreDocs[i].doc);
				TLID koID = IdentifierUtil.fromExternalForm(theDoc.get(LuceneIndex.FIELD_KO_ID));
                String          docType = theDoc.get(LuceneIndex.FIELD_DOCTYPE);
                KnowledgeObject theKO   = LuceneIndex.getKO(koID, docType, this.kbase);
                
                if (theKO != null) {
                    SearchResult theResult = new SearchResult(theKO, this.engine);
    
                    // check search attribute SearchAttribute.RANKING
                    if (idxRanking != -1) {
                        // get the score of the current result
                        float score = scoreDocs[i].score;
						Float scoreValue = Float.valueOf(score);
                        if (scoreValue.floatValue() >= minScore.floatValue()) {
                            theResult.addSearchResultAttribute(
                                new SearchResultAttribute(
                                SearchAttribute.RANKING.getKey(), 
                                scoreValue)
                            );
                        } 
                        else {
                            continue;
                        }
                    }
                    // search attribute SearchAttribute.DESCRIPTION
                    if (descrAttribute != null && ((Boolean)descrAttribute.getValue()).booleanValue() == true) {
                    	SearchResultAttribute theDescr = searchAttributes.get(idxDescr);
                        if (((Boolean)theDescr.getValue()).equals(Boolean.TRUE)) {
                            Document luceneDocument = searcher.doc(scoreDocs[i].doc);
							IndexableField descriptionField = luceneDocument.getField(LuceneIndex.FIELD_DESCRIPTION);
                            if (description == null) {
                                description = new StringBuffer(); 
                            } else {
                                description.setLength(0); // recyle String Buffer
                            }
                            description.append(descriptionField.stringValue()).append("...");
                            theResult.addSearchResultAttribute(
                                new SearchResultAttribute(
                                SearchAttribute.DESCRIPTION.getKey(), 
                                description.toString())
                            );
                        }
                    }
    
                    searchResults.add (theResult);
                }
            }
        }
        catch (IOException ioe) {
            Logger.error("Could not resolve Lucene hits.", ioe, this);
            throw new SearchException(ioe.toString());
        }
    }
    

    /**
     * Convert the found Lucene hits to search results
     * and add search result attributes depending on the 
     * given list of search attributes. The given list
     * of searchable documents will sort out the results
     * that are not part of this list.
     *
     * @param    hits               The hits collection with the found Lucene documents
     * @param    searchAttributes   The search (result) attributes to be included for each hit
     * @param    searchableDocs     The documents to be searched in, never <code>null</code>
     * @throws SearchException in case the hits cannot be resolved
     */
	protected void convertHitsFiltered(TopDocs hits, List searchAttributes, List searchableDocs, IndexSearcher searcher)
        throws SearchException {
            
        try {
            Float minScore = null;
            int idxRanking = searchAttributes.indexOf(SearchAttribute.RANKING);
            if (idxRanking != -1) {
                SearchAttribute theRankingAttr = (SearchAttribute) searchAttributes.get(idxRanking);
                minScore = (Float)theRankingAttr.getValue();
            }
            int idxDescr = searchAttributes.indexOf(SearchAttribute.DESCRIPTION);
            SearchAttribute descrAttribute = null;
            if (idxDescr != -1) {
                descrAttribute = (SearchAttribute) searchAttributes.get(idxDescr);
            }
            StringBuffer description = new StringBuffer();
            final ScoreDoc[] scoreDocs = hits.scoreDocs;
            for (int i = 0, size = scoreDocs.length; i < size; i++) {
				Document theDoc = searcher.doc(scoreDocs[i].doc);
				TLID koID = IdentifierUtil.fromExternalForm(theDoc.get(LuceneIndex.FIELD_KO_ID));
                String docType = theDoc.get(LuceneIndex.FIELD_DOCTYPE);
                KnowledgeObject theKO = LuceneIndex.getKO(koID, docType, this.kbase);
                
                if (theKO != null && searchableDocs.contains(theKO)) {
                    SearchResult theResult = new SearchResult(theKO, this.engine);
    
                    // check search attribute SearchAttribute.RANKING
                    if (idxRanking != -1) {
                        // get the score of the current result
                        float score = scoreDocs[i].score;
						Float scoreValue = Float.valueOf(score);
                        if (scoreValue.floatValue() >= minScore.floatValue()) {
                            theResult.addSearchResultAttribute(
                                new SearchResultAttribute(
                                SearchAttribute.RANKING.getKey(), 
                                scoreValue)
                            );
                        } 
                        else {
                            continue;
                        }
                    }
                    // search attribute SearchAttribute.DESCRIPTION
                    if (descrAttribute != null && ((Boolean)descrAttribute.getValue()).booleanValue() == true) {
                        SearchAttribute theDescr = (SearchAttribute) searchAttributes.get(idxDescr);
                        if (((Boolean)theDescr.getValue()).equals(Boolean.TRUE)) {
                            Document luceneDocument = searcher.doc(scoreDocs[i].doc);
							IndexableField descriptionField = luceneDocument.getField(LuceneIndex.FIELD_DESCRIPTION);
                            description.setLength(0);
                            description.append(descriptionField.stringValue()).append("...");
                            theResult.addSearchResultAttribute(
                                new SearchResultAttribute(
                                SearchAttribute.DESCRIPTION.getKey(), 
                                description.toString())
                            );
                        }
                    }
    
                    searchResults.add (theResult);
                }
            }
        }
        catch (IOException ioe) {
            Logger.error("Could not resolve Lucene hits.", ioe, this);
            throw new SearchException(ioe.toString());
        }
    }
}
