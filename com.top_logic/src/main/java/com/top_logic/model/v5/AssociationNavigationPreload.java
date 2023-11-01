/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.v5;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQueryUtil;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.DBObjectKey;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.PreloadContext;

/**
 * {@link AssociationCachePreload} that also fetches the objects on the other end.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AssociationNavigationPreload extends AssociationCachePreload {

	/**
	 * Creates a {@link AssociationNavigationPreload}.
	 * 
	 * @param query
	 *        See {@link #getQuery()}.
	 */
	public AssociationNavigationPreload(
			AbstractAssociationQuery<KnowledgeAssociation, ? extends Collection<KnowledgeAssociation>> query) {
		super(query);
	}

	@Override
	public void prepare(PreloadContext context, Collection<?> baseObjects) {
		super.prepare(context, baseObjects);

		KnowledgeBase kb = getWrappersKnowledgeBase(baseObjects);
		if (kb == null) {
			return;
		}
		
		BulkIdLoad loader = new BulkIdLoad(kb);
		@SuppressWarnings("unchecked")
		AbstractAssociationQuery<KnowledgeAssociation, ? extends Collection<KnowledgeAssociation>> query =
			(AbstractAssociationQuery<KnowledgeAssociation, ? extends Collection<KnowledgeAssociation>>) getQuery();
		boolean outgoing = AssociationQueryUtil.isOutgoing(query);
		for (Object baseObject : baseObjects) {
			TLObject wrapper = (TLObject) baseObject;
			// Only valid wrappers (still alive) can be used to resolve links
			if (wrapper.tValid()) {
				Collection<KnowledgeAssociation> links = AbstractWrapper.resolveLinks(wrapper, query);
				for (KnowledgeAssociation link : links) {
					DBObjectKey key;
					if (outgoing) {
						key = (DBObjectKey) link.getDestinationIdentity();
					} else {
						key = (DBObjectKey) link.getSourceIdentity();
					}
					
					loader.add(key);
				}
			}

		}

		List<KnowledgeItem> destinationKIs = loader.load();
		// Associations are designed to reference KnowledgeObjects. Therefore cast is ok.
		List<KnowledgeObject> destinationKOs = CollectionUtil.dynamicCastView(KnowledgeObject.class, destinationKIs);
		if (destinationKOs.size() == 0) {
			return;
		}
		
		List<TLObject> destinations = WrapperFactory.getWrappersForKOsGeneric(destinationKOs);

		processDestinations(context, destinations);
	}

	/**
	 * Hook for processing the destination objects.
	 */
	protected void processDestinations(PreloadContext context, List<TLObject> destinations) {
		for (Object destination : destinations) {
			context.keepObject(destination);
		}
	}
}