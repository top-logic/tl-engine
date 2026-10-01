/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.blob;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.basic.module.TestModuleUtil;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.DirectDownload;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.blob.FileSystemBlobStore;

/**
 * Test of {@link DirectDownload} offered by {@link BlobBinaryData}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDirectDownload extends BasicTestCase {

	private static final String SIGNING_STORE = "signing";

	private static final URI SIGNING_BASE = URI.create("https://storage.example.com/");

	/**
	 * {@link FileSystemBlobStore} issuing download URLs that encode their arguments.
	 */
	public static class SigningStore extends FileSystemBlobStore {

		/**
		 * Configuration of a {@link SigningStore}.
		 */
		public interface Config<I extends SigningStore> extends FileSystemBlobStore.Config<I> {
			@Override
			@ClassDefault(SigningStore.class)
			Class<? extends I> getImplementationClass();
		}

		/**
		 * Creates a {@link SigningStore}.
		 */
		public SigningStore(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public URI createDownloadUrl(String key, long size, String contentType, String fileName) {
			return SIGNING_BASE.resolve(key + "?size=" + size + "&type=" + contentType + "&name=" + fileName);
		}
	}

	private File _root;

	private BlobStoreService _formerService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir("direct-download");

		BlobStoreService.Config<?> config = TypedConfiguration.newConfigItem(BlobStoreService.Config.class);

		FileSystemBlobStore.Config<?> plainConfig = TypedConfiguration.newConfigItem(FileSystemBlobStore.Config.class);
		plainConfig.setName(BlobStoreService.DEFAULT_STORE_NAME);
		plainConfig.setRoot(new File(_root, "plain").getPath());
		config.getStores().put(plainConfig.getName(), plainConfig);

		SigningStore.Config<?> signingConfig = TypedConfiguration.newConfigItem(SigningStore.Config.class);
		signingConfig.setName(SIGNING_STORE);
		signingConfig.setRoot(new File(_root, "signing").getPath());
		config.getStores().put(signingConfig.getName(), signingConfig);

		BlobStoreService service =
			(BlobStoreService) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		_formerService = TestModuleUtil.installNewInstance(BlobStoreService.Module.INSTANCE, service);
	}

	@Override
	protected void tearDown() throws Exception {
		TestModuleUtil.installNewInstance(BlobStoreService.Module.INSTANCE, _formerService);
		FileUtilities.deleteR(_root);
		super.tearDown();
	}

	/** Content not kept in a blob store is never downloaded directly. */
	public void testInlineContent() {
		assertNull(DirectDownload.getDirectDownloadUrl(null));
		BinaryData inline = BinaryDataFactory.createBinaryData(bytes("hello"), "text/plain", "a.txt");
		assertFalse(inline instanceof DirectDownload);
		assertNull(DirectDownload.getDirectDownloadUrl(inline));
	}

	/** A store without download URLs streams its content. */
	public void testStoreWithoutDownloadUrls() throws IOException {
		BlobBinaryData blob = upload(BlobStoreService.DEFAULT_STORE_NAME, "a.txt");
		assertNull(blob.getDirectDownloadUrl());
		assertNull(DirectDownload.getDirectDownloadUrl(blob));
	}

	/** The URL is created by the store from the key and the metadata of the value. */
	public void testStoreWithDownloadUrls() throws IOException {
		BlobBinaryData blob = upload(SIGNING_STORE, "a.txt");
		URI expected = SIGNING_BASE.resolve(blob.getKey() + "?size=5&type=text/plain&name=a.txt");
		assertEquals(expected, blob.getDirectDownloadUrl());
		assertEquals(expected, DirectDownload.getDirectDownloadUrl(blob));
	}

	/** A value without name passes no file name to the store. */
	public void testNoName() throws IOException {
		BlobBinaryData blob = upload(SIGNING_STORE, null);
		assertEquals(BinaryData.NO_NAME, blob.getName());
		assertEquals(SIGNING_BASE.resolve(blob.getKey() + "?size=5&type=text/plain&name=null"),
			DirectDownload.getDirectDownloadUrl(blob));
	}

	/** The default implementation of a store issues no download URLs. */
	public void testDefaultStore() {
		BlobStore store = BlobStoreService.getInstance().getStore(null);
		assertNull(store.createDownloadUrl("any", 1000, "text/plain", "a.txt"));
	}

	private static BlobBinaryData upload(String store, String name) throws IOException {
		BlobStore target = BlobStoreService.getInstance().getStore(store);
		String key = target.put(new ByteArrayInputStream(bytes("hello")), 5, "text/plain");
		return new BlobBinaryData(store, key, "00", 5, "text/plain", name);
	}

	private static byte[] bytes(String text) {
		return text.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDirectDownload.class, BlobStoreService.Module.INSTANCE,
				Settings.Module.INSTANCE));
	}

}
