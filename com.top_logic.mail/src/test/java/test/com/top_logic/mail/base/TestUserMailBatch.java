/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.util.Collection;

import javax.mail.MessagingException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.base.mail.MailSenderService;
import com.top_logic.base.mail.UserMailBatch;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mail.proxy.MailReceiverService;

/** 
 * Tests for {@link com.top_logic.base.mail.UserMailBatch}
 *
 * @author    hbo
 */
public class TestUserMailBatch extends BasicTestCase {

	private PersonManager _personManager;

    /**
     * Constructor for TestUserMailBatch.
     * @param aName name of the test to execute.
     */
    public TestUserMailBatch (String aName) {
        super (aName);
    }
    
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_personManager = PersonManager.getManager();
	}

    /**
	 * Test the {@link com.top_logic.base.mail.UserMailBatch#run}-method.
	 * 
	 * An e-mail is sent to that address that is assigned to the user
	 * {@link TestMailHelper#getReceiver()} in the Users.test.xml
	 */    
	public void testRun() throws MessagingException {
		String userMailAddress = TestMailHelper.getReceiver();
		String theUserName = TestMailHelper.VALID_USER_NAME;
		checkUserExists(theUserName, userMailAddress);
		String subject = TestMailHelper.newSubject();
		UserMailBatch theUserMailBatch = new UserMailBatch(
            theUserName
			, subject
			, TestUserMailBatch.class + ".testRun()");
        theUserMailBatch.run();
		TestMailHelper.deleteSentMail(subject);
    }

	/**
	 * Tests the tests are correctly configured.
	 */
	private void checkUserExists(String userName, String userMailAddress) {
		Person person = Person.byName(userName);
		assertNotNull("Person is created because user is defined in Users.test.xml", person);
		UserInterface user = Person.userOrNull(person);
		assertNotNull("User is defined in Users.test.xml", user);
		assertEquals("Email is defined in Users.test.xml", userMailAddress, user.getEMail());
	}
    
   /**
    * Test the {@link com.top_logic.base.mail.UserMailBatch#createRecipientSet} 
    * -method.
    * There are some preconditions for this test-case
    *   a user top-logic exist in Users.xml and he has not the role user
    *   a user aqwjkjjkiiuui does not exists in Users.xml
    *   a role user exists
    */
    public void testCreateRecipientSet() {
		String theUserName = "root," + TestPersonSetup.USER_ID;
        UserMailBatchTestHelper theUserMailBatch = new UserMailBatchTestHelper
                            (theUserName, "Test-Mail", "hallo world");
        Collection<UserInterface> theRecipientList = theUserMailBatch.createRecipientSet();
		assertEquals(2, theRecipientList.size());
    }
    
   /**
    * The method constructing a test suite for this class.
    *
    * @return    The test to be executed.
    */
    public static Test suite () {
        Test innerTest = new TestSuite(TestUserMailBatch.class);
		innerTest = ServiceTestSetup.createSetup(innerTest,
			MailSenderService.Module.INSTANCE,
			MailReceiverService.Module.INSTANCE,
			MailHelper.Module.INSTANCE);

		return PersonManagerSetup.createPersonManagerSetup(
			TestPersonSetup.wrap(innerTest));
    }

   /**
    * The main program for executing this test also from console.
    *
    * @param    args    Will be ignored.
    */
    public static void main (String[] args) {
        SHOW_TIME = true;
        
        Logger.configureStdout();   // "INFO"
        
        TestRunner.run (suite ());
    }
}
