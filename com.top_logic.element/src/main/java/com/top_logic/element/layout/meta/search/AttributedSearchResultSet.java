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
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * Search result set created by an attributed search.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AttributedSearchResultSet implements SearchResultSet {

    private final List<AttributedSearchResult> result;

    private final List messages;

    private final List<String> columns;

	private final Set<? extends TLClass> _types;

	/**
	 * Creates a {@link AttributedSearchResultSet} for a single type.
	 * 
	 * @see #AttributedSearchResultSet(Collection, Set, List, List)
	 */
	public AttributedSearchResultSet(Collection<? extends TLObject> aResult, TLClass type,
			List<String> someColumns, List someMessages) {
		this(aResult, Collections.singleton(type), someColumns, someMessages);
	}

    /**
	 * Creates a {@link AttributedSearchResultSet}.
	 * 
	 * @param types
	 *        The meta element being base of this search result, must not be <code>null</code>.
	 */
	public AttributedSearchResultSet(Collection<? extends TLObject> aResult, Set<? extends TLClass> types,
			List<String> someColumns, List someMessages) {
        this.result      = new ArrayList<>(aResult.size());
		_types = types;
        this.messages    = someMessages == null ? Collections.emptyList()         : someMessages;
        this.columns     = someColumns == null  ? Collections.<String>emptyList() : someColumns;

		for (TLObject theAttributed : aResult) {
			if (theAttributed == null) {
				continue;
			}
            this.result.add(new AttributedSearchResult(theAttributed));
        }
    }

    /**
     * @see com.top_logic.knowledge.searching.SearchResultSet#getSearchMessages()
     */
    @Override
	public List getSearchMessages() {
        return this.messages;
    }

    @Override
	public List<? extends AttributedSearchResult> getSearchResults() {
        return this.result;
    }
    
    @Override
	public List<? extends TLObject> getResultObjects() {
    	List<? extends AttributedSearchResult> theResult = this.getSearchResults();
		List<TLObject> theRes = new ArrayList<>(theResult.size());
    	for (AttributedSearchResult asr : theResult) {
			theRes.add(asr.getResult());
    	}
    	return theRes;
    }
    
    /**
     * @see com.top_logic.knowledge.searching.SearchResultSet#isClosed()
     */
    @Override
	public boolean isClosed() {
        return true;
    }
    
    /**
     * @see com.top_logic.knowledge.searching.SearchResultSet#waitForClosed(long)
     */
    @Override
	public boolean waitForClosed(long aMillis) {
        return true;
    }
    
    /**
     * @see com.top_logic.knowledge.searching.SearchResultSet#close()
     */
    @Override
	public void close() {
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " ["
                    + "#results: " + this.result.size()
                    + ", #messages: " + this.messages.size()
			+ ", metaElement: " + _types
                    + ']';
    }

    /** 
     * Return the columns to be displayed in search result.
     * 
     * @return    The columns to be displayed, my be <code>null</code>.
     */
    public List<String> getResultColumns() {
        return this.columns;
    }

    /**
	 * @deprecated Use {@link #getTypes()}.
	 */
	@Deprecated
    public TLClass getMetaElement() {
		return CollectionUtil.getSingleValueFromCollection(_types);
    }

	/**
	 * The types this search was about.
	 */
	public Set<? extends TLClass> getTypes() {
		return _types;
	}
}
