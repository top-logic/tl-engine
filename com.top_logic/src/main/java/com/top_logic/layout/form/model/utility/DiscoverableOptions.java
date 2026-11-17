/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.tree.model.FilterTreeModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.model.security.DiscoverRight;

/**
 * Restriction of an {@link OptionModel} to the options the current user may discover.
 *
 * <p>
 * An option list is an object source: it offers only objects the user holds the
 * {@link com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup#DISCOVER Discover} right on, see
 * {@link DiscoverRight}. Options that are no business objects (strings, model elements) are kept.
 * </p>
 *
 * <p>
 * The restricted model delegates to the given model and keeps its behavior: listeners, resetting
 * and the selectable options of a tree. The options are filtered when they are read, for the user
 * of the current interaction.
 * </p>
 */
public final class DiscoverableOptions {

	private DiscoverableOptions() {
		// Static utility.
	}

	/**
	 * The given options restricted to the objects the current user may discover.
	 *
	 * <p>
	 * A {@link ListOptionModel} offers the discoverable elements of its list. A
	 * {@link TreeOptionModel} hides every undiscoverable node together with its subtree, like a
	 * navigation tree; an undiscoverable root is not shown. Any other option model is returned
	 * unchanged.
	 * </p>
	 *
	 * @param options
	 *        The options to restrict. May be <code>null</code>.
	 * @return The restricted options, <code>null</code> if no options are given.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> OptionModel<T> discoverable(OptionModel<T> options) {
		if (options instanceof DiscoverableListOptions || options instanceof DiscoverableTreeOptions) {
			return options;
		}
		if (options instanceof ListOptionModel list) {
			return new DiscoverableListOptions<>(list);
		}
		if (options instanceof TreeOptionModel tree) {
			return new DiscoverableTreeOptions<>(tree);
		}
		return options;
	}

	/**
	 * The given list restricted to the objects the current user may discover.
	 *
	 * @param options
	 *        The options to restrict.
	 * @return The discoverable options, the given list itself if all options are discoverable.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> discoverable(List<T> options) {
		return (List<T>) DiscoverRight.filter(options);
	}

	/**
	 * {@link ListOptionModel} offering the discoverable elements of another {@link ListOptionModel}.
	 */
	private static final class DiscoverableListOptions<T> extends OptionModelProxy<T> implements ListOptionModel<T> {

		private final ListOptionModel<T> _delegate;

		DiscoverableListOptions(ListOptionModel<T> delegate) {
			_delegate = delegate;
		}

		@Override
		public ListOptionModel<T> delegate() {
			return _delegate;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<? extends T> getBaseModel() {
			return discoverable((List<T>) _delegate.getBaseModel());
		}

		@Override
		public int getOptionCount() {
			return getBaseModel().size();
		}

		@Override
		public Iterator<T> iterator() {
			return ListOptionModel.super.iterator();
		}

		@Override
		public T getDefaultValue() {
			T defaultValue = _delegate.getDefaultValue();
			return DiscoverRight.isDiscoverable(defaultValue) ? defaultValue : null;
		}

	}

	/**
	 * {@link TreeOptionModel} hiding the undiscoverable nodes of another {@link TreeOptionModel}.
	 */
	private static final class DiscoverableTreeOptions<N> extends OptionModelProxy<N> implements TreeOptionModel<N> {

		private static final Filter<Object> DISCOVERABLE = DiscoverRight::isDiscoverable;

		private final TreeOptionModel<N> _delegate;

		private TLTreeModel<N> _baseTree;

		private TLTreeModel<N> _filteredTree;

		DiscoverableTreeOptions(TreeOptionModel<N> delegate) {
			_delegate = delegate;
		}

		@Override
		public TreeOptionModel<N> delegate() {
			return _delegate;
		}

		@Override
		public TLTreeModel<N> getBaseModel() {
			TLTreeModel<N> baseTree = _delegate.getBaseModel();
			if (baseTree != _baseTree) {
				_baseTree = baseTree;
				_filteredTree = new FilterTreeModel<>(baseTree, DISCOVERABLE);
			}
			return _filteredTree;
		}

		@Override
		public int getOptionCount() {
			return TreeOptionModel.super.getOptionCount();
		}

		@Override
		public Iterator<N> iterator() {
			return TreeOptionModel.super.iterator();
		}

		@Override
		public Filter<? super N> getSelectableOptionsFilter() {
			return _delegate.getSelectableOptionsFilter();
		}

		@Override
		public void setSelectableOptionsFilter(Filter<? super N> selectableOptionsFilter) {
			_delegate.setSelectableOptionsFilter(selectableOptionsFilter);
		}

		@Override
		public boolean showRootNode() {
			return _delegate.showRootNode() && DiscoverRight.isDiscoverable(getBaseModel().getRoot());
		}

		@Override
		public void setShowRoot(boolean showRoot) {
			_delegate.setShowRoot(showRoot);
		}

		@Override
		public N getDefaultValue() {
			N defaultValue = _delegate.getDefaultValue();
			return DiscoverRight.isDiscoverable(defaultValue) ? defaultValue : null;
		}

	}

}
