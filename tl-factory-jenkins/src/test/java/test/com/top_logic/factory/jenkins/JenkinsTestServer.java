/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.jenkins;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.gitea.GiteaTestServer;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.BasicCredentials;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.factory.jenkins.JenkinsCIProvider;

/**
 * A Jenkins server in a Docker container for the integration tests, next to a {@link GiteaTestServer
 * Gitea} on a Docker network of their own, from which the jobs clone.
 *
 * <p>
 * The image is built from the image context named by the system property {@link #CONTEXT_PROPERTY}
 * (the build sets it to {@code src/test/docker/jenkins}), which pins the Jenkins release and its
 * plugins. The container gets the administrator {@link #ADMIN} with a random API token and the
 * Jenkins credential {@link #SCM_CREDENTIALS_ID} with the read token of the Gitea. The Gitea is
 * reached from the Jenkins container at {@link #GITEA_HOST}. If no Docker daemon answers or the
 * image context is not given, {@link #suite(Class, Consumer)} contains no tests.
 * </p>
 */
public final class JenkinsTestServer implements AutoCloseable {

	/**
	 * System property naming the directory of the image context of the Jenkins.
	 */
	public static final String CONTEXT_PROPERTY = "tl.factory.jenkins.context";

	/**
	 * Port of the web interface and the API inside the container.
	 */
	public static final int JENKINS_PORT = 8080;

	/**
	 * The administrator of the server.
	 */
	public static final String ADMIN = "factory-admin";

	/**
	 * The ID of the Jenkins credential with read access to the Gitea.
	 */
	public static final String SCM_CREDENTIALS_ID = "tl-factory-test-gitea";

	/**
	 * The host name of the Gitea in the network of the containers.
	 */
	public static final String GITEA_HOST = "gitea";

	/**
	 * The tag of the built image.
	 */
	public static final String IMAGE = "tl-factory-jenkins-test:latest";

	private static final String ADMIN_PASSWORD = "factory-admin-password";

	/**
	 * Version prefix of an API token of Jenkins, followed by 32 hex digits.
	 */
	private static final String TOKEN_VERSION = "11";

	private static final int TOKEN_BYTES = 16;

	/**
	 * Label marking the containers and networks of the tests.
	 */
	private static final String LABEL = "test.tl-factory-jenkins";

	private static final Duration STARTUP = Duration.ofSeconds(180);

	private final DockerClient _docker;

	private final String _network;

	private final GiteaTestServer _gitea;

	private final String _containerId;

	private final String _baseUrl;

	private final String _token;

	private JenkinsTestServer(DockerClient docker, String network, GiteaTestServer gitea, String containerId,
			String baseUrl, String token) {
		_docker = docker;
		_network = network;
		_gitea = gitea;
		_containerId = containerId;
		_baseUrl = baseUrl;
		_token = token;
	}

