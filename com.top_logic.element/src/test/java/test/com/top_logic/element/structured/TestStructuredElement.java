/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.structured;

import static java.util.Collections.*;

import java.sql.SQLException;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.element.core.CreateElementException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.element.structured.wrap.StructuredElementWrapper;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;

/**
 * Test for {@link StructuredElement}s
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestStructuredElement extends BasicTestCase {

	private static final String KNOWN_BUG_TICKER_21418 =
		"Ticket #21418: OrderedLinkQuery is not compatible with the ListStorage.";

	private static final List<StructuredElement> NO_CHILDREN = list();

	private static final String PROJECT_STRUCTURE = "projElement";

	private AttributedStructuredElementWrapper _projectRoot;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_projectRoot = getStructureRoot(PROJECT_STRUCTURE);
	}

	@Override
	protected void tearDown() throws Exception {
		Transaction tx = beginTransaction();
		for (StructuredElement child : _projectRoot.getChildren()) {
			child.tDelete();
		}
		tx.commit();
		super.tearDown();
	}

	public void testChildrenRead() {
		Transaction transaction = beginTransaction();
		try {
			StructuredElement root = getRoot();
			assertChildren(root, NO_CHILDREN);

			StructuredElement child_1 = newProject("child 1");
			assertChildren(root, list(child_1));
			assertChildren(child_1, NO_CHILDREN);
			assertParent(root, child_1);

			StructuredElement child_2;
			try {
				child_2 = newProject("child 2");
			} catch (CreateElementException ex) {
				throw new RuntimeException(KNOWN_BUG_TICKER_21418, ex);
			}
			assertChildren(root, list(child_1, child_2));
			assertChildren(child_1, NO_CHILDREN);
			assertChildren(child_2, NO_CHILDREN);

			StructuredElement grandChild_1_1 = newSubproject(child_1, "grand-child 1.1");
			assertChildren(root, list(child_1, child_2));
			assertChildren(child_1, list(grandChild_1_1));
			assertChildren(child_2, NO_CHILDREN);
		} finally {
			transaction.rollback();
		}
	}

	public void testParentOverride() {
		try (Transaction transaction = beginTransaction()) {
			StructuredElement child_1 = newProject("child 1");

			TLClass type = (TLClass) child_1.tType();
			TLReference parentAttr = (TLReference) type.getPart(StructuredElementWrapper.PARENT_ATTR);

			TLObject parent = (TLObject) child_1.tValue(parentAttr);
			assertEquals(getRoot(), parent);

			TLClass parentType = (TLClass) parent.tType();
			TLReference childAttr = (TLReference) parentType.getPart(StructuredElementWrapper.CHILDREN_ATTR);

			assertTrue(parentAttr.isOverride());
			assertTrue(childAttr.isOverride());

			assertEquals(parentAttr, parentAttr.getEnd().getReference());
			assertEquals(childAttr, childAttr.getEnd().getReference());
		}
	}

	public void testChildren_removeChild() {
		Transaction transaction = beginTransaction();
		try {
			StructuredElement root = getRoot();
			StructuredElement child_1 = newProject("child 1");
			StructuredElement child_2 = newProject("child 2");
			StructuredElement grandChild_1_1 = newSubproject(child_1, "grand-child 1.1");

			assertChildren(root, list(child_1, child_2));
			assertChildren(child_1, list(grandChild_1_1));
			assertChildren(child_2, NO_CHILDREN);

			grandChild_1_1.tDelete();
			assertFalse(grandChild_1_1.tValid());
			assertChildren(root, list(child_1, child_2));
			assertChildren(child_1, NO_CHILDREN);
			assertChildren(child_2, NO_CHILDREN);

			child_2.tDelete();
			assertFalse(child_2.tValid());
			assertChildren(root, list(child_1));
			assertChildren(child_1, NO_CHILDREN);

			child_1.tDelete();
			assertFalse(child_1.tValid());
			assertChildren(root, NO_CHILDREN);
		} finally {
			transaction.rollback();
		}
	}

	public void testChildren_isChild() {
		Transaction transaction = beginTransaction();
		try {
			StructuredElement root = getRoot();
			StructuredElement child_1 = newProject("child 1");
			StructuredElement child_2 = newProject("child 2");
			StructuredElement grandChild_1_1 = newSubproject(child_1, "grand-child 1.1");

			assertFalse(root.isChild(root));
			assertTrue(root.isChild(child_1));
			assertTrue(root.isChild(child_2));
			assertFalse(root.isChild(grandChild_1_1));

			assertFalse(child_1.isChild(root));
			assertFalse(child_1.isChild(child_1));
			assertFalse(child_1.isChild(child_2));
			assertTrue(child_1.isChild(grandChild_1_1));

			assertFalse(child_2.isChild(root));
			assertFalse(child_2.isChild(child_1));
			assertFalse(child_2.isChild(child_2));
			assertFalse(child_2.isChild(grandChild_1_1));

			assertFalse(grandChild_1_1.isChild(root));
			assertFalse(grandChild_1_1.isChild(child_1));
			assertFalse(grandChild_1_1.isChild(child_2));
			assertFalse(grandChild_1_1.isChild(grandChild_1_1));
		} finally {
			transaction.rollback();
		}
	}

	public void testChildren_addValue_removeValue() {
		Transaction transaction = beginTransaction();
		try {
			AttributedStructuredElementWrapper root = getRoot();
			AttributedStructuredElementWrapper child_1 = newProject("child 1");
			AttributedStructuredElementWrapper child_2 = newProject("child 2");
			assertChildren(root, list(child_1, child_2));

			AttributedStructuredElementWrapper grandChild_1_1 = newSubproject(child_1, "grand-child 1.1");

			AttributeUtil.removeValue(child_1, StructuredElement.CHILDREN_ATTR, grandChild_1_1);
			assertTrue(grandChild_1_1.tValid());
			assertChildren(root, list(child_1, child_2));
			assertChildren(child_1, NO_CHILDREN);
			assertChildren(child_2, NO_CHILDREN);

			try {
				AttributeOperations.addValue(child_2, StructuredElement.CHILDREN_ATTR, grandChild_1_1);
			} catch (IllegalStateException ex) {
				throw new RuntimeException(KNOWN_BUG_TICKER_21418, ex);
			}
			assertChildren(root, list(child_1, child_2));
			assertChildren(child_1, NO_CHILDREN);
			assertChildren(child_2, list(grandChild_1_1));
		} finally {
			transaction.rollback();
		}
	}

	public void testChildren_setValue() {
		Transaction transaction = beginTransaction();
		try {
			AttributedStructuredElementWrapper root = getRoot();
			AttributedStructuredElementWrapper child_1 = newProject("child 1");
			AttributedStructuredElementWrapper child_2 = newProject("child 2");
			AttributedStructuredElementWrapper grandChild_1_1 = newSubproject(child_1, "grand-child 1.1");

			child_1.setValue(StructuredElement.CHILDREN_ATTR, NO_CHILDREN);
			assertTrue(grandChild_1_1.tValid());
			assertChildren(root, list(child_1, child_2));
			assertChildren(child_1, NO_CHILDREN);
			assertChildren(child_2, NO_CHILDREN);

			try {
				child_2.setValue(StructuredElement.CHILDREN_ATTR, list(grandChild_1_1));
			} catch (IllegalStateException ex) {
				throw new RuntimeException(KNOWN_BUG_TICKER_21418, ex);
			}
			assertChildren(root, list(child_1, child_2));
			assertChildren(child_1, NO_CHILDREN);
			assertChildren(child_2, list(grandChild_1_1));
		} finally {
			transaction.rollback();
		}
	}

	public void testGenericCreate() {
		AttributedStructuredElementWrapper parent;
		try (Transaction tx = beginTransaction()) {
			parent = newProject("genericCreate");
			tx.commit();
		}

		try (Transaction tx = beginTransaction()) {
			String moduleName = parent.tType().getModule().getName();
			ModelFactory factory = DynamicModelService.getFactoryFor(moduleName);
			TLClass abstractType = (TLClass) factory.getModule().getType("Subproject");
			assertTrue("Test needs an abstract type.", abstractType.isAbstract());
			try {
				StructuredElement child = (StructuredElement) factory.createObject(abstractType, parent, null);
				child.setValue("name", "genericSubProject");
				tx.commit();
				fail("Must not be able to create element for abstract type '" + abstractType + "'.");
			} catch (Exception ex) {
				// expected
			}
		}

		StructuredElement child;
		try (Transaction tx = beginTransaction()) {
			// Concrete class is defined in the root type.
			TLClass subProjectType = (TLClass) ((TLScope) _projectRoot).getType("Subproject");
			child = (StructuredElement) DynamicModelService.getInstance().createObject(subProjectType, parent, null);
			child.setValue("name", "genericSubProject");
			parent.getChildrenModifiable().add(child);
			tx.commit();
		}

		assertEquals(parent, child.getParent());
		assertChildren(parent, list(child));
	}

	public void testChildren_multipleParent_addValue() {
		Transaction transaction = beginTransaction();
		try {
			AttributedStructuredElementWrapper child_1 = newProject("child 1");
			AttributedStructuredElementWrapper child_2 = newProject("child 2");
			AttributedStructuredElementWrapper grandChild_1_1 = newSubproject(child_1, "grand-child 1.1");

			String message = "Ticket #21392: A StructuredElement must have at most one parent.";
			try {
				AttributeOperations.addValue(child_2, StructuredElement.CHILDREN_ATTR, grandChild_1_1);
				transaction.commit();
			} catch (KnowledgeBaseException ex) {
				assertInstanceof("Must fail with a unique constraint violation.", ex.getCause(), SQLException.class);
				return;
			}
			fail(message);
		} finally {
			transaction.rollback();
		}
	}

	public void testChildren_multipleParent_setValue() {
		Transaction transaction = beginTransaction();
		try {
			AttributedStructuredElementWrapper child_1 = newProject("child 1");
			AttributedStructuredElementWrapper child_2 = newProject("child 2");
			AttributedStructuredElementWrapper grandChild_1_1 = newSubproject(child_1, "grand-child 1.1");

			String message = "Ticket #21392: A StructuredElement must have at most one parent.";
			try {
				child_2.setValue(StructuredElement.CHILDREN_ATTR, list(grandChild_1_1));
				transaction.commit();
			} catch (KnowledgeBaseException ex) {
				assertInstanceof("Must fail with a unique constraint violation.", ex.getCause(), SQLException.class);
				return;
			}
			fail(message);
		} finally {
			transaction.rollback();
		}
	}

	public void testGetChildrenModifiableReadLive() {
		Transaction transaction = beginTransaction();
		try {
			StructuredElement root = getRoot();
			List<StructuredElement> rootChildrenLive = root.getChildrenModifiable();
			assertEquals(emptyList(), rootChildrenLive);

			StructuredElement child_1 = newProject("child 1");
			assertEquals(list(child_1), rootChildrenLive);

			StructuredElement child_2;
			try {
				child_2 = newProject("child 2");
			} catch (CreateElementException ex) {
				throw new RuntimeException(KNOWN_BUG_TICKER_21418, ex);
			}
			assertEquals(list(child_1, child_2), rootChildrenLive);
		} finally {
			transaction.rollback();
		}
	}

	public void testGetChildrenModifiable_Write() {
		Transaction transaction = beginTransaction();
		try {
			StructuredElement child_1 = newProject("child 1");
			StructuredElement child_2 = newProject("child 2");
			StructuredElement grandChild = newSubproject(child_1, "grand-child 1.1");
			assertEquals(list(grandChild), child_1.getChildren());
			StructuredElement removedChild = child_1.getChildrenModifiable().remove(0);
			assertEquals(grandChild, removedChild);
			assertEquals(emptyList(), child_1.getChildren());
			assertEquals(emptyList(), child_2.getChildren());
			child_2.getChildrenModifiable().add(grandChild);
			assertEquals(list(grandChild), child_2.getChildrenModifiable());
			assertEquals(KNOWN_BUG_TICKER_21418, list(grandChild), child_2.getChildren());
		} finally {
			transaction.rollback();
		}
	}

	public void testGetChildrenModifiable_WriteWithCommit() {
		Transaction transaction = beginTransaction();
		StructuredElement child_1;
		StructuredElement child_2;
		StructuredElement grandChild;
		try {
			child_1 = newProject("child 1");
			child_2 = newProject("child 2");
			grandChild = newSubproject(child_1, "grand-child 1.1");
			assertEquals(list(grandChild), child_1.getChildren());
			StructuredElement removedChild = child_1.getChildrenModifiable().remove(0);
			assertEquals(grandChild, removedChild);
			assertEquals(emptyList(), child_1.getChildren());
			assertEquals(emptyList(), child_2.getChildren());
			child_2.getChildrenModifiable().add(grandChild);
		} finally {
			transaction.commit();
		}
		assertEquals(list(grandChild), child_2.getChildrenModifiable());
		assertEquals(KNOWN_BUG_TICKER_21418, list(grandChild), child_2.getChildren());
	}

	private void assertParent(StructuredElement expected, StructuredElement child) {
		assertEquals(expected, child.getParent());
		assertEquals(expected, child.getValue(StructuredElementWrapper.PARENT_ATTR));
		assertEquals(expected, child.tContainer());
		assertEquals(StructuredElement.CHILDREN_ATTR, child.tContainerReference().getName());
	}

	private void assertChildren(StructuredElement parent, List<? extends StructuredElement> expectedChildren) {
		assertEquals(expectedChildren, parent.getChildren());
		assertEquals(expectedChildren, parent.getChildrenModifiable());
		assertEquals(CollectionFactory.set(expectedChildren), CollectionFactory.set(parent.getChildren()));
		assertEquals(expectedChildren, parent.getValue(StructuredElement.CHILDREN_ATTR));
	}

	private AttributedStructuredElementWrapper newProject(String name) {
		return (AttributedStructuredElementWrapper) _projectRoot.createChild(name, "Project");
	}

	private AttributedStructuredElementWrapper newSubproject(StructuredElement parent, String name) {
		return (AttributedStructuredElementWrapper) parent.createChild(name, "Subproject");
	}

	public void testRecursiveDelete() {
		Transaction tx = beginTransaction();
		StructuredElement p1 = newProject("p1");
		StructuredElement subproject1 = newSubproject(p1, "subproject1");
		StructuredElement subproject1_1 = newSubproject(subproject1, "subproject1_1");
		tx.commit();

		Transaction tx2 = beginTransaction();
		p1.tDelete();
		tx2.commit();
		assertFalse("Project was removed.", p1.tValid());
		assertFalse("Ticket #20926: Removing ancestor also removes children.", subproject1_1.tValid());

	}

	private AttributedStructuredElementWrapper getRoot() {
		return _projectRoot;
	}

	private AttributedStructuredElementWrapper getStructureRoot(String structureName) {
		StructuredElementFactory factory = (StructuredElementFactory) DynamicModelService.getFactoryFor(structureName);
		StructuredElement structureRoot = factory.getRoot();
		return (AttributedStructuredElementWrapper) structureRoot;
	}

	private Transaction beginTransaction() {
		return _projectRoot.getKnowledgeBase().beginTransaction();
	}

	/**
	 * Return the suite of tests to perform.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestStructuredElement.class);
	}

}
