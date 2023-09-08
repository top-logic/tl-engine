/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base.imap;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.mail.base.MailTestUtils;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.base.MailServer;
import com.top_logic.mail.base.imap.IMAPMailFolder;
import com.top_logic.util.sched.Scheduler;

/**
 * Test for {@link IMAPMailFolder}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestIMAPMailFolder extends BasicTestCase {

	private IMAPMailFolder _testRootFolder;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MailFolder rootFolder = MailServer.getInstance().getRootFolder();
		Transaction tx = kb().beginTransaction();
		try {
			MailFolder subFolder = rootFolder.getFolder("TestIMAPMailFolder", true);
			assertInstanceof(subFolder, IMAPMailFolder.class);
			_testRootFolder = (IMAPMailFolder) subFolder;
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		Transaction tx = kb().beginTransaction();
		try {
			MailTestUtils.deleteFolderAndWrapper(_testRootFolder);
			tx.commit();
		} finally {
			tx.rollback();
		}
		super.tearDown();
	}

	public void testMailSubFolder() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		MailFolder folder = _testRootFolder.getFolder("testSubFolder", true);
		tx.commit();
		assertNotNull(folder);

		// disconnect to ensure upcoming connect.
		_testRootFolder.disconnect();

		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener();
		_testRootFolder.connect();
		logListener.assertNoErrorLogged("Ticket #21140: MailFolders contain MailFolder children.");
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestIMAPMailFolder}.
	 */
	public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(TestIMAPMailFolder.class,
			MimeTypes.Module.INSTANCE,
			MailSenderService.Module.INSTANCE,
			MailServer.Module.INSTANCE,
			Scheduler.Module.INSTANCE,
			PersistencyLayer.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
	}

}

