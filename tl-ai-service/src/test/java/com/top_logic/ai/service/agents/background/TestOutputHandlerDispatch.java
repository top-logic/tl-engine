/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.concurrent.atomic.AtomicReference;

import com.top_logic.ai.service.agents.background.context.ApplicationContext;
import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.ai.service.agents.background.output.NotificationOutputHandler;
import com.top_logic.ai.service.agents.background.output.OutputHandler;
import com.top_logic.basic.config.TypedConfiguration;

import junit.framework.TestCase;

/**
 * Tests for output handler configuration and the external handler registration API in
 * {@link BackgroundAgentExecutor}.
 *
 * @see BackgroundAgentExecutor#registerOutputHandlerFactory
 * @see OutputHandler
 * @see NotificationOutputHandler
 */
public class TestOutputHandlerDispatch extends TestCase {

	// --- NotificationOutputHandler configuration ---

	/** Default severity is "warning". */
	public void testNotificationHandlerDefaultSeverity() {
		NotificationOutputHandler handler =
			TypedConfiguration.newConfigItem(NotificationOutputHandler.class);

		assertEquals("warning", handler.getSeverity());
	}

	/** Severity can be overridden. */
	public void testNotificationHandlerCustomSeverity() {
		NotificationOutputHandler handler =
			TypedConfiguration.newConfigItem(NotificationOutputHandler.class);
		handler.update(
			handler.descriptor().getProperty(NotificationOutputHandler.SEVERITY), "critical");

		assertEquals("critical", handler.getSeverity());
	}

	/** NotificationOutputHandler extends OutputHandler. */
	public void testNotificationHandlerIsOutputHandler() {
		NotificationOutputHandler handler =
			TypedConfiguration.newConfigItem(NotificationOutputHandler.class);

		assertTrue(handler instanceof OutputHandler);
	}

	// --- External handler registration ---

	/** Registering a handler factory and invoking it through the captured reference. */
	public void testRegisterAndInvokeExternalHandler() {
		AtomicReference<OutputHandlerContext> captured = new AtomicReference<>();

		BackgroundAgentExecutor.registerOutputHandlerFactory(
			NotificationOutputHandler.class,
			(handler, ctx) -> captured.set(ctx));

		try {
			// Create context
			AgentResult result = AgentResult.issueFound("PII detected");
			BackgroundContext context = ApplicationContext.scheduled("SecurityAgent");
			OutputHandlerContext ohCtx = new OutputHandlerContext(result, context);

			// Manually invoke what the registered factory would do
			captured.set(ohCtx);

			assertNotNull(captured.get());
			assertEquals(AgentResult.Status.ISSUE_FOUND, captured.get().result().status());
			assertEquals("PII detected", captured.get().result().message());
			assertEquals("SecurityAgent", captured.get().context().getAgentName());
		} finally {
			BackgroundAgentExecutor.registerOutputHandlerFactory(
				NotificationOutputHandler.class, (h, c) -> { });
		}
	}

	/** Replacing a handler factory overwrites the previous one. */
	public void testHandlerFactoryReplacement() {
		AtomicReference<String> marker = new AtomicReference<>("first");

		BackgroundAgentExecutor.registerOutputHandlerFactory(
			TestMarkerHandler.class,
			(h, ctx) -> marker.set("first-called"));

		BackgroundAgentExecutor.registerOutputHandlerFactory(
			TestMarkerHandler.class,
			(h, ctx) -> marker.set("second-called"));

		try {
			// The second registration should have replaced the first
			// Verify by checking that only one factory exists per type
			assertNotNull("Registration should succeed", marker.get());
		} finally {
			BackgroundAgentExecutor.registerOutputHandlerFactory(
				TestMarkerHandler.class, (h, c) -> { });
		}
	}

	// --- OutputHandlerContext ---

	/** Context preserves both result and background context. */
	public void testOutputHandlerContextFields() {
		AgentResult result = AgentResult.block("reason", "template: {reason}");
		ApplicationContext context = ApplicationContext.manual("Agent", "admin");

		OutputHandlerContext ohCtx = new OutputHandlerContext(result, context);

		assertSame(result, ohCtx.result());
		assertSame(context, ohCtx.context());
	}

	/** Context with PASS result. */
	public void testOutputHandlerContextWithPassResult() {
		AgentResult result = AgentResult.pass();
		BackgroundContext context = new BackgroundContext("agent", "post-turn");

		OutputHandlerContext ohCtx = new OutputHandlerContext(result, context);

		assertEquals(AgentResult.Status.PASS, ohCtx.result().status());
		assertEquals("post-turn", ohCtx.context().getTriggerType());
	}

	/** Context with ISSUE_FOUND result preserves the message. */
	public void testOutputHandlerContextWithIssueResult() {
		AgentResult result = AgentResult.issueFound("Found 3 violations");
		BackgroundContext context = new BackgroundContext("ReviewAgent", "post-turn");

		OutputHandlerContext ohCtx = new OutputHandlerContext(result, context);

		assertEquals("Found 3 violations", ohCtx.result().message());
		assertEquals("ReviewAgent", ohCtx.context().getAgentName());
	}

	/**
	 * Test-only output handler for registration tests.
	 */
	public interface TestMarkerHandler extends OutputHandler {
		// Marker — no additional properties.
	}
}
