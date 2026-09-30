/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.Iterator;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterIterator;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * {@link OptionModel} wrapping a {@link TLTreeModel} of structured options of a
 * {@link SelectField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeOptionModel<N> extends OptionModel<N> {

	@Override
	public TLTreeModel<N> getBaseModel();

	/**
	 * {@link Filter}, which determines, which nodes are selectable.
	 */
	Filter<? super N> getSelectableOptionsFilter();

	/**
	 * @see #getSelectableOptionsFilter()
	 */
	void setSelectableOptionsFilter(Filter<? super N> selectableOptionsFilter);

	/**
	 * Whether the root node of the tree should be visible during selection.
	 */
	boolean showRootNode();

	/**
	 * @see #showRootNode()
	 */
	void setShowRoot(boolean showRoot);

	@Override
	default int getOptionCount() {
		if (!getBaseModel().isFinite()) {
			return INFINITE;
		}
		return CollectionUtil.size(iterator());
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
	default Iterator<N> iterator() {
		TLTreeModel<N> tree = getBaseModel();
		return new FilterIterator<>(new DescendantDFSIterator<>(tree, tree.getRoot(), showRootNode()),
			getSelectableOptionsFilter());
	}

}
