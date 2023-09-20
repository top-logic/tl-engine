/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.basic.AbstractDisplayContext;
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

	public static void inPersonContext(Person p, InContext job) {
		getManager().inPersonContextInternal(p, job);
	}

	protected void inPersonContextInternal(Person p, InContext job) {
		TLInteractionContext interaction = getInteractionInternal();
		if (interaction == null) {
			interaction = newInteractionInternal();
			registerSubSessionInSessionForInteraction(interaction);
			setInteraction(interaction);
			try {
				TLSessionContext session = interaction.getSessionContext();
				TLSubSessionContext subsession = interaction.getSubSessionContext();
				try {
					subsession.setPerson(p);
					job.inContext();
				} finally {
					sessionUnbound(session);
				}
			} finally {
				removeInteraction();
			}
		} else {
			job.inContext();
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
	 * Executes the given job in context of the root user.
	 */
	public static void inRootPersonContext(InContext job) {
		getManager().inRootPersonContextInternal(job);
	}

	private void inRootPersonContextInternal(InContext job) {
		TLInteractionContext interaction = getInteractionInternal();
		if (interaction == null) {
			interaction = newInteractionInternal();
			registerSubSessionInSessionForInteraction(interaction);
			setInteraction(interaction);
			try {
				TLSessionContext session = interaction.getSessionContext();
				TLSubSessionContext subsession = interaction.getSubSessionContext();
				try {
					// Do not fetch person before subsession is installed.
					Person root = PersonManager.getManager().getRoot();
					subsession.setPerson(root);
					job.inContext();
				} finally {
					sessionUnbound(session);
				}
			} finally {
				removeInteraction();
			}
		} else {
			Person root = PersonManager.getManager().getRoot();
			TLSubSessionContext subsession = interaction.getSubSessionContext();
			Person currentPerson = subsession.getPerson();
			boolean useContextID = currentPerson == null;
			String formerContextId = null;
			if (useContextID) {
				formerContextId = subsession.getContextId();
			}
			subsession.setPerson(root);
			try {
				job.inContext();
			} finally {
				subsession.setPerson(currentPerson);
				if (useContextID) {
					subsession.setContextId(formerContextId);
				}
			}
		}
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
				DefaultDisplayContext.teardownDisplayContext(req, (AbstractDisplayContext) displayContext);
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
				DefaultDisplayContext.teardownDisplayContext(req, (AbstractDisplayContext) displayContext);
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

