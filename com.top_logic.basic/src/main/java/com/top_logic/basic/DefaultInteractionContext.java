/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.annotation.FrameworkInternal;
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

	private final ServletContext _servletContext;

	private final HttpServletRequest _request;

	private HttpServletResponse _response;

	private boolean _invalid;

	public DefaultInteractionContext(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response) {
		this._servletContext = servletContext;
		this._request = request;
		this._response = response;
	}

	@Override
	public HttpServletRequest asRequest() {
		checkNotInvalid();
		return _request;
	}

	@Override
	public HttpServletResponse asResponse() {
		checkNotInvalid();
		return _response;
	}

	@Override
	public ServletContext asServletContext() {
		checkNotInvalid();
		return _servletContext;
	}

	protected final void checkNotInvalid() {
		if (_invalid) {
			throw new IllegalStateException(
				"This InteractionContext is invalid since the corresponding request is already responded");
		}
	}

	/**
	 * Updates the underlying {@link HttpServletResponse}.
	 * 
	 * @param response
	 *        The new response to use.
	 */
	@FrameworkInternal
	public void setResponse(HttpServletResponse response) {
		this._response = response;
	}

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
		this._invalid = true;
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

