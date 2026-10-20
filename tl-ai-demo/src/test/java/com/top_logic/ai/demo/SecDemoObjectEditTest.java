/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.util.TLContext;

/**
 * The object tools of Albert ({@code object-edit.*}, Gitea #625) against the test bed's real accounts
 * and real model rights.
 *
 * <p>
 * Every test runs a real {@link AgentLoop} turn whose one tool call is the tool under test, with the
 * persona's own security context - so the invocation gate, the tool's checks, the turn's transaction,
 * the pre-commit guard and the commit are the ones a chat run goes through. What the fixture grants
 * (see {@code secdemo.config.xml}): {@code projectPartner} may read, write and create projects and
 * tickets - per object, on the projects the account partners on - and delete nothing, and nobody but
 * an administrator writes {@code pm:Project#partners}. Anna partners on Alpha, Ben only reads tickets.
 * </p>
 */
public class SecDemoObjectEditTest extends SecDemoPersonaTest {

	private static final String UPDATE = "object-edit.updateObject";

	private static final String CREATE = "object-edit.createObject";

	private static final String DELETE = "object-edit.deleteObject";

	private static final Instant T0 = Instant.parse("2026-10-02T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	/**
	 * A partner changes a ticket of her own project in one call, and the database records her - not
	 * the system, not an administrator - as the author of the change.
	 */
	public void testAPartnerChangesHerOwnTicketAndIsRecordedAsTheAuthor() {
		TLObject ticket = ticketOf(ALPHA);
		TLStructuredTypePart title = part("pm:Ticket#title");
		Object before = ticket.tValue(title);
		try {
			Turn turn = runAs(ANNA, UPDATE,
				"{\"id\": \"" + ReadFilter.idOf(ticket) + "\", \"values\": {\"title\": \"Changed by Anna\"}}");

			assertFalse("The call must succeed: " + turn.answer(), turn.failed());
			assertTrue("The turn must commit.", turn.committed());
			assertEquals("Changed by Anna", ticket.tValue(title));
			assertEquals("The revision must name the account the agent acted for.",
				contextIdOf(ANNA), turn.author());
		} finally {
			setAsAdministrator(ticket, title, before);
		}
	}

	/** The same partner is refused the ticket of a project she does not partner on, and it stays. */
	public void testAPartnerIsRefusedAnotherProjectsTicket() {
		TLObject ticket = ticketOf(BETA);
		TLStructuredTypePart title = part("pm:Ticket#title");
		Object before = ticket.tValue(title);

		Turn turn = runAs(ANNA, UPDATE,
			"{\"id\": \"" + ReadFilter.idOf(ticket) + "\", \"values\": {\"title\": \"Foreign\"}}");

		assertTrue("The call must be refused: " + turn.answer(), turn.failed());
		assertEquals(before, ticket.tValue(title));
		assertFalse("The refusal is not a rollback: nothing was written that would need one.",
			turn.rolledBack());
	}

	/**
	 * Holding the object is not holding each of its attributes: the partner writes her project, but
	 * not who partners on it - and the refusal names that attribute.
	 */
	public void testARestrictedAttributeIsRefusedByName() {
		TLObject project = project(ALPHA);
		TLStructuredTypePart partners = part("pm:Project#partners");
		Collection<?> before = List.copyOf((Collection<?>) project.tValue(partners));

		Turn turn = runAs(ANNA, UPDATE, "{\"id\": \"" + ReadFilter.idOf(project)
			+ "\", \"values\": {\"partners\": [\"" + ReadFilter.idOf(account(DORA)) + "\"]}}");

		assertTrue("The call must be refused: " + turn.answer(), turn.failed());
		assertTrue("The refusal must name the attribute: " + turn.answer(), turn.answer().contains("'partners'"));
		assertEquals(before, List.copyOf((Collection<?>) project.tValue(partners)));
	}

	/**
	 * An account that only reads is not offered the tools at all (it lacks {@code data.write}), and is
	 * refused by the model rights as well when a tool is handed to it anyway.
	 */
	public void testAReaderIsRefusedTwice() {
		assertDenied(BEN, UPDATE, "data.write");
		assertDenied(BEN, CREATE, "data.write");
		assertDenied(BEN, DELETE, "data.write");

		TLObject ticket = ticketOf(ALPHA);
		TLStructuredTypePart title = part("pm:Ticket#title");
		Object before = ticket.tValue(title);
		Turn turn = runAs(BEN, toolPermitting(BEN), UPDATE,
			"{\"id\": \"" + ReadFilter.idOf(ticket) + "\", \"values\": {\"title\": \"Reader\"}}");

		assertTrue("The model rights must refuse the write: " + turn.answer(), turn.failed());
		assertEquals(before, ticket.tValue(title));
	}

	/** A partner creates a ticket inside her project; inside another project nothing is created. */
	public void testCreateInsideAProject() {
		List<TLObject> before = new ArrayList<>(tickets());
		try {
			Turn own = runAs(ANNA, CREATE, "{\"type\": \"pm:Ticket\", \"values\": {\"title\": \"New in Alpha\"},"
				+ " \"container\": \"" + ReadFilter.idOf(project(ALPHA)) + "\"}");
			assertFalse("Creating in her own project must succeed: " + own.answer(), own.failed());
			assertTrue(own.committed());
			assertEquals(before.size() + 1, tickets().size());
			assertEquals(contextIdOf(ANNA), own.author());

			Turn foreign = runAs(ANNA, CREATE, "{\"type\": \"pm:Ticket\", \"values\": {\"title\": \"New in Beta\"},"
				+ " \"container\": \"" + ReadFilter.idOf(project(BETA)) + "\"}");
			assertTrue("Creating in a foreign project must be refused: " + foreign.answer(), foreign.failed());
			assertEquals(before.size() + 1, tickets().size());
		} finally {
			deleteTicketsBeyond(before);
		}
	}

	/** Deleting is a right of its own, and the fixture grants it to nobody: the ticket stays. */
	public void testDeleteIsRefusedWithoutTheDeleteRight() {
		TLObject ticket = ticketOf(ALPHA);

		Turn turn = runAs(ANNA, DELETE, "{\"id\": \"" + ReadFilter.idOf(ticket) + "\"}");

		assertTrue("The call must be refused: " + turn.answer(), turn.failed());
		assertTrue(turn.answer().contains("may not delete"));
		assertTrue("The ticket must still be there.", ticket.tValid());
	}

	/**
	 * A run nobody watches acts as its account and is bounded by the background allowlist, which this
	 * fixture writes without {@code data.write}: no write tool there, for a partner either.
	 */
	public void testAnUnattendedRunGetsNoWriteTool() {
		SecurityContext unattended = securityFor(ANNA, true);
		for (String name : List.of(UPDATE, CREATE, DELETE)) {
			assertFalse(name + " must not reach an unattended run.", unattended.hasToolPermission(tool(name)));
		}
	}

	// -- running a turn --

	/** The outcome of one turn: what the tool answered and what became of the transaction. */
	private record Turn(String answer, boolean failed, boolean committed, boolean rolledBack, String author) {
		// Intentionally empty.
	}

	private Turn runAs(String login, String toolName, String arguments) {
		return runAs(login, securityFor(login), toolName, arguments);
	}

	/**
	 * Runs one agent turn as the given persona whose single tool call is the given tool.
	 */
	private Turn runAs(String login, SecurityContext security, String toolName, String arguments) {
		actAs(login);
		Tool tool = tool(toolName);
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(tool);
		catalog.freeze();

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Do it."))));
		List<String> events = new ArrayList<>();
		AgentEventListener listener = event -> {
			if (event instanceof AgentEvent.TransactionCommitted) {
				events.add("commit");
			} else if (event instanceof AgentEvent.TransactionRolledBack) {
				events.add("rollback");
			}
		};

		Revision revision;
		try (TransactionScope scope = new TransactionScope()) {
			ToolContext context = new ToolContext(security, ApprovalCallback.AUTO_APPROVE, scope);
			new AgentLoop(params(), provider(tool.name(), arguments), catalog).run(conversation, context, listener);
			revision = scope.commitRevision();
		}

		String answer = null;
		boolean failed = false;
		for (Message message : conversation.messages()) {
			if (message instanceof ToolResultMessage result) {
				if (result.state() instanceof ToolState.Completed completed) {
					answer = completed.result();
				} else if (result.state() instanceof ToolState.Failed failure) {
					answer = failure.error();
					failed = true;
				}
			}
		}
		assertNotNull("The tool must have been called.", answer);
		return new Turn(answer, failed, events.contains("commit"), events.contains("rollback"),
			revision == null ? null : revision.getAuthor());
	}

	/**
	 * The persona's real model rights with every tool granted - for the account the tool layer
	 * already refuses, so that the model rights are what is asked.
	 */
	private SecurityContext toolPermitting(String login) {
		SecurityContext real = securityFor(login);
		return new SecurityContext() {

			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return real.modelSecurity();
			}

			@Override
			public ExecutionMode executionMode() {
				return real.executionMode();
			}
		};
	}

