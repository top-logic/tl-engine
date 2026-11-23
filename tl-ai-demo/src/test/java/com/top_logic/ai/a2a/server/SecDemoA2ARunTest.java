/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.server;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;

import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.TaskState;
import com.top_logic.ai.demo.SecDemoPersonaTest;
import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.ai.tool.api.Tool;
import com.top_logic.ai.tool.api.ToolGroups;
import com.top_logic.ai.tool.api.ToolMode;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.ai.core.agent.ToolContext;
import com.top_logic.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.ai.core.execution.AgentEnvironment;
import com.top_logic.ai.core.session.ConversationLog;
import com.top_logic.ai.core.session.ConversationLogListener;
import com.top_logic.ai.core.tools.ToolExecutions;
import com.top_logic.ai.core.tools.task.TaskStore;
import com.top_logic.util.TLContextManager;

/**
 * The tool context of a run that another agent starts over the A2A endpoint, against the agent
 * security test bed.
 *
 * <p>
 * The calling agent answers the questions of the run, but it approves no tool call, and the person
 * of the account is not present. So the run is a background run: the {@code <background-allowlist>}
 * bounds its tools in addition to the roles of the account, also for an administrator. The run stays
 * attended for questions, and {@code allow-writes} of the served agent only narrows.
 * </p>
 *
 * <p>
 * The test bed's policy keeps {@code data.write} and {@code egress} off its allowlist and puts
 * {@code script} on it. {@code Anna} holds {@code data.write} and {@code script}. No persona holds
 * {@code egress}, so the rows about {@code egress} use an administrator, whom the roles bound in
 * nothing.
 * </p>
 *
 * <p>
 * The class sits in the package of {@link AgentRunBackend} because the tool context of an A2A run
 * comes from its package-visible {@link AgentRunBackend#toolContext}, which is no API for
 * applications. The policy,
 * the personas and the tools come from the test bed of this module.
 * </p>
 */
public class SecDemoA2ARunTest extends SecDemoPersonaTest {

	/** In {@code interaction}, which every run may use. */
	private static final String ASK_USER_TOOL = "user-interaction.askUser";

	/** How long the test waits for the question and for the answer. */
	private static final long WAIT_MILLIS = 10_000;

	/** A {@link ToolMode#READ} tool of {@code egress}. */
	private static final String EGRESS_READ_TOOL = "a2a.listAgents";

	/** A {@link ToolMode#EXTERNAL} tool of {@code egress}. */
	private static final String EGRESS_EXTERNAL_TOOL = "a2a.askAgent";

	/** An A2A run is a background run, and the calling agent can still answer its questions. */
	public void testAnA2ARunIsABackgroundRunThatMayAsk() {
		ToolContext run = a2aRun(actAs(ANNA), true);

		assertTrue("Nobody approves a tool call of an A2A run.", run.security().isBackgroundRun());
		assertTrue("The calling agent answers the questions of the run, so askUser must not be refused.",
			run.attended());
		assertNull("askUser is in interaction, which no ceiling withholds.",
			run.security().toolDenialReason(tool(ASK_USER_TOOL)));
	}

	/**
	 * A question of the run goes to the calling agent, and its answer is what {@code askUser}
	 * returns.
	 *
	 * <p>
	 * The question parks the thread of the run until the answer arrives. So it is asked on a second
	 * thread, in the person context of the account and with an {@link A2AInteractionStore} bound, as
	 * {@link AgentRunBackend} runs it.
	 * </p>
	 */
	public void testAQuestionOfTheRunGoesToTheCallingAgent() throws Exception {
		Person account = actAs(ANNA);
		ToolContext run = a2aRun(account, true);
		A2ATask task = new A2ATask("question-task", "question-context", "albert", ANNA);
		assertTrue("Precondition: the task is working.", task.moveTo(TaskState.WORKING, null));

		ExecutorService runThread = Executors.newSingleThreadExecutor();
		try {
			Future<String> answer = runThread.submit(
				() -> TLContextManager.<String, RuntimeException, RuntimeException> inPersonContext(account, () -> {
					try (AgentEnvironment env = AgentEnvironment.open(
						new ConversationLogListener(new ConversationLog()), new TaskStore(),
						new A2AInteractionStore(task, () -> {
							// Nothing to persist.
						}), Collections.synchronizedSet(new LinkedHashSet<>()), run.cancellationToken())) {
						return ToolExecutions.execute(tool(ASK_USER_TOOL),
							Map.of("question", "Which project is meant?", "header", "Clarify", "type", "text"), run)
							.text();
					}
				}));

			awaitState(task, TaskState.INPUT_REQUIRED);
			assertTrue("The waiting task must take the answer of the calling agent.", task.answer(
				A2AJson.textMessage(A2AJson.ROLE_USER, task.contextId(), task.id(), "Project Alpha")));
			String result = answer.get(WAIT_MILLIS, TimeUnit.MILLISECONDS);
			assertTrue("askUser must return the answer of the calling agent: " + result,
				result.contains("Project Alpha"));
		} finally {
			// Ends a question that is still parked when an assertion failed; no interrupt, which
			// could hit a database read.
			run.cancellationToken().cancel();
			runThread.shutdown();
		}
	}

