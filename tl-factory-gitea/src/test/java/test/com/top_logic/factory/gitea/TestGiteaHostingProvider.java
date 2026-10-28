/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.gitea;

import java.io.IOException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.factory.gitea.GiteaTestServer.ExecResult;

import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.git.GitCapabilities;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.gitea.GiteaHostingProvider;
import com.top_logic.factory.gitea.GiteaProviderType;
import com.top_logic.factory.gitea.client.api.OrganizationApi;
import com.top_logic.factory.gitea.client.api.RepositoryApi;
import com.top_logic.factory.gitea.client.model.AddCollaboratorOption;
import com.top_logic.factory.gitea.client.model.BranchProtection;
import com.top_logic.factory.gitea.client.model.Organization;
import com.top_logic.util.error.TopLogicException;

/**
 * Integration test of {@link GiteaHostingProvider} against a Gitea started in a Docker container.
 *
 * <p>
 * The server is a {@link GiteaTestServer}; without Docker or image, the suite contains no tests.
 * </p>
 */
public class TestGiteaHostingProvider extends TestCase {

	private static final String LOOPBACK = GiteaTestServer.LOOPBACK;

	private static final int GITEA_PORT = GiteaTestServer.GITEA_PORT;

	private static final String ADMIN = GiteaTestServer.ADMIN;

	private static final String ORG = GiteaTestServer.ORG;

	private static final String BRANCH = GiteaTestServer.BRANCH;

	private static final List<String> PROTECTED = List.of("Jenkinsfile", "deploy/**");

	private static final String PLAIN_USER = "plain-user";

	private static final String OTHER_USER = "other-user";

	private static GiteaTestServer _server;

	/**
	 * A created repository has the clone URL, web URL and default branch of the server, and is
	 * found by both.
	 */
	public void testCreate() {
		GiteaHostingProvider provider = provider(_server.token());
		assertEquals(ORG, provider.getOwner());

		GitRepository created = provider.createRepository("create", "A test repository");
		assertEquals(ORG + "/create", created.getFullName());
		assertEquals(_server.baseUrl() + ORG + "/create.git", created.getCloneUrl());
		assertEquals(_server.baseUrl() + ORG + "/create", created.getWebUrl());
		assertEquals(BRANCH, created.getDefaultBranch());

		assertEquals(created, provider.findRepository(created.getCloneUrl()));
		assertEquals(created, provider.findRepository(created.getWebUrl()));
		assertEquals(created, provider.findRepository(ORG + "/create"));
	}

	/**
	 * A second repository of the same name is rejected.
	 */
	public void testDuplicate() throws Exception {
		GiteaHostingProvider provider = provider(_server.token());
		provider.createRepository("duplicate", null);
		assertErrorKey(I18NConstants.ERROR_REPOSITORY_EXISTS__REPOSITORY.fill(ORG + "/duplicate"),
			() -> provider.createRepository("duplicate", null));
	}

	/**
	 * A name Gitea does not accept is reported as such.
	 */
	public void testInvalidName() throws Exception {
		GiteaHostingProvider provider = provider(_server.token());
		assertErrorKey(I18NConstants.ERROR_INVALID_REPOSITORY_NAME__NAME.fill("in valid"),
			() -> provider.createRepository("in valid", null));
	}

	/**
	 * References to repositories that do not exist or are not on the server find nothing.
	 */
	public void testNotFound() {
		GiteaHostingProvider provider = provider(_server.token());
		assertNull(provider.findRepository(ORG + "/missing"));
		assertNull(provider.findRepository(_server.baseUrl() + "nobody/missing.git"));
		assertNull(provider.findRepository("https://elsewhere.example.com/" + ORG + "/missing.git"));
		assertNull(provider.findRepository("missing"));
	}

	/**
	 * The branch protection read back from Gitea, also after it is replaced.
	 */
	public void testProtectBranch() throws Exception {
		GiteaHostingProvider provider = provider(_server.token());
		GitRepository repository = provider.createRepository("protect", null);

		provider.protectBranch(repository, BRANCH, List.of("Jenkinsfile"));
		provider.protectBranch(repository, BRANCH, PROTECTED);

		BranchProtection protection =
			new RepositoryApi(_server.client(_server.token())).repoGetBranchProtection(ORG, "protect", BRANCH);
		assertEquals(BRANCH, protection.getRuleName());
		assertEquals(Boolean.TRUE, protection.getEnablePush());
		assertEquals(Boolean.FALSE, protection.getEnablePushWhitelist());
		assertEquals(Boolean.FALSE, protection.getEnableForcePush());
		assertEquals(Boolean.FALSE, protection.getBlockAdminMergeOverride());
		assertEquals("Jenkinsfile;deploy/**", protection.getProtectedFilePatterns());
		assertEquals(Long.valueOf(1), protection.getRequiredApprovals());
		assertEquals(Boolean.TRUE, protection.getBlockOnRejectedReviews());
	}

