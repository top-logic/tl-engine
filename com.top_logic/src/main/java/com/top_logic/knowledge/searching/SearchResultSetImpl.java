/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.top_logic.basic.TLID;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class SearchResultSetImpl implements SearchResultSetSPI {

    /** The list of messages occured. */
    private List messages;

    /** Flag, if this result set has been closed. */
    private boolean closed;

    /** Flag set, when this instance is closing. */
	private Boolean inClosing = Boolean.FALSE;

    /** The engies filling this result set. */
    private List engines;

	/** The map of found hits. */
	private Map<TLID, SearchResult> results = new HashMap<>();

    /**
     * Constructor for SearchResultSetImpl.
     * 
     * The given array must not be <code>null</code> but can be empty (which
     * is not so useful for a search result).
     * 
     * @param    someEngines    The array of all engines to be searched.
     */
    public SearchResultSetImpl(SearchEngine[] someEngines) {
        this(Arrays.asList(someEngines));
    }

    /**
     * Constructor for SearchResultSetImpl.
     * 
     * The given list must not be <code>null</code> but can be empty (which
     * is not so useful for a search result). If there are any elements within
     * the list they have to be of class 
     * {@link com.top_logic.knowledge.searching.SearchEngine}.
     * 
     * @param    someEngines    The list of all engines to be searched.
     * @see      com.top_logic.knowledge.searching.SearchEngine
     */
    public SearchResultSetImpl(List someEngines) {
        super();

        this.engines = new ArrayList(someEngines);
    }

    /**
     * @see com.top_logic.knowledge.searching.SearchResultSet#getSearchResults()
     */
    @Override
	public List getSearchResults() {
        return new ArrayList(this.results.values());
    }

    /**
     * @see com.top_logic.knowledge.searching.SearchResultSet#getSearchMessages()
     */
    @Override
	public List getSearchMessages() {
        if (this.messages == null) {
            this.messages = new Vector();
        }

        return (this.messages);
    }

    @Override
	public boolean addSearchResult(SearchResult aResult) {
        
		SearchResult thisResult = this.findResult(this.results, aResult);

		if (thisResult != null) {
			Iterator theIt = aResult.getEngines().iterator();

			while (theIt.hasNext()) {
				thisResult.addSearchEngine((SearchEngine) theIt.next());
			}

			theIt = aResult.getAttributes().iterator();

			while (theIt.hasNext()) {
				thisResult.addSearchResultAttribute((SearchResultAttribute) theIt.next());
			}

			return (true);
		} else {
			return this.putResult(this.results, aResult);
		}
    }

    @Override
	public boolean addSearchMessage(SearchMessage aMessage) {
        return (this.getSearchMessages().add(aMessage));
    }

    @Override
	public void finished(SearchEngine anEngine) {
        if (this.engines.remove(anEngine)) {
            if (this.engines.size() == 0) {
                this.close();
            }
        }
    }

    @Override
	public boolean isClosed() {
        return (this.closed);
    }

    @Override
	public void close() {
        if (!this.inClosing.booleanValue()) {
			this.inClosing = Boolean.TRUE;

            List theEngines = new ArrayList(this.engines);

            for (Iterator theIt = theEngines.iterator(); theIt.hasNext(); ) {
                ((SearchEngine) theIt.next()).cancel(this);
            }

            this.closed    = true;

			this.inClosing = Boolean.FALSE;
        }
    }

    @Override
	public String toString() {
        return this.getClass().getName() + " [" + this.toStringValues() + ']';
    }

	/**
	 * Check if the given result is already contained in this result set.
	 */
	protected SearchResult findResult(Map<TLID, SearchResult> map, SearchResult result) {
		return map.get(result.getObject().getObjectName());
    }
    
	/**
	 * Add a given result to this result set
	 */
	protected boolean putResult(Map<TLID, SearchResult> map, SearchResult result) {
		return map.put(result.getObject().getObjectName(), result) != null;
    }
    

     @Override
	public boolean waitForClosed(long millis) {
        
        boolean closed   = isClosed();
        long    sleeping = 100;
        while (!closed && millis > 0) {
            try {
                if (sleeping < millis) {
                    Thread.sleep(sleeping);
                    sleeping <<= 1; // *2 -> 100, 200, 400, 800, 1600
                }
                else 
                    Thread.sleep(millis);
                millis -= sleeping;
            }
            catch (InterruptedException e) { // ???
                break;
            }
            closed = isClosed();
        }
        return closed;
    }

     protected String toStringValues() {
		return "closed: " + this.closed +
             ", #engines: " + ((this.engines == null) ? 0 : this.engines.size()) +
             ", #results: " + ((this.results == null) ? 0 : this.results.size()) +
             ", #messages: " + ((this.messages == null) ? 0 : this.messages.size());
     }

	@Override
	public List getResultObjects() {
		List theObjects = new ArrayList();
		for (Iterator theIt=this.getSearchResults().iterator(); theIt.hasNext();) {
			theObjects.add(((SearchResult) theIt.next()).getObject());
		}
		
		return theObjects;
	}
}