	/**
	 * The tests of the given class around a Jenkins and a Gitea server, none if no Docker daemon is
	 * reachable or no image context is given.
	 *
	 * @param tests
	 *        The test class.
	 * @param server
	 *        Gets the started server before the tests, {@code null} after them.
	 */
	public static Test suite(Class<? extends TestCase> tests, Consumer<JenkinsTestServer> server) {
		String context = System.getProperty(CONTEXT_PROPERTY);
		if (context == null || context.isEmpty()) {
			System.err.println("Skipping " + tests.getName() + ": no Jenkins image context in " + CONTEXT_PROPERTY + ".");
			return new TestSuite(tests.getName());
		}
		if (!GiteaTestServer.dockerAvailable()) {
			System.err.println("Skipping " + tests.getName() + ": no Docker daemon reachable at "
				+ GiteaTestServer.dockerHost() + ".");
			return new TestSuite(tests.getName());
		}
		Test setup = new TestSetup(new TestSuite(tests)) {
			private JenkinsTestServer _started;

			@Override
			protected void setUp() throws Exception {
				_started = start(new File(context));
				server.accept(_started);
			}

			@Override
			protected void tearDown() throws Exception {
				server.accept(null);
				if (_started != null) {
					_started.close();
					_started = null;
				}
			}
		};
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(setup, TypeIndex.Module.INSTANCE, SecretStoreService.Module.INSTANCE));
	}

	/**
	 * Builds the image from the given context and starts a Jenkins of it next to a Gitea.
	 */
	public static JenkinsTestServer start(File context) throws Exception {
		DockerClient docker = GiteaTestServer.createClient();
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		String network = "tl-factory-jenkins-test-" + suffix;
		docker.createNetworkCmd().withName(network).withLabels(Map.of(LABEL, "true")).exec();
		GiteaTestServer gitea = null;
		String containerId = null;
		try {
			gitea = GiteaTestServer.start(GiteaTestServer.pinnedImage(), network, GITEA_HOST);
			docker.buildImageCmd(context).withTags(Set.of(IMAGE)).exec(new BuildImageResultCallback()).awaitImageId();

			byte[] secret = new byte[TOKEN_BYTES];
			new SecureRandom().nextBytes(secret);
			String token = TOKEN_VERSION + HexFormat.of().formatHex(secret);
			int port = GiteaTestServer.freePort();
			String baseUrl = "http://" + GiteaTestServer.LOOPBACK + ":" + port + "/";
			ExposedPort http = ExposedPort.tcp(JENKINS_PORT);
			containerId = docker.createContainerCmd(IMAGE)
				.withName("tl-factory-jenkins-test-" + suffix)
				.withLabels(Map.of(LABEL, "true"))
				.withEnv(
					"TL_TEST_ADMIN=" + ADMIN,
					"TL_TEST_ADMIN_PASSWORD=" + ADMIN_PASSWORD,
					"TL_TEST_API_TOKEN=" + token,
					"TL_TEST_SCM_CREDENTIALS_ID=" + SCM_CREDENTIALS_ID,
					"TL_TEST_SCM_USER=" + GiteaTestServer.ADMIN,
					"TL_TEST_SCM_PASSWORD=" + gitea.readToken())
				.withExposedPorts(http)
				.withHostConfig(HostConfig.newHostConfig()
					.withNetworkMode(network)
					.withPortBindings(new PortBinding(Ports.Binding.bindIpAndPort(GiteaTestServer.LOOPBACK, port), http)))
				.exec()
				.getId();
			JenkinsTestServer server = new JenkinsTestServer(docker, network, gitea, containerId, baseUrl, token);
			try {
				docker.startContainerCmd(containerId).exec();
				server.awaitStarted();
				return server;
			} catch (Exception | Error ex) {
				server.close();
				throw ex;
			}
		} catch (Exception | Error ex) {
			if (containerId == null) {
				if (gitea != null) {
					gitea.close();
				}
				removeNetwork(docker, network);
				docker.close();
			}
			throw ex;
		}
	}

	private void awaitStarted() throws Exception {
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
		HttpRequest request = HttpRequest.newBuilder(URI.create(_baseUrl + JenkinsCIProvider.API_JSON))
			.header(CredentialProvider.AUTHORIZATION, BasicCredentials.basicAuthorization(ADMIN, _token))
			.timeout(Duration.ofSeconds(5)).build();
		long deadline = System.nanoTime() + STARTUP.toNanos();
		while (true) {
			try {
				if (http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200) {
					return;
				}
			} catch (IOException ex) {
				// Not yet listening.
			}
			if (System.nanoTime() > deadline) {
				fail("Jenkins did not start within " + STARTUP + ".");
			}
			Thread.sleep(500);
		}
	}

	/**
	 * The URL of the server as reached from the host, with a slash at its end.
	 */
	public String baseUrl() {
		return _baseUrl;
	}

	/**
	 * The API token of {@link #ADMIN}.
	 */
	public String token() {
		return _token;
	}

	/**
	 * The Gitea the jobs clone from.
	 */
	public GiteaTestServer gitea() {
		return _gitea;
	}

	/**
	 * The URL the Jenkins clones the given repository of the Gitea organization from.
	 */
	public static String cloneUrl(String repository) {
		return "http://" + GITEA_HOST + ":" + GiteaTestServer.GITEA_PORT + "/" + GiteaTestServer.ORG + "/" + repository
			+ ".git";
	}

	/**
	 * A provider of this server authenticated as {@link #ADMIN} with the given API token, checking
	 * out with {@link #SCM_CREDENTIALS_ID}.
	 */
	public JenkinsCIProvider provider(String token) {
		JenkinsCIProvider.Config<?> config = TypedConfiguration.newConfigItem(JenkinsCIProvider.Config.class);
		TypedConfigUtil.setProperty(config, JenkinsCIProvider.Config.BASE_URL, _baseUrl);
		TypedConfigUtil.setProperty(config, JenkinsCIProvider.Config.SCM_CREDENTIALS_ID, SCM_CREDENTIALS_ID);
		TypedConfigUtil.setProperty(config, JenkinsCIProvider.Config.TIMEOUT, 20000L);
		TypedConfigUtil.setProperty(config, JenkinsCIProvider.Config.QUEUE_WAIT, 30000L);
		TypedConfigUtil.setProperty(config, JenkinsCIProvider.Config.CREDENTIALS, credentials(token));
		return (JenkinsCIProvider) TypedConfigUtil.createInstance(config);
	}

	private static PolymorphicConfiguration<? extends CredentialProvider> credentials(String token) {
		LiteralSecretSource.Config secret = TypedConfiguration.newConfigItem(LiteralSecretSource.Config.class);
		secret.setValue(token);
		BasicCredentials.Config basic = TypedConfiguration.newConfigItem(BasicCredentials.Config.class);
		basic.setUser(ADMIN);
		basic.setPassword(secret);
		return basic;
	}

	@Override
	public void close() throws IOException {
		try {
			try {
				_docker.removeContainerCmd(_containerId).withForce(true).withRemoveVolumes(true).exec();
			} catch (NotFoundException ex) {
				// Already gone.
			}
			_gitea.close();
			removeNetwork(_docker, _network);
		} finally {
			_docker.close();
		}
	}

	private static void removeNetwork(DockerClient docker, String network) {
		try {
			docker.removeNetworkCmd(network).exec();
		} catch (NotFoundException ex) {
			// Already gone.
		}
	}

}
