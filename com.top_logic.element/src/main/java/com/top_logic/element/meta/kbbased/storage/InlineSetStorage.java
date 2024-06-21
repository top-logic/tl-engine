/*
* SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.IndexedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.export.SinglePreloadContribution;
import com.top_logic.model.v5.AssociationCachePreload;

/**
 * {@link InlineCollectionStorage} for unordered references.
 * 
 * @see InlineListStorage
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(classifiers = TLStorage.REFERENCE_CLASSIFIER)
@Label("Unsorted storage in the target table")
public class InlineSetStorage<C extends InlineSetStorage.Config<?>> extends InlineCollectionStorage<C> {

	/**
	 * Configuration options for {@link InlineSetStorage}.
	 */
	@TagName("inline-set-storage")
	public interface Config<I extends InlineSetStorage<?>> extends InlineCollectionStorage.Config<I> {
		// No additional properties here.
	}

	private IndexedLinkQuery<TLObject, TLObject> _outgoingQuery;

	/**
	 * Creates a {@link InlineSetStorage}.
	 */
	public InlineSetStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		String table = TLAnnotations.getTable(attribute.getType());
		Map<String, KnowledgeItem> filter;
		if (getConfig().getReferenceColumn() != null) {
			filter = Collections.singletonMap(getConfig().getReferenceColumn(), attribute.getDefinition().tHandle());
		} else {
			filter = Collections.emptyMap();
		}
		_outgoingQuery = IndexedLinkQuery.indexedLinkQuery(new NamedConstant(attribute.getName() + " liveQuery"),
			TLObject.class, table, getConfig().getContainerColumn(), null, TLObject.class, filter, true);
	}

	@Override
	protected String getTable() {
		return _outgoingQuery.getAssociationTypeName();
	}

	@Override
	public Collection<TLObject> getLiveCollection(TLObject object, TLStructuredTypePart attribute) {
		return liveSet(object, attribute);
	}

	/**
	 * Mapping from the targets of the composition to itself.
	 */
	private BidiMap<TLObject, TLObject> internalValue(TLObject object) {
		return AbstractWrapper.resolveLinks(object, _outgoingQuery);

	}

	private Set<TLObject> liveSet(TLObject object, TLStructuredTypePart attribute) {
		String referenceColumn = getConfig().getReferenceColumn();
		Consumer<TLObject> addCallback;
		if (referenceColumn != null) {
			addCallback = value -> checkAndSetReference(value, referenceColumn, attribute);
		} else {
			addCallback = value -> {
				// Nothing to do here.
			};
		}
		return new ReferenceSettingLiveSet<>(internalValue(object).values(),
			addCallback,
			value -> value.tHandle().setAttributeValue(referenceColumn, null));
	}

	private void checkAndSetReference(TLObject value, String refColumn, TLStructuredTypePart reference) {
		KnowledgeItem refKI = reference.getDefinition().tHandle();
		Object previousValue = value.tHandle().setAttributeValue(refColumn, refKI);
		if (previousValue != null && previousValue != refKI) {
			value.tHandle().setAttributeValue(refColumn, previousValue);
			throw new IllegalArgumentException("Value '" + value + "' for reference '" + reference
					+ "' is already assigned to reference '" + ((KnowledgeItem) previousValue).getWrapper() + "'.");
		}
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		return new HashSet<>(internalValue(object).keySet());
	}

	@Override
	protected void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		if (newValue != null && !(newValue instanceof Collection)) {
			throw new IllegalArgumentException("Given value is no Collection");
		}
		Collection<?> newValues = (Collection<?>) newValue;
		Set<TLObject> links = liveSet(object, attribute);
		if (newValues == null || newValues.isEmpty()) {
			links.clear();
			return;
		}

		Collection<TLObject> toAdd = new HashSet(newValues);
		toAdd.removeAll(links);

		Collection<TLObject> toRemove = new HashSet<>(links);
		toRemove.removeAll(newValues);
		if (toAdd.isEmpty() && toRemove.isEmpty()) {
			return; // No real change
		}
		links.removeAll(toRemove);
		links.addAll(toAdd);
	}

	@Override
	public PreloadContribution getPreload() {
		return new SinglePreloadContribution(new AssociationCachePreload(_outgoingQuery));
	}

	/**
	 * Mutable {@link AbstractSet set} that applies special callbacks to removed and added elements.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class ReferenceSettingLiveSet<E> extends AbstractSet<E> {

		private Set<E> _base;

		private Consumer<E> _addCallback;

		private Consumer<E> _removeCallback;

		/**
		 * Creates a {@link ReferenceSettingLiveSet}.
		 */
		public ReferenceSettingLiveSet(Set<E> base, Consumer<E> addCallback, Consumer<E> removeCallback) {
			_base = base;
			_addCallback = addCallback;
			_removeCallback = removeCallback;
		}

		@Override
		public int size() {
			return _base.size();
		}

		@Override
		public Iterator<E> iterator() {
			Iterator<E> baseIter = _base.iterator();
			return new Iterator<>() {

				E _lastNextResult = null;

				@Override
				public boolean hasNext() {
					return baseIter.hasNext();
				}

				@Override
				public E next() {
					_lastNextResult = baseIter.next();
					return _lastNextResult;
				}

				@Override
				public void remove() {
					if (_lastNextResult == null) {
						throw new IllegalStateException("next() was not called, or remove() already called.");
					}
					baseIter.remove();
					E removed = _lastNextResult;
					_lastNextResult = null;
					_removeCallback.accept(removed);
				}
			};
		}

		@Override
		public boolean add(E e) {
			if (contains(e)) {
				return false;
			}
			_addCallback.accept(e);
			_base.add(e);
			return true;
		}

		@Override
		public boolean contains(Object o) {
			return _base.contains(o);
		}

		@Override
		public boolean remove(Object o) {
			boolean removed = _base.remove(o);
			if (!removed) {
				return false;
			}
			try {
				_removeCallback.accept(cast(o));
			} catch (RuntimeException ex) {
				_base.add(cast(o));
				throw ex;
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		private E cast(Object o) {
			return (E) o;
		}

		@Override
		public Object[] toArray() {
			return _base.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return _base.toArray(a);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return _base.containsAll(c);
		}

	}

}
