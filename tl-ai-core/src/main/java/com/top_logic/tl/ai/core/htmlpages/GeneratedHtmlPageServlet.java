/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.htmlpages;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.tl.ai.core.htmlpages.model.GeneratedHtmlPage;
import com.top_logic.util.NoContextServlet;

/**
 * Serves persisted {@link GeneratedHtmlPage}s at {@code /html/{id}} anonymously.
 *
 * <p>
 * The unguessable base64url id acts as the capability — anyone with the link can open the page.
 * Extends {@link NoContextServlet} so a {@code DisplayContext} / {@code SessionContext} are
 * installed (KB lookups require an {@code InteractionContext}) without requiring an authenticated
 * TopLogic session.
 * </p>
 *
 * <p>
 * The response carries a {@code Content-Security-Policy: sandbox ...} header so the LLM-authored
 * HTML runs in an opaque origin: scripts work for interactivity, but the document cannot read the
 * viewer's session cookies or impersonate them against the application's APIs.
 * </p>
 *
 * <p>
 * The {@code /html/*} mapping of this servlet also covers paths that belong to other modules'
 * static web resources, which a servlet mapping shadows. {@link PdfJsResourceServlet} carves the
 * one known collision, {@code /html/pdfjs/*}, back out; anything similar has to be carved out the
 * same way.
 * </p>
 */
public class GeneratedHtmlPageServlet extends NoContextServlet {

	/**
	 * Isolates LLM-authored HTML from the app origin: keeps interactivity, drops same-origin
	 * privileges (no app cookies, no localStorage, no credentialed fetches to /chat /api).
	 */
	private static final String SANDBOX_CSP =
		"sandbox allow-scripts allow-forms allow-modals allow-downloads";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = extractId(req.getPathInfo());
		if (id == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		Optional<GeneratedHtmlPage> found = GeneratedHtmlPageService.Module.INSTANCE
			.getImplementationInstance().findById(id);
		if (found.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		BinaryDataSource content = found.get().getHtmlContent();
		if (content == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		resp.setContentType(content.getContentType());
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Content-Security-Policy", SANDBOX_CSP);
		content.deliverTo(resp.getOutputStream());
	}

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
}
