/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.screenshots;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.TopLogicServlet;

/**
 * Serves a captured {@link Screenshot} at {@code /screenshot/{id}} to the account that captured it
 * (issue #216).
 *
 * <p>
 * {@code browserScreenshot} persists a PNG and hands the model this route's URL; the model writes
 * it into its answer as a Markdown image and the chat renders it inline. The same URL works in the
 * browser's address bar, which is the "look at it" path the screenshot feature was missing.
 * </p>
 *
 * <p>
 * <b>A login is required, and the login is the boundary.</b> Unlike {@code GeneratedHtmlPageServlet}
 * - which answers {@code /html/{id}} anonymously, because a published page is meant to be handed
 * around - a screenshot is a picture of a page somebody drove a browser to on their own account,
 * and nothing about it is meant to be shared by link. This servlet therefore sits on
 * {@link TopLogicServlet}, the framework's "a valid session is required" base, refuses a request
 * that carries no signed-in account, and then asks {@link ScreenshotService#mayRead} - the owner,
 * or an administrator. The unguessable id is defence in depth on top of that, not the rule.
 * </p>
 *
 * <p>
 * <b>Unknown and foreign look the same.</b> Both answer 404, so the route cannot be used to find
 * out which ids exist. A request carrying no login is answered 401 - except for the framework's
 * one-time cookie probe, which redirects a request with no cookie at all to the start page before
 * this servlet ever sees it (see {@link #handleNoSession}).
 * </p>
 *
 * @see ScreenshotService#mayRead(Screenshot, Person)
 * @see ScreenshotService#viewUrl(Screenshot)
 */
public class ScreenshotServlet extends TopLogicServlet {

	/** Answered to a request that carries no login, in place of the image. */
	private static final String NOT_AUTHENTICATED = "Not authenticated.";

	/**
	 * Name under which this route keeps its sub-session in the HTTP session.
	 *
	 * <p>
	 * The UI keys a sub-session by browser window, because each window holds its own control tree.
	 * A content route holds no state at all, so one per session is enough - and one shared name
	 * keeps the session from growing a sub-session per image request.
	 * </p>
	 */
	private static final String SUB_SESSION = "tl-ai-screenshot";

	/**
	 * How long a browser may keep a delivered screenshot.
	 *
	 * <p>
	 * A screenshot is written once and never updated, so its bytes are immutable for the life of
	 * its id. {@code private} because the answer depends on who asked: no shared cache may hold it.
	 * </p>
	 */
	private static final String CACHE_CONTROL = "private, max-age=31536000, immutable";

	/** What a content type that is not an image is served as. */
	private static final String FALLBACK_CONTENT_TYPE = "application/octet-stream";

	/**
	 * Puts the acting account's sub-session on the thread before the route reads anything.
	 *
	 * <p>
	 * A session alone puts neither a sub-session nor a person on the thread:
	 * {@code TLContextManager#inInteraction(TLSessionContext, ...)}, which {@link TopLogicServlet}
	 * calls, installs the <em>session</em> context and nothing else, while
	 * {@code TLContext#getPerson()} reads the sub-session. Without one the knowledge base has no
	 * revision to read at and the lookup below fails outright. {@code ViewServlet} builds a
	 * sub-session per browser window for the same reason; a content route shares one.
	 * </p>
	 */
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		TLSessionContext session = TLContextManager.getSession();
		if (session != null) {
			installSubSession(request, session);
		}

