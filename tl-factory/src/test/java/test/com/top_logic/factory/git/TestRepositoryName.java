/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.git;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of {@link RepositoryName#parse(String, String)} and of the
 * {@link RecordingGitHostingProvider} built on it.
 */
public class TestRepositoryName extends TestCase {

	private static final String BASE = "https://git.example.com/";

	private static final RepositoryName APP = new RepositoryName("my-org", "my-app");

	/**
	 * The full name, with and without {@link RepositoryName#GIT_SUFFIX}.
	 */
	public void testFullName() {
		assertEquals(APP, RepositoryName.parse("my-org/my-app", BASE));
		assertEquals(APP, RepositoryName.parse("  my-org/my-app.git ", BASE));
		assertEquals("my-org/my-app", APP.getFullName());
	}

	/**
	 * Clone and page URLs over HTTP(S), regardless of scheme, port and case of the host.
	 */
	public void testHttpUrls() {
		assertEquals(APP, RepositoryName.parse("https://git.example.com/my-org/my-app.git", BASE));
		assertEquals(APP, RepositoryName.parse("https://git.example.com/my-org/my-app", BASE));
		assertEquals(APP, RepositoryName.parse("https://git.example.com/my-org/my-app/", BASE));
		assertEquals(APP, RepositoryName.parse("http://GIT.example.com:3000/my-org/my-app.git", BASE));
		assertEquals(APP, RepositoryName.parse("https://git.example.com/my-org/my-app/src/branch/main", BASE));
	}

	/**
	 * A hosting service served below a path: the path is part of every URL.
	 */
	public void testBasePath() {
		String base = "https://example.com/git";
		assertEquals(APP, RepositoryName.parse("https://example.com/git/my-org/my-app.git", base));
		assertNull(RepositoryName.parse("https://example.com/my-org/my-app.git", base));
		assertNull(RepositoryName.parse("https://example.com/gitx/my-org/my-app.git", base));
	}

	/**
	 * SSH clone URLs, as URL and in the SCP-like form.
	 */
	public void testSshUrls() {
		assertEquals(APP, RepositoryName.parse("ssh://git@git.example.com:2222/my-org/my-app.git", BASE));
		assertEquals(APP, RepositoryName.parse("git@git.example.com:my-org/my-app.git", BASE));
	}

	/**
	 * References to another host or without owner and name.
	 */
	public void testNoRepository() {
		assertNull(RepositoryName.parse(null, BASE));
		assertNull(RepositoryName.parse(" ", BASE));
		assertNull(RepositoryName.parse("my-app", BASE));
		assertNull(RepositoryName.parse("https://github.com/my-org/my-app.git", BASE));
		assertNull(RepositoryName.parse("git@github.com:my-org/my-app.git", BASE));
		assertNull(RepositoryName.parse("https://git.example.com/my-org", BASE));
		assertNull(RepositoryName.parse("ftp://git.example.com/my-org/my-app", BASE));
		assertNull(RepositoryName.parse("my-org/my-app/extra", BASE));
	}

	/**
	 * The test double keeps the contract of the provider.
	 */
	public void testRecordingProvider() {
		RecordingGitHostingProvider provider = new RecordingGitHostingProvider();
		GitRepository created = provider.createRepository("app", "An app");
		assertEquals(RecordingGitHostingProvider.DEFAULT_OWNER + "/app", created.getFullName());
		assertEquals(created, provider.findRepository(created.getCloneUrl()));
		assertEquals(created, provider.findRepository(created.getFullName()));
		assertNull(provider.findRepository("other/app"));

		try {
			provider.createRepository("app", null);
			fail("Duplicate repository created.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_REPOSITORY_EXISTS__REPOSITORY.fill(created.getFullName()),
				ex.getErrorKey());
		}

		List<String> patterns = List.of("Jenkinsfile", "deploy/**");
		provider.protectBranch(created, created.getDefaultBranch(), patterns);
		assertEquals(patterns, provider.protection(created, created.getDefaultBranch()));
		assertNull(provider.protection(created, "other"));
	}

}
