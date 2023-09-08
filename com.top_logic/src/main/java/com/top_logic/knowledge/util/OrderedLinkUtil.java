/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.model.TLObject;

/**
 * Utility functions for loading and storing ordered collection values from and to associations with
 * an ordering attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OrderedLinkUtil {

	/** The maximum value for the sort-order attribute. */
	public static final int MAX_ORDER = Integer.MAX_VALUE;

	/** The default increment of the sort-order attribute when a new entry is appended. */
	public static final int APPEND_INC = 8192;

	/** The default increment of the sort-order attribute when a new entry is inserted. */
	public static final int INSERT_INC = 256;

	/**
	 * The number of items looked ahead when computing differences between the original and the
	 * updated list.
	 * 
	 * <p>
	 * The value must be one plus the number of deletions in the middle of a list that should be
	 * accepted without re-indexing.
	 * </p>
	 */
	private static final int MAX_LOOKAHEAD = 4;

	/**
	 * Stores an ordered collection value into association links with an order attribute.
	 * 
	 * <p>
	 * In contrast to
	 * {@link AbstractWrapper#updateOrderedLinkSet(TLObject, OrderedLinkQuery, List)}, this
	 * implementation can handle ordered bags (true list values) instead only ordered sets (at the
	 * cost of additional association link instances).
	 * </p>
	 * 
	 * @param self
	 *        The base object containing the value.
	 * @param associationQuery
	 *        The association to create links in. The query must <b>not</b> have a live result.
	 * @param newValues
	 *        The ordered value to store.
	 * 
	 * @see AbstractWrapper#resolveOrderedValue(TLObject, OrderedLinkQuery, Class) Resolving the
	 *      corresponding attribute value.
	 * @see OrderedLinkQuery#getOrderAttribute()
	 * @see AbstractWrapper#updateOrderedLinkSet(TLObject, OrderedLinkQuery, List) Storing the
	 *      association inline into the values.
	 */
	public static void setOrderedValue(TLObject self, OrderedLinkQuery<KnowledgeAssociation> associationQuery,
			List<? extends TLObject> newValues) {
		if (associationQuery.hasLiveResult()) {
			/* Ticket #27402: It is not possible to use a "live" query. The live query updates its
			 * result incrementally. During execution, new associations are created and old
			 * associations can be reused. Finally, each association is given a new order. During
			 * this process, the internal list may become invalid because the order changes
			 * spontaneously, so elements may not be found. */
			throw new IllegalArgumentException("Setting ordered value is not possible with a live result query.");
		}
		String association = associationQuery.getAssociationTypeName();
		String orderAttribute = associationQuery.getOrderAttribute();
		List<KnowledgeAssociation> oldLinks = AbstractWrapper.resolveLinks(self, associationQuery);
		List<KnowledgeAssociation> links = reorderLinks(self.tHandle(), association, oldLinks, newValues);
		updateIndices(links, orderAttribute);
	}

	/**
	 * Update the given order attribute in a way that it reflects the implicit order of the given
	 * objects.
	 * 
	 * @param links
	 *        The objects to reorder.
	 * @param orderAttribute
	 *        The integer attribute to update with the ordering criterium.
	 */
	public static <L extends TLObject> void updateIndices(List<L> links, String orderAttribute) {
		try {
			assignIndicesOptimized(links, orderAttribute);
		} catch (IndexRangeToShort ex) {
			try {
				assignIndices(links, 0, links.size(), MAX_ORDER, orderAttribute, APPEND_INC);
			} catch (IndexRangeToShort ex1) {
				throw new KnowledgeBaseRuntimeException("Failed to set ordered link attribute.", ex);
			}
		}
	}

	private static List<KnowledgeAssociation> reorderLinks(KnowledgeItem self, String association,
			List<KnowledgeAssociation> oldLinks, List<? extends TLObject> newValues) {
		return tryReorderLinks(self, association, oldLinks, newValues);
	}

	private static List<KnowledgeAssociation> tryReorderLinks(KnowledgeItem self, String association,
			List<KnowledgeAssociation> oldLinks, List<? extends TLObject> newValues) throws DataObjectException {

		KnowledgeBase kb = self.getKnowledgeBase();
		List<KnowledgeAssociation> newLinks = new ArrayList<>(newValues.size());

		int oldIdx = 0;
		int oldSize = oldLinks.size();
		for (int newIdx = 0, cnt = newValues.size(); newIdx < cnt; newIdx++) {
			TLObject newValue = newValues.get(newIdx);
	
			// Search in oldLinks with limited lookahead a link that has newValue in its destination
			// reference. If such link is found, the skipped links are deleted and the found link is
			// reused. If no such link is found, newValue is assumed to be inserted.
			searchLink:
			{
				for (int lookaheadIdx = oldIdx, lookaheadStop = Math.min(oldIdx + MAX_LOOKAHEAD, oldSize);
						lookaheadIdx < lookaheadStop; lookaheadIdx++) {
					KnowledgeAssociation oldLink = oldLinks.get(lookaheadIdx);
					if (destination(oldLink) == newValue) {
						newLinks.add(oldLink);
						kb.deleteAll(oldLinks.subList(oldIdx, lookaheadIdx));
						oldIdx = lookaheadIdx + 1;
						break searchLink;
					}
				}

				// Not found, assume insert.
				KnowledgeAssociation newLink = kb.createAssociation(self, newValue.tHandle(), association);
				newLinks.add(newLink);
			}
		}
	
		kb.deleteAll(oldLinks.subList(oldIdx, oldSize));
	
		return newLinks;
	}

	/**
	 * Assign new values to the order attribute by keeping non-conflicting order values.
	 * 
	 * @param orderAttribute
	 *        The integer attribute to store the ordering criterium in.
	 * 
	 * @throws IndexRangeToShort
	 *         If optimized assignment is not possible.
	 */
	private static <L extends TLObject> void assignIndicesOptimized(List<L> links, String orderAttribute)
			throws IndexRangeToShort {
		// The index of the last link in links that had an unchanged non-conflicting order
		// assignment.
		int last = -1;
	
		// The oder value of the last link.
		int lastOrder = -1;
		for (int n = 0, cnt = links.size(); n < cnt; n++) {
			L link = links.get(n);
	
			Number linkOderValue = getOrder(link, orderAttribute);
			if (linkOderValue != null) {
				int linkOrder = linkOderValue.intValue();
				if (linkOrder > lastOrder) {
					// The current link has a non-conflicting index value already set.
					if (last < n - 1) {
						// There are links between last and n that require a new order assignment.
						int lastPossibleOrderValue = linkOrder - 1;
						assignIndices(links, last + 1, n, lastPossibleOrderValue, orderAttribute, INSERT_INC);
					}
	
					last = n;
					lastOrder = linkOrder;
				}
			}
		}
		if (last < links.size() - 1) {
			int lastPossibleOrderValue = MAX_ORDER;
			assignIndices(links, last + 1, links.size(), lastPossibleOrderValue, orderAttribute, APPEND_INC);
		}
	}

	/**
	 * Assign new order values to links in the given range.
	 * 
	 * @param links
	 *        All links.
	 * @param start
	 *        The index of the first link to assign a new oder value to.
	 * @param stop
	 *        The index after the last index to assign a new order value to.
	 * @param lastPossibleOrderValue
	 *        The last order value that is available for assignment.
	 * @param orderAttribute
	 *        The integer attribute to store the ordering criterium in.
	 * @param maxDelta
	 *        Maximum difference between the new orders of the links.
	 * 
	 * @throws IndexRangeToShort
	 *         If assignment is not possible due to missing index values.
	 */
	private static <L extends TLObject> void assignIndices(List<L> links, int start, int stop,
			int lastPossibleOrderValue, String orderAttribute, int maxDelta) throws IndexRangeToShort {
		int firstPossibleOrderValue = firstPossibleValue(links, start, orderAttribute);
		int delta = delta(firstPossibleOrderValue, lastPossibleOrderValue, stop - start, maxDelta);
		applyOrder(links, start, stop, orderAttribute, firstPossibleOrderValue, delta);
	}

	private static <L extends TLObject> void applyOrder(List<L> links, int start, int stop, String orderAttribute,
			int previousOrderValue, int delta) throws DataObjectException {
		int offset = delta / 2;
		int newOrderValue = previousOrderValue + offset;
		List<L> subList = links.subList(start, stop);
		/* Iterate over a copy of the links list, because it may be a "live" list, such that
		 * modifying the order attribute also the list order is changed directly. */
		for (TLObject link : subList.toArray(new TLObject[subList.size()])) {
			setOrder(link, orderAttribute, newOrderValue);
			newOrderValue += delta;
		}
	}

	private static <L extends TLObject> int firstPossibleValue(List<L> links, int start, String orderAttribute)
			throws IndexRangeToShort {
		int firstPossibleOrderValue;
		if (start == 0) {
			firstPossibleOrderValue = 0;
		} else {
			int preceedingOrderValue = getOrder(links.get(start - 1), orderAttribute).intValue();
			if (preceedingOrderValue == MAX_ORDER) {
				// It is not possible to add 1 to the maximum value.
				throw new IndexRangeToShort();
			}
			firstPossibleOrderValue = preceedingOrderValue + 1;
		}
		return firstPossibleOrderValue;
	}

	private static int delta(int firstPossibleOrderValue, int lastPossibleOrderValue, int linkCnt, int maxDelta)
			throws IndexRangeToShort {
		// The number of available index values. Note, lastPossibleOrderValue is potentially
		// MAX_ORDER and firstPossibleOrderValue 0. Therefore, the result of the expression
		// potentially exceeds the integer range.
		long availableOrderRange = ((long) lastPossibleOrderValue) - firstPossibleOrderValue + 1;
	
		if (linkCnt > availableOrderRange) {
			throw new IndexRangeToShort();
		}
	
		long delta = availableOrderRange / linkCnt;
		/* Keep half of the remaining "space" free for further mass insert operations. Otherwise,
		 * every single insert would cause a recalculation and reassignment of every sort-order
		 * value in that list.
		 * 
		 * delta is at most MAX_ORDER + 1, so that half is again in integer range. */
		int halfDelta = (int) (delta / 2);
		if (halfDelta > maxDelta) {
			return maxDelta;
		}
		return Math.min(halfDelta, maxDelta);
	}

	/**
	 * Returns the order which the element must have to insert into the given list at the given
	 * index.
	 * 
	 * <p>
	 * It is supposed that the given links are already sorted using the given order attribute. This
	 * method computes a order which can be set to an element to insert into the given list at the
	 * index position without violating the sorted property.
	 * </p>
	 * 
	 * <p>
	 * When it is not possible to compute such an order, the links are re-indexed (not necessarily
	 * all) to ensure such an order exists.
	 * </p>
	 * 
	 * @param links
	 *        The list of {@link TLObject} in which an element should be inserted.
	 * @param index
	 *        The index in the list which the new element should have.
	 * @param orderAttribute
	 *        The attribute which holds the sort order.
	 * 
	 * @return The order which the element must have to insert into the given list at the given
	 *         index.
	 * 
	 * @throws IndexOutOfBoundsException
	 *         if the index is out of range (index < 0 || index >= size())
	 */
	public static <L extends TLObject> Number determineInsertOrder(List<L> links, int index, String orderAttribute) {
		try {
			return getInsertOrder(links, index, orderAttribute);
		} catch (IndexRangeToShort ex) {
			try {
				return ensureInsertPosition(links, index, orderAttribute);
			} catch (IndexRangeToShort ex1) {
				throw new KnowledgeBaseRuntimeException(ex1);
			}
		}
	}

	private static <L extends TLObject> int ensureInsertPosition(List<L> links, int index, String orderAttribute)
			throws IndexRangeToShort, DataObjectException {
		int numberLinks = links.size();
		if (index == 0) {
			int delta = delta(0, MAX_ORDER, numberLinks + 1, APPEND_INC);
			applyOrder(links, 0, numberLinks, orderAttribute, delta, delta);
			return delta;
		}
		if (index == numberLinks) {
			int delta = delta(0, MAX_ORDER, numberLinks + 1, APPEND_INC);
			applyOrder(links, 0, numberLinks, orderAttribute, 0, delta);
			return getOrder(links.get(numberLinks - 1), orderAttribute).intValue() + delta;
		}
		int order = getOrder(links.get(index), orderAttribute).intValue();
		int delta = delta(order, MAX_ORDER, numberLinks - index + 1, APPEND_INC);
		applyOrder(links, index, numberLinks, orderAttribute, order + delta, delta);
		return order + (delta / 2);
	}

	/**
	 * Assign new values to the order attribute by keeping non-conflicting order values.
	 * 
	 * @param orderAttribute
	 *        The integer attribute to store the ordering criterium in.
	 * 
	 * @throws IndexRangeToShort
	 *         If optimized assignment is not possible.
	 */
	private static <L extends TLObject> int getInsertOrder(List<L> links, int index, String orderAttribute)
			throws IndexRangeToShort {
		if (links.isEmpty()) {
			return APPEND_INC;
		}
		if (index == links.size()) {
			int order = firstPossibleValue(links, index, orderAttribute);
			int delta = delta(order, MAX_ORDER, 1, APPEND_INC);
			return order + (delta / 2);
		}

		int previousIndex;
		if (index == 0) {
			previousIndex = -1;
		} else {
			previousIndex = getOrder(links.get(index - 1), orderAttribute).intValue();
		}
		int order = getOrder(links.get(index), orderAttribute).intValue();
		int delta = order - previousIndex;
		if (delta < 2) {
			// no space between elements.
			throw new IndexRangeToShort();
		}
		return previousIndex + (delta / 2);
	}

	private static TLObject destination(KnowledgeAssociation link) {
		try {
			return link.getDestinationObject().getWrapper();
		} catch (InvalidLinkException ex) {
			throw new WrapperRuntimeException(ex);
		}
	}

	/**
	 * Determines the {@link Number} stored in the <code>orderAttribute</code> of the given link.
	 * 
	 * @param link
	 *        The link to get order from.
	 * @param orderAttribute
	 *        Name of the attribute storing the sort order.
	 * 
	 * @throws IllegalArgumentException
	 *         when the given link has no attribute with the given name.
	 */
	@FrameworkInternal
	public static Number getOrder(TLObject link, String orderAttribute) {
		KnowledgeItem handle = link.tHandle();
		try {
			return (Number) handle.getAttributeValue(orderAttribute);
		} catch (NoSuchAttributeException ex) {
			throw errorMissingOrderAttribute(handle.tTable(), orderAttribute, ex);
		}
	}

	static void setOrder(TLObject link, String orderAttribute, int order) throws DataObjectException {
		KnowledgeItem handle = link.tHandle();
		try {
			handle.setAttributeValue(orderAttribute, order);
		} catch (NoSuchAttributeException ex) {
			throw errorMissingOrderAttribute(handle.tTable(), orderAttribute, ex);
		}
	}

	static IllegalArgumentException errorMissingOrderAttribute(MetaObject linkType, String orderAttribute, NoSuchAttributeException ex) {
		throw new IllegalArgumentException("Link type '" + linkType + "' does not suport the '" + orderAttribute
			+ "' attribute.", ex);
	}

	/**
	 * {@link Comparator} that compares arbitrary {@link TLObject}s accoring to a given integer
	 * attribute.
	 */
	public static final class LinkOrder implements Comparator<TLObject> {
	
		private final String _orderAttribute;
	
		/**
		 * Creates a {@link LinkOrder}.
		 * 
		 * @param orderAttribute
		 *        The name of the integer attribute that is used to compare objects.
		 */
		public LinkOrder(String orderAttribute) {
			_orderAttribute = orderAttribute;
		}
	
		@Override
		public int compare(TLObject item1, TLObject item2) {
			int index1 = getOrder(item1).intValue();
			int index2 = getOrder(item2).intValue();

			if (index1 < index2) {
				return -1;
			} else if (index1 > index2) {
				return 1;
			} else {
				return 0;
			}
		}

		private Number getOrder(TLObject link) {
			return OrderedLinkUtil.getOrder(link, _orderAttribute);
		}
	}

	private static class IndexRangeToShort extends Exception {
		public IndexRangeToShort() {
			super();
		}
	}

}
