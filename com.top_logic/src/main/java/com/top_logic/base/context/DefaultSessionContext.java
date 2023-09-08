/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.context;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;

import com.top_logic.basic.Logger;
import com.top_logic.basic.SimpleSessionContext;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.util.TLContext;

/**
 * Standard implementation of {@link TLSessionContext}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultSessionContext extends SimpleSessionContext implements TLSessionContext {

	private final ConcurrentHashMap<String, TLSubSessionContext> _subSessions =
		new ConcurrentHashMap<>();

	private final ContentHandlersRegistry _contentHandler;

	private Person _originalUser;

	/**
	 * Creates a new {@link DefaultSessionContext}.
	 * 
	 * @param handlersRegistry
	 *        Value of {@link #getHandlersRegistry()}
	 */
	protected DefaultSessionContext(ContentHandlersRegistry handlersRegistry) {
		_contentHandler = handlersRegistry;
	}

	@Override
	public ContentHandlersRegistry getHandlersRegistry() {
		return _contentHandler;
	}

	@Override
	public TLSubSessionContext getSubSession(String id) {
		return _subSessions.get(id);
	}

	@Override
	public Map<String, TLSubSessionContext> getSubSessions() {
		return map(_subSessions);
	}

	@Override
	public TLSubSessionContext setIfAbsent(String id, TLSubSessionContext newContext) {
		return MapUtil.putIfAbsent(_subSessions, id, newContext);
	}

	@Override
	protected void afterValueUnboundHook(HttpSessionBindingEvent event) {
		super.afterValueUnboundHook(event);
		Iterator<TLSubSessionContext> subSessions = _subSessions.values().iterator();
		while (subSessions.hasNext()) {
			TLSubSessionContext removedSubSession = subSessions.next();
			subSessions.remove();
			handleSubSessionRemoved(removedSubSession);
		}
	}

	private void handleSubSessionRemoved(TLSubSessionContext removedSubSession) {
		((TLContext) removedSubSession).informUnboundListeners();
	}

	@Override
	public void removeSubSession(String id) {
		TLSubSessionContext removedSubSession = _subSessions.get(id);
		if (removedSubSession != null) {
			_subSessions.remove(id);
			handleSubSessionRemoved(removedSubSession);
		}
	}

	/**
	 * Creates a new {@link DefaultSessionContext}.
	 */
	public static DefaultSessionContext newDefaultSessionContext() {
		return new DefaultSessionContext(new ContentHandlersRegistry());
	}

	/**
	 * Creates a new {@link DefaultSessionContext} for the given {@link HttpSession}.
	 */
	public static DefaultSessionContext newDefaultSessionContext(final HttpSession session) {
		ContentHandlersRegistry registry = new ContentHandlersRegistry() {
			@Override
			protected void notifyUnused() {
				Logger.info("Last subsession terminated, terminating session.", ContentHandlersRegistry.class);
				session.invalidate();

				super.notifyUnused();
			}
		};
		return new DefaultSessionContext(registry);
	}

	@Override
	public Person getOriginalUser() {
		return _originalUser;
	}

	/**
	 * @see #getOriginalUser()
	 */
	public void setOriginalUser(Person user) {
		_originalUser = user;
		setOriginalContextId(TLSessionContext.contextId(user));
	}

}

