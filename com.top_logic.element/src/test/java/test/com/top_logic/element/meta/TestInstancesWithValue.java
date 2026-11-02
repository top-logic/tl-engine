/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.model.util.TLModelTest;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.dob.meta.MOClass;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.storage.ColumnStorage;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link AttributeOperations#instancesWithValue(TLClass, String, Object, Class)}: the
 * instances of a type with a value of a primitive attribute stored in a column of the table or as
 * flex attribute, including the instances of subtypes.
 */
@SuppressWarnings("javadoc")
public class TestInstancesWithValue extends TLModelTest {

	private static final String MODULE = "TestXMLInstanceImporter";

	private static final String TABLE = "StructuredElement";

	/** The mandatory column of {@link #TABLE}. */
	private static final String NAME_COLUMN = "name";

	/** Type in the table {@code StructuredElement} with the column {@code name}. */
	private TLClass _typeA;

	private TLClass _uniqueBase;

	private TLClass _uniqueSub;

	private final List<TLObject> _created = new ArrayList<>();

	/** The transaction creating the objects of a test, {@code null} before the first. */
	private Transaction _tx;

	@Override
	protected TLModel setUpModel() {
		return ModelService.getApplicationModel();
	}

	@Override
	protected TLFactory setUpFactory() {
		return ModelService.getInstance().getFactory();
	}

	@Override
	protected void tearDownModel() {
		// Application model is not modified.
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_typeA = testType("A");
		_uniqueBase = testType("UniqueBase");
		_uniqueSub = testType("UniqueSub");
	}

	@Override
	protected void tearDown() throws Exception {
		if (_tx != null) {
			_tx.rollback();
			_tx = null;
			_created.clear();
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			Collections.reverse(_created);
			for (TLObject object : _created) {
				object.tDelete();
			}
			tx.commit();
		}
		_created.clear();
		super.tearDown();
	}

	/**
	 * The attribute {@code name} of the table {@code StructuredElement} is a column, {@code str} a
	 * flex attribute: both find exactly the objects with the value.
	 */
	public void testColumnAndFlex() throws Exception {
		MOClass table = (MOClass) PersistencyLayer.getKnowledgeBase().getMORepository().getMetaObject(TABLE);
		assertNotNull("The value of 'name' is stored in a column.",
			table.getAttributeOrNull(storageAttribute(_typeA, "name")));
		assertNull("The value of 'str' is a flex attribute.", table.getAttributeOrNull(storageAttribute(_typeA, "str")));

		TLObject first = newA("TIWV-first", "TIWV-flex-1");
		TLObject second = newA("TIWV-second", "TIWV-flex-2");
		TLObject sameFlex = newA("TIWV-third", "TIWV-flex-1");
		commit();

		assertEquals(List.of(first), find(_typeA, "name", "TIWV-first"));
		assertEquals(List.of(second), find(_typeA, "name", "TIWV-second"));
		assertEquals(Set.of(first, sameFlex), Set.copyOf(find(_typeA, "str", "TIWV-flex-1")));
		assertEquals(List.of(second), find(_typeA, "str", "TIWV-flex-2"));
	}

	/**
	 * Searching the supertype finds the instances of a subtype, which overrides the attribute;
	 * searching the subtype finds no instance of the supertype. The values are unique, the
	 * attribute has a uniqueness constraint.
	 */
	public void testSubtypeFound() throws Exception {
		TLObject base = newObject(_uniqueBase, "code", "TIWV-code-base");
		TLObject sub = newObject(_uniqueSub, "code", "TIWV-code-sub");
		commit();

		assertEquals(List.of(base), find(_uniqueBase, "code", "TIWV-code-base"));
		assertEquals("An instance of the subtype found by the supertype.", List.of(sub),
			find(_uniqueBase, "code", "TIWV-code-sub"));
		assertEquals(List.of(sub), find(_uniqueSub, "code", "TIWV-code-sub"));
		assertEquals("No instance of the supertype found by the subtype.", List.of(),
			find(_uniqueSub, "code", "TIWV-code-base"));
	}

	public void testNoMatch() throws Exception {
		newA("TIWV-present", "TIWV-flex-present");
		commit();

		assertEquals(List.of(), find(_typeA, "name", "TIWV-absent"));
		assertEquals(List.of(), find(_typeA, "str", "TIWV-absent"));
	}

	/**
	 * Only committed objects are found.
	 */
	public void testUncommittedNotFound() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			TLObject a = getFactory().createObject(_typeA, null, null);
			a.tUpdateByName("name", "TIWV-uncommitted");
			assertEquals(List.of(), find(_typeA, "name", "TIWV-uncommitted"));
		}
	}

	public void testReferenceRejected() {
		try {
			find(_typeA, "other", "TIWV-any");
			fail("A reference is no primitive attribute.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	public void testNullRejected() {
		try {
			find(_typeA, "name", null);
			fail("No value to search for.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	private TLObject newA(String name, String str) {
		TLObject result = newObject(_typeA, "name", name);
		result.tUpdateByName("str", str);
		return result;
	}

	private TLObject newObject(TLClass type, String attribute, Object value) {
		if (_tx == null) {
			_tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
		}
		TLObject result = getFactory().createObject(type, null, null);
		result.tUpdateByName(attribute, value);
		if (type != _typeA) {
			// The mandatory column of the table, not an attribute of the type.
			result.tHandle().setAttributeValue(NAME_COLUMN, value);
		}
		_created.add(result);
		return result;
	}

	private void commit() throws Exception {
		_tx.commit();
		_tx = null;
	}

	private static List<TLObject> find(TLClass type, String attribute, Object value) {
		return AttributeOperations.instancesWithValue(type, attribute, value, TLObject.class);
	}

	private static String storageAttribute(TLClass type, String attribute) {
		return ((ColumnStorage) type.getPartOrFail(attribute).getStorageImplementation()).getStorageAttribute();
	}

	private TLClass testType(String name) {
		return (TLClass) TLModelUtil.findType(getModel(), MODULE + ":" + name);
	}

	public static Test suite() {
		return suiteTransient(
			KBSetup.getSingleKBTest(
				ServiceTestSetup.createSetup(TestInstancesWithValue.class, ModelService.Module.INSTANCE)));
	}

}
