/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * The four rows every {@code pm} scenario starts from, and putting them back after a run changed
 * them.
 *
 * <p>
 * Small on purpose. A fixture a reader can hold in their head is one whose expectations can be
 * checked by eye, and the numbers below appear verbatim in the scenario file - "Alpha has two
 * tickets" is a fact of this class before it is an expectation of a scenario.
 * </p>
 *
 * <p>
 * {@link #reset()} deletes and recreates rather than trying to undo what a run did. It is the only
 * approach that is right whatever the run did - wrote nothing, wrote a little, deleted everything -
 * and the alternative, a hand-written script that reverses each scenario's changes, is the
 * maintenance burden that the prototype of this eval in the demo application already demonstrates.
 * </p>
 */
public class PmFixture implements EvalFixture {

	/** The project every read scenario asks about. */
	public static final String ALPHA = "Alpha";

	/** The second project, so that a scenario can be wrong by answering about all projects. */
	public static final String BETA = "Beta";

	private static final String PROJECT_TYPE = "pm:Project";

	private static final String TICKET_TYPE = "pm:Ticket";

	/** Ticket titles of {@link #ALPHA}, in creation order. */
	public static final List<String> ALPHA_TICKETS = List.of("Login fails", "Export is slow");

	/** Ticket titles of {@link #BETA}, in creation order. */
	public static final List<String> BETA_TICKETS = List.of("Wrong currency");

	@Override
	public void reset() {
		Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			deleteAll(TICKET_TYPE);
			deleteAll(PROJECT_TYPE);
			createProject(ALPHA, ALPHA_TICKETS);
			createProject(BETA, BETA_TICKETS);
			tx.commit();
		} finally {
			tx.rollback();
		}
		// The agent runs on this thread but commits through its own scope; a later read of this
		// thread has to see what was just written, so the stable view is advanced here as well as
		// after the run.
		HistoryUtils.updateSessionRevision();
	}

	/** How many tickets the seeded state has, over all projects. */
	public static int seededTicketCount() {
		return ALPHA_TICKETS.size() + BETA_TICKETS.size();
	}

	private static void createProject(String name, List<String> ticketTitles) {
		TLObject project = ModelService.getInstance().getFactory().createObject(type(PROJECT_TYPE));
		project.tUpdateByName("name", name);
		List<TLObject> tickets = new ArrayList<>();
		for (String title : ticketTitles) {
			TLObject ticket = ModelService.getInstance().getFactory().createObject(type(TICKET_TYPE));
			ticket.tUpdateByName("title", title);
			ticket.tUpdateByName("status", "open");
			tickets.add(ticket);
		}
		project.tUpdateByName("tickets", tickets);
	}

	private static void deleteAll(String typeName) {
		for (TLObject instance : MetaElementUtil.getAllInstancesOf(type(typeName), TLObject.class)) {
			if (instance.tValid()) {
				instance.tDelete();
			}
		}
	}

	private static TLClass type(String qualifiedName) {
		return (TLClass) TLModelUtil.findType(qualifiedName);
	}
}
