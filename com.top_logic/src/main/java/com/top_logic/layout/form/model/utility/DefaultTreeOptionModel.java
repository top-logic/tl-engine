/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.Iterator;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterIterator;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.tree.model.TLTreeModel;
/**
 * This class is a container for select field options, stored as tree.
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class DefaultTreeOptionModel<N> extends AbstractOptionModel<N> implements TreeOptionModel<N> {

	private TLTreeModel<N> _tree;

	private Filter<? super N> _selectableFilter;

	/** @see #showRootNode() */
	private boolean _showRoot = true;
	
	/**
	 * Creates a {@link DefaultTreeOptionModel}.
	 * 
	 * @param tree
	 *        The tree model of options.
	 */
	public DefaultTreeOptionModel(TLTreeModel<N> tree) {
		this(tree, FilterFactory.trueFilter());
	}

	/**
	 * Creates a {@link DefaultTreeOptionModel}.
	 * 
	 * @param tree
	 *        The tree model of options.
	 * @param selectableFilter
	 *        See {@link #getSelectableOptionsFilter()}
	 */
	public DefaultTreeOptionModel(TLTreeModel<N> tree, Filter<? super N> selectableFilter) {
		this(tree, selectableFilter, selectableFilter.accept(tree.getRoot()));
	}
	
	/**
	 * Creates a {@link DefaultTreeOptionModel}.
	 * 
	 * @param tree
	 *        The tree model of options.
	 * @param selectableFilter
	 *        See {@link #getSelectableOptionsFilter()}
	 * @param showRoot
	 *        See {@link #showRootNode()}
	 */
	public DefaultTreeOptionModel(TLTreeModel<N> tree, Filter<? super N> selectableFilter, boolean showRoot) {
		assert (tree != null) : "Tree must not be null";
		_tree = tree;
		_showRoot = showRoot;
		_selectableFilter = selectableFilter;
	}

	@Override
	public TLTreeModel<N> getBaseModel() {
		return _tree;
	}
	
	@Override
	public int getOptionCount() {
		if (!getBaseModel().isFinite()) {
			return INFINITE;
		}
		return CollectionUtil.size(iterator());
	}

	@Override
	public void setSelectableOptionsFilter(Filter<? super N> selectableOptionsFilter) {
		this._selectableFilter = selectableOptionsFilter;
	}

	@Override
	public Filter<? super N> getSelectableOptionsFilter() {
		return _selectableFilter;
	}

	@Override
	public void setShowRoot(boolean showRoot) {
		_showRoot = showRoot;
	}

	@Override
	public boolean showRootNode() {
		return _showRoot;
	}

	/**
	 * Iterator over all options of an option tree.
	 * 
	 * <p>
	 * Thereby the iterator retrieves the elements in depth-first order.
	 * </p>
	 * 
	 * @return an iterator over all options
	 */
	@Override
	public Iterator<N> iterator() {
		return new FilterIterator<>(new DescendantDFSIterator<>(_tree, _tree.getRoot(), _showRoot), _selectableFilter);
	}

}
