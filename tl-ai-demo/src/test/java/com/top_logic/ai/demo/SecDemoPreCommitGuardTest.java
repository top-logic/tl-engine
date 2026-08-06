/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
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
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.util.model.ModelService;

/**
 * End-to-end test of the pre-commit guard (L4) against the test bed in
 * {@code WEB-INF/conf/secdemo.config.xml}: a tool that performs <b>no check of its own</b> writes
 * through the turn's transaction, and the guard is the only thing between that write and the database.
 *
 * <p>
 * That is the scenario the layer exists for. Every other layer of the design assumes the tool
 * cooperates - {@link SecDemoModelSecurityTest} covers the cooperating case, where TL-Script itself
 * asks {@code ModelAccessRights} before it writes. The tool here does not ask, exactly as a remote MCP
 * tool or a Java tool whose author forgot does not ask, and the persona it runs as
 * (Anna) legitimately holds the {@code data.write} capability, so the tool layer (L1)
 * lets the call through - see {@link #testTheToolLayerLetsTheCallThrough()}. Nothing but L4 is left.
 * </p>
 *
 * <p>
 * The unchecked tool lives here and not in the shipped fixture on purpose: the demo application must
 * not gain a tool that writes without checking. It is registered only in the {@link ToolCatalog} of
 * the agent run each test builds, and it writes through the model factory the same way any Java tool
 * would.
 * </p>
 *
 * <p>
 * The runs go through the real {@link AgentLoop} with a stubbed provider, so what is under test is the
 * wiring as well as the decision: the turn's single transaction, the guard call before the commit, and
 * the all-or-nothing rollback. No LLM key is needed.
 * </p>
 *
 * @see doc/security/testbed.md section 4.8
 */
public class SecDemoPreCommitGuardTest extends SecDemoPersonaTest {

	/** The unchecked write tool the model is made to call. */
	private static final String TOOL = "writeUnchecked";

