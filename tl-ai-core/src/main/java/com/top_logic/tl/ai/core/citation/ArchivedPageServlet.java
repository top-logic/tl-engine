/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.SignedInContentServlet;
import com.top_logic.tl.ai.core.execution.model.ArchivedPage;
import com.top_logic.tl.ai.core.scripting.Converters;

/**
 * Shows the copy of a web page that {@link PageArchive} kept when an agent run read it, at
 * {@code /archived-page/{handle}}.
 *
 * <p>
 * The footnote of a web source offers it beside the live page: the page may have changed since the
 * answer was written, or be gone. The copy is what the agent read - the extracted text, not the
 * original layout - under a line saying where it came from and when.
 * </p>
 *
 * <p>
 * <b>Who may look: the account whose run read the page, or an administrator</b>, the rule of
 * {@code /screenshot/{id}} and for the same reason - a page fetched under somebody's browsing rights
 * can be an intranet page. Unknown and foreign handles both answer 404.
 * </p>
 *
 * <p>
 * <b>The text is somebody else's.</b> It is rendered through {@link Converters#markdownToHtmlString},
 * which escapes raw HTML and filters what is left through the sanitizer's allow-list, and the page
 * is served under a content security policy that loads nothing - no script, no image, no style sheet
 * from anywhere - so an archived page can neither act in this application's origin nor tell its
 * author that somebody opened it.
 * </p>
 *
 * @see PageArchive#mayRead(ArchivedPage, Person)
 */
public class ArchivedPageServlet extends SignedInContentServlet {

	/** Nothing is loaded from anywhere; the page's own inline style is the only exception. */
	private static final String CONTENT_SECURITY_POLICY =
		"default-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; form-action 'none'; frame-ancestors 'self'";

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")
		.withZone(ZoneOffset.UTC);

	@Override
	protected String subSessionName() {
		return "tl-ai-archived-page";
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Person account = account(req);
		if (account == null) {
			Logger.info("Refused " + describe(req, null) + ": not authenticated.", ArchivedPageServlet.class);
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, NOT_AUTHENTICATED);
			return;
		}

		String handle = extractId(req.getPathInfo());
		Optional<ArchivedPage> found = handle == null || !PageArchive.Module.INSTANCE.isActive()
			? Optional.<ArchivedPage> empty()
			: PageArchive.getInstance().findByHandle(handle);
		// One answer for "no such copy" and for "not yours": the difference would tell a caller which
		// handles exist.
		if (found.isEmpty() || !PageArchive.mayRead(found.get(), account)) {
			Logger.info("Refused " + describe(req, account) + ": no readable archived page for that handle.",
				ArchivedPageServlet.class);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		ArchivedPage page = found.get();
		resp.setContentType("text/html; charset=utf-8");
		resp.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
		resp.setHeader("X-Content-Type-Options", "nosniff");
		// The copy never changes, but whether this account may see it can: no shared cache keeps it.
		resp.setHeader("Cache-Control", "private, max-age=3600");
		resp.setHeader("Referrer-Policy", "no-referrer");
		PrintWriter out = resp.getWriter();
		out.write(render(page));
		out.flush();
	}

	/** The copy as a page: where it came from and when, then the text. */
	static String render(ArchivedPage page) {
		String url = page.getUrl() == null ? "" : page.getUrl();
		String title = page.getTitle() == null || page.getTitle().isBlank() ? url : page.getTitle();
		Date fetchedAt = page.getFetchedAt();
		String when = fetchedAt == null ? "" : TIME.format(fetchedAt.toInstant());
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\">")
			.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
			.append("<title>").append(TagUtil.encodeXML(title)).append("</title>")
			.append("<style>")
			.append("body{font:15px/1.55 system-ui,sans-serif;max-width:46rem;margin:0 auto;padding:1rem 1.25rem;")
			.append("color:#1f2328;background:#fff}")
			.append(".archived{border:1px solid #d0d7de;border-radius:6px;background:#f6f8fa;padding:.6rem .8rem;")
			.append("margin-bottom:1.5rem;font-size:13px;color:#57606a}")
			.append(".archived a{color:#0969da;word-break:break-all}")
			.append("pre,code{white-space:pre-wrap;word-break:break-word}")
			.append("@media (prefers-color-scheme:dark){body{color:#e6edf3;background:#0d1117}")
			.append(".archived{background:#161b22;border-color:#30363d;color:#8b949e}.archived a{color:#4493f8}}")
			.append("</style></head><body>")
			.append("<div class=\"archived\">Archived copy of ");
		if (ReadingList.isWebAddress(url)) {
			html.append("<a href=\"").append(TagUtil.encodeXMLAttribute(url))
				.append("\" rel=\"noopener noreferrer\" target=\"_blank\">").append(TagUtil.encodeXML(url))
				.append("</a>");
		} else {
			html.append(TagUtil.encodeXML(url));
		}
		html.append(", as an agent read it");
		if (!when.isEmpty()) {
			html.append(" on ").append(TagUtil.encodeXML(when));
		}
		html.append(". The page may have changed since.</div>")
			.append("<h1>").append(TagUtil.encodeXML(title)).append("</h1>")
			.append(Converters.markdownToHtmlString(PageArchive.text(page)))
			.append("</body></html>");
		return html.toString();
	}
}
