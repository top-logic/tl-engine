/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.sched;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.sched.AgentTask.Config;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;

/**
 * The configuration surface of {@link AgentTask}: what an unconfigured task is, what the XML of
 * issue #384 parses to, and which configurations are refused at boot.
 */
public class TestAgentTaskConfig extends TestCase {

	/**
	 * What a task that says nothing beyond its prompt runs as.
	 *
	 * <p>
	 * These four defaults are the ticket's own: the entry-point agent, the {@code root} account,
	 * auto-approval and a recorded session. Each of them is what an application would otherwise
	 * have had to write out, so each is worth pinning.
	 * </p>
	 */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);

		assertNull("No agent named: the application's entry point answers, as in the chat.",
			config.getAgent());
		assertNull("No account named: the run acts as root.", config.getRunAs());
		assertEquals("A scheduled run has nobody to confirm its tool calls.",
			ApprovalMode.AUTO_APPROVE, config.getApprovalMode());
		assertTrue("The transcript is the point of going through the chat.", config.isRecordSession());
		assertNull("Without a title the session is named after the task.", config.getSessionTitle());
		assertNull(config.getPrompt());
		assertNull(config.getPromptExpression());
	}

	/** The ticket's own example, as an application writes it. */
	public void testTicketExampleParses() throws Exception {
		Config config = parse("<task %s name='HourlyHunt' run-on-startup='false'"
			+ " agent='Ida' run-as='root' approval-mode='AUTO_APPROVE' record-session='true'>"
			+ "<prompt>Scheduled hunt. Load hunt-lead. Hunt exactly one new person.</prompt>"
			+ "<schedules><periodically interval='1h' start-time='00:15'/></schedules>"
			+ "</task>");

		assertEquals("HourlyHunt", config.getName());
		assertEquals("Ida", config.getAgent());
		assertEquals("root", config.getRunAs());
		assertEquals(ApprovalMode.AUTO_APPROVE, config.getApprovalMode());
		assertTrue(config.isRecordSession());
		assertFalse(config.isRunOnStartup());
		assertEquals("Scheduled hunt. Load hunt-lead. Hunt exactly one new person.", config.getPrompt());
		assertEquals("The schedule is the engine's own, untouched.", 1, config.getSchedules().size());
	}

	/**
	 * A prompt of several lines is written as an element, so it stays readable in the XML.
	 *
	 * <p>
	 * The reason the property is offered as an element at all: a scheduled prompt is an
	 * instruction with rules in it, not a sentence, and as an attribute it would be one long line.
	 * </p>
	 */
	public void testMultiLinePromptParses() throws Exception {
		Config config = parse("<task %s name='Nightly'><prompt>Line one.\nLine two.</prompt></task>");

		assertEquals("Line one.\nLine two.", config.getPrompt());
	}

	/** A TL-Script prompt parses as an expression, not as text. */
	public void testPromptExpressionParses() throws Exception {
		Config config = parse("<task %s name='Nightly'>"
			+ "<prompt-expression>\"Summarise \" + count(all(`tl.ai.execution:AgentSession`))"
			+ " + \" sessions.\"</prompt-expression></task>");

		assertNull("The two are alternatives; naming one leaves the other unset.", config.getPrompt());
		assertNotNull(config.getPromptExpression());
	}

	/** A task with both kinds of prompt is refused: which one it would send is undecidable. */
	public void testBothPromptsRefused() throws Exception {
		assertRefused(parse("<task %s name='Nightly'><prompt>a</prompt>"
			+ "<prompt-expression>\"b\"</prompt-expression></task>"), "both");
	}

	/** A task with no prompt at all is refused: there is nothing to ask. */
	public void testNoPromptRefused() throws Exception {
		assertRefused(parse("<task %s name='Nightly'/>"), "neither");
	}

	/**
	 * That instantiating the given configuration is reported as a configuration error naming
	 * {@code reason}.
	 *
	 * <p>
	 * At boot this reaches the administrator as a startup failure of the {@code Scheduler}; the
	 * point of asserting it here is that the task refuses at boot rather than at 03:00, when the
	 * only trace would be a task result nobody is watching.
	 * </p>
	 */
	private static void assertRefused(Config config, String reason) {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(config);

		String errors = String.join(" | ", log.getErrors());
		assertTrue("Expected a configuration error naming '" + reason + "', got: " + errors,
			errors.contains(reason));
	}

	private static Config parse(String xml) throws Exception {
		String attributes = "xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'";
		return (Config) TypedConfiguration.parse(CharacterContents.newContent(String.format(xml, attributes)));
	}

	/** Boots the module system so typed configuration resolves the nested schedule tags. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestAgentTaskConfig.class));
	}
}
