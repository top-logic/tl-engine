/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.security.PreCommitGuard.Violation;
import com.top_logic.tl.ai.core.security.ToolChangeSet.Creation;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Tests that {@link ModelSecurityPreCommitGuard} asks {@link ModelSecurity} the right question per
 * change, and reports what it found without quoting the data.
 *
 * <p>
 * {@link ModelSecurity} is a recording stub: what is under test is which question is asked for a
 * creation, an update and a deletion - not the engine's role evaluation, which
 * {@link TestModelAccessRightsSecurity} and the engine's own tests cover. No booted application is
 * needed, so these run in the plain unit test suite; the counterpart against a real fixture is
 * {@code SecDemoPreCommitGuardTest} in {@code tl-ai-demo}.
 * </p>
 */
public class TestPreCommitGuard extends TestCase {

	/** Grants a fixed set of questions, denies everything else, and records what it was asked. */
	private static class StubSecurity implements ModelSecurity {

		private final Set<String> _granted;

		final List<String> _asked = new ArrayList<>();

		StubSecurity(Set<String> granted) {
			_granted = granted;
		}

		private boolean answer(String question) {
			_asked.add(question);
			return _granted.contains(question);
		}

		@Override
		public boolean canRead(TLObject obj) {
			return answer("read:" + obj);
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return answer("read:" + obj + "." + attribute);
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return answer("write:" + obj);
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return answer("write:" + obj + "." + attribute);
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return answer("delete:" + obj);
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return answer("create:" + type + "@" + context);
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute) {
			return answer("create:" + parent + "/" + compositionAttribute);
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return answer(operation.getID() + ":" + obj);
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return List.copyOf(objects);
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return Set.of();
		}
	}

	private static final TLClass NOTE = named(TLClass.class, "pm:Ticket");

	private static final TLClass SECRET = named(TLClass.class, "pm:Project");

	private static final TLObject PROJECT = named(TLObject.class, "project");

	private static final TLObject NOTE1 = instanceOf(NOTE, "note1");

	private static final TLObject SECRET1 = instanceOf(SECRET, "secret1");

	private static final TLStructuredTypePart NOTES = named(TLStructuredTypePart.class, "notes");

	private static final TLStructuredTypePart TITLE = named(TLStructuredTypePart.class, "title");

	private static final TLStructuredTypePart VALUE = named(TLStructuredTypePart.class, "value");

	/** A turn that writes an object the principal may write commits: no violation. */
	public void testPermittedUpdatePasses() {
		StubSecurity model = new StubSecurity(Set.of("write:note1", "write:note1.title"));

		List<Violation> violations = check(model,
			new ToolChangeSet(List.of(), Map.of(NOTE1, Set.of(TITLE)), List.of()));

		assertEquals(List.of(), violations);
		assertEquals("Both the object and the touched attribute must be asked about.",
			List.of("write:note1", "write:note1.title"), model._asked);
	}

	/** A turn that writes an object the principal may not write is refused. */
	public void testForbiddenUpdateIsAViolation() {
		StubSecurity model = new StubSecurity(Set.of());

		List<Violation> violations = check(model,
			new ToolChangeSet(List.of(), Map.of(SECRET1, Set.of(VALUE)), List.of()));

		assertEquals(1, violations.size());
		assertEquals(SimpleBoundCommandGroup.WRITE, violations.get(0).operation());
		assertSame(SECRET1, violations.get(0).object());
		assertNull("The object as a whole was refused, not one attribute of it.", violations.get(0).attribute());
	}

	/**
	 * An attribute the principal may not write is refused even when the object as a whole is
	 * writeable - model security is attribute-granular.
	 */
	public void testForbiddenAttributeIsAViolation() {
		StubSecurity model = new StubSecurity(Set.of("write:note1"));

		List<Violation> violations = check(model,
			new ToolChangeSet(List.of(), Map.of(NOTE1, Set.of(TITLE)), List.of()));

		assertEquals(1, violations.size());
		assertEquals(SimpleBoundCommandGroup.WRITE, violations.get(0).operation());
		assertSame("The violation names the attribute it was refused for.", TITLE, violations.get(0).attribute());
	}