	/**
	 * What the protection of an existing branch enforces on pushes: a push adding commits is
	 * accepted, a push changing a protected file or rewriting the history is rejected - here for the
	 * token of an organization owner and site administrator.
	 */
	public void testProtectionEnforced() throws Exception {
		GiteaHostingProvider provider = provider(_server.token());
		GitRepository repository = provider.createRepository("enforced", null);

		String remote = "http://" + ADMIN + ":" + _server.token() + "@localhost:" + GITEA_PORT + "/" + ORG
			+ "/enforced.git";
		assertSucceeded("Clone.", _server.git("rm -rf /tmp/enforced && git clone -q " + remote + " /tmp/enforced && cd /tmp/enforced"
			+ " && git config user.email test@example.com && git config user.name Test"
			+ " && git symbolic-ref HEAD refs/heads/" + BRANCH
			+ " && echo one > README.md && git add README.md && git commit -qm one"));
		assertSucceeded("First push.", _server.git(push(false)));
		// Gitea rejects the push creating a branch whose rule has protected file patterns.
		provider.protectBranch(repository, BRANCH, PROTECTED);

		assertSucceeded("Commit.", _server.git(inClone("echo two > README.md && git commit -qam two")));
		assertSucceeded("Push of a commit.", _server.git(push(false)));

		_server.git(inClone("echo pipeline > Jenkinsfile && git add Jenkinsfile && git commit -qm jenkins"));
		ExecResult jenkins = _server.git(push(false));
		assertTrue("Push changing Jenkinsfile accepted.", jenkins.exitCode() != 0);
		assertTrue(jenkins.err(), jenkins.err().contains("protected from changing file"));

		_server.git(inClone("git reset -q --hard HEAD~1 && mkdir -p deploy/k8s && echo x > deploy/k8s/app.yaml"
			+ " && git add deploy && git commit -qm deploy"));
		ExecResult deploy = _server.git(push(false));
		assertTrue("Push changing deploy/ accepted.", deploy.exitCode() != 0);

		_server.git(inClone("git reset -q --hard HEAD~2 && echo three > README.md && git commit -qam three"));
		ExecResult force = _server.git(push(true));
		assertTrue("Force push accepted.", force.exitCode() != 0);
		assertTrue(force.err(), force.err().contains("force push"));
	}

	/**
	 * The credentials of the type send the token from the named secret, here
	 * {@link GiteaProviderType#TOKEN_SECRET}.
	 */
	public void testDefaultCredentials() {
		String before = System.getProperty(GiteaProviderType.TOKEN_SECRET);
		System.setProperty(GiteaProviderType.TOKEN_SECRET, _server.token());
		try {
			GiteaHostingProvider provider = provider(null);
			GitRepository created = provider.createRepository("default-credentials", null);
			assertEquals(created, provider.findRepository(created.getFullName()));
		} finally {
			if (before == null) {
				System.clearProperty(GiteaProviderType.TOKEN_SECRET);
			} else {
				System.setProperty(GiteaProviderType.TOKEN_SECRET, before);
			}
		}
	}

	/**
	 * A token Gitea does not know is reported as rejected credentials.
	 */
	public void testWrongToken() throws Exception {
		GiteaHostingProvider provider = provider("not-a-token");
		ResKey rejected = I18NConstants.ERROR_AUTHENTICATION_FAILED__SERVICE.fill(_server.baseUrl());
		assertErrorKey(rejected, () -> provider.createRepository("wrong-token", null));
		assertErrorKey(rejected, () -> provider.findRepository(ORG + "/wrong-token"));
	}

	/**
	 * A token without the scopes for creating repositories is reported as missing permission.
	 */
	public void testMissingPermission() throws Exception {
		GiteaHostingProvider provider = provider(_server.readToken());
		assertErrorKey(I18NConstants.ERROR_PERMISSION_DENIED__REPOSITORY.fill(ORG + "/read-only"),
			() -> provider.createRepository("read-only", null));
	}

	/**
	 * The token of the administrator, who created the organization, may create and administer its
	 * repositories.
	 */
	public void testCapabilitiesOfAdministrator() {
		assertEquals(GitCapabilities.ALL, provider(_server.token()).capabilities());
		GitRepository repository = provider(_server.token()).createRepository("administered", null);
		assertTrue(repository.canAdminister());
	}

