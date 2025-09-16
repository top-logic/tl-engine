/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.MutableList;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.export.SinglePreloadContribution;
import com.top_logic.model.v5.AssociationCachePreload;

/**
 * {@link InlineCollectionStorage} for ordered references.
 * 
 * @see InlineSetStorage
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(classifiers = TLStorage.REFERENCE_CLASSIFIER)
@Label("Sorted storage in the target table")
public class InlineListStorage<C extends InlineListStorage.Config<?>> extends InlineCollectionStorage<C> {

	/**
	 * Configuration options for {@link InlineListStorage}.
	 */
	@TagName("inline-list-storage")
	@DisplayInherited(DisplayStrategy.PREPEND)
	public interface Config<I extends InlineListStorage<?>> extends InlineCollectionStorage.Config<I> {

		/**
		 * Configuration name of {@link #getOrderColumn()}.
		 */
		String ORDER_COLUMN = "order-column";

		/**
		 * Name of the column in the target table that holds the order of the referenced objects.
		 */
		@Name(ORDER_COLUMN)
		@Mandatory
		String getOrderColumn();

		/**
		 * Setter for {@link #getOrderColumn()}.
		 */
		void setOrderColumn(String value);

	}

	private OrderedLinkQuery<TLObject> _outgoingQuery;

	/**
	 * Creates a {@link InlineListStorage}.
	 */
	public InlineListStorage(InstantiationContext context, C config) {
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
		_outgoingQuery = AssociationQuery.createOrderedLinkQuery("inlineList", TLObject.class, table,
			getConfig().getContainerColumn(), getConfig().getOrderColumn(), filter, true);

		String partCol = getConfig().getReferenceColumn();
		String baseObjectCol = getConfig().getContainerColumn();
		checkKeyAttributes(attribute, partCol, baseObjectCol);
	}

	@Override
	public String getTable() {
		return _outgoingQuery.getAssociationTypeName();
	}

	@Override
	public Collection<TLObject> getLiveCollection(TLObject object, TLStructuredTypePart attribute) {
		return liveList(object, attribute);
	}

	private List<TLObject> internalValue(TLObject object) {
		return AbstractWrapper.resolveLinks(object, _outgoingQuery);
	}

	private List<TLObject> liveList(TLObject object, TLStructuredTypePart attribute) {
		String referenceColumn = getConfig().getReferenceColumn();
		Consumer<TLObject> addCallback;
		if (referenceColumn != null) {
			addCallback = value -> checkAndSetReference(value, referenceColumn, attribute);
		} else {
			addCallback = value -> {
				// Nothing to do here.
			};
		}
		return new ReferenceSettingLiveList<>(internalValue(object),
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
		return new ArrayList<>(internalValue(object));
	}

	@Override
	protected void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		if (newValue != null && !(newValue instanceof Collection)) {
			throw new IllegalArgumentException("Given value is no Collection");
		}
		Collection<?> newValues = (Collection<?>) newValue;
		List<TLObject> links = liveList(object, attribute);
		if (newValues == null || newValues.isEmpty()) {
			links.clear();
			return;
		}
		Iterator<?> srcIt = newValues.iterator();
		int destPos = 0;

		TLObject src = null;
		while (srcIt.hasNext()) {
			src = (TLObject) srcIt.next();

			if (destPos < links.size()) {
				TLObject oldVal = links.get(destPos);
				if (oldVal.tId().equals(src.tHandle().tId())) {
					// Entry exists, advance pointers.
				} else {
					// Remove as it may occur later in the list
					links.remove(src);
					// Add new entry.
					links.add(destPos, src);
				}
				src = null;
				destPos++;
			} else {
				// Reached the end of the current link list, add all at the end.
				break;
			}
		}

		if (destPos < links.size()) {
			assert src == null && !srcIt.hasNext();

			// Remove tail.
			for (int n = links.size() - 1; n >= destPos; n--) {
				links.remove(n);
			}
		} else {
			if (src != null) {
				links.add(src);
			}
			links.addAll((Collection<? extends TLObject>) CollectionUtil.toList(srcIt));
		}

	}

	@Override
	public PreloadContribution getPreload() {
		return new SinglePreloadContribution(new AssociationCachePreload(_outgoingQuery));
	}

	/**
	 * {@link MutableList} that applies special callbacks to removed and added elements.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class ReferenceSettingLiveList<E> extends MutableList<E> {

		private List<E> _base;

		private Consumer<E> _addCallback;

		private Consumer<E> _removeCallback;

		/**
		 * Creates a {@link ReferenceSettingLiveList}.
		 */
		public ReferenceSettingLiveList(List<E> base, Consumer<E> addCallback, Consumer<E> removeCallback) {
			_base = base;
			_addCallback = addCallback;
			_removeCallback = removeCallback;
		}

		@Override
		public int size() {
			return _base.size();
		}

		@Override
		public E remove(int index) {
			E removed = _base.remove(index);
			try {
				_removeCallback.accept(removed);
			} catch (RuntimeException ex) {
				_base.add(index, removed);
				throw ex;
			}
			return removed;
		}

		@Override
		public E set(int index, E element) {
			_addCallback.accept(element);
			E before = _base.set(index, element);
			try {
				_removeCallback.accept(before);
			} catch (RuntimeException ex) {
				_base.set(index, before);
				throw ex;
			}
			return before;
		}

		@Override
		public void add(int index, E element) {
			_addCallback.accept(element);
			_base.add(index, element);
		}

		@Override
		public E get(int index) {
			return _base.get(index);
		}

		@Override
		public boolean contains(Object o) {
			return _base.contains(o);
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

		@Override
		public int indexOf(Object o) {
			return _base.indexOf(o);
		}

		@Override
		public int lastIndexOf(Object o) {
			return _base.lastIndexOf(o);
		}

		@Override
		public void sort(Comparator<? super E> c) {
			_base.sort(c);
		}

	}

}
