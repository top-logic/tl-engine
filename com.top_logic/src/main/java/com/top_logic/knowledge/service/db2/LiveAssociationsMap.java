/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.iterators.AbstractMapIteratorDecorator;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.InverseBidiMap;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;

/**
 * {@link Associations} buffer that provides an ordered set of links.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class LiveAssociationsMap<K, T extends TLObject> extends AssociationsMap<K, T> {

	private final BidiMap<K, T> _mapView;

	private BidiMap<K, T> _linkMap;

	/**
	 * Whether a view to {@link #_linkMap} has been handed out.
	 */
	private boolean _shared;

	private int _modCount;

	/**
	 * Creates a {@link LiveAssociationsMap}.
	 * 
	 * @param associationCache
	 *        The owning cache.
	 * @param minValidity
	 *        See {@link #minValidity()}.
	 * @param maxValidity
	 *        See {@link #maxValidity()}.
	 * @param items
	 *        The initial link items to put into the buffer.
	 * @param localCache
	 *        See {@link #isLocalCache()}.
	 */
	public LiveAssociationsMap(IndexedLinkCache<K, T> associationCache, long minValidity, long maxValidity,
			List<T> items, boolean localCache, KnowledgeItem baseItem) {
		super(associationCache, minValidity, maxValidity, items, localCache);

		_linkMap = index(items);
		_mapView = new MapView<>(baseItem, associationCache);
	}

	@Override
	protected Iterable<T> getAssociationItems() {
		return links().values();
	}

	final synchronized BidiMap<K, T> linksShared() {
		_shared = true;
		return links();
	}

	@Override
	protected synchronized final BidiMap<K, T> links() {
		return _linkMap;
	}

	final int modCount() {
		return _modCount;
	}

	@Override
	public synchronized BidiMap<K, T> getAssociations() {
		return _mapView;
	}

	@Override
	protected void beforeChange() {
		_modCount++;

		if (_shared) {
			_linkMap = new BidiHashMap<>(_linkMap);
			_shared = false;
		}
	}

	static final class MapView<K, T extends TLObject> implements BidiMap<K, T> {
	
		private final KnowledgeItem _baseItem;
	
		private final IndexedLinkCache<K, T> _cache;
	
		private Set<K> _keySet;
	
		private Set<T> _valueSet;

		private EntrySetView _entrySet;

		private BidiMap<T, K> _inverse;
	
		public MapView(KnowledgeItem baseItem, IndexedLinkCache<K, T> cache) {
			_baseItem = baseItem;
			_cache = cache;
		}
	
		@Override
		public int size() {
			return current().size();
		}
	
		@Override
		public boolean isEmpty() {
			return current().isEmpty();
		}
	
		@Override
		public boolean containsKey(Object key) {
			return current().containsKey(key);
		}
	
		@Override
		public boolean containsValue(Object value) {
			return current().containsValue(value);
		}
	
		@Override
		public T get(Object key) {
			return current().get(key);
		}
	
		@Override
		public K getKey(Object value) {
			return current().getKey(value);
		}
	
		@Override
		public MapIterator<K, T> mapIterator() {
			LiveAssociationsMap<K, T> cacheInstance = lookup();

			final BidiMap<K, T> linksWhenStarted = cacheInstance.linksShared();
			final int modCountWhenStarted = cacheInstance.modCount();
			return new AbstractMapIteratorDecorator<K, T>(linksWhenStarted.mapIterator()) {
				private BidiMap<K, T> _iterated = linksWhenStarted;

				private int _modCount = modCountWhenStarted;
		
				@Override
				public K next() {
					checkForModification();
					return super.next();
				}

				@Override
				public void remove() {
					MapView.this.remove(getKey());
					resetModCount();
				}

				@Override
				public T setValue(T value) {
					T oldValue = getValue();
					values().remove(oldValue);
					values().add(value);
		
					resetModCount();
					return oldValue;
				}

				private void checkForModification() {
					LiveAssociationsMap<K, T> current = lookup();
					if (current.links() != _iterated || current.modCount() != _modCount) {
						throw new ConcurrentModificationException();
					}
				}

				private void resetModCount() {
					// Prevent concurrent modification on further calls to next(). The
					// iteration will work as expected, because the initial delegate set
					// stays unmodified.
					LiveAssociationsMap<K, T> current = lookup();
					_iterated = current.links();
					_modCount = current.modCount();
				}

			};
		}
	
		@Override
		public Set<K> keySet() {
			if (_keySet == null) {
				_keySet = new KeySetView();
			}
			return _keySet;
		}
	
		@Override
		public Set<T> values() {
			if (_valueSet == null) {
				_valueSet = new ValuesView();
			}
			return _valueSet;
		}
	
		@Override
		public Set<Entry<K, T>> entrySet() {
			if (_entrySet == null) {
				_entrySet = new EntrySetView();
			}
			return _entrySet;
		}
	
		@Override
		public BidiMap<T, K> inverseBidiMap() {
			if (_inverse == null) {
				_inverse = new InverseBidiMap<>(this);
			}
			return _inverse;
		}
	
		@Override
		public T put(K key, T value) {
			throw unsupportedPut();
		}
	
		@Override
		public void putAll(java.util.Map<? extends K, ? extends T> mapToCopy) {
			throw unsupportedPut();
		}
	
		private UnsupportedOperationException unsupportedPut() {
			return new UnsupportedOperationException(
				"Must not put into an indexed collection, add to values() instead.");
		}
	
		@Override
		public T remove(Object key) {
			T value = get(key);
			if (value != null) {
				values().remove(value);
			}
			return value;
		}
	
		@Override
		public K removeValue(Object value) {
			K key = getKey(value);
			values().remove(value);
			return key;
		}
	
		@Override
		public void clear() {
			values().clear();
		}

		/**
		 * Implementation taken from {@link AbstractMap#equals(Object)}.
		 * 
		 * @see AbstractMap#equals(Object)
		 * @see Object#equals(Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;

			if (!(o instanceof Map))
				return false;
			Map<?, ?> m = (Map<?, ?>) o;
			if (m.size() != size())
				return false;

			try {
				for (Entry<K, T> e : entrySet()) {
					K key = e.getKey();
					T value = e.getValue();
					if (value == null) {
						if (!(m.get(key) == null && m.containsKey(key)))
							return false;
					} else {
						if (!value.equals(m.get(key)))
							return false;
					}
				}
			} catch (ClassCastException unused) {
				return false;
			} catch (NullPointerException unused) {
				return false;
			}

			return true;
		}

		/**
		 * Implementation taken from {@link AbstractMap#hashCode()}.
		 * 
		 * @see AbstractMap#hashCode()
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int h = 0;
			for (Entry<K, T> entry : entrySet())
				h += entry.hashCode();
			return h;
		}

		/**
		 * Implementation taken from {@link AbstractMap#toString()}.
		 * 
		 * @see AbstractMap#toString()
		 * @see Object#toString()
		 */
		@Override
		public String toString() {
			Iterator<Entry<K, T>> i = entrySet().iterator();
			if (!i.hasNext())
				return "{}";

			StringBuilder sb = new StringBuilder();
			sb.append('{');
			for (;;) {
				Entry<K, T> e = i.next();
				Object key = e.getKey();
				Object value = e.getValue();
				sb.append(key == this ? "(this Map)" : key);
				sb.append('=');
				sb.append(value == this ? "(this Map)" : value);
				if (!i.hasNext())
					return sb.append('}').toString();
				sb.append(',').append(' ');
			}
		}

	
		private final class EntrySetView extends AbstractSet<Entry<K, T>> {

			public EntrySetView() {
				super();
			}

			@Override
			public boolean add(Entry<K, T> e) {
				throw new UnsupportedOperationException(
					"Must not add to an entry set of an indexed collection, modify the values() set instead.");
			}

			@Override
			public boolean contains(Object o) {
				if (o == null) {
					return false;
				}
				if (!(o instanceof Entry<?, ?>)) {
					return false;
				}

				T value = get(((Entry<?, ?>) o).getKey());
				return o.equals(value);
			}

			@Override
			public Iterator<Entry<K, T>> iterator() {
				LiveAssociationsMap<K, T> cacheInstance = lookup();
				final BidiMap<K, T> iterated = cacheInstance.linksShared();
				final int modCount = cacheInstance.modCount();
				return new AbstractIterator<>(iterated, modCount, iterated.entrySet().iterator()) {

					@Override
					protected void internalRemove(Entry<K, T> last) {
						T element = last.getValue();
						values().remove(element);
					}

				};
			}

			@Override
			public int size() {
				return MapView.this.size();
			}
		}

		private final class KeySetView extends AbstractSet<K> {
	
			public KeySetView() {
				super();
			}
	
			@Override
			public boolean add(K e) {
				throw unsupportedAdd();
			}
	
			@Override
			public boolean addAll(java.util.Collection<? extends K> c) {
				throw unsupportedAdd();
			}
	
			@Override
			public boolean remove(Object key) {
				return values().remove(get(key));
			}
	
			@Override
			public Iterator<K> iterator() {
				LiveAssociationsMap<K, T> cacheInstance = lookup();
				final BidiMap<K, T> iterated = cacheInstance.linksShared();
				final int modCount = cacheInstance.modCount();
				return new AbstractIterator<>(iterated, modCount, iterated.keySet().iterator()) {
					@Override
					protected void internalRemove(K last) {
						keySet().remove(last);
					}
				};
			}
	
			private UnsupportedOperationException unsupportedAdd() {
				return new UnsupportedOperationException(
					"Must not add to an index set of an indexed collection.");
			}
	
			@Override
			public int size() {
				return impl().size();
			}
	
			@Override
			public boolean isEmpty() {
				return impl().isEmpty();
			}
	
			@Override
			public boolean contains(Object o) {
				return impl().contains(o);
			}
	
			@Override
			public Object[] toArray() {
				return impl().toArray();
			}
	
			@Override
			public <E> E[] toArray(E[] a) {
				return impl().toArray(a);
			}
	
			@Override
			public boolean containsAll(Collection<?> c) {
				return impl().containsAll(c);
			}
	
			final Set<K> impl() {
				return current().keySet();
			}

		}
	
		private final class ValuesView extends AbstractSet<T> {
	
			public ValuesView() {
				super();
			}
	
			@Override
			public boolean add(T e) {
				{
					Object owner = owner();
					Object ownerBefore = setOwner(e, owner);
					return owner != ownerBefore;
				}
			}
	
			@Override
			public boolean remove(Object o) {
				if (!valueType().isInstance(o)) {
					return false;
				}
	
				{
					TLObject value = (TLObject) o;
					Object ownerBefore = value.tHandle().getAttributeValue(referenceAttribute());
					if (ownerBefore != owner()) {
						return false;
					}
	
					setOwner(value, null);
					return true;
				}
			}
	
			@Override
			public Iterator<T> iterator() {
				LiveAssociationsMap<K, T> cacheInstance = lookup();
				final BidiMap<K, T> iterated = cacheInstance.linksShared();
				final int modCount = cacheInstance.modCount();
				return new AbstractIterator<>(iterated, modCount, iterated.values().iterator()) {
					@Override
					protected void internalRemove(T last) {
						ValuesView.this.remove(last);
					}
				};
			}
	
			private Object setOwner(TLObject e, Object newOwner) throws DataObjectException {
				return e.tHandle().setAttributeValue(referenceAttribute(), newOwner);
			}
	
			// Read-only API delegates to the current view.
	
			@Override
			public int size() {
				return impl().size();
			}
	
			@Override
			public boolean isEmpty() {
				return impl().isEmpty();
			}
	
			@Override
			public boolean contains(Object o) {
				return impl().contains(o);
			}
	
			@Override
			public Object[] toArray() {
				return impl().toArray();
			}
	
			@Override
			public <E> E[] toArray(E[] a) {
				return impl().toArray(a);
			}
	
			@Override
			public boolean containsAll(Collection<?> c) {
				return impl().containsAll(c);
			}
	
			final Set<T> impl() {
				return current().values();
			}
	
		}
	
		private abstract class AbstractIterator<E> implements Iterator<E> {

			private BidiMap<?, ?> _iterated;

			private int _modCount;

			private final Iterator<E> _impl;

			private E _last;

			public AbstractIterator(BidiMap<?, ?> initialIterated, int initialModCount, Iterator<E> impl) {
				_iterated = initialIterated;
				_modCount = initialModCount;
				_impl = impl;
			}

			@Override
			public boolean hasNext() {
				return _impl.hasNext();
			}

			@Override
			public E next() {
				checkForModification();
				_last = _impl.next();
				return _last;
			}

			@Override
			public void remove() {
				if (_last == null) {
					throw new IllegalStateException("Missing call to next().");
				}

				internalRemove(_last);

				_last = null;
				resetModCount();
			}

			protected abstract void internalRemove(E last);

			protected final void checkForModification() {
				LiveAssociationsMap<K, T> current = lookup();
				if (current.links() != _iterated) {
					// Global view changed to local view.
					throw new ConcurrentModificationException();
				}
				if (current.modCount() != _modCount) {
					// Local view has been modified.
					throw new ConcurrentModificationException();
				}
			}

			protected final void resetModCount() {
				// Prevent concurrent modification on further calls to next(). The
				// iteration will work as expected, because the initial delegate set
				// stays unmodified.
				LiveAssociationsMap<K, T> current = lookup();
				_iterated = current.links();
				_modCount = current.modCount();
			}

		}

		final BidiMap<K, T> current() {
			return lookup().links();
		}

		final LiveAssociationsMap<K, T> lookup() {
			return (LiveAssociationsMap<K, T>) _cache.lookup();
		}
	
		final KnowledgeItem owner() {
			return _baseItem;
		}
	
		final String referenceAttribute() {
			return _cache.getQuery().getReferenceAttribute();
		}
	
		final Class<T> valueType() {
			return _cache.getQuery().getExpectedType();
		}
	
	}

}
