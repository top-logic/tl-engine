/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import com.top_logic.layout.basic.Command;

/**
 * Component capability that allows to apply and cancel.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CanApply {

	/**
	 * Returns a {@link Command} which is used to make the changes in this {@link ChangeHandler}
	 * persistent.
	 * 
	 * @return Must be <code>null</code> if the changes can not be made persistent, or
	 *         {@link #getDiscardClosure()} returns <code>null</code>.
	 * 
	 * @see #getDiscardClosure()
	 */
	public Command getApplyClosure();

	/**
	 * A {@link Command} which is used to discard the changes which currently exists in this
	 * {@link ChangeHandler}.
	 * 
	 * <p>
	 * If <code>null</code> is returned, then also {@link #getApplyClosure()} must return
	 * <code>null</code>.
	 * </p>
	 * 
	 * @see ChangeHandler#getApplyClosure()
	 */
	public Command getDiscardClosure();

}
