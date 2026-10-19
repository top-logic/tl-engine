/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;

/**
 * Shared {@link Cleaner} of the engine that runs clean up actions for objects that became
 * unreachable.
 *
 * <p>
 * Replacement for {@link Object}'s finalization: Instead of overriding the finalize method, an
 * object registers an action that is run when the object is no longer reachable.
 * </p>
 *
 * <p>
 * Note: The registered action must not reference the registered object, neither directly nor
 * indirectly (e.g. as lambda or inner class capturing <code>this</code>). Otherwise, the object
 * never becomes unreachable and the action is never run.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResourceCleaner {

	private static final Cleaner CLEANER = Cleaner.create();

	/**
	 * Registers an action that is run when the given object becomes unreachable.
	 *
	 * @param obj
	 *        The object to watch.
	 * @param action
	 *        The action to run, when the given object becomes unreachable. Must not reference the
	 *        given object.
	 * @return The {@link Cleanable} to run the action explicitly (e.g. when the resource is closed
	 *         regularly). The action is run at most once.
	 */
	public static Cleanable register(Object obj, Runnable action) {
		return CLEANER.register(obj, action);
	}

}
