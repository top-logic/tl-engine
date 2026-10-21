/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundRole;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Test case for {@link TLContextManager#inPersonContext(Person, com.top_logic.basic.thread.InContext)}
 * in combination with {@link ThreadContext#inSystemContext(Class, com.top_logic.basic.thread.InContext)}
 * and {@link ThreadContext#withoutSecurityChecks(com.top_logic.basic.thread.InContext)}.
 */
public class TestPersonContext extends BasicTestCase {

	private Person _person;

	private SimpleBoundObject _object;

	private Set<BoundRole> _roles;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_person = TestPerson.createPerson("personContextUser");
		_object = new SimpleBoundObject("personContextObject");
		_roles = Set.of(new SimpleBoundRole("personContextRole"));
	}

	@Override
	protected void tearDown() throws Exception {
		TestPerson.deletePersonAndUser(_person);
		_person = null;
		_object = null;
		_roles = null;
		super.tearDown();
	}

	/**
	 * Tests that a person context inside a system context sets the person on the current
	 * sub-session and switches the access checks on, and that the system context is restored
	 * afterwards.
	 */
	public void testPersonContextInSystemContext() {
		assertSystemContext();
		TLContext outer = TLContext.getContext();
		String systemContextId = outer.getContextId();

		TLContextManager.inPersonContext(_person, () -> {
			assertSame("The sub-session is kept.", outer, TLContext.getContext());
			assertSame(_person, TLContext.currentUser());
			assertEquals(TLSessionContext.contextId(_person), outer.getContextId());
			assertFalse(ThreadContext.isSecurityBypassed());
			assertFalse("A person without administrative rights is no administrator.", ThreadContext.isAdmin());
			assertFalse(ThreadContext.isSystemContext());
		});

		assertSame(outer, TLContext.getContext());
		assertEquals(systemContextId, outer.getContextId());
		assertSystemContext();
	}

	/**
	 * Tests that the system context is restored when the job of a person context fails.
	 */
	public void testPersonContextRestoredOnFailure() {
		TLContext outer = TLContext.getContext();
		String systemContextId = outer.getContextId();
		RuntimeException failure = new RuntimeException("Expected failure.");
		try {
			TLContextManager.<Void, RuntimeException, RuntimeException> inPersonContext(_person, () -> {
				throw failure;
			});
			fail("Failure expected.");
		} catch (RuntimeException ex) {
			assertSame(failure, ex);
		}

		assertSame(outer, TLContext.getContext());
		assertEquals(systemContextId, outer.getContextId());
		assertSystemContext();
	}

	/**
	 * Tests that a system context inside a person context removes the person from the current
	 * sub-session and switches the access checks off, and that the person context is restored
	 * afterwards, also when the job fails.
	 */
	public void testSystemContextInPersonContext() {
		TLContextManager.inPersonContext(_person, () -> {
			TLContext personContext = TLContext.getContext();
			ThreadContext.inSystemContext(TestPersonContext.class, () -> {
				assertSame("The sub-session is kept.", personContext, TLContext.getContext());
				assertSystemContext();
				assertTrue(ThreadContext.isAdmin());
				assertTrue(AccessManager.getInstance().hasRole(_object, _roles));
			});

			assertPersonContextRestored(personContext);

			RuntimeException failure = new RuntimeException("Expected failure.");
			try {
				ThreadContext.<Void, RuntimeException, RuntimeException> inSystemContext(TestPersonContext.class,
					() -> {
						throw failure;
					});
				fail("Failure expected.");
			} catch (RuntimeException ex) {
				assertSame(failure, ex);
			}
			assertPersonContextRestored(personContext);
		});
	}

	private void assertPersonContextRestored(TLContext personContext) {
		assertSame(personContext, TLContext.getContext());
		assertSame(_person, TLContext.currentUser());
		assertEquals(TLSessionContext.contextId(_person), personContext.getContextId());
		assertFalse(ThreadContext.isSecurityBypassed());
		assertFalse(ThreadContext.isSystemContext());
		assertFalse(AccessManager.getInstance().hasRole(_object, _roles));
	}

	/**
	 * Tests that {@link ThreadContext#withoutSecurityChecks(com.top_logic.basic.thread.InContext)}
	 * inside a person context keeps the person and allows every access during its job only.
	 */
	public void testWithoutSecurityChecksInPersonContext() {
		TLContextManager.inPersonContext(_person, () -> {
			assertDenied();

			ThreadContext.withoutSecurityChecks(() -> {
				assertSame(_person, TLContext.currentUser());
				assertTrue(ThreadContext.isSecurityBypassed());
				assertTrue(AccessManager.getInstance().hasRole(_object, _roles));
				assertEquals(Boolean.TRUE,
					BoundChecker.isAllowedBypass(_person, SimpleBoundCommandGroup.READ));
			});

			assertDenied();
		});
	}

	/**
	 * Tests that a person context without a person denies every access.
	 */
	public void testNoPersonDenied() {
		TLContextManager.inPersonContext(null, () -> {
			assertNull(TLContext.currentUser());
			assertFalse(ThreadContext.isSecurityBypassed());
			assertFalse(ThreadContext.isAdmin());
			assertFalse(AccessManager.getInstance().hasRole(_object, _roles));
			assertEquals(Boolean.FALSE, BoundChecker.isAllowedBypass(null, SimpleBoundCommandGroup.READ));
		});
	}

	/**
	 * Tests that {@link ThreadContext#isAdmin()} holds while the security checks are bypassed, and
	 * for a person context of an administrator also while the security checks are on.
	 */
	public void testAdminContext() {
		TLContextManager.inPersonContext(_person, () -> {
			assertFalse(ThreadContext.isAdmin());
			ThreadContext.withoutSecurityChecks(() -> {
				assertTrue("Bypassed security checks give administrative rights.", ThreadContext.isAdmin());
				ThreadContext.withSecurityChecks(true, () -> assertFalse(ThreadContext.isAdmin()));
			});
			assertFalse(ThreadContext.isAdmin());
		});

		Person root = PersonManager.getManager().getRoot();
		assertTrue(Person.isAdmin(root));
		TLContextManager.inPersonContext(root, () -> {
			assertFalse(ThreadContext.isSecurityBypassed());
			assertTrue("An administrator has administrative rights.", ThreadContext.isAdmin());
		});
	}

	private void assertDenied() {
		assertSame(_person, TLContext.currentUser());
		assertFalse(ThreadContext.isSecurityBypassed());
		assertFalse("The person has no role on the object.", AccessManager.getInstance().hasRole(_object, _roles));
		assertNull("The access of the person is decided by its roles.",
			BoundChecker.isAllowedBypass(_person, SimpleBoundCommandGroup.READ));
	}

	private static void assertSystemContext() {
		assertNull(TLContext.currentUser());
		assertTrue(ThreadContext.isSecurityBypassed());
		assertTrue(ThreadContext.isSystemContext());
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(TestPersonContext.class, AccessManager.Module.INSTANCE));
	}

}
