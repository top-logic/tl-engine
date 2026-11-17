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
import com.top_logic.knowledge.service.Revision;
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
	 * Opt-out composition — mirror of {@link #testOptOutComposition()} but mirroring the UI
	 * pattern: create child without container, then attach to the opt-out composite reference.
	 * The object creation must not appear in the log.
	 */
	public void testOptOutCompositionCreateThenAttach() {
		TLObject root = createNode("root");
		TLObject noiseChild;
		try (Transaction tx = beginTX()) {
			noiseChild = _factory.createObject(_nodeType, null);
			noiseChild.tUpdateByName("name", "noise-child");
			List<Object> noise = new ArrayList<>((Collection<?>) root.tValueByName("noise"));
			noise.add(noiseChild);
			root.tUpdateByName("noise", noise);
			tx.commit();
		}

		Collection<ChangeSet> log = log(root);
		assertDoesNotContain(log, noiseChild);
	}

	/**
	 * The isolated {@code Update} that modifies only an opt-out reference on the root is
	 * filtered out — the user intention "this relationship is not part of my change log"
	 * applies to edits of the relationship itself, not just to its elements.
	 */
	public void testOptOutUpdateOnRootIsFiltered() {
		TLObject root = createNode("root");
		TLObject noiseChild;
		try (Transaction tx = beginTX()) {
			noiseChild = _factory.createObject(_nodeType, null);
			noiseChild.tUpdateByName("name", "noise-child");
			tx.commit();
		}

		// Dedicated transaction: the only change on 'root' is the opt-out ref update.
		try (Transaction tx = beginTX()) {
			List<Object> noise = new ArrayList<>((Collection<?>) root.tValueByName("noise"));
			noise.add(noiseChild);
			root.tUpdateByName("noise", noise);
			tx.commit();
		}

		Collection<ChangeSet> log = log(root);
		assertFalse("Log must not contain an update that only touches an opt-out reference",
			hasUpdateOnRef(log, root, "noise"));
	}

	private static boolean hasUpdateOnRef(Collection<ChangeSet> log, TLObject target, String refName) {
		for (ChangeSet cs : log) {
			for (Change change : cs.getChanges()) {
				if (!(change instanceof com.top_logic.element.changelog.model.Update)) {
					continue;
				}
				if (!sameIdentity(change, target)) {
					continue;
				}
				for (com.top_logic.element.changelog.model.Modification m :
						((com.top_logic.element.changelog.model.Update) change).getModifications()) {
					com.top_logic.model.TLStructuredTypePart part = m.getPart();
					if (part != null && refName.equals(part.getName())) {
						return true;
					}
				}
			}
		}
		return false;
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

	/**
	 * Multi-valued navigation: a node referenced through the opt-in reference {@code peers} of two
	 * roots belongs to the change subtree of both, but not to the one of a root not referencing it.
	 */
	public void testPeerOfTwoRoots() {
		TLObject r1 = createNode("r1");
		TLObject r2 = createNode("r2");
		TLObject r3 = createNode("r3");
		TLObject x = createNode("x");
		addPeer(r1, x);
		addPeer(r2, x);

		Revision modification = rename(x, "x-2");

		assertTrue(mentionsAt(log(r1), x, modification));
		assertTrue(mentionsAt(log(r2), x, modification));
		assertFalse(containsRevision(log(r3), modification));
	}

	/**
	 * A node removed from the opt-in reference {@code peers} leaves the change subtree of the
	 * former owner: its later modifications are no longer part of the owner's change log.
	 */
	public void testRemovedPeerLeavesSubtree() {
		TLObject r1 = createNode("r1");
		TLObject x = createNode("x");
		addPeer(r1, x);

		Revision whilePeer = rename(x, "x-2");

		try (Transaction tx = beginTX()) {
			List<Object> peers = new ArrayList<>((Collection<?>) r1.tValueByName("peers"));
			peers.remove(x);
			r1.tUpdateByName("peers", peers);
			tx.commit();
		}

		Revision afterRemoval = rename(x, "x-3");

		Collection<ChangeSet> log = log(r1);
		assertTrue(mentionsAt(log, x, whilePeer));
		assertFalse(containsRevision(log, afterRemoval));
	}

	/**
	 * Membership is evaluated in the revision of each change: a composition part modified in its
	 * first container, moved to a second one and modified there shows its first modification in
	 * the log of the first container only, the second modification in the log of the second
	 * container only, and the move in both.
	 */
	public void testRootChangesOverTime() {
		TLObject a = createNode("a");
		TLObject b = createNode("b");
		TLObject x = createChild(a, "x");

		Revision change1 = rename(x, "x-1");
		Revision change2 = moveChild(x, a, b);
		Revision change3 = rename(x, "x-3");

		Collection<ChangeSet> logA = log(a);
		Collection<ChangeSet> logB = log(b);

		assertTrue(mentionsAt(logA, x, change1));
		assertFalse(containsRevision(logB, change1));

		assertTrue(containsRevision(logA, change2));
		assertTrue(containsRevision(logB, change2));

		assertTrue(mentionsAt(logB, x, change3));
		assertFalse(containsRevision(logA, change3));
	}

	/**
	 * Moving a part from {@code a.children} to {@code b.children} is part of the change log of both
	 * containers: for {@code a} through the state in the revision before the move, for {@code b}
	 * through the state in the revision of the move.
	 */
	public void testMoveOutOfComposition() {
		TLObject a = createNode("a");
		TLObject b = createNode("b");
		TLObject x = createChild(a, "x");

		Revision move = moveChild(x, a, b);

		assertTrue(containsRevision(log(a), move));
		assertTrue(containsRevision(log(b), move));
	}

	private TLObject createChild(TLObject parent, String name) {
		try (Transaction tx = beginTX()) {
			TLObject child = _factory.createObject(_nodeType, parent);
			child.tUpdateByName("name", name);
			List<Object> children = new ArrayList<>((Collection<?>) parent.tValueByName("children"));
			children.add(child);
			parent.tUpdateByName("children", children);
			tx.commit();
			return child;
		}
	}

	private Revision moveChild(TLObject child, TLObject from, TLObject to) {
		try (Transaction tx = beginTX()) {
			List<Object> fromChildren = new ArrayList<>((Collection<?>) from.tValueByName("children"));
			fromChildren.remove(child);
			from.tUpdateByName("children", fromChildren);
			List<Object> toChildren = new ArrayList<>((Collection<?>) to.tValueByName("children"));
			toChildren.add(child);
			to.tUpdateByName("children", toChildren);
			tx.commit();
			return tx.getCommitRevision();
		}
	}

	private void addPeer(TLObject owner, TLObject peer) {
		try (Transaction tx = beginTX()) {
			List<Object> peers = new ArrayList<>((Collection<?>) owner.tValueByName("peers"));
			peers.add(peer);
			owner.tUpdateByName("peers", peers);
			tx.commit();
		}
	}

	private static boolean containsRevision(Collection<ChangeSet> log, Revision revision) {
		return findRevision(log, revision) != null;
	}

	private static boolean mentionsAt(Collection<ChangeSet> log, TLObject obj, Revision revision) {
		ChangeSet cs = findRevision(log, revision);
		if (cs == null) {
			return false;
		}
		for (Change change : cs.getChanges()) {
			if (sameIdentity(change, obj)) {
				return true;
			}
		}
		return false;
	}

	private static ChangeSet findRevision(Collection<ChangeSet> log, Revision revision) {
		for (ChangeSet cs : log) {
			if (cs.getRevision().getCommitNumber() == revision.getCommitNumber()) {
				return cs;
			}
		}
		return null;
	}

	private TLObject createNode(String name) {
		try (Transaction tx = beginTX()) {
			TLObject node = _factory.createObject(_nodeType, null);
			node.tUpdateByName("name", name);
			tx.commit();
			return node;
		}
	}

	private Revision rename(TLObject node, String newName) {
		try (Transaction tx = beginTX()) {
			node.tUpdateByName("name", newName);
			tx.commit();
			return tx.getCommitRevision();
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
