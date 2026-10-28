/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import com.top_logic.basic.util.ResKey;

/**
 * What the credentials of a {@link GitHostingProvider} may do in its
 * {@link GitHostingProvider#getOwner() owner}, as the hosting service reports it.
 *
 * @param createRepositories
 *        Whether the credentials may create repositories in the owner.
 * @param protectBranches
 *        Whether the credentials may administer the repositories of the owner, e.g. protect their
 *        branches. A single repository tells it for itself, see
 *        {@link GitRepository#canAdminister()}.
 * @param problem
 *        Why the hosting service could not be asked, {@code null} if it answered. With a problem,
 *        the credentials are assumed to be allowed nothing.
 *
 * @see GitHostingProvider#capabilities()
 */
public record GitCapabilities(boolean createRepositories, boolean protectBranches, ResKey problem) {

	/**
	 * The capabilities of credentials that may create and administer repositories in the owner.
	 */
	public static final GitCapabilities ALL = new GitCapabilities(true, true, null);

	/**
	 * The capabilities of credentials that may neither create nor administer repositories in the
	 * owner.
	 */
	public static final GitCapabilities NONE = new GitCapabilities(false, false, null);

	/**
	 * The capabilities of credentials whose rights the hosting service could not tell.
	 *
	 * @param problem
	 *        Why it could not.
	 */
	public static GitCapabilities unknown(ResKey problem) {
		return new GitCapabilities(false, false, problem);
	}

}
