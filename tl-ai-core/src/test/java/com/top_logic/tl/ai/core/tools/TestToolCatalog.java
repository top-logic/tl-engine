/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolContent;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Contract tests for {@link ToolCatalog}: registration, freeze, lookup, schema
 * production, and the {@code execute()} dispatch method.
 *
 * <p>Subsumes the former {@code TestToolCatalogCollision} tests.</p>
 */
public class TestToolCatalog extends TestCase {

	/**
	 * Test suite that boots the {@link com.top_logic.basic.thread.ThreadContextManager} module.
	 *
	 * <p>
	 * {@link ToolCatalog#execute} routes tool invocation through
	 * {@link com.top_logic.tl.ai.core.tools.ToolExecutions#execute}, which establishes a TopLogic
	 * interaction context (tools may touch the {@code KnowledgeBase}). That requires the
	 * {@code ThreadContextManager} module to be started, so the plain {@code TestCase} runner is not
	 * enough.
	 * </p>
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestToolCatalog.class)));
	}

	// ===== Registration Contract =====

	/** Register a tool and verify getTool(name) returns it. */
	public void testRegisterAndRetrieve() {
		ToolCatalog catalog = new ToolCatalog();
		Tool tool = echoTool();

		catalog.register(tool);

		assertSame(tool, catalog.getTool("echo"));
	}

	/** Register a tool and verify tools() contains it. */
	public void testRegisterAppearsInToolsList() {
		ToolCatalog catalog = new ToolCatalog();
		Tool tool = echoTool();

		catalog.register(tool);

		List<Tool> tools = catalog.tools();
		assertEquals(1, tools.size());
		assertSame(tool, tools.get(0));
	}

	/** Register a tool and verify toolSchemas() includes its schema. */
	public void testRegisterProducesSchema() {
		ToolCatalog catalog = new ToolCatalog();
		Tool tool = echoTool();

		catalog.register(tool);

		List<ToolSchema> schemas = catalog.toolSchemas();
		assertEquals(1, schemas.size());
		assertEquals("echo", schemas.get(0).name());
	}

	/** A tool with broken JSON in inputSchemaJson throws IllegalArgumentException at registration. */
	public void testRegisterRejectsInvalidSchema() {
		ToolCatalog catalog = new ToolCatalog();
		Tool bad = invalidSchemaTool();

		try {
			catalog.register(bad);
			fail("Expected IllegalArgumentException for invalid schema JSON.");
		} catch (IllegalArgumentException e) {
			assertTrue("Error should mention the tool name, got: " + e.getMessage(),
				e.getMessage().contains("badSchema"));
		}
	}

	/** register(Object) with a @ToolMethod-annotated object creates tools from methods. */
	public void testRegisterAnnotatedProvider() {
		ToolCatalog catalog = new ToolCatalog();

		catalog.register(new SampleToolProvider());

		Tool greet = catalog.getTool("greet");
		assertNotNull("Annotated tool 'greet' should be registered.", greet);
		assertEquals("Says hello", greet.description());
		assertEquals(ToolMode.READ, greet.mode());
	}

	/** Two tools with the same name produce an error that includes both class names. */
	public void testCollisionContainsClassNames() {
		ToolCatalog catalog = new ToolCatalog();

		Tool first = new FirstTool();
		Tool second = new SecondTool();

		catalog.register(first);

		try {
			catalog.register(second);
			fail("Expected IllegalArgumentException for name collision.");
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			assertTrue("Should contain tool name, got: " + msg,
				msg.contains("'search'"));
			assertTrue("Should contain existing class name, got: " + msg,
				msg.contains("FirstTool"));
			assertTrue("Should contain new class name, got: " + msg,
				msg.contains("SecondTool"));
		}
	}

