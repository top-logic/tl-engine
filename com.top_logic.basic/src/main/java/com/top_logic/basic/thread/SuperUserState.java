/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import java.util.EmptyStackException;

/**
 * Temporarily set super user state.
 * 
 * <pre>
 * pushSuperUser();
 * try {
 *  ...
 * } finally {
 *   popSuperUser();
 * }
 * </pre>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SuperUserState {

	/**
	 * Reset the super user mode. Access restrictions apply for the current user in the current
	 * thread.
	 * 
	 * Use only to finally reset the SuperUser, normally you should use something like
	 * 
	 * <pre>
	 * pushSuperUser();
	 * try {
	 *  ...
	 * } finally {
	 *   popSuperUser();
	 * }
	 * </pre>
	 * 
	 * This way you can nest SuperUserContexts that do not influence each other.
	 */
	void resetSuperUser();

	/**
	 * Set the super user mode one higher than it was before.
	 * 
	 * The next call to {@link #popSuperUser()} will result in the same state as before.
	 */
	void pushSuperUser();

	/**
	 * Reset the super user to the mode before the last call to {@link #pushSuperUser()}.
	 * 
	 * @throws EmptyStackException
	 *         in case SuperUserMode was not set before.
	 */
	void popSuperUser();

	/**
	 * Checks whether the superuser is currently set
	 */
	boolean isSuperUser();

}
