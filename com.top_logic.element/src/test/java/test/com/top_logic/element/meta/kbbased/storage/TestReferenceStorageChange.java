/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.service.KBUtils.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.StorageImplementationFactory;
import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.util.model.ModelService;

/**
 * Test for the adaption of the storage of a persistent {@link TLReference}, when a property
 * determining its storage changes in a running application.
 */
@SuppressWarnings("javadoc")
public class TestReferenceStorageChange extends BasicTestCase {

	private static final Class<TestReferenceStorageChange> THIS_CLASS = TestReferenceStorageChange.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String OWNER_TYPE_NAME = "Owner";

	private static final String CHILD_TYPE_NAME = "Child";

	private TLObject _owner;

	private TLObject _a;

	private TLObject _b;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inTransaction(() -> {
			_owner = instantiate(OWNER_TYPE_NAME);
			_a = instantiate(CHILD_TYPE_NAME);
			_b = instantiate(CHILD_TYPE_NAME);
		});
	}

	/**
	 * A bag becoming unique keeps the first occurrence of each value and stores its links in the
	 * table without duplicates.
	 */
	public void testBagToUnique() {
		TLReference ref = reference("toUnique");
		inTransaction(() -> _owner.tUpdate(ref, List.of(_a, _b, _a)));
		assertEquals(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION, table(ref));

		inTransaction(() -> ref.setBag(false));

		assertEquals(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION, table(ref));
		assertEquals(List.of(_a, _b), _owner.tValue(ref));
		assertLinks(2, ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION);
		assertLinks(0, ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION);
		assertAfterReload(ref, List.of(_a, _b));
	}

	/**
	 * A reference becoming a bag stores its links in the bag table.
	 */
	public void testUniqueToBag() {
		TLReference ref = reference("toBag");
		inTransaction(() -> _owner.tUpdate(ref, List.of(_a, _b)));

		inTransaction(() -> ref.setBag(true));

		assertEquals(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION, table(ref));
		assertEquals(List.of(_a, _b), _owner.tValue(ref));
		assertLinks(2, ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION);

		// The bag now holds a value more than once.
		inTransaction(() -> _owner.tAdd(ref, _a));
		assertEquals(List.of(_a, _b, _a), _owner.tValue(ref));
	}

	/**
	 * A change of the deletion policy moves the links to the table of the new policy.
	 */
	public void testDeletionPolicy() {
		TLReference ref = reference("toVeto");
		inTransaction(() -> _owner.tUpdate(ref, Set.of(_a, _b)));

		inTransaction(() -> ref.setDeletionPolicy(DeletionPolicy.VETO));
		assertEquals(ApplicationObjectUtil.WRAPPER_VETO_ASSOCIATION, table(ref));
		assertEquals(Set.of(_a, _b), _owner.tValue(ref));
		assertLinks(2, ApplicationObjectUtil.WRAPPER_VETO_ASSOCIATION);
		assertLinks(0, ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION);

		inTransaction(() -> ref.setDeletionPolicy(DeletionPolicy.DELETE_REFERER));
		assertEquals(ApplicationObjectUtil.WRAPPER_DELETE_REFERER_ASSOCIATION, table(ref));
		assertEquals(Set.of(_a, _b), _owner.tValue(ref));
		assertLinks(0, ApplicationObjectUtil.WRAPPER_VETO_ASSOCIATION);
		assertAfterReload(ref, Set.of(_a, _b));
	}

	/**
	 * A change of the history type stores the values through the storage of the new history
	 * type.
	 */
	public void testHistoryType() {
		TLReference ref = reference("toHistoric");
		inTransaction(() -> _owner.tUpdate(ref, Set.of(_a, _b)));

		inTransaction(() -> ref.setHistoryType(HistoryType.HISTORIC));
		assertEquals(ApplicationObjectUtil.HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION, table(ref));
		assertEquals(Set.of(_a, _b), current(_owner.tValue(ref)));
		assertLinks(2, ApplicationObjectUtil.HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION);

		inTransaction(() -> ref.setHistoryType(HistoryType.CURRENT));
		assertEquals(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION, table(ref));
		assertEquals(Set.of(_a, _b), _owner.tValue(ref));
		assertLinks(0, ApplicationObjectUtil.HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION);
	}

	/**
	 * A set becoming ordered keeps its values in a list storage.
	 */
	public void testSetToList() {
		TLReference ref = reference("toList");
		inTransaction(() -> _owner.tUpdate(ref, Set.of(_a, _b)));

		inTransaction(() -> ref.setOrdered(true));
		assertTrue(ref.getStorageImplementation() instanceof ListStorage<?>);
		Object value = _owner.tValue(ref);
		assertTrue(value instanceof List<?>);
		assertEquals(Set.of(_a, _b), Set.copyOf((Collection<?>) value));
		assertLinks(2, ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION);
	}

	/**
	 * The parts of a composition survive the change of its storage.
	 */
	public void testCompositeKeepsParts() {
		TLReference ref = reference("parts");
		inTransaction(() -> _owner.tUpdate(ref, Set.of(_a, _b)));
		assertEquals(ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION, table(ref));

		// A composition holds each part once, its storage does not depend on the bag flag.
		inTransaction(() -> ref.setBag(true));
		assertEquals(ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION, table(ref));
		assertEquals(Set.of(_a, _b), Set.copyOf((Collection<?>) _owner.tValue(ref)));

		inTransaction(() -> ref.setDeletionPolicy(DeletionPolicy.VETO));
		assertEquals(ApplicationObjectUtil.STRUCTURE_CHILD_VETO_ASSOCIATION, table(ref));
		assertTrue(_a.tValid());
		assertTrue(_b.tValid());
		assertEquals(Set.of(_a, _b), Set.copyOf((Collection<?>) _owner.tValue(ref)));
		assertLinks(0, ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION);
		assertLinks(2, ApplicationObjectUtil.STRUCTURE_CHILD_VETO_ASSOCIATION);
	}

	/**
	 * A reference without values changes its storage.
	 */
	public void testWithoutValues() {
		TLReference ref = reference("empty");
		inTransaction(() -> ref.setBag(true));
		assertEquals(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION, table(ref));
		assertTrue(((Collection<?>) _owner.tValue(ref)).isEmpty());
	}

	/**
	 * Rolling back the change keeps the old links and the old storage.
	 */
	public void testRollback() {
		TLReference ref = reference("rollback");
		inTransaction(() -> _owner.tUpdate(ref, List.of(_a, _b)));

		KnowledgeBase kb = kb();
		Transaction tx = kb.beginTransaction(ResKey.text("bag change"));
		try {
			ref.setBag(true);
			assertEquals(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION, table(ref));
			assertEquals(List.of(_a, _b), _owner.tValue(ref));
		} finally {
			tx.rollback();
		}

		assertFalse(ref.isBag());
		assertEquals(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION, table(ref));
		assertEquals(List.of(_a, _b), _owner.tValue(ref));
		assertLinks(2, ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION);
		assertLinks(0, ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION);
	}

	/**
	 * Checks the value of the given reference read through a storage created from the committed
	 * state of the reference, as after a restart of the application.
	 */
	private void assertAfterReload(TLReference ref, Object expected) {
		StorageImplementation reloaded = StorageImplementationFactory.createStorageImplementation(ref);
		reloaded.init(ref);
		assertEquals(table(ref), ((LinkStorage<?>) reloaded).getTable());
		assertEquals(expected, reloaded.getAttributeValue(_owner, ref));
	}

	private static Set<TLObject> current(Object values) {
		return ((Collection<?>) values).stream()
			.map(TLObject.class::cast)
			.map(WrapperHistoryUtils::getCurrent)
			.collect(Collectors.toSet());
	}

	/**
	 * Checks the number of links from {@link #_owner} stored in the given table.
	 */
	private void assertLinks(int expected, String table) {
		List<KnowledgeAssociation> stored = kb().search(queryUnresolved(
			filter(
				allOf(table),
				eqBinary(ExpressionFactory.reference(table, DBKnowledgeAssociation.REFERENCE_SOURCE_NAME),
					literal(_owner.tHandle()))),
			KnowledgeAssociation.class));
		assertEquals("Links in table '" + table + "'.", expected, stored.size());
	}

	private static String table(TLReference reference) {
		return ((LinkStorage<?>) reference.getStorageImplementation()).getTable();
	}

	private static TLReference reference(String name) {
		return (TLReference) type(OWNER_TYPE_NAME).getPart(name);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static TLObject instantiate(String typeName) {
		return DynamicModelService.getInstance().createObject(type(typeName));
	}

	private static TLClass type(String typeName) {
		return (TLClass) module().getType(typeName);
	}

	private static TLModule module() {
		return ModelService.getInstance().getModel().getModule(MODULE_NAME);
	}

	public static Test suite() {
		Test kbSetup = KBSetup.getSingleKBTest(THIS_CLASS);
		String configFileName = THIS_CLASS.getSimpleName() + FileUtilities.XML_FILE_ENDING;
		String createFilePath = CustomPropertiesDecorator.createFileName(THIS_CLASS, configFileName);
		Test customConfigSetup = TestUtils.doNotMerge(new CustomPropertiesSetup(kbSetup, createFilePath, true));
		return TLTestSetup.createTLTestSetup(customConfigSetup);
	}

}
