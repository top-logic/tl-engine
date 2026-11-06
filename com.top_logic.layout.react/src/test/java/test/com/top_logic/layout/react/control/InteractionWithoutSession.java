/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.lang.reflect.Proxy;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.basic.component.ControlSupport;

/**
 * The display context of an interaction whose request belongs to no session, as much of an
 * interaction as a replayed command needs to resolve recorded identities.
 */
public final class InteractionWithoutSession extends DummyDisplayContext {

	private final HttpServletRequest _request = (HttpServletRequest) Proxy.newProxyInstance(
		HttpServletRequest.class.getClassLoader(), new Class<?>[] { HttpServletRequest.class },
		(proxy, method, args) -> {
			Class<?> type = method.getReturnType();
			if (type == boolean.class) {
				return Boolean.FALSE;
			}
			if (type == int.class) {
				return Integer.valueOf(0);
			}
			if (type == long.class) {
				return Long.valueOf(0);
			}
			return null;
		});

	/**
	 * Creates an {@link InteractionWithoutSession}.
	 */
	public InteractionWithoutSession() {
		initScope(new ControlSupport(null));
	}

	/**
	 * An {@link InteractionWithoutSession} that belongs to a window of a fresh session nevertheless
	 * - as an interaction of the application does -, for code asking the session context, such as
	 * the naming of an object, which looks for script variables naming it in the session.
	 *
	 * <p>
	 * The request still belongs to no HTTP session. Requires the {@link ThreadContextManager}.
	 * </p>
	 */
	public static InteractionWithoutSession inSession() {
		InteractionWithoutSession result = new InteractionWithoutSession();
		ThreadContextManager manager = ThreadContextManager.getManager();
		ThreadContext window = manager.newSubSessionContext();
		window.setSessionContext(manager.newSessionContext());
		result.installSessionContext(window.getSessionContext());
		result.installSubSessionContext(window);
		return result;
	}

	@Override
	public HttpServletRequest asRequest() {
		return _request;
	}

}
