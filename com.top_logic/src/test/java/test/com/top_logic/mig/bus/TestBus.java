/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.bus;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.Channel;
import com.top_logic.event.bus.Sender;
import com.top_logic.event.bus.Service;

/** 
 * Test class for check of function of Application Bus.
 *
 * The application bus helps in sending messages between different classes
 * inside one VM.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */ 
public class TestBus extends TestCase {

    private Sender firstSender;

    private Sender secondSender;

    private Sender thirdSender;

    private Service firstService;

    private Service secondService;

    private Service thirdService;

    private Service someServices;

    private Service allServices;

	private Sender anotherFirstSender;

    /**
     * Default constructor.
     *
     * @param    aName    Name of test to execute.
     */
    public TestBus (String aName) {
        super (aName);
    }

    /**
     * Define the different channels.
     */
    @Override
	protected void setUp () throws Exception {
        this.firstService  = new Service ("Movie",          "Action");
        this.secondService = new Service ("Movie",          "SciFi");
        this.thirdService  = new Service ("News",           "n-tv");
        this.someServices  = new Service ("Movie",          Service.WILDCARD);
        this.allServices   = new Service (Service.WILDCARD, Service.WILDCARD);

        this.firstSender   = new Sender (this.firstService);
		this.anotherFirstSender = new Sender(this.firstService);
        this.secondSender  = new Sender (this.secondService);
        this.thirdSender   = new Sender (this.thirdService);
    }

    /**
     * Reset the different channels.
     */
    @Override
	protected void tearDown () throws Exception {
        this.firstSender   = null;
		this.anotherFirstSender = null;
        this.secondSender  = null;
        this.thirdSender   = null;

        this.firstService  = null;
        this.secondService = null;
        this.thirdService  = null;
        this.someServices  = null;
        this.allServices   = null;
    }

    public void testAddSender () {
        Bus theBus = Bus.getInstance ();
        assertNotNull (theBus.addSender (this.firstSender));
        assertNotNull (theBus.addSender (this.secondSender));
        assertNotNull (theBus.addSender (this.thirdSender));
    }

    public void testRemoveSender () {
        Bus theBus = Bus.getInstance ();

        Channel c1 = theBus.addSender(this.firstSender);
		Channel cI = theBus.addSender(this.anotherFirstSender);
        Channel c2 = theBus.addSender(this.secondSender);
        Channel c3 = theBus.addSender(this.thirdSender);
        
        assertNotNull(c1);
        assertNotSame(c1, c2);
        assertNotNull(c3);
        assertNotSame(c1, c3);
		assertNotNull(cI);
		assertSame(c1, cI);

        theBus.removeSender(this.firstSender);

		Channel theChannel = theBus.getMap().get(this.firstSender.getService());

		assertNotNull(anotherFirstSender + " is contained in channel.", theChannel);
        assertFalse(theChannel.contains(this.firstSender));
		assertTrue(theChannel.contains(this.anotherFirstSender));
    }

    public void testService () {
        Service theService = new Service ("Movie", "Action");
        
        assertNotNull(theService.toString()); // Just to call it once
        assertEquals (firstService , theService);
        assertFalse  (firstService.equals(secondService));
        assertFalse  (secondService.equals(thirdService));
        assertEquals (someServices , theService);
        assertEquals (theService   , someServices);
        assertEquals (theService   , allServices);
        assertEquals (theService   , allServices);
        assertTrue   (!secondService.equals (theService));
        assertTrue   (!thirdService .equals (theService));
    }

    /** 
     * Return the suite of tests to execute.
     */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return ServiceTestSetup.createSetup(new TestBus("testRemoveSender"), Bus.Module.INSTANCE);
		}
		return ServiceTestSetup.createSetup(TestBus.class, Bus.Module.INSTANCE);
    }

    /** 
     * Main function for direct testing.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
