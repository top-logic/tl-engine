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
	 */
	public static final class Account {

		private final TLID _id;

		private final String _name;

		private final long _ordering;

		private final boolean _externallyManaged;

		/**
		 * Creates an {@link Account}.
		 *
		 * @param id
		 *        The account's technical identifier.
		 * @param name
		 *        The account's current login name.
		 * @param ordering
		 *        A stable tie-breaker; the smallest value wins when choosing the keeper.
		 * @param externallyManaged
		 *        Whether the account is managed by an external directory (e.g. LDAP); such an
		 *        account is preferred as keeper so its name keeps matching the directory.
		 */
		public Account(TLID id, String name, long ordering, boolean externallyManaged) {
			_id = id;
			_name = name;
			_ordering = ordering;
			_externallyManaged = externallyManaged;
		}

		/** The account's technical identifier. */
		public TLID getId() {
			return _id;
		}

		/** The account's current login name. */
		public String getName() {
			return _name;
		}

		/** A stable tie-breaker; the smallest value wins when choosing the keeper. */
		public long getOrdering() {
			return _ordering;
		}

		/** Whether the account is managed by an external directory. */
		public boolean isExternallyManaged() {
			return _externallyManaged;
		}
	}

	/**
	 * A planned rename of one account.
	 */
	public static final class Rename {

		private final TLID _id;

		private final String _oldName;

		private final String _newName;

		/**
		 * Creates a {@link Rename}.
		 */
		public Rename(TLID id, String oldName, String newName) {
			_id = id;
			_oldName = oldName;
			_newName = newName;
		}

		/** The identifier of the account to rename. */
		public TLID getId() {
			return _id;
		}

		/** The account's current name. */
		public String getOldName() {
			return _oldName;
		}

		/** The name the account is renamed to. */
		public String getNewName() {
			return _newName;
		}
	}

	/**
	 * Computes the renames that make all account names unique case-insensitively while keeping
	 * their original spelling.
	 *
	 * <p>
	 * Accounts are grouped by their lower-cased name. Within a group the keeper is the
	 * {@link Account#isExternallyManaged() externally managed} account (else the one with the
	 * smallest {@link Account#getOrdering() ordering}); the keeper is never renamed. Each other
	 * member keeps its own spelling plus the smallest integer suffix that is free (compared
	 * case-insensitively).
	 * </p>
	 *
	 * @return One {@link Rename} per account that must be renamed; empty if there are no
	 *         collisions. Idempotent: collision-free input yields an empty list.
	 */
	public static List<Rename> computeRenames(List<Account> accounts) {
		Map<String, List<Account>> groups = new LinkedHashMap<>();
		for (Account account : accounts) {
			groups.computeIfAbsent(lower(account.getName()), x -> new ArrayList<>()).add(account);
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
				String target = nextFreeName(account.getName(), used);
				used.add(lower(target));
				renames.add(new Rename(account.getId(), account.getName(), target));
			}
		}
		return renames;
	}

	private static Account chooseKeeper(List<Account> group) {
		Account managed = null;
		Account oldest = null;
		for (Account account : group) {
			if (account.isExternallyManaged()
				&& (managed == null || account.getOrdering() < managed.getOrdering())) {
				managed = account;
			}
			if (oldest == null || account.getOrdering() < oldest.getOrdering()) {
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
