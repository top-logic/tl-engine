/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.event.convert;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import test.com.top_logic.knowledge.service.db2.TestKnowledgeEvent;

import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.event.CachingEventWriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.RevisionModificator;
import com.top_logic.knowledge.event.convert.RevisionModificator.Modus;
import com.top_logic.knowledge.event.convert.RevisionModificator.RevisionAttributes;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;

/**
 * Test case for {@link RevisionModificator} without persistency layer.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestRevisionModificatorUnit extends TestCase {

	private static final String FOO_ATTR = "foo";

	private static final RevisionAttributes REV_ATTRS = RevisionModificator.revAttributes(
		Collections.singletonMap("A", Collections.singleton(FOO_ATTR)));

	private static final MOClass A = new MOClassImpl("A");

	private static final MOClass B = new MOClassImpl("B");

	private CachingEventWriter _buffer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_buffer = new CachingEventWriter();
	}

	@Override
	protected void tearDown() throws Exception {
		_buffer = null;

		super.tearDown();
	}

	public void testInsert() {
		RevisionModificator modificator = new RevisionModificator(REV_ATTRS, 1L);
		modificator.setModus(Modus.replay);

		long r1 = 1;
		rewrite(modificator, cs(r1));
		long r2 = 2;
		rewrite(modificator, cs(r2));

		modificator.setModus(Modus.fakingHistory);

		// Synthetic changes with no original revision numbers.
		long r3 = 3;
		rewrite(modificator, cs(r3));
		long r4 = 4;
		rewrite(modificator, cs(r4));

		modificator.setModus(Modus.replay);

		// Continuing with "original" revisions.
		rewrite(modificator, cs(r3));

		long r5 = 5;
		assertEquals(5, eventCnt());
		assertEquals(r5, lastEvent().getRevision());

		assertEquals(r1, modificator.modifyRevNumber(r1));
		assertEquals(r2, modificator.modifyRevNumber(r2));
		assertEquals(r5, modificator.modifyRevNumber(r3));
	}

	public void testAutoInsert() {
		RevisionModificator modificator = new RevisionModificator(REV_ATTRS, 1L);
		modificator.setModus(Modus.auto);

		long r1 = 1;
		rewrite(modificator, cs(r1));
		long r2 = 2;
		rewrite(modificator, cs(r2));

		// Synthetic changes with no original revision numbers.
		long r3 = 3;
		rewrite(modificator, cs(r3));

		long r4 = 4;
		rewrite(modificator, cs(r4));

		// Continuing with "original" revisions.
		rewrite(modificator, cs(r3));

		long r5 = 5;
		assertEquals(5, eventCnt());
		assertEquals(r5, lastEvent().getRevision());

		assertEquals(r1, modificator.modifyRevNumber(r1));
		assertEquals(r2, modificator.modifyRevNumber(r2));
		assertEquals(r5, modificator.modifyRevNumber(r3));
	}

	public void testAutoInsertAfterAutoDrop() {
		RevisionModificator modificator = new RevisionModificator(REV_ATTRS, 1L);
		modificator.setModus(Modus.auto);

		// Event 1
		long r1 = 1;
		rewrite(modificator, cs(r1));

		// Event 2
		long r2 = 2;
		rewrite(modificator, cs(r2, attrUpdate(r1)));

		// Auto-drop r3
		long r3 = 3;

		// Event 3
		long r4 = 4;
		rewrite(modificator, cs(r4));

		// Event 4
		long r5 = 5;
		rewrite(modificator, cs(r5, attrUpdate(r4)));

		// Auto-drop r6.
		long r6 = 6;

		// Event 5
		long r7 = 7;
		rewrite(modificator, cs(r7));

		// Event 6
		long r8 = 8;
		rewrite(modificator, cs(r8, attrUpdate(r7)));

		// Event 7
		long r9 = 9;
		rewrite(modificator, cs(r9));

		// Event 8
		// Continue with r5 and mark the previous revisions r5 - r9 as auto-inserted.
		rewrite(modificator, cs(r5, attrUpdate(r5)));

		// Event 9
		rewrite(modificator, cs(r6, attrUpdate(r4)));

		// Event 10
		rewrite(modificator, cs(r7, attrUpdate(r2)));

		assertEquals(10, eventCnt());
		assertEquals(10, lastEvent().getRevision());

		assertEquals(r1, modificator.modifyRevNumber(r1));
		assertEquals(r2, modificator.modifyRevNumber(r2));
		assertEquals(r3, modificator.modifyRevNumber(r4));
		assertEquals(r8, modificator.modifyRevNumber(r5));
		assertEquals(r9, modificator.modifyRevNumber(r6));
		assertEquals(10, modificator.modifyRevNumber(r7));

		assertEquals(r1, revValue(2));
		assertEquals(r3, revValue(4));
		assertEquals(r5, revValue(6));
		assertEquals(r8, revValue(8));
		assertEquals(r3, revValue(9));
		assertEquals(r2, revValue(10));
	}

	public void testContinueOriginalSequenceAfterAutoInsert() {
		RevisionModificator modificator = new RevisionModificator(REV_ATTRS, 1L);
		modificator.setModus(Modus.auto);

		// Event 1
		long r1 = 1;
		rewrite(modificator, cs(r1));

		// Auto-drop r2, r3
		long r2 = 2;

		// Event 2
		long r4 = 4;
		rewrite(modificator, cs(r4, attrUpdate(r1)));

		// Auto-drop r5

		// Event 3
		long r6 = 6;
		rewrite(modificator, cs(r6, attrUpdate(r4)));

		// Continue with original sequence. By accident this starts with the revision that is next
		// expected to be created.

		// Event 4
		rewrite(modificator, cs(r4, attrUpdate(r1)));

		// Event 5
		long r5 = 5;
		rewrite(modificator, cs(r5, attrUpdate(r4)));

		assertEquals(r1, modificator.modifyRevNumber(r1));
		assertEquals(r4, modificator.modifyRevNumber(r4));
		assertEquals(r5, modificator.modifyRevNumber(r5));

		assertEquals(r1, revValue(2));
		assertEquals(r2, revValue(3));
		assertEquals(r1, revValue(4));
		assertEquals(r4, revValue(5));

	}

	/**
	 * The value of the revision attribute {@link #FOO_ATTR} in the event with the given ID
	 * (one-based index into the list of all produced change sets).
	 */
	private Object revValue(int eventId) {
		return _buffer.getAllEvents().get(eventId - 1).getUpdates().get(0).getValues().get(FOO_ATTR);
	}

	private ItemUpdate attrUpdate(long r1) {
		return update(A, id("x"), attr(FOO_ATTR, r1));
	}

	public void testDrop() {
		RevisionModificator modificator = new RevisionModificator(REV_ATTRS, 1L);
		modificator.setModus(Modus.replay);

		long r1 = 1;
		rewrite(modificator, cs(r1));
		long r2 = 2;
		rewrite(modificator, cs(r2));

		modificator.setModus(Modus.fakingHistory);

		long r3 = 3;
		long r4 = 4;
		// Announce that revision 3 and 4 have been dropped.
		modificator.dropRevisions(2);

		modificator.setModus(Modus.replay);

		long r5 = 5;
		rewrite(modificator, cs(r5));

		assertEquals(3, eventCnt());
		assertEquals(r3, lastEvent().getRevision());

		assertEquals(r1, modificator.modifyRevNumber(r1));
		assertEquals(r2, modificator.modifyRevNumber(r2));

		// Dropped revisions point to the revision that represents the state before the drop
		// (assuming that dropped revisions make no useful modifications to the system).
		assertEquals(r2, modificator.modifyRevNumber(r3));
		assertEquals(r2, modificator.modifyRevNumber(r4));

		assertEquals(r3, modificator.modifyRevNumber(r5));
	}

	public void testAutoDrop() {
		RevisionModificator modificator = new RevisionModificator(REV_ATTRS, 1L);
		modificator.setModus(Modus.auto);

		long r1 = 1;
		rewrite(modificator, cs(r1));
		long r2 = 2;
		rewrite(modificator, cs(r2));

		// Dropped revisions.
		long r3 = 3;
		long r4 = 4;

		long r5 = 5;
		rewrite(modificator, cs(r5));

		assertEquals(3, eventCnt());
		assertEquals(r3, lastEvent().getRevision());

		assertEquals(r1, modificator.modifyRevNumber(r1));
		assertEquals(r2, modificator.modifyRevNumber(r2));

		// Dropped revisions point to the revision that represents the state before the drop
		// (assuming that dropped revisions make no useful modifications to the system).
		assertEquals(r2, modificator.modifyRevNumber(r3));
		assertEquals(r2, modificator.modifyRevNumber(r4));

		assertEquals(r3, modificator.modifyRevNumber(r5));
	}

	public void testModifyAttributes() {
		RevisionModificator modificator = new RevisionModificator(REV_ATTRS, 1L);
		modificator.setModus(Modus.replay);

		long r1 = 1;
		rewrite(modificator, cs(r1));

		modificator.setModus(Modus.fakingHistory);

		long r2 = 2;

		long r3 = 3;

		// Announce that revision 2 and 3 have been dropped.
		modificator.dropRevisions(2);

		modificator.setModus(Modus.replay);

		long r4 = 4;
		rewrite(modificator, cs(r4));

		TLID x = id("x");
		TLID y = id("y");
		TLID z = id("z");
		long r5 = 5;
		rewrite(modificator, cs(r5, create(A, x,
			attr("a", ref(B, z)),
			attr("b", ref(B, y, r4)),
			attr(FOO_ATTR, r4)
			)));

		assertEquals(3, eventCnt());
		assertEquals(3, lastEvent().getRevision());
		assertEquals(x, lastEvent().getCreations().get(0).getObjectName());
		assertEquals(ref(B, z), lastEvent().getCreations().get(0).getValues().get("a"));
		assertEquals(ref(B, y, r2), lastEvent().getCreations().get(0).getValues().get("b"));
		assertEquals(r2, lastEvent().getCreations().get(0).getValues().get(FOO_ATTR));

		assertEquals(r1, modificator.modifyRevNumber(r1));
		assertEquals(r2, modificator.modifyRevNumber(r4));
		assertEquals(r3, modificator.modifyRevNumber(r5));
	}

	private TLID id(String name) {
		return StringID.valueOf(name);
	}

	private ObjectKey ref(MOClass type, TLID id) {
		return ref(type, id, Revision.CURRENT_REV);
	}

	private ObjectKey ref(MOClass type, TLID id, long rev) {
		return new DefaultObjectKey(1L, rev, type, id);
	}

	private ItemCreation create(MOClass type, TLID id, Attr... attrs) {
		ObjectCreation event = new ObjectCreation(0L, new ObjectBranchId(1L, type, id));
		set(event, attrs);
		return event;
	}

	private ItemUpdate update(MOClass type, TLID id, Attr... attrs) {
		ItemUpdate event = new ItemUpdate(0L, new ObjectBranchId(1L, type, id), false);
		set(event, attrs);
		return event;
	}

	private void set(ItemChange event, Attr... attrs) {
		for (Attr attr : attrs) {
			event.setValue(attr.getName(), null, attr.getValue());
		}
	}

	private Attr attr(String name, Object value) {
		return new Attr(name, value);
	}

	private void rewrite(EventRewriter rewriter, ChangeSet cs) {
		rewriter.rewrite(cs, _buffer);
	}

	private ChangeSet lastEvent() {
		List<ChangeSet> all = _buffer.getAllEvents();
		return all.get(all.size() - 1);
	}

	private int eventCnt() {
		return _buffer.getAllEvents().size();
	}

	private ChangeSet cs(long rev, KnowledgeEvent... events) {
		ChangeSet cs = new ChangeSet(rev);
		cs.setCommit(TestKnowledgeEvent.newCommit(rev, TestRevisionModificatorUnit.class));
		for (KnowledgeEvent event : events) {
			event.setRevision(rev);
			cs.add(event);
		}
		return cs;
	}

	static class Attr {
	
		private final String _name;
	
		private final Object _value;
	
		public Attr(String name, Object value) {
			_name = name;
			_value = value;
		}
	
		public String getName() {
			return _name;
		}
	
		public Object getValue() {
			return _value;
		}
	
	}

	/**
	 * Creates {@link RevisionModificator} which allows rewrite {@link ChangeSet} sequence with
	 * arbitrary first revision into the given {@link KnowledgeBase}.
	 */
	public static RevisionModificator newRevisionModificator(KnowledgeBase kb) {
		long nextRevision = kb.getHistoryManager().getLastRevision() + 1;
		RevisionModificator modificator =
			new RevisionModificator(RevisionAttributes.NO_REVISION_ATTRIBUTES, nextRevision);
		modificator.setModus(Modus.auto);
		return modificator;
	}

}
