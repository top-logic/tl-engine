/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.azure;

import java.util.concurrent.atomic.AtomicInteger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.SimpleTestFactory;

import com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

/**
 * Starts the Azure storage emulator Azurite in a Docker container for a test suite.
 *
 * <p>
 * Without a Docker environment, the suite is replaced by a single successful test that reports the
 * skip.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AzuriteSetup extends TestSetup {

	/**
	 * The Azurite image the tests run against.
	 */
	private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite:3.37.0";

	/**
	 * Port of the blob service inside the container.
	 */
	private static final int BLOB_PORT = 10000;

	/**
	 * Name of the well-known development account of Azurite.
	 */
	public static final String ACCOUNT_NAME = "devstoreaccount1";

	/**
	 * Key of the well-known development account of Azurite.
	 */
	public static final String ACCOUNT_KEY =
		"Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

	private static final AtomicInteger CONTAINER_COUNTER = new AtomicInteger();

	private static GenericContainer<?> _azurite;

	private static BlobServiceClient _admin;

	private AzuriteSetup(Test test) {
		super(test);
	}

	/**
	 * The URL of the blob service of the development account, including the account name as path.
	 */
	public static String endpoint() {
		return "http://" + _azurite.getHost() + ":" + _azurite.getMappedPort(BLOB_PORT) + "/" + ACCOUNT_NAME;
	}

	/**
	 * The connection string of the development account.
	 */
	public static String connectionString() {
		return "DefaultEndpointsProtocol=http;AccountName=" + ACCOUNT_NAME + ";AccountKey=" + ACCOUNT_KEY
			+ ";BlobEndpoint=" + endpoint() + ";";
	}

	/**
	 * Client of the blob service with full access, for preparing and checking the storage.
	 */
	public static BlobServiceClient admin() {
		return _admin;
	}

	/**
	 * Creates a new empty container with a unique name.
	 */
	public static BlobContainerClient createContainer() {
		String name = "test-" + CONTAINER_COUNTER.incrementAndGet() + "-" + System.currentTimeMillis();
		return _admin.createBlobContainer(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_azurite = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
			.withCommand("azurite-blob", "--blobHost", "0.0.0.0", "--blobPort", String.valueOf(BLOB_PORT),
				"--skipApiVersionCheck", "--loose")
			.withExposedPorts(BLOB_PORT);
		_azurite.start();
		_admin = new BlobServiceClientBuilder()
			.httpClient(new JdkHttpClientBuilder().build())
			.connectionString(connectionString())
			.buildClient();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			_admin = null;
			if (_azurite != null) {
				_azurite.stop();
			}
			_azurite = null;
		} finally {
			super.tearDown();
		}
	}

	/**
	 * The suite of the given test class running against Azurite, or a skipped suite if no Docker
	 * environment is available.
	 */
	public static Test suite(Class<? extends junit.framework.TestCase> testClass) {
		if (!DockerClientFactory.instance().isDockerAvailable()) {
			TestSuite skipped = new TestSuite(testClass.getName());
			skipped.addTest(SimpleTestFactory.newSuccessfulTest("Skipped: Docker is not available."));
			return skipped;
		}
		return ModuleTestSetup.setupModule(new AzuriteSetup(new TestSuite(testClass)));
	}

}
