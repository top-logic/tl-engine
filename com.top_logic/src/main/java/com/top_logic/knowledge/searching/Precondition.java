/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.util.List;
import java.util.Vector;

import com.top_logic.knowledge.objects.KnowledgeObject;

/** 
 * The preconditions define the conditions the search service has to attend
 * when searching.
 *
 * The conditions can define the search engines to ask for answer, the objects
 * to be considered and also additional attributes selected by the user for
 * getting a better quality and substantiation of the search results.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Precondition {

    /** The list of engines to be visited. */
	private List<SearchEngine> engines;

    /** The list of additional search attributes as defined by the user. */
	private List<SearchAttribute> attributes;

    /** The objects the user wants to contain in his search. */
    private KnowledgeObject[] objects;
    
    /** 
     * The knowledge base to be used for searching. Search engines that
     * do not belong to this KB will be ignored.
     */
    private String kbName;

    /**
     * Constructor for Precondition.
     */
    public Precondition() {
        super();
    }

    /**
     * Return the search engines to be used for search.
     * 
     * If the list of engines to be inspected is empty, the search service
     * will automatically ask all known engines for answer.
     * 
     * @return    The list of engines, must not be <code>null</code>.
     */    
    public List<SearchEngine> getSearchEngines() {
        if (this.engines == null) {
			this.engines = new Vector<>();
        }

        return (this.engines);
    }

    /**
     * Append the given search engine to the list of engines.
     * 
     * @param    anEngine    The engine to be added.
     * @return   <code>true</code>, if this collection changed as a result 
     *           of the call.
     */
    public boolean addSearchEngine(SearchEngine anEngine) {
        return (this.getSearchEngines().add(anEngine));
    }

    /**
     * Remove the given search engine from the list of engines.
     * 
     * @param    anEngine    The engine to be removed.
     * @return   <code>true</code>, if this collection changed as a result 
     *           of the call.
     */
    public boolean removeSearchEngine(SearchEngine anEngine) {
        return (this.getSearchEngines().remove(anEngine));
    }

    /**
     * Return the search attributes to be used for search.
     * 
     * @return    The list of attributes, must not be <code>null</code>.
     */    
	public List<SearchAttribute> getSearchAttributes() {
        if (this.attributes == null) {
			this.attributes = new Vector<>();
        }

        return (this.attributes);
    }

    /**
     * Append the given search attribute to the list of attributes.
     * 
     * @param    anAttr    The attribute to be added.
     * @return   <code>true</code>, if this collection changed as a result 
     *           of the call.
     */
    public boolean addSearchAttributes(SearchAttribute anAttr) {
        return (this.getSearchAttributes().add(anAttr));
    }

    /**
     * Remove the given search attribute from the list of attributes.
     * 
     * @param    anAttr    The attribute to be removed.
     * @return   <code>true</code>, if this collection changed as a result 
     *           of the call.
     */
    public boolean removeSearchAttributes(SearchAttribute anAttr) {
        return (this.getSearchAttributes().remove(anAttr));
    }

    /**
     * Return the array of all knowledge objects to be used within the search.
     * 
     * If the returned value is <code>null</code> or an empty array, the
     * search engine has to search in all knowledge objects.
     * 
     * @return    The array of objects to be used for search.
     */
    public KnowledgeObject[] getKnowledgeObjects() {
        return (this.objects);
    }

    /**
     * Set the list of knowledge objects to be used within the search.
     * 
     * If the given list is <code>null</code> or an empty list, the
     * search engine has to search in all knowledge objects.
     * 
     * @param    someObjects    The list of objects to be used.
     * @return   The old selection of objects.
     */
	public KnowledgeObject[] setKnowledgeObjects(List<? extends KnowledgeObject> someObjects) {
        KnowledgeObject[] theObjects = null;

        if (someObjects != null) {
            int theSize = someObjects.size();

            theObjects = new KnowledgeObject[theSize];

            if (theSize > 0) {
				theObjects = someObjects.toArray(theObjects);
            }
        }

        return (this.setKnowledgeObjects(theObjects));
    }

    /**
     * Set the array of knowledge objects to be used within the search.
     * 
     * If the given array is <code>null</code> or an empty array, the
     * search engine has to search in all knowledge objects.
     * 
     * @param    someObjects    The array of objects to be used.
     * @return   The old selection of objects.
     */
    public KnowledgeObject[] setKnowledgeObjects(KnowledgeObject[] someObjects) {
        KnowledgeObject[] theOld = this.objects;

        this.objects = someObjects;

        return (theOld);
    }
    
    /**
     * Set the name of the knowledge base to be used for resolving hits to 
     * knowledge objects.
     * 
     * @param   aKBName     The name of the knowledge base
     */
    public void setKnowledgeBaseName(String aKBName) {
        this.kbName = aKBName;
    }
    
    /**
     * Get the name of the knowledge base that is used for resolving hits
     * to knowledge objects.
     * 
     * @return the name of the knowledge base
     */
    public String getKnowledgeBaseName() {
        return this.kbName;
    }
}
