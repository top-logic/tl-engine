/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;

/**
 * {@link FilterTreeModel} is an adapter for some {@link TLTreeModel} where the access to the
 * children is filtered by some filter.
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FilterTreeModel<N> extends TLTreeModelAdapter<TLTreeModel<N>, N> {

	private final Filter<? super N> filter;

	public FilterTreeModel(TLTreeModel<N> aModel, Filter<? super N> aFilter) {
    	super(aModel);
        this.filter = aFilter;
    }

    @Override
	public List<? extends N> getChildren(N aParent) {
        return FilterUtil.filterList(this.filter, super.getChildren(aParent));
    }

    @Override
	public boolean hasChild(N parent, Object node) {
		return super.hasChild(parent, node) && this.filter.accept((N) node);
    }
    
    @Override
	public boolean hasChildren(N node) {
    	boolean hasChildren = super.hasChildren(node);
    	if (!hasChildren) 
    		return false;
		return FilterUtil.hasMatch(getChildrenUnfiltered(node), filter);
    }
    
    @Override
	public Iterator<? extends N> getChildIterator(N node) {
    	return FilterUtil.filterIterator(filter, getChildrenUnfiltered(node));
    }

	/**
	 * Returns an iterator containing all children of the given node, not just
	 * the nodes which matches the given filter.
	 */
	private Iterator<? extends N> getChildrenUnfiltered(N node) {
		return super.getChildIterator(node);
	}
}
