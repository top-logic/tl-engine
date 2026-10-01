/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.module.TestModuleUtil;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.io.MORepositoryBuilder;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.HashingInputStream;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.blob.FileSystemBlobStore;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.BlobReferenceAttribute;
import com.top_logic.dob.attr.HybridBinaryAttribute;
import com.top_logic.dob.attr.InlineBinaryAttribute;
import com.top_logic.dob.attr.RefBinaryAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.DBTypeRepository;

/**
 * Test of the binary attribute kinds {@link InlineBinaryAttribute}, {@link RefBinaryAttribute}
 * and {@link HybridBinaryAttribute}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBinaryAttributeKinds extends AbstractDBKnowledgeBaseClusterTest {

	static final String TYPE = "BinaryKinds";

	static final String MANDATORY_TYPE = "BinaryKindsMandatory";

	static final String INLINE_ATTR = "inlineData";

	static final String REF_ATTR = "refData";

	static final String HYBRID_ATTR = "hybridData";

	static final String MANDATORY_REF_ATTR = "mandatoryRef";

	static final String OTHER_STORE = "other";

	static final int THRESHOLD = 1024;

	private static final String TYPE_SCHEMA =
		"<" + DOXMLConstants.META_OBJECT_ELEMENT + " object_name='" + TYPE + "'>"
			+ "<attributes>"
			+ "<" + InlineBinaryAttribute.Config.TAG_NAME + " att_name='" + INLINE_ATTR + "'/>"
			+ "<" + RefBinaryAttribute.Config.TAG_NAME + " att_name='" + REF_ATTR + "' store='" + OTHER_STORE + "'/>"
			+ "<" + HybridBinaryAttribute.Config.TAG_NAME + " att_name='" + HYBRID_ATTR + "' threshold='1KB'/>"
			+ "</attributes>"
			+ "</" + DOXMLConstants.META_OBJECT_ELEMENT + ">";

	private static final String MANDATORY_TYPE_SCHEMA =
		"<" + DOXMLConstants.META_OBJECT_ELEMENT + " object_name='" + MANDATORY_TYPE + "'>"
			+ "<attributes>"
			+ "<" + RefBinaryAttribute.Config.TAG_NAME + " att_name='" + MANDATORY_REF_ATTR + "' mandatory='true'/>"
			+ "</attributes>"
			+ "</" + DOXMLConstants.META_OBJECT_ELEMENT + ">";

	private File _root;

	private BlobStoreService _service;

	private BlobStoreService _formerService;

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseClusterTestSetup setup = (DBKnowledgeBaseClusterTestSetup) super.createSetup(self);
		setup.addAdditionalTypes((log, typeFactory, typeRepository) -> {
			try {
				typeRepository.addMetaObject(createType(TYPE_SCHEMA));
				typeRepository.addMetaObject(createType(MANDATORY_TYPE_SCHEMA));
			} catch (Exception ex) {
				throw new AssertionError("Creating test types failed.", ex);
			}
		});
		return setup;
	}

	private static MOClass createType(String schema) throws Exception {
		MetaObjectConfig config = TypedConfiguration.parse(DOXMLConstants.META_OBJECT_ELEMENT,
			MetaObjectConfig.class, CharacterContents.newContent(schema));
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(config.getObjectName());
		type.setSuperclass(new DeferredMetaObject(B_NAME));
		for (AttributeConfig attributeConfig : config.getAttributes()) {
			type.addAttribute(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(attributeConfig));
		}
		return type;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir("binary-attribute-kinds");
		_service = newService();
		_formerService = TestModuleUtil.installNewInstance(BlobStoreService.Module.INSTANCE, _service);
	}

	@Override
	protected void tearDown() throws Exception {
		TestModuleUtil.installNewInstance(BlobStoreService.Module.INSTANCE, _formerService);
		for (BlobStore store : _service.getStores().values()) {
			store.close();
		}
		FileUtilities.deleteR(_root);
		super.tearDown();
	}

	private BlobStoreService newService() {
		BlobStoreService.Config<?> config = TypedConfiguration.newConfigItem(BlobStoreService.Config.class);
		config.setDefaultStore(BlobStoreService.DEFAULT_STORE_NAME);
		for (String name : List.of(BlobStoreService.DEFAULT_STORE_NAME, OTHER_STORE)) {
			FileSystemBlobStore.Config<?> storeConfig =
				TypedConfiguration.newConfigItem(FileSystemBlobStore.Config.class);
			storeConfig.setName(name);
			storeConfig.setRoot(new File(_root, name).getPath());
			config.getStores().put(name, storeConfig);
		}
		return (BlobStoreService) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/** The schema declares the columns of each kind with the configured store and threshold. */
	public void testSchema() {
		MOClass type = type(TYPE);

		MOAttribute inline = type.getAttribute(INLINE_ATTR);
		assertTrue(inline instanceof InlineBinaryAttribute);
		assertFalse(inline instanceof BlobReferenceAttribute);
		assertEquals(4, inline.getDbMapping().length);
		assertNotNull(((AbstractBinaryAttribute) inline).getDataColumn());
		assertNull(((AbstractBinaryAttribute) inline).getKeyColumn());

		MOAttribute ref = type.getAttribute(REF_ATTR);
		assertTrue(ref instanceof BlobReferenceAttribute);
		assertEquals(OTHER_STORE, ((BlobReferenceAttribute) ref).getStoreName());
		assertEquals(5, ref.getDbMapping().length);
		assertNull(((AbstractBinaryAttribute) ref).getDataColumn());
		assertEquals("REF_DATA" + AbstractBinaryAttribute.SUFFIX_KEY,
			((BlobReferenceAttribute) ref).getKeyColumn().getDBName());

		MOAttribute hybrid = type.getAttribute(HYBRID_ATTR);
		assertTrue(hybrid instanceof BlobReferenceAttribute);
		assertNull(((BlobReferenceAttribute) hybrid).getStoreName());
		assertEquals(THRESHOLD, ((HybridBinaryAttribute) hybrid).getThreshold());
		assertEquals(6, hybrid.getDbMapping().length);

		MOAttribute mandatory = type(MANDATORY_TYPE).getAttribute(MANDATORY_REF_ATTR);
		assertTrue(((AbstractBinaryAttribute) mandatory).getKeyColumn().isSQLNotNull());
		assertTrue(((AbstractBinaryAttribute) mandatory).getSizeColumn().isSQLNotNull());
		assertFalse(((AbstractBinaryAttribute) mandatory).getNameColumn().isSQLNotNull());
	}

	public void testInline() throws Exception {
		doTestWriteRead(INLINE_ATTR, 5000);
	}

	public void testRef() throws Exception {
		doTestWriteRead(REF_ATTR, 5000);
	}

	public void testHybridSmall() throws Exception {
		doTestWriteRead(HYBRID_ATTR, THRESHOLD - 1);
	}

	public void testHybridLarge() throws Exception {
		doTestWriteRead(HYBRID_ATTR, THRESHOLD);
	}

	private void doTestWriteRead(String attr, int size) throws Exception {
		byte[] content = randomContent(size);
		BinaryData data = BinaryDataFactory.createBinaryData(content, "image/png", "picture.png");

		Transaction tx = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		item.setAttributeValue(attr, data);
		assertContent(content, "image/png", "picture.png", (BinaryData) item.getAttributeValue(attr));
		commit(tx);

		assertContent(content, "image/png", "picture.png", (BinaryData) item.getAttributeValue(attr));

		// Read on the second node, loading the object from the database.
		refetchNode2();
		KnowledgeObject node2Item = (KnowledgeObject) node2Item(item);
		assertContent(content, "image/png", "picture.png", (BinaryData) node2Item.getAttributeValue(attr));
	}

	public void testHybridColumns() throws Exception {
		byte[] small = randomContent(THRESHOLD - 1);
		byte[] large = randomContent(THRESHOLD);

		Transaction tx = begin();
		KnowledgeObject smallItem = newA(TYPE, "small");
		smallItem.setAttributeValue(HYBRID_ATTR, BinaryDataFactory.createBinaryData(small));
		KnowledgeObject largeItem = newA(TYPE, "large");
		largeItem.setAttributeValue(HYBRID_ATTR, BinaryDataFactory.createBinaryData(large));
		commit(tx);

		assertFalse(smallItem.getAttributeValue(HYBRID_ATTR) instanceof BlobBinaryData);
		assertTrue(largeItem.getAttributeValue(HYBRID_ATTR) instanceof BlobBinaryData);

		Object[] smallRow = hybridColumns(smallItem);
		assertNull("Key of inline content.", smallRow[0]);
		assertNull("Hash of inline content.", smallRow[1]);
		assertTrue("Inline content.", (Boolean) smallRow[2]);

		Object[] largeRow = hybridColumns(largeItem);
		assertEquals(((BlobBinaryData) largeItem.getAttributeValue(HYBRID_ATTR)).getKey(), largeRow[0]);
		assertEquals(sha256(large), largeRow[1]);
		assertFalse("No inline content for external content.", (Boolean) largeRow[2]);
	}

	/** Content of unknown size is placed by its actual size. */
	public void testHybridUnknownSize() throws Exception {
		byte[] small = randomContent(THRESHOLD - 1);
		byte[] exact = randomContent(THRESHOLD);
		byte[] large = randomContent(3 * THRESHOLD + 7);

		Transaction tx = begin();
		KnowledgeObject smallItem = newA(TYPE, "small");
		smallItem.setAttributeValue(HYBRID_ATTR, unknownSize(small));
		KnowledgeObject exactItem = newA(TYPE, "exact");
		exactItem.setAttributeValue(HYBRID_ATTR, unknownSize(exact));
		KnowledgeObject largeItem = newA(TYPE, "large");
		largeItem.setAttributeValue(HYBRID_ATTR, unknownSize(large));
		commit(tx);

		assertFalse(smallItem.getAttributeValue(HYBRID_ATTR) instanceof BlobBinaryData);
		assertTrue(exactItem.getAttributeValue(HYBRID_ATTR) instanceof BlobBinaryData);
		assertTrue(largeItem.getAttributeValue(HYBRID_ATTR) instanceof BlobBinaryData);

		refetchNode2();
		assertContent(small, "text/plain", "unknown.txt",
			(BinaryData) node2Item(smallItem).getAttributeValue(HYBRID_ATTR));
		assertContent(exact, "text/plain", "unknown.txt",
			(BinaryData) node2Item(exactItem).getAttributeValue(HYBRID_ATTR));
		assertContent(large, "text/plain", "unknown.txt",
			(BinaryData) node2Item(largeItem).getAttributeValue(HYBRID_ATTR));
	}

	/** Content of unknown size in inline and reference attributes. */
	public void testUnknownSize() throws Exception {
		byte[] content = randomContent(5000);

		Transaction tx = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		item.setAttributeValue(INLINE_ATTR, unknownSize(content));
		item.setAttributeValue(REF_ATTR, unknownSize(content));
		commit(tx);

		refetchNode2();
		KnowledgeObject node2Item = (KnowledgeObject) node2Item(item);
		assertContent(content, "text/plain", "unknown.txt", (BinaryData) node2Item.getAttributeValue(INLINE_ATTR));
		assertContent(content, "text/plain", "unknown.txt", (BinaryData) node2Item.getAttributeValue(REF_ATTR));
	}

	/** The content of a reference attribute is uploaded when the value is set. */
	public void testUploadAtSetTime() throws Exception {
		byte[] content = randomContent(2000);

		Transaction tx = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		item.setAttributeValue(REF_ATTR, BinaryDataFactory.createBinaryData(content));

		BlobBinaryData value = (BlobBinaryData) item.getAttributeValue(REF_ATTR);
		assertEquals(OTHER_STORE, value.getStoreName());
		assertEquals(sha256(content), value.getHash());
		assertEquals(content.length, value.getSize());
		assertTrue("Content not uploaded before commit.", storeKeys(OTHER_STORE).contains(value.getKey()));
		try (InputStream in = _service.getStore(OTHER_STORE).get(value.getKey())) {
			assertEquals(content, StreamUtilities.readStreamContents(in));
		}
		commit(tx);
	}

	/** A rollback leaves the uploaded content unreferenced and the object unchanged. */
	public void testRollback() throws Exception {
		byte[] original = randomContent(2000);
		byte[] update = randomContent(3000);

		Transaction tx = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		item.setAttributeValue(REF_ATTR, BinaryDataFactory.createBinaryData(original));
		commit(tx);
		String originalKey = ((BlobBinaryData) item.getAttributeValue(REF_ATTR)).getKey();

		Transaction tx2 = begin();
		item.setAttributeValue(REF_ATTR, BinaryDataFactory.createBinaryData(update));
		String updateKey = ((BlobBinaryData) item.getAttributeValue(REF_ATTR)).getKey();
		rollback(tx2);

		assertNotEquals(originalKey, updateKey);
		assertEquals(originalKey, ((BlobBinaryData) item.getAttributeValue(REF_ATTR)).getKey());
		assertContent(original, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME,
			(BinaryData) item.getAttributeValue(REF_ATTR));
		List<String> keys = storeKeys(OTHER_STORE);
		assertTrue(keys.contains(originalKey));
		assertTrue("Unreferenced content stays in the store.", keys.contains(updateKey));
	}

	/** Historic revisions read their own content. */
	public void testHistory() throws Exception {
		if (!type(TYPE).isVersioned()) {
			return;
		}
		byte[] v1 = randomContent(5000);
		byte[] v2 = randomContent(6000);
		byte[] small1 = randomContent(10);
		byte[] small2 = randomContent(20);

		Transaction tx1 = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		item.setAttributeValue(INLINE_ATTR, BinaryDataFactory.createBinaryData(v1, "text/plain", "v1.txt"));
		item.setAttributeValue(REF_ATTR, BinaryDataFactory.createBinaryData(v1, "text/plain", "v1.txt"));
		item.setAttributeValue(HYBRID_ATTR, BinaryDataFactory.createBinaryData(small1, "text/plain", "v1.txt"));
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		item.setAttributeValue(INLINE_ATTR, BinaryDataFactory.createBinaryData(v2, "text/csv", "v2.csv"));
		item.setAttributeValue(REF_ATTR, BinaryDataFactory.createBinaryData(v2, "text/csv", "v2.csv"));
		item.setAttributeValue(HYBRID_ATTR, BinaryDataFactory.createBinaryData(v2, "text/csv", "v2.csv"));
		commit(tx2);

		Transaction tx3 = begin();
		item.setAttributeValue(HYBRID_ATTR, BinaryDataFactory.createBinaryData(small2, "text/csv", "v3.csv"));
		commit(tx3);
		Revision r2 = tx2.getCommitRevision();

		KnowledgeObject historic1 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(r1, item);
		assertContent(v1, "text/plain", "v1.txt", (BinaryData) historic1.getAttributeValue(INLINE_ATTR));
		assertContent(v1, "text/plain", "v1.txt", (BinaryData) historic1.getAttributeValue(REF_ATTR));
		assertContent(small1, "text/plain", "v1.txt", (BinaryData) historic1.getAttributeValue(HYBRID_ATTR));

		KnowledgeObject historic2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(r2, item);
		assertContent(v2, "text/csv", "v2.csv", (BinaryData) historic2.getAttributeValue(INLINE_ATTR));
		assertContent(v2, "text/csv", "v2.csv", (BinaryData) historic2.getAttributeValue(REF_ATTR));
		assertContent(v2, "text/csv", "v2.csv", (BinaryData) historic2.getAttributeValue(HYBRID_ATTR));

		assertContent(small2, "text/csv", "v3.csv", (BinaryData) item.getAttributeValue(HYBRID_ATTR));
	}

	/** A full read of corrupted external content fails. */
	public void testIntegrityCheck() throws Exception {
		byte[] content = randomContent(5000);

		Transaction tx = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		item.setAttributeValue(REF_ATTR, BinaryDataFactory.createBinaryData(content));
		commit(tx);

		BlobBinaryData value = (BlobBinaryData) item.getAttributeValue(REF_ATTR);
		Path file = ((FileSystemBlobStore) _service.getStore(OTHER_STORE)).getFile(value.getKey());
		byte[] corrupted = content.clone();
		corrupted[100] ^= 0x01;
		Files.write(file, corrupted);

		try (InputStream in = value.getStream()) {
			StreamUtilities.readStreamContents(in);
			fail("Corrupted content must be detected.");
		} catch (IOException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(value.getKey()));
		}

		// Range reads are not checked.
		try (InputStream in = value.getStream(100, 1)) {
			assertEquals(corrupted[100] & 0xFF, in.read());
		}

		// Truncated content.
		Files.write(file, new byte[] { 1, 2, 3 });
		try (InputStream in = value.getStream()) {
			StreamUtilities.readStreamContents(in);
			fail("Truncated content must be detected.");
		} catch (IOException ex) {
			// Expected.
		}
	}

	public void testNullValues() throws Exception {
		byte[] content = randomContent(5000);

		Transaction tx = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		assertNull(item.getAttributeValue(INLINE_ATTR));
		assertNull(item.getAttributeValue(REF_ATTR));
		assertNull(item.getAttributeValue(HYBRID_ATTR));
		item.setAttributeValue(INLINE_ATTR, BinaryDataFactory.createBinaryData(content));
		item.setAttributeValue(REF_ATTR, BinaryDataFactory.createBinaryData(content));
		item.setAttributeValue(HYBRID_ATTR, BinaryDataFactory.createBinaryData(content));
		commit(tx);

		Transaction tx2 = begin();
		item.setAttributeValue(INLINE_ATTR, null);
		item.setAttributeValue(REF_ATTR, null);
		item.setAttributeValue(HYBRID_ATTR, null);
		commit(tx2);

		assertNull(item.getAttributeValue(INLINE_ATTR));
		assertNull(item.getAttributeValue(REF_ATTR));
		assertNull(item.getAttributeValue(HYBRID_ATTR));

		refetchNode2();
		KnowledgeObject node2Item = (KnowledgeObject) node2Item(item);
		assertNull(node2Item.getAttributeValue(INLINE_ATTR));
		assertNull(node2Item.getAttributeValue(REF_ATTR));
		assertNull(node2Item.getAttributeValue(HYBRID_ATTR));
	}

	public void testEmptyContent() throws Exception {
		Transaction tx = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		item.setAttributeValue(INLINE_ATTR, BinaryDataFactory.createBinaryData(new byte[0], "text/plain", "e"));
		item.setAttributeValue(REF_ATTR, BinaryDataFactory.createBinaryData(new byte[0], "text/plain", "e"));
		item.setAttributeValue(HYBRID_ATTR, BinaryDataFactory.createBinaryData(new byte[0], "text/plain", "e"));
		commit(tx);

		refetchNode2();
		KnowledgeObject node2Item = (KnowledgeObject) node2Item(item);
		assertContent(new byte[0], "text/plain", "e", (BinaryData) node2Item.getAttributeValue(INLINE_ATTR));
		assertContent(new byte[0], "text/plain", "e", (BinaryData) node2Item.getAttributeValue(REF_ATTR));
		assertContent(new byte[0], "text/plain", "e", (BinaryData) node2Item.getAttributeValue(HYBRID_ATTR));
	}

	public void testMandatory() throws Exception {
		Transaction tx = begin();
		newA(MANDATORY_TYPE, "missing");
		commit(tx, true);

		byte[] content = randomContent(100);
		Transaction tx2 = begin();
		KnowledgeObject item = newA(MANDATORY_TYPE, "set");
		item.setAttributeValue(MANDATORY_REF_ATTR, BinaryDataFactory.createBinaryData(content));
		commit(tx2);

		refetchNode2();
		assertContent(content, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME,
			(BinaryData) node2Item(item).getAttributeValue(MANDATORY_REF_ATTR));
	}

	public void testIncompatibleValue() throws Exception {
		Transaction tx = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		try {
			item.setAttributeValue(REF_ATTR, "no binary");
			fail("Non-binary value must be rejected.");
		} catch (Exception ex) {
			// Expected.
		}
		rollback(tx);
	}

	/** Copying a value to another attribute stores an independent copy. */
	public void testCopyBetweenKinds() throws Exception {
		byte[] content = randomContent(5000);

		Transaction tx = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		item.setAttributeValue(REF_ATTR, BinaryDataFactory.createBinaryData(content, "text/plain", "c.txt"));
		BlobBinaryData refValue = (BlobBinaryData) item.getAttributeValue(REF_ATTR);
		item.setAttributeValue(INLINE_ATTR, refValue);
		item.setAttributeValue(HYBRID_ATTR, refValue);
		commit(tx);

		BlobBinaryData hybridValue = (BlobBinaryData) item.getAttributeValue(HYBRID_ATTR);
		assertNull(hybridValue.getStoreName());
		assertNotEquals(refValue.getKey(), hybridValue.getKey());

		refetchNode2();
		KnowledgeObject node2Item = (KnowledgeObject) node2Item(item);
		assertContent(content, "text/plain", "c.txt", (BinaryData) node2Item.getAttributeValue(INLINE_ATTR));
		assertContent(content, "text/plain", "c.txt", (BinaryData) node2Item.getAttributeValue(HYBRID_ATTR));
	}

	private Object[] hybridColumns(KnowledgeObject item) throws SQLException {
		MOClass type = (MOClass) item.tTable();
		DBTableMetaObject table = type.getDBMapping();
		AbstractBinaryAttribute attr = (AbstractBinaryAttribute) type.getAttribute(HYBRID_ATTR);
		String idColumn = table.getAttribute(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME).getDbMapping()[0].getDBName();
		String revMinColumn = table.getAttribute(BasicTypes.REV_MIN_ATTRIBUTE_NAME).getDbMapping()[0].getDBName();

		PooledConnection connection = kb().getConnectionPool().borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			String query = "SELECT " + sql.columnRef(attr.getKeyColumn().getDBName()) + ", "
				+ sql.columnRef(attr.getHashColumn().getDBName()) + ", "
				+ sql.columnRef(attr.getDataColumn().getDBName())
				+ " FROM " + sql.tableRef(table.getDBName())
				+ " WHERE " + sql.columnRef(idColumn) + " = ?"
				+ " ORDER BY " + sql.columnRef(revMinColumn) + " DESC";
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				TLID id = item.getObjectName();
				IdentifierUtil.setId(statement, 1, id);
				try (ResultSet result = statement.executeQuery()) {
					assertTrue(result.next());
					String key = result.getString(1);
					String hash = result.getString(2);
					boolean hasData = result.getBlob(3) != null;
					return new Object[] { key, hash, Boolean.valueOf(hasData) };
				}
			}
		} finally {
			kb().getConnectionPool().releaseReadConnection(connection);
		}
	}

	private List<String> storeKeys(String storeName) throws IOException {
		try (Stream<BlobInfo> blobs = _service.getStore(storeName).list()) {
			return blobs.map(BlobInfo::key).collect(Collectors.toList());
		}
	}

	private static BinaryData unknownSize(byte[] content) {
		return new AbstractBinaryData() {
			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(content);
			}

			@Override
			public long getSize() {
				return -1;
			}

			@Override
			public String getName() {
				return "unknown.txt";
			}

			@Override
			public String getContentType() {
				return "text/plain";
			}
		};
	}

	private static String sha256(byte[] content) throws IOException {
		HashingInputStream in = HashingInputStream.sha256(new ByteArrayInputStream(content));
		StreamUtilities.readStreamContents(in);
		return in.getHash();
	}

	private static void assertContent(byte[] expected, String contentType, String name, BinaryData actual)
			throws IOException {
		assertNotNull(actual);
		assertEquals(name, actual.getName());
		assertEquals(contentType, actual.getContentType());
		assertEquals(expected.length, actual.getSize());
		try (InputStream in = actual.getStream()) {
			assertEquals(expected, StreamUtilities.readStreamContents(in));
		}
	}

	/**
	 * A plain <code>Blob</code> attribute in the schema of a knowledge base is rejected with an
	 * error naming the binary attribute kind to use instead.
	 */
	public void testPlainBlobAttributeRejected() throws Exception {
		String schemaXml = "<" + MORepositoryBuilder.ROOT_TAG + ">"
			+ "<" + MetaObjectsConfig.METAOBJECTS + ">"
			+ "<" + DOXMLConstants.META_OBJECT_ELEMENT + " object_name='PlainBlob'>"
			+ "<attributes>"
			+ "<" + DOXMLConstants.MO_ATTRIBUTE_ELEMENT + " att_name='data' att_type='Blob'/>"
			+ "</attributes>"
			+ "</" + DOXMLConstants.META_OBJECT_ELEMENT + ">"
			+ "</" + MetaObjectsConfig.METAOBJECTS + ">"
			+ "</" + MORepositoryBuilder.ROOT_TAG + ">";
		MetaObjectsConfig types = TypedConfiguration.parse(MORepositoryBuilder.ROOT_TAG, MetaObjectsConfig.class,
			CharacterContents.newContent(schemaXml));

		// Parsing the declaration succeeds, so that a schema stored in the database can be read.
		SchemaConfiguration schema = TypedConfiguration.newConfigItem(SchemaConfiguration.class);
		schema.setMetaObjects(types);
		SchemaSetup setup = new SchemaSetup(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, schema);
		assertNotNull(setup.createMORepository(DefaultMOFactory.INSTANCE).getTypeOrNull("PlainBlob"));

		BufferingProtocol log = new BufferingProtocol();
		try {
			DBTypeRepository.newRepository(new DefaultInstantiationContext(log),
				kb().getConnectionPool().getSQLDialect(), setup, true);
			fail("Plain Blob attribute must be rejected.");
		} catch (ConfigurationException | AbortExecutionException ex) {
			// Expected.
		}
		String errors = String.join("\n", log.getErrors());
		assertTrue(errors, errors.contains("'data'"));
		assertTrue(errors, errors.contains(InlineBinaryAttribute.Config.TAG_NAME));
	}

	private static byte[] randomContent(int size) {
		byte[] result = new byte[size];
		new Random(size).nextBytes(result);
		return result;
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestBinaryAttributeKinds.class);
	}

}
