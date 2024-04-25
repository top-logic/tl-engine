/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.model.TLObject;

/**
 * Elements in the list have order values that are not necessarily consecutive.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LiveOrderedAssociationsList<T extends TLObject> extends LiveAssociationsList<T> {

	/**
	 * Creates a new {@link LiveOrderedAssociationsList}.
	 */
	public LiveOrderedAssociationsList(OrderedLinkCache<T> associationCache, long minValidity, long maxValidity,
			List<T> items, boolean localCache) {
		super(associationCache, minValidity, maxValidity, items, localCache);
	}

	@Override
	protected ListView<T> createLiveView(OrderedLinkCache<T> associationCache, String orderAttribute) {
		return new ListViewExt<>(associationCache, orderAttribute);
	}

	private static class ListViewExt<T extends TLObject> extends ListView<T> {

		private static final int MAX_ORDER = OrderedLinkUtil.MAX_ORDER;

		private static final int APPEND_INC = OrderedLinkUtil.APPEND_INC;

		private static final int INSERT_INC = OrderedLinkUtil.INSERT_INC;

		public ListViewExt(OrderedLinkCache<T> associationCache, String orderAttribute) {
			super(associationCache, orderAttribute);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void sort(Comparator<? super T> c) {
			if (size() < 2) {
				return;
			}
			List<TLObject> currentOrder = new ArrayList<>(lookup());
			TLObject[] a = currentOrder.toArray(TLObject[]::new);
			Arrays.sort(a, (Comparator) c);

			int insertStart = -1;
			int destPos = 0;
			for (int i = 0; i < a.length; i++) {
				TLObject tmp = a[i];
				TLObject dest = currentOrder.get(destPos);
				if (tmp == dest) {
					if (insertStart != -1) {
						// Insert elements from "insertStart" to "i-1" before "destPos"
						int numberElements = i - insertStart;
						int indexBefore;
						if (destPos == 0) {
							indexBefore = 0;
						} else {
							indexBefore = order(currentOrder.get(destPos - 1));
						}
						int indexAfter = order(dest);

						int indexInc = Math.min(INSERT_INC, (indexAfter - indexBefore) / (numberElements + 1));
						if (indexInc > 0) {
							int newIndex = indexBefore;
							for (int j = insertStart; j <i;j++) {
								newIndex += indexInc;
								setOrder(a[j], newIndex);
								// Do not handle moved element twice
								currentOrder.remove(a[j]);
							}
						} else {
							// No incremental insert possible
							OrderedLinkUtil.updateIndices(Arrays.asList(a), _orderAttribute);
							return;
						}
					}
					insertStart = -1;
					destPos++;
					continue;
				} else {
					if (insertStart == -1) {
						insertStart = i;
					}
				}
			}
			assert insertStart == -1;
		}

		@Override
		protected void updateOrderOnAppend(List<T> cache, int size, T element) {
			int lastOrder;
			int orderInc;
			if (size > 0) {
				T last = cache.get(size - 1);
				lastOrder = order(last);
				int maxOrder = MAX_ORDER;
				orderInc = Math.min(APPEND_INC, (maxOrder - lastOrder) / 2);
			} else {
				lastOrder = 0;
				orderInc = APPEND_INC;
			}

			if (orderInc > 0) {
				int newIndex = lastOrder + orderInc;
				setOrder(element, newIndex);
			} else {
				List<TLObject> expected = new ArrayList<>(cache.size() + 1);
				expected.addAll(cache);
				expected.add(element);
				OrderedLinkUtil.updateIndices(expected, _orderAttribute);
			}
		}
		@Override
		protected void updateOrderOnAppend(List<T> cache, int size, Collection<? extends T> elements) {
			int lastOrder;
			int orderInc;
			if (size > 0) {
				T last = cache.get(size - 1);
				lastOrder = order(last);
				int maxOrder = MAX_ORDER;
				orderInc = Math.min(APPEND_INC, (maxOrder - lastOrder) / (elements.size() + 1));
			} else {
				lastOrder = 0;
				orderInc = APPEND_INC;
			}

			if (orderInc > 0) {
				int newIndex = lastOrder;
				for (T element : elements) {
					newIndex += orderInc;
					setOrder(element, newIndex);
				}
			} else {
				List<TLObject> expected = new ArrayList<>(cache.size() + 1);
				expected.addAll(cache);
				expected.addAll(elements);
				OrderedLinkUtil.updateIndices(expected, _orderAttribute);
			}
		}

		@Override
		protected void updateIndexOnInsert(List<T> cache, int size, int index, T element) {
			int indexBefore;
			if (index == 0) {
				indexBefore = 0;
			} else {
				indexBefore = order(cache.get(index - 1));
			}
			int indexAfter = order(cache.get(index));

			int indexInc = Math.min(INSERT_INC, (indexAfter - indexBefore) / 2);
			if (indexInc > 0) {
				int newIndex = indexBefore + indexInc;
				setOrder(element, newIndex);
			} else {
				List<TLObject> expected = new ArrayList<>(size + 1);
				expected.addAll(cache.subList(0, index));
				expected.add(element);
				expected.addAll(cache.subList(index, size));
				OrderedLinkUtil.updateIndices(expected, _orderAttribute);
			}
		}

		@Override
		protected void updateIndexOnInsert(List<T> cache, int size, int index, Collection<? extends T> elements) {
			int indexBefore;
			if (index == 0) {
				indexBefore = 0;
			} else {
				indexBefore = order(cache.get(index - 1));
			}
			int indexAfter = order(cache.get(index));

			int indexInc = Math.min(INSERT_INC, (indexAfter - indexBefore) / (elements.size() + 1));
			if (indexInc > 0) {
				int newIndex = indexBefore;
				for (T element : elements) {
					newIndex += indexInc;
					setOrder(element, newIndex);
				}
			} else {
				List<TLObject> expected = new ArrayList<>(size + 1);
				expected.addAll(cache.subList(0, index));
				expected.addAll(elements);
				expected.addAll(cache.subList(index, size));
				OrderedLinkUtil.updateIndices(expected, _orderAttribute);
			}
		}

		@Override
		protected void updateIndexOnRemove(List<T> cache, int index) {
			// Indexes may not be updated, because they already contains gaps.
		}

	}

}

