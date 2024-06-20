/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.jsp.PageContext;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.internal.WindowId;
import com.top_logic.layout.internal.WindowRegistry;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.util.TLContextManager;

/**
 * The class {@link ContentHandlersRegistry} is a registry to register top level
 * {@link ContentHandler}.
 * 
 * It is an {@link AbstractCompositeContentHandler} for technical reason, but does not handle any content.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ContentHandlersRegistry extends WindowRegistry<SubsessionHandler> {

	private LayoutConfig _config = ApplicationConfig.getInstance().getConfig(LayoutConfig.class);

	/**
	 * Request parameter to specify an alternative layout.
	 */
	public static final String LAYOUT_PARAMETER = "layout";

	/**
	 * Request parameter to specify an alternative language.
	 */
	public static final String LANG_PARAMETER = "lang";

	/**
	 * Request parameter to specify an alternative timezone.
	 */
	public static final String ZONE_PARAMETER = "zone";

	/**
	 * Creates a {@link ContentHandlersRegistry}.
	 */
	public ContentHandlersRegistry() {
		super(SubsessionHandler.class);
	}

	private boolean checkSubsessionDeny(DisplayContext context) throws IOException {
		boolean deny = !allowSubsession();
		if (deny) {
			context.asResponse().sendRedirect(SubsessionHandler.getSubsessionLocation(context));
		}
		return deny;
	}

	/**
	 * Whether a user can spawn a new subsession.
	 */
	@CalledFromJSP
	public static boolean allowSubsession(PageContext pageContext) {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
		ContentHandlersRegistry handlerRegistry = displayContext.getSessionContext().getHandlersRegistry();
		return handlerRegistry.allowSubsession();
	}

	private boolean allowSubsession() {
		int subsessionCount = getWindowCount();
		LayoutConfig config = ApplicationConfig.getInstance().getConfig(LayoutConfig.class);
		int maxSubsessionCount = config.getMaxSubsessionCount();
		return maxSubsessionCount <= 0 || subsessionCount < maxSubsessionCount;
	}

	@Override
	public void registerContentHandler(String id, ContentHandler handler) {
		throw new UnsupportedOperationException("Must not register additional handlers in the subsession registry.");
	}

	@Override
	public boolean deregisterContentHandler(ContentHandler handler) {
		final boolean result = super.deregisterContentHandler(handler);
		checkUsed();
		return result;
	}

	private void checkUsed() {
		if (!isInUse()) {
			notifyUnused();
		}
	}

	/**
	 * Hook method called after the last registered content handler has been removed and no more
	 * logins are pending.
	 */
	protected void notifyUnused() {
		// Hook for subclasses.
	}

	private boolean isInUse() {
		return hasWindows();
	}

	@Override
	protected void handleWindowNameCreation(DisplayContext context) throws IOException {
		if (checkSubsessionDeny(context)) {
			return;
		}

		super.handleWindowNameCreation(context);
	}

	@Override
	protected String getPreferredRawWindowName() {
		return StringServices.nonEmpty(_config.getStableWindowName());
	}

	@Override
	protected void redirectToWindowContent(DisplayContext context, WindowId windowId) throws IOException {
		if (getContentHandler(windowId.getWindowName()) == null) {
			if (checkSubsessionDeny(context)) {
				return;
			}
		}

		super.redirectToWindowContent(context, windowId);
	}

	@Override
	protected void dispatch(DisplayContext context, SubsessionHandler rootHandler, WindowId windowId,
			URLParser url) throws IOException {
		TLSubSessionContext subSession;
		if (rootHandler == null) {
			rootHandler = startLogin(context, windowId);
			// subsession is created in startLogin().
			subSession = TLContextManager.getSession().getSubSession(windowId.getWindowName());
			TLContextManager.initLayoutContext(subSession, rootHandler);
		} else {
			subSession = TLContextManager.getSession().getSubSession(windowId.getWindowName());
		}

		context.installSubSessionContext(subSession);

		super.dispatch(context, rootHandler, windowId, url);
	}

	private SubsessionHandler startLogin(DisplayContext context, WindowId subsessionId) throws IOException {
		String layout = context.asRequest().getParameter(ContentHandlersRegistry.LAYOUT_PARAMETER);
		if (StringServices.isEmpty(layout)) {
			List<String> layouts = _config.getLayouts();
			if (layouts.isEmpty()) {
				throw new IOException("No layout found to instantiate.");
			}
			layout = layouts.get(0);
		} else {
			if (!LayoutUtils.isApplicationRootLayout(layout)) {
				List<String> layouts = _config.getLayouts();
				Logger.warn(
					"Tried to use a non-root layout '" + layout + "' for login available root layouts: " + layouts,
					ContentHandlersRegistry.class);
				layout = layouts.get(0);
			}
		}
		final TLSessionContext session = TLContextManager.getSession();

		Locale locale = locale(context.asRequest().getParameter(ContentHandlersRegistry.LANG_PARAMETER));
		TimeZone zone =
			zone(StringServices.nonEmpty(context.asRequest().getParameter(ContentHandlersRegistry.ZONE_PARAMETER)));

		return startLogin(session, layout, subsessionId, session.getOriginalUser(), locale, zone);
	}

	private TimeZone zone(String zone) {
		if (StringServices.isEmpty(zone)) {
			return null;
		}
		return TimeZone.getTimeZone(zone);
	}

	private Locale locale(String lang) {
		if (StringServices.isEmpty(lang)) {
			return null;
		}
		return new Locale(lang);
	}

	/**
	 * Creates a new {@link SubsessionHandler} to be "displayed" in a new browser window.
	 * 
	 * @param session
	 *        The {@link SessionContext} in which to create a new {@link SubSessionContext}.
	 * @param rootLayoutName
	 *        The layout name of the root layout to load in the new window.
	 * @param subsessionId
	 *        The {@link WindowId} that identifiers the browser window. The client must append the
	 *        {@link WindowId#getEncodedForm()} to the request URL.
	 * @param person
	 *        The account on behalf of which to create the new subsession.
	 * @param zone
	 *        The {@link TimeZone} to create the subsession for, or <code>null</code>, if the user's
	 *        default timezone should be used.
	 * @param locale
	 *        The {@link Locale} to use for the new subsession, or <code>null</code>, if the user's
	 *        default locale should be used.
	 * @return The newly created subsession. The first request to the new subsession must occur
	 *         within the next 30 seconds. Otherwise, the subsession is automatically destroyed.
	 */
	public SubsessionHandler startLogin(TLSessionContext session, String rootLayoutName, final WindowId subsessionId,
			Person person, Locale locale, TimeZone zone) {
		final String handlerId = subsessionId.getWindowName();
		Runnable cancelTimeout = new Runnable() {
			@Override
			public void run() {
				Logger.info("Canceled login request after timeout.", ContentHandlersRegistry.class);
				loginCanceled(handlerId, session);
			}
		};
		ScheduledFuture<?> cancelTimer =
			SchedulerService.getInstance().schedule(cancelTimeout, 30, TimeUnit.SECONDS);

		SubsessionHandler login = new SubsessionHandler(this, subsessionId, rootLayoutName, cancelTimer);

		SubsessionHandler existing = registerIfAbsent(handlerId, login);
		boolean success = existing == null;
		SubsessionHandler result = success ? login : existing;
		if (!success) {
			cancelTimer.cancel(false);
		} else {
			TLSubSessionContext subSession = session.getSubSession(handlerId);
			if (subSession == null) {
				subSession = (TLSubSessionContext) ThreadContextManager.getManager().newSubSessionContext();
				subSession.setSessionContext(session);
				subSession = session.setIfAbsent(handlerId, subSession);
				subSession.setPerson(person);
				if (locale != null) {
					subSession.setCurrentLocale(locale);
				}
				if (zone != null) {
					subSession.setCurrentTimeZone(zone);
				}

				KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
				HistoryManager hm = kb.getHistoryManager();
				UpdateChainLink lastKBRevision = ((UpdateChainView) kb.getUpdateChain()).current();
				subSession.updateSessionRevision(hm, lastKBRevision);
			}
		}

		return result;
	}

	void loginCanceled(String windowName, TLSessionContext session) {
		session.removeSubSession(windowName);
		SubsessionHandler login = internalDeregisterWindow(windowName);
		if (login != null) {
			checkUsed();
		}
	}

}
