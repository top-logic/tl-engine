/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.knowledge.service.KBUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.model.ModelService;

/**
 * Test for the uniqueness constraint of the link tables storing references that are no
 * {@link TLStructuredTypePart#isBag() bag}, and for the separate tables storing bags.
 */
@SuppressWarnings("javadoc")
public class TestLinkTableUniqueness extends BasicTestCase {

	private static final Class<TestLinkTableUniqueness> THIS_CLASS = TestLinkTableUniqueness.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String PARENT_TYPE_NAME = "Parent";

	private static final String CHILD_TYPE_NAME = "Child";

	private static final String SET_ATTRIBUTE_NAME = "set";

	private static final String BAG_ATTRIBUTE_NAME = "bag";

	private static final String HISTORIC_ATTRIBUTE_NAME = "historic";

	private TLObject _parent;

	private TLObject _a;

	private TLObject _b;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inTransaction(() -> {
			_parent = instantiate(PARENT_TYPE_NAME);
			_a = instantiate(CHILD_TYPE_NAME);
			_b = instantiate(CHILD_TYPE_NAME);
		});
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> deleteAll(List.of(_parent, _a, _b)));
		super.tearDown();
	}

	/**
	 * Checks that the links of a bag reference are stored in a bag table, and the links of a
	 * reference that is no bag in the standard table.
	 */
	public void testTables() {
		assertFalse(set().isBag());
		assertTrue(bag().isBag());
		assertEquals(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION, table(set()));
		assertEquals(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION, table(bag()));
		assertEquals(ApplicationObjectUtil.HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION, table(historic()));

		inTransaction(() -> {
			_parent.tAdd(set(), _a);
			_parent.tAdd(bag(), _b);
			_parent.tAdd(bag(), _b);
		});
		assertEquals(Set.of(_a), _parent.tValue(set()));
		assertEquals(List.of(_b, _b), _parent.tValue(bag()));
		assertEquals(1, links(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION).size());
		assertEquals(2, links(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_BAG_ASSOCIATION).size());
	}

	/**
	 * Checks that the database rejects a second link with the same source, attribute, and
	 * destination for a reference that is no bag.
	 */
	public void testDuplicateRejected() {
		inTransaction(() -> _parent.tAdd(set(), _a));

		assertDuplicateRejected(set(), _a);
		assertEquals(Set.of(_a), _parent.tValue(set()));
	}

	/**
	 * Checks that a bag reference may hold the same value more than once.
	 */
	public void testBagDuplicateAccepted() {
		inTransaction(() -> _parent.tAdd(bag(), _a));

		inTransaction(() -> createLink(bag(), _a));
		assertEquals(2, links(table(bag())).size());
		assertEquals(2, ((Collection<?>) _parent.tValue(bag())).size());
		assertTrue(((Collection<?>) _parent.tValue(bag())).stream().allMatch(_a::equals));
	}

	/**
	 * Checks that a value can be removed and added again within a single commit.
	 */
	public void testRemoveAndAddInOneCommit() {
		inTransaction(() -> _parent.tAdd(set(), _a));

		inTransaction(() -> {
			List<KnowledgeAssociation> links = links(table(set()));
			assertEquals(1, links.size());
			links.get(0).delete();
			createLink(set(), _a);
		});
		assertEquals(Set.of(_a), _parent.tValue(set()));
		assertEquals(1, links(table(set())).size());

		inTransaction(() -> {
			_parent.tRemove(set(), _a);
			_parent.tAdd(set(), _a);
		});
		assertEquals(Set.of(_a), _parent.tValue(set()));
	}

	/**
	 * Checks that a historic reference may hold the same object in different revisions, but not
	 * the same object in the same revision twice.
	 */
	public void testHistoricRevisions() {
		Revision first = HistoryUtils.getLastRevision();
		inTransaction(() -> _parent.tAdd(set(), _b));
		Revision second = HistoryUtils.getLastRevision();
		assertTrue(second.getCommitNumber() > first.getCommitNumber());

		TLObject aFirst = WrapperHistoryUtils.getWrapper(first, _a);
		TLObject aSecond = WrapperHistoryUtils.getWrapper(second, _a);
		assertFalse(aFirst.tHandle().tId().equals(aSecond.tHandle().tId()));

		inTransaction(() -> {
			_parent.tAdd(historic(), aFirst);
			_parent.tAdd(historic(), aSecond);
		});
		assertEquals(2, links(table(historic())).size());

		assertDuplicateRejected(historic(), aFirst);
		assertEquals(2, links(table(historic())).size());
	}

	private void assertDuplicateRejected(TLStructuredTypePart attribute, TLObject value) {
		KnowledgeBase kb = kb();
		Transaction tx = kb.beginTransaction(ResKey.text("duplicate link"));
		try {
			createLink(attribute, value);
			try {
				tx.commit();
				fail("Duplicate link for '" + attribute + "' must be rejected.");
			} catch (KnowledgeBaseException ex) {
				// Expected.
			}
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Creates a link of the given attribute from {@link #_parent} to the given value directly in
	 * the knowledge base, bypassing the storage implementation.
	 */
	private void createLink(TLStructuredTypePart attribute, TLObject value) {
		KnowledgeAssociation link = kb().createAssociation(_parent.tHandle(), value.tHandle(), table(attribute));
		link.setAttributeValue(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR, attribute.tHandle());
	}

	private List<KnowledgeAssociation> links(String table) {
		List<KnowledgeAssociation> result = new ArrayList<>();
		Iterator<KnowledgeAssociation> it = ((KnowledgeObject) _parent.tHandle()).getOutgoingAssociations(table);
		while (it.hasNext()) {
			result.add(it.next());
		}
		return result;
	}

	private static String table(TLStructuredTypePart attribute) {
		return ((LinkStorage<?>) attribute.getStorageImplementation()).getTable();
	}

	private TLStructuredTypePart set() {
		return part(SET_ATTRIBUTE_NAME);
	}

	private TLStructuredTypePart bag() {
		return part(BAG_ATTRIBUTE_NAME);
	}

	private TLStructuredTypePart historic() {
		return part(HISTORIC_ATTRIBUTE_NAME);
	}

	private TLStructuredTypePart part(String name) {
		return type(PARENT_TYPE_NAME).getPart(name);
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
