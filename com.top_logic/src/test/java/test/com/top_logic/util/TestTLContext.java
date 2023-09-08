/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.context.SubSessionListener;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.util.MultiKBContext;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Test class for {@link TLContext}.
 *
 * @author <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestTLContext extends BasicTestCase {

    private TLContext context;

	/** Default CTor, create a test with this Function Name */
	public TestTLContext (String name) {
        super (name);
    }
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		ThreadContextManager manager = TLContextManager.getManager();
		this.context = (TLContext) manager.newSubSessionContext();
		context.setSessionContext(manager.newSessionContext());
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.context = null;
		super.tearDown();
	}

	public void testListener() {
		class TestingSubSessionListener implements SubSessionListener {

			List<SubSessionContext> _closedContexts = new ArrayList<>();

			@Override
			public void notifySubSessionUnbound(TLSubSessionContext context) {
				if (_closedContexts.contains(context)) {
					fail("Context '" + context + "' must not be closed twice.");
				}
				_closedContexts.add(context);
			}

		}
		TestingSubSessionListener l = new TestingSubSessionListener();
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				TLContextManager.getSubSession().addUnboundListener(l);
			}
		});
		assertEquals(1, l._closedContexts.size());

	}

	public void testContextClass() {
		/* For the tests in this class it is not needed that the context is a MultiKBContext. But
		 * that context can handle more than one KnowledgeBase, so it is necessary for KnowledgeBase
		 * tests. */
		assertInstanceof("It is expected that the context is a " + MultiKBContext.class, this.context, MultiKBContext.class);
	}

    /**
     *  Test main Features of TLContext.
     */
	public void testMain() {
		TLContext currentContext = TLContext.getContext();

		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				ThreadContextManager.inContext(context, new InContext() {
					@Override
					public void inContext() {
						assertEquals(context, TLContext.getContext());
					}
				});
			}
		});
    
		assertSame("Setting TLContext in different thread has no effects outside that thread.", currentContext,
			TLContext.getContext());
    }

    /** Test some Trivial accessors for better coverage.
     */
    public void testTrivial () throws Exception {
        
        // Do not try this in a running top-logic  ;->
        context.setCurrentLocale(Locale.TRADITIONAL_CHINESE);
        assertEquals(Locale.TRADITIONAL_CHINESE, context.getCurrentLocale());

		for (String timeZoneId : TimeZone.getAvailableIDs()) {
			TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
			context.setCurrentTimeZone(timeZone);
			assertEquals(timeZone, context.getCurrentTimeZone());
		}
    }

    /** Return the suite of Tests to execute */
    public static Test suite () {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestTLContext.class));
    }
    
}
