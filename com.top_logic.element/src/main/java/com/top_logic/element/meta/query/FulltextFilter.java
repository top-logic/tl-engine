/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.search.Query;
import com.top_logic.base.services.SearchFactory;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.knowledge.searching.QueryConfig;
import com.top_logic.knowledge.searching.SearchResult;
import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.knowledge.searching.SearchService;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;

/**
 * Filter for full text search.
 * 
 * @author    <a href="mailto:fma@top-logic.com">Franka Musz</a>
 */
public class FulltextFilter extends BasicCollectionFilter {

    /** key for value Map to store pattern. */
    public static final String KEY_PATTERN = "pattern";

    /** key for value Map to store mode flag. */
    public static final String KEY_MODE    = "mode";
 
    /** AND Mode. */
    public static final boolean MODE_AND    = true;

    /** Do an exact search */
    public static final boolean EXACT_MATCH    = true;
    
    /** The pattern to search for */
	private String pattern;
    
    /** true: use AND, false use OR */
	private boolean mode;

	/** If false, a search with wildcards will be done  */
	private boolean exactMatch;
	
    /**
     * Samll sort order, since a FullTextfilter should be quite selective.
     */
	protected static final Integer SORT_ORDER = Integer.valueOf(9);

    private String[] koTypesForLucene;
    
	/**
	 * CTor with pattern and search mode
	 * 
	 * @param aPattern	the search pattern
	 * @param aMode 	define how the words of the search pattern are combined in the search
	 * 					- AND or OR search, true for AND
	 * @param doNegate 	 the negation flag
	 * @param isRelevant the relevant flag
	 */
	public FulltextFilter(String[] koTypesForLucene, String aPattern, boolean aMode, boolean exactMatch, boolean doNegate, boolean isRelevant) {
		super(doNegate, isRelevant);
		
		this.exactMatch = exactMatch;
		this.pattern = aPattern;
		this.mode    = aMode;
		this.koTypesForLucene = koTypesForLucene;
	}

	/**
	 * CTor that gets all values from a Map.
	 * 
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
     * 
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 *         or some of its values do not match the filter's constraints.
	 */
	public FulltextFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);
	}

	/**
	 * @see com.top_logic.element.meta.query.CollectionFilter#filter(java.util.Collection)
	 */
	@Override
	public Collection filter(Collection aList)
			throws NoSuchAttributeException, AttributeException {
    	if (!this.isRelevant()) {
    		return aList;
    	}
    	
		Collection theDocs = this.getDocumentResult();
        theDocs = this.getSearchObjectsForDocuments(theDocs);
		if (this.getNegate()) {
			aList.removeAll(theDocs);
		}
		else {
			aList.retainAll(theDocs);
		}

		return aList;
	}
    
    /**
     * Get the objects found objects belong to.
     * 
     * @param aDocs the objects found by the search engine
     * @return the objects found objects belong to.
     */
    protected Collection getSearchObjectsForDocuments(Collection aDocs) {
       
        if (aDocs != null && !aDocs.isEmpty()) {
            Set            theObjects = new HashSet(aDocs.size());
            Iterator       theDocs    = aDocs.iterator();
            while (theDocs.hasNext()) {
                SearchResult theRes = (SearchResult) theDocs.next();
				TLObject theDoc = WrapperFactory.getWrapper(theRes.getObject());
				TLObject theObject = this.getSearchObjectFor(theDoc);
				if (theObject != null) {
				    theObjects.add(theObject);
				}
            }
            return theObjects;
        }
        
        return  new HashSet();
    }
	
	/**
     * Get the object the found object belongs to.
     * 
     * This allows subclasses to reolve the "Owner" of the Document.
     * 
	 * @param aDoc the object found by the search engine
	 * @return the object the found object belongs to
	 */
	protected TLObject getSearchObjectFor(TLObject aDoc) {
        return aDoc;
    }

    /**
	 * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
	 */
	@Override
	public Integer getSortOrder() {
		return SORT_ORDER;
	}
	
	/**
	* Return the result from the search service.
    * 
    * TODO KBU/KHA start search in CTor so search can start in Background?
	* 
	* The returned list will not be checked for security, 
    * this has to be done elsewhere.
	* 
	* @return    The result of the search, must not be <code>null</code>.
	*/
   private List getDocumentResult() {
	   SearchService theService = this.getSearchService();

	   try {
	       QueryConfig     theConf  = new QueryConfig();
		   Query           theQuery = Query.getFullTextQuery(pattern, this.mode, this.exactMatch);
		   SearchResultSet theSet   = null;
		   theConf.setQuery(theQuery);
		   theConf.setKOTypesForLucene(this.koTypesForLucene);

		   theSet = theService.search(theConf);
			theSet.waitForClosed(50000);
		   return theSet.getSearchResults();
	   } 
	   catch (Exception ex) {
		   Logger.info ("Exception in search: ", ex, this);
	   }

	   return new ArrayList();
   }
   		
   /**
	* Return the service responsible for search.
	*  
	* @return    The currently configured search service.
	*/
	private SearchService getSearchService() {
		try {
			return SearchFactory.getSearchService();
		}
		catch (Exception ex) {
			Logger.error("Unable to get search service!", ex, this);
		}	
		return null;
	}
	  
	/**
	 * Add pattern and mode
	 * 
     * @see com.top_logic.element.meta.query.CollectionFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = super.getValueMap();

        theMap.put(KEY_PATTERN, this.pattern);
        theMap.put(KEY_MODE,      Boolean.valueOf(this.mode));
        theMap.put("exactMatch",  Boolean.valueOf(this.exactMatch));
        if (this.koTypesForLucene != null) {
            theMap.put("koTypesForLucene", StringServices.toString(this.koTypesForLucene, ","));
        }
        return theMap;
    }
    
    /**
     * Get and set pattern and mode
     * 
     * @see com.top_logic.element.meta.query.CollectionFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        Object thePattern = aValueMap.get(KEY_PATTERN);
        if (!(thePattern instanceof String)) {	// No null value allowed!!
            throw new IllegalArgumentException ("Value for key " + KEY_PATTERN + " must be a String!");
        }
        this.pattern = (String) thePattern;

        Object theMode = aValueMap.get(KEY_MODE);
        if (!(theMode instanceof Boolean)) {
            throw new IllegalArgumentException ("Value for key " + KEY_MODE + " must be a Boolean!");
        }
        this.mode = ((Boolean) theMode).booleanValue();

        Object wildCards = aValueMap.get("exactMatch");
        if (!(wildCards instanceof Boolean)) {
            throw new IllegalArgumentException ("Value for key exactMatch must be a Boolean!");
        }
        this.exactMatch = ((Boolean) wildCards).booleanValue();
        
        Object koTypes = aValueMap.get("koTypesForLucene");
        if (koTypes instanceof String) {
            this.koTypesForLucene = StringServices.split((String) koTypes, ',');
        }
    }
    
    /**
     * Get the current search pattern
     * 
     * @return  the current search pattern
     */
    public String getPattern() {
        return this.pattern;
    }
    
    /**
     * Get the current search mode
     * 
     * @return  the current search mode
     */
    public boolean getMode() {
        return this.mode;
    }
    
    public boolean getExactMatch() {
        return this.exactMatch;
    }

}
