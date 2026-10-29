/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.jenkins;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.factory.gitea.GiteaTestServer;
import test.com.top_logic.factory.gitea.GiteaTestServer.ExecResult;

import com.top_logic.basic.credential.BasicCredentials;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.factory.ci.CIBuild;
import com.top_logic.factory.ci.CIBuildStart;
import com.top_logic.factory.ci.CIBuildState;
import com.top_logic.factory.ci.CIConsole;
import com.top_logic.factory.ci.CIJob;
import com.top_logic.factory.ci.CIRoute;
import com.top_logic.factory.ci.I18NConstants;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.jenkins.JenkinsCIProvider;
import com.top_logic.util.error.TopLogicException;

/**
 * Integration test of {@link JenkinsCIProvider} against a Jenkins in a Docker container built from
 * the pinned image context, whose jobs clone from a Gitea next to it (see {@link JenkinsTestServer}):
 * creating and updating a job with its folders, a successful and a failing build of a pipeline from
 * the repository, the end of the console output, and rejected credentials.
 */
public class TestJenkinsCIProvider extends TestCase {

	private static final String OK_MARK = "tl-factory-ci-ok";

	private static final String FAILURE_MARK = "tl-factory-ci-failure";

	private static final Duration BUILD_TIMEOUT = Duration.ofMinutes(3);

	private static JenkinsTestServer _server;

