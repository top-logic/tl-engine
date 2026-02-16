/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.element.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import junit.framework.Test;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.cs.TLObjectChange;
import com.top_logic.model.cs.TLObjectChangeSet;
import com.top_logic.model.cs.TLObjectCreation;
import com.top_logic.model.cs.TLObjectDeletion;
import com.top_logic.model.cs.TLObjectUpdate;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests the {@link TLObjectChangeSet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestTLObjectChangeSet extends AbstractTLObjectChangeSetTest {

	private TLModule _testModule;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		extendApplicationModel(TestTLObjectChangeSet.class, "ext.model.xml");
		_testModule = TLModelUtil.findModule("test.com.top_logic.element.model.TestTLObjectChangeSet");
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = beginTX()) {
			TLReference tTypeAttr = TlModelFactory.getTTypeTLObjectAttr();
			_testModule.getClasses().stream().forEach(type -> KBUtils.deleteAll(tTypeAttr.getReferers(type)));
			_testModule.tDelete();
			tx.commit();
		}
		super.tearDown();
	}

	/**
	 * Test creating reference and value.
	 */
	public void testCreatingTLReference() {
		ModelFactory factory = DynamicModelService.getInstance().getFactory(_testModule);
		
		TLClass aType = (TLClass) _testModule.getType("A");
		TLClass bType = (TLClass) _testModule.getType("B");

		AtomicReference<TLObject> aRef = new AtomicReference<>();
		AtomicReference<TLObject> bRef = new AtomicReference<>();
		try (Transaction tx = beginTX()) {
			TLObject a = factory.createObject(aType);
			a.tUpdateByName("name", "a1");
			aRef.set(a);
			TLObject b = factory.createObject(bType, a);
			b.tUpdateByName("name", "b1");
			bRef.set(b);
			tx.commit();
		}
		
		testNextCommit(change -> {
			Map<TLObject, TLObjectUpdate> updates = toMap(change.updates());
			TLObjectUpdate update = updates.get(aRef.get());
			TLStructuredTypePart bRefPart = aType.getPart("otherB");
			assertEquals(bRef.get(), update.newValues().get(bRefPart));
			assertEmpty(true, update.oldValues().keySet());
		});
		try (Transaction tx = beginTX()) {
			TLModelUtil.addReference(aType, "otherB", bType, "sourceA");
			aRef.get().tUpdateByName("otherB", bRef.get());
			tx.commit();
		}
		
	}		
	/**
	 * Test deleting references.
	 */
	public void testDeleteTLReference() {
		ModelFactory factory = DynamicModelService.getInstance().getFactory(_testModule);

		TLClass aType = (TLClass) _testModule.getType("A");
		TLClass bType = (TLClass) _testModule.getType("B");

		AtomicReference<TLObject> aRef = new AtomicReference<>();
		AtomicReference<TLObject> bRef = new AtomicReference<>();
		try (Transaction tx = beginTX()) {
			TLObject a = factory.createObject(aType);
			a.tUpdateByName("name", "a1");
			aRef.set(a);
			TLObject b = factory.createObject(bType, a);
			b.tUpdateByName("name", "b1");
			bRef.set(b);
			a.tUpdateByName("b", b);
			tx.commit();
		}

		TLStructuredTypePart bReference = aType.getPart("b");
		testNextCommit(change -> {
			assertEquals(0, change.creations().size());

			Map<TLObject, TLObjectUpdate> updates = toMap(change.updates());
			TLObjectUpdate typeUpdate = updates.get(aType);
			assertNull("Reference to deleted part is just a back reference, therefore not reported!", typeUpdate);
			TLObjectUpdate objectUpdate = updates.get(aRef.get());
			assertEquals(bRef.get(), objectUpdate.oldValues().get(bReference));
			assertNull(objectUpdate.newValues().get(bReference));
			assertFalse("Attribute was deleted.", objectUpdate.newValues().containsKey(bReference));

			List<TLObjectDeletion> deletions = change.deletions();
			assertEquals(1, deletions.size());
			assertEquals(bReference, deletions.get(0).object());

		});
		try (Transaction tx = beginTX()) {
			bReference.tDelete();
			tx.commit();
		}
		assertTrue(aRef.get().tValid());
		assertTrue(bRef.get().tValid());

	}

	/**
	 * Default tests.
	 */
	public void testDefaultChanges() {
		ModelFactory factory = DynamicModelService.getInstance().getFactory(_testModule);

		TLClass aType = (TLClass) _testModule.getType("A");
		TLClass bType = (TLClass) _testModule.getType("B");

		AtomicReference<TLObject> aRef = new AtomicReference<>();
		AtomicReference<TLObject> bRef = new AtomicReference<>();
		testNextCommit(changeSet -> {
			List<TLObjectCreation> creations = changeSet.creations();
			assertEquals(2, creations.size());
			TLObject a = aRef.get();
			TLObject b = bRef.get();
			TLObject o1 = creations.get(0).object();
			TLObject o2 = creations.get(1).object();
			if (a == o1) {
				assertSame(b, o2);
			} else if (b == o1) {
				assertSame(a, o2);
			} else {
				fail("Unexpected creation: " + o1 + " " + a + " " + b);
			}

			assertEquals(0, changeSet.updates().size());
			
			assertEquals(0, changeSet.deletions().size());
		});

		try (Transaction tx = beginTX()) {
			TLObject a = factory.createObject(aType);
			a.tUpdateByName("name", "a1");
			aRef.set(a);
			TLObject b = factory.createObject(bType, a);
			b.tUpdateByName("name", "b1");
			bRef.set(b);
			a.tUpdateByName("b", b);
			tx.commit();
		}

		testNextCommit(changeSet -> {
			assertEquals(0, changeSet.creations().size());
			
			List<TLObjectUpdate> updates = changeSet.updates();
			assertEquals(1, updates.size());
			TLObjectUpdate update = updates.get(0);
			assertEquals(aRef.get(), update.object());
			TLStructuredTypePart changedPart = aType.getPart("b");
			Map<TLStructuredTypePart, Object> newValues = update.newValues();
			assertNull(newValues.get(changedPart));
			assertTrue(newValues.containsKey(changedPart));
			assertEquals(bRef.get(), update.oldValues().get(changedPart));
			
			List<TLObjectDeletion> deletions = changeSet.deletions();
			assertEquals(1, deletions.size());
			assertEquals(bRef.get(), deletions.get(0).object());
		});

		try (Transaction tx = beginTX()) {
			bRef.get().tDelete();
			tx.commit();
		}
		testNextCommit(changeSet -> {
			assertEquals(0, changeSet.creations().size());
			assertEquals(0, changeSet.deletions().size());
			List<TLObjectUpdate> updates = changeSet.updates();

			assertEquals(1, updates.size());
			TLObjectUpdate update = updates.get(0);
			assertEquals(aRef.get(), update.object());
			TLStructuredTypePart namePart = aType.getPart("name");
			TLStructuredTypePart namedef = namePart.getDefinition();
			assertNotEquals("Test that the correct part is contained in the changes.", namedef, namePart);
			assertEquals("a1", update.oldValues().get(namePart));
			assertEquals("a1_new", update.newValues().get(namePart));
		});
		
		try (Transaction tx = beginTX()) {
			aRef.get().tUpdateByName("name", "a1_new");
			tx.commit();
		}

	}

	private static <T extends TLObjectChange> Map<TLObject, T> toMap(Collection<? extends T> changes) {
		return changes.stream().collect(Collectors.toMap(TLObjectChange::object, Function.identity()));
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestTLObjectChangeSet}.
	 */
	public static Test suite() {
		return suiteForChangeSet(TestTLObjectChangeSet.class);
	}
}

