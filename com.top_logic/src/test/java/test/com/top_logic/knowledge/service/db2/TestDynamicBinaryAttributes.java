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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.module.TestModuleUtil;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.HashingInputStream;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.blob.FileSystemBlobStore;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.BinaryStorageSettings;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.CommittableAdapter;
import com.top_logic.knowledge.service.DynamicBinaryStoragePolicy;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge;
import com.top_logic.knowledge.service.db2.FlexAttributeFetch;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.FlexVersionedDataManager;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.migration.AlterColumnProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;

/**
 * Test of binary values of dynamic attributes, stored in the table
 * {@link AbstractFlexDataManager#FLEX_BINARY_DATA}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDynamicBinaryAttributes extends AbstractDBKnowledgeBaseClusterTest {

	private static final String ATTR = "dynBinary";

	private static final String OTHER_STORE = "other";

	/** Larger than the default threshold of 64 KB. */
	private static final int LARGE = 70 * 1024;

	/** Smaller than the default threshold and small enough to be kept in memory when read. */
	private static final int SMALL = 1000;

	private File _root;

	private BlobStoreService _service;

	private BlobStoreService _formerService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir("dynamic-binary-attributes");
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

	/** Small content is stored inline in the binary table, not in the flex data table. */
	public void testSmallInline() throws Exception {
		byte[] content = randomContent(SMALL);

		Transaction tx = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content, "image/png", "small.png"));
		assertFalse(item.getAttributeValue(ATTR) instanceof BlobBinaryData);
		commit(tx);

		assertContent(content, "image/png", "small.png", (BinaryData) item.getAttributeValue(ATTR));

		List<BinaryRow> rows = binaryRows(item);
		assertEquals(1, rows.size());
		BinaryRow row = rows.get(0);
		assertNull(row._key);
		assertNull(row._hash);
		assertNull(row._store);
		assertTrue(row._inline);
		assertEquals(Long.valueOf(content.length), Long.valueOf(row._size));
		assertEquals("image/png", row._contentType);
		assertEquals("small.png", row._name);
		assertEquals(Revision.CURRENT_REV, row._revMax);
		assertEquals("No flex data row for a binary value.", 0, flexRowCount(item));

		refetchNode2();
		assertContent(content, "image/png", "small.png", (BinaryData) node2Item(item).getAttributeValue(ATTR));
	}

	/** A binary value given as initial value is stored. */
	public void testInitialValue() throws Exception {
		byte[] small = randomContent(SMALL);
		byte[] large = randomContent(LARGE);

		Transaction tx = begin();
		KnowledgeItem item = kb().createKnowledgeItem(trunk(), E_NAME,
			new NameValueBuffer()
				.put(A1_NAME, "e1")
				.put(ATTR, BinaryDataFactory.createBinaryData(small, "text/plain", "small.txt"))
				.put("largeBinary", BinaryDataFactory.createBinaryData(large, "text/plain", "large.txt")),
			KnowledgeItem.class);
		assertTrue(item.getAttributeValue("largeBinary") instanceof BlobBinaryData);
		commit(tx);

		assertContent(small, "text/plain", "small.txt", (BinaryData) item.getAttributeValue(ATTR));
		assertContent(large, "text/plain", "large.txt", (BinaryData) item.getAttributeValue("largeBinary"));

		refetchNode2();
		assertContent(small, "text/plain", "small.txt", (BinaryData) node2Item(item).getAttributeValue(ATTR));
		assertContent(large, "text/plain", "large.txt",
			(BinaryData) node2Item(item).getAttributeValue("largeBinary"));
	}

	/** Large content inline in the table is read lazily. */
	public void testMediumInline() throws Exception {
		byte[] content = randomContent(20000);

		Transaction tx = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content, "text/plain", "medium.txt"));
		commit(tx);

		assertTrue(binaryRows(item).get(0)._inline);

		refetchNode2();
		assertContent(content, "text/plain", "medium.txt", (BinaryData) node2Item(item).getAttributeValue(ATTR));
	}

	/** Large content is uploaded to the default store when it is set. */
	public void testLargeInDefaultStore() throws Exception {
		byte[] content = randomContent(LARGE);

		Transaction tx = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content, "application/pdf", "doc.pdf"));
		BlobBinaryData value = (BlobBinaryData) item.getAttributeValue(ATTR);
		assertNull(value.getStoreName());
		assertTrue("Content uploaded before commit.",
			storeKeys(BlobStoreService.DEFAULT_STORE_NAME).contains(value.getKey()));
		commit(tx);

		try (InputStream in = _service.getStore(BlobStoreService.DEFAULT_STORE_NAME).get(value.getKey())) {
			assertEquals(content, StreamUtilities.readStreamContents(in));
		}

		BinaryRow row = binaryRows(item).get(0);
		assertEquals(value.getKey(), row._key);
		assertEquals(sha256(content), row._hash);
		assertNull("Default store.", row._store);
		assertFalse(row._inline);
		assertEquals(content.length, row._size);

		refetchNode2();
		BinaryData node2Value = (BinaryData) node2Item(item).getAttributeValue(ATTR);
		assertTrue(node2Value instanceof BlobBinaryData);
		assertContent(content, "application/pdf", "doc.pdf", node2Value);
	}

	/** Setting content equal to the current content changes nothing. */
	public void testSetEqualContent() throws Exception {
		byte[] content = randomContent(LARGE);

		Transaction tx = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content));
		commit(tx);
		int blobCount = storeKeys(BlobStoreService.DEFAULT_STORE_NAME).size();

		Transaction tx2 = begin();
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content));
		commit(tx2);

		assertEquals("No upload of equal content.", blobCount, storeKeys(BlobStoreService.DEFAULT_STORE_NAME).size());
		assertEquals(1, binaryRows(item).size());
	}

	/** Overwriting keeps the former value readable in its revision. */
	public void testOverwriteHistory() throws Exception {
		byte[] v1 = randomContent(LARGE);
		byte[] v2 = randomContent(SMALL);
		byte[] v3 = randomContent(LARGE + 1);

		Transaction tx1 = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(v1, "text/plain", "v1.txt"));
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(v2, "text/csv", "v2.csv"));
		commit(tx2);
		Revision r2 = tx2.getCommitRevision();

		Transaction tx3 = begin();
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(v3, "text/xml", "v3.xml"));
		commit(tx3);

		List<BinaryRow> rows = binaryRows(item);
		assertEquals(3, rows.size());
		assertEquals(Revision.CURRENT_REV, rows.get(0)._revMax);
		assertEquals(tx3.getCommitRevision().getCommitNumber() - 1, rows.get(1)._revMax);
		assertEquals(r2.getCommitNumber(), rows.get(1)._revMin);
		assertEquals(r2.getCommitNumber() - 1, rows.get(2)._revMax);
		assertEquals(r1.getCommitNumber(), rows.get(2)._revMin);

		assertContent(v3, "text/xml", "v3.xml", (BinaryData) item.getAttributeValue(ATTR));
		KnowledgeItem historic1 = HistoryUtils.getKnowledgeItem(r1, item);
		assertContent(v1, "text/plain", "v1.txt", (BinaryData) historic1.getAttributeValue(ATTR));
		KnowledgeItem historic2 = HistoryUtils.getKnowledgeItem(r2, item);
		assertContent(v2, "text/csv", "v2.csv", (BinaryData) historic2.getAttributeValue(ATTR));

		refetchNode2();
		KnowledgeItem node2Item = node2Item(item);
		assertContent(v3, "text/xml", "v3.xml", (BinaryData) node2Item.getAttributeValue(ATTR));
		KnowledgeItem node2Historic1 =
			HistoryUtils.getKnowledgeItem(HistoryUtils.getHistoryManager(kbNode2()).getRevision(r1.getCommitNumber()),
				node2Item);
		assertContent(v1, "text/plain", "v1.txt", (BinaryData) node2Historic1.getAttributeValue(ATTR));
	}

	/** A value read on another node is updated by a refetch. */
	public void testRefetch() throws Exception {
		byte[] v1 = randomContent(SMALL);
		byte[] v2 = randomContent(LARGE);

		Transaction tx1 = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(v1));
		commit(tx1);

		refetchNode2();
		KnowledgeItem node2Item = node2Item(item);
		assertContent(v1, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME,
			(BinaryData) node2Item.getAttributeValue(ATTR));

		Transaction tx2 = begin();
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(v2));
		commit(tx2);

		refetchNode2();
		assertContent(v2, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME,
			(BinaryData) node2Item.getAttributeValue(ATTR));

		Transaction tx3 = begin();
		item.setAttributeValue(ATTR, null);
		commit(tx3);

		refetchNode2();
		assertNull(node2Item.getAttributeValue(ATTR));
	}

	/** An attribute can change between a binary and a non-binary value. */
	public void testChangeValueKind() throws Exception {
		byte[] content = randomContent(SMALL);

		Transaction tx1 = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, "text");
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content));
		commit(tx2);
		Revision r2 = tx2.getCommitRevision();

		Transaction tx3 = begin();
		item.setAttributeValue(ATTR, Long.valueOf(42));
		commit(tx3);

		assertEquals(Long.valueOf(42), item.getAttributeValue(ATTR));
		assertEquals("text", HistoryUtils.getKnowledgeItem(r1, item).getAttributeValue(ATTR));
		assertContent(content, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME,
			(BinaryData) HistoryUtils.getKnowledgeItem(r2, item).getAttributeValue(ATTR));

		refetchNode2();
		assertEquals(Long.valueOf(42), node2Item(item).getAttributeValue(ATTR));
	}

	/** Deleting a value or the object outdates the row of the value. */
	public void testDelete() throws Exception {
		byte[] content = randomContent(LARGE);

		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content));
		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content));
		KnowledgeObject e3 = newE("e3");
		e3.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content));
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		e1.setAttributeValue(ATTR, null);
		e2.delete();
		e3.delete();
		commit(tx2);

		assertNull(e1.getAttributeValue(ATTR));
		for (KnowledgeObject item : List.of(e1, e2, e3)) {
			List<BinaryRow> rows = binaryRows(item);
			assertEquals(1, rows.size());
			assertEquals(tx2.getCommitRevision().getCommitNumber() - 1, rows.get(0)._revMax);
		}
		assertContent(content, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME,
			(BinaryData) HistoryUtils.getKnowledgeItem(r1, e2).getAttributeValue(ATTR));

		refetchNode2();
		assertNull(node2Item(e1).getAttributeValue(ATTR));
	}

	/** A rollback leaves the object unchanged. */
	public void testRollback() throws Exception {
		byte[] original = randomContent(SMALL);
		byte[] update = randomContent(LARGE);

		Transaction tx = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(original));
		commit(tx);

		Transaction tx2 = begin();
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(update));
		String updateKey = ((BlobBinaryData) item.getAttributeValue(ATTR)).getKey();
		rollback(tx2);

		assertContent(original, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME,
			(BinaryData) item.getAttributeValue(ATTR));
		assertEquals(1, binaryRows(item).size());
		assertTrue("Unreferenced content stays in the store.",
			storeKeys(BlobStoreService.DEFAULT_STORE_NAME).contains(updateKey));

		refetchNode2();
		assertContent(original, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME,
			(BinaryData) node2Item(item).getAttributeValue(ATTR));
	}

	/** Bulk loading delivers the binary values together with the other dynamic values. */
	public void testBulkLoad() throws Exception {
		int count = 30;
		List<KnowledgeObject> items = new ArrayList<>();
		List<byte[]> contents = new ArrayList<>();
		Transaction tx = begin();
		for (int n = 0; n < count; n++) {
			KnowledgeObject item = newE("e" + n);
			byte[] content = randomContent(100 + n);
			if (n % 3 != 0) {
				item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content));
			}
			if (n % 2 == 0) {
				item.setAttributeValue("flex", "value" + n);
			}
			items.add(item);
			contents.add(content);
		}
		commit(tx);

		refetchNode2();
		List<KnowledgeItem> node2Items = new ArrayList<>();
		for (KnowledgeObject item : items) {
			node2Items.add(node2Item(item));
		}
		FlexAttributeFetch.INSTANCE.prepareKnowledgeItems(node2Items);

		// The values must be loaded by the bulk load: The rows are not needed any more.
		deleteBinaryRows();

		for (int n = 0; n < count; n++) {
			KnowledgeItem node2Item = node2Items.get(n);
			if (n % 3 != 0) {
				assertContent(contents.get(n), BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME,
					(BinaryData) node2Item.getAttributeValue(ATTR));
			} else {
				assertNull(node2Item.getAttributeValue(ATTR));
			}
			if (n % 2 == 0) {
				assertEquals("value" + n, node2Item.getAttributeValue("flex"));
			} else {
				assertNull(node2Item.getAttributeValue("flex"));
			}
		}
	}

	/** Creating a branch copies the binary values of the branched types. */
	public void testBranch() throws Exception {
		if (noMultipleBranches()) {
			return;
		}
		byte[] small = randomContent(SMALL);
		byte[] large = randomContent(LARGE);

		Transaction tx = begin();
		KnowledgeObject e1 = newB("b1");
		e1.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(small, "text/plain", "s.txt"));
		KnowledgeObject e2 = newB("b2");
		e2.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(large, "text/plain", "l.txt"));
		commit(tx);

		Set<MetaObject> branchScheme = new HashSet<>();
		branchScheme.add(type(B_NAME));
		Branch branch = HistoryUtils.createBranch(HistoryUtils.getTrunk(), tx.getCommitRevision(), branchScheme);

		KnowledgeItem e1Branch = HistoryUtils.getKnowledgeItem(branch, e1);
		KnowledgeItem e2Branch = HistoryUtils.getKnowledgeItem(branch, e2);
		assertNotNull(e1Branch);
		assertContent(small, "text/plain", "s.txt", (BinaryData) e1Branch.getAttributeValue(ATTR));
		assertContent(large, "text/plain", "l.txt", (BinaryData) e2Branch.getAttributeValue(ATTR));
	}

	/** Purging a deleted object erases the rows of its binary values. */
	public void testPurge() throws Exception {
		Transaction tx = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(randomContent(SMALL)));
		commit(tx);
		ObjectKey key = item.tId();
		List<BinaryRow> rowsBefore = binaryRows(item);
		assertEquals(1, rowsBefore.size());

		Transaction tx2 = begin();
		item.delete();
		commit(tx2);

		ConnectionPool pool = kb().getConnectionPool();
		DeletedObjectPurge purge = new DeletedObjectPurge(pool.getSQLDialect(), pool, kb().getMORepository());
		DeletedObjectPurge.Report report = purge.purge(Arrays.asList(key), new AssertProtocol(getClass().getName()));
		assertNotNull("Rows of binary values erased: " + report,
			report.getTable(AbstractFlexDataManager.FLEX_BINARY_DATA_DB_NAME));
		assertEquals(0, binaryRows(key).size());
	}

	/** Renaming a dynamic attribute in a migration renames its binary values. */
	public void testRenameInMigration() throws Exception {
		String newName = "renamedBinary";
		byte[] content = randomContent(SMALL);

		Transaction tx = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(ATTR, BinaryDataFactory.createBinaryData(content));
		commit(tx);
		ObjectKey key = item.tId();

		String processorConfig = "<processor"
			+ " class='" + AlterColumnProcessor.class.getName() + "'"
			+ " table='" + E_NAME + "'"
			+ " column='" + ATTR + "'"
			+ " new-name='" + newName + "'"
			+ "/>";
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<AlterColumnProcessor> config =
			TypedConfiguration.parse("processor", PolymorphicConfiguration.class,
				CharacterContents.newContent(processorConfig));
		AlterColumnProcessor processor = TypedConfigUtil.createInstance(config);

		ConnectionPool pool = kb().getConnectionPool();
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public MORepository getPersistentRepository() {
					return kb().getMORepository();
				}

				@Override
				public SchemaConfiguration getPersistentSchema() {
					return getApplicationSchema();
				}
			};
			processor.doMigration(context, log, connection);
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
		assertTrue("Renamed rows logged: " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.startsWith("Renamed 1 flex attributes")));

		assertEquals(0, binaryRows(key, ATTR).size());
		List<BinaryRow> renamed = binaryRows(key, newName);
		assertEquals(1, renamed.size());
		assertEquals(content.length, renamed.get(0)._size);
	}

	/** The storage policy chooses store and threshold per attribute. */
	public void testStoragePolicy() throws Exception {
		String special = "special";
		DynamicBinaryStoragePolicy policy = (item, attribute, defaults) -> attribute.equals(special)
			? new BinaryStorageSettings(OTHER_STORE, 100) : defaults;
		FlexDataManager manager = new FlexVersionedDataManager(kb().getConnectionPool(),
			kb().lookupType(AbstractFlexDataManager.FLEX_DATA),
			kb().lookupType(AbstractFlexDataManager.FLEX_BINARY_DATA),
			new BinaryStorageSettings(null, 1000), policy);

		byte[] content = randomContent(500);

		Transaction tx = begin();
		KnowledgeObject item = newE("e1");
		commit(tx);

		BinaryData specialValue =
			manager.toStoredValue(item, special, BinaryDataFactory.createBinaryData(content, "a/b", "special"));
		assertTrue(specialValue instanceof BlobBinaryData);
		assertEquals(OTHER_STORE, ((BlobBinaryData) specialValue).getStoreName());
		assertTrue(storeKeys(OTHER_STORE).contains(((BlobBinaryData) specialValue).getKey()));

		BinaryData plainValue =
			manager.toStoredValue(item, ATTR, BinaryDataFactory.createBinaryData(content, "a/b", "plain"));
		assertFalse("Below the default threshold.", plainValue instanceof BlobBinaryData);

		Transaction tx2 = begin();
		FlexData data = manager.load(kb(), item.tId(), true);
		data.setAttributeValue(special, specialValue);
		data.setAttributeValue(ATTR, plainValue);
		commitHandler().addCommittable(new CommittableAdapter() {
			@Override
			public boolean prepare(CommitContext context) {
				return manager.store(item.tId(), data, context);
			}
		});
		commit(tx2);

		BinaryRow specialRow = binaryRows(item.tId(), special).get(0);
		assertEquals(OTHER_STORE, specialRow._store);
		assertEquals(((BlobBinaryData) specialValue).getKey(), specialRow._key);
		assertTrue(binaryRows(item.tId(), ATTR).get(0)._inline);

		FlexData loaded = manager.load(kb(), item.tId(), false);
		BinaryData loadedSpecial = (BinaryData) loaded.getAttributeValue(special);
		assertEquals(OTHER_STORE, ((BlobBinaryData) loadedSpecial).getStoreName());
		assertContent(content, "a/b", "special", loadedSpecial);
		assertContent(content, "a/b", "plain", (BinaryData) loaded.getAttributeValue(ATTR));
	}

	private static final class BinaryRow {
		String _key;

		String _hash;

		String _store;

		long _size;

		String _contentType;

		String _name;

		boolean _inline;

		long _revMin;

		long _revMax;
	}

	private List<BinaryRow> binaryRows(KnowledgeItem item) throws SQLException {
		return binaryRows(item.tId(), ATTR);
	}

	private List<BinaryRow> binaryRows(ObjectKey key) throws SQLException {
		return binaryRows(key, ATTR);
	}

	/**
	 * The rows of the given attribute of the given object in the binary table, latest first.
	 */
	private List<BinaryRow> binaryRows(ObjectKey key, String attribute) throws SQLException {
		MOKnowledgeItemImpl table = kb().lookupType(AbstractFlexDataManager.FLEX_BINARY_DATA);
		AbstractBinaryAttribute content =
			(AbstractBinaryAttribute) table.getAttributeOrNull(AbstractFlexDataManager.CONTENT);

		PooledConnection connection = kb().getConnectionPool().borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			String query = "SELECT "
				+ sql.columnRef(content.getKeyColumn().getDBName()) + ", "
				+ sql.columnRef(content.getHashColumn().getDBName()) + ", "
				+ sql.columnRef(content.getStoreColumn().getDBName()) + ", "
				+ sql.columnRef(content.getSizeColumn().getDBName()) + ", "
				+ sql.columnRef(content.getContentTypeColumn().getDBName()) + ", "
				+ sql.columnRef(content.getNameColumn().getDBName()) + ", "
				+ sql.columnRef(BasicTypes.REV_MIN_DB_NAME) + ", "
				+ sql.columnRef(BasicTypes.REV_MAX_DB_NAME) + ", "
				+ sql.columnRef(content.getDataColumn().getDBName())
				+ " FROM " + sql.tableRef(table.getDBName())
				+ " WHERE " + sql.columnRef(AbstractFlexDataManager.TYPE_DBNAME) + " = ?"
				+ " AND " + sql.columnRef(AbstractFlexDataManager.IDENTIFIER_DBNAME) + " = ?"
				+ " AND " + sql.columnRef(AbstractFlexDataManager.ATTRIBUTE_DBNAME) + " = ?"
				+ " ORDER BY " + sql.columnRef(BasicTypes.REV_MIN_DB_NAME) + " DESC";
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				statement.setString(1, key.getObjectType().getName());
				IdentifierUtil.setId(statement, 2, key.getObjectName());
				statement.setString(3, attribute);
				List<BinaryRow> result = new ArrayList<>();
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						BinaryRow row = new BinaryRow();
						row._key = resultSet.getString(1);
						row._hash = resultSet.getString(2);
						row._store = resultSet.getString(3);
						row._size = resultSet.getLong(4);
						row._contentType = resultSet.getString(5);
						row._name = resultSet.getString(6);
						row._revMin = resultSet.getLong(7);
						row._revMax = resultSet.getLong(8);
						row._inline = resultSet.getBlob(9) != null;
						result.add(row);
					}
				}
				return result;
			}
		} finally {
			kb().getConnectionPool().releaseReadConnection(connection);
		}
	}

	private int flexRowCount(KnowledgeItem item) throws SQLException {
		MOKnowledgeItemImpl table = kb().lookupType(AbstractFlexDataManager.FLEX_DATA);
		PooledConnection connection = kb().getConnectionPool().borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			String query = "SELECT COUNT(*) FROM " + sql.tableRef(table.getDBName())
				+ " WHERE " + sql.columnRef(AbstractFlexDataManager.IDENTIFIER_DBNAME) + " = ?"
				+ " AND " + sql.columnRef(AbstractFlexDataManager.ATTRIBUTE_DBNAME) + " = ?";
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				IdentifierUtil.setId(statement, 1, item.getObjectName());
				statement.setString(2, ATTR);
				try (ResultSet resultSet = statement.executeQuery()) {
					assertTrue(resultSet.next());
					return resultSet.getInt(1);
				}
			}
		} finally {
			kb().getConnectionPool().releaseReadConnection(connection);
		}
	}

	private void deleteBinaryRows() throws SQLException {
		MOKnowledgeItemImpl table = kb().lookupType(AbstractFlexDataManager.FLEX_BINARY_DATA);
		PooledConnection connection = kb().getConnectionPool().borrowWriteConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			try (PreparedStatement statement =
				connection.prepareStatement("DELETE FROM " + sql.tableRef(table.getDBName()))) {
				statement.executeUpdate();
			}
			connection.commit();
		} finally {
			kb().getConnectionPool().releaseWriteConnection(connection);
		}
	}

	private List<String> storeKeys(String storeName) throws IOException {
		try (Stream<BlobInfo> blobs = _service.getStore(storeName).list()) {
			return blobs.map(BlobInfo::key).collect(Collectors.toList());
		}
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

	private static byte[] randomContent(int size) {
		byte[] result = new byte[size];
		new Random(size).nextBytes(result);
		return result;
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestDynamicBinaryAttributes.class);
	}

}
