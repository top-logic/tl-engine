/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.layout.structured;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.layout.structured.StructuredElementTreeModel;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;

/**
 * {@link TestCase} of {@link StructuredElementTreeModel}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestStructuredElementTreeModel extends BasicTestCase {

	private static final class RejectProjectA1Filter implements Filter<StructuredElement> {

		/**
		 * Singleton {@link RejectProjectA1Filter} instance.
		 */
		public static final RejectProjectA1Filter INSTANCE = new RejectProjectA1Filter();

		private RejectProjectA1Filter() {
			// Singleton constructor.
		}

		@Override
		public boolean accept(StructuredElement element) {
			if (element.getName().equals("projectA1")) {
				return false;
			} else {
				return true;
			}
		}
	}

	private StructuredElement root;
	private List<StructuredElement> projects;

	@Override
	protected void setUp() throws Exception {
		createStructuredElementHierarchy();
	}

	@Override
	protected void tearDown() throws Exception {
		removeStructuredElementHierarchy();
	}

	public void testGetPathToStructureRoot() throws Exception {
		StructuredElementTreeModel treeModel = new StructuredElementTreeModel(root);
		StructuredElement thirdLevelChild = getThirdLevelChild();

		String[] expectedPathToRoot = { "projectA11", "projectA1", "projectA", root.getName() };
		List<StructuredElement> pathToRoot = treeModel.createPathToRoot(thirdLevelChild);
		assertPathToRoot(expectedPathToRoot, pathToRoot);
	}

	public void testGetSingleElementPathToStructureRoot() throws Exception {
		StructuredElementTreeModel treeModel = new StructuredElementTreeModel(root);

		String[] expectedPathToRoot = { root.getName() };
		List<StructuredElement> pathToRoot = treeModel.createPathToRoot(root);
		assertPathToRoot(expectedPathToRoot, pathToRoot);
	}

	public void testGetPathToTreeRoot() throws Exception {
		StructuredElementTreeModel treeModel = new StructuredElementTreeModel(getProjectA());
		StructuredElement thirdLevelChild = getThirdLevelChild();

		String[] expectedPathToRoot = { "projectA11", "projectA1", "projectA" };
		List<StructuredElement> pathToRoot = treeModel.createPathToRoot(thirdLevelChild);
		assertPathToRoot(expectedPathToRoot, pathToRoot);
	}

	public void testGetSingleElementPathToTreeRoot() throws Exception {
		StructuredElementTreeModel treeModel = new StructuredElementTreeModel(getProjectA());

		String[] expectedPathToRoot = { "projectA" };
		List<StructuredElement> pathToRoot = treeModel.createPathToRoot(getProjectA());
		assertPathToRoot(expectedPathToRoot, pathToRoot);
	}

	public void testGetEmptyPathToTreeRoot() throws Exception {
		StructuredElementTreeModel treeModel = new StructuredElementTreeModel(getProjectA());

		String[] expectedPathToRoot = {};
		List<StructuredElement> pathToRoot = treeModel.createPathToRoot(getProjectB());
		assertPathToRoot(expectedPathToRoot, pathToRoot);
	}

	public void testGetEmptyPathToTreeRootFilter() throws Exception {
		StructuredElementTreeModel treeModel =
			new StructuredElementTreeModel(getProjectA(), RejectProjectA1Filter.INSTANCE);
		StructuredElement thirdLevelChild = getThirdLevelChild();

		String[] expectedPathToRoot = {};
		List<StructuredElement> pathToRoot = treeModel.createPathToRoot(thirdLevelChild);
		assertPathToRoot(expectedPathToRoot, pathToRoot);
	}

	public void testGetEmptyPathToTreeRootNullChild() throws Exception {
		StructuredElementTreeModel treeModel = new StructuredElementTreeModel(getProjectA());

		String[] expectedPathToRoot = {};
		List<StructuredElement> pathToRoot = treeModel.createPathToRoot(null);
		assertPathToRoot(expectedPathToRoot, pathToRoot);
	}

	public void testGetParentException() throws Exception {
		StructuredElementTreeModel treeModel =
			new StructuredElementTreeModel(getProjectA(), RejectProjectA1Filter.INSTANCE);
		StructuredElement secondLevelChild = getSecondLevelChild();

		try {
			treeModel.getParent(secondLevelChild);
			fail("Exception must be thrown, in case parent node does not belong to this tree model!");
		} catch (IllegalArgumentException ex) {
			assertEquals("The given child is not part of this tree.", ex.getMessage());
		}
	}

	public void testGetParentNoRootException() throws Exception {
		StructuredElement secondLevelChild = getSecondLevelChild();
		StructuredElementTreeModel treeModel =
			new StructuredElementTreeModel(secondLevelChild, RejectProjectA1Filter.INSTANCE);

		try {
			treeModel.getParent(secondLevelChild);
		} catch (IllegalArgumentException ex) {
			fail("Tree model filter must not be applied to tree model's root node!");
		}
	}

	public void testContainsNodeTrue() throws Exception {
		StructuredElementTreeModel treeModel = new StructuredElementTreeModel(getProjectA());
		StructuredElement thirdLevelChild = getThirdLevelChild();

		assertTrue(treeModel.containsNode(thirdLevelChild));
	}

	public void testContainsNodeFalse() throws Exception {
		StructuredElementTreeModel treeModel = new StructuredElementTreeModel(getProjectB());
		StructuredElement thirdLevelChild = getThirdLevelChild();

		assertFalse(treeModel.containsNode(thirdLevelChild));
	}

	public void testContainsNodeException() throws Exception {
		StructuredElementTreeModel treeModel = new StructuredElementTreeModel(getProjectA());

		try {
			treeModel.containsNode(null);
			fail("Exception must be thrown, in case 'null' node containment shall be checked!");
		} catch (IllegalArgumentException ex) {
			assertEquals("Cannot compute containment for node 'null'!", ex.getMessage());
		}
	}

	private void createStructuredElementHierarchy() throws KnowledgeBaseException {
		KnowledgeBase knowledgeBase = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		Transaction transaction = knowledgeBase.beginTransaction();
		try {
			root = ((StructuredElementFactory) DynamicModelService.getFactoryFor("projElement")).getRoot();
			projects = new ArrayList<>();

			StructuredElement projectA = root.createChild("projectA", "Project");
			createSubProjectStructure(projectA, 2, 0);
			projects.add(projectA);

			StructuredElement projectB = root.createChild("projectB", "Project");
			createSubProjectStructure(projectB, 2, 0);
			projects.add(projectB);
			transaction.commit();
		} finally {
			transaction.rollback();
		}
	}

	private void createSubProjectStructure(StructuredElement element, int targetDepth, int currentDepth) {
		StructuredElement subProject1 = element.createChild(element.getName() + "1", "Subproject");
		StructuredElement subProject2 = element.createChild(element.getName() + "2", "Subproject");
		if (currentDepth < targetDepth) {
			createSubProjectStructure(subProject1, targetDepth, currentDepth + 1);
			createSubProjectStructure(subProject2, targetDepth, currentDepth + 1);
		}
	}

	private void removeStructuredElementHierarchy() throws KnowledgeBaseException {
		KnowledgeBase knowledgeBase = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		Transaction transaction = knowledgeBase.beginTransaction();
		try {
			for (StructuredElement project : projects) {
				removeSubProjectStructure(project);
				project.tDelete();
			}
			projects.clear();
			transaction.commit();
		} finally {
			transaction.rollback();
		}
	}

	private void removeSubProjectStructure(StructuredElement element) {
		List<? extends StructuredElement> children = element.getChildren();
		for (StructuredElement child : children) {
			removeSubProjectStructure(child);
			child.tDelete();
		}
	}

	private void assertPathToRoot(String[] expectedPathToRoot, List<StructuredElement> pathToRoot) {
		assertEquals("Path has unexpected length!", expectedPathToRoot.length, pathToRoot.size());
		for (int i = 0; i < expectedPathToRoot.length; i++) {
			assertEquals("Path element is not expected!", expectedPathToRoot[i], pathToRoot.get(i).getName());
		}
	}

	private StructuredElement getSecondLevelChild() {
		StructuredElement firstLevelChild = getProjectA();
		StructuredElement secondLevelChild = firstLevelChild.getChildren().get(0);
		return secondLevelChild;
	}

	private StructuredElement getThirdLevelChild() {
		StructuredElement secondLevelChild = getSecondLevelChild();
		StructuredElement thirdLevelChild = secondLevelChild.getChildren().get(0);
		return thirdLevelChild;
	}

	private StructuredElement getProjectA() {
		return projects.get(0);
	}

	private StructuredElement getProjectB() {
		return projects.get(1);
	}

	/**
	 * Return the suite of Tests to perform.
	 * 
	 * @return the test for this class
	 */
	public static Test suite() {
		// test limited to MySQL until #4001 gets fixed to have a running security test
		return TestUtils.doNotMerge(KBSetup.getKBTest(TestStructuredElementTreeModel.class));
	}
}
