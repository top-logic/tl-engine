/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.gui.layout.webfolder;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WebFolder;


/**
 * @author     <a href="mailto:mga@top-logic.com">mga</a>
 */
public abstract class AbstractFolderTest extends BasicTestCase {

	protected static final String PARENT_BASE = "repository://";

	private WebFolder parent;

    
    public AbstractFolderTest() {
        super();
    }

    public AbstractFolderTest(String someName) {
        super(someName);
    }

	@Override
	protected void tearDown() throws Exception {
		if (parent != null) {
			Transaction deleteParentTX = parent.getKnowledgeBase().beginTransaction();
			WebFolder.deleteRecursively(parent);
			deleteParentTX.commit();
		}
		super.tearDown();
	}

    /**
     * Return the name of the parent folder to be used for this test.
     * 
     * @return    The name of the parent folder used for this test.
     */
    protected abstract String getParentName();

    /**
     * Return the parent to be used for this test.
     * 
     * If the parent doesn't exists, it'll be created.
     * 
     * @return    The requested parent.
     * @throws    Exception    If creating the parent fails for a reason.
     */
	protected WebFolder getParent() throws Exception {
		WebFolder theFolder = parent;
        if (theFolder == null) {
			String theName = this.getParentName();
			DataAccessProxy theProxy = new DataAccessProxy(PARENT_BASE);
			DataAccessProxy theFolderProxy = theProxy.getChildProxy(theName);
			if (!theFolderProxy.exists()) {
				theFolderProxy = theProxy.createContainerProxy(theName);
			}
			theFolder = WebFolder.getInstance(theProxy);
			if (theFolder == null) {
				Transaction createTX = WebFolder.getDefaultKnowledgeBase().beginTransaction();
				theFolder = WebFolder.createFolder(theName, PARENT_BASE);
				createTX.commit();
			}

			this.logSpace("Using folder " + theFolder);
			parent = theFolder;
        }

        return (theFolder);
    }

}
