/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.demo.react.model.tickets.DemoTicketsFactory;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.ObjectFunctions;

/**
 * Permalink of a ticket of the demo: {@code /servlet/ticket-link?nr=<number>}.
 *
 * <p>
 * The number is the identifier a ticket carries in the URL of the ticket list
 * ({@code tickets.view.xml}). A link naming an existing ticket is redirected to the page of that
 * ticket. Any other link is answered with the page {@link #ERROR_VIEW} right away, under the URL of
 * the link and with an error status, through
 * {@link ViewServlet#forwardToView(HttpServletRequest, HttpServletResponse, String, int, Map)}: a
 * link without a number, or with something else than a number, with
 * {@link HttpServletResponse#SC_BAD_REQUEST} and the reason {@link #REASON_INVALID}, a number no
 * ticket carries with {@link HttpServletResponse#SC_NOT_FOUND} and the reason
 * {@link #REASON_UNKNOWN}. The page receives the reason as parameter {@link #REASON_PARAM} and the
 * number of the link as parameter {@link #NR_PARAM}.
 * </p>
 *
 * <p>
 * Whether a ticket exists is decided without the access rights of the visitor: the page of the
 * ticket checks them when the visitor arrives there.
 * </p>
 */
public class TicketLinkServlet extends HttpServlet {

	/** Parameter of the link naming the number of the ticket, and of {@link #ERROR_VIEW} as well. */
	public static final String NR_PARAM = "nr";

	/** Parameter of {@link #ERROR_VIEW} naming why the link leads to no ticket. */
	public static final String REASON_PARAM = "reason";

	/** Value of {@link #REASON_PARAM} for a link that names no number. */
	public static final String REASON_INVALID = "invalid";

	/** Value of {@link #REASON_PARAM} for a number no ticket carries. */
	public static final String REASON_UNKNOWN = "unknown";

	/** The page explaining that a link leads to no ticket, an anonymous entry point. */
	public static final String ERROR_VIEW = "ticket-link-error.view.xml";

	/**
	 * The route of the page of a ticket below {@link ViewServlet#ROOT_PATH}, followed by the number
	 * of the ticket.
	 */
	private static final String TICKET_ROUTE = "tickets/ticket/";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String nr = request.getParameter(NR_PARAM);
		if (!isNumber(nr)) {
			showError(request, response, HttpServletResponse.SC_BAD_REQUEST, REASON_INVALID, nr);
			return;
		}
		TLObject ticket = ThreadContext.inSystemContext(TicketLinkServlet.class,
			() -> ObjectFunctions.resolve(nr, DemoTicketsFactory.getTicketType(), false));
		if (ticket == null) {
			showError(request, response, HttpServletResponse.SC_NOT_FOUND, REASON_UNKNOWN, nr);
			return;
		}
		// The identifier in the form the ticket list writes it, whatever form the link has.
		response.sendRedirect(
			request.getContextPath() + ViewServlet.ROOT_PATH + TICKET_ROUTE + ObjectFunctions.id(ticket));
	}

	private static boolean isNumber(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		try {
			Long.parseLong(value);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	private static void showError(HttpServletRequest request, HttpServletResponse response, int status,
			String reason, String nr) throws ServletException, IOException {
		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put(REASON_PARAM, reason);
		parameters.put(NR_PARAM, nr == null ? "" : nr);
		ViewServlet.forwardToView(request, response, ERROR_VIEW, status, parameters);
	}

}
