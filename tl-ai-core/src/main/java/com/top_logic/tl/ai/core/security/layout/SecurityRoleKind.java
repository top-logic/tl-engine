/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.Set;

/**
 * What kind of security role a name refers to, where an administrator picks one.
 */
/*
 * P8 of the concept: the role picker of the raw editor offers the framework's own roles next to the
 * application's, in one flat list, with nothing saying that a grant to one of them is meaningless and a
 * grant to another one reaches every account. Sorting them into three groups is the whole fix - the
 * names themselves stay exactly what the policy stores.
 *
 * Classified by name rather than by asking the engine, because the question is not "does this role
 * exist" but "does granting tools to it mean anything", and that is a property of what the framework
 * uses the role for. The list of internal names is therefore short and explicit: a role this class does
 * not know is an application role, which is the harmless default.
 */
public enum SecurityRoleKind {

	/** A role of this application, granted to accounts to give them their job. */
	APPLICATION,

	/** A role of the framework whose meaning here has to be known before granting it. */
	FRAMEWORK,

	/** A role the framework uses internally, where a tool grant means nothing. */
	INTERNAL;

	/** The module prefix of the roles the framework's security structure defines. */
	private static final String FRAMEWORK_PREFIX = "SecurityStructure.";

	/**
	 * The roles the framework assigns for parts of its own user interface.
	 *
	 * <p>
	 * They decide what a user may see and select in the application, never what an agent may call, so a
	 * tool grant to one of them reaches nobody.
	 * </p>
	 */
	private static final Set<String> INTERNAL_ROLES = Set.of("navigation", "selection",
		"PersonalProfile");

	/**
	 * The kind of the role of the given name.
	 *
	 * @param roleName
	 *        The role name, may be <code>null</code>.
	 * @return The kind, never <code>null</code>; {@link #APPLICATION} for every name this class does not
	 *         recognize.
	 */
	public static SecurityRoleKind of(String roleName) {
		if (roleName == null) {
			return APPLICATION;
		}
		if (INTERNAL_ROLES.contains(roleName)) {
			return INTERNAL;
		}
		return roleName.startsWith(FRAMEWORK_PREFIX) ? FRAMEWORK : APPLICATION;
	}

	/** The role every logged-in account holds, so that a grant to it reaches everyone. */
	public static final String EVERY_ACCOUNT = FRAMEWORK_PREFIX + "user";

	/** The role administrators hold, who bypass the tool layer anyway. */
	public static final String ADMINISTRATOR = FRAMEWORK_PREFIX + "superuser";

}
