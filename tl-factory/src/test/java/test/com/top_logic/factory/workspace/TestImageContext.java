/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace;

import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

import com.top_logic.factory.workspace.ImageContext;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;

/**
 * Test of the content hash of {@link ImageContext} and of the workspace image context of
 * {@link ApplicationWorkspaces}.
 */
public class TestImageContext extends TestCase {

	/**
	 * The hash depends on the content only, not on the order in which files are added.
	 */
	public void testHashIndependentOfOrder() {
		String hash1 = new ImageContext().add("a", "1").add("b/c", "2").contentHash();
		String hash2 = new ImageContext().add("b/c", "2").add("a", "1").contentHash();
		assertEquals(hash1, hash2);
		assertEquals(ImageContext.HASH_LENGTH, hash1.length());
		assertTrue(hash1, hash1.matches("[0-9a-f]+"));
	}

	/**
	 * Changing a content, a path or the split between path and content changes the hash.
	 */
	public void testHashChanges() {
		String base = new ImageContext().add("a", "1").add("b", "2").contentHash();
		assertFalse(base.equals(new ImageContext().add("a", "1").add("b", "3").contentHash()));
		assertFalse(base.equals(new ImageContext().add("a", "1").add("c", "2").contentHash()));
		assertFalse(base.equals(new ImageContext().add("a", "1").contentHash()));
		assertFalse(new ImageContext().add("ab", "c").contentHash()
			.equals(new ImageContext().add("a", "bc").contentHash()));
	}

	/**
	 * A path leaving the context is rejected.
	 */
	public void testInvalidPaths() {
		for (String path : new String[] { "", "/abs", "../up", "a/../b", "a//b", ".", "a/." }) {
			try {
				new ImageContext().add(path, "x");
				fail("Accepted path '" + path + "'.");
			} catch (IllegalArgumentException ex) {
				// Expected.
			}
		}
		new ImageContext().add(".hidden/file.txt", "x");
	}

	/**
	 * The workspace image context contains the image files, with the repository URL in the Maven
	 * settings, and its hash depends on the repository URL.
	 */
	public void testWorkspaceImageContext() {
		String url = "https://repo.example.com/maven/?a=1&b=2";
		ImageContext context = ApplicationWorkspaces.imageContext(url);
		assertTrue(context.getFiles().containsKey(ImageContext.DOCKERFILE));
		assertTrue(context.getFiles().containsKey(ApplicationWorkspaces.ENTRY_SCRIPT));
		String settings =
			new String(context.getFiles().get(ApplicationWorkspaces.SETTINGS_FILE), StandardCharsets.UTF_8);
		assertTrue(settings, settings.contains("https://repo.example.com/maven/?a=1&amp;b=2"));
		assertFalse(settings, settings.contains(ApplicationWorkspaces.REPOSITORY_URL_PLACEHOLDER));

		assertEquals(context.contentHash(), ApplicationWorkspaces.imageContext(url).contentHash());
		assertFalse(context.contentHash()
			.equals(ApplicationWorkspaces.imageContext("https://other.example.com/").contentHash()));
	}

}