	/** Same instance registered twice produces an error. */
	public void testSameToolRegisteredTwice() {
		ToolCatalog catalog = new ToolCatalog();
		Tool tool = new FirstTool();
		catalog.register(tool);

		try {
			catalog.register(tool);
			fail("Expected IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("'search'"));
		}
	}

	// ===== Freeze Contract =====

	/** freeze() then register() throws IllegalStateException. */
	public void testFreezePreventsRegistration() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.freeze();

		try {
			catalog.register(echoTool());
			fail("Expected IllegalStateException after freeze.");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	/** freeze() then getTool/tools()/toolSchemas() all work. */
	public void testFreezeAllowsReads() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(echoTool());
		catalog.freeze();

		assertNotNull(catalog.getTool("echo"));
		assertEquals(1, catalog.tools().size());
		assertEquals(1, catalog.toolSchemas().size());
	}

	/** isFrozen() returns false before freeze(), true after. */
	public void testIsFrozenReflectsState() {
		ToolCatalog catalog = new ToolCatalog();
		assertFalse(catalog.isFrozen());

		catalog.freeze();
		assertTrue(catalog.isFrozen());
	}

	// ===== Execute (Dispatch) Contract =====

	/** Registered tool returns ToolResult -> ToolResultMessage with Completed state. */
	public void testExecuteSuccess() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(echoTool());

		ToolCall call = new ToolCall("call-1", "echo", "{\"msg\": \"hi\"}");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		assertEquals("call-1", result.toolCallId());
		assertEquals("echo", result.toolName());
		assertInstanceOf(ToolState.Completed.class, result.state());

		ToolState.Completed completed = (ToolState.Completed) result.state();
		assertEquals("Echo: hi", completed.result());
	}

	/** Tool name not registered -> Failed state with "Unknown tool" and available names. */
	public void testExecuteUnknownTool() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(echoTool());

