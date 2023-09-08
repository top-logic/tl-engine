/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.common.webfolder.ui;

import java.util.Comparator;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.dummy.DummyWrapper;

import com.top_logic.basic.Named;
import com.top_logic.basic.StringID;
import com.top_logic.common.folder.impl.TransientFolderDefinition;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.webfolder.model.WebFolderTreeBuilder;
import com.top_logic.common.webfolder.ui.WebFolderComparator;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;

/**
 * The class {@link TestWebFolderComparator} tests {@link WebFolderComparator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestWebFolderComparator extends BasicTestCase {

	private AbstractMutableTLTreeModel<FolderNode> _treeModel;

	private FolderNode _root;

	private Comparator _comparator;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Can not use instance because initializer uses ThreadContextManager
		_comparator = new WebFolderComparator();
		String folderName = "TestRootUserObject";
		_treeModel = new AbstractMutableTLTreeModel<>(WebFolderTreeBuilder.INSTANCE, newFolder(folderName));
		_root = _treeModel.getRoot();
	}

	private FolderNode newNode(FolderNode parent, Named businessObject) {
		return new FolderNode(_treeModel, parent, businessObject, false);
	}

	private DummyWrapper newContent(String contentName) {
		DummyWrapper namedContent = DummyWrapper.obj(StringID.createRandomID());
		namedContent.setName(contentName);
		return namedContent;
	}

	private FolderNode newContentNode(String contentName) {
		return addContent(_root, newContent(contentName));
	}

	private TransientFolderDefinition newFolder(String folderName) {
		return new TransientFolderDefinition(folderName);
	}

	private FolderNode newFolderNode(String folderName) {
		return addContent(_root, newFolder(folderName));
	}

	private FolderNode addContent(FolderNode parent, Named content) {
		return newNode(parent, content);
	}

	public void testContentOrder() {
		FolderNode content1 = newContentNode("aa");
		FolderNode content2 = newContentNode("cc");
		FolderNode content3 = newContentNode("bb");
		assertOrder(content1, content3, content2);
	}

	public void testFolderOrder() {
		FolderNode content1 = newFolderNode("aa");
		FolderNode content2 = newFolderNode("cc");
		FolderNode content3 = newFolderNode("bb");
		assertOrder(content1, content3, content2);
	}

	public void testFolderOrderIgnoreContent() {
		FolderNode content1 = newFolderNode("aa");
		addContent(content1, newFolder("folder1"));
		FolderNode content2 = newFolderNode("cc");
		FolderNode content3 = newFolderNode("bb");
		addContent(content3, newFolder("folder1"));
		addContent(content3, newFolder("folder2"));
		assertOrder(content1, content3, content2);
	}

	public void testFolderBeforeContent() {
		FolderNode content1 = newFolderNode("a");
		FolderNode content2 = newContentNode("f");
		FolderNode content3 = newFolderNode("e");
		FolderNode content4 = newContentNode("d");
		FolderNode content5 = newFolderNode("c");
		FolderNode content6 = newContentNode("z");
		assertOrder(content1, content5, content3, content4, content2, content6);
	}

	private void assertOrder(FolderNode... nodes) {
		for (int i = 0; i < nodes.length; i++) {
			for (int j = i + 1; j < nodes.length; j++) {
				assertLess(nodes[i], nodes[j]);
			}
		}
	}

	private void assertLess(FolderNode node1, FolderNode node2) {
		assertTrue(_comparator.compare(node1, node2) < 0);
		assertTrue(_comparator.compare(node2, node1) > 0);
		assertTrue(_comparator.compare(node2, node2) == 0);
		assertTrue(_comparator.compare(node1, node1) == 0);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestWebFolderComparator}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestWebFolderComparator.class);
	}
}
