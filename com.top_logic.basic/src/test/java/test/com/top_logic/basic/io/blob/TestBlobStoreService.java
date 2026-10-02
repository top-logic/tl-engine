/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.blob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.blob.AbstractBlobStore;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreNames;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.blob.FileSystemBlobStore;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;

/**
 * Test of {@link BlobStoreService}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBlobStoreService extends BasicTestCase {

	private static final String OTHER_STORE = "other";

	private static final String FAILING_STORE = "failing";

	private static final String RECORDING_STORE = "recording";

	private File _root;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir("blob-store-service");
	}

	@Override
	protected void tearDown() throws Exception {
		FileUtilities.deleteR(_root);
		super.tearDown();
	}

	/** Stores are looked up by name, no name means the default store. */
	public void testLookup() {
		BlobStoreService service = newService(BlobStoreService.DEFAULT_STORE_NAME,
			BlobStoreService.DEFAULT_STORE_NAME, OTHER_STORE);

		BlobStore defaultStore = service.getDefaultStore();
		assertNotNull(defaultStore);
		assertEquals(BlobStoreService.DEFAULT_STORE_NAME, defaultStore.getName());
		assertSame(defaultStore, service.getStore(null));
		assertSame(defaultStore, service.getStore(""));
		assertSame(defaultStore, service.getStore(BlobStoreService.DEFAULT_STORE_NAME));

		BlobStore other = service.getStore(OTHER_STORE);
		assertEquals(OTHER_STORE, other.getName());
		assertNotSame(defaultStore, other);

		assertEquals(Arrays.asList(BlobStoreService.DEFAULT_STORE_NAME, OTHER_STORE),
			new ArrayList<>(service.getStores().keySet()));
	}

	/** The default store can be any of the configured stores. */
	public void testConfiguredDefault() {
		BlobStoreService service = newService(OTHER_STORE, BlobStoreService.DEFAULT_STORE_NAME, OTHER_STORE);
		assertEquals(OTHER_STORE, service.getDefaultStore().getName());
		assertSame(service.getDefaultStore(), service.getStore(null));
	}

	/** Looking up an unknown store fails with a message naming the store. */
	public void testUnknownStore() {
		BlobStoreService service = newService(BlobStoreService.DEFAULT_STORE_NAME,
			BlobStoreService.DEFAULT_STORE_NAME);
		try {
			service.getStore("unknown");
			fail("Unknown store must be rejected.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("'unknown'"));
		}
	}

	/** A default store name that names no configured store is a configuration error. */
	public void testMissingDefault() {
		BufferingProtocol log = new BufferingProtocol();
		newService(new SimpleInstantiationContext(log), "missing", BlobStoreService.DEFAULT_STORE_NAME);
		assertTrue("Missing default store must be reported.", log.hasErrors());
	}

	/** The application configuration provides the default store. */
	public void testApplicationConfiguration() {
		BlobStoreService service = BlobStoreService.getInstance();
		BlobStore defaultStore = service.getDefaultStore();
		assertNotNull(defaultStore);
		assertEquals(BlobStoreService.DEFAULT_STORE_NAME, defaultStore.getName());
		assertTrue(defaultStore instanceof FileSystemBlobStore);
	}

	/** Shutting the service down closes all its stores, also if closing one of them fails. */
	public void testStoresClosedOnShutdown() throws Exception {
		BlobStoreService.Config<?> config = (BlobStoreService.Config<?>) ApplicationConfig.getInstance()
			.getServiceConfiguration(BlobStoreService.class);
		Map<String, BlobStore.Config<?>> stores = config.getStores();
		stores.put(FAILING_STORE, closeTrackingConfig(FAILING_STORE, true));
		stores.put(RECORDING_STORE, closeTrackingConfig(RECORDING_STORE, false));
		try {
			restartService();

			CloseTrackingBlobStore failing =
				(CloseTrackingBlobStore) BlobStoreService.getInstance().getStore(FAILING_STORE);
			CloseTrackingBlobStore recording =
				(CloseTrackingBlobStore) BlobStoreService.getInstance().getStore(RECORDING_STORE);
			assertFalse(failing.isClosed());
			assertFalse(recording.isClosed());

			ModuleUtil.INSTANCE.shutDown(BlobStoreService.Module.INSTANCE);

			assertTrue("A store must be closed on shutdown.", failing.isClosed());
			assertTrue("A failing close must not prevent closing the other stores.", recording.isClosed());
		} finally {
			stores.remove(FAILING_STORE);
			stores.remove(RECORDING_STORE);
			restartService();
		}
	}

	/** The store name options list the configured stores, no options without active service. */
	public void testStoreNameOptions() throws ModuleException {
		BlobStoreNames options = new BlobStoreNames();
		assertEquals(new ArrayList<>(BlobStoreService.getInstance().getStores().keySet()), options.apply());
		assertTrue(options.apply().contains(BlobStoreService.getInstance().getDefaultStore().getName()));

		ModuleUtil.INSTANCE.shutDown(BlobStoreService.Module.INSTANCE);
		try {
			assertFalse(BlobStoreService.Module.INSTANCE.isActive());
			assertEquals(new ArrayList<>(), options.apply());
		} finally {
			ModuleUtil.INSTANCE.startUp(BlobStoreService.Module.INSTANCE);
		}
	}

	private static void restartService() throws ModuleException {
		ModuleUtil.INSTANCE.shutDown(BlobStoreService.Module.INSTANCE);
		ModuleUtil.INSTANCE.startUp(BlobStoreService.Module.INSTANCE);
	}

	private static CloseTrackingBlobStore.Config<?> closeTrackingConfig(String name, boolean failOnClose) {
		CloseTrackingBlobStore.Config<?> result =
			TypedConfiguration.newConfigItem(CloseTrackingBlobStore.Config.class);
		result.setName(name);
		result.setFailOnClose(failOnClose);
		return result;
	}

	private BlobStoreService newService(String defaultStore, String... storeNames) {
		return newService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, defaultStore, storeNames);
	}

	private BlobStoreService newService(InstantiationContext context, String defaultStore, String... storeNames) {
		BlobStoreService.Config<?> config = TypedConfiguration.newConfigItem(BlobStoreService.Config.class);
		config.setDefaultStore(defaultStore);
		for (String name : storeNames) {
			FileSystemBlobStore.Config<?> storeConfig =
				TypedConfiguration.newConfigItem(FileSystemBlobStore.Config.class);
			storeConfig.setName(name);
			storeConfig.setRoot(new File(_root, name).getPath());
			config.getStores().put(name, storeConfig);
		}
		return (BlobStoreService) context.getInstance(config);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestBlobStoreService.class, BlobStoreService.Module.INSTANCE));
	}

	/**
	 * {@link BlobStore} without content that records whether it has been closed.
	 */
	public static class CloseTrackingBlobStore extends AbstractBlobStore<CloseTrackingBlobStore.Config<?>> {

		/**
		 * Configuration of a {@link CloseTrackingBlobStore}.
		 */
		public interface Config<I extends CloseTrackingBlobStore> extends BlobStore.Config<I> {

			/**
			 * Whether {@link CloseTrackingBlobStore#close()} fails after recording the call.
			 */
			boolean isFailOnClose();

			/**
			 * @see #isFailOnClose()
			 */
			void setFailOnClose(boolean value);

			@Override
			@ClassDefault(CloseTrackingBlobStore.class)
			Class<? extends I> getImplementationClass();

		}

		private boolean _closed;

		/**
		 * Creates a {@link CloseTrackingBlobStore} from configuration.
		 */
		@CalledByReflection
		public CloseTrackingBlobStore(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		/**
		 * Whether {@link #close()} has been called.
		 */
		public boolean isClosed() {
			return _closed;
		}

		@Override
		public void close() throws IOException {
			_closed = true;
			if (getConfig().isFailOnClose()) {
				throw new IOException("Simulated failure closing " + getName() + ".");
			}
		}

		@Override
		public String put(InputStream content, long size, String contentType) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InputStream get(String key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InputStream get(String key, long offset, long length) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete(String key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Stream<BlobInfo> list() {
			return Stream.empty();
		}

	}

}