		ToolCall call = new ToolCall("call-1", "noSuchTool", "{}");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		ToolState.Failed failed = (ToolState.Failed) result.state();
		assertTrue("Should mention 'Unknown tool', got: " + failed.error(),
			failed.error().contains("Unknown tool"));
		assertTrue("Should list available tool names, got: " + failed.error(),
			failed.error().contains("echo"));
	}

	/**
	 * A name the registry cannot resolve is reported as "no such tool" with the list of names the
	 * principal may actually call - never as a permission problem, and never disclosing the names of
	 * tools they may not use.
	 *
	 * <p>
	 * Under enforced tool access an unresolvable name has no tool group, so routing it through the
	 * permission check would answer every typo with "belongs to no tool group ... denied to
	 * everyone" and strip the hint the model corrects itself with.
	 * </p>
	 */
	public void testExecuteUnknownToolUnderRestrictedSecurity() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(echoTool());
		catalog.register(secretTool());

		ToolCall call = new ToolCall("call-1", "ecko", "{}");
		ToolResultMessage result = catalog.execute(call, restrictedTo("echo"));

		ToolState.Failed failed = (ToolState.Failed) result.state();
		assertTrue("Should mention 'Unknown tool', got: " + failed.error(),
			failed.error().contains("Unknown tool"));
		assertFalse("Must not be reported as a permission problem, got: " + failed.error(),
			failed.error().contains("Permission denied"));
		assertTrue("Should list the permitted tool, got: " + failed.error(),
			failed.error().contains("echo"));
		assertFalse("Must not list a tool the user may not call, got: " + failed.error(),
			failed.error().contains("secret"));
	}

	/** The same message, and the same filtering, for a catalog that grows through skills. */
	public void testDynamicCatalogUnknownToolListsPermittedTools() {
		ToolCatalog base = new ToolCatalog();
		base.register(echoTool());
		base.register(secretTool());
		DynamicToolCatalog catalog = new DynamicToolCatalog(base.freeze());

		ToolCall call = new ToolCall("call-1", "ecko", "{}");
		ToolResultMessage result = catalog.execute(call, restrictedTo("echo"));

		ToolState.Failed failed = (ToolState.Failed) result.state();
		assertTrue("Should mention 'Unknown tool', got: " + failed.error(),
			failed.error().contains("Unknown tool"));
		assertTrue("Should list the permitted tool, got: " + failed.error(),
			failed.error().contains("echo"));
		assertFalse("Must not list a tool the user may not call, got: " + failed.error(),
			failed.error().contains("secret"));
	}

	private static ToolContext restrictedTo(String permittedToolName) {
		SecurityContext security = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return permittedToolName.equals(tool.name());
			}

			@Override
			public AiSecurityMode securityMode() {
				return AiSecurityMode.ENFORCED;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
		return new ToolContext(security, ApprovalCallback.AUTO_APPROVE);
	}

	private static Tool secretTool() {
		return new Tool() {
			@Override
			public String name() {
				return "secret";
			}

			@Override
			public String description() {
				return "A tool the test user may not call.";
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema("secret", description(), "{\"type\":\"object\",\"properties\":{}}");
			}

			@Override
			public ToolMode mode() {
				return ToolMode.READ;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of("nope");
			}
		};
	}

	/** Malformed JSON arguments -> Failed state with "Invalid JSON". */
	public void testExecuteInvalidJson() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(echoTool());

		ToolCall call = new ToolCall("call-1", "echo", "not valid json {{");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		ToolState.Failed failed = (ToolState.Failed) result.state();
		assertTrue("Should mention 'Invalid JSON', got: " + failed.error(),
			failed.error().contains("Invalid JSON"));
		assertTrue("Never reached the tool body — must be synthetic.", failed.synthetic());
	}

	/**
	 * An {@code @ToolMethod} parameter that cannot be coerced from the LLM-supplied JSON value
	 * fails before the method is invoked -> Failed state is synthetic.
	 */
	public void testExecuteArgumentCoercionFailureIsSynthetic() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(new SampleToolProvider());

		ToolCall call = new ToolCall("call-1", "repeat", "{\"times\": \"not-a-number\"}");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		ToolState.Failed failed = (ToolState.Failed) result.state();
		assertTrue("Should mention the parameter, got: " + failed.error(),
			failed.error().contains("times"));
		assertTrue("Failed before the tool method was invoked — must be synthetic.",
			failed.synthetic());
	}

	/** Tool throws RuntimeException -> Failed state with "Tool 'x' failed: message". */
	public void testExecuteToolThrowsRuntime() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(throwingTool(new RuntimeException("boom")));

		ToolCall call = new ToolCall("call-1", "thrower", "{}");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		ToolState.Failed failed = (ToolState.Failed) result.state();
		assertTrue("Should wrap with tool name, got: " + failed.error(),
			failed.error().contains("Tool 'thrower' failed:"));
		assertTrue("Should contain original message, got: " + failed.error(),
			failed.error().contains("boom"));
	}

	/** Tool throws IllegalArgumentException -> Failed state with just the message. */
	public void testExecuteToolThrowsIllegalArgument() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(throwingTool(new IllegalArgumentException("bad arg")));

		ToolCall call = new ToolCall("call-1", "thrower", "{}");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		ToolState.Failed failed = (ToolState.Failed) result.state();
		assertEquals("bad arg", failed.error());
		assertFalse("The tool's own body ran and may have produced side effects — must not be synthetic.",
			failed.synthetic());
	}

	/** Tool throws CancellationException -> Failed state with "Tool execution cancelled." */
	public void testExecuteToolCancelled() {
		ToolCatalog catalog = new ToolCatalog();
		Tool cancelling = new Tool() {
			@Override public String name() { return "canceller"; }
			@Override public String description() { return "Cancels"; }
			@Override public ToolSchema schema() {
				return new ToolSchema("canceller", "Cancels", "{\"type\":\"object\",\"properties\":{}}");
			}
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
				throw new CancellationException("cancelled");
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
		catalog.register(cancelling);

		ToolCall call = new ToolCall("call-1", "canceller", "{}");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		ToolState.Failed failed = (ToolState.Failed) result.state();
		assertEquals("Tool execution cancelled.", failed.error());
	}

	/** Tool throws exception with null getMessage() -> Failed state uses class simple name. */
	public void testExecuteToolNullExceptionMessage() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(throwingTool(new NullPointerException()));

		ToolCall call = new ToolCall("call-1", "thrower", "{}");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		ToolState.Failed failed = (ToolState.Failed) result.state();
		assertTrue("Should contain class simple name, got: " + failed.error(),
			failed.error().contains("NullPointerException"));
	}

	/** ToolResult with summary -> ToolState.Completed carries it. */
	public void testExecutePropagatesSummary() {
		ToolCatalog catalog = new ToolCatalog();
		Tool withSummary = new Tool() {
			@Override public String name() { return "summarizer"; }
			@Override public String description() { return "Returns summary"; }
			@Override public ToolSchema schema() {
				return new ToolSchema("summarizer", "Returns summary", "{\"type\":\"object\",\"properties\":{}}");
			}
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
				return new ToolResult("full text", "short summary", -1);
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
		catalog.register(withSummary);

		ToolCall call = new ToolCall("call-1", "summarizer", "{}");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		ToolState.Completed completed = (ToolState.Completed) result.state();
		assertEquals("full text", completed.result());
		assertEquals("short summary", completed.summary());
	}

	/** ToolResult with content parts -> ToolState.Completed carries them as ContentParts. */
	public void testExecutePropagatesContentParts() {
		ToolCatalog catalog = new ToolCatalog();
		Tool multimodal = new Tool() {
			@Override public String name() { return "viewer"; }
			@Override public String description() { return "Returns an image"; }
			@Override public ToolSchema schema() {
				return new ToolSchema("viewer", "Returns an image", "{\"type\":\"object\",\"properties\":{}}");
			}
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
				return ToolResult.multimodal("A screenshot.", new ToolContent.Image("QQ==", "image/png"));
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
		catalog.register(multimodal);

		ToolResultMessage result = catalog.execute(new ToolCall("call-1", "viewer", "{}"),
			ToolContext.EMPTY);

		ToolState.Completed completed = (ToolState.Completed) result.state();
		assertEquals("The text projection stays authoritative.", "A screenshot.", completed.result());
		assertEquals(2, completed.content().size());
		assertEquals("A screenshot.", ((TextContent) completed.content().get(0)).text());
		ImageContent image = (ImageContent) completed.content().get(1);
		assertEquals("image/png", image.mediaType());
		assertEquals("data:image/png;base64,QQ==", image.url());
	}

	/** A text-only tool result carries no content parts, exactly as before. */
	public void testTextOnlyResultHasNoContentParts() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(echoTool());

		ToolResultMessage result = catalog.execute(new ToolCall("call-1", "echo", "{\"msg\":\"hi\"}"),
			ToolContext.EMPTY);

		assertTrue(((ToolState.Completed) result.state()).content().isEmpty());
	}

	/** An annotated method may return a {@link ToolResult} directly, content and all. */
	public void testAnnotatedMethodReturningToolResult() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(new SampleToolProvider());

		ToolResultMessage result = catalog.execute(new ToolCall("call-1", "show", "{}"),
			ToolContext.EMPTY);

		ToolState.Completed completed = (ToolState.Completed) result.state();
		assertEquals("A document.", completed.result());
		assertEquals(2, completed.content().size());
		assertTrue(completed.content().get(1) instanceof PdfContent);
	}

	/** ToolResult with truncationHint -> ToolState.Completed carries it. */
	public void testExecutePropagatesTruncationHint() {
		ToolCatalog catalog = new ToolCatalog();
		Tool withHint = new Tool() {
			@Override public String name() { return "hinter"; }
			@Override public String description() { return "Returns hint"; }
			@Override public ToolSchema schema() {
				return new ToolSchema("hinter", "Returns hint", "{\"type\":\"object\",\"properties\":{}}");
			}
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
				return new ToolResult("data", null, 500);
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
		catalog.register(withHint);

		ToolCall call = new ToolCall("call-1", "hinter", "{}");
		ToolResultMessage result = catalog.execute(call, ToolContext.EMPTY);

		ToolState.Completed completed = (ToolState.Completed) result.state();
		assertEquals(500, completed.truncationHint());
	}

	// ===== Schema Filtering =====

	/** Register READ + STRUCTURAL tools, filter by mode -> only matching schemas returned. */
	public void testToolSchemasWithFilter() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(echoTool()); // ToolMode.READ
		catalog.register(structuralTool()); // ToolMode.STRUCTURAL

		List<ToolSchema> readOnly = catalog.toolSchemas(t -> t.mode() == ToolMode.READ);
		assertEquals(1, readOnly.size());
		assertEquals("echo", readOnly.get(0).name());

		List<ToolSchema> structural = catalog.toolSchemas(t -> t.mode() == ToolMode.STRUCTURAL);
		assertEquals(1, structural.size());
		assertEquals("modifier", structural.get(0).name());

		List<ToolSchema> all = catalog.toolSchemas();
		assertEquals(2, all.size());
	}

	// ===== Helpers =====

	private static void assertInstanceOf(Class<?> expected, Object actual) {
		assertNotNull("Expected non-null instance of " + expected.getSimpleName(), actual);
		assertTrue("Expected instance of " + expected.getSimpleName()
				+ " but got " + actual.getClass().getSimpleName(),
			expected.isInstance(actual));
	}

	private static Tool echoTool() {
		return new Tool() {
			@Override public String name() { return "echo"; }
			@Override public String description() { return "Echoes input"; }
			@Override public ToolSchema schema() {
				return new ToolSchema("echo", "Echoes input",
					"{\"type\":\"object\",\"properties\":{\"msg\":{\"type\":\"string\"}}}");
			}
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
				return ToolResult.of("Echo: " + args.get("msg"));
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
	}

	private static Tool structuralTool() {
		return new Tool() {
			@Override public String name() { return "modifier"; }
			@Override public String description() { return "Modifies structure"; }
			@Override public ToolSchema schema() {
				return new ToolSchema("modifier", "Modifies structure",
					"{\"type\":\"object\",\"properties\":{}}");
			}
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
				return ToolResult.of("done");
			}
			@Override public ToolMode mode() { return ToolMode.STRUCTURAL; }
		};
	}

	private static Tool throwingTool(RuntimeException ex) {
		return new Tool() {
			@Override public String name() { return "thrower"; }
			@Override public String description() { return "Always throws"; }
			@Override public ToolSchema schema() {
				return new ToolSchema("thrower", "Always throws",
					"{\"type\":\"object\",\"properties\":{}}");
			}
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
				throw ex;
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
	}

	private static Tool invalidSchemaTool() {
		return new Tool() {
			@Override public String name() { return "badSchema"; }
			@Override public String description() { return "Bad schema"; }
			@Override public ToolSchema schema() {
				return new ToolSchema("badSchema", "Bad schema", "not valid json {{");
			}
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
				return ToolResult.of("unreachable");
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
	}

	// ----- Collision test helper classes (from former TestToolCatalogCollision) -----

	private static class FirstTool implements Tool {
		@Override public String name() { return "search"; }
		@Override public String description() { return "first"; }
		@Override public ToolSchema schema() { return new ToolSchema("search", "first", "{}"); }
		@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
			return ToolResult.of("");
		}
		@Override public ToolMode mode() { return ToolMode.READ; }
	}

	private static class SecondTool implements Tool {
		@Override public String name() { return "search"; }
		@Override public String description() { return "second"; }
		@Override public ToolSchema schema() { return new ToolSchema("search", "second", "{}"); }
		@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
			return ToolResult.of("");
		}
		@Override public ToolMode mode() { return ToolMode.READ; }
	}

	// ----- Annotated tool provider for testRegisterAnnotatedProvider -----

	/** Sample provider with a {@link ToolMethod}-annotated method. */
	public static class SampleToolProvider {
		@ToolMethod(name = "greet", description = "Says hello", mode = ToolMode.READ)
		public String greet(@P("Name to greet") String name) {
			return "Hello, " + name;
		}

		@ToolMethod(name = "repeat", description = "Repeats a marker", mode = ToolMode.READ)
		public String repeat(@P("Number of repetitions") int times) {
			return "x".repeat(times);
		}

		@ToolMethod(name = "show", description = "Shows a document", mode = ToolMode.READ)
		public ToolResult show() {
			return ToolResult.multimodal("A document.",
				new ToolContent.Document("JVBERi0=", "application/pdf"));
		}
	}
}
