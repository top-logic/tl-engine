/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.common.webfolder.ui;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.knowledge.dummy.DummyWrapper;

import com.top_logic.basic.StringID;
import com.top_logic.common.folder.impl.TransientFolderDefinition;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.webfolder.model.WebFolderAccessor;
import com.top_logic.common.webfolder.model.WebFolderTreeBuilder;
import com.top_logic.common.webfolder.ui.WebFolderFieldProvider;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;

/**
 * Test case for {@link WebFolderFieldProvider}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestWebFolderFieldProvider extends BasicTestCase {

	WebFolderFieldProvider fieldProvider;

	FolderNode emptyFolderNode;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fieldProvider = new WebFolderFieldProvider(
			ExecutableState.EXECUTABLE, ExecutableState.EXECUTABLE, ExecutableState.EXECUTABLE, true);
		AbstractMutableTLTreeModel<FolderNode> treeModel = new AbstractMutableTLTreeModel<>(
			WebFolderTreeBuilder.INSTANCE,
			new TransientFolderDefinition("TestRootUserObject"));
		emptyFolderNode = new FolderNode(treeModel,
											treeModel.getRoot(),
											DummyWrapper.obj(StringID.createRandomID()),
											false);

		TLContext.getContext().setCurrentPerson(PersonManager.getManager().getRoot());
	}

	public void testGetLockFieldName() {
		checkNameCreationAndRetrievalEquality(WebFolderAccessor.LOCK);
	}

	public void testGetDeleteFieldName() {
		checkNameCreationAndRetrievalEquality(WebFolderAccessor.DELETE);
	}

	public void testGetKeywordsFieldName() {
		checkNameCreationAndRetrievalEquality(WebFolderAccessor.KEYWORDS);
	}

	public void testGetSimilarDocumentsFieldName() {
		checkNameCreationAndRetrievalEquality(WebFolderAccessor.SIMILAR_DOCUMENTS);
	}

	public void testGetDownloadFieldName() {
		checkNameCreationAndRetrievalEquality(WebFolderAccessor.DOWNLOAD);
	}

	public void testGetClipboardFieldName() {
		checkNameCreationAndRetrievalEquality(WebFolderAccessor.CLIPBOARD);
	}

	public void testGetVersionFieldName() {
		checkNameCreationAndRetrievalEquality(WebFolderAccessor.VERSION);
	}

	public void testGetMailFieldName() {
		checkNameCreationAndRetrievalEquality(WebFolderAccessor.MAIL);
	}

	private void checkNameCreationAndRetrievalEquality(String fieldName) {
		FormMember lockField = fieldProvider.createField(emptyFolderNode, null, fieldName);

		assertEquals(lockField.getName(), fieldProvider.getFieldName(emptyFolderNode, null, fieldName));
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(TestWebFolderFieldProvider.class,
			DefaultTestFactory.INSTANCE);
//		return TLTestSetup.createTLTestSetup(
//			ServiceTestSetup.createSetup(null,
//										 TestWebFolderFieldProvider.class,
//										 PersonManager.Module.INSTANCE));
	}
}
