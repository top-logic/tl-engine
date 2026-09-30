/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.model.TLObject;

/**
 * {@link LiveAssociationsList} for order attributes that are actually index attributes, i.e. the
 * order of an element in the list is equal to its index in the list.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LiveIndexedAssociationsList<T extends TLObject> extends LiveAssociationsList<T> {

	/**
	 * Creates a new {@link LiveIndexedAssociationsList}.
	 */
	public LiveIndexedAssociationsList(OrderedLinkCache<T> associationCache, long minValidity, long maxValidity,
			List<T> items, boolean localCache) {
		super(associationCache, minValidity, maxValidity, items, localCache);
	}

	@Override
	protected ListView<T> createLiveView(OrderedLinkCache<T> associationCache, String orderAttribute) {
		return new ListViewExt<>(associationCache, orderAttribute);
	}

	@Override
	protected boolean internalRemove(T link) {
		List<T> items = directItems();
		int position = getIndex(link);
		if (position >= 0 && position < items.size()) {
			T orig = items.get(position);
			if (orig == link) {
				items.remove(position);
				return true;
			}
		}
		return super.internalRemove(link);
	}

	private int getIndex(T link) {
		return OrderedLinkUtil.getOrder(link, _orderAttribute).intValue();
	}

	private static class ListViewExt<T extends TLObject> extends ListView<T> {

		public ListViewExt(OrderedLinkCache<T> associationCache, String orderAttribute) {
			super(associationCache, orderAttribute);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void sort(Comparator<? super T> c) {
			TLObject[] a = this.toArray(TLObject[]::new);
			Arrays.sort(a, (Comparator) c);
			for (int i = 0; i < a.length; i++) {
				setOrder(a[i], i);
			}

		}

		@Override
		protected void updateOrderOnAppend(List<T> cache, int size, T element) {
			setOrder(element, size);
		}

		@Override
		protected void updateOrderOnAppend(List<T> cache, int size, Collection<? extends T> elements) {
			for (T element : elements) {
				setOrder(element, size++);
			}
		}

		@Override
		protected void updateIndexOnInsert(List<T> cache, int size, int index, T element) {
			for (int i = size - 1; i >= index; i--) {
				setOrder(cache.get(i), i + 1);
			}
			setOrder(element, index);
		}

		@Override
		protected void updateIndexOnInsert(List<T> cache, int size, int index, Collection<? extends T> elements) {
			int insertSize = elements.size();
			for (int i = size - 1; i >= index; i--) {
				setOrder(cache.get(i), i + insertSize);
			}
			for (T element : elements) {
				setOrder(element, index++);
			}
		}

		@Override
		protected void updateIndexOnRemove(List<T> cache, int index) {
			for (int i = cache.size() - 1; i > index; i--) {
				setOrder(cache.get(i), i - 1);
			}
		}

	}

}
