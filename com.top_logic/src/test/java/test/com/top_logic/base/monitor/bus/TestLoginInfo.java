/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.monitor.bus;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.monitor.bus.LoginInfo;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Check the functionality of the LoginInfo class.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestLoginInfo extends BasicTestCase {

    /** The name of the test user. */
	private static final String DEFAULT_USER = TestPersonSetup.USER_ID;

    /** The user to be used for this test. */
	private Person user;

    /**
     * Constructor for the specific test case.
     *
     * @param    aName    The name of the method to be tested.
     */
	public TestLoginInfo (String aName) {
        super (aName);
    }

    public void testCreation () throws Exception {
        try {
            new LoginInfo (null, null);

            fail ("Expected an IllegalArgumentException when created " +
                       "with an empty user!");
        }
        catch (IllegalArgumentException ex) {
            Logger.debug ("Create with (null, null): OK, " + ex, this);
        }

        Logger.debug ("Create with (user, null): " +
                      new LoginInfo (this.user, null), this);
        Logger.debug ("Create with (user, date): " +
                      new LoginInfo (this.user, new Date ()), this);
//        LoginInfo   theInfo;
//        UserMonitor theMonitor = this.reader.getUserMonitor ();
//        Iterator    theIt      = theMonitor.getLoginInfoList ().iterator ();
//
//        while (theIt.hasNext ()) {
//            theInfo = (LoginInfo) theIt.next ();
//
//            System.out.println ("LoginInfo: " + theInfo);
//        }                       
    }

    public void testLogout () throws Exception {
        Date      theOld  = new Date (0L);
        Date      theCurr = new Date ();
        Date      theNew  = new Date (theCurr.getTime () + 360000);
        LoginInfo theInfo = new LoginInfo (this.user, new Date ());

        assertTrue ("LoginInfo should be open!", 
                         theInfo.isOpen ());
        assertTrue ("LoginInfo should reject empty date!", 
                         !theInfo.setLogout (null));
        assertTrue ("LoginInfo should reject date older than login!", 
                         !theInfo.setLogout (theOld));

        Logger.debug ("Info is: " + theInfo, this);

        assertTrue ("LoginInfo should accept date newer than login!", 
                         theInfo.setLogout (theNew));

        Logger.debug ("Info is: " + theInfo, this);

        assertTrue ("LoginInfo should reject second setting of date!", 
                         !theInfo.setLogout (theNew));
    }

    public void testDuration () throws Exception {
        Date      theCurr = new Date ();
        Date      theNew  = new Date (theCurr.getTime () + 360000);
        LoginInfo theInfo = new LoginInfo (this.user, new Date ());

        assertTrue ("LoginInfo should accept date newer than login!", 
                         theInfo.setLogout (theNew));

        long duration =  theInfo.getDuration (); // Depending on platform this will be rounded.
        assertTrue ("Duration to short", duration > 359000L);     
        assertTrue ("Duration to long" , duration < 361000L);     
    }

    // Protected method

    @Override
	protected void setUp () throws Exception {
        super.setUp (); //setup TLTestCase

		this.user = PersonManager.getManager().getPersonByName(DEFAULT_USER);
    }

    @Override
	protected void tearDown () throws Exception {
        super.tearDown (); //setup TLTestCase
        this.user = null;
    }

    public static Test suite () {
		return PersonManagerSetup.createPersonManagerSetup(
			TestPersonSetup.wrap(
				new TestSuite(TestLoginInfo.class)));
    }

    /**
     * Main method to start this test case.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        Logger.configureStdout("ERROR");

        junit.textui.TestRunner.run (suite ());
    }
}
