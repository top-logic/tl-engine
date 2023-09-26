/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.knowledge.wrap.person;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.SourceIterator;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * {@link PersonManager} deleting stored queries of the person also.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class ElementPersonManager extends PersonManager {

	/**
	 * Creates a new {@link ElementPersonManager}.
	 */
	@CalledByReflection
	public ElementPersonManager(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void postProcessUserDeletion(Person aPerson) {
		super.postProcessUserDeletion(aPerson);
		deleteStoredQueries(aPerson);
	}

	protected void deleteStoredQueries(Person aPerson) {
		SourceIterator it = new SourceIterator(aPerson.tHandle().getIncomingAssociations(StoredQuery.OWNER_ASSOCIATION));
		while (it.hasNext()) {
			KnowledgeObject storedQueryKO = it.next();
			if (storedQueryKO.isInstanceOf(StoredQuery.STORED_QUERY_KO)) try {
					StoredQuery storedQuery = (StoredQuery) WrapperFactory.getWrapper(storedQueryKO);
				List<?> publishedGroups = MapBasedPersistancySupport.getGroupAssociation(storedQuery);
				if (publishedGroups.isEmpty() || getRoot() == aPerson) {
					// not published to any group - delete it
					storedQueryKO.delete();
				}
				else {
					// reassign to root user ...
						aPerson.getKnowledgeBase().createAssociation(storedQueryKO, getRoot().tHandle(),
							StoredQuery.OWNER_ASSOCIATION);
					it.currentKA().delete();
				}

			}
			catch (Exception e) {
				Logger.warn("Failed to remove stored querys for person.", e, ElementPersonManager.class);
			}
		}
	}

}
