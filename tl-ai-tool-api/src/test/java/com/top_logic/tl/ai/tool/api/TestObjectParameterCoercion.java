/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.model.TLObject;

/**
 * Tests that a Java tool declaring a model-typed parameter receives the object, resolved under the
 * read rights of the run's principal - the same thing a configured script tool has always done
 * (Gitea #381).
 */
public class TestObjectParameterCoercion extends TestCase {

	/** A tool whose parameters carry objects rather than their identifiers. */
	public static class ObjectTools {

		// Marking is off here so that these tests assert the coerced arguments and nothing else;
		// what the framework puts around an answer is TestAnnotatedToolMarking's subject.

		@ToolMethod(description = "Reports what it was handed", mode = ToolMode.READ,
			marking = AnswerMarking.NONE)
		public String inspect(@P("the invoice") TLObject invoice) {
			return "got " + label(invoice);
		}

		@ToolMethod(description = "Reports what it was handed", mode = ToolMode.READ,
			marking = AnswerMarking.NONE)
		public String inspectAll(List<TLObject> invoices) {
			return "got " + invoices.size();
		}
	}

	/** A context that resolves the identifiers of the objects it was given and no others. */
	private static final class StubContext implements ToolExecutionContext {

		private final Map<String, TLObject> _objects;

		StubContext(Map<String, TLObject> objects) {
			_objects = objects;
		}

		@Override
		public ProgressReporter progressReporter() {
			return ProgressReporter.NOOP;
		}

		@Override
		public CancellationToken cancellationToken() {
			return CancellationToken.UNCANCELLABLE;
		}

		@Override
		public TLObject resolveObject(String id) {
			TLObject result = _objects.get(id);
			if (result == null) {
				// The one answer for "no such object" and "not yours to see", as TidResolver gives it.
				throw new IllegalArgumentException("Object with TID '" + id + "' not found");
			}
			return result;
		}
	}

	/**
	 * The model is asked for an identifier, not for an object it cannot write.
	 */
	public void testParameterIsDeclaredAsAnIdentifier() {
		String schema = tool("inspect").schema().inputSchemaJson();

		assertTrue("A model object travels as a string: " + schema,
			schema.contains("\"invoice\":{\"type\":\"string\""));
		assertTrue("...and the model is told what the string is: " + schema,
			schema.contains("TypeName:id"));
		assertTrue("The author's own words are kept: " + schema,
			schema.contains("the invoice"));
	}

	/**
	 * The identifier reaches the method as the object.
	 */
	public void testIdentifierIsResolved() {
		TLObject invoice = object();
		ToolResult result = tool("inspect").execute(Map.of("invoice", "Invoice:1"),
			new StubContext(Map.of("Invoice:1", invoice)));

		assertEquals("got " + label(invoice), result.text());
	}

	/**
	 * A list of identifiers reaches the method as a list of objects.
	 */
	public void testListOfIdentifiersIsResolved() {
		ToolResult result = tool("inspectAll").execute(
			Map.of("invoices", List.of("Invoice:1", "Invoice:2")),
			new StubContext(Map.of("Invoice:1", object(), "Invoice:2", object())));

		assertEquals("got 2", result.text());
	}

	/**
	 * An identifier that names nothing - or names something the principal may not read, which is
	 * spelled the same way - fails before the method runs, so the turn's writes survive.
	 */
	public void testUnknownIdentifierFailsWithoutRunningTheTool() {
		try {
			tool("inspect").execute(Map.of("invoice", "Invoice:404"), new StubContext(Map.of()));
			fail("An identifier that names nothing must be refused.");
		} catch (ToolArgumentCoercionException expected) {
			assertTrue("The reason reaches the model: " + expected.getMessage(),
				expected.getMessage().contains("Invoice:404"));
			assertTrue("...and says what is wrong: " + expected.getMessage(),
				expected.getMessage().contains("not found"));
		}
	}

	/**
	 * A caller that already holds the object passes it - TL-Script's {@code aiCallTool} does - and
	 * nothing is resolved or checked again.
	 */
	public void testAnObjectPassesThroughUnresolved() {
		TLObject invoice = object();

		ToolResult result = tool("inspect").execute(Map.of("invoice", invoice), null);

		assertEquals("got " + label(invoice), result.text());
	}

	/**
	 * A call with no run to ask is a programming error and says so, rather than handing the method a
	 * string where it declared an object.
	 */
	public void testWithoutAContextTheCallIsRefused() {
		try {
			tool("inspect").execute(Map.of("invoice", "Invoice:1"), null);
			fail("Resolving an identifier is a read check and needs a principal.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage().contains("read rights"));
		}
	}

	/**
	 * A context that belongs to no run refuses rather than resolving unchecked.
	 */
	public void testAContextWithoutARunRefuses() {
		ToolExecutionContext noRun = new ToolExecutionContext() {
			@Override
			public ProgressReporter progressReporter() {
				return ProgressReporter.NOOP;
			}

			@Override
			public CancellationToken cancellationToken() {
				return CancellationToken.UNCANCELLABLE;
			}
		};

		try {
			tool("inspect").execute(Map.of("invoice", "Invoice:1"), noRun);
			fail("A context with no principal must not resolve an identifier.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage().contains("names no principal"));
		}
	}

	private static Tool tool(String name) {
		for (Tool tool : AnnotatedTools.from(new ObjectTools())) {
			if (tool.name().equals(name)) {
				return tool;
			}
		}
		throw new AssertionError("No tool '" + name + "'.");
	}

	/** A stand-in object; these tests are about the plumbing, not about the engine's model. */
	private static TLObject object() {
		return (TLObject) Proxy.newProxyInstance(TestObjectParameterCoercion.class.getClassLoader(),
			new Class<?>[] { TLObject.class },
			(proxy, method, args) -> {
				if ("toString".equals(method.getName())) {
					return "object-" + System.identityHashCode(proxy);
				}
				if ("hashCode".equals(method.getName())) {
					return Integer.valueOf(System.identityHashCode(proxy));
				}
				if ("equals".equals(method.getName())) {
					return Boolean.valueOf(proxy == args[0]);
				}
				return null;
			});
	}

	private static String label(TLObject object) {
		return String.valueOf(object);
	}

}
