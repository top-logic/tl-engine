/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.element.meta.TestWithModelExtension;

import com.top_logic.element.changelog.ChangeLogBuilder;
import com.top_logic.element.changelog.SubtreeFilter;
import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link SubtreeFilter} used together with {@link ChangeLogBuilder}.
 */
@SuppressWarnings("javadoc")
public class TestSubtreeChangeLog extends TestWithModelExtension {

	private static final String MODULE = "test.com.top_logic.element.changelog.TestSubtreeChangeLog";

	private TLModule _testModule;

	private TLClass _nodeType;

	private ModelFactory _factory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		extendApplicationModel(TestSubtreeChangeLog.class, "ext.model.xml");
		_testModule = TLModelUtil.findModule(MODULE);
		_nodeType = (TLClass) _testModule.getType("Node");
		_factory = DynamicModelService.getInstance().getFactory(_testModule);
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = beginTX()) {
			List<TLObject> instances = new ArrayList<>();
			for (TLClass clazz : _testModule.getClasses()) {
				instances.addAll(MetaElementUtil.getAllDirectInstancesOf(clazz, TLObject.class));
			}
			KBUtils.deleteAll(instances);
			_testModule.tDelete();
			tx.commit();
		}
		super.tearDown();
	}

	/**
	 * A creation of a child inside R's subtree is reported; an unrelated creation is not.
	 */
	public void testCreationInSubtree() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");
		TLObject unrelated = createNode(null, "unrelated");

		Collection<ChangeSet> log = log(root);

		assertContains(log, root);
		assertContains(log, child);
		assertDoesNotContain(log, unrelated);
	}

	/**
	 * A modification of a transitive descendant is reported.
	 */
	public void testTransitiveChildModification() {
		TLObject root = createNode(null, "root");
		TLObject mid = createNode(root, "mid");
		TLObject leaf = createNode(mid, "leaf");

		rename(leaf, "leaf-2");

		Collection<ChangeSet> log = log(root);
		assertContains(log, leaf);
	}

	/**
	 * Modifications of an unrelated object are not reported.
	 */
	public void testUnrelatedModificationIgnored() {
		TLObject root = createNode(null, "root");
		TLObject unrelated = createNode(null, "unrelated");

		rename(unrelated, "unrelated-2");

		Collection<ChangeSet> log = log(root);
		assertDoesNotContain(log, unrelated);
	}

	/**
	 * The change log is clamped to R's creation revision as its lower bound.
	 */
	public void testStartClampedToRootCreation() {
		TLObject unrelatedBefore = createNode(null, "before");
		rename(unrelatedBefore, "before-2");

		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");

		Collection<ChangeSet> log = log(root);
		assertContains(log, root);
		assertContains(log, child);
		assertDoesNotContain(log, unrelatedBefore);
	}

	/**
	 * Deletion of a child in the subtree is reported.
	 */
	public void testDeletionInSubtree() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");

		try (Transaction tx = beginTX()) {
			child.tDelete();
			tx.commit();
		}

		Collection<ChangeSet> log = log(root);
		assertContains(log, child);
	}

	private TLObject createNode(TLObject parent, String name) {
		try (Transaction tx = beginTX()) {
			TLObject node = _factory.createObject(_nodeType, parent);
			node.tUpdateByName("name", name);
			if (parent != null) {
				List<Object> current = new ArrayList<>((Collection<?>) parent.tValueByName("children"));
				current.add(node);
				parent.tUpdateByName("children", current);
			}
			tx.commit();
			return node;
		}
	}

	private void rename(TLObject node, String newName) {
		try (Transaction tx = beginTX()) {
			node.tUpdateByName("name", newName);
			tx.commit();
		}
	}

	private Collection<ChangeSet> log(TLObject root) {
		return new ChangeLogBuilder(_kb, ModelService.getApplicationModel())
			.setFilter(new SubtreeFilter(root))
			.build();
	}

	private static void assertContains(Collection<ChangeSet> log, TLObject obj) {
		assertTrue("Log does not mention " + obj, mentions(log, obj));
	}

	private static void assertDoesNotContain(Collection<ChangeSet> log, TLObject obj) {
		assertFalse("Log unexpectedly mentions " + obj, mentions(log, obj));
	}

	private static boolean mentions(Collection<ChangeSet> log, TLObject obj) {
		for (ChangeSet cs : log) {
			for (Change change : cs.getChanges()) {
				if (sameIdentity(change, obj)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean sameIdentity(Change change, TLObject target) {
		TLObject changed = change.getObject();
		if (changed == null) {
			return false;
		}
		return changed.tId().getObjectName().equals(target.tId().getObjectName())
			&& changed.tId().getObjectType().equals(target.tId().getObjectType());
	}

	public static Test suite() {
		return suite(TestSubtreeChangeLog.class);
	}

}
