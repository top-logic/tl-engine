/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.StringID;
import com.top_logic.basic.func.Identity;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Test case for {@link ChangeSet#merge(com.top_logic.knowledge.event.KnowledgeEvent)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestKnowledgeEventMerge extends TestCase {

	private static final MetaObject TYPE = new MOClassImpl("A");

	static boolean KEEP_OLD;

	public void testMergeCreate() {
		ChangeSet cs = new ChangeSet(1);

		cs.merge(create("a"));
		assertEquals(1, cs.getCreations().size());
		assertEquals(null, cs.getCreations().get(0).getValues().get("foo"));
		assertEquals(null, cs.getCreations().get(0).getOldValue("foo"));

		cs.merge(update("a", "foo", "bar"));
		assertEquals(1, cs.getCreations().size());
		assertEquals("bar", cs.getCreations().get(0).getValues().get("foo"));
		assertEquals(null, cs.getCreations().get(0).getOldValue("foo"));

		cs.merge(update("a", "foo", "baz"));
		assertEquals(1, cs.getCreations().size());
		assertEquals("baz", cs.getCreations().get(0).getValues().get("foo"));
		assertEquals(null, cs.getCreations().get(0).getOldValue("foo"));

		cs.merge(delete("a"));
		assertEquals(0, cs.getCreations().size());
		assertEquals(0, cs.getUpdates().size());
		assertEquals(0, cs.getDeletions().size());
	}

	public void testMergeUpdate() {
		ChangeSet cs = new ChangeSet(1);

		cs.merge(update("a", "foo", "orig", "bar"));
		assertEquals(1, cs.getUpdates().size());
		assertEquals("bar", cs.getUpdates().get(0).getValues().get("foo"));
		assertOldVal("orig", cs.getUpdates().get(0).getOldValue("foo"));

		cs.merge(update("a", "foo", "orig2", "baz"));
		assertEquals(1, cs.getUpdates().size());
		assertEquals("baz", cs.getUpdates().get(0).getValues().get("foo"));
		assertOldVal("orig2", cs.getUpdates().get(0).getOldValue("foo"));

		cs.merge(delete("a"));
		assertEquals(0, cs.getCreations().size());
		assertEquals(0, cs.getUpdates().size());
		assertEquals(1, cs.getDeletions().size());
		assertOldVal("orig2", cs.getDeletions().get(0).getOldValue("foo"));
	}

	public void testMergeDelete() {
		ChangeSet cs = new ChangeSet(1);

		cs.merge(delete("a"));
		assertEquals(1, cs.getDeletions().size());
		assertEquals(null, cs.getDeletions().get(0).getValues().get("foo"));
		assertOldVal(null, cs.getDeletions().get(0).getOldValue("foo"));

		cs.merge(update("a", "foo", "orig", null));
		assertEquals(1, cs.getDeletions().size());
		assertOldVal("orig", cs.getDeletions().get(0).getOldValue("foo"));
	}

	public void testMergeAdd() {
		ChangeSet cs = new ChangeSet(1);

		cs.merge(create("a"));
		cs.merge(update("b"));
		cs.merge(delete("c"));
		cs.mergeAll(list(create("d"), update("e"), delete("f")));

		assertEquals(2, cs.getCreations().size());
		assertEquals(2, cs.getUpdates().size());
		assertEquals(2, cs.getDeletions().size());
	}

	@SuppressWarnings("unused")
	public void testLargeMerge() {
		ChangeSet cs = new ChangeSet(1);
		int csSize = 500000;
		int numberUpdates = 100000;
		for (int i = 0; i < csSize; i++) {
			cs.getCreations().add(create("a" + i));
		}
		List<ItemUpdate> updates = new ArrayList<>(numberUpdates);
		for (int i = 0; i < numberUpdates; i++) {
			ItemUpdate update = update("a" + i, "attr", "" + i);
			updates.add(update);
		}
		if (false) {
			updates.forEach(update -> cs.merge(update));
		} else {
			cs.mergeAll(updates);
		}

		List<ObjectCreation> creations = cs.getCreations();
		assertEquals(csSize, creations.size());
		Map<ObjectBranchId, ObjectCreation> events = creations
			.stream()
			.collect(Collectors.toMap(evt -> evt.getObjectId(), Identity.getInstance()));
		for (int i = 0; i < numberUpdates; i++) {
			ObjectCreation creation = events.get(id("a" + i));
			assertNotNull(creation);
			assertEquals("" + i, creation.getValues().get("attr"));
		}
		for (int i = numberUpdates; i < csSize; i++) {
			ObjectCreation creation = events.get(id("a" + i));
			assertNotNull(creation);
			assertFalse(creation.getValues().containsKey("attr"));
		}
	}

	private ObjectCreation create(String name) {
		return new ObjectCreation(1, id(name));
	}

	private ItemUpdate update(String name, String attribute, String newValue) {
		return update(name, attribute, null, newValue);
	}

	private ItemUpdate update(String name, String attribute, String oldValue, String newValue) {
		ItemUpdate update = update(name);
		update.setValue(attribute, oldValue, newValue);
		return update;
	}

	private ItemUpdate update(String name) {
		ItemUpdate update = new ItemUpdate(1, id(name), KEEP_OLD);
		return update;
	}

	private ItemDeletion delete(String name) {
		ItemDeletion delete = new ItemDeletion(1, id(name));
		return delete;
	}

	private ObjectBranchId id(String name) {
		return new ObjectBranchId(1, TYPE, StringID.valueOf(name));
	}

	private void assertOldVal(Object expected, Object actual) {
		if (KEEP_OLD) {
			assertEquals(expected, actual);
		} else {
			assertEquals(null, actual);
		}
	}

	public static class Setup extends TestSetup {

		private final boolean _keepOld;

		public Setup(Test test, boolean keepOld) {
			super(test);
			_keepOld = keepOld;
		}

		@Override
		protected void setUp() throws Exception {
			TestKnowledgeEventMerge.KEEP_OLD = _keepOld;
		}

	}

	public static Test suite() {
		TestSuite result = new TestSuite();
		result.addTest(new Setup(new TestSuite(TestKnowledgeEventMerge.class), true));
		result.addTest(new Setup(new TestSuite(TestKnowledgeEventMerge.class), false));
		return result;
	}

}
