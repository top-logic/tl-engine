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
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.migration._29423.PersonNameCollisions;
import com.top_logic.knowledge.service.migration._29423.PersonNameCollisions.Rename;
import com.top_logic.knowledge.service.migration._29423.PersonNameCollisions.Row;

/**
 * Test for {@link PersonNameCollisions} (Ticket #29423).
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestPersonNameCollisions extends TestCase {

	private static final long CURRENT = Revision.CURRENT_REV;

	/**
	 * Names that are already unique case-insensitively produce no renames.
	 */
	public void testNoCollisionNoRenames() {
		assertTrue(PersonNameCollisions.computeRenames(
			list(alive(1, "Alice", 1, CURRENT), alive(2, "bob", 1, CURRENT))).isEmpty());
	}

	/**
	 * A single account changing its own name over time (even only in case) is not a collision with
	 * itself, so it is never renamed.
	 */
	public void testSelfCaseChangeNotRenamed() {
		// Account 1 is "ADMIN" in revisions [15, 36] and "admin" from revision 37 on.
		assertTrue(PersonNameCollisions.computeRenames(
			list(alive(1, "ADMIN", 15, 36), alive(1, "admin", 37, CURRENT))).isEmpty());
	}

	/**
	 * Two different accounts sharing a name case-insensitively during an overlapping revision
	 * interval collide; the loser is renamed for its whole overlapping name version.
	 */
	public void testOverlappingAccountsLoserRenamed() {
		// Account 1 (living) keeps "admin"; the deleted account 2 collides in [20, 50].
		Map<TLID, Rename> byId = byId(PersonNameCollisions.computeRenames(
			list(alive(1, "admin", 15, CURRENT), deleted(2, "admin", 20, 50))));
		assertNull("Living keeper (account 1) is not renamed.", byId.get(id(1)));
		Rename rename = byId.get(id(2));
		assertEquals("admin2", rename.newName());
		assertEquals(20, rename.minRev());
		assertEquals(50, rename.maxRev());
	}

	/**
	 * Two different accounts with the same name in non-overlapping revision intervals do not collide
	 * and are both kept.
	 */
	public void testNonOverlappingSameNameKept() {
		assertTrue(PersonNameCollisions.computeRenames(
			list(deleted(1, "admin", 10, 20), deleted(2, "admin", 30, 40))).isEmpty());
	}

	/**
	 * Only the name version that actually overlaps the keeper is renamed; a further name version of
	 * the same loser account outside the collision keeps its name.
	 */
	public void testOnlyOverlappingVersionRenamed() {
		// Account 1 (living) holds "admin" in [10, 60]. Account 2 holds "admin" in [20, 50] (overlap)
		// and again in [100, 200] (no overlap, separated by a gap).
		List<Rename> renames = PersonNameCollisions.computeRenames(list(
			alive(1, "admin", 10, 60),
			deleted(2, "admin", 20, 50),
			deleted(2, "admin", 100, 200)));
		assertEquals("Only the overlapping version is renamed.", 1, renames.size());
		Rename rename = renames.get(0);
		assertEquals(id(2), rename.id());
		assertEquals(20, rename.minRev());
		assertEquals(50, rename.maxRev());
		assertEquals("admin2", rename.newName());
	}

	/**
	 * The externally managed (e.g. LDAP) account is kept, so its name keeps matching the directory,
	 * even if it is not the oldest account in the collision.
	 */
	public void testExternallyManagedIsKept() {
		Map<TLID, Rename> byId = byId(PersonNameCollisions.computeRenames(
			list(alive(1, "john", 1, CURRENT), managed(2, "John", 1, CURRENT))));
		assertNull("Managed keeper (account 2) keeps its name.", byId.get(id(2)));
		assertEquals("john2", byId.get(id(1)).newName());
	}

	/**
	 * A living account keeps its name even against an older deleted (history-only) account, which is
	 * renamed instead.
	 */
	public void testLivingAccountIsKept() {
		Map<TLID, Rename> byId = byId(PersonNameCollisions.computeRenames(
			list(deleted(1, "Admin", 1, 100), alive(2, "admin", 1, CURRENT))));
		assertNull("Living keeper (account 2) keeps its name.", byId.get(id(2)));
		assertEquals("Admin2", byId.get(id(1)).newName());
	}

	/**
	 * Within a collision group the keeper keeps its original spelling and the remaining members are
	 * suffixed, skipping suffixes that would collide case-insensitively.
	 */
	public void testKeeperKeepsSpellingOthersSuffixed() {
		// All three overlap fully; account 1 (smallest ordering) keeps its spelling.
		Map<TLID, Rename> byId = byId(PersonNameCollisions.computeRenames(list(
			alive(1, "Admin", 1, CURRENT),
			alive(2, "admin", 1, CURRENT),
			alive(3, "ADMIN", 1, CURRENT))));
		assertNull("Keeper 'Admin' keeps its spelling.", byId.get(id(1)));
		assertEquals("admin2", byId.get(id(2)).newName());
		// "admin2" is now taken, so "ADMIN" must skip "ADMIN2" (case-insensitively equal) to "ADMIN3".
		assertEquals("ADMIN3", byId.get(id(3)).newName());
	}

	/**
	 * Creates a living local (not externally managed) account row.
	 */
	private static Row alive(long id, String name, long minRev, long maxRev) {
		return new Row(id(id), name, minRev, maxRev, false, true);
	}

	/**
	 * Creates a deleted (history-only) local account row.
	 */
	private static Row deleted(long id, String name, long minRev, long maxRev) {
		return new Row(id(id), name, minRev, maxRev, false, false);
	}

	/**
	 * Creates a living externally managed account row.
	 */
	private static Row managed(long id, String name, long minRev, long maxRev) {
		return new Row(id(id), name, minRev, maxRev, true, true);
	}

	/**
	 * The {@link TLID} for the given numeric id.
	 */
	private static TLID id(long id) {
		return LongID.valueOf(id);
	}

	/**
	 * Collects the given rows into a list.
	 */
	private static List<Row> list(Row... rows) {
		return Arrays.asList(rows);
	}

	/**
	 * Indexes the given renames by the (unique per test) account id for easy assertion.
	 */
	private static Map<TLID, Rename> byId(List<Rename> renames) {
		Map<TLID, Rename> result = new HashMap<>();
		for (Rename rename : renames) {
			result.put(rename.id(), rename);
		}
		return result;
	}

}
