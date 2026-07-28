/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration._29423;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.LongID;
import com.top_logic.knowledge.service.migration._29423.PersonNameCollisions;
import com.top_logic.knowledge.service.migration._29423.PersonNameCollisions.Account;
import com.top_logic.knowledge.service.migration._29423.PersonNameCollisions.Rename;

/**
 * Test for {@link PersonNameCollisions} (Ticket #29423).
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestPersonNameCollisions extends TestCase {

	/**
	 * Names that are already unique case-insensitively produce no renames.
	 */
	public void testNoCollisionNoRenames() {
		assertTrue(PersonNameCollisions.computeRenames(
			list(account(1, "Alice"), account(2, "bob"))).isEmpty());
	}

	/**
	 * Within a collision group the keeper keeps its original spelling and the remaining members are
	 * suffixed, skipping suffixes that would collide case-insensitively.
	 */
	public void testKeeperKeepsSpellingOthersSuffixed() {
		// No externally managed member -> oldest (id 1) keeps its spelling.
		Map<String, String> byOldName = index(PersonNameCollisions.computeRenames(
			list(account(1, "Admin"), account(2, "admin"), account(3, "ADMIN"))));
		assertNull("Keeper 'Admin' keeps its spelling.", byOldName.get("Admin"));
		assertEquals("admin2", byOldName.get("admin"));
		// "admin2" is now taken, so "ADMIN" must skip "ADMIN2" (case-insensitively equal) to "ADMIN3".
		assertEquals("ADMIN3", byOldName.get("ADMIN"));
	}

	/**
	 * The externally managed (e.g. LDAP) account is kept, so its name keeps matching the directory,
	 * even if it is not the oldest account in the group.
	 */
	public void testExternallyManagedIsKept() {
		// The externally managed account (id 2) keeps its name; the local one is renamed.
		Map<String, String> byOldName = index(PersonNameCollisions.computeRenames(
			list(account(1, "john"), managed(2, "John"))));
		assertNull("Managed keeper 'John' keeps its name.", byOldName.get("John"));
		assertEquals("john2", byOldName.get("john"));
	}

	/**
	 * A generated suffixed name that already exists (compared case-insensitively) is skipped in
	 * favor of the next free suffix.
	 */
	public void testSuffixSkipsTakenNamesCaseInsensitively() {
		// "john2" already exists -> the renamed collision member must become "John3".
		Map<String, String> byOldName = index(PersonNameCollisions.computeRenames(
			list(account(1, "john"), account(2, "John"), account(3, "john2"))));
		assertNull("Keeper 'john' is unchanged.", byOldName.get("john"));
		assertNull("'john2' has no collision.", byOldName.get("john2"));
		assertEquals("John3", byOldName.get("John"));
	}

	/**
	 * Creates a local (not externally managed) account whose id doubles as its ordering.
	 */
	private static Account account(long id, String name) {
		return new Account(LongID.valueOf(id), name, id, false);
	}

	/**
	 * Creates an externally managed account whose id doubles as its ordering.
	 */
	private static Account managed(long id, String name) {
		return new Account(LongID.valueOf(id), name, id, true);
	}

	/**
	 * Collects the given accounts into a list.
	 */
	private static List<Account> list(Account... accounts) {
		return Arrays.asList(accounts);
	}

	/**
	 * Indexes the given renames by their (unique) old name for easy assertion.
	 */
	private static Map<String, String> index(List<Rename> renames) {
		Map<String, String> result = new HashMap<>();
		for (Rename rename : renames) {
			result.put(rename.getOldName(), rename.getNewName());
		}
		return result;
	}

}
