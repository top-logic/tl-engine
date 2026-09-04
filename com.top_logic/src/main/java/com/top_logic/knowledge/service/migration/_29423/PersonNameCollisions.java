/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration._29423;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.TLID;

/**
 * Decides which account names must be renamed so that, at every point in time, no two distinct
 * accounts share the same name case-insensitively, while keeping their original spelling (Ticket
 * #29423).
 *
 * <p>
 * Accounts are stored as versioned rows: one account (one {@link Row#id() identifier}) may carry
 * several name versions over its history, each valid in a revision interval
 * {@code [minRev, maxRev]}. A single account changing its own name over time (even only in case,
 * e.g. {@code ADMIN} then {@code admin}) is never a collision with itself. A collision exists only
 * between two <em>different</em> accounts whose names are equal case-insensitively during an
 * <em>overlapping</em> revision interval.
 * </p>
 *
 * <p>
 * This is pure, database-independent logic so that the case-collision handling can be unit-tested
 * (the test databases use a case-insensitive collation and cannot even hold a collision).
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class PersonNameCollisions {

	/**
	 * One versioned row of an account's name for the collision analysis.
	 *
	 * <p>
	 * The account-level properties ({@link #externallyManaged()}, {@link #alive()}) are the same for
	 * every row of the same {@link #id()}.
	 * </p>
	 *
	 * @param id
	 *        The account's technical identifier; also the tie-breaker for choosing the keeper (the
	 *        smallest id wins).
	 * @param name
	 *        The account's login name in this revision interval.
	 * @param minRev
	 *        First revision (inclusive) in which the name is valid.
	 * @param maxRev
	 *        Last revision (inclusive) in which the name is valid; the current revision for a row
	 *        that is still valid.
	 * @param externallyManaged
	 *        Whether the account is managed by an external directory (e.g. LDAP); such an account is
	 *        preferred as keeper so its name keeps matching the directory.
	 * @param alive
	 *        Whether the account currently exists (has a current revision). Living accounts are
	 *        preferred as keeper, so a deleted (history-only) account never forces the rename of a
	 *        living one.
	 */
	public record Row(TLID id, String name, long minRev, long maxRev, boolean externallyManaged, boolean alive) {
		// Data record.
	}

	/**
	 * A planned rename of one name version of an account.
	 *
	 * <p>
	 * The rename applies exactly to the revision interval {@code [minRev, maxRev]} of the affected
	 * account, so that other name versions of the same account (and revisions outside the collision)
	 * keep their original name.
	 * </p>
	 *
	 * @param id
	 *        The identifier of the account to rename.
	 * @param minRev
	 *        First revision (inclusive) the rename applies to.
	 * @param maxRev
	 *        Last revision (inclusive) the rename applies to.
	 * @param oldName
	 *        The account's name in that interval.
	 * @param newName
	 *        The name the account is renamed to in that interval.
	 */
	public record Rename(TLID id, long minRev, long maxRev, String oldName, String newName) {
		// Data record.
	}

	/**
	 * A maximal run of consecutive rows of one account with the same (case-insensitive) name.
	 */
	private record Segment(TLID id, String name, long minRev, long maxRev, boolean externallyManaged,
			boolean alive) {
		// Data record.
	}

	/**
	 * Computes the renames that make all account names unique case-insensitively at every point in
	 * time, while keeping their original spelling.
	 *
	 * <p>
	 * Consecutive rows of the same account with the same lower-cased name are coalesced into one name
	 * version. Name versions are grouped by their lower-cased name; within a group, versions of
	 * higher-priority accounts claim their revision intervals first. A version whose interval overlaps
	 * an already claimed interval must be renamed (it keeps its own spelling plus the smallest integer
	 * suffix that is free, compared case-insensitively); a non-overlapping version keeps its name. The
	 * keeper priority prefers an {@link Row#alive() living} account, then an
	 * {@link Row#externallyManaged() externally managed} one, then the smallest {@link Row#id() id}.
	 * </p>
	 *
	 * @return One {@link Rename} per name version that must be renamed; empty if there are no
	 *         collisions. Idempotent: collision-free input yields an empty list.
	 */
	public static List<Rename> computeRenames(List<Row> rows) {
		List<Segment> segments = coalesce(rows);

		// Group name versions by their lower-cased name.
		Map<String, List<Segment>> groups = new LinkedHashMap<>();
		for (Segment segment : segments) {
			groups.computeIfAbsent(lower(segment.name()), x -> new ArrayList<>()).add(segment);
		}

		// All lower-cased names that are already claimed, to avoid secondary collisions.
		Set<String> used = new HashSet<>(groups.keySet());

		List<Rename> renames = new ArrayList<>();
		for (List<Segment> group : groups.values()) {
			resolveGroup(group, used, renames);
		}
		return renames;
	}

	/**
	 * Coalesces consecutive rows of the same account with the same lower-cased name into
	 * {@link Segment}s (one name version), preserving the spelling of the first such row.
	 */
	private static List<Segment> coalesce(List<Row> rows) {
		Map<TLID, List<Row>> byAccount = new LinkedHashMap<>();
		for (Row row : rows) {
			byAccount.computeIfAbsent(row.id(), x -> new ArrayList<>()).add(row);
		}

		List<Segment> segments = new ArrayList<>();
		for (List<Row> accountRows : byAccount.values()) {
			accountRows.sort((a, b) -> Long.compare(a.minRev(), b.minRev()));

			Row start = null;
			long maxRev = 0;
			for (Row row : accountRows) {
				boolean sameVersion = start != null
					&& lower(start.name()).equals(lower(row.name()))
					&& row.minRev() <= maxRev + 1;
				if (sameVersion) {
					maxRev = Math.max(maxRev, row.maxRev());
				} else {
					if (start != null) {
						segments.add(segment(start, maxRev));
					}
					start = row;
					maxRev = row.maxRev();
				}
			}
			if (start != null) {
				segments.add(segment(start, maxRev));
			}
		}
		return segments;
	}

	private static Segment segment(Row start, long maxRev) {
		return new Segment(start.id(), start.name(), start.minRev(), maxRev,
			start.externallyManaged(), start.alive());
	}

	/**
	 * Resolves the collisions within one lower-cased-name group: keeps the highest-priority
	 * non-overlapping name versions and renames the overlapping ones.
	 */
	private static void resolveGroup(List<Segment> group, Set<String> used, List<Rename> renames) {
		group.sort(PersonNameCollisions::byPriority);

		List<long[]> kept = new ArrayList<>();
		for (Segment segment : group) {
			if (overlapsKept(segment, kept)) {
				String target = nextFreeName(segment.name(), used);
				used.add(lower(target));
				renames.add(new Rename(segment.id(), segment.minRev(), segment.maxRev(), segment.name(), target));
			} else {
				kept.add(new long[] { segment.minRev(), segment.maxRev() });
			}
		}
	}

	private static boolean overlapsKept(Segment segment, List<long[]> kept) {
		for (long[] interval : kept) {
			if (segment.minRev() <= interval[1] && interval[0] <= segment.maxRev()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Orders name versions so that the preferred keeper comes first: a living account before a
	 * deleted one, then an externally managed account before a local one, then the smaller id
	 * (finally the earlier interval, for a stable result).
	 */
	private static int byPriority(Segment a, Segment b) {
		if (a.alive() != b.alive()) {
			return a.alive() ? -1 : 1;
		}
		if (a.externallyManaged() != b.externallyManaged()) {
			return a.externallyManaged() ? -1 : 1;
		}
		int byId = a.id().compareTo(b.id());
		if (byId != 0) {
			return byId;
		}
		return Long.compare(a.minRev(), b.minRev());
	}

	private static String nextFreeName(String base, Set<String> used) {
		int suffix = 2;
		String candidate = base + suffix;
		while (used.contains(lower(candidate))) {
			suffix++;
			candidate = base + suffix;
		}
		return candidate;
	}

	private static String lower(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	private PersonNameCollisions() {
		// Utility class.
	}
}
