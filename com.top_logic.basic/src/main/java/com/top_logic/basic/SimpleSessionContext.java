/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.List;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.top_logic.basic.col.ConcurrentTypedAnnotationContainer;
import com.top_logic.basic.col.CopyOnChangeListProvider;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Abstract implementation of {@link SessionContext}.
 * 
 * <p>
 * This {@link SessionContext} implements also {@link HttpSessionBindingListener}. If a
 * {@link HttpSessionBindingListener} method is called on this listener, the event is dipatched to
 * the registered listeners.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleSessionContext extends ConcurrentTypedAnnotationContainer implements SessionContext,
		HttpSessionBindingListener {

	private final CopyOnChangeListProvider<HttpSessionBindingListener> _listeners =
		new CopyOnChangeListProvider<>();

	private Object _id = new Object();

	private String _originalContextId;

	@Override
	public Object getId() {
		return _id;
	}

	@Override
	public String getOriginalContextId() {
		return _originalContextId;
	}

	@Override
	public void setOriginalContextId(String contextId) {
		_originalContextId = contextId;
	}

	/**
	 * Returns an unmodifiable view to the {@link HttpSessionBindingListener} associated to this
	 * {@link SessionContext}.
	 */
	protected List<HttpSessionBindingListener> getListeners() {
		return _listeners.get();
	}

	@Override
	public void addHttpSessionBindingListener(HttpSessionBindingListener listener) {
		_listeners.addIfAbsent(listener);
	}

	@Override
	public void removeHttpSessionBindingListener(HttpSessionBindingListener listener) {
		_listeners.remove(listener);
	}

	/**
	 * The {@link HttpSessionBindingListener} is added as attribute to the session. When doing this,
	 * it is registered as listener.
	 * 
	 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		beforeValueBoundHook(event);
		List<HttpSessionBindingListener> listeners = getListeners();
		int size = listeners.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				notifyValueBound(listeners.get(i), event);
			}
		}
		afterValueBoundHook(event);
	}

	private void notifyValueBound(HttpSessionBindingListener listener, HttpSessionBindingEvent event) {
		try {
			listener.valueBound(event);
		} catch (Exception ex) {
			Logger.error("Listener failed on notifyBound. Listener: '" + listener + "', Event: '" + event
				+ "', Cause: " + ex.getMessage(), ex, SimpleSessionContext.class);
		}
	}

	/**
	 * Hook method.
	 * 
	 * <p>
	 * Method is called before any listener is informed about an
	 * {@link #valueBound(HttpSessionBindingEvent)}.
	 * </p>
	 * 
	 * @param event
	 *        event received in {@link #valueBound(HttpSessionBindingEvent)}
	 * 
	 * @see #valueBound(HttpSessionBindingEvent)
	 */
	protected void afterValueBoundHook(HttpSessionBindingEvent event) {
		// hook method
	}

	/**
	 * Hook method.
	 * 
	 * <p>
	 * Method is called after all listeners are informed about an
	 * {@link #valueBound(HttpSessionBindingEvent)}.
	 * </p>
	 * 
	 * @param event
	 *        event received in {@link #valueBound(HttpSessionBindingEvent)}
	 * 
	 * @see #valueBound(HttpSessionBindingEvent)
	 */
	protected void beforeValueBoundHook(HttpSessionBindingEvent event) {
		// hook method
	}

	/**
	 * The {@link HttpSessionBindingListener} is added as attribute to the session. When doing this,
	 * it is registered as listener.
	 * 
	 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	@Override
	public void valueUnbound(final HttpSessionBindingEvent event) {
		if (ThreadContextManager.getSubSession() == null) {
			ThreadContext subsession = ThreadContextManager.getManager().newSubSessionContext();
			subsession.setSessionContext(this);
			subsession.setContextId(getOriginalContextId());
			ThreadContextManager.inContext(subsession, new InContext() {

				@Override
				public void inContext() {
					internalValueUnbound(event);
				}
			});
		} else {
			internalValueUnbound(event);
		}
	}

	void internalValueUnbound(final HttpSessionBindingEvent event) {
		beforeValueUnboundHook(event);
		final List<HttpSessionBindingListener> listeners = getListeners();
		final int size = listeners.size();
		if (size > 0) {
			notifyListeners(event, listeners, size);
		}
		afterValueUnboundHook(event);

		// context has been removed from session. Remove all known listeners because they are not
		// longer called.
		_listeners.clear();
	}

	void notifyListeners(final HttpSessionBindingEvent event, final List<HttpSessionBindingListener> listeners,
			final int size) {
		for (int i = size - 1; i >= 0; i--) {
			notifyValueUnbound(listeners.get(i), event);
		}
	}

	private void notifyValueUnbound(HttpSessionBindingListener listener, HttpSessionBindingEvent event) {
		try {
			listener.valueUnbound(event);
		} catch (Exception ex) {
			Logger.error("Listener failed on notifyUnbound. Listener: '" + listener + "', Event: '" + event
				+ "', Cause: " + ex.getMessage(), ex, SimpleSessionContext.class);
		}
	}

	/**
	 * Hook method.
	 * 
	 * <p>
	 * Method is called before any listener is informed about an
	 * {@link #valueUnbound(HttpSessionBindingEvent)}.
	 * </p>
	 * 
	 * @param event
	 *        event received in {@link #valueUnbound(HttpSessionBindingEvent)}
	 * 
	 * @see #valueUnbound(HttpSessionBindingEvent)
	 */
	protected void afterValueUnboundHook(HttpSessionBindingEvent event) {
		// hook method
	}

	/**
	 * Hook method.
	 * 
	 * <p>
	 * Method is called after all listeners are informed about an
	 * {@link #valueUnbound(HttpSessionBindingEvent)}.
	 * </p>
	 * 
	 * @param event
	 *        event received in {@link #valueUnbound(HttpSessionBindingEvent)}
	 * 
	 * @see #valueUnbound(HttpSessionBindingEvent)
	 */
	protected void beforeValueUnboundHook(HttpSessionBindingEvent event) {
		// hook method
	}

}

