/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.top_logic.basic.util.Computation;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.model.TLObject;

/**
 * {@link AssociationsList} that manages a mutable view of the cached link items.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LiveAssociationsList<T extends TLObject> extends AssociationsList<T> {

	private final OrderedLinkCache<T> _cache;

	private final List<T> _view;

	private final ModCountList<T> _items;

	/**
	 * Creates a {@link AssociationsList}.
	 * 
	 * @param associationCache
	 *        The owning cache.
	 * @param items
	 *        The initial link items to put into the buffer.
	 * @param localCache
	 *        Whether this {@link AssociationsList} is for global or local (with modification)
	 *        access.
	 */
	public LiveAssociationsList(OrderedLinkCache<T> associationCache, long minValidity, long maxValidity,
			List<T> items, boolean localCache) {
		super(associationCache, minValidity, maxValidity, items, localCache);
		_cache = associationCache;

		_view = createLiveView(associationCache, _orderAttribute);
		_items = sortLinks(items);
	}

	protected abstract ListView<T> createLiveView(OrderedLinkCache<T> associationCache, String orderAttribute);

	private ModCountList<T> sortLinks(final Collection<T> links) {
		switch (links.size()) {
			case 0:
			case 1: {
				return new ModCountList<>(links);
			}
			default: {
				if (isLocalCache()) {
					return sortDirect(links);
				} else {
					/* For a global cache the local modification must not be considered, because
					 * e.g. the order attribute may be changed or an object may be deleted. */
					KnowledgeBase kb = baseItem().getKnowledgeBase();
					return kb.inRevision(minValidity(), new Computation<ModCountList<T>>() {
						@Override
						public ModCountList<T> run() {
							return sortDirect(links);
						}
					});
				}
			}
		}
	}

	final ModCountList<T> sortDirect(final Collection<T> links) {
		final ModCountList<T> result = new ModCountList<>(links);
		Collections.sort(result, _linkOrder);
		return result;
	}

	@Override
	protected Iterable<T> getAssociationItems() {
		return _items;
	}

	@Override
	public List<T> getAssociations() {
		return _view;
	}

	final ModCountList<T> directItems() {
		return _items;
	}

	@Override
	protected boolean addLinkResolved(T link) {
		return internalAdd(link);
	}

	/**
	 * Handles adding of an object matching the association query.
	 * 
	 * @param link
	 *        the link object.
	 * @return Whether the links was newly added, <code>false</code>, if the link was already
	 *         contained in this buffer.
	 */
	protected boolean internalAdd(T link) {
		List<T> items = directItems();
		int position = getPosition(link, items);
		if (position >= 0) {
			// Already in the list.
			return false;
		} else {
			int index = -position - 1;
			items.add(index, link);
			return true;
		}
	}

	private int getPosition(T link, List<T> items) {
		try {
			if (link.tHandle().getAttributeValue(_orderAttribute) == null) {
				return -1;
			}
		} catch (NoSuchAttributeException ex) {
			return -1;
		}

		return getIdentityMatchPosition(items, link, Collections.binarySearch(items, link, _linkOrder));
	}

	private int getIdentityMatchPosition(List<T> items, T link, int position) {
		if (position < 0) {
			return position;
		}

		T current = items.get(position);
		if (current == link) {
			return position;
		}

		// Search among items with the same sort order to the left.
		int beforeIndex = position;
		while (beforeIndex > 0) {
			beforeIndex--;

			current = items.get(beforeIndex);
			if (current == link) {
				return beforeIndex;
			}
			if (_linkOrder.compare(link, current) != 0) {
				break;
			}
		}

		// Search among items with the same sort order to the right.
		int afterIndex = position;
		int size = items.size();
		while (true) {
			afterIndex++;
			if (afterIndex == size) {
				break;
			}
			current = items.get(afterIndex);
			if (current == link) {
				return afterIndex;
			}
			if (_linkOrder.compare(link, current) != 0) {
				break;
			}
		}

		return -afterIndex - 1;
	}

	@Override
	protected void modifyLinkResolved(T link) {
		internalRemove(link);
		internalAdd(link);
	}

	@Override
	protected boolean removeLinkResolved(TLObject item) {
		if (!expectedType().isInstance(item)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		T link = (T) item;

		return internalRemove(link);
	}

	/**
	 * Removes a link from this buffer.
	 * 
	 * @param link
	 *        The link to remove.
	 * @return Whether the link was member of this buffer before this call.
	 */
	protected boolean internalRemove(T link) {
		List<T> items = directItems();
		int position = getPosition(link, items);
		if (position >= 0) {
			items.remove(position);
			return true;
		} else {
			// Not found in the list, for safety scan the whole list, maybe, the index property has
			// changed.
			return items.remove(link);
		}
	}

	final KnowledgeItem baseItem() {
		return _cache.getBaseItem();
	}

	/**
	 * {@link List} that is is a view for the actual cache value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected abstract static class ListView<T extends TLObject> implements List<T> {

		/** The {@link OrderedLinkCache} this is a view for */
		protected final OrderedLinkCache<T> _cache;

		/** Name of the order attribute */
		protected final String _orderAttribute;

		/**
		 * Constructor creates a new {@link LiveAssociationsList.ListView}.
		 */
		public ListView(OrderedLinkCache<T> associationCache, String orderAttribute) {
			_cache = associationCache;
			_orderAttribute = orderAttribute;
		}

		// Atomic read API

		@Override
		public boolean isEmpty() {
			return lookup().isEmpty();
		}

		@Override
		public int size() {
			return lookup().size();
		}

		@Override
		public T get(int index) {
			return lookup().get(index);
		}

		@Override
		public boolean contains(Object o) {
			return lookup().contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return lookup().containsAll(c);
		}

		@Override
		public int indexOf(Object o) {
			return lookup().indexOf(o);
		}

		@Override
		public int lastIndexOf(Object o) {
			return lookup().lastIndexOf(o);
		}

		@Override
		public Object[] toArray() {
			return lookup().toArray();
		}
	
		@Override
		public <E> E[] toArray(E[] a) {
			return lookup().toArray(a);
		}
	
		// General read API

		@Override
		public Iterator<T> iterator() {
			return listIterator();
		}

		@Override
		public ListIterator<T> listIterator() {
			return listIterator(0);
		}

		final int modCount() {
			return lookup().modCount();
		}

		@Override
		public ListIterator<T> listIterator(int index) {
			return new ListIterator<>() {
				private int _index = 0;

				private ModCountList<T> _current = lookup();

				private int _modCount = ListView.this.modCount();

				@Override
				public boolean hasNext() {
					return _index < size();
				}

				@Override
				public T next() {
					checkForComodification();
					return get(_index++);
				}

				@Override
				public void remove() {
					checkForComodification();
					ListView.this.remove(--_index);
					updateModCount();
				}

				@Override
				public boolean hasPrevious() {
					return _index > 0;
				}

				@Override
				public T previous() {
					checkForComodification();
					return get(--_index);
				}

				@Override
				public int nextIndex() {
					return _index;
				}

				@Override
				public int previousIndex() {
					return _index - 1;
				}

				@Override
				public void set(T e) {
					checkForComodification();
					ListView.this.set(_index, e);
					updateModCount();
				}

				@Override
				public void add(T e) {
					checkForComodification();
					ListView.this.add(_index++, e);
					updateModCount();
				}

				private void updateModCount() {
					_current = lookup();
					_modCount = _current.modCount();
				}

				private void checkForComodification() {
					ModCountList<T> current = lookup();
					if (current != _current) {
						throw new ConcurrentModificationException();
					}
					if (_modCount != current.modCount()) {
						throw new ConcurrentModificationException();
					}
				}

			};
		}

		@Override
		public List<T> subList(final int start, final int stop) {
			return new SubList(start, stop);
		}

		// Write API

		@Override
		public T set(int index, T element) {
			T before = get(index);
			if (element == before) {
				return before;
			}

			checkAdd(element);

			Object orderBefore = orderValue(before);
			clearParent(before);

			setOrder(element, orderBefore);
			setParent(element);

			return before;
		}

		private void checkAdd(Collection<? extends T> c) {
			for (T element : c) {
				checkAdd(element);
			}
		}

		private void checkAdd(T element) {
			Object oldContainer = container(element);
			if (oldContainer == baseItem()) {
				throw new IllegalArgumentException("Element must not be added twice to a sorted set.");
			}
			if (oldContainer != null) {
				throw new IllegalArgumentException("Element already contained in another set.");
			}
		}

		@Override
		public boolean add(T element) {
			checkAdd(element);
			
			List<T> cache = lookup();
			int size = cache.size();
			return append(cache, size, element);
		}

		@Override
		public void add(int index, T element) {
			checkAdd(element);

			List<T> cache = lookup();
			int size = cache.size();
			if (index < 0 || index > size) {
				throw new IndexOutOfBoundsException("Index '" + index + "' of range [0, " + size + "].");
			}

			if (index == size) {
				append(cache, size, element);
			} else {
				insert(cache, size, index, element);
			}
		}

		private void insert(List<T> cache, int size, int index, T element) {
			updateIndexOnInsert(cache, size, index, element);

			setParent(element);
		}

		private boolean insert(List<T> cache, int size, int index, Collection<? extends T> elements) {
			int insertSize = elements.size();
			if (insertSize == 0) {
				return false;
			}

			updateIndexOnInsert(cache, size, index, elements);

			for (T element : elements) {
				setParent(element);
			}
			return true;
		}

		private boolean append(List<T> cache, int size, T element) {
			updateOrderOnAppend(cache, size, element);

			// After updating the sort order attributes to prevent inserting in the wrong oder in
			// the cache list.
			setParent(element);
			return true;
		}

		private boolean append(List<T> cache, int size, Collection<? extends T> elements) {
			int appendSize = elements.size();
			if (appendSize == 0) {
				return false;
			}

			updateOrderOnAppend(cache, size, elements);
			
			// After updating the sort order attributes to prevent inserting in the wrong oder in
			// the cache list.
			for (T element : elements) {
				setParent(element);
			}

			return true;
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			checkAdd(c);

			List<T> cache = lookup();
			int size = cache.size();
			return append(cache, size, c);
		}

		@Override
		public boolean addAll(int index, Collection<? extends T> c) {
			checkAdd(c);

			List<T> cache = lookup();
			int size = cache.size();
			if (index < 0 || index > size) {
				throw new IndexOutOfBoundsException("Index '" + index + "' of range [0, " + size + "].");
			}

			if (index == size) {
				return append(cache, size, c);
			} else {
				return insert(cache, size, index, c);
			}
		}

		@Override
		public T remove(int index) {
			return remove(lookup(), index);
		}

		@Override
		public boolean remove(Object o) {
			return remove(lookup(), o);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			List<T> cache = lookup();

			boolean removedAny = false;
			for (Object o : c) {
				boolean removed = remove(cache, o);
				removedAny |= removed;
			}
			return removedAny;
		}

		private boolean remove(List<T> cache, Object o) {
			int index = cache.indexOf(o);
			if (index < 0) {
				return false;
			}

			remove(cache, index);
			return true;
		}
	
		@Override
		public boolean retainAll(Collection<?> c) {
			boolean modified = false;
			List<T> cache = lookup();
			for (int n = cache.size() - 1; n >= 0; n--) {
				if (!c.contains(cache.get(n))) {
					remove(cache, n);
					modified = true;
				}
			}

			return modified;
		}

		@Override
		public void clear() {
			List<T> cache = lookup();
			for (int n = cache.size() - 1; n >= 0; n--) {
				remove(cache, n);
			}
		}

		@Override
		public String toString() {
			return lookup().toString();
		}

		@Override
		public int hashCode() {
			int hashCode = 1;
			Iterator<T> it = iterator();
			while (it.hasNext()) {
				T obj = it.next();
				hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
			}
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}

			if (!(obj instanceof List)) {
				return false;
			}

			ListIterator<T> it1 = listIterator();
			ListIterator<?> it2 = ((List<?>) obj).listIterator();
			while (it1.hasNext() && it2.hasNext()) {
				T o1 = it1.next();
				Object o2 = it2.next();
				if (!(o1 == null ? o2 == null : o1.equals(o2))) {
					return false;
				}
			}
			return !(it1.hasNext() || it2.hasNext());
		}

		private T remove(List<T> cache, int index) {
			updateIndexOnRemove(cache, index);

			T element = cache.get(index);
			clearParent(element);
			return element;
		}

		/**
		 * Updates the indexes of the items in the given cache list in reaction to an upcoming
		 * insert of the given item.
		 * 
		 * <p>
		 * The cache is before the element is inserted.
		 * </p>
		 * 
		 * @param cache
		 *        The {@link List} to update {@link #order(TLObject)} for.
		 * @param size
		 *        Size of the given cache.
		 * @param index
		 *        The index where the element will be inserted.
		 * @param element
		 *        The element to insert.
		 */
		protected abstract void updateIndexOnInsert(List<T> cache, int size, int index, T element);

		/**
		 * Updates the indexes of the items in the given cache list in reaction to an upcoming
		 * insert of the given items.
		 * 
		 * <p>
		 * The cache is before the elements are inserted.
		 * </p>
		 * 
		 * @param cache
		 *        The {@link List} to update {@link #order(TLObject)} for.
		 * @param size
		 *        Size of the given cache.
		 * @param index
		 *        The index where the elements will be inserted.
		 * @param elements
		 *        The elements to insert.
		 */
		protected abstract void updateIndexOnInsert(List<T> cache, int size, int index,
				Collection<? extends T> elements);

		/**
		 * Updates the indexes of the items in the given cache list in reaction to an upcoming
		 * append of the given item.
		 * 
		 * <p>
		 * The cache is before the element is appended.
		 * </p>
		 * 
		 * @param cache
		 *        The {@link List} to update {@link #order(TLObject)} for.
		 * @param size
		 *        Size of the given cache.
		 * @param element
		 *        The element to append.
		 */
		protected abstract void updateOrderOnAppend(List<T> cache, int size, T element);

		/**
		 * Updates the indexes of the items in the given cache list in reaction to an upcoming
		 * append of the given items.
		 * 
		 * <p>
		 * The cache is before the elements are appended.
		 * </p>
		 * 
		 * @param cache
		 *        The {@link List} to update {@link #order(TLObject)} for.
		 * @param size
		 *        Size of the given cache.
		 * @param elements
		 *        The elements to append.
		 */
		protected abstract void updateOrderOnAppend(List<T> cache, int size, Collection<? extends T> elements);

		/**
		 * Updates the indexes of the items in the given cache list in reaction to an upcoming
		 * removal of the item at given index.
		 * 
		 * <p>
		 * The cache is before the removal.
		 * </p>
		 * 
		 * @param cache
		 *        The {@link List} to update {@link #order(TLObject)} for.
		 * @param index
		 *        The index of the element to remove.
		 */
		protected abstract void updateIndexOnRemove(List<T> cache, int index);

		private void setParent(T element) {
			setContainer(element, baseItem());
		}

		private void clearParent(T element) {
			setContainer(element, null);
		}

		private Object container(T item) {
			try {
				return item.tHandle().getAttributeValue(referenceAttribute());
			} catch (NoSuchAttributeException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}

		private void setContainer(T item, Object container) {
			item.tHandle().setAttributeValue(referenceAttribute(), container);
		}

		private String referenceAttribute() {
			return _cache.getQuery().getReferenceAttribute();
		}

		/**
		 * Setter for {@link #order(TLObject)}
		 */
		protected void setOrder(T item, int orderingValue) {
			setOrder(item, Integer.valueOf(orderingValue));
		}

		private void setOrder(T item, Object orderingValue) {
			try {
				item.tHandle().setAttributeValue(_orderAttribute, orderingValue);
			} catch (DataObjectException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}

		/**
		 * The order value of the given {@link TLObject}
		 */
		protected int order(T item) {
			return ((Number) orderValue(item)).intValue();
		}

		private Object orderValue(T item) {
			try {
				return item.tHandle().getAttributeValue(_orderAttribute);
			} catch (NoSuchAttributeException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}

		final ModCountList<T> lookup() {
			return cache().directItems();
		}

		private LiveAssociationsList<T> cache() {
			return (LiveAssociationsList<T>) _cache.lookup();
		}

		private final class SubList extends AbstractList<T> {
		
			private final int _start;
		
			private int _stop;
		
			public SubList(int start, int stop) {
				_start = start;
				_stop = stop;
				updateModCount();
			}
		
			@Override
			public int size() {
				checkForComodification();
				return _stop - _start;
			}
		
			@Override
			public T get(int index) {
				checkIndex(index);
				checkForComodification();
				return ListView.this.get(baseIndex(index));
			}
		
			@Override
			public List<T> subList(int fromIndex, int toIndex) {
				checkRange(fromIndex, toIndex);
				return new SubList(baseIndex(fromIndex), baseIndex(toIndex));
			}
		
			@Override
			public void add(int index, T element) {
				checkIndex(index);
				ListView.this.add(baseIndex(index), element);
				_stop++;
				updateModCount();
			}
		
			@Override
			public T remove(int index) {
				checkIndex(index);
				T result = ListView.this.remove(baseIndex(index));
				_stop--;
				updateModCount();
				return result;
			}
		
			@Override
			public T set(int index, T element) {
				checkIndex(index);
				T result = ListView.this.set(baseIndex(index), element);
				updateModCount();
				return result;
			}
		
			private void checkForComodification() {
				if (ListView.this.modCount() != this.modCount)
					throw new ConcurrentModificationException();
			}

			private void updateModCount() {
				modCount = ListView.this.modCount();
			}
		
			private int baseIndex(int index) {
				return _start + index;
			}
		
			private void checkRange(int fromIndex, int toIndex) {
				if (toIndex < fromIndex) {
					throw new IllegalArgumentException("Negative range [" + fromIndex + "," + toIndex + "): ");
				}
				checkIndex(fromIndex);
				checkStop(toIndex);
			}
		
			private void checkIndex(int index) {
				if (index >= size()) {
					throw new IndexOutOfBoundsException("Index after end of sublist [" + _start + "," + _stop + "): "
						+ index);
				}
				if (index < 0) {
					throw new IndexOutOfBoundsException("Negative index access: " + index);
				}
			}
		
			private void checkStop(int stop) {
				if (stop > size()) {
					throw new IndexOutOfBoundsException("Position after end of sublist [" + _start + "," + stop + "): "
						+ stop);
				}
				if (stop < 0) {
					throw new IndexOutOfBoundsException("Negative index access: " + stop);
				}
			}
		}

		final KnowledgeItem baseItem() {
			return _cache.getBaseItem();
		}

	}

	/**
	 * {@link ArrayList} allowing public access to the current {@link #modCount()}.
	 */
	protected static class ModCountList<T> extends ArrayList<T> {

		/**
		 * Creates a new {@link LiveAssociationsList.ModCountList}.
		 * 
		 * @param c
		 *        see {@link ArrayList#ArrayList(Collection)}
		 */
		public ModCountList(Collection<? extends T> c) {
			super(c);
		}

		/**
		 * The number of times this list has been structurally modified.
		 * 
		 * @see #modCount
		 */
		public int modCount() {
			return modCount;
		}

	}

}
