/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.access;

import java.util.Optional;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;

/**
 * Function that decides, whether a given object can be deleted.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DeleteConstraint {

	/**
	 * Checks whether the given object can be deleted.
	 * 
	 * <p>
	 * A deletion veto is signaled by returning a non-<code>null</code> {@link ResKey} describing
	 * the reason, why the given object cannot be deleted.
	 * </p>
	 *
	 * @param obj
	 *        The object in question.
	 * @return If the given object cannot be deleted, a reason why deletion is not possible,
	 *         {@link Optional#empty()} otherwise.
	 */
	Optional<ResKey> getDeleteVeto(TLObject obj);

}
