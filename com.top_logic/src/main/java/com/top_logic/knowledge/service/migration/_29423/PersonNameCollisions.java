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
 * Decides which account names must be renamed so that no two accounts share the same name
 * case-insensitively, while keeping their original spelling (Ticket #29423).
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
	 * An account whose name participates in the collision analysis.
	 *
	 * @param id
	 *        The account's technical identifier.
	 * @param name
	 *        The account's login name (constant over the account's lifetime).
	 * @param ordering
	 *        A stable tie-breaker; the smallest value wins when choosing the keeper.
	 * @param externallyManaged
	 *        Whether the account is managed by an external directory (e.g. LDAP); such an account is
	 *        preferred as keeper so its name keeps matching the directory.
	 * @param alive
	 *        Whether the account currently exists (has a current revision). Living accounts are
	 *        preferred as keeper, so a deleted (history-only) account never forces the rename of a
	 *        living one.
	 */
	public record Account(TLID id, String name, long ordering, boolean externallyManaged, boolean alive) {
		// Data record.
	}

	/**
	 * A planned rename of one account.
	 *
	 * @param id
	 *        The identifier of the account to rename.
	 * @param oldName
	 *        The account's current name.
	 * @param newName
	 *        The name the account is renamed to.
	 */
	public record Rename(TLID id, String oldName, String newName) {
		// Data record.
	}

	/**
	 * Computes the renames that make all account names unique case-insensitively while keeping
	 * their original spelling.
	 *
	 * <p>
	 * Accounts are grouped by their lower-cased name. Within a group the keeper is chosen preferring
	 * a {@link Account#alive() living} account, then an {@link Account#externallyManaged() externally
	 * managed} one, then the one with the smallest {@link Account#ordering() ordering}; the keeper is
	 * never renamed. Each other member keeps its own spelling plus the smallest integer suffix that
	 * is free (compared case-insensitively).
	 * </p>
	 *
	 * @return One {@link Rename} per account that must be renamed; empty if there are no
	 *         collisions. Idempotent: collision-free input yields an empty list.
	 */
	public static List<Rename> computeRenames(List<Account> accounts) {
		Map<String, List<Account>> groups = new LinkedHashMap<>();
		for (Account account : accounts) {
			groups.computeIfAbsent(lower(account.name()), x -> new ArrayList<>()).add(account);
		}

		// All lower-cased names that are already claimed, to avoid secondary collisions.
		Set<String> used = new HashSet<>(groups.keySet());

		List<Rename> renames = new ArrayList<>();
		for (List<Account> group : groups.values()) {
			if (group.size() == 1) {
				continue;
			}
			Account keeper = chooseKeeper(group);
			for (Account account : group) {
				if (account == keeper) {
					continue;
				}
				String target = nextFreeName(account.name(), used);
				used.add(lower(target));
				renames.add(new Rename(account.id(), account.name(), target));
			}
		}
		return renames;
	}

	private static Account chooseKeeper(List<Account> group) {
		// Prefer a living account: a deleted (history-only) account must never force the rename of
		// a living one.
		boolean anyAlive = false;
		for (Account account : group) {
			if (account.alive()) {
				anyAlive = true;
				break;
			}
		}

		Account managed = null;
		Account oldest = null;
		for (Account account : group) {
			if (anyAlive && !account.alive()) {
				continue;
			}
			if (account.externallyManaged() && (managed == null || account.ordering() < managed.ordering())) {
				managed = account;
			}
			if (oldest == null || account.ordering() < oldest.ordering()) {
				oldest = account;
			}
		}
		return managed != null ? managed : oldest;
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
