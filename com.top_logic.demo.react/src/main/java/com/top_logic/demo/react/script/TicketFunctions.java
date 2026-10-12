/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.script;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.model.search.expr.config.operations.UsesSecurity;
import com.top_logic.model.util.TLModelUtil;

/**
 * TL-Script functions about the tickets of the demo, implemented in Java.
 *
 * <p>
 * The functions read the attributes of a ticket through
 * {@link EvalContext#access(TLObject, TLStructuredTypePart, boolean)}, so that a form evaluating a
 * script calling them evaluates it again when the ticket's attributes are edited, and sees the
 * edited values.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #isClosed(EvalContext, TLObject, boolean)} is called
 *           <code>demoTicketsIsClosed</code>.
 */
@ScriptPrefix(TicketFunctions.PREFIX)
public class TicketFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "demoTickets";

	/** Qualified name of the status reference of a ticket. */
	public static final String TICKET_STATUS = "demo.tickets:Ticket#status";

	/** Qualified name of the status of a closed ticket. */
	public static final String STATUS_CLOSED = "demo.tickets:TicketStatus#closed";

	/**
	 * Whether the given ticket is closed.
	 *
	 * @param definitions
	 *        The context of the evaluation.
	 * @param ticket
	 *        The ticket to check.
	 * @param withSecurity
	 *        Whether the read access to the status of the ticket is checked.
	 * @return Whether the status of the ticket is the closed one.
	 */
	@SideEffectFree
	public static boolean isClosed(EvalContext definitions, @Mandatory TLObject ticket,
			@UsesSecurity boolean withSecurity) {
		TLStructuredTypePart statusPart = TLModelUtil.findPart(TICKET_STATUS);
		TLClassifier closed = TLModelUtil.findPart(STATUS_CLOSED);
		return definitions.access(ticket, statusPart, withSecurity) == closed;
	}

}
