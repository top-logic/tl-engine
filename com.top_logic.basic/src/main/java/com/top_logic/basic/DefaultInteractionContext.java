/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.List;

import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.thread.UnboundListener;


/**
 * Default implementation of {@link InteractionContext}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultInteractionContext extends LazyTypedAnnotatable implements InteractionContext {

	private SessionContext _session;

	private SubSessionContext _subSession;
	
	private Object _unboundListeners = InlineList.newInlineList();

	@Override
	public void installSessionContext(SessionContext session) {
		_session = session;
		if (_subSession != null && !StringServices.equals(_session, _subSession.getSessionContext())) {
			_subSession = null;
		}
	}

	@Override
	public SessionContext getSessionContext() {
		return _session;
	}

	@Override
	public void installSubSessionContext(SubSessionContext subSession) {
		_subSession = subSession;
		if (_subSession != null) {
			_session = _subSession.getSessionContext();
		}
	}

	@Override
	public SubSessionContext getSubSessionContext() {
		return _subSession;
	}

	@Override
	public void invalidate() {
		notifyUnbound();
		_subSession = null;
		_session = null;
	}

	private void notifyUnbound() {
		List<UnboundListener> l = InlineList.toList(UnboundListener.class, _unboundListeners);
		for (int index = 0, size = l.size(); index < size; index++) {
			try {
				l.get(index).threadUnbound(this);
			} catch (Throwable ex) {
				Logger.error("Exception in context removal.", ex, DefaultInteractionContext.class);
			}
		}
	}

	@Override
	public void addUnboundListener(UnboundListener l) {
		if (!InlineList.contains(_unboundListeners, l)) {
			_unboundListeners = InlineList.add(UnboundListener.class, _unboundListeners, l);
		}
	}

}

