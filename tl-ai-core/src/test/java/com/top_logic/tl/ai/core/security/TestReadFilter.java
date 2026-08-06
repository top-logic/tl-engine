/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ProgressReporter;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * Tests the traversal and the omission rules of {@link ReadFilter}.
 *
 * <p>
 * {@link ModelSecurity} is a stub that reads a fixed set of names, because what is under test is what
 * the filter does with the answers - which object contributes nothing, which attribute is left out,
 * how deep the filtering goes - and not the engine's role evaluation, which
 * {@link TestModelAccessRightsSecurity} and the engine's own tests cover.
 * </p>
 *
 * <p>
 * Nothing here renders a label: {@code MetaLabelProvider} needs a booted application, so the
 * disclosed identity of a <em>readable</em> object is asserted in {@code tl-ai-demo}'s
 * {@code SecDemoReadFilterTest}, against real instances and two real personas.
 * </p>
 */
public class TestReadFilter extends TestCase {

	/** Reads exactly the objects and attributes whose name is in the granted set. */
	private static class StubSecurity implements ModelSecurity {

		private final Set<String> _readable;

		StubSecurity(Set<String> readable) {
			_readable = readable;
		}

		@Override
		public boolean canRead(TLObject obj) {
			return _readable.contains(String.valueOf(obj));
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return _readable.contains(obj + "." + attribute);
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return false;
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return false;
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return false;
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute) {
			return false;
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return false;
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			List<T> result = new ArrayList<>(objects.size());
			for (T obj : objects) {
				if (canRead(obj)) {
					result.add(obj);
				}
			}
			return result;
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return Set.of();
		}
	}

	private static final TLStructuredTypePart TITLE = named(TLStructuredTypePart.class, "title");

	private static final TLStructuredTypePart SALARY = named(TLStructuredTypePart.class, "salary");

	private static final TLObject VISIBLE = object("visible");

	private static final TLObject HIDDEN = object("hidden");

	private static ReadFilter filter(String... readable) {
		return ReadFilter.forSecurity(new StubSecurity(Set.of(readable)));
	}

	/** A readable object survives the filter; an unreadable one becomes nothing at all. */
	public void testTopLevelObject() {
		ReadFilter filter = filter("visible");

		assertSame(VISIBLE, filter.visible(VISIBLE));
		assertNull(filter.visible(HIDDEN));
		assertNull("An object the principal may not read discloses no identity, not even an empty one.",
			filter.disclose(HIDDEN));
	}

	/**
	 * An unreadable object is <em>omitted</em> from a collection rather than kept as a placeholder: a
	 * placeholder would still disclose that it exists and how many there are.
	 */
	public void testUnreadableElementIsOmittedNotNulled() {
		Object result = filter("visible").visible(Arrays.asList(VISIBLE, HIDDEN));

		assertEquals(List.of(VISIBLE), result);
	}

	/** A collection nothing had to be removed from is returned unchanged, by identity. */
	public void testUntouchedCollectionIsTheSameInstance() {
		List<TLObject> input = List.of(VISIBLE);

		assertSame(input, filter("visible").visible(input));
	}

	/** A null element was not put there by the filter, so it stays. */
	public void testOriginalNullSurvives() {
		Object result = filter("visible").visible(Arrays.asList(VISIBLE, null, HIDDEN));

		assertEquals(Arrays.asList(VISIBLE, null), result);
	}

	/** The shape of a set is kept, so a caller that relied on uniqueness still gets it. */
	public void testSetStaysASet() {
		Object result = filter("visible").visible(new LinkedHashSet<>(Arrays.asList(VISIBLE, HIDDEN)));

		assertTrue(String.valueOf(result), result instanceof Set<?>);
		assertEquals(Set.of(VISIBLE), result);
	}

	/** Filtering reaches into nested collections, not only the top level. */
	public void testNestingIsFiltered() {
		Object result = filter("visible").visible(
			List.of(List.of(VISIBLE, HIDDEN), List.of(HIDDEN)));

		assertEquals(List.of(List.of(VISIBLE), List.of()), result);
	}

	/** An array is filtered too, and becomes the sequence both renderings treat it as. */
	public void testArrayIsFiltered() {
		Object result = filter("visible").visible(new Object[] { VISIBLE, HIDDEN });

		assertEquals(List.of(VISIBLE), result);
	}

	/** A map entry whose value is an object the principal may not read goes as a whole. */
	public void testEntryWithHiddenValueIsDropped() {
		Map<String, Object> input = new LinkedHashMap<>();
		input.put("owner", HIDDEN);
		input.put("deputy", VISIBLE);
		input.put("count", 2);

		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put("deputy", VISIBLE);
		expected.put("count", 2);

		assertEquals(expected, filter("visible").visible(input));
	}

	/** So does an entry keyed by one: the key itself would disclose the object. */
	public void testEntryWithHiddenKeyIsDropped() {
		Map<Object, Object> input = new LinkedHashMap<>();
		input.put(HIDDEN, "secret budget");
		input.put(VISIBLE, "public budget");

		assertEquals(Map.of(VISIBLE, "public budget"), filter("visible").visible(input));
	}

