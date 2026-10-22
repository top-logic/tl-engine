/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.service.KBUtils.*;
import static java.util.Arrays.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.storage.AssociationQueryBasedStorage;
import com.top_logic.element.meta.kbbased.storage.BinaryCollectionStorage;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for {@link BinaryCollectionStorage}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBinaryCollectionStorage extends BasicTestCase {

	private static final Class<TestBinaryCollectionStorage> THIS_CLASS = TestBinaryCollectionStorage.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String TYPE_NAME = "Node";

	private static final String UNVERSIONED_TYPE_NAME = "UnversionedNode";

	private static final String FILES_ATTRIBUTE = "files";

	private static final String FILE_SET_ATTRIBUTE = "fileSet";

	private static final BinaryData A = file(new byte[] { 1, 2, 3 }, "text/plain", "a.txt");

	private static final BinaryData B = file(new byte[] { 4, 5 }, "image/png", "b.png");

	private static final BinaryData C = file(new byte[] { 6 }, "application/pdf", "c.pdf");

	private TLObject _node;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inTransaction(() -> _node = DynamicModelService.getInstance().createObject(getNodeType()));
	}

	@Override
	protected void tearDown() throws Exception {
		if (_node.tValid()) {
			inTransaction(() -> _node.tDelete());
		}
		super.tearDown();
	}

	public void testStorageSelection() {
		assertInstanceof(AttributeOperations.getStorageImplementation(getNodeType().getPart(FILES_ATTRIBUTE)),
			BinaryCollectionStorage.class);
		assertInstanceof(AttributeOperations.getStorageImplementation(getNodeType().getPart(FILE_SET_ATTRIBUTE)),
			BinaryCollectionStorage.class);
	}

	public void testUnversionedOwner() throws IOException {
		TLClass type = (TLClass) getModule().getType(UNVERSIONED_TYPE_NAME);
		StorageImplementation storage = AttributeOperations.getStorageImplementation(type.getPart(FILES_ATTRIBUTE));
		assertInstanceof(storage, BinaryCollectionStorage.class);
		assertEquals(BinaryCollectionStorage.UNVERSIONED_TABLE,
			((BinaryCollectionStorage<?>) storage).getConfig().getTable());

		TLObject owner = inTransaction(() -> DynamicModelService.getInstance().createObject(type));
		try {
			inTransaction(() -> owner.tUpdateByName(FILES_ATTRIBUTE, asList(A, B)));
			assertFiles(asList(A, B), (List<?>) owner.tValueByName(FILES_ATTRIBUTE));
			assertEquals(2, rows(BinaryCollectionStorage.UNVERSIONED_TABLE, owner).size());

			List<?> loaded = (List<?>) owner.tValueByName(FILES_ATTRIBUTE);
			inTransaction(() -> owner.tUpdateByName(FILES_ATTRIBUTE, asList(loaded.get(1), C)));
			assertFiles(asList(B, C), (List<?>) owner.tValueByName(FILES_ATTRIBUTE));
			assertEquals(2, rows(BinaryCollectionStorage.UNVERSIONED_TABLE, owner).size());
		} finally {
			inTransaction(() -> owner.tDelete());
		}
		assertEquals(0, rows(BinaryCollectionStorage.UNVERSIONED_TABLE, owner).size());
	}

	public void testRoundTrip() throws IOException {
		store(FILES_ATTRIBUTE, asList(A, B, C));

		List<?> loaded = loadList(FILES_ATTRIBUTE);
		assertFiles(asList(A, B, C), loaded);
		assertEquals(asList(0, 1, 2), sortOrders());
	}

	public void testReorderKeepsRows() throws IOException {
		store(FILES_ATTRIBUTE, asList(A, B, C));
		List<KnowledgeItem> rowsBefore = rows();

		List<?> loaded = loadList(FILES_ATTRIBUTE);
		store(FILES_ATTRIBUTE, asList(loaded.get(2), loaded.get(0), loaded.get(1)));

		assertFiles(asList(C, A, B), loadList(FILES_ATTRIBUTE));
		assertEquals(asList(0, 1, 2), sortOrders());
		assertEquals(identities(rowsBefore), identities(rows()));
	}

	public void testReplaceKeepsUnchangedRows() throws IOException {
		store(FILES_ATTRIBUTE, asList(A, B));
		List<?> loaded = loadList(FILES_ATTRIBUTE);
		KnowledgeItem rowA = rowOf(A.getName());
		KnowledgeItem rowB = rowOf(B.getName());

		store(FILES_ATTRIBUTE, asList(loaded.get(0), C));

		assertFiles(asList(A, C), loadList(FILES_ATTRIBUTE));
		assertEquals(2, rows().size());
		assertSame(rowA, rowOf(A.getName()));
		assertFalse(rowB.isAlive());
	}

	public void testValueOfOtherObjectIsCopied() throws IOException {
		store(FILES_ATTRIBUTE, asList(A, B));
		List<?> loaded = loadList(FILES_ATTRIBUTE);

		TLObject other = inTransaction(() -> DynamicModelService.getInstance().createObject(getNodeType()));
		try {
			inTransaction(() -> other.tUpdateByName(FILES_ATTRIBUTE, loaded));

			assertFiles(asList(A, B), (List<?>) other.tValueByName(FILES_ATTRIBUTE));
			assertFiles(asList(A, B), loadList(FILES_ATTRIBUTE));
			assertEquals(2, rows().size());
		} finally {
			inTransaction(() -> other.tDelete());
		}
	}

	public void testAddRemove() throws IOException {
		store(FILES_ATTRIBUTE, asList(A));
		inTransaction(() -> _node.tAddByName(FILES_ATTRIBUTE, B));
		inTransaction(() -> _node.tAddByName(FILES_ATTRIBUTE, C));
		assertFiles(asList(A, B, C), loadList(FILES_ATTRIBUTE));

		Object storedB = loadList(FILES_ATTRIBUTE).get(1);
		inTransaction(() -> _node.tRemoveByName(FILES_ATTRIBUTE, storedB));
		assertFiles(asList(A, C), loadList(FILES_ATTRIBUTE));

		// Removing by content.
		inTransaction(() -> _node.tRemoveByName(FILES_ATTRIBUTE, A));
		assertFiles(asList(C), loadList(FILES_ATTRIBUTE));
		assertEquals(1, rows().size());
	}

	public void testUnorderedIsSet() throws IOException {
		store(FILE_SET_ATTRIBUTE, asList(A, B));

		Object value = _node.tValueByName(FILE_SET_ATTRIBUTE);
		assertInstanceof(value, Set.class);
		assertFiles(asList(A, B), new ArrayList<>((Collection<?>) value));
	}

	public void testClear() {
		store(FILES_ATTRIBUTE, asList(A, B));
		store(FILES_ATTRIBUTE, null);
		assertEquals(Collections.emptyList(), loadList(FILES_ATTRIBUTE));
		assertEquals(0, rows().size());

		store(FILES_ATTRIBUTE, asList(A, B));
		store(FILES_ATTRIBUTE, Collections.emptyList());
		assertEquals(Collections.emptyList(), loadList(FILES_ATTRIBUTE));
		assertEquals(0, rows().size());
	}

	public void testDeleteOwnerDeletesRows() {
		store(FILES_ATTRIBUTE, asList(A, B));
		store(FILE_SET_ATTRIBUTE, asList(C));
		assertEquals(3, rows().size());

		inTransaction(() -> _node.tDelete());

		assertEquals(0, rows().size());
	}

	public void testRejectNonBinary() {
		store(FILES_ATTRIBUTE, asList(A));
		try {
			store(FILES_ATTRIBUTE, asList(B, "no file"));
			fail("A string cannot be stored in a binary attribute.");
		} catch (TopLogicException ex) {
			// Expected.
		}
		assertEquals(1, loadList(FILES_ATTRIBUTE).size());
	}

	private static void assertFiles(List<BinaryData> expected, List<?> actual) throws IOException {
		assertEquals(expected.size(), actual.size());
		for (int n = 0; n < expected.size(); n++) {
			BinaryData expectedFile = expected.get(n);
			BinaryData actualFile = (BinaryData) actual.get(n);
			assertEquals(expectedFile.getName(), actualFile.getName());
			assertEquals(expectedFile.getContentType(), actualFile.getContentType());
			assertTrue(Arrays.equals(StreamUtilities.readStreamContents(expectedFile),
				StreamUtilities.readStreamContents(actualFile)));
		}
	}

	private List<Integer> sortOrders() {
		List<Integer> result = new ArrayList<>();
		for (KnowledgeItem row : rows()) {
			result.add((Integer) row.getAttributeValue(BinaryCollectionStorage.SORT_ORDER_ATTRIBUTE_NAME));
		}
		return result;
	}

	private KnowledgeItem rowOf(String fileName) {
		for (KnowledgeItem row : rows()) {
			if (fileName.equals(row.getAttributeValue(BinaryCollectionStorage.NAME_ATTRIBUTE_NAME))) {
				return row;
			}
		}
		return null;
	}

	private static List<Object> identities(List<KnowledgeItem> rows) {
		List<Object> result = new ArrayList<>();
		for (KnowledgeItem row : rows) {
			result.add(row.tId());
		}
		result.sort(Comparator.comparing(Object::toString));
		return result;
	}

	/**
	 * The rows of {@link #_node} in the table of {@link BinaryCollectionStorage}, sorted by their
	 * position.
	 */
	private List<KnowledgeItem> rows() {
		return rows(BinaryCollectionStorage.TABLE, _node);
	}

	/**
	 * The rows of the given owner in the given table, sorted by their position.
	 */
	private static List<KnowledgeItem> rows(String table, TLObject owner) {
		List<KnowledgeItem> result = new ArrayList<>();
		for (KnowledgeItem row : PersistencyLayer.getKnowledgeBase()
			.search(queryUnresolved(allOf(table), KnowledgeItem.class))) {
			if (row.getAttributeValue(AssociationQueryBasedStorage.OBJECT_ATTRIBUTE_NAME) == owner.tHandle()) {
				result.add(row);
			}
		}
		result.sort(Comparator.comparingInt(
			row -> ((Integer) row.getAttributeValue(BinaryCollectionStorage.SORT_ORDER_ATTRIBUTE_NAME)).intValue()));
		return result;
	}

	private void store(String attributeName, List<?> values) {
		inTransaction(() -> _node.tUpdateByName(attributeName, values));
	}

	private List<?> loadList(String attributeName) {
		return (List<?>) _node.tValueByName(attributeName);
	}

	private static BinaryData file(byte[] content, String contentType, String name) {
		return BinaryDataFactory.createBinaryData(content, contentType, name);
	}

	private TLClass getNodeType() {
		return (TLClass) getModule().getType(TYPE_NAME);
	}

	private TLModule getModule() {
		return ModelService.getInstance().getModel().getModule(MODULE_NAME);
	}

	public static Test suite() {
		Test kbSetup = KBSetup.getSingleKBTest(THIS_CLASS);
		Test customConfigSetup = createCustomConfigSetup(THIS_CLASS, kbSetup);
		return TLTestSetup.createTLTestSetup(customConfigSetup);
	}

	private static Test createCustomConfigSetup(Class<?> testClass, Test innerSetup) {
		String configFileName = testClass.getSimpleName() + FileUtilities.XML_FILE_ENDING;
		String createFilePath = CustomPropertiesDecorator.createFileName(testClass, configFileName);
		return TestUtils.doNotMerge(new CustomPropertiesSetup(innerSetup, createFilePath, true));
	}

}
