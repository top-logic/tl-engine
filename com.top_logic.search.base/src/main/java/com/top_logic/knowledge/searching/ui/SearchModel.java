/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.SearchFactory;
import com.top_logic.knowledge.searching.SearchAttribute;
import com.top_logic.knowledge.searching.SearchService;

/**
 * The search model for the searching GUI
 *
 * @author    <a href="mailto:DRO@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class SearchModel implements Serializable {

    /** The maximum number of recently used search terms to be stored */
    public static final int MAX_RECENTLY_TERMS = 10;

    /** The search service to be used for searching information. */
    private transient SearchService searchService;

    /** The map of search attributes supported for search */
    private transient Map searchAttributes;

    /** The map of KO attribute filters */
    private transient Map koAttributeFilters;

    /** The keywords to be used for search (will be placed in query config later). */
    private transient String keywords;
    
    /** The search mode, "AND" or "OR" */
    private transient String mode;
    
    /** The list of selected search engines */
    private transient List selectedEngines;

    /** The recently used search terms */
    private transient LinkedList recentTerms;

    /**
     * Constructor for SearchModel.
     */
    public SearchModel() {
        super();
    }

    /**
     * Return the search attributes supported by the selected 
     * search engines.
     * 
     * @return    The map of search attributes
     */
    public Map getSearchAttributes() {
        if (this.searchAttributes == null) {
            try {
                this.searchAttributes = new HashMap();
                this.searchAttributes.put(SearchAttribute.RANKING.getKey(), SearchAttribute.RANKING.clone());
                this.searchAttributes.put(SearchAttribute.DESCRIPTION.getKey(), SearchAttribute.DESCRIPTION.clone());
                this.searchAttributes.put(SearchAttribute.PROFILES.getKey(), SearchAttribute.PROFILES.clone());
            }
            catch (CloneNotSupportedException ex) {
                this.searchAttributes = new HashMap();
                this.searchAttributes.put(SearchAttribute.RANKING.getKey(), new SearchAttribute(SearchAttribute.RANKING.getKey(), SearchAttribute.RANKING.getRange()));
                this.searchAttributes.put(SearchAttribute.DESCRIPTION.getKey(), new SearchAttribute(SearchAttribute.DESCRIPTION.getKey(), SearchAttribute.DESCRIPTION.getRange()));
                this.searchAttributes.put(SearchAttribute.PROFILES.getKey(), new SearchAttribute(SearchAttribute.PROFILES.getKey(), SearchAttribute.PROFILES.getRange()));
            } 
        }
        
        return (this.searchAttributes);
    }
    
    /**
     * Return a search attribute with the given key
     * 
     * @return    The search attribute with the given key
     */ 
    public SearchAttribute getSearchAttribute(String aKey) {
        return ((SearchAttribute) this.getSearchAttributes().get(aKey));
    }
    
    /**
     * Set the value of the search attribute with the given key.
     * 
     * @param    aKey   The key of the search attribute
     * @param    aValue The value to be set for the attribute
     */
    public void setSearchAttribute(String aKey, Object aValue) {
        SearchAttribute theAttribute = this.getSearchAttribute(aKey);
        if (theAttribute != null) {
            theAttribute.setValue(aValue);
        }
    }

    /**
     * Return the recently used search terms
     * 
     * @return the recently used search terms
     */
    public List getSearchTerms() {
        if (this.recentTerms == null) {
            this.recentTerms = new LinkedList();
        }
        return this.recentTerms;
    }

    /**
     * Return all search engines, which are known by the system.
     * 
     * @return    The list with the known search engines.
     */
    public List getAllSearchEngines() {
        return (new ArrayList(this.getSearchService().getSearchEngines()));
    }
    
    /**
     * Set the list of selected search engines 
     * 
     * @param   someEngines     The list of selected search engines
     */
    public void setSelectedEngines(List someEngines) {
        selectedEngines = someEngines;
    }
    
    /**
     * Get the list of selected search engines
     * 
     * @return the list of  selected search engines
     */
    public List getSelectedEngines() {
        if (selectedEngines == null) {
            selectedEngines = new ArrayList();
        }
        return selectedEngines;
    }

    /**
     * Return the search service to be used for searching.
     * 
     * @return    The search service to be used.
     */
    public SearchService getSearchService() {
        if (this.searchService == null) {
            this.searchService = SearchFactory.getSearchService();
        }

        return (this.searchService);
    }

    /**
     * Add a KO attribute filter
     * 
     * @param    key      The key of the attribute filter to be added
     * @param    value    The value of the attribute filter
     */
    public boolean addKOAttributeFilter(String key, String value) {
        if (this.koAttributeFilters == null) {
            this.koAttributeFilters = new HashMap();
        }
        this.koAttributeFilters.put(key, value);
        return true;
    }
    
    /**
     * Remove a KO attribute filter
     * 
     * @param key The key of the attribute filter to be removed
     */
    public boolean removeKOAttributeFilter(String key) {
        if (this.koAttributeFilters != null) {
            this.koAttributeFilters.remove(key);
        } 
        return true;
    }
    
    /**
     * Return all KO attribute filtes 
     * 
     * @return all KO attribute filters
     */ 
    public Map getKOAttributeFilters() {
        if (this.koAttributeFilters == null) {
            koAttributeFilters = new HashMap();
        }
        return this.koAttributeFilters;
    }

    /**
     * Return the keywords to be used for the query.
     * 
     * @return    The keywords.
     */
    public String getKeywords() {
        return (this.keywords);
    }

    /**
     * Define the keywords to be used for the search.
     * 
     * @param    someKeywords    The keywords to be used for search.
     */
    public void setKeywords(String someKeywords) {
        this.keywords = someKeywords;
        this.addSearchTerm(someKeywords);
    }

    /** 
     * Return the search mode (AND or OR)
     * 
     * @return  The search mode
     */
    public String getMode() {
        return this.mode;
    }
    
    /**
     * Set the search mode, either AND or OR
     * 
     * @param  aMode    The search mode
     */
    public void setMode(String aMode) {
        this.mode = aMode;
    }

    /**
     * Add a search term to the list of recently used search terms.
     * 
     * @param   aTerm   The search term to be added
     */
    public void addSearchTerm(String aTerm) {
        if (this.recentTerms == null) {
            this.recentTerms = new LinkedList();
        }
        
        // add search term, if search term already exists in list 
        // it will be deleted first
        int index = this.recentTerms.indexOf(aTerm);
        if (index > -1) {
            this.recentTerms.remove(index);
        }
        this.recentTerms.addFirst(aTerm);
        
        // check wheter list size exceeds the maximum of MAX_RECENTLY_TERMS
        if (this.recentTerms.size() > MAX_RECENTLY_TERMS) {
            this.recentTerms.removeLast();
        }
    }
    
}
