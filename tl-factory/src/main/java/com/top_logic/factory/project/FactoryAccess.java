/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;

/**
 * The checks of the operations of the control application that change a project, its workspace or
 * the conversations with the agent of the workspace, but do not write them through the model
 * operations, which check the access rights themselves.
 */
public class FactoryAccess {

	private FactoryAccess() {
		// Static checks only.
	}

	/**
	 * Ensures that the current user may write the given object.
	 *
	 * @throws TopLogicException
	 *         If the current user may not.
	 */
	public static void checkWrite(TLObject object) {
		if (!ModelAccessRights.getInstance().isAllowed(TLContext.currentUser(), object, SimpleBoundCommandGroup.WRITE)) {
			throw new TopLogicException(I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(object));
		}
	}

	/**
	 * Ensures that the given user may write the given object, checked with the security of that
	 * user whatever context the caller runs in, e.g. on a worker thread of a chat.
	 *
	 * @param user
	 *        The user, {@code null} for none, who may write nothing.
	 * @throws TopLogicException
	 *         If the given user may not.
	 */
	public static void checkWrite(Person user, TLObject object) {
		TLContextManager.inPersonContext(user, () -> checkWrite(object));
	}

}
