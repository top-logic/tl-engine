/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.context;

import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.ContentHandlersRegistry;

/**
 * Abstraction of a user session.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLSessionContext extends SessionContext {

	/**
	 * The registry for all top-level {@link ContentHandler}s in this session.
	 */
	ContentHandlersRegistry getHandlersRegistry();

	/**
	 * Returns the {@link TLSubSessionContext} formerly registered under the given Id.
	 */
	TLSubSessionContext getSubSession(String id);

	/**
	 * Returns a copy of the {@link TLSubSessionContext} {@link Map}.
	 * 
	 * @return Never null. A new mutable and resizable {@link Map} copy.
	 */
	Map<String, TLSubSessionContext> getSubSessions();

	/**
	 * Sets the given {@link TLSubSessionContext} if no one is formerly set for the given Id. If
	 * there is one it is not changed.
	 * 
	 * @return The sub session which is finally present under the given id.
	 */
	TLSubSessionContext setIfAbsent(String id, TLSubSessionContext newContext);

	/**
	 * Removes the {@link TLSubSessionContext}, formerly registered under the given Id.
	 */
	void removeSubSession(String id);

	/**
	 * The {@link Person} as authenticated when this session was created.
	 */
	Person getOriginalUser();

	/**
	 * The {@link SubSessionContext#getContextId()} for a given real account, or <code>null</code>
	 * if none was given.
	 */
	static String contextId(Person person) {
		if (person == null) {
			return null;
		} else {
			TLID internalID = KBUtils.getWrappedObjectName(person);
			return SessionContext.PERSON_ID_PREFIX + IdentifierUtil.toExternalForm(internalID);
		}
	}

}