	/**
	 * A creation is validated as CREATE on the type in the committed container plus WRITE on the
	 * container's composition attribute - and the created object itself is never asked about.
	 *
	 * @see PreCommitGuard the known limit of this layer
	 */
	public void testCreationIsValidatedOnTypeAndContainer() {
		StubSecurity model = new StubSecurity(Set.of("create:pm:Ticket@project", "create:project/notes"));

		List<Violation> violations = check(model,
			new ToolChangeSet(List.of(new Creation(NOTE1, NOTE, PROJECT, NOTES)), Map.of(), List.of()));

		assertEquals(List.of(), violations);
		assertEquals(List.of("create:pm:Ticket@project", "create:project/notes"), model._asked);
	}

	/** A creation the principal has no CREATE right for is refused. */
	public void testForbiddenCreationIsAViolation() {
		StubSecurity model = new StubSecurity(Set.of("create:project/notes"));

		List<Violation> violations = check(model,
			new ToolChangeSet(List.of(new Creation(NOTE1, NOTE, PROJECT, NOTES)), Map.of(), List.of()));

		assertEquals(1, violations.size());
		assertEquals(SimpleBoundCommandGroup.CREATE, violations.get(0).operation());
		assertEquals("pm:Ticket", violations.get(0).typeName());
		assertEquals("Once the type is refused the container question is pointless.",
			List.of("create:pm:Ticket@project"), model._asked);
	}

	/**
	 * A creation whose type is permitted but whose target composition is not is refused as well -
	 * condition 2 of the engine's create check.
	 */
	public void testForbiddenCompositionIsAViolation() {
		StubSecurity model = new StubSecurity(Set.of("create:pm:Ticket@project"));

		List<Violation> violations = check(model,
			new ToolChangeSet(List.of(new Creation(NOTE1, NOTE, PROJECT, NOTES)), Map.of(), List.of()));

		assertEquals(1, violations.size());
		assertEquals(SimpleBoundCommandGroup.CREATE, violations.get(0).operation());
		assertEquals(List.of("create:pm:Ticket@project", "create:project/notes"), model._asked);
	}

	/** A top-level creation is checked against the global security root, with no composition. */
	public void testTopLevelCreationAsksOnlyForTheType() {
		StubSecurity model = new StubSecurity(Set.of("create:pm:Ticket@null"));

		List<Violation> violations = check(model,
			new ToolChangeSet(List.of(new Creation(NOTE1, NOTE, null, null)), Map.of(), List.of()));

		assertEquals(List.of(), violations);
		assertEquals(List.of("create:pm:Ticket@null"), model._asked);
	}

	/** A deletion is asked as DELETE, not derived from write. */
	public void testDeletionIsAskedAsDelete() {
		StubSecurity model = new StubSecurity(Set.of("write:note1"));

		List<Violation> violations = check(model,
			new ToolChangeSet(List.of(), Map.of(), List.of(NOTE1)));

		assertEquals(1, violations.size());
		assertEquals(SimpleBoundCommandGroup.DELETE, violations.get(0).operation());
		assertEquals(List.of("delete:note1"), model._asked);
	}

	/** An empty change set costs nothing and passes. */
	public void testEmptyChangeSetPasses() {
		StubSecurity model = new StubSecurity(Set.of());

		assertEquals(List.of(), check(model, ToolChangeSet.EMPTY));
		assertEquals(List.of(), model._asked);
	}