	/** A value inside a map entry is filtered without losing the entry. */
	public void testCollectionInsideAMapEntryIsFiltered() {
		Object result = filter("visible").visible(Map.of("members", List.of(VISIBLE, HIDDEN)));

		assertEquals(Map.of("members", List.of(VISIBLE)), result);
	}

	/** Values that are not model data pass through untouched. */
	public void testPrimitivesPassThrough() {
		ReadFilter filter = filter();

		assertEquals("text", filter.visible("text"));
		assertEquals(Integer.valueOf(3), filter.visible(3));
		assertEquals(Boolean.TRUE, filter.visible(true));
		assertNull(filter.visible(null));
	}

	/**
	 * A count is a number by the time it arrives, so the filter cannot touch it. Stated as a test
	 * because it is the accepted boundary of the guarantee (issue #291), not an oversight.
	 */
	public void testAggregatesAreNotFiltered() {
		assertEquals("The read filter cannot filter a cardinality; per-object filtering is what it does.",
			Integer.valueOf(99), filter().visible(99));
	}

	/** An attribute the principal may not read is absent, and the object still renders. */
	public void testPerAttributeReadIsRespected() {
		TLObject employee = instance("employee", TITLE, SALARY);
		ReadFilter filter = filter("employee", "employee.title");

		Map<String, Object> attributes = filter.attributesOf(employee);

		assertEquals(Set.of("title"), attributes.keySet());
		assertEquals("Chief", attributes.get("title"));
	}

	/** An object the principal may not read reports nothing, not an empty attribute map. */
	public void testUnreadableObjectHasNoAttributes() {
		TLObject employee = instance("employee", TITLE, SALARY);

		assertNull(filter("employee.title").attributesOf(employee));
	}

	/** The text rendering never sees an object the principal may not read. */
	public void testTextOfHiddenObjectIsEmpty() {
		assertEquals("", filter("visible").text(HIDDEN));
	}

	/** A filter cannot be built without rights to check against. */
	public void testSecurityIsMandatory() {
		try {
			ReadFilter.forSecurity(null);
			fail("A read filter with no rights would disclose everything.");
		} catch (NullPointerException expected) {
			// Expected.
		}
	}

	/** A context that is not an agent run's cannot name a principal, so it is refused. */
	public void testContextWithoutARunIsRefused() {
		try {
			// A bare context, as a tool called outside an agent loop would get.
			ReadFilter.of(new ToolExecutionContext() {

				@Override
				public ProgressReporter progressReporter() {
					return ProgressReporter.NOOP;
				}

				@Override
				public CancellationToken cancellationToken() {
					return CancellationToken.UNCANCELLABLE;
				}
			});
			fail("A disclosure decision with no principal must not fall back to permissive.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("no run to ask"));
		}
	}

	/** {@link ReadFilter#readable(Collection)} is the pagination-safe entry point. */
	public void testReadableFiltersBeforeCounting() {
		List<TLObject> all = List.of(VISIBLE, HIDDEN, HIDDEN);

		List<TLObject> readable = filter("visible").readable(all);

		assertEquals(List.of(VISIBLE), readable);
		assertEquals("A total taken over the filtered list is the only honest one.", 1, readable.size());
	}

	/**
	 * A stand-in for a model object identified by its name.
	 */
	private static TLObject object(String name) {
		return (TLObject) Proxy.newProxyInstance(TLObject.class.getClassLoader(),
			new Class<?>[] { TLObject.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "toString" -> name;
				case "hashCode" -> name.hashCode();
				case "equals" -> proxy == args[0];
				default -> null;
			});
	}

	/**
	 * A stand-in for an object whose type has the given attributes, each holding a fixed value.
	 */
	private static TLObject instance(String name, TLStructuredTypePart... parts) {
		TLStructuredType type = named(TLStructuredType.class, name + "Type", Map.of(
			"getAllParts", List.of(parts)));
		return (TLObject) Proxy.newProxyInstance(TLObject.class.getClassLoader(),
			new Class<?>[] { TLObject.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "tType" -> type;
				case "tValue" -> args[0] == TITLE ? "Chief" : "120000";
				case "toString" -> name;
				case "hashCode" -> name.hashCode();
				case "equals" -> proxy == args[0];
				default -> null;
			});
	}

	private static <T> T named(Class<T> type, String name) {
		return named(type, name, Map.of());
	}

	/**
	 * A stand-in for a model element identified by its name, answering the given methods.
	 */
	private static <T> T named(Class<T> type, String name, Map<String, Object> answers) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
			(proxy, method, args) -> {
				if (answers.containsKey(method.getName())) {
					return answers.get(method.getName());
				}
				return switch (method.getName()) {
					case "getName", "toString" -> name;
					case "hashCode" -> name.hashCode();
					case "equals" -> proxy == args[0];
					default -> null;
				};
			}));
	}
}