	/**
	 * The token of a user who is no member of the organization may do nothing there, everything in
	 * the namespace of the user; a repository of the organization the user may only write to is
	 * reported as not administrable, and protecting its branch is refused.
	 */
	public void testCapabilitiesOfPlainUser() throws Exception {
		String token = _server.createUser(PLAIN_USER);
		assertEquals(GitCapabilities.NONE, provider(token).capabilities());
		assertEquals(GitCapabilities.ALL, _server.provider(PLAIN_USER, token, false).capabilities());

		GitRepository shared = provider(_server.token()).createRepository("shared", null);
		new RepositoryApi(_server.client(_server.token())).repoAddCollaborator(ORG, "shared", PLAIN_USER,
			new AddCollaboratorOption().permission(AddCollaboratorOption.PermissionEnum.WRITE));
		GitRepository seen = provider(token).findRepository(shared.getFullName());
		assertNotNull("The collaborator reads the repository.", seen);
		assertFalse("A collaborator with write access does not administer it.", seen.canAdminister());
		assertErrorKey(I18NConstants.ERROR_PERMISSION_DENIED__REPOSITORY.fill(shared.getFullName()),
			() -> provider(token).protectBranch(seen, BRANCH, PROTECTED));

		GitRepository own = _server.provider(PLAIN_USER, token, false).createRepository("own", null);
		assertEquals("In the namespace of the user.", PLAIN_USER + "/own", own.getFullName());
		assertTrue(own.canAdminister());
	}

	/**
	 * A token without the scope read:user cannot tell its rights.
	 */
	public void testCapabilitiesWithoutUserScope() {
		GitCapabilities capabilities = provider(_server.readToken()).capabilities();
		assertFalse(capabilities.createRepositories());
		assertNotNull(capabilities.problem());
		assertEquals(I18NConstants.ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM.getKey(),
			capabilities.problem().plain().getKey());
	}

	/**
	 * A provider creating its owner creates a private organization of the user of the token with
	 * the first repository, then the repository in it; before, the owner counts as fully
	 * accessible.
	 */
	public void testOrganizationCreated() throws Exception {
		String owner = ORG + "-tenant-1";
		GiteaHostingProvider provider = _server.provider(owner, _server.token(), true);
		assertEquals(GitCapabilities.ALL, provider.capabilities());
		assertErrorKey(I18NConstants.ERROR_OWNER_NOT_FOUND__OWNER.fill(owner),
			() -> _server.provider(owner, _server.token(), false).createRepository("not-yet", null));

		GitRepository created = provider.createRepository("app", null);
		assertEquals(owner + "/app", created.getFullName());
		assertTrue(created.canAdminister());
		Organization organization = new OrganizationApi(_server.client(_server.token())).orgGet(owner);
		assertEquals(Organization.VisibilityEnum.PRIVATE, organization.getVisibility());
		assertEquals(GitCapabilities.ALL, provider.capabilities());
		assertEquals("The organization is used again.", owner + "/second",
			provider.createRepository("second", null).getFullName());

		String plain = _server.createUser(OTHER_USER);
		assertNull("Another user does not see the repositories of the organization.",
			provider(plain).findRepository(created.getFullName()));
		assertErrorKey(I18NConstants.ERROR_PERMISSION_DENIED__REPOSITORY.fill(owner + "/intruder"),
			() -> _server.provider(owner, plain, true).createRepository("intruder", null));
	}

	/**
	 * A server that does not answer is reported as unreachable.
	 */
	public void testUnreachable() throws Exception {
		int port = freePort();
		GiteaHostingProvider provider = provider("http://" + LOOPBACK + ":" + port + "/", _server.token());
		try {
			provider.createRepository("unreachable", null);
			fail("Server without listener reached.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_SERVICE_UNREACHABLE__SERVICE_PROBLEM.getKey(),
				ex.getErrorKey().plain().getKey());
		}
	}

	private static void assertSucceeded(String message, ExecResult result) {
		assertEquals(message + " " + result.err() + result.out(), 0, result.exitCode());
	}

	private static String push(boolean force) {
		return inClone("git push" + (force ? " --force" : "") + " origin " + BRANCH);
	}

	private static String inClone(String command) {
		return "cd /tmp/enforced && " + command;
	}

	private static GiteaHostingProvider provider(String token) {
		return _server.provider(token);
	}

	private static GiteaHostingProvider provider(String baseUrl, String token) {
		return GiteaTestServer.provider(baseUrl, token);
	}

	private static void assertErrorKey(ResKey expected, Operation operation) throws Exception {
		try {
			operation.run();
			fail("Expected failure " + expected + ".");
		} catch (TopLogicException ex) {
			assertEquals(expected, ex.getErrorKey());
		}
	}

	private static int freePort() throws IOException {
		return GiteaTestServer.freePort();
	}

	/**
	 * The tests of this class around a Gitea server, none if no Docker daemon is reachable or no
	 * image is given.
	 */
	public static Test suite() {
		return GiteaTestServer.suite(TestGiteaHostingProvider.class, server -> _server = server);
	}

	/**
	 * An operation expected to fail.
	 */
	private interface Operation {

		void run() throws Exception;

	}

}
