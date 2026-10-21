/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Random;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.context.DefaultSessionContext;
import com.top_logic.base.context.TLInteractionContext;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.basic.DefaultDisplayContext;

/**
 * Factory class for {@link ThreadContext} in <i>TopLogic</i>.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLContextManager extends ThreadContextManager {

	/**
	 * Creates a new {@link TLContextManager}.
	 */
	public TLContextManager(InstantiationContext context, Config configuration) {
		super(context, configuration);
	}

	public static TLContextManager getManager() {
		return (TLContextManager) ThreadContextManager.getManager();
	}

	@Override
	protected ThreadContext internalNewSubSessionContext() {
		return new TLContext();
	}

	@Override
	public InteractionContext internalNewInteraction(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response) {
		return new DefaultDisplayContext(servletContext, request, response);
	}

	@Override
	public SessionContext internalNewSessionContext() {
		return DefaultSessionContext.newDefaultSessionContext();
	}

	@Override
	public SessionContext internalNewSessionContext(HttpSession session) {
		return DefaultSessionContext.newDefaultSessionContext(session);
	}

	/**
	 * Runs the given job in context of the given person.
	 * 
	 * @see #inPersonContext(Person, ComputationEx2)
	 */
	public static void inPersonContext(Person p, InContext job) {
		getManager().inPersonContextInternal(p, job);
	}

	/**
	 * Implementation of {@link #inPersonContext(Person, InContext)}.
	 */
	protected void inPersonContextInternal(Person p, InContext job) {
		inPersonContextInternal(p, () -> {
			job.inContext();
			return null;
		});
	}

	/**
	 * Computes the given job in context of the given person.
	 * 
	 * <p>
	 * The job runs with the given {@link Person} and with
	 * {@link ThreadContext#withSecurityChecks(boolean, ComputationEx2) security checks}, so that access is decided
	 * for the given person. The person context is established in any case: If no interaction is
	 * active, a new interaction is created for the job. Otherwise, the {@link TLSubSessionContext}
	 * of the current interaction gets the given person for the job, keeping its session. After the
	 * job, the previous state is restored.
	 * </p>
	 * 
	 * @param p
	 *        The person to run the job for, <code>null</code> for a context without a person, in
	 *        which every access is denied.
	 * @param job
	 *        Callback to execute.
	 * @return The result of the given job.
	 * 
	 * @see ThreadContext#inSystemContext(Class, ComputationEx2)
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T inPersonContext(Person p,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		return getManager().inPersonContextInternal(p, job);
	}

	/**
	 * Implementation of {@link #inPersonContext(Person, ComputationEx2)}.
	 */
	protected <T, E1 extends Throwable, E2 extends Throwable> T inPersonContextInternal(Person p,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		return this.<T, E1, E2> inIdentity(subSession -> ((TLSubSessionContext) subSession).setPerson(p), true, job);
	}

	/**
	 * Records the person, context id, locale, time zone and personal configuration of the given
	 * sub-session.
	 */
	@Override
	protected Runnable captureIdentity(ThreadContext subSession) {
		if (subSession instanceof TLContext context) {
			return context.captureIdentity();
		}
		return super.captureIdentity(subSession);
	}

	/**
	 * Removes the person of the given sub-session before identifying it by the given context id.
	 */
	@Override
	protected void applySystemIdentity(ThreadContext subSession, String contextId) {
		if (subSession instanceof TLContext context) {
			context.setSystemIdentity(contextId);
		} else {
			super.applySystemIdentity(subSession, contextId);
		}
	}

	protected static TLInteractionContext newInteraction() {
		return getManager().newInteractionInternal();
	}

	@Override
	protected TLInteractionContext newInteractionInternal() {
		return (TLInteractionContext) super.newInteractionInternal();
	}

	/**
	 * {@link ThreadContextManager#getSubSession()}.
	 */
	public static TLSubSessionContext getSubSession() {
		return getManager().getSubSessionInternal();
	}

	/**
	 * @see #getSubSession()
	 */
	@Override
	protected TLSubSessionContext getSubSessionInternal() {
		return (TLSubSessionContext) super.getSubSessionInternal();
	}

	/**
	 * {@link ThreadContextManager#getSession()}.
	 */
	public static TLSessionContext getSession() {
		return getManager().getSessionInternal();
	}

	/**
	 * @see #getSessionInternal()
	 */
	@Override
	protected TLSessionContext getSessionInternal() {
		return (TLSessionContext) super.getSessionInternal();
	}

	/**
	 * {@link ThreadContextManager#getInteraction()}.
	 */
	public static TLInteractionContext getInteraction() {
		return getManager().getInteractionInternal();
	}

	/**
	 * @see #getInteraction()
	 */
	@Override
	protected TLInteractionContext getInteractionInternal() {
		return (TLInteractionContext) super.getInteractionInternal();
	}

	public static void inInteraction(TLSessionContext sessionContext, ServletContext servlet,
			HttpServletRequest req, HttpServletResponse resp, InContext job) {
		getManager().inInteractionInternal(sessionContext, servlet, req, resp, job);
	}

	protected void inInteractionInternal(TLSessionContext sessionContext, ServletContext servlet,
			HttpServletRequest req,
			HttpServletResponse resp, InContext job) {
		boolean existingDisplayContext = DefaultDisplayContext.hasDisplayContext(req);
	
		TLSessionContext formerSessionContext;
		DisplayContext displayContext;
		if (existingDisplayContext) {
			displayContext = DefaultDisplayContext.getDisplayContext(req);
			formerSessionContext = displayContext.getSessionContext();
			if (formerSessionContext == sessionContext) {
				job.inContext();
				return;
			}
		} else {
			TLSubSessionContext subSession = getSubSessionInternal();
			displayContext = DefaultDisplayContext.setupDisplayContext(subSession, servlet, req, resp);
			formerSessionContext = null;
		}
		displayContext.installSessionContext(sessionContext);
		try {
			job.inContext();
		} finally {
			if (!existingDisplayContext) {
				DefaultDisplayContext.teardownDisplayContext(req);
			} else {
				displayContext.installSessionContext(formerSessionContext);
			}
	
		}
	}

	public static void inInteraction(TLSubSessionContext sessionContext, ServletContext servlet,
			HttpServletRequest req, HttpServletResponse resp, InContext job) {
		getManager().inInteractionInternal(sessionContext, servlet, req, resp, job);
	}

	protected void inInteractionInternal(TLSubSessionContext sessionContext, ServletContext servlet,
			HttpServletRequest req, HttpServletResponse resp, InContext job) {
		boolean existingDisplayContext = DefaultDisplayContext.hasDisplayContext(req);
	
		TLSubSessionContext formerSessionContext;
		DisplayContext displayContext;
		if (existingDisplayContext) {
			displayContext = DefaultDisplayContext.getDisplayContext(req);
			formerSessionContext = displayContext.getSubSessionContext();
			if (formerSessionContext == sessionContext) {
				job.inContext();
				return;
			}
		} else {
			TLSubSessionContext subSession = getSubSessionInternal();
			displayContext = DefaultDisplayContext.setupDisplayContext(subSession, servlet, req, resp);
			formerSessionContext = null;
		}
		displayContext.installSubSessionContext(sessionContext);
		try {
			job.inContext();
		} finally {
			if (!existingDisplayContext) {
				DefaultDisplayContext.teardownDisplayContext(req);
			} else {
				displayContext.installSubSessionContext(formerSessionContext);
			}
	
		}
	}

	/**
	 * The same as {@link Person#getName() name} of the {@link TLSubSessionContext#getPerson()
	 * person} of the given session.
	 */
	public static String getCurrentUserName(TLSubSessionContext sessionContext) {
		Person currentPerson = sessionContext.getPerson();
		if (currentPerson == null) {
			return null;
		}
		return currentPerson.getName();
	}

	/**
	 * Initialises {@link TLSubSessionContext#getLayoutContext() layout context} of given sub
	 * session with the given layout.
	 * 
	 * @param subSession
	 *        A {@link TLSubSessionContext} created with the {@link TLContextManager}.
	 * @param layoutContext
	 *        The value of {@link TLSubSessionContext#getLayoutContext()}.
	 */
	public static void initLayoutContext(TLSubSessionContext subSession, LayoutContext layoutContext) {
		((TLContext) subSession).initLayoutContext(layoutContext);
	}

	/**
	 * Registers the {@link TLSubSessionContext} in the {@link TLSessionContext} under a random id.
	 * 
	 * The connection is released when the interaction is unbound.
	 */
	@Override
	public void registerSubSessionInSessionForInteraction(InteractionContext interaction) {
		TLSubSessionContext subSession = (TLSubSessionContext) interaction.getSubSessionContext();
		TLSessionContext session = (TLSessionContext) interaction.getSessionContext();

		Person user = session.getOriginalUser();
		if (user != null) {
			// Take initial locale from user. This is relevant to display the change password page
			// in the user's locale even after the first password change attempt has failed.
			subSession.setCurrentLocale(user.getLocale());
		}

		Random rand = new Random();
		String id;
		while (true) {
			id = Long.toString(rand.nextLong(), Character.MAX_RADIX);
			TLSubSessionContext storedSubSession = session.setIfAbsent(id, subSession);
			if (storedSubSession == subSession) {
				break;
			}
		}
		String subSessionID = id;
		interaction.addUnboundListener(context -> session.removeSubSession(subSessionID));
	}

}