	/** The id the database records as the author of a commit made by the given persona. */
	private String contextIdOf(String login) {
		actAs(login);
		return TLContext.getContext().getContextId();
	}

	/** Puts a value back with the rights of an administrator, so a test leaves the fixture as it was. */
	private void setAsAdministrator(TLObject object, TLStructuredTypePart part, Object value) {
		Person admin = anyAdministrator();
		TLContext.getContext().setCurrentPerson(admin);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			object.tUpdate(part, value);
			tx.commit();
		}
	}

	private static AgentParams params() {
		return new AgentParams("test-model", "You change things.", 5, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/** A provider that makes the one given tool call and then finishes. */
	private static LLMProvider provider(String toolName, String arguments) {
		AtomicInteger call = new AtomicInteger();
		return (request, handler) -> {
			int index = call.getAndIncrement();
			if (index == 0) {
				ToolCall toolCall = new ToolCall("tc1", toolName, arguments);
				handler.onEvent(new StreamEvent.ToolCallStart(0, toolCall.id(), toolCall.name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(0, toolCall.arguments()));
				handler.onEvent(new StreamEvent.MessageComplete("msg-0", "test-model", "tool_use",
					USAGE, List.of(toolCall)));
				return;
			}
			if (index == 1) {
				TextContent text = new TextContent("Done.");
				handler.onEvent(new StreamEvent.TextDelta(0, text.text()));
				handler.onEvent(new StreamEvent.MessageComplete("msg-1", "test-model", "end_turn",
					USAGE, List.of(text)));
				return;
			}
			throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400,
				"The turn must be over after one tool call.");
		};
	}

	private static TLStructuredTypePart part(String qualifiedName) {
		return (TLStructuredTypePart) TLModelUtil.findPart(qualifiedName);
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoObjectEditTest.class);
	}
}
