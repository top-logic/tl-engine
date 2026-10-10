/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Tests that {@link ModelAccessRightsSecurity} maps each façade method onto the right
 * {@link ModelAccessRights} question.
 *
 * <p>
 * The engine's rights service is replaced by a recording stub: what is under test is the mapping and
 * the filtering, not the engine's role evaluation (which has its own tests). No booted application is
 * needed, so these run in the plain unit test suite.
 * </p>
 */
public class TestModelAccessRightsSecurity extends TestCase {

	/** Grants a fixed set of (object, operation) pairs and denies everything else. */
	private static class StubRights implements ModelAccessRights {

		private final Set<String> _granted;

		private final Set<String> _grantedCreate;

		StubRights(Set<String> granted, Set<String> grantedCreate) {
			_granted = granted;
			_grantedCreate = grantedCreate;
		}

		@Override
		public boolean isAllowed(Person person, TLObject instance, BoundCommandGroup commandGroup) {
			return _granted.contains(key(instance, null, commandGroup));
		}

		@Override
		public boolean isAllowed(Person person, TLObject instance, TLStructuredTypePart attribute,
				BoundCommandGroup commandGroup) {
			return _granted.contains(key(instance, attribute, commandGroup));
		}

		@Override
		public boolean isAllowedCreate(Person person, TLObject parent, TLStructuredTypePart compositionAttribute,
				TLClass type) {
			return _grantedCreate.contains(parent + "/" + compositionAttribute + (type == null ? "" : ":" + type));
		}

		@Override
		public boolean isAllowedInitial(Person person, TLObject draft, TLStructuredTypePart attribute,
				BoundCommandGroup commandGroup) {
			return _granted.contains("initial:" + key(draft, attribute, commandGroup));
		}

		@Override
		public boolean hasGrant(TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isAllowedCreate(Person person, TLClass type, TLObject context) {
			return _grantedCreate.contains(type + "@" + context);
		}

		@Override
		public Set<TLClass> getAccessibleTypes(Person person, BoundCommandGroup commandGroup) {
			return Set.of();
		}

		@Override
		public Set<BoundedRole> getAllowedRoles(TLClass type, BoundCommandGroup commandGroup) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<BoundedRole> getAllowedRoles(TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
			throw new UnsupportedOperationException();
		}

		static String key(TLObject instance, TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
			return instance + (attribute == null ? "" : "." + attribute) + ":" + commandGroup.getID();
		}
	}

	private static final TLObject VISIBLE = named(TLObject.class, "visible");

	private static final TLObject HIDDEN = named(TLObject.class, "hidden");

	private static final TLStructuredTypePart SALARY = named(TLStructuredTypePart.class, "salary");

	/** Reads are asked as {@code READ} on the object. */
	public void testReadIsDelegated() {
		ModelSecurity security = security(Set.of(StubRights.key(VISIBLE, null, SimpleBoundCommandGroup.READ)));

		assertTrue(security.canRead(VISIBLE));
		assertFalse(security.canRead(HIDDEN));
	}

	/** Attribute reads are asked as {@code READ} on the (object, attribute) pair. */
	public void testAttributeReadIsDelegated() {
		ModelSecurity security = security(Set.of(
			StubRights.key(VISIBLE, null, SimpleBoundCommandGroup.READ),
			StubRights.key(VISIBLE, SALARY, SimpleBoundCommandGroup.READ)));

		assertTrue(security.canRead(VISIBLE, SALARY));
		assertFalse(security.canRead(HIDDEN, SALARY));
	}

	/** Write and delete are asked as their own command groups, not derived from read. */
	public void testWriteAndDeleteAreSeparateQuestions() {
		ModelSecurity security = security(Set.of(
			StubRights.key(VISIBLE, null, SimpleBoundCommandGroup.READ),
			StubRights.key(VISIBLE, null, SimpleBoundCommandGroup.WRITE)));

		assertTrue(security.canWrite(VISIBLE));
		assertFalse("Read access must not imply delete access.", security.canDelete(VISIBLE));
	}

