/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.knowledge.objects.KnowledgeObject;

/** 
 * One result from a list of found objects.
 * 
 * This instance hold all information about a found knowledge object. It
 * provides the list of all search engines, which found the object and also
 * additional information describing the quality of the found object. The
 * kind of quality can differ from engine to engine, but this result only
 * collects that information and the processing class is responsible for
 * the interpretation of this information.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class SearchResult implements com.top_logic.knowledge.analyze.SearchResult {

    /** The found knowledge object. */
    private KnowledgeObject object;

    /** The engines, which have found the object. */
    private List engines;

    /** Additional attributes for more information about the quality of hit. */
    private List attributes;

    // Contructors

    /**
     * Constructor for SearchResult.
     * 
     * @param    anObject   The found knowledge object.
     * @param    anEngine   The search engine that found the knowledge object.
     * @throws   IllegalArgumentException    If the given object is 
     *                                       <code>null</code>.
     */
    public SearchResult(KnowledgeObject anObject, SearchEngine anEngine) 
                                            throws IllegalArgumentException {
        super();

        if (anObject == null) {
            throw new IllegalArgumentException("Given knowledge object is null");
        }
        if (anEngine == null) {
            throw new IllegalArgumentException("Given search engine is null");
        }

        this.object = anObject;
        this.addSearchEngine(anEngine);
    }

    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
                        + "object: " + this.object
                        + ']');
    }

    @Override
	public int hashCode() {
        return (this.object.hashCode());
    }

    @Override
	public boolean equals(Object anObject) {
		if (anObject == this) {
			return true;
		}
        if (anObject instanceof SearchResult) {
            return (((SearchResult) anObject).object.equals(this.object));
        }
        else {
            return (false);
        }
    }

	@Override
	public Object getResult() {
		return getObject();
	}

	@Override
	public double getRelevance() {
		return 1.0;
	}

    /**
     * Return the knowledge object held by this result.
     * 
     * @return    The knowledge object, must not be <code>null</code>.
     */
    public KnowledgeObject getObject() {
        return (this.object);
    }

    /**
     * Return the list of {@link com.top_logic.knowledge.searching.SearchEngine engines}
     * finding the held knowledge object.
     * 
     * @return    The list of engines.
     * @see       com.top_logic.knowledge.searching.SearchEngine
     */
    public List getEngines() {
        if (this.engines == null) {
            this.engines = new ArrayList();
        }

        return (this.engines);
    }

    /**
     * Return the list of {@link com.top_logic.knowledge.searching.SearchResultAttribute attributes}
     * finding the held knowledge object.
     * 
     * @return    The list of engines.
     * @see       com.top_logic.knowledge.searching.SearchResultAttribute
     */
    public List getAttributes() {
        if (this.attributes == null) {
            this.attributes = new ArrayList();
        }

        return (this.attributes);
    }

    /**
     * Append an engine to the list of engines finding the knowledge object.
     *
     * @param    anEngine    The engine to be appended.
     * @return   <code>true</code>, if this collection changed as a result 
     *           of the call.
     */
    public boolean addSearchEngine(SearchEngine anEngine) {
        return (this.getEngines().add(anEngine));
    }

    /**
     * Remove the given engine from the list of engines finding the 
     * knowledge object.
     *
     * @param    anEngine    The engine to be removed.
     * @return   <code>true</code>, if this collection changed as a result 
     *           of the call.
     */
    public boolean removeSearchEngine(SearchEngine anEngine) {
        return (this.getEngines().remove(anEngine));
    }

    /**
     * Append an attribute to the list of attributes qualifying the 
     * search result.
     *
     * @param    anAttr    The attributes to be appended.
     * @return   <code>true</code>, if this collection changed as a result 
     *           of the call.
     */
    public boolean addSearchResultAttribute(SearchResultAttribute anAttr) {
        return (this.getAttributes().add(anAttr));
    }

    /**
     * Remove the given attribute from the list of attributes.
     *
     * @param    anAttr    The attributes to be removed.
     * @return   <code>true</code>, if this collection changed as a result 
     *           of the call.
     */
    public boolean removeSearchResultAttribute(SearchResultAttribute anAttr) {
        return (this.getAttributes().remove(anAttr));
    }

}