	/**
	 * What the model is told names the operation and the type, counts the objects, and quotes
	 * <b>no</b> value.
	 */
	public void testNoticeNamesTheOperationAndTypeButNoValue() {
		StubSecurity model = new StubSecurity(Set.of());
		TLObject secret2 = instanceOf(SECRET, "secret2");

		List<Violation> violations = check(model, new ToolChangeSet(List.of(),
			Map.of(SECRET1, Set.of(VALUE), secret2, Set.of(VALUE)), List.of()));
		String notice = PreCommitGuard.describe(violations);

		assertTrue("The notice must name the operation, but was: " + notice, notice.contains("Write"));
		assertTrue("The notice must name the type, but was: " + notice,
			notice.contains(String.valueOf(SECRET1.tType())));
		assertTrue("The notice must count the objects, but was: " + notice, notice.contains("2 objects"));
		assertFalse("The notice must not quote the attribute, but was: " + notice, notice.contains("value"));
		assertFalse("The notice must not name the objects, but was: " + notice, notice.contains("secret1"));
	}

	/** Each violation is recorded in the audit log, naming the operation and the type, never a value. */
	public void testEachViolationIsAudited() {
		StubSecurity model = new StubSecurity(Set.of());
		CollectingLogListener log = new CollectingLogListener(Set.of(Level.ERROR), true);
		try {
			check(model, new ToolChangeSet(List.of(), Map.of(SECRET1, Set.of(VALUE)), List.of(NOTE1)));
		} finally {
			log.deactivate();
		}

		List<String> audited = new ArrayList<>();
		for (LogEntry entry : log.getAndClearLogEntries()) {
			if (entry.getMessage().startsWith("Pre-commit check failed:")) {
				audited.add(entry.getMessage());
			}
		}
		assertEquals("One audit line per violation, but got: " + audited, 2, audited.size());
		assertTrue(audited.get(0).contains("operation='Write'"));
		assertTrue(audited.get(0).contains("type='" + SECRET1.tType() + "'"));
		assertTrue(audited.get(1).contains("operation='Delete'"));
		for (String line : audited) {
			assertFalse("The audit line must not quote the touched attribute: " + line,
				line.contains("value"));
		}
	}

	/**
	 * Every run gets the real guard.
	 *
	 * <p>
	 * There is no mode that skips this layer: a turn that opened a transaction is verified before it
	 * commits, whatever the run is.
	 * </p>
	 */
	public void testEveryRunGetsTheModelSecurityGuard() {
		PreCommitGuard guard = PreCommitGuard.forRun();

		assertTrue(guard.enforces());
		assertTrue(guard instanceof ModelSecurityPreCommitGuard);
	}

	/**
	 * {@link PreCommitGuard#PERMISSIVE} accepts every change.
	 *
	 * <p>
	 * The test seam, and no longer anything {@link PreCommitGuard#forRun} can answer with.
	 * </p>
	 */
	public void testPermissiveAcceptsEverything() {
		assertFalse(PreCommitGuard.PERMISSIVE.enforces());
		assertEquals(List.of(), PreCommitGuard.PERMISSIVE.check(context(),
			new ToolChangeSet(List.of(new Creation(NOTE1, NOTE, PROJECT, NOTES)),
				Map.of(SECRET1, Set.of(VALUE)), List.of(NOTE1))));
	}

	private static List<Violation> check(ModelSecurity model, ToolChangeSet changes) {
		return new ModelSecurityPreCommitGuard().check(context(model), changes);
	}

	private static SecurityContext context() {
		return context(ModelSecurity.UNCHECKED);
	}

	private static SecurityContext context(ModelSecurity model) {
		return new SecurityContext() {

			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return model;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
	}

	/**
	 * A stand-in for an instance of the given type, identified by name.
	 */
	private static TLObject instanceOf(TLClass type, String name) {
		return (TLObject) Proxy.newProxyInstance(TLObject.class.getClassLoader(),
			new Class<?>[] { TLObject.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "tType" -> type;
				case "toString" -> name;
				case "hashCode" -> name.hashCode();
				case "equals" -> proxy == args[0];
				default -> null;
			});
	}

	/**
	 * A stand-in for a model element that is only ever passed through and identified by name.
	 */
	private static <T> T named(Class<T> type, String name) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
			(proxy, method, args) -> switch (method.getName()) {
				case "toString" -> name;
				case "hashCode" -> name.hashCode();
				case "equals" -> proxy == args[0];
				default -> null;
			}));
	}
}