	/**
	 * A business operation beyond read/write/delete is forwarded as its own command group rather than
	 * decomposed into reads and writes.
	 */
	public void testCustomOperationIsDelegated() {
		BoundCommandGroup operation = SimpleBoundCommandGroup.EXPORT;
		ModelSecurity security = security(Set.of(
			StubRights.key(VISIBLE, null, operation),
			StubRights.key(HIDDEN, null, SimpleBoundCommandGroup.READ),
			StubRights.key(HIDDEN, null, SimpleBoundCommandGroup.WRITE)));

		assertTrue(security.canPerform(VISIBLE, operation));
		assertFalse("Read plus write must not imply a custom operation.",
			security.canPerform(HIDDEN, operation));
	}

	/** Creation goes through the engine's two-condition create check, not through write. */
	public void testCreateIsDelegated() {
		TLClass type = named(TLClass.class, "Contract");
		TLObject context = named(TLObject.class, "module");
		TLClass special = named(TLClass.class, "FrameContract");
		ModelSecurity security = new ModelAccessRightsSecurity(null,
			new StubRights(Set.of(), Set.of(type + "@" + context, VISIBLE + "/" + SALARY,
				HIDDEN + "/" + SALARY + ":" + special)));

		assertTrue(security.canCreate(type, context));
		assertFalse(security.canCreate(type, HIDDEN));
		assertTrue(security.canCreate(VISIBLE, SALARY, null));
		assertFalse(security.canCreate(HIDDEN, SALARY, null));
		assertTrue("The type of the new child is passed on, not replaced by the composition's target type.",
			security.canCreate(HIDDEN, SALARY, special));
	}

	/** A new object's initial values are asked as the engine's create-time {@code WRITE} question. */
	public void testInitialValueIsDelegated() {
		ModelSecurity security = security(Set.of(
			"initial:" + StubRights.key(VISIBLE, SALARY, SimpleBoundCommandGroup.WRITE),
			StubRights.key(HIDDEN, SALARY, SimpleBoundCommandGroup.WRITE)));

		assertTrue(security.canSetInitial(VISIBLE, SALARY));
		assertFalse("The write right on a committed object must not stand in for the create-time question.",
			security.canSetInitial(HIDDEN, SALARY));
	}

	/** Filtering keeps only readable objects, in input order. */
	public void testFilterReadableDropsUnreadable() {
		ModelSecurity security = security(Set.of(StubRights.key(VISIBLE, null, SimpleBoundCommandGroup.READ)));

		assertEquals(List.of(VISIBLE), security.filterReadable(List.of(HIDDEN, VISIBLE, HIDDEN)));
		assertEquals(List.of(), security.filterReadable(List.of(HIDDEN)));
	}

	/** A denied read raises a {@link ToolSecurityException} that names the operation. */
	public void testRequireReadDeniesWithoutLeakingTheValue() {
		ModelSecurity security = security(Set.of(StubRights.key(VISIBLE, null, SimpleBoundCommandGroup.READ)));

		security.requireRead(VISIBLE);
		try {
			security.requireRead(HIDDEN);
			fail("Expected the denied read to be refused.");
		} catch (ToolSecurityException ex) {
			assertEquals(SimpleBoundCommandGroup.READ, ex.operation());
			assertFalse("The denial must not quote the withheld object.",
				ex.getMessage().contains("hidden"));
		}
	}

	/** {@link ModelSecurity#UNCHECKED} is the today-behaviour implementation: it grants everything. */
	public void testUncheckedGrantsEverything() {
		ModelSecurity security = ModelSecurity.UNCHECKED;

		assertTrue(security.canRead(HIDDEN));
		assertTrue(security.canRead(HIDDEN, SALARY));
		assertTrue(security.canWrite(HIDDEN));
		assertTrue(security.canWrite(HIDDEN, SALARY));
		assertTrue(security.canDelete(HIDDEN));
		assertTrue(security.canCreate(HIDDEN, SALARY, null));
		assertTrue(security.canSetInitial(HIDDEN, SALARY));
		assertTrue(security.canPerform(HIDDEN, SimpleBoundCommandGroup.WRITE));
		assertEquals(List.of(HIDDEN, VISIBLE), security.filterReadable(List.of(HIDDEN, VISIBLE)));
		security.requireRead(HIDDEN);
		security.requireWrite(HIDDEN);
	}

	private static ModelSecurity security(Set<String> granted) {
		return new ModelAccessRightsSecurity(null, new StubRights(granted, Set.of()));
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