	private static final Instant T0 = Instant.parse("2026-08-04T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	/** The title the tool writes, so a test can tell its ticket from the seeded ones. */
	private static final String WRITTEN_TITLE = "Unchecked ticket";

	/** The title the tool writes where it may not - must never appear in what the model is told. */
	private static final String WRITTEN_ELSEWHERE = "Unchecked-foreign-title";

	/**
	 * What the unchecked tool does when it runs.
	 */
	private interface UncheckedWrite {

		/** Writes something, without asking anybody whether it may. */
		void write();
	}

	/**
	 * The tool layer lets this call through: {@code secwriter1} holds {@code data.write}, which is the
	 * group the tool declares.
	 *
	 * <p>
	 * The precondition of every other test here. If L1 refused the call, the rollbacks below would
	 * prove nothing about L4.
	 * </p>
	 */
	public void testTheToolLayerLetsTheCallThrough() {
		SecurityContext security = securityFor(ANNA);

		Tool tool = uncheckedTool(() -> {
			// Never executed: this test only asks the invocation gate.
		});
		assertNull("The tool layer must not be what refuses this call: "
			+ security.toolDenialReason(tool), security.toolDenialReason(tool));
		assertTrue(security.hasToolPermission(tool));
	}

	/**
	 * A write the persona <em>is</em> entitled to make is committed: the guard does not stand in the
	 * way of legitimate work.
	 */
	public void testPermittedUncheckedWriteCommits() {
		List<TLObject> before = new ArrayList<>(tickets());
		try {
			Turn turn = runTurnAs(ANNA, () -> createTicket(WRITTEN_TITLE));

			assertTrue("The turn must commit: projectPartner holds Create on pm:Ticket, and Anna holds"
				+ " that role on the security root - the context a top-level creation is checked"
				+ " against.", turn.committed());
			assertNull("Nothing was rolled back, so nothing has to be corrected.", turn.notice());
			assertEquals("The ticket must have been persisted.", before.size() + 1, tickets().size());
		} finally {
			deleteTicketsBeyond(before);
		}
	}

	/**
	 * Creating an object inside a container the persona may not write is caught before the commit.
	 *
	 * <p>
	 * A per-object forbidden creation, which is the shape this model can express: Anna holds
	 * {@code Create} on {@code pm:Ticket} and may not write project Beta, and a creation inside a
	 * composition needs both - {@code Create} on the created type in the container's context
	 * <em>and</em> {@code Write} on the container's composition attribute.
	 * </p>
	 */
	public void testForbiddenCreationIsRolledBack() {
		List<TLObject> before = new ArrayList<>(tickets());
		TLObject foreignProject = project(BETA);
		try {
			Turn turn = runTurnAs(ANNA, () -> createTicketIn(foreignProject, WRITTEN_ELSEWHERE));

			assertFalse("The turn must not commit.", turn.committed());
			assertEquals("Pre-commit security check failed", turn.rollbackReason());
			assertEquals("No ticket may have been persisted.", before.size(), tickets().size());

			String notice = turn.notice();
			assertNotNull("The model must be told that its writes are gone.", notice);
			assertTrue("The notice must name the type, but was: " + notice, notice.contains("pm:"));
			assertFalse("The notice must not quote the written value, but was: " + notice,
				notice.contains(WRITTEN_ELSEWHERE));
		} finally {
			deleteTicketsBeyond(before);
		}
	}

	/**
	 * Writing an attribute of an object the persona may not write is caught, and the committed value
	 * is untouched.
	 */
	public void testForbiddenUpdateIsRolledBack() {
		TLObject foreignTicket = ticketOf(BETA);
		TLStructuredTypePart title = part("pm:Ticket#title");
		Object titleBefore = foreignTicket.tValue(title);

		Turn turn = runTurnAs(ANNA, () -> foreignTicket.tUpdate(title, WRITTEN_ELSEWHERE));

		assertFalse("The turn must not commit: Anna does not partner on Beta.", turn.committed());
		assertEquals("The committed value must be unchanged.", titleBefore, foreignTicket.tValue(title));

		String notice = turn.notice();
		assertNotNull(notice);
		assertTrue("The notice must name the operation, but was: " + notice, notice.contains("Write"));
		assertTrue("The notice must name the type, but was: " + notice, notice.contains("pm:Ticket"));
		assertFalse("The notice must not quote the written value, but was: " + notice,
			notice.contains(WRITTEN_ELSEWHERE));
	}

	/**
	 * A deletion is its own right: the fixture grants {@code projectPartner} Read, Create and Write on
	 * {@code pm:Ticket} and no Delete, so deleting one is refused - even a ticket of the persona's own
	 * project, which it may otherwise write freely.
	 */
	public void testForbiddenDeletionIsRolledBack() {
		List<TLObject> before = new ArrayList<>(tickets());
		TLObject ticket = ticketOf(ALPHA);

		Turn turn = runTurnAs(ANNA, () -> ticket.tDelete());

		assertFalse("The turn must not commit.", turn.committed());
		assertEquals("The ticket must still be there.", before.size(), tickets().size());
		assertTrue("The ticket must still be alive.", ticket.tValid());

		String notice = turn.notice();
		assertNotNull(notice);
		assertTrue("The notice must name the operation, but was: " + notice, notice.contains("Delete"));
	}

	/**
	 * The rollback is all-or-nothing: a turn that made one permitted and one forbidden change loses
	 * <b>both</b>.
	 *
	 * <p>
	 * The whole reason the guard runs once per turn rather than per tool call. A partial commit would
	 * leave the application in a state no user could have produced, and the model would have no way to
	 * find out which half survived.
	 * </p>
	 */
	public void testRollbackIsAllOrNothing() {
		List<TLObject> before = new ArrayList<>(tickets());
		TLObject foreignTicket = ticketOf(BETA);
		TLStructuredTypePart title = part("pm:Ticket#title");
		Object titleBefore = foreignTicket.tValue(title);
		try {
			Turn turn = runTurnAs(ANNA, () -> {
				createTicket(WRITTEN_TITLE);
				foreignTicket.tUpdate(title, WRITTEN_ELSEWHERE);
			});

			assertFalse("The turn must not commit.", turn.committed());
			assertEquals("The permitted ticket must be gone as well.", before.size(), tickets().size());
			assertEquals(titleBefore, foreignTicket.tValue(title));
			assertTrue("The model must be told that the rollback took everything: " + turn.notice(),
				turn.notice().contains("all-or-nothing"));
		} finally {
			deleteTicketsBeyond(before);
		}
	}

	/**
	 * The persona who scopes nothing is refused by the guard as well, and L1 let her through.
	 *
	 * <p>
	 * Dora holds {@code data.write} on the security root, so the invocation gate is genuinely not what
	 * refuses this call - {@link #testTheToolLayerLetsTheCallThrough()} for Anna is the same statement,
	 * and Dora is indistinguishable from Anna at that layer. What refuses her is that she partners on
	 * nothing, per object, at the end of the turn.
	 * </p>
	 */
	public void testPersonaWhoScopesNothingIsRefusedByTheGuardToo() {
		assertAllowed(DORA, CLASSIFIED_WRITE_SCRIPT_TOOL);

		TLObject ticket = ticketOf(ALPHA);
		TLStructuredTypePart title = part("pm:Ticket#title");
		Object titleBefore = ticket.tValue(title);

		Turn turn = runTurnAs(DORA, () -> ticket.tUpdate(title, WRITTEN_ELSEWHERE));

		assertFalse("The turn must not commit.", turn.committed());
		assertEquals("Nothing may have been persisted.", titleBefore, ticket.tValue(title));
		assertTrue(turn.notice().contains("pm:Ticket"));
	}

	/**
	 * The outcome of one agent turn, as the loop's events and the conversation report it.
	 */
	private record Turn(boolean committed, String rollbackReason, String notice) {
		// Intentionally empty.
	}

	/**
	 * Runs one agent turn as the given persona, with the real security context that persona gets.
	 */
	private Turn runTurnAs(String login, UncheckedWrite write) {
		return runTurnAs(login, securityFor(login), write);
	}

	/**
	 * Runs one agent turn whose single tool call performs the given unchecked write.
	 *
	 * @param login
	 *        The persona to bind to the thread, so the engine's checks see it.
	 * @param security
	 *        The security context of the run.
	 * @param write
	 *        What the tool does.
	 */
	private Turn runTurnAs(String login, SecurityContext security, UncheckedWrite write) {
		actAs(login);

		ToolCatalog catalog = new ToolCatalog();
		catalog.register(uncheckedTool(write));
		catalog.freeze();

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Write something."))));

		List<String> events = new ArrayList<>();
		AgentEventListener listener = event -> {
			if (event instanceof AgentEvent.TransactionCommitted) {
				events.add("commit");
			} else if (event instanceof AgentEvent.TransactionRolledBack rolledBack) {
				events.add("rollback:" + rolledBack.reason());
			}
		};

		try (TransactionScope scope = new TransactionScope()) {
			ToolContext context = new ToolContext(security, ApprovalCallback.AUTO_APPROVE, scope);
			AgentLoop loop = new AgentLoop(params(), provider(), catalog);
			loop.run(conversation, context, listener);
		}

		String rollbackReason = null;
		for (String event : events) {
			if (event.startsWith("rollback:")) {
				rollbackReason = event.substring("rollback:".length());
			}
		}
		return new Turn(events.contains("commit"), rollbackReason, notice(conversation));
	}

	/**
	 * A tool that writes and asks nobody whether it may - a stand-in for a remote MCP tool or a Java
	 * tool whose author forgot the check.
	 *
	 * <p>
	 * It declares {@link ToolGroups#DATA_WRITE}, which the fixture grants to {@code projectPartner}, so
	 * the invocation gate is not what refuses it. Its {@link ToolMode#DATA_WRITE} mode is what makes
	 * the loop open a transaction for the turn.
	 * </p>
	 */
	private static Tool uncheckedTool(UncheckedWrite write) {
		return new Tool() {

			@Override
			public String name() {
				return TOOL;
			}

			@Override
			public String description() {
				return "Writes without checking anything.";
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(TOOL, description(), "{}");
			}

			@Override
			public ToolMode mode() {
				return ToolMode.DATA_WRITE;
			}

			@Override
			public String group() {
				return ToolGroups.DATA_WRITE;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				// Deliberately no context.modelSecurity() check: that is the whole point.
				write.write();
				return ToolResult.of("Written.");
			}
		};
	}

	/**
	 * A security context that enforces model access for the given persona but grants every tool.
	 *
	 * <p>
	 * For the persona the tool layer already refuses. The model security is the persona's real one -
	 * the engine's rights for that account under the fixture's {@code <security-config>} - so what is
	 * relaxed is only the layer this test is not about.
	 * </p>
	 */
	private SecurityContext toolPermittingContext(String login) {
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

	private static AgentParams params() {
		return new AgentParams("test-model", "You write things.", 5,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/**
	 * A provider that asks for the unchecked tool once and then finishes.
	 */
	private static LLMProvider provider() {
		AtomicInteger call = new AtomicInteger();
		return (request, handler) -> {
			int index = call.getAndIncrement();
			if (index == 0) {
				ToolCall toolCall = new ToolCall("tc1", TOOL, "{}");
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

	/** The {@code [Framework notice]} the loop added, or <code>null</code>. */
	private static String notice(Conversation conversation) {
		for (Message message : conversation.messages()) {
			if (message instanceof UserMessage user) {
				for (ContentPart part : user.content()) {
					if (part instanceof TextContent text
							&& text.text().startsWith("[Framework notice]")) {
						return text.text();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Creates a top-level ticket with its mandatory title set.
	 *
	 * <p>
	 * The title matters: without it the commit would fail on a model constraint, and a test that
	 * expects the <em>guard</em> to stop the commit would pass for the wrong reason.
	 * </p>
	 */
	private static TLObject createTicket(String title) {
		TLClass type = (TLClass) TLModelUtil.findType("pm:Ticket");
		TLObject ticket = ModelService.getInstance().getFactory().createObject(type, null);
		ticket.tUpdate(part("pm:Ticket#title"), title);
		return ticket;
	}

	/**
	 * Creates a ticket inside the given project's composition.
	 */
	@SuppressWarnings("unchecked")
	private static TLObject createTicketIn(TLObject project, String title) {
		TLObject ticket = createTicket(title);
		TLStructuredTypePart tickets = part("pm:Project#tickets");
		List<TLObject> contained =
			new ArrayList<>((java.util.Collection<TLObject>) project.tValue(tickets));
		contained.add(ticket);
		project.tUpdate(tickets, contained);
		return ticket;
	}

	private static TLStructuredTypePart part(String qualifiedName) {
		return (TLStructuredTypePart) TLModelUtil.findPart(qualifiedName);
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoPreCommitGuardTest.class);
	}
}
