/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.blob;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.basic.module.TestModuleUtil;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.HashingInputStream;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.basic.io.blob.FileSystemBlobStore;

/**
 * Test of {@link BlobUpload} and {@link BlobBinaryData}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBlobUpload extends BasicTestCase {

	private static final int THRESHOLD = 100;

	private File _root;

	private BlobStoreService _service;

	private BlobStoreService _formerService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir("blob-upload");
		BlobStoreService.Config<?> config = TypedConfiguration.newConfigItem(BlobStoreService.Config.class);
		FileSystemBlobStore.Config<?> storeConfig = TypedConfiguration.newConfigItem(FileSystemBlobStore.Config.class);
		storeConfig.setName(BlobStoreService.DEFAULT_STORE_NAME);
		storeConfig.setRoot(_root.getPath());
		config.getStores().put(BlobStoreService.DEFAULT_STORE_NAME, storeConfig);
		_service = (BlobStoreService) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		_formerService = TestModuleUtil.installNewInstance(BlobStoreService.Module.INSTANCE, _service);
	}

	@Override
	protected void tearDown() throws Exception {
		TestModuleUtil.installNewInstance(BlobStoreService.Module.INSTANCE, _formerService);
		FileUtilities.deleteR(_root);
		super.tearDown();
	}

	public void testUpload() throws IOException {
		byte[] content = content(1000);
		BlobBinaryData blob =
			BlobUpload.upload(null, BinaryDataFactory.createBinaryData(content, "image/png", "a.png"));
		assertNull(blob.getStoreName());
		assertEquals(1000, blob.getSize());
		assertEquals("image/png", blob.getContentType());
		assertEquals("a.png", blob.getName());
		assertEquals(sha256(content), blob.getHash());
		assertTrue(Arrays.equals(content, StreamUtilities.readStreamContents(blob)));
	}

	public void testUploadUnknownSize() throws IOException {
		byte[] content = content(1000);
		BlobBinaryData blob = BlobUpload.upload(null, unknownSize(content));
		assertEquals(1000, blob.getSize());
		assertEquals(sha256(content), blob.getHash());
	}

	public void testThreshold() throws IOException {
		assertFalse(BlobUpload.uploadAboveThreshold(null, THRESHOLD,
			BinaryDataFactory.createBinaryData(content(THRESHOLD - 1))) instanceof BlobBinaryData);
		assertTrue(BlobUpload.uploadAboveThreshold(null, THRESHOLD,
			BinaryDataFactory.createBinaryData(content(THRESHOLD))) instanceof BlobBinaryData);

		BinaryData small = BlobUpload.uploadAboveThreshold(null, THRESHOLD, unknownSize(content(THRESHOLD - 1)));
		assertFalse(small instanceof BlobBinaryData);
		assertEquals(THRESHOLD - 1, small.getSize());
		assertEquals("unknown.bin", small.getName());

		byte[] large = content(THRESHOLD * 5 + 3);
		BinaryData uploaded = BlobUpload.uploadAboveThreshold(null, THRESHOLD, unknownSize(large));
		assertTrue(uploaded instanceof BlobBinaryData);
		assertEquals(large.length, uploaded.getSize());
		assertEquals(sha256(large), ((BlobBinaryData) uploaded).getHash());
		assertTrue(Arrays.equals(large, StreamUtilities.readStreamContents(uploaded)));
	}

	/** A small blob that is kept inline is copied, not referenced. */
	public void testInlineCopyOfBlob() throws IOException {
		BlobBinaryData blob = BlobUpload.upload(null, BinaryDataFactory.createBinaryData(content(10)));
		BinaryData inline = BlobUpload.uploadAboveThreshold(null, THRESHOLD, blob);
		assertFalse(inline instanceof BlobBinaryData);
		assertEquals(blob, inline);
	}

	/** Inline content is never uploaded, independent of its size. */
	public void testInline() throws IOException {
		BlobBinaryData blob = BlobUpload.upload(null, BinaryDataFactory.createBinaryData(content(THRESHOLD * 2)));
		BinaryData copy = BlobUpload.inline(blob);
		assertFalse(copy instanceof BlobBinaryData);
		assertEquals(blob, copy);

		byte[] large = content(THRESHOLD * 5);
		BinaryData unknown = BlobUpload.inline(unknownSize(large));
		assertFalse(unknown instanceof BlobBinaryData);
		assertEquals(large.length, unknown.getSize());
		assertTrue(Arrays.equals(large, StreamUtilities.readStreamContents(unknown)));

		BinaryData known = BinaryDataFactory.createBinaryData(content(10));
		assertSame(known, BlobUpload.inline(known));
		assertNull(BlobUpload.inline(null));
	}

	/** Content of unknown size cannot be decided with a threshold too large for buffering. */
	public void testThresholdTooLargeForUnknownSize() throws IOException {
		try {
			BlobUpload.uploadAboveThreshold(null, BlobUpload.MAX_BUFFERED_THRESHOLD + 1L, unknownSize(content(10)));
			fail("Threshold too large for buffering expected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	public void testWithKnownSize() throws IOException {
		BinaryData known = BinaryDataFactory.createBinaryData(content(10));
		assertSame(known, BlobUpload.withKnownSize(known));
		assertEquals(10, BlobUpload.withKnownSize(unknownSize(content(10))).getSize());
		assertNull(BlobUpload.withKnownSize(null));
	}

	public void testIntegrity() throws IOException {
		byte[] content = content(1000);
		BlobBinaryData blob = BlobUpload.upload(null, BinaryDataFactory.createBinaryData(content));
		content[0] ^= 1;
		Files.write(((FileSystemBlobStore) _service.getDefaultStore()).getFile(blob.getKey()), content);
		try (InputStream in = blob.getStream()) {
			StreamUtilities.readStreamContents(in);
			fail("Modified content must be detected.");
		} catch (IOException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(blob.getKey()));
		}
	}

	public void testEqualsByHash() throws IOException {
		byte[] content = content(500);
		BlobBinaryData blob1 = BlobUpload.upload(null, BinaryDataFactory.createBinaryData(content));
		BlobBinaryData blob2 = BlobUpload.upload(null, BinaryDataFactory.createBinaryData(content));
		BlobBinaryData other = BlobUpload.upload(null, BinaryDataFactory.createBinaryData(content(501)));
		assertNotEquals(blob1.getKey(), blob2.getKey());
		assertEquals(blob1, blob2);
		assertNotEquals(blob1, other);
		assertEquals(blob1, BinaryDataFactory.createBinaryData(content));
	}

	private static String sha256(byte[] content) throws IOException {
		HashingInputStream in = HashingInputStream.sha256(new ByteArrayInputStream(content));
		StreamUtilities.readStreamContents(in);
		assertTrue(in.isEnd());
		assertEquals(content.length, in.getCount());
		return in.getHash();
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
				return "unknown.bin";
			}

			@Override
			public String getContentType() {
				return BinaryData.CONTENT_TYPE_OCTET_STREAM;
			}
		};
	}

	private static byte[] content(int size) {
		byte[] result = new byte[size];
		new Random(size).nextBytes(result);
		return result;
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestBlobUpload.class, BlobStoreService.Module.INSTANCE,
				Settings.Module.INSTANCE));
	}

}
