/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.screenshots;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.SignedInContentServlet;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;

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
 * {@link SignedInContentServlet}, the "a valid login is required" base, refuses a request
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
public class ScreenshotServlet extends SignedInContentServlet {

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

	@Override
	protected String subSessionName() {
		return "tl-ai-screenshot";
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
}
