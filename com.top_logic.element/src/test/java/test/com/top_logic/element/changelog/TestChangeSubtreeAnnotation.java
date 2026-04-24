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
import com.top_logic.model.annotate.TLChangeSubtree;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link TLChangeSubtree} driving {@link SubtreeFilter}.
 *
 * <p>
 * Reuses the {@code TestSubtreeChangeLog} test model, which declares a single {@code Node} class
 * with references covering all annotation scenarios: default composition ({@code children}),
 * opt-out composition ({@code noise}), opt-in forward-ref ({@code peers}), opt-in back-ref
 * ({@code home}/{@code homeMembers}) and mutually annotated pair ({@code twinA}/{@code twinB}).
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestChangeSubtreeAnnotation extends TestWithModelExtension {

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
	 * Opt-out composition: children under the {@code noise} composite reference do not belong to
	 * the change-subtree of their container.
	 */
	public void testOptOutComposition() {
		TLObject root = createNode("root");
		TLObject noiseChild;
		try (Transaction tx = beginTX()) {
			noiseChild = _factory.createObject(_nodeType, root);
			noiseChild.tUpdateByName("name", "noise-child");
			List<Object> noise = new ArrayList<>((Collection<?>) root.tValueByName("noise"));
			noise.add(noiseChild);
			root.tUpdateByName("noise", noise);
			tx.commit();
		}
		rename(noiseChild, "noise-child-2");

		Collection<ChangeSet> log = log(root);
		assertDoesNotContain(log, noiseChild);
	}

	/**
	 * Opt-in forward-ref: the owner of an annotated forward-reference sees changes to the
	 * referenced targets in its change-subtree.
	 */
	public void testOptInForwardRef() {
		TLObject root = createNode("root");
		TLObject peer = createNode("peer");
		try (Transaction tx = beginTX()) {
			List<Object> peers = new ArrayList<>((Collection<?>) root.tValueByName("peers"));
			peers.add(peer);
			root.tUpdateByName("peers", peers);
			tx.commit();
		}
		rename(peer, "peer-2");

		Collection<ChangeSet> log = log(root);
		assertContains(log, peer);
	}

	/**
	 * Opt-in back-ref: an annotation on the back side of a pair identifies a root-direction
	 * where the back-ref's owner is the root side.
	 */
	public void testOptInBackRef() {
		// homeMembers (forward, multiple) <-> home (back, annotated). Annotation on 'home'
		// makes the owner of 'home' (the member) the root side; target of home = a Node
		// referenced as 'home' belongs to the member's change-subtree.
		TLObject member = createNode("member");
		TLObject home = createNode("home");
		addHomeMember(home, member);
		rename(home, "home-2");

		Collection<ChangeSet> log = log(member);
		assertContains(log, home);
	}

	/**
	 * Dual-state check: when a member is moved from home A to home B, the changes on both home
	 * nodes are visible in the member's change-log — the old home through the pre-move state,
	 * the new home through the post-move state.
	 */
	public void testDualStateMove() {
		TLObject member = createNode("member");
		TLObject homeA = createNode("homeA");
		TLObject homeB = createNode("homeB");
		addHomeMember(homeA, member);

		try (Transaction tx = beginTX()) {
			List<Object> aMembers = new ArrayList<>((Collection<?>) homeA.tValueByName("homeMembers"));
			aMembers.remove(member);
			homeA.tUpdateByName("homeMembers", aMembers);
			List<Object> bMembers = new ArrayList<>((Collection<?>) homeB.tValueByName("homeMembers"));
			bMembers.add(member);
			homeB.tUpdateByName("homeMembers", bMembers);
			tx.commit();
		}

		Collection<ChangeSet> log = log(member);
		assertContains(log, homeA);
		assertContains(log, homeB);
	}

	private void addHomeMember(TLObject home, TLObject member) {
		try (Transaction tx = beginTX()) {
			List<Object> members = new ArrayList<>((Collection<?>) home.tValueByName("homeMembers"));
			members.add(member);
			home.tUpdateByName("homeMembers", members);
			tx.commit();
		}
	}

	/**
	 * Mutual annotation on both ends of a pair: navigation terminates without infinite loop and
	 * each end sees the other in its subtree.
	 */
	public void testMutualAnnotationCycleGuard() {
		TLObject a = createNode("a");
		TLObject b = createNode("b");
		try (Transaction tx = beginTX()) {
			// twinA is the back-side (inverse-reference="twinB"); twinB is the forward-side.
			// Setting a.twinB = b establishes the pair.
			a.tUpdateByName("twinB", b);
			tx.commit();
		}
		rename(b, "b-2");

		Collection<ChangeSet> fromA = log(a);
		assertContains(fromA, b);

		rename(a, "a-2");
		Collection<ChangeSet> fromB = log(b);
		assertContains(fromB, a);
	}

	/**
	 * Changes to an object that is neither referenced nor contained are not part of the log.
	 */
	public void testUnrelatedIgnored() {
		TLObject root = createNode("root");
		TLObject unrelated = createNode("unrelated");
		rename(unrelated, "unrelated-2");

		Collection<ChangeSet> log = log(root);
		assertDoesNotContain(log, unrelated);
	}

	/**
	 * Combination: a Node that is both a composition child and a peer appears once (existential
	 * subtree membership).
	 */
	public void testCombinedMembership() {
		TLObject root = createNode("root");
		TLObject child;
		try (Transaction tx = beginTX()) {
			child = _factory.createObject(_nodeType, root);
			child.tUpdateByName("name", "child");
			List<Object> children = new ArrayList<>((Collection<?>) root.tValueByName("children"));
			children.add(child);
			root.tUpdateByName("children", children);
			// ...and also add the same node as a peer.
			List<Object> peers = new ArrayList<>((Collection<?>) root.tValueByName("peers"));
			peers.add(child);
			root.tUpdateByName("peers", peers);
			tx.commit();
		}
		rename(child, "child-2");

		Collection<ChangeSet> log = log(root);
		assertContains(log, child);
	}

	private TLObject createNode(String name) {
		try (Transaction tx = beginTX()) {
			TLObject node = _factory.createObject(_nodeType, null);
			node.tUpdateByName("name", name);
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
		return suite(TestChangeSubtreeAnnotation.class);
	}

}
