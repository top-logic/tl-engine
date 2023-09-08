/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.util.Resources;

/**
 * Abstract super class for {@link ReadOnlyAccessor} that resolves the author of a commit.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractAuthorAccessor<T> extends ReadOnlyAccessor<T> {

	private static final Property<Map<String, Person>> PERSON_CACHE = TypedAnnotatable.propertyMap("personCache");

	/**
	 * Tries to resolve the author with the given string representation as stored in the
	 * {@link Revision} table.
	 */
	protected Object resolveAuthor(String author) {
		if (author.startsWith(SessionContext.PERSON_ID_PREFIX)) {
			return getPerson(author.substring(SessionContext.PERSON_ID_PREFIX.length()));
		}
		if (author.startsWith(SessionContext.SYSTEM_ID_PREFIX)) {
			return getSystemAuthor(author.substring(SessionContext.SYSTEM_ID_PREFIX.length()));
		}
		return author;
	}

	private Object getSystemAuthor(String systemId) {
		return Resources.getInstance().getMessage(I18NConstants.CHANGED_BY_SYSTEM, systemId);
	}

	/**
	 * Resolves the given "author" (string representation in the {@link Revision} table) to a
	 * {@link Person}.
	 * 
	 * @param author
	 *        Author from {@link Revision#getAuthor()}.
	 * @return The represented author, or <code>null</code> if the author is not a {@link Person}.
	 */
	public static Person resolvePerson(String author) {
		if (author.startsWith(SessionContext.PERSON_ID_PREFIX)) {
			return getPerson(author.substring(SessionContext.PERSON_ID_PREFIX.length()));
		}
		return null;
	}

	private static Person getPerson(String personId) {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		Person person;
		if (interaction != null) {
			Map<String, Person> cache = interaction.get(PERSON_CACHE);
			if (cache.isEmpty()) {
				cache = new HashMap<>();
				interaction.set(PERSON_CACHE, cache);
				person = null;
			} else {
				person = cache.get(personId);
			}
			if (person == null) {
				person = lookupPerson(personId);
				cache.put(personId, person);
			}
		} else {
			person = lookupPerson(personId);
		}
		return person;
	}

	private static Person lookupPerson(String personId) {
		TLID id = IdentifierUtil.fromExternalForm(personId);
		Person currentPerson = (Person) WrapperFactory.getWrapper(null, null, id, Person.OBJECT_NAME);
		if (currentPerson != null) {
			return currentPerson;
		}
		HistoryQuery historyQuery =
			historyQuery(filter(allOf(Person.OBJECT_NAME), eq(true, identifier(), literal(id))));
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Map<?, List<LongRange>> result = kb.search(historyQuery);
		for (Entry<?, List<LongRange>> resultEntry : result.entrySet()) {
			ObjectBranchId personObjectBranchId = (ObjectBranchId) resultEntry.getKey();
			List<LongRange> lifeRanges = resultEntry.getValue();
			LongRange lastLifeRange = lifeRanges.get(lifeRanges.size() - 1);
			ObjectKey historicPerson =
				personObjectBranchId.toObjectKey(HistoryUtils.getRevision(lastLifeRange.getEndValue()));
			return (Person) kb.resolveObjectKey(historicPerson).getWrapper();
		}
		return null;
	}

}

