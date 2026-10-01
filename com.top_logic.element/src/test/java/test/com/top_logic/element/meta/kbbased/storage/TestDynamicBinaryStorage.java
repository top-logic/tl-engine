/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.knowledge.service.KBUtils.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.TestModuleUtil;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.blob.FileSystemBlobStore;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.TLBinaryStorage;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.util.model.ModelService;

/**
 * Test of the {@link TLBinaryStorage} annotation on binary attributes stored in the table of
 * dynamic binary values.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDynamicBinaryStorage extends BasicTestCase {

	private static final Class<TestDynamicBinaryStorage> THIS_CLASS = TestDynamicBinaryStorage.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String TYPE_NAME = "Node";

	private static final String PLAIN_ATTRIBUTE = "plain";

	private static final String ANNOTATED_ATTRIBUTE = "annotated";

	private static final String STORE_ONLY_ATTRIBUTE = "storeOnly";

	private static final String OTHER_STORE = "other";

	/** Larger than the default threshold of 64 KB. */
	private static final int LARGE = 70 * 1024;

	private File _root;

	private BlobStoreService _service;

	private BlobStoreService _formerService;

	private TLObject _node;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir("dynamic-binary-storage");
		_service = newService();
		_formerService = TestModuleUtil.installNewInstance(BlobStoreService.Module.INSTANCE, _service);
		inTransaction(() -> _node = DynamicModelService.getInstance().createObject(getNodeType()));
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> _node.tDelete());
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

	public void testAnnotation() {
		TLBinaryStorage annotation =
			getNodeType().getPart(ANNOTATED_ATTRIBUTE).getAnnotation(TLBinaryStorage.class);
		assertEquals(OTHER_STORE, annotation.getStore());
		assertEquals(Long.valueOf(100), annotation.getThreshold());

		TLBinaryStorage storeOnly =
			getNodeType().getPart(STORE_ONLY_ATTRIBUTE).getAnnotation(TLBinaryStorage.class);
		assertEquals(OTHER_STORE, storeOnly.getStore());
		assertNull(storeOnly.getThreshold());
	}

	/** Content above the annotated threshold is stored in the annotated store. */
	public void testAnnotatedStoreAndThreshold() throws IOException {
		byte[] content = randomContent(500);

		store(ANNOTATED_ATTRIBUTE, content);

		BlobBinaryData stored = (BlobBinaryData) storedValue(ANNOTATED_ATTRIBUTE);
		assertEquals(OTHER_STORE, stored.getStoreName());
		try (InputStream in = _service.getStore(OTHER_STORE).get(stored.getKey())) {
			assertEquals(content, StreamUtilities.readStreamContents(in));
		}
		assertContent(content, load(ANNOTATED_ATTRIBUTE));
	}

	/** Content below the annotated threshold is stored inline. */
	public void testBelowAnnotatedThreshold() throws IOException {
		byte[] content = randomContent(50);

		store(ANNOTATED_ATTRIBUTE, content);

		assertFalse(storedValue(ANNOTATED_ATTRIBUTE) instanceof BlobBinaryData);
		assertContent(content, load(ANNOTATED_ATTRIBUTE));
	}

	/** An annotation without threshold uses the default threshold. */
	public void testStoreOnly() throws IOException {
		byte[] small = randomContent(1000);
		store(STORE_ONLY_ATTRIBUTE, small);
		assertFalse(storedValue(STORE_ONLY_ATTRIBUTE) instanceof BlobBinaryData);

		byte[] large = randomContent(LARGE);
		store(STORE_ONLY_ATTRIBUTE, large);
		assertEquals(OTHER_STORE, ((BlobBinaryData) storedValue(STORE_ONLY_ATTRIBUTE)).getStoreName());
		assertContent(large, load(STORE_ONLY_ATTRIBUTE));
	}

	/** An attribute without annotation uses the default store and threshold. */
	public void testPlain() throws IOException {
		byte[] small = randomContent(500);
		store(PLAIN_ATTRIBUTE, small);
		assertFalse(storedValue(PLAIN_ATTRIBUTE) instanceof BlobBinaryData);

		byte[] large = randomContent(LARGE);
		store(PLAIN_ATTRIBUTE, large);
		BlobBinaryData stored = (BlobBinaryData) storedValue(PLAIN_ATTRIBUTE);
		assertNull(stored.getStoreName());
		assertContent(large, load(PLAIN_ATTRIBUTE));
	}

	/**
	 * A binary value given as initial value at object creation gets the annotated settings,
	 * independent of the order of the initial values.
	 */
	public void testInitialValue() throws IOException {
		byte[] content = randomContent(500);
		BinaryData data = BinaryDataFactory.createBinaryData(content, "text/plain", "initial.txt");

		NameValueBuffer values = new NameValueBuffer();
		values.setValue(ANNOTATED_ATTRIBUTE, data);
		values.setValue(PersistentObject.TYPE_REF, getNodeType().tHandle());

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLObject[] created = new TLObject[1];
		inTransaction(() -> created[0] = kb.createObject(kb.getHistoryManager().getContextBranch(),
			TLAnnotations.getTable(getNodeType()), values));
		try {
			Object stored = created[0].tHandle().getAttributeValue(ANNOTATED_ATTRIBUTE);
			assertEquals(OTHER_STORE, ((BlobBinaryData) stored).getStoreName());
			assertContent(content, (BinaryData) created[0].tValueByName(ANNOTATED_ATTRIBUTE));
		} finally {
			inTransaction(() -> created[0].tDelete());
		}
	}

	/**
	 * A binary value set in the transaction creating the object gets the annotated settings.
	 */
	public void testSetInCreateTransaction() throws IOException {
		byte[] content = randomContent(500);

		TLObject[] created = new TLObject[1];
		inTransaction(() -> {
			created[0] = DynamicModelService.getInstance().createObject(getNodeType());
			created[0].tUpdateByName(ANNOTATED_ATTRIBUTE,
				BinaryDataFactory.createBinaryData(content, "text/plain", "created.txt"));
		});
		try {
			Object stored = created[0].tHandle().getAttributeValue(ANNOTATED_ATTRIBUTE);
			assertEquals(OTHER_STORE, ((BlobBinaryData) stored).getStoreName());
			assertContent(content, (BinaryData) created[0].tValueByName(ANNOTATED_ATTRIBUTE));
		} finally {
			inTransaction(() -> created[0].tDelete());
		}
	}

	private void store(String attributeName, byte[] content) {
		inTransaction(() -> _node.tUpdateByName(attributeName,
			BinaryDataFactory.createBinaryData(content, "text/plain", attributeName + ".txt")));
	}

	private BinaryData load(String attributeName) {
		return (BinaryData) _node.tValueByName(attributeName);
	}

	private Object storedValue(String attributeName) {
		return _node.tHandle().getAttributeValue(attributeName);
	}

	private static void assertContent(byte[] expected, BinaryData actual) throws IOException {
		assertNotNull(actual);
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
