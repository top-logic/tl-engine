/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.gui.layout.webfolder;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.io.binary.TestingBinaryData;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.gui.layout.webfolder.CreateFolderHandler;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.CreatePhysicalResource;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WebFolderFactory;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Testcase for the {@link com.top_logic.knowledge.gui.layout.webfolder.CreateFolderHandler}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestCreateFolderHandler extends AbstractFolderTest {

    protected static final String FOLDER_NAME = "TEST_CREATE_FOLDER";
    protected static final String PARENT_NAME = "TEST_CREATE_PARENT";

    /**
	 * Constructor for TestCreateFolderHandler.
	 */
    public TestCreateFolderHandler(String arg0) {
        super(arg0);
    }

    @Override
	protected String getParentName() {
        return (PARENT_NAME);
    }

    public void testCreateFolder() throws Exception {
		CreateFolderHandler theHandler = this.getHandler();

		WebFolder noParentNoNameFolder = theHandler.createFolder(null, null);
		assertNull("Created a folder for parent null and name null", noParentNoNameFolder);

		WebFolder noParentFolder = theHandler.createFolder("NoWay", null);
		assertNull("Created a folder for parent null", noParentFolder);

		WebFolder parent = this.getParent();
		assertNotNull("Unable to get parent folder", parent);

		int numberChildrenBefore = parent.getContent().size();

		KnowledgeBase kb = WebFolder.getDefaultKnowledgeBase();
		Transaction createSubFolderTX = kb.beginTransaction();
		WebFolder subFolder = theHandler.createFolder(FOLDER_NAME, parent);
		assertNotNull("Created no folder for parent " + parent, subFolder);
		try {
			createSubFolderTX.commit();

			int numberChildrenAfterCreation = parent.getContent().size();
			assertEquals("Unexpected number of children in " + parent, numberChildrenBefore + 1,
				numberChildrenAfterCreation);
		} finally {
			Transaction delSubFolderTX = subFolder.getKnowledgeBase().beginTransaction();
			assertTrue("Unable to delete folder (KO)", WebFolder.deleteRecursively(subFolder));
			delSubFolderTX.commit();
		}

	}

	// Test case was supposed to show broken concurrent lazy creation of webfolder's
	// physical resource. Indeed it is broken by design, due to no synchronization takes
	// place (and cannot be done at all). Unluckily the bug probably shows up in cluster mode
	// only. In single node mode another design flaw covers the concurrent creation bug. Because
	// the physical resource becomes cached after its creation in a transaction, another
	// concurrent transaction would detect and use it as reference, regardless the first transaction
	// will be rolled back or not.
	public void testMultiThreadedDocumentCreationInInitiallyEmptyFolder() throws Exception {
		final Barrier barrier = createBarrier(2);
		final WebFolder webFolder = createEmptyWebFolder();

		if (isLazyCreationEnabled()) {
			assertFalse(webFolder.hasDAP());
		}

		parallelTest(2, new ExecutionFactory() {

			@Override
			public Execution createExecution(final int id) {
				return new Execution() {

					@Override
					public void run() throws Exception {
						KnowledgeBase kb = WebFolder.getDefaultKnowledgeBase();
						Transaction createSubFolderTX = kb.beginTransaction();
						TestingBinaryData newData = new TestingBinaryData(12345678, 2048);
						webFolder.createOrUpdateDocument("TestDocument" + id, newData);
						barrier.enter(0);
						assertTrue(webFolder.hasDAP());
						createSubFolderTX.commit();
					}
				};
			}
		});

		HistoryUtils.updateSessionRevision();
		assertEquals(2, webFolder.getContent().size());
	}

	private WebFolder createEmptyWebFolder() throws Exception, KnowledgeBaseException {
		CreateFolderHandler theHandler = this.getHandler();
		WebFolder parent = this.getParent();
		assertNotNull("Unable to get parent folder", parent);

		KnowledgeBase kb = WebFolder.getDefaultKnowledgeBase();
		Transaction createSubFolderTX = kb.beginTransaction();
		WebFolder subFolder = theHandler.createFolder(FOLDER_NAME, parent);
		assertNotNull("Created no folder for parent " + parent, subFolder);
		createSubFolderTX.commit();

		return subFolder;
	}

	private boolean isLazyCreationEnabled() {
		return WebFolderFactory.getInstance().getCreateMode() == CreatePhysicalResource.DEFERRED;
	}

    protected CreateFolderHandler getHandler() {
		return AbstractCommandHandler.newInstance(CreateFolderHandler.class, CreateFolderHandler.COMMAND);
    }

    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
    public static Test suite () {
		TestFactory moduleStarter =
			ServiceTestSetup.createStarterFactoryForModules(MimeTypes.Module.INSTANCE,
				WebFolderFactory.Module.INSTANCE, CommandHandlerFactory.Module.INSTANCE);
		return KBSetup.getKBTest(TestCreateFolderHandler.class, moduleStarter);
    }

    /**
     * Start this test.
     * 
     * @param    args    Not used.
     */      
    public static void main(String[] args) {
        // KBSetup.CREATE_TABLES = false;
        SHOW_TIME = true;
        Logger.configureStdout();
        TestRunner.run (suite ());
    }

}
