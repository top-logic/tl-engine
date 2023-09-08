/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.function.Supplier;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * {@link TreeOptionModel} whose actual implementation is created lazy.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LazyTreeOptionModel<N> extends OptionModelProxy<N> implements TreeOptionModel<N> {

	private final Supplier<? extends TreeOptionModel<N>> _supplier;

	private TreeOptionModel<N> _delegate;

	private Filter<? super N> _selectableFilterOptions = null;

	private boolean _filterSet = false;

	private Boolean _showRoot = null;

	/**
	 * Creates a new {@link LazyTreeOptionModel}.
	 */
	public LazyTreeOptionModel(Supplier<? extends TreeOptionModel<N>> impl) {
		_supplier = impl;
	}

	/**
	 * The actual {@link TreeOptionModel} that is created lazy.
	 * 
	 * <p>
	 * Calling this method initialises the {@link TreeOptionModel} to delegate all methods to.
	 * </p>
	 */
	@Override
	public TreeOptionModel<N> delegate() {
		if (_delegate == null) {
			_delegate = createDelegate();
		}
		return _delegate;
	}

	/**
	 * Creates the actual {@link TreeOptionModel} to delegate methods to.
	 */
	protected TreeOptionModel<N> createDelegate() {
		TreeOptionModel<N> delegate = _supplier.get();
		if (_filterSet) {
			delegate.setSelectableOptionsFilter(_selectableFilterOptions);
		}
		if (_showRoot != null) {
			delegate.setShowRoot(_showRoot.booleanValue());
		}
		return delegate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TLTreeModel<N> getBaseModel() {
		return (TLTreeModel<N>) super.getBaseModel();
	}

	@Override
	public Filter<? super N> getSelectableOptionsFilter() {
		if (_delegate != null) {
			return _delegate.getSelectableOptionsFilter();
		}
		if (_filterSet) {
			return _selectableFilterOptions;
		}
		return delegate().getSelectableOptionsFilter();
	}

	@Override
	public void setSelectableOptionsFilter(Filter<? super N> selectableOptionsFilter) {
		if (_delegate != null) {
			_delegate.setSelectableOptionsFilter(selectableOptionsFilter);
			return;
		}
		_selectableFilterOptions = selectableOptionsFilter;
		_filterSet = true;
	}

	@Override
	public boolean showRootNode() {
		if (_delegate != null) {
			return _delegate.showRootNode();
		}
		if (_showRoot != null) {
			return _showRoot.booleanValue();
		}
		return delegate().showRootNode();
	}

	@Override
	public void setShowRoot(boolean showRoot) {
		if (_delegate != null) {
			_delegate.setShowRoot(showRoot);
			return;
		}
		_showRoot = Boolean.valueOf(showRoot);
	}

}

