/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.base.mail.MailSenderService;
import com.top_logic.mail.proxy.MailReceiverService;

/**
 * The TestMailtransport tests the access on {@link MailSenderService#send(Message, List, boolean)} 
 * from multiple threads
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestMailtransport extends BasicTestCase {

    public static Test suite() { 
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestMailtransport.class,
			MailSenderService.Module.INSTANCE, MailReceiverService.Module.INSTANCE));
    }

    public static void main(String[] args) {
        TestRunner theRunner = new TestRunner();
        theRunner.doRun(suite());
    }
    
    public void testSendMailThreaded() throws Exception {
        final int maxMails = 3;
        
        SendMailThread theT1 = new SendMailThread("TestMailtransport Thread 1", maxMails, 13, false);
        SendMailThread theT2 = new SendMailThread("TestMailtransport Thread 2", maxMails, 15, true);
        SendMailThread theT3 = new SendMailThread("TestMailtransport Thread 3", maxMails, 600, false);
        
        theT1.start();
        theT2.start();
        theT3.start();
        theT3.join();
        theT2.join();
        theT1.join();
		theT1.deleteMails();
		theT2.deleteMails();
		theT3.deleteMails();
        assertEquals(maxMails * 3, theT1.success + theT2.success + theT3.success);
    }
    
	Message createMessage(String aThread) throws MessagingException, AddressException {
		Message theMessage = MailSenderService.getInstance().createEmptyMessage();
        theMessage.setFrom(new InternetAddress(MailSenderService.getFromAddress()));
		theMessage.setRecipient(RecipientType.TO, new InternetAddress(TestMailHelper.getReceiver()));
		String theSubject = TestMailHelper.newSubject() + " / " + aThread;
        String theContent     = "Class: "      + MailSenderService.class;
               theContent    += "\nTestcase: " + this.getClass();
               theContent    += "\nThread: " + aThread;
               theContent    += "\nTest: testSendMailThreaded()";
               theContent    += "\n" + (new Date());
        theMessage.setSubject(theSubject);
        theMessage.setContent(theContent, MailHelper.CONTENT_TYPE_TEXT);
        theMessage.setSentDate(new Date());
        theMessage.saveChanges();
        return theMessage;
    }
    
    private class SendMailThread extends Thread {
        // Constructors

        int maxMails;
        int success;
        boolean reloadTransport;
        long waitTime;

		List<String> _subjects = new ArrayList<>();
        public SendMailThread(String aName, int maxMails, long waitTime, boolean reloadTransport) {
            super(aName);
            this.maxMails = maxMails;
            this.waitTime = waitTime;
            this.reloadTransport = reloadTransport;
        }
        
		public void deleteMails() throws MessagingException {
			for (String subject : _subjects) {
				TestMailHelper.deleteSentMail(subject);
			}
		}

		@Override
		public void run() {
            MailSenderService theTransport = MailSenderService.getInstance();
            for (int i=0; i<this.maxMails; i++) {
                List<Address> theInvalid = new ArrayList<>();
                try {
                    if (reloadTransport) {
                        theTransport.reload();
                    }
					Message theMessage =
						TestMailtransport.this.createMessage(this.getName());
                    sleep(this.waitTime);
                    assertTrue("Sending from " + getName() + " failed", theTransport.send(theMessage, theInvalid, false));
					_subjects.add(theMessage.getSubject());
                    success++;
                } catch (Exception ex) {
                    fail("Exeption while sending " + getName(), ex);
                }
                assertTrue("Invalid addresses is not empty!", theInvalid.isEmpty());
            }
        }
    }
}

