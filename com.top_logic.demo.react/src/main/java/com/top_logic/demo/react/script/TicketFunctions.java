/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.script;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.demo.react.model.tickets.Ticket;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.TracingScope;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.model.util.TLModelUtil;

/**
 * TL-Script functions about the tickets of the demo, implemented in Java.
 *
 * <p>
 * The functions read the attributes of a ticket through {@link TLObject#tValueByName(String)}
 * like any other Java code. A form evaluates its scripts in a {@link TracingScope}, which reports
 * these reads and answers them from the values entered in the form, so a script calling these
 * functions is evaluated again when the ticket's attributes are edited, and sees the edited values.
 * </p>
 *
 * <p>
 * A function takes the ticket as {@link TLObject} rather than {@link Ticket}, since a form in edit
 * mode passes its overlay of the ticket to its scripts.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #isClosed(TLObject)} is called <code>demoTicketsIsClosed</code>.
 */
@ScriptPrefix(TicketFunctions.PREFIX)
public class TicketFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "demoTickets";

	/** Qualified name of the status of a closed ticket. */
	public static final String STATUS_CLOSED = "demo.tickets:TicketStatus#closed";

	/**
	 * Whether the given ticket is closed.
	 *
	 * <p>
	 * The status of the ticket is read without an access check.
	 * </p>
	 *
	 * @param ticket
	 *        The ticket to check.
	 * @return Whether the status of the ticket is the closed one.
	 */
	@SideEffectFree
	public static boolean isClosed(@Mandatory TLObject ticket) {
		TLClassifier closed = TLModelUtil.findPart(STATUS_CLOSED);
		return ticket.tValueByName(Ticket.STATUS_ATTR) == closed;
	}

}
