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
    
    /** Check that putCOntext works as override.
     */
	public void testPutContext() {
		ThreadContext.inSystemContext(TestThreadContext.class, new InContext() {

			@Override
			public void inContext() {
				final ThreadContext currentContext = ThreadContext.getThreadContext();
				assertNotNull(currentContext);
				Logger.configureStdout("ERROR"); // suppress valid WARNING message
				try {
					ThreadContext.inSystemContext(TestThreadContext.class, new InContext() {

						@Override
						public void inContext() {
							assertSame(currentContext, ThreadContext.getThreadContext());
						}
					});
				} finally {
					Logger.configureStdout();
				}
			}
		});
    }
 
    /** 
     * Test the SupeUrser methods 
     */
    public void testSuperUser () throws Exception {
       
        assertFalse(ThreadContext.isAdmin());
        ThreadContext.pushSuperUser();
        assertTrue (ThreadContext.isAdmin());
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
