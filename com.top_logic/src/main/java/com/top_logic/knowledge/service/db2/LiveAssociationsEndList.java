/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.MutableList;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.AssociationQueryUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.knowledge.util.ReferenceAccess;
import com.top_logic.model.TLObject;

/**
 * {@link MutableList} to wrap a live list containing {@link KnowledgeAssociation} which allows
 * access to an end of the association.
 * 
 * @see LiveAssociationsList
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LiveAssociationsEndList<T extends TLObject> extends MutableList<T> {

	private final TLObject _self;

	private final OrderedLinkQuery<KnowledgeAssociation> _associationQuery;

	private final List<KnowledgeAssociation> _links;

	private final Mapping<KnowledgeAssociation, T> _endAccess;

	private final boolean _outgoingQuery;

	/**
	 * Creates a new {@link LiveAssociationsEndList}.
	 * 
	 * @param self
	 *        The self end of the association.
	 * @param associationQuery
	 *        Live association query.
	 * @param liveLinks
	 *        The live links.
	 * @param targetType
	 *        The type of the end to access.
	 */
	public LiveAssociationsEndList(TLObject self, OrderedLinkQuery<KnowledgeAssociation> associationQuery,
			List<KnowledgeAssociation> liveLinks, Class<T> targetType) {
		_outgoingQuery = AssociationQueryUtil.isOutgoing(associationQuery);
		_self = self;
		_associationQuery = associationQuery;
		_endAccess = endAccess(_outgoingQuery, targetType);
		_links = liveLinks;
	}

	private static <T extends TLObject> Mapping<KnowledgeAssociation, T> endAccess(boolean outgoing,
			Class<T> targetType) {
		if (outgoing) {
			return ReferenceAccess.outgoingAccess(targetType);
		} else {
			return ReferenceAccess.incomingAccess(targetType);
		}
	}

	@Override
	public T remove(int index) {
		KnowledgeAssociation link = _links.get(index);
		T result = _endAccess.map(link);

		/* Fetch all necessary information before removal of link, as link can not be accessed after
		 * removal. */
		removeLink(link);
		return result;
	}

	@Override
	public T set(int index, T element) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		KnowledgeAssociation currentLink = _links.get(index);
		T formerElement = _endAccess.map(currentLink);
		if (formerElement == element) {
			// setting of same object again.
			return formerElement;
		}

		Number newOrder = getOrder(currentLink);

		/* Fetch all necessary information before removal of link, as link can not be accessed after
		 * removal. */
		removeLink(currentLink);

		createLink(index, element, newOrder);

		return formerElement;
	}

	private Number getOrder(KnowledgeAssociation link) {
		return OrderedLinkUtil.getOrder(link, orderAttribute());
	}

	@Override
	public void add(int index, T element) {
		if (index < 0 || index > size()) {
			throw new IndexOutOfBoundsException();
		}
		Number order = OrderedLinkUtil.determineInsertOrder(_links, index, orderAttribute());

		createLink(index, element, order);
	}

	@Override
	public T get(int index) {
		return _endAccess.map(_links.get(index));
	}

	@Override
	public int size() {
		return _links.size();
	}

	private void removeLink(KnowledgeAssociation link) {
		/* Delete association. The magic of information of caches will update list. */
		{
			if (_outgoingQuery) {
				DBKnowledgeAssociation.clearDestinationAndRemoveLink(link);
			} else {
				DBKnowledgeAssociation.clearSourceAndRemoveLink(link);
			}
		}
	}

	private void createLink(int index, T element, Object order) {
		/* Create association. The magic of information of caches will update list. */
		newAssociation(element, order);
		assert element == get(index);
	}

	private KnowledgeAssociation newAssociation(T target, Object order) {
		KeyValueBuffer<String, Object> initialValues = new KeyValueBuffer<String, Object>()
			.put(orderAttribute(), order);
		KnowledgeBase kb = _self.tHandle().getKnowledgeBase();
		AssociationQuery.fillInitialValuesFromQueryArguments(kb, initialValues, _associationQuery.getAttributeQuery());
		addReferences(initialValues, target);
		return kb.createKnowledgeItem(_associationQuery.getAssociationTypeName(), initialValues,
			KnowledgeAssociation.class);
	}

	private void addReferences(KeyValueBuffer<String, Object> initialValues, T target) {
		if (_outgoingQuery) {
			initialValues.put(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, _self.tHandle());
			initialValues.put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, target.tHandle());
		} else {
			initialValues.put(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, target.tHandle());
			initialValues.put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, _self.tHandle());
		}
	}

	private String orderAttribute() {
		return _associationQuery.getOrderAttribute();
	}

	@Override
	public void sort(Comparator<? super T> c) {
		_links.sort((l1, l2) -> c.compare(_endAccess.map(l1), _endAccess.map(l2)));
	}

}