	/** allow-writes="true" alone opens no write tool that the allowlist does not name. */
	public void testAWriteToolOffTheAllowlistIsRefusedAlthoughWritesAreAllowed() {
		assertAllowed(ANNA, CLASSIFIED_WRITE_SCRIPT_TOOL);

		assertRefusedByTheAllowlist(a2aRun(actAs(ANNA), true), CLASSIFIED_WRITE_SCRIPT_TOOL);
	}

	/** The allowlist is the only limit of an administrator account, so it must bound one. */
	public void testTheAllowlistBoundsAnAdministrator() {
		Person administrator = administrator();
		assertNull("Precondition: the roles do not bound an administrator.",
			TopLogicSecurityContext.forRun(administrator, false)
				.toolDenialReason(tool(CLASSIFIED_WRITE_SCRIPT_TOOL)));

		assertRefusedByTheAllowlist(a2aRun(administrator, true), CLASSIFIED_WRITE_SCRIPT_TOOL);
	}

	/**
	 * A write tool on the allowlist needs allow-writes="true" in addition.
	 *
	 * <p>
	 * With it, the gate admits the tool. A change that the tool saves still costs {@code data.write},
	 * which is off the list, as for every background run.
	 * </p>
	 */
	public void testAnAllowlistedWriteToolFollowsAllowWrites() {
		Tool write = tool(SCRIPT_TOOL);
		assertTrue("Precondition: the tool changes the application.", write.mode().writesApplicationState());

		ToolContext writes = a2aRun(actAs(ANNA), true);
		assertNull("The group script is on the allowlist and Anna holds it, so the gate admits the tool.",
			writes.security().toolDenialReason(write));
		List<TLObject> seeded = tickets();
		try {
			String refused = changeData(writes.security(), CREATE_TICKET, false);
			assertTrue("A change still needs data.write, which is off the allowlist: " + refused,
				refused.contains(ToolGroups.DATA_WRITE));
			assertEquals("Nothing may have been written.", seeded.size(), tickets().size());
		} finally {
			deleteTicketsBeyond(seeded);
		}

		ToolContext readOnly = a2aRun(actAs(ANNA), false);
		assertFalse("Without allow-writes, a served agent does not change the application.",
			readOnly.security().hasToolPermission(write));
		String reason = readOnly.security().toolDenialReason(write);
		assertNotNull("A refusal must state a reason.", reason);
		assertTrue("The reason must name the served agent's switch, not the allowlist: " + reason,
			reason.contains("asked by another agent"));
	}

	/**
	 * A READ or EXTERNAL tool of a group off the allowlist is refused, whatever allow-writes says.
	 *
	 * <p>
	 * allow-writes only takes away the tools that change the application. A call to another agent
	 * changes nothing here, so without the ceiling a served agent would call out with every grant of
	 * its account.
	 * </p>
	 */
	public void testAReadOrExternalToolOffTheAllowlistIsRefused() {
		Person administrator = administrator();
		for (String toolName : new String[] { EGRESS_READ_TOOL, EGRESS_EXTERNAL_TOOL }) {
			assertNull("Precondition: the roles do not bound an administrator.",
				TopLogicSecurityContext.forRun(administrator, false).toolDenialReason(tool(toolName)));
			for (boolean allowWrites : new boolean[] { true, false }) {
				assertRefusedByTheAllowlist(a2aRun(administrator, allowWrites), toolName);
			}
		}
	}

	/**
	 * The tool context that {@link AgentRunBackend} gives a run of the given account, which it builds
	 * in the context of that account.
	 */
	private static ToolContext a2aRun(Person account, boolean allowWrites) {
		actAs(account);
		return AgentRunBackend.toolContext(account, allowWrites, null, new CancellationToken());
	}

	private static void awaitState(A2ATask task, TaskState expected) throws InterruptedException {
		long deadline = System.currentTimeMillis() + WAIT_MILLIS;
		while (task.state() != expected && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertEquals("The question of the run must put the task into " + expected + ".", expected,
			task.state());
	}

	private static Person administrator() {
		Person administrator = anyAdministrator();
		assertNotNull("The application must have at least one administrator account.", administrator);
		return administrator;
	}

	private static void assertRefusedByTheAllowlist(ToolContext run, String toolName) {
		Tool tool = tool(toolName);
		assertFalse("An A2A run must not reach '" + toolName + "', which is off the background allowlist.",
			run.security().hasToolPermission(tool));
		String reason = run.security().toolDenialReason(tool);
		assertNotNull("A refusal must state a reason.", reason);
		assertTrue("The reason must name the allowlist: " + reason, reason.contains("background allowlist"));
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoA2ARunTest.class);
	}
}
