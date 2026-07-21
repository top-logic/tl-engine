/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket29221;

/**
 * Combined migration for ticket #29221.
 *
 * <p>
 * Runs both migration steps of the ticket in one invocation:
 * </p>
 * <ol>
 * <li>{@link RemoveSecurityDomain} over the layout directory, dropping the no longer supported
 * {@code securityDomain} configuration.</li>
 * <li>{@link AddRoleRuleId} over the configuration directory, assigning an {@code id} to role-rule
 * configurations that do not have one yet.</li>
 * </ol>
 *
 * <p>
 * The two steps keep separate directories on purpose: {@link RemoveSecurityDomain} rewrites (and
 * pretty-prints) every file it visits, so it must stay scoped to the already-normalized layout
 * files, while {@link AddRoleRuleId} only rewrites files it actually changes and can therefore
 * safely scan the whole configuration tree.
 * </p>
 */
public class MigrateTicket29221 {

	/**
	 * Entry-point of the combined {@code #29221} migration.
	 *
	 * @param args
	 *        {@code args[0]} is the layout directory (for {@link RemoveSecurityDomain}),
	 *        {@code args[1]} is the configuration directory (for {@link AddRoleRuleId}).
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: " + MigrateTicket29221.class.getName() + " <layoutDir> <configDir>");
			return;
		}

		new RemoveSecurityDomain().runMain(new String[] { args[0] });
		new AddRoleRuleId().runMain(new String[] { args[1] });
	}

}
