/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.module.TestModuleUtil;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.blob.FileSystemBlobStore;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.dob.attr.HybridBinaryAttribute;
import com.top_logic.dob.attr.RefBinaryAttribute;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.blob.BlobGarbageCollector;
import com.top_logic.knowledge.service.blob.BlobGarbageCollector.KeyColumn;
import com.top_logic.knowledge.service.blob.BlobGarbageCollector.Result;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Test of {@link BlobGarbageCollector} on the blobs referenced by declared and dynamic binary
 * attributes.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBlobGarbageCollection extends AbstractDBKnowledgeBaseClusterTest {

	static final String TYPE = "BlobGCKinds";

	static final String REF_ATTR = "refData";

	static final String HYBRID_ATTR = "hybridData";

	static final String DYNAMIC_ATTR = "dynBinary";

	static final String OTHER_STORE = "other";

	/** Larger than the threshold of the hybrid attribute. */
	static final int LARGE_HYBRID = 2000;

	/** Larger than the default threshold of the dynamic binary attributes (64 KB). */
	static final int LARGE_DYNAMIC = 70 * 1024;

	private static final String TYPE_SCHEMA =
		"<" + DOXMLConstants.META_OBJECT_ELEMENT + " object_name='" + TYPE + "'>"
			+ "<attributes>"
			+ "<" + RefBinaryAttribute.Config.TAG_NAME + " att_name='" + REF_ATTR + "' store='" + OTHER_STORE + "'/>"
			+ "<" + HybridBinaryAttribute.Config.TAG_NAME + " att_name='" + HYBRID_ATTR + "' threshold='1KB'/>"
			+ "</attributes>"
			+ "</" + DOXMLConstants.META_OBJECT_ELEMENT + ">";

	private File _root;

	private BlobStoreService _service;

	private BlobStoreService _formerService;

	private int _contentSeed;

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseClusterTestSetup setup = (DBKnowledgeBaseClusterTestSetup) super.createSetup(self);
		setup.addAdditionalTypes((log, typeFactory, typeRepository) -> {
			try {
				typeRepository.addMetaObject(createType(TYPE_SCHEMA));
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
		_root = createdCleanTestDir("blob-garbage-collection");
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

	/** The key columns of the declared attributes and of the dynamic binary table are found. */
	public void testKeyColumns() {
		List<String> tables = collector().getKeyColumns().stream()
			.map(column -> column.table().getName() + "#" + column.attribute().getName())
			.collect(Collectors.toList());
		assertTrue(tables.toString(), tables.contains(TYPE + "#" + REF_ATTR));
		assertTrue(tables.toString(), tables.contains(TYPE + "#" + HYBRID_ATTR));
		assertTrue(tables.toString(),
			tables.contains(AbstractFlexDataManager.FLEX_BINARY_DATA + "#" + AbstractFlexDataManager.CONTENT));
		for (KeyColumn column : collector().getKeyColumns()) {
			assertNotNull(column.column());
		}
	}

	/** Only blobs not referenced by any row are deleted, and only after the grace period. */
	public void testCollect() throws Exception {
		boolean versioned = type(TYPE).isVersioned();
		List<String> committed = new ArrayList<>();
		List<String> historic = new ArrayList<>();
		List<String> orphans = new ArrayList<>();

		// Initial values.
		Transaction tx1 = begin();
		KnowledgeObject item = newA(TYPE, "a1");
		setContent(item, REF_ATTR, 5000);
		setContent(item, HYBRID_ATTR, LARGE_HYBRID);
		KnowledgeObject dynamic = newE("e1");
		setContent(dynamic, DYNAMIC_ATTR, LARGE_DYNAMIC);
		KnowledgeObject deleted = newA(TYPE, "deleted");
		setContent(deleted, REF_ATTR, 3000);
		setContent(deleted, HYBRID_ATTR, LARGE_HYBRID);
		KnowledgeObject deletedDynamic = newE("e2");
		setContent(deletedDynamic, DYNAMIC_ATTR, LARGE_DYNAMIC);
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();
		List<String> initial = List.of(key(item, REF_ATTR), key(item, HYBRID_ATTR), key(dynamic, DYNAMIC_ATTR));
		List<String> ofDeleted =
			List.of(key(deleted, REF_ATTR), key(deleted, HYBRID_ATTR), key(deletedDynamic, DYNAMIC_ATTR));

		// Overwrite: the former values stay referenced by historic revisions.
		Transaction tx2 = begin();
		setContent(item, REF_ATTR, 5001);
		setContent(item, HYBRID_ATTR, LARGE_HYBRID + 1);
		setContent(dynamic, DYNAMIC_ATTR, LARGE_DYNAMIC + 1);
		// Overwritten within the same transaction: never committed.
		orphans.add(setContent(deleted, REF_ATTR, 3001));
		setContent(deleted, REF_ATTR, 3002);
		commit(tx2);
		committed.add(key(item, REF_ATTR));
		committed.add(key(item, HYBRID_ATTR));
		committed.add(key(dynamic, DYNAMIC_ATTR));
		List<String> ofDeletedOverwritten = List.of(key(deleted, REF_ATTR));

		// Rollback: the uploaded content is never referenced.
		Transaction tx3 = begin();
		orphans.add(setContent(item, REF_ATTR, 5002));
		orphans.add(setContent(item, HYBRID_ATTR, LARGE_HYBRID + 2));
		orphans.add(setContent(dynamic, DYNAMIC_ATTR, LARGE_DYNAMIC + 2));
		rollback(tx3);

		// Delete: the values stay referenced by historic revisions.
		Transaction tx4 = begin();
		deleted.delete();
		deletedDynamic.delete();
		commit(tx4);

		if (versioned) {
			historic.addAll(initial);
			historic.addAll(ofDeleted);
			historic.addAll(ofDeletedOverwritten);
		} else {
			orphans.addAll(initial);
			orphans.addAll(ofDeleted);
			orphans.addAll(ofDeletedOverwritten);
		}

		Set<String> before = allKeys();
		assertTrue(before.containsAll(committed));
		assertTrue(before.containsAll(historic));
		assertTrue(before.containsAll(orphans));

		// Within the grace period nothing is deleted.
		List<Result> young = collectAll(BlobGarbageCollector.DEFAULT_GRACE_PERIOD, Instant.now());
		for (Result result : young) {
			assertFalse(result.aborted());
			assertEquals(0, result.deleted());
			assertEquals(0, result.failures());
		}
		assertEquals(before, allKeys());

		// After the grace period exactly the orphans are deleted.
		List<Result> results = collectAll(Duration.ZERO, Instant.now().plusSeconds(60));
		long deletedCount = 0;
		for (Result result : results) {
			assertFalse(result.aborted());
			assertEquals(0, result.failures());
			assertEquals(0, result.keptByGrace());
			deletedCount += result.deleted();
		}
		assertEquals(orphans.size(), deletedCount);

		Set<String> after = allKeys();
		for (String orphan : orphans) {
			assertFalse("Orphan not deleted: " + orphan, after.contains(orphan));
		}
		assertTrue(after.containsAll(committed));
		assertTrue(after.containsAll(historic));
		assertEquals(committed.size() + historic.size(), after.size());

		// All committed content is still readable.
		assertReadable((BinaryData) item.getAttributeValue(REF_ATTR));
		assertReadable((BinaryData) item.getAttributeValue(HYBRID_ATTR));
		assertReadable((BinaryData) dynamic.getAttributeValue(DYNAMIC_ATTR));
		if (versioned) {
			KnowledgeItem historicItem = HistoryUtils.getKnowledgeItem(r1, item);
			assertReadable((BinaryData) historicItem.getAttributeValue(REF_ATTR));
			assertReadable((BinaryData) historicItem.getAttributeValue(HYBRID_ATTR));
			KnowledgeItem historicDynamic = HistoryUtils.getKnowledgeItem(r1, dynamic);
			assertReadable((BinaryData) historicDynamic.getAttributeValue(DYNAMIC_ATTR));
			KnowledgeItem historicDeleted = HistoryUtils.getKnowledgeItem(r1, deleted);
			assertReadable((BinaryData) historicDeleted.getAttributeValue(REF_ATTR));
			KnowledgeItem historicDeletedDynamic = HistoryUtils.getKnowledgeItem(r1, deletedDynamic);
			assertReadable((BinaryData) historicDeletedDynamic.getAttributeValue(DYNAMIC_ATTR));
		}

		// A second run finds nothing left to do.
		for (Result result : collectAll(Duration.ZERO, Instant.now().plusSeconds(60))) {
			assertEquals(0, result.deleted());
		}
	}

	/** Leftover temporary files of the file system store are removed after the grace period. */
	public void testTempFileCleanup() throws Exception {
		FileSystemBlobStore store = (FileSystemBlobStore) _service.getStore(OTHER_STORE);
		Path tempDir = store.getRoot().resolve(FileSystemBlobStore.TEMP_DIR_NAME);
		Files.createDirectories(tempDir);
		Path oldFile = Files.write(tempDir.resolve("old-upload"), new byte[] { 1, 2, 3 });
		Files.setLastModifiedTime(oldFile, FileTime.from(Instant.now().minus(Duration.ofDays(2))));
		Path youngFile = Files.write(tempDir.resolve("young-upload"), new byte[] { 4, 5, 6 });

		BufferingProtocol log = new BufferingProtocol();
		Result result = collector().collect(store, BlobGarbageCollector.DEFAULT_GRACE_PERIOD, Instant.now(), log);
		assertFalse(log.getErrors().toString(), log.hasErrors());
		assertFalse(result.aborted());

		assertFalse("Old temporary file removed.", Files.exists(oldFile));
		assertTrue("Young temporary file kept.", Files.exists(youngFile));
	}

	private List<Result> collectAll(Duration grace, Instant now) throws Exception {
		BlobGarbageCollector collector = collector();
		List<Result> results = new ArrayList<>();
		for (BlobStore store : _service.getStores().values()) {
			BufferingProtocol log = new BufferingProtocol();
			results.add(collector.collect(store, grace, now, log));
			assertFalse(log.getErrors().toString(), log.hasErrors());
		}
		return results;
	}

	private BlobGarbageCollector collector() {
		return BlobGarbageCollector.newInstance(kb());
	}

	/**
	 * Sets new external content and returns its key.
	 */
	private String setContent(KnowledgeObject item, String attr, int size) throws Exception {
		item.setAttributeValue(attr, BinaryDataFactory.createBinaryData(randomContent(size)));
		return key(item, attr);
	}

	private static String key(KnowledgeObject item, String attr) {
		Object value = item.getAttributeValue(attr);
		assertTrue("Content stored externally: " + attr, value instanceof BlobBinaryData);
		return ((BlobBinaryData) value).getKey();
	}

	private Set<String> allKeys() throws IOException {
		Set<String> result = new HashSet<>();
		for (BlobStore store : _service.getStores().values()) {
			try (Stream<BlobInfo> blobs = store.list()) {
				blobs.map(BlobInfo::key).forEach(result::add);
			}
		}
		return result;
	}

	private static void assertReadable(BinaryData data) throws IOException {
		assertNotNull(data);
		try (InputStream in = data.getStream()) {
			assertEquals(data.getSize(), StreamUtilities.readStreamContents(in).length);
		}
	}

	private byte[] randomContent(int size) {
		byte[] result = new byte[size];
		new Random(size * 31 + _contentSeed++).nextBytes(result);
		return result;
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestBlobGarbageCollection.class);
	}

}
