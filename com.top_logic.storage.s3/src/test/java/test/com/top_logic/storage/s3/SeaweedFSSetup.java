/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.s3;

import java.net.URI;
import java.time.Duration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.SimpleTestFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Starts the S3-compatible object storage SeaweedFS in a Docker container for a test suite.
 *
 * <p>
 * The S3 gateway requires signed requests by a single identity with full access, see
 * {@link #ACCESS_KEY} and {@link #SECRET_KEY}. The server encrypts with SSE-S3 using the key
 * encryption key {@link #SSE_KEK} and with SSE-KMS using keys of its local key management service,
 * which creates a requested key on first use.
 * </p>
 *
 * <p>
 * Without a Docker environment, the suite is replaced by a single successful test that reports the
 * skip.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SeaweedFSSetup extends TestSetup {

	/**
	 * The SeaweedFS image the tests run against.
	 */
	private static final String SEAWEEDFS_IMAGE = "chrislusf/seaweedfs:4.48";

	/**
	 * Port of the S3 gateway inside the container.
	 */
	private static final int S3_PORT = 8333;

	/**
	 * Location of the S3 configuration inside the container.
	 */
	private static final String S3_CONFIG_PATH = "/etc/seaweedfs/s3.json";

	/**
	 * Environment variable of SeaweedFS defining the hex encoded 256 bit key encryption key for
	 * SSE-S3.
	 */
	private static final String SSE_KEK_VARIABLE = "WEED_S3_SSE_KEK";

	/**
	 * Key encryption key of the server for SSE-S3.
	 */
	private static final String SSE_KEK = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f";

	/**
	 * Maximum size of a volume of the server in MiB.
	 *
	 * <p>
	 * Each bucket is a collection of volumes of its own. Small volumes allow many buckets on a
	 * small disk.
	 * </p>
	 */
	private static final int VOLUME_SIZE_LIMIT_MB = 64;

	/**
	 * Region the clients sign their requests for.
	 */
	public static final Region REGION = Region.US_EAST_1;

	/**
	 * Access key of the identity of the S3 gateway.
	 */
	public static final String ACCESS_KEY = "test-access-key";

	/**
	 * Secret key of the identity of the S3 gateway.
	 */
	public static final String SECRET_KEY = "test-secret-key";

	/**
	 * The S3 configuration: the identity with full access and the local key management service.
	 */
	private static final String S3_CONFIG = """
		{
		  "identities": [
		    {
		      "name": "test",
		      "credentials": [{"accessKey": "%s", "secretKey": "%s"}],
		      "actions": ["Admin", "Read", "Write", "List", "Tagging"]
		    }
		  ],
		  "kms": {
		    "default_provider": "local",
		    "providers": {
		      "local": {"type": "local", "enableOnDemandCreate": true}
		    }
		  }
		}
		""".formatted(ACCESS_KEY, SECRET_KEY);

	private static GenericContainer<?> _seaweedfs;

	private static S3Client _admin;

	private SeaweedFSSetup(Test test) {
		super(test);
	}

	/**
	 * The URL of the S3 gateway.
	 */
	public static String endpoint() {
		return "http://" + _seaweedfs.getHost() + ":" + _seaweedfs.getMappedPort(S3_PORT);
	}

	/**
	 * Client of the S3 gateway with full access, for preparing and checking the storage.
	 */
	public static S3Client admin() {
		return _admin;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_seaweedfs = new GenericContainer<>(DockerImageName.parse(SEAWEEDFS_IMAGE))
			.withCommand("server", "-s3", "-s3.port=" + S3_PORT, "-s3.config=" + S3_CONFIG_PATH,
				"-master.volumeSizeLimitMB=" + VOLUME_SIZE_LIMIT_MB, "-volume.max=0")
			.withEnv(SSE_KEK_VARIABLE, SSE_KEK)
			.withCopyToContainer(Transferable.of(S3_CONFIG), S3_CONFIG_PATH)
			.withExposedPorts(S3_PORT)
			// Without signature, the gateway rejects any request; it answers at all once it is ready.
			.waitingFor(Wait.forHttp("/").forPort(S3_PORT).forStatusCodeMatching(status -> status < 500)
				.withStartupTimeout(Duration.ofMinutes(2)));
		_seaweedfs.start();
		_admin = S3Client.builder()
			.httpClientBuilder(UrlConnectionHttpClient.builder())
			.endpointOverride(URI.create(endpoint()))
			.region(REGION)
			.forcePathStyle(Boolean.TRUE)
			// The gateway rejects some uploads with the trailing checksums the SDK sends by default.
			.requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
			.responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
			.credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
			.build();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (_admin != null) {
				_admin.close();
			}
		} finally {
			_admin = null;
			if (_seaweedfs != null) {
				_seaweedfs.stop();
			}
			_seaweedfs = null;
			super.tearDown();
		}
	}

	/**
	 * The suite of the given test class running against SeaweedFS, or a skipped suite if no Docker
	 * environment is available.
	 */
	public static Test suite(Class<? extends TestCase> testClass) {
		if (!DockerClientFactory.instance().isDockerAvailable()) {
			TestSuite skipped = new TestSuite(testClass.getName());
			skipped.addTest(SimpleTestFactory.newSuccessfulTest("Skipped: Docker is not available."));
			return skipped;
		}
		return ModuleTestSetup.setupModule(new SeaweedFSSetup(new TestSuite(testClass)));
	}

}
