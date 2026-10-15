/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.thread;


import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.EmptyStackException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.CustomPropertiesSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.thread.UnboundListener;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test the {@link ThreadContext}.
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestThreadContext extends TestCase {

	private static final String USER_CONTEXT_ID = SessionContext.PERSON_ID_PREFIX + "user";

    /** Default CTor, create a test with this Function Name */
	public TestThreadContext (String name) {
        super (name);
    }
	
	public void testCorrectImplementationClass() {
		ThreadContext newContext = ThreadContextManager.getManager().newSubSessionContext();
		assertInstanceof(newContext, ExampleContext.class);
	}

	/**
	 * Test handling of Events using a ThreadContextListener.
	 */
    public void testEvents () throws Exception {
		final TestedListener tl = new TestedListener();
		assertFalse(tl.unBound);

		ThreadContext.inSystemContext(TestThreadContext.class, new InContext() {
			@Override
			public void inContext() {
				ThreadContextManager.getInteraction().addUnboundListener(tl);
				assertFalse(tl.unBound);
			}
		});

		assertTrue(tl.unBound);
		tl.unBound = false;

		ThreadContext.inSystemContext(TestThreadContext.class, new InContext() {
			@Override
			public void inContext() {
				final InteractionContext interaction = ThreadContextManager.getInteraction();
				interaction.addUnboundListener(tl);

				final SubSessionContext subSession = ThreadContextManager.getSubSession();
				// noop, should be ignored
				ThreadContextManager.inContext(subSession, new InContext() {
					@Override
					public void inContext() {
						assertSame("No new InteractionContext must be installed when executing in same subsession.",
							interaction, ThreadContextManager.getInteraction());
						assertSame("No new SubSession must be installed when executing in same subsession.",
							subSession, ThreadContextManager.getSubSession());
						interaction.addUnboundListener(tl);
					}
				});
				assertFalse("Listener must not be called as no new interaction is installed", tl.unBound);
			}
		});

		assertTrue(tl.unBound);
    }
    
	/**
	 * A system context nested in another system context changes the context id of the current
	 * sub-session and restores it afterwards.
	 */
	public void testNestedSystemContext() {
		ThreadContext.inSystemContext(TestThreadContext.class, new InContext() {

			@Override
			public void inContext() {
				final ThreadContext currentContext = ThreadContext.getThreadContext();
				assertNotNull(currentContext);
				String outerContextId = currentContext.getContextId();
				ThreadContext.inSystemContext(ExampleContext.class, new InContext() {

					@Override
					public void inContext() {
						ThreadContext nested = ThreadContext.getThreadContext();
						assertSame("The sub-session is kept.", currentContext, nested);
						assertEquals(ThreadContextManager.systemContextId(ExampleContext.class), nested.getContextId());
						assertTrue(ThreadContext.isSecurityUnchecked());
					}
				});
				assertSame(currentContext, ThreadContext.getThreadContext());
				assertEquals(outerContextId, currentContext.getContextId());
				assertTrue(ThreadContext.isSecurityUnchecked());
			}
		});
	}

	/**
	 * Outside of any job, the security is checked.
	 */
	public void testSecurityCheckedOutsideJob() {
		assertNull(ThreadContextManager.getInteraction());
		assertFalse(ThreadContext.isSecurityUnchecked());
		assertFalse(ThreadContext.isAdmin());
		assertFalse(ThreadContext.isSystemContext());
	}

	/**
	 * A system context without a surrounding interaction creates an interaction of its own with
	 * unchecked security and removes it afterwards.
	 */
	public void testSystemContextWithoutInteraction() {
		String result = ThreadContext.inSystemContext(TestThreadContext.class, () -> {
			assertNotNull(ThreadContextManager.getInteraction());
			assertEquals(ThreadContextManager.systemContextId(TestThreadContext.class),
				ThreadContextManager.getSubSession().getContextId());
			assertTrue(ThreadContext.isSystemContext());
			assertTrue(ThreadContext.isSecurityUnchecked());
			assertTrue(ThreadContext.isAdmin());
			return "result";
		});
		assertEquals("result", result);

		assertNull(ThreadContextManager.getInteraction());
		assertFalse(ThreadContext.isSecurityUnchecked());
		assertFalse(ThreadContext.isAdmin());
	}

	/**
	 * A system context within an interaction of a user makes the user's sub-session a system
	 * context for the job and restores its identity and the security state afterwards.
	 */
	public void testSystemContextInsideInteraction() {
		ThreadContext userContext = newUserContext();
		ThreadContextManager.inContext(userContext, () -> {
			InteractionContext interaction = ThreadContextManager.getInteraction();
			SessionContext userSession = ThreadContextManager.getSession();
			assertSame(userContext, ThreadContextManager.getSubSession());
			assertFalse(ThreadContext.isSystemContext());
			assertFalse(ThreadContext.isSecurityUnchecked());

			int result = ThreadContext.inSystemContext(TestThreadContext.class, () -> {
				assertSame("No new interaction is created.", interaction, ThreadContextManager.getInteraction());
				SubSessionContext systemContext = ThreadContextManager.getSubSession();
				assertSame("The sub-session is kept.", userContext, systemContext);
				assertSame("The session is kept.", userSession, ThreadContextManager.getSession());
				assertEquals(ThreadContextManager.systemContextId(TestThreadContext.class),
					systemContext.getContextId());
				assertTrue(ThreadContext.isSystemContext());
				assertTrue(ThreadContext.isSecurityUnchecked());
				assertTrue(ThreadContext.isAdmin());
				return 42;
			});
			assertEquals(42, result);
			assertUserContextRestored(interaction, userContext, userSession);

			RuntimeException failure = new RuntimeException("failure in system context");
			try {
				ThreadContext.inSystemContext(TestThreadContext.class, new InContext() {
					@Override
					public void inContext() {
						assertTrue(ThreadContext.isSecurityUnchecked());
						throw failure;
					}
				});
				fail("Expected failure.");
			} catch (RuntimeException ex) {
				assertSame(failure, ex);
			}
			assertUserContextRestored(interaction, userContext, userSession);
		});
		assertNull(ThreadContextManager.getInteraction());
	}

	private static void assertUserContextRestored(InteractionContext interaction, ThreadContext userContext,
			SessionContext userSession) {
		assertSame(interaction, ThreadContextManager.getInteraction());
		assertSame(userContext, ThreadContextManager.getSubSession());
		assertSame(userSession, ThreadContextManager.getSession());
		assertEquals(USER_CONTEXT_ID, userContext.getContextId());
		assertFalse(ThreadContext.isSystemContext());
		assertFalse(ThreadContext.isSecurityUnchecked());
		assertFalse(ThreadContext.isAdmin());
	}

	/**
	 * Unchecked security keeps the current context, nests, and is restored also on failure.
	 */
	public void testUncheckedSecurity() {
		ThreadContext userContext = newUserContext();
		ThreadContextManager.inContext(userContext, () -> {
			assertFalse(ThreadContext.isSecurityUnchecked());
			assertFalse(ThreadContext.isAdmin());

			ThreadContext.uncheckedSecurity(() -> {
				assertSame("Unchecked security keeps the context.", userContext,
					ThreadContextManager.getSubSession());
				assertTrue(ThreadContext.isSecurityUnchecked());
				assertTrue(ThreadContext.isAdmin());

				boolean inner = ThreadContext.uncheckedSecurity(() -> ThreadContext.isSecurityUnchecked());
				assertTrue(inner);
				assertTrue("Nested job must not reset the outer state.", ThreadContext.isSecurityUnchecked());

				boolean checked =
					ThreadContext.withSecurityUnchecked(false, () -> ThreadContext.isSecurityUnchecked());
				assertFalse("Security can be switched on for a job.", checked);
				assertTrue(ThreadContext.isSecurityUnchecked());
			});
			assertFalse(ThreadContext.isSecurityUnchecked());

			RuntimeException failure = new RuntimeException("failure with unchecked security");
			try {
				ThreadContext.uncheckedSecurity(new InContext() {
					@Override
					public void inContext() {
						throw failure;
					}
				});
				fail("Expected failure.");
			} catch (RuntimeException ex) {
				assertSame(failure, ex);
			}
			assertFalse(ThreadContext.isSecurityUnchecked());
			assertFalse(ThreadContext.isAdmin());
		});
	}

	private static ThreadContext newUserContext() {
		ThreadContextManager manager = ThreadContextManager.getManager();
		ThreadContext userContext = manager.newSubSessionContext();
		userContext.setSessionContext(manager.newSessionContext());
		userContext.setContextId(USER_CONTEXT_ID);
		return userContext;
	}

    /** 
     * Test the SupeUrser methods 
     */
    public void testSuperUser () throws Exception {
       
        assertFalse(ThreadContext.isAdmin());
        ThreadContext.pushSuperUser();
        assertTrue (ThreadContext.isAdmin());
        assertTrue(ThreadContext.isSecurityUnchecked());
        ThreadContext.pushSuperUser();
        assertTrue (ThreadContext.isAdmin());
        ThreadContext.resetSuperUser();
        assertFalse(ThreadContext.isAdmin());
        try {
        	ThreadContext.popSuperUser();
        	fail("Expected EmptyStackException");
        } catch (EmptyStackException expected) { /* expected */ }
            
        assertFalse(ThreadContext.isAdmin());
        ThreadContext.pushSuperUser();
        assertTrue (ThreadContext.isAdmin());
        ThreadContext.pushSuperUser();
        assertTrue (ThreadContext.isAdmin());
        ThreadContext.popSuperUser();
        assertTrue (ThreadContext.isAdmin());
        ThreadContext.popSuperUser();
        assertFalse(ThreadContext.isAdmin());
        assertFalse(ThreadContext.isSecurityUnchecked());
    }

    /** Inner class implementing ThreadContextListener */
	static class TestedListener implements UnboundListener {
        
        /** Will be set when Thread was unbound */
        public boolean unBound;
        
        /**
         * Set the member for testing.
         */
        @Override
		public void threadUnbound(InteractionContext context) {
            unBound = true;
        }
    }

    /** Return the suite of Tests to execute */
    public static Test suite () {
        TestSuite suite = new TestSuite("TestThreadContext");

		suite.addTest(new CustomPropertiesSetup(new TestSuite(TestThreadContext.class),
			FileManager.markDirect(
				"./" + ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/thread/setup.xml")));
		
        return BasicTestSetup.createBasicTestSetup(suite);
    }

    /** Main function for direct testing */
    public static void main (String[] args) {
    	Logger.configureStdout();
    	
        junit.textui.TestRunner.run (suite ());
    }
}
