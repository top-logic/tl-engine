/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.bus;

import java.rmi.RemoteException;
import java.util.List;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.event.bus.AbstractReceiver;
import com.top_logic.event.bus.AbstractReceiver.Config;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.event.bus.Channel;
import com.top_logic.event.bus.IReceiver;
import com.top_logic.event.bus.Sender;
import com.top_logic.event.bus.Service;

/**
 * Testcase for {@link com.top_logic.event.bus.Channel}.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestChannel extends BasicTestCase {

    /** Used internally for testing */
    protected Object message;

    /**
     * Default constructor.
     *
     * @param    aName    Name of test to execute.
     */
    public TestChannel(String aName) {
        super(aName);
    }

    @Override
	public void tearDown() throws Exception {
        // Cleanup to allow GC   
        message = null;
        super.tearDown();
    }

    /** Single Tescase for now */
    public void testChannel() {
        Service  any  = new Service(Service.WILDCARD,Service.WILDCARD);
        Sender sender = new Sender();
		Config config;
		try {
			config = (Config) ApplicationConfig.getInstance().getServiceConfiguration(AbstractReceiver.class);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		IReceiver rec = new AbstractReceiver(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config) {
            @Override
			public void receive (BusEvent aBusEvent) throws RemoteException {
                message = aBusEvent.getMessage();
            }
        };
        
        TestableChannel theChannel = new TestableChannel(any);
        
        assertNotNull(theChannel.toString());
        assertSame(any, theChannel.getService());
        assertTrue(theChannel.isEmpty());
        assertEquals(0, theChannel.getReceiverList().size());
        assertTrue(theChannel.isEmpty());
        assertEquals(0, theChannel.getSenderList().size());
        assertTrue(theChannel.isEmpty());
        
        theChannel.subscribe(rec);
        assertEquals(1, theChannel.getReceiverList().size());
        assertTrue(!theChannel.isEmpty());

        theChannel.send(new BusEvent(sender,any,"", "Message from Nowhere"));
        assertEquals("Message from Nowhere", message);

        theChannel.send(new BusEvent(sender, any, "", null));
        assertNull("null message not received", message);

        theChannel.unsubscribe(rec);
        assertEquals(0, theChannel.getReceiverList().size());
        assertTrue(theChannel.isEmpty());
        
        theChannel.addSender(sender);
        assertEquals(1, theChannel.getSenderList().size());
        assertTrue(!theChannel.isEmpty());
        
        theChannel.removeSender(sender);
        assertEquals(0, theChannel.getSenderList().size());
        assertTrue(theChannel.isEmpty());
    }

    
    /** Inner class to make SenderList Acessible */
    static class TestableChannel extends Channel {
            
        
        public TestableChannel(Service aService) {
            super(aService);
        }

        /** make public for testing */
        @Override
		public List getSenderList() {
            return super.getSenderList();
        }

    }

    /** 
     * Return the suite of tests to execute.
     */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(TestChannel.class);
    }

    /** 
     * Main function for direct testing.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        TestRunner.run (suite ());
    }

}