	private JenkinsCIProvider _provider;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_provider = _server.provider(_server.token());
	}

	/**
	 * The job is created in its folders on the first call, its configuration replaced on the next:
	 * clone URL, branch, SCM credential, pipeline file and the escaped description.
	 */
	public void testEnsureJob() throws Exception {
		GitRepository repository = repository("ci-ensure");
		CIJob job = _provider.ensureJob(repository, "First <description> & \"quotes\"");
		String fullName = JenkinsCIProvider.DEFAULT_FOLDER + "/" + GiteaTestServer.ORG + "/ci-ensure";
		assertEquals(fullName, job.name());
		String jobPath = "job/" + JenkinsCIProvider.DEFAULT_FOLDER + "/job/" + GiteaTestServer.ORG + "/job/ci-ensure/";
		assertEquals("The page below the Jenkins URL.", _server.rootUrl() + jobPath, job.webUrl());
		assertEquals(job, _provider.findJob(fullName));
		assertNull(_provider.findJob(fullName + "-missing"));
		String jobUrl = _server.baseUrl() + jobPath;

		String config = get(jobUrl + JenkinsCIProvider.CONFIG_XML);
		assertTrue(config, config.contains(JenkinsTestServer.cloneUrl("ci-ensure")));
		assertTrue(config, config.contains(JenkinsTestServer.SCM_CREDENTIALS_ID));
		assertTrue(config, config.contains("*/" + GiteaTestServer.BRANCH));
		assertTrue(config, config.contains("<scriptPath>" + JenkinsCIProvider.DEFAULT_SCRIPT_PATH + "</scriptPath>"));
		assertTrue(config, config.contains("First &lt;description&gt; &amp; &quot;quotes&quot;"));
		assertNull("No build yet.", _provider.lastBuild(job.name()));

		CIJob again = _provider.ensureJob(repository, "Second");
		assertEquals(job, again);
		String updated = get(jobUrl + JenkinsCIProvider.CONFIG_XML);
		assertTrue(updated, updated.contains("<description>Second</description>"));
	}

	/**
	 * A build of a pipeline from the repository succeeds; its state, number and console output are
	 * reported, the output bounded to its last lines.
	 */
	public void testSuccessfulBuild() throws Exception {
		GitRepository repository = repository("ci-success");
		push("ci-success", "echo '" + OK_MARK + "'\nfor (int i = 0; i < 50; i++) { echo \"line ${i}\" }\n");
		CIJob job = _provider.ensureJob(repository, "Successful build");

		CIBuildStart start = _provider.startBuild(job.name());
		assertFalse("Waited for the build to leave the queue.", start.isQueued());
		assertEquals(Integer.valueOf(1), start.number());
		assertEquals(job.webUrl() + "1/", start.webUrl());

		CIBuild build = awaitBuild(job.name());
		assertEquals(console(job, 1), CIBuildState.SUCCESS, build.state());
		assertEquals(1, build.number());
		assertEquals(start.webUrl(), build.webUrl());
		assertTrue(build.startTime().isBefore(Instant.now()));

		CIConsole full = _provider.console(job.name(), 1, CIRoute.DEFAULT_MAX_LOG_SIZE);
		assertFalse(full.truncated());
		assertTrue(full.text(), full.text().contains(OK_MARK));
		assertTrue(full.text(), full.text().contains("Finished: SUCCESS"));
		assertTrue(full.text(), full.text().startsWith("Started by user"));
		assertFalse("No console notes.", full.text().contains("\u001B[8mha:"));

		// The bound applies to the raw output, which has the console notes of Jenkins in it.
		int max = 2000;
		CIConsole tail = _provider.console(job.name(), 1, max);
		assertTrue(tail.truncated());
		assertTrue(tail.text(), tail.text().length() <= max);
		assertTrue(tail.text(), tail.text().contains("line 49"));
		assertTrue(tail.text(), tail.text().strip().endsWith("Finished: SUCCESS"));
		assertFalse(tail.text(), tail.text().contains(OK_MARK));
		assertFalse("No console notes.", tail.text().contains("\u001B[8mha:"));
	}

	/**
	 * A failing pipeline ends with {@link CIBuildState#FAILURE}, its console has the error.
	 */
	public void testFailingBuild() throws Exception {
		GitRepository repository = repository("ci-failure");
		push("ci-failure", "error '" + FAILURE_MARK + "'\n");
		CIJob job = _provider.ensureJob(repository, null);
		_provider.startBuild(job.name());

		CIBuild build = awaitBuild(job.name());
		assertEquals(CIBuildState.FAILURE, build.state());
		CIConsole console = _provider.console(job.name(), build.number(), CIRoute.DEFAULT_MAX_LOG_SIZE);
		assertTrue(console.text(), console.text().contains(FAILURE_MARK));
		assertTrue(console.text(), console.text().contains("Finished: FAILURE"));
	}

	/**
	 * A job or build that does not exist is reported as such.
	 */
	public void testNotFound() {
		String missing = JenkinsCIProvider.DEFAULT_FOLDER + "/" + GiteaTestServer.ORG + "/missing";
		assertError(I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB.getKey(), () -> _provider.lastBuild(missing));
		assertError(I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB.getKey(), () -> _provider.startBuild(missing));
		CIJob job = _provider.ensureJob(repository("ci-no-build"), null);
		assertError(I18NConstants.ERROR_CI_BUILD_NOT_FOUND__JOB_BUILD.getKey(),
			() -> _provider.console(job.name(), 7, 1000));
	}

	/**
	 * A wrong API token is reported as rejected credentials.
	 */
	public void testWrongToken() {
		JenkinsCIProvider wrong = _server.provider("11" + "0".repeat(32));
		assertError(I18NConstants.ERROR_CI_AUTHENTICATION_FAILED__SERVICE.getKey(),
			() -> wrong.ensureJob(repository("ci-wrong-token"), null));
	}

	private static void assertError(String key, Runnable operation) {
		try {
			operation.run();
			fail("Expected error " + key);
		} catch (TopLogicException ex) {
			assertEquals(ex.getMessage(), key, ex.getErrorKey().plain().getKey());
		}
	}

	/**
	 * Waits until the last build of the job has ended.
	 */
	private CIBuild awaitBuild(String job) throws InterruptedException {
		long deadline = System.nanoTime() + BUILD_TIMEOUT.toNanos();
		while (true) {
			CIBuild build = _provider.lastBuild(job);
			if (build != null && build.state().isDone()) {
				return build;
			}
			assertTrue("Build did not end: " + build, System.nanoTime() < deadline);
			Thread.sleep(500);
		}
	}

	private String console(CIJob job, int number) {
		return _provider.console(job.name(), number, CIRoute.DEFAULT_MAX_LOG_SIZE).text();
	}

	/**
	 * Creates a repository in the Gitea; the result names it with the URL the Jenkins reaches it at.
	 */
	private static GitRepository repository(String name) {
		GitRepository created = _server.gitea().provider(_server.gitea().token()).createRepository(name, null);
		return new GitRepository(new RepositoryName(GiteaTestServer.ORG, name), JenkinsTestServer.cloneUrl(name),
			created.getWebUrl(), GiteaTestServer.BRANCH);
	}

	/**
	 * Pushes a Jenkinsfile with the given pipeline as the first commit of the default branch.
	 */
	private static void push(String repository, String pipeline) throws InterruptedException {
		GiteaTestServer gitea = _server.gitea();
		String remote = "http://" + GiteaTestServer.ADMIN + ":" + gitea.token() + "@localhost:"
			+ GiteaTestServer.GITEA_PORT + "/" + GiteaTestServer.ORG + "/" + repository + ".git";
		String dir = "/tmp/" + repository;
		ExecResult result = gitea.git("rm -rf " + dir + " && git init -q " + dir + " && cd " + dir
			+ " && git config user.email test@example.com && git config user.name Test"
			+ " && git symbolic-ref HEAD refs/heads/" + GiteaTestServer.BRANCH
			+ " && printf '%s' \"$0\" > " + JenkinsCIProvider.DEFAULT_SCRIPT_PATH
			+ " && git add . && git commit -qm pipeline && git push -q " + remote + " HEAD", pipeline);
		assertEquals(result.err() + result.out(), 0, result.exitCode());
	}

	private static String get(String url) throws Exception {
		try (HttpClient client = HttpClient.newHttpClient()) {
			HttpResponse<String> response = client.send(HttpRequest.newBuilder(URI.create(url))
				.header(CredentialProvider.AUTHORIZATION,
					BasicCredentials.basicAuthorization(JenkinsTestServer.ADMIN, _server.token()))
				.build(), HttpResponse.BodyHandlers.ofString());
			assertEquals(response.body(), 200, response.statusCode());
			return response.body();
		}
	}

	/**
	 * The tests of this class around a Jenkins and a Gitea in Docker containers.
	 */
	public static Test suite() {
		return JenkinsTestServer.suite(TestJenkinsCIProvider.class, server -> _server = server);
	}

}