		super.doService(request, response);
	}

	/**
	 * Answers a request that reached the application without any session at all.
	 *
	 * <p>
	 * {@link TopLogicServlet} sends such a request to the start page once to find out whether the
	 * client takes a cookie; it arrives here only when it came back without one. That probe is
	 * {@code private} and {@code final} in the framework, so the first cookie-less request is a
	 * 302 whatever this class says; the second one is answered here, and 401 rather than another
	 * redirect, which an image tag could only fail on.
	 * </p>
	 */
	@Override
	protected void handleNoSession(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		Logger.info("Refused " + describe(request, null) + ": no session.", ScreenshotServlet.class);
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, NOT_AUTHENTICATED);
	}

	/**
	 * Delivers the addressed screenshot, or says no.
	 *
	 * <p>
	 * Both halves of the rule live here rather than in {@link #doService}, because {@code GET} is
	 * the only method this route answers at all - everything else is the container's 405 - and a
	 * check in the one method that serves bytes is a check nothing can be routed around.
	 * </p>
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Person account = account(req);
		if (account == null) {
			Logger.info("Refused " + describe(req, null) + ": not authenticated.", ScreenshotServlet.class);
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, NOT_AUTHENTICATED);
			return;
		}

		String id = extractId(req.getPathInfo());
		Optional<Screenshot> found = id == null
			? Optional.<Screenshot> empty()
			: ScreenshotService.getInstance().findById(id);

		// One answer for "no such screenshot" and for "not yours": the difference between the two
		// would tell a caller which ids exist.
		if (found.isEmpty() || !ScreenshotService.mayRead(found.get(), account)) {
			Logger.info("Refused " + describe(req, account) + ": no readable screenshot for that id.",
				ScreenshotServlet.class);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		BinaryDataSource image = found.get().getImage();
		if (image == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		resp.setContentType(contentType(found.get()));
		long size = image.getSize();
		if (size >= 0) {
			resp.setContentLengthLong(size);
		}
		resp.setHeader("Cache-Control", CACHE_CONTROL);
		// The bytes are a stored upload from the browser tool, so the declared type is the only
		// type: a sniffing browser must not be allowed to find markup in them and run it.
		resp.setHeader("X-Content-Type-Options", "nosniff");
		image.deliverTo(resp.getOutputStream());
	}

	/**
	 * What the stored image is served as.
	 *
	 * <p>
	 * The stored content type decides, but only within {@code image/*}: everything the capture path
	 * writes is a PNG, and a stored value that says something else is served as an opaque download
	 * rather than as a document the browser would render in this application's origin.
	 * </p>
	 */
	private static String contentType(Screenshot screenshot) {
		String declared = screenshot.getContentType();
		if (declared == null || !declared.toLowerCase(Locale.ROOT).startsWith("image/")) {
			return FALLBACK_CONTENT_TYPE;
		}
		return declared;
	}

	/** The first path segment of {@code /screenshot/{id}}, or <code>null</code> when there is none. */
	private static String extractId(String pathInfo) {
		if (pathInfo == null || pathInfo.length() < 2) {
			return null;
		}
		String id = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
		int slash = id.indexOf('/');
		if (slash >= 0) {
			id = id.substring(0, slash);
		}
		return id.isEmpty() ? null : id;
	}

	/**
	 * The account this request acts as, or <code>null</code> when nobody is signed in.
	 *
	 * <p>
	 * Read off the request's own HTTP session, which is where the login is recorded - not off the
	 * thread, which carries a person only once something has put one there. The <em>anonymous</em>
	 * account is nobody: an application that lets visitors in gives them a perfectly valid session,
	 * and that is not a login.
	 * </p>
	 */
	private static Person account(HttpServletRequest request) {
		SessionService sessions = SessionService.getInstance();
		HttpSession httpSession = sessions.getSession(request);
		if (httpSession == null) {
			return null;
		}
		TLSessionContext session = sessions.getSession(httpSession);
		Person account = session == null ? null : session.getOriginalUser();
		return account == null || PersonManager.getManager().isAnonymous(account) ? null : account;
	}

	/** Who asked for what, for the log. Never the session id - a reader could take the session over. */
	private static String describe(HttpServletRequest request, Person account) {
		String who = account == null ? "an unauthenticated request" : "account '" + account.getName() + "'";
		return who + " " + request.getMethod() + " " + request.getRequestURI();
	}

	/** Installs the session's own sub-session on this thread, creating it on first use. */
	private static void installSubSession(HttpServletRequest request, TLSessionContext session) {
		TLSubSessionContext subSession = session.getSubSession(SUB_SESSION);
		if (subSession == null) {
			subSession = (TLSubSessionContext) ThreadContextManager.getManager().newSubSessionContext();
			subSession.setSessionContext(session);
			subSession = session.setIfAbsent(SUB_SESSION, subSession);
		}
		subSession.setPerson(session.getOriginalUser());

		// Every request reads the newest stand: a screenshot captured a moment ago must be
		// deliverable, and a sub-session that outlives the request would otherwise keep the
		// revision of its first one.
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());

		DefaultDisplayContext.getDisplayContext(request).installSubSessionContext(subSession);
	}

}
